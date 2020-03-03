/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2014 Eli Lilly and Company Limited
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * ------------------------------------------------------------------------
*/
package org.erlwood.knime.utils.debugging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.erlwood.knime.utils.gui.layout.TableLayout;

/**
 * Frame to display memory snapshots.
 */
@SuppressWarnings("serial")
public class MemorySnapshots  extends JFrame  {
	/** Date formatter. */
	private static final SimpleDateFormat SDF = new SimpleDateFormat();
	
	/** Snapshot table model. */
	private MemorySnapshotsTableModel memorySnapshotTableModel = new MemorySnapshotsTableModel();
	
	/** Table for snapshots. */
	private JTable table;
	
	/** Memory MX bean for collecting usage stats. */
	private MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
	
	/** 1 second timer for getting memory usage. */
	private Timer timer = new Timer();
	
	/** Current snapshot record. */
	private MemorySnapshotRecord currentSnapshot = null;
	
	/** Toolbar buttons. */
	private JButton cmdRecord = new JButton("Start Snapshot");
	private JButton cmdStopRecord = new JButton("Stop Snapshot");
	private JButton cmdClear = new JButton("Clear All Snapshots");
	
	/**
	 * Constructor.
	 */
	public MemorySnapshots() {
		setSize(800, 300);
		setTitle("Memory Snapshots");
		setContentPane(createMainPanel());	
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				setVisible(false);
			}
		});
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
		timer.scheduleAtFixedRate(new GetMemoryUsageTask(), 0, 1000);
	}
	

	/** 
	 * @return Creates and returns the main panel.
	 */
	private JPanel createMainPanel() {
		JPanel pnl = new JPanel();
		
		pnl.setLayout(new BorderLayout());
		
		pnl.add(createToolbarPanel(), BorderLayout.NORTH);
		pnl.add(createCenterPanel(), BorderLayout.CENTER);
		return pnl;
	}
	

	/** 
	 * @return Creates and returns the toolbar panel.
	 */
	private JPanel createToolbarPanel() {
		JPanel pnl = new JPanel(new TableLayout(new double[][]{{TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}, {TableLayout.PREFERRED}}));
		
		
		pnl.add(cmdRecord, 		"0,0");
		pnl.add(cmdStopRecord, 	"2,0");
		pnl.add(cmdClear, 		"4,0");
				
		
		cmdRecord.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				startRecord();
			}
		});
		
		cmdStopRecord.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				stopRecord();
			}
		});

		
		cmdClear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clearAllSnapshots();				
			}
		});
		
		cmdStopRecord.setEnabled(false);
		
		pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		return pnl;
	}
	
	/**
	 * Starts the snapshot recording.
	 */
	private void startRecord() {
		currentSnapshot = new MemorySnapshotRecord(mxBean.getHeapMemoryUsage());
		memorySnapshotTableModel.addSnapshot(currentSnapshot);
		cmdStopRecord.setEnabled(true);
		cmdRecord.setEnabled(false);
	}
	
	/**
	 * Stops the snapshot recording.
	 */
	private void stopRecord() {
		if (currentSnapshot == null) {
			return;
		}
		currentSnapshot.stop();
		currentSnapshot = null;
		cmdStopRecord.setEnabled(false);
		cmdRecord.setEnabled(true);
		memorySnapshotTableModel.fireTableDataChanged();
	
	}
	
	/**
	 * Clears all the snapshots.
	 */
	private void clearAllSnapshots() {
		stopRecord();
		memorySnapshotTableModel.clear();
	}


	/** 
	 * @return Creates and returns the center panel.
	 */
	private JPanel createCenterPanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.FILL}}));
		
		pnl.add(createTablePanel(), 	"0,0");
				
		pnl.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		return pnl;
	}
	
	/** 
	 * @return Creates and returns the table panel.
	 */
	private JPanel createTablePanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		
		table = new JTable(memorySnapshotTableModel);		
		JScrollPane scp = new JScrollPane(table);
		scp.getViewport().setBackground(Color.white);
		pnl.add(scp);
		return pnl;
	}
	
	/**
	 * Memory Snapshot table model.
	 * @author Luke Bullard
	 *
	 */
	private class MemorySnapshotsTableModel extends AbstractTableModel {
		/** The columns. */
		private String[]	columnNames = new String[] {"Time Started", "Time Ended", "Committed Start", "Used Start", "Committed End", "Used End", "Committed Max", "Used Max", "Used Diff"};
		
		/** The list of snapshots. */
		private List<MemorySnapshotRecord> rows = new ArrayList<MemorySnapshotRecord>();
		
		/**
		 * Adds a new snapshot to the list.
		 * @param ss The snapshot to add.
		 */
		public void addSnapshot(final MemorySnapshotRecord ss) {
			//	Ensure we are running in the EDT.
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						addSnapshot(ss);
					}				
				});
				return;
			}
			rows.add(ss);
			fireTableDataChanged();			
		}

		/**
		 * Clears all the snapshots.
		 */
		public void clear() {
			//	Ensure we are running in the EDT.
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						clear();
					}				
				});
				return;
			}
					
			rows.clear();
			fireTableDataChanged();			
		}
		
		@Override
		public String getColumnName(int index) {
			return columnNames[index];
		}
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			MemorySnapshotRecord ner = rows.get(row);
					
			switch(col) {
				case 0:
					return SDF.format(new Date(ner.getStartTime()));				
				case 1:
					long t = ner.getEndTime();
					if (t == 0) {
						return null;
					}
					return SDF.format(new Date(t));				
				case 2:
					return MemoryProfiler.getByteShorthand(ner.getCommittedStart());				
				case 3:
					return MemoryProfiler.getByteShorthand(ner.getUsedStart());								
				case 4:
					return MemoryProfiler.getByteShorthand(ner.getCommittedEnd());				
				case 5:
					return MemoryProfiler.getByteShorthand(ner.getUsedEnd());
				case 6:
					return MemoryProfiler.getByteShorthand(ner.getCommittedMax());
				case 7:
					return MemoryProfiler.getByteShorthand(ner.getUsedMax());
				case 8:
					return MemoryProfiler.getByteShorthand(ner.getUsedDiff());
			}
			return null;
		}
		
	}
	

	/**
	 * Snapshot record. Created when a snapshot begins and records data until
	 * stop() is called.
	 * @author Luke Bullard
	 *
	 */
	private class MemorySnapshotRecord {
		private final long startTime;		
		private final long committedStart;
		private final long usedStart;
		
		private long endTime;
		private long committedEnd;
		private long usedEnd;
		private long committedMax;
		private long usedMax;
		private long usedDiff;

		public MemorySnapshotRecord(MemoryUsage u) {
			startTime 		= System.currentTimeMillis();
			committedStart 	= u.getCommitted();
			usedStart 		= u.getUsed();
		}
		
		public void stop() {
			endTime = System.currentTimeMillis();
		}
		
		/**
		 * Sets the usage data.
		 * @param u The MemoryUsage to use
		 */
		public void setUsage(MemoryUsage u) {
			committedEnd 	= u.getCommitted();
			usedEnd 		= u.getUsed();
			committedMax 	= Math.max(committedMax, committedEnd);
			usedMax 		= Math.max(usedMax, usedEnd);
			
			usedDiff = usedMax - usedStart;
		}
		
		public long getEndTime() {
			return endTime;
		}
		
		public long getCommittedEnd() {
			return committedEnd;
		}

		public long getUsedEnd() {
			return usedEnd;
		}

		public long getCommittedMax() {
			return committedMax;
		}

		public long getUsedMax() {
			return usedMax;
		}

		public long getUsedDiff() {
			return usedDiff;
		}

		public long getStartTime() {
			return startTime;
		}

		public long getCommittedStart() {
			return committedStart;
		}

		public long getUsedStart() {
			return usedStart;
		}

		

	}

	/**
	 * Task to collect the usage and distribute to the current
	 * snapshot (if available).
	 * @author Luke Bullard
	 *
	 */
	private class GetMemoryUsageTask extends TimerTask {

		@Override
		public void run() {
			if (currentSnapshot != null) {
				currentSnapshot.setUsage(mxBean.getHeapMemoryUsage());
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						memorySnapshotTableModel.fireTableDataChanged();
					}				
				});
			}		
		}
		
	}
	
}



