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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import org.knime.core.node.workflow.NodeID;

import org.erlwood.knime.ErlwoodNodeStateChangeListener;
import org.erlwood.knime.ErlwoodNodeStateChangeListener.NodeExecutionListener;

/**
 * Frame to display node execution stats.
 */
public class NodeExecutionStatistics  extends JFrame implements NodeExecutionListener {
	private static final DecimalFormat DF = new DecimalFormat("0.00%");
	
	private RawTableModel rawTableModel = new RawTableModel();
	private AggregateTableModel aggregateTableModel = new AggregateTableModel();
	private JTable rawTable;
	private JTable aggregateTable;
	
	public NodeExecutionStatistics() {
		setSize(500, 500);
		setTitle("Node Execution Statistics");
		setContentPane(createMainPanel());	
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				setVisible(false);
			}
		});
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	private JPanel createMainPanel() {
		JPanel pnl = new JPanel();
		
		pnl.setLayout(new BorderLayout());
		
		pnl.add(createToolbarPanel(), BorderLayout.NORTH);
		pnl.add(createCenterPanel(), BorderLayout.CENTER);
		return pnl;
	}
	
	private JPanel createToolbarPanel() {
		JPanel pnl = new JPanel();
		
		JButton clear = new JButton("Clear");
		pnl.add(clear);
		
		clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rawTableModel.clear();
				aggregateTableModel.clear();
			}
		});
		
		final JCheckBox chkCollect = new JCheckBox("Collect Data");
		pnl.add(chkCollect);
		
		chkCollect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (chkCollect.isSelected()) {
					ErlwoodNodeStateChangeListener.getInstance().addNodeExecutionListener(NodeExecutionStatistics.this);
				} else {
					ErlwoodNodeStateChangeListener.getInstance().removeNodeExecutionListener(NodeExecutionStatistics.this);
				}
			}
		});
		
		return pnl;
	}
	
	private JPanel createCenterPanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		
		
		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Raw", createRawPanel());
		tab.addTab("Aggregate", createAggregatePanel());
		pnl.add(tab, BorderLayout.CENTER);
				
		return pnl;
	}
	
	private JPanel createRawPanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		
		rawTable = new JTable(rawTableModel);
		rawTable.setRowSorter(new TableRowSorter(rawTableModel));
		JScrollPane scp = new JScrollPane(rawTable);
		scp.getViewport().setBackground(Color.white);
		pnl.add(scp);
		return pnl;
	}
	
	private JPanel createAggregatePanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		
		aggregateTable = new JTable(aggregateTableModel);
		TableRowSorter trs = new TableRowSorter(aggregateTableModel);		
		aggregateTable.setRowSorter(trs);
		
		JScrollPane scp = new JScrollPane(aggregateTable);
		scp.getViewport().setBackground(Color.white);
		pnl.add(scp);
		return pnl;
	}
	
	private class RawTableModel extends AbstractTableModel {
		private String[]	columnNames = new String[] {"Name", "Id", "Execution Time", "IO Time"};
		private List<NodeExecutionRecord> rows = new ArrayList<NodeExecutionRecord>();
		
		void addNodeExecution(String name, NodeID id, long executionTime, long ioTiming) {
			rows.add(new NodeExecutionRecord(name, id, executionTime, ioTiming));
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fireTableDataChanged();
				}				
			});
			
		}
		
		public void clear() {
			rows.clear();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fireTableDataChanged();
				}				
			});
		}

		@Override
		public Class<?> getColumnClass(int index) {
			switch(index) {
				case 0:
					return String.class;
				case 1:
					return String.class;
				case 2:
					return Long.class;
				case 3:
					return Long.class;
			}			
			return null;
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
			NodeExecutionRecord ner = rows.get(row);
			switch(col) {
			case 0:
				return ner.getName();				
			case 1:
				return ner.getId();				
			case 2:
				return ner.getExecutionTime();				
			case 3:
				return ner.getIOTiming() == 0 ? "" : ner.getIOTiming();				
			}
			return null;
		}
		
	}
	
	private class AggregateTableModel extends AbstractTableModel {

		private String[]	columnNames = new String[] {"Name", "Execution Time", "%", "IO Time"};
		private List<NodeExecutionRecord> rows = new ArrayList<NodeExecutionRecord>();
		private Map<String, NodeExecutionRecord> executionMap = new HashMap<String, NodeExecutionRecord>();
		private long totalExecutionTime = 0;
		
		void addNodeExecution(String name, NodeID id, long executionTime, long ioTiming) {
			NodeExecutionRecord ner = executionMap.get(name);
			if (ner == null) {
				ner = new NodeExecutionRecord(name, id, executionTime, ioTiming);
				executionMap.put(name, ner);
				rows.add(ner);	
			} else {
				ner.incrementExecutionTime(executionTime);
				ner.incrementIOTiming(ioTiming);
			}
			totalExecutionTime += executionTime;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fireTableDataChanged();
				}				
			});			
		}
		
		public void clear() {
			rows.clear();
			executionMap.clear();
			totalExecutionTime = 0;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fireTableDataChanged();
				}				
			});
		}

		@Override
		public Class<?> getColumnClass(int index) {
			switch(index) {
				case 0:
					return String.class;
				case 1:
					return Long.class;
				case 2:
					return String.class;
				case 3:
					return String.class;
			}			
			return null;
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
			NodeExecutionRecord ner = rows.get(row);
			switch(col) {
			case 0:
				return ner.getName();										
			case 1:
				return ner.getExecutionTime();				
			case 2:
				return DF.format((double)ner.getExecutionTime() / (double)totalExecutionTime);
			case 3:
				if (ner.getIOTiming() == 0) {
					return "";
				}
				return ner.getIOTiming() + "    (" + DF.format((double)ner.getIOTiming() / (double)ner.getExecutionTime()) + ")";				
			}
			return null;
		}
		
	}
	
	@Override
	public void nodeExecuted(final String name, final NodeID id, final long executionTime, final long ioTiming) {
		rawTableModel.addNodeExecution(name, id, executionTime, ioTiming);
		aggregateTableModel.addNodeExecution(name, id, executionTime, ioTiming);
	}
	
	private class NodeExecutionRecord {
		private final String name;
		private final NodeID id;
		private long executionTime;
		private long ioTiming;
		
		public NodeExecutionRecord(String name, NodeID id, long executionTime, long ioTiming) {
			this.name 			= name;
			this.id 			= id;
			this.executionTime 	= executionTime;
			this.ioTiming 		= ioTiming;
		}

		public void incrementIOTiming(long ioTiming) {
			this.ioTiming += ioTiming;
		}

		public void incrementExecutionTime(long executionTime) {
			this.executionTime += executionTime;
		}

		public long getIOTiming() {
			return ioTiming;
		}

		public long getExecutionTime() {
			return executionTime;
		}

		public NodeID getId() {
			return id;
		}

		public String getName() {
			return name;
		}
		
	}


	
}
