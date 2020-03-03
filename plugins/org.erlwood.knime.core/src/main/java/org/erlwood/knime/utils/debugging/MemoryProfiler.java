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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.erlwood.knime.utils.gui.GuiUtils;
import org.erlwood.knime.utils.gui.layout.TableLayout;

/**
 * Frame to display memory stats.
 */
@SuppressWarnings("serial")
public class MemoryProfiler  extends JFrame  {
		
	private static final DecimalFormat BYTE_FORMAT = new DecimalFormat("##0.00");

	/** Memory MX bean for collecting usage stats. */
	private MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
	
	/** total usage panel. */	
	private MemoryUsagePanel totalUsagePanel 	= new MemoryUsagePanel();
	
	/** details usage panel. */	
	private MemoryUsagePanel detailsUsagePanel 	= new MemoryUsagePanel();
		
	/** 1 second timer for getting memory usage. */
	private Timer timer = new Timer();
	
	/** The MmeorySnapshot frame. */
	private MemorySnapshots ms = null;
		
	/** Snapshot table model. */
	private MemoryPoolTableModel memoryPoolTableModel = new MemoryPoolTableModel();
	
	/** Table for memory pools. */
	private JTable table;
	
	/**
	 * Constructor.
	 */
	public MemoryProfiler() {
		setSize(800, 670);
		setTitle("Memory Profiler");
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
		
		pnl.add(createToolbarPanel(), BorderLayout.SOUTH);
		pnl.add(createCenterPanel(), BorderLayout.CENTER);
		return pnl;
	}
	
	/** 
	 * @return Creates and returns the toolbar panel.
	 */
	private JPanel createToolbarPanel() {
		JPanel pnl = new JPanel();
		
		JButton cmdSnapshots = new JButton("Snapshots...");
		pnl.add(cmdSnapshots);
		
		cmdSnapshots.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (ms == null) {
					ms = new MemorySnapshots();
				}
				ms.setVisible(true);
				ms.toFront();
			}
		});
			
		
		return pnl;
	}
	
	/** 
	 * @return Creates and returns the center panel.
	 */
	private JPanel createCenterPanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, TableLayout.PREFERRED,  TableLayout.FILL}}));
		
		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Overview", createTotalUsagePanel());
		tab.addTab("Details", createDetailsPanel());
		
		pnl.add(tab, 	"0,0");
				
		pnl.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		return pnl;
	}
	
	
	/** 
	 * @return Creates and returns the details panel.
	 */
	private JPanel createDetailsPanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.FILL}}));
		
		pnl.add(createTablePanel(), 			"0,0");
		pnl.add(detailsUsagePanel, 				"0,2");
		pnl.add(createMemoryPoolLegendPanel(), 	"0,4");
		
		pnl.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		return pnl;
	}
	
	/** 
	 * @return Creates and returns the table panel.
	 */
	private JPanel createTablePanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		
		table = new JTable(memoryPoolTableModel);
		DefaultTableCellRenderer r = new DefaultTableCellRenderer();
		r.setHorizontalAlignment(SwingConstants.RIGHT);
		
		//	Right align the numneric columns
		for (int i = 2; i < 6; i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(r);
		}
		
		JScrollPane scp = new JScrollPane(table);
		scp.getViewport().setBackground(Color.white);
		table.setPreferredScrollableViewportSize(new Dimension(100, table.getRowHeight() * 7));
		pnl.add(scp, BorderLayout.CENTER);
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				processSelectedDetails();				
			}
		});
		
		return pnl;
	}
	
	/** 
	 * @return Creates and returns the heap usage panel.
	 */
	private JPanel createTotalUsagePanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 30, TableLayout.PREFERRED} }));
		
		pnl.add(GuiUtils.getHeaderPanel("Total Memory Usage"), "0,0");
		pnl.add(totalUsagePanel, "0,2");
		pnl.add(createMemoryUsageLegendPanel(), "0,4");
		
		pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		return pnl;
	}
	
	
	/** 
	 * @return Creates and returns the memory usage legend panel.
	 */
	private JPanel createMemoryUsageLegendPanel() {
		JPanel pnl = new JPanel();
		pnl.setBackground(Color.white);
		pnl.setLayout(new TableLayout(new double[][] {{10, TableLayout.FILL, 10}, 
				{10,
				 TableLayout.PREFERRED, 2, TableLayout.PREFERRED, 10, 
				 TableLayout.PREFERRED, 2, TableLayout.PREFERRED, 10, 
				 TableLayout.PREFERRED, 2, TableLayout.PREFERRED, 10, 
				 TableLayout.PREFERRED, 2, TableLayout.PREFERRED, 10,  
				 TableLayout.FILL} }));
		
		JLabel txtInit = new JLabel();
		txtInit.setText("<html><body>Represents the initial amount of memory (in bytes) that the Java virtual machine requests from the operating system for memory management during startup. The Java virtual machine may request additional memory from the operating system and may also release memory to the system over time.</body></html>");
		
		JLabel txtUsed = new JLabel();
		txtUsed.setText("<html><body>Represents the amount of memory currently used (in bytes).</body></html>");
		
		JLabel txtCommitted = new JLabel();
		txtCommitted.setText("<html><body>Represents the amount of memory (in bytes) that is guaranteed to be available for use by the Java virtual machine. The amount of committed memory may change over time (increase or decrease). The Java virtual machine may release memory to the system and committed could be less than init. committed will always be greater than or equal to used.</body></html>");
		
		JLabel txtMax = new JLabel();
		txtMax.setText("<html><body>Represents the maximum amount of memory (in bytes) that can be used for memory management. The maximum amount of memory may change over time if defined. The amount of used and committed memory will always be less than or equal to max if max is defined. A memory allocation may fail if it attempts to increase the used memory such that used > committed even if used <= max would still be true (for example, when the system is low on virtual memory).</body></html>");
		
		
		pnl.add(createInitLabel(), 	"1,1");
		pnl.add(txtInit, 			"1,3");
		
		pnl.add(createUsedLabel(), "1,5");
		pnl.add(txtUsed, "1,7");
		
		pnl.add(createCommittedLabel(), "1,9");
		pnl.add(txtCommitted, "1,11");
		
		pnl.add(createMaxLabel(), "1,13");
		pnl.add(txtMax, "1,15");
		
		pnl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return pnl;		
	}
	
	
	/** 
	 * @return Creates and returns the memory pool legend panel.
	 */
	private JPanel createMemoryPoolLegendPanel() {
		JPanel pnl = new JPanel();
		pnl.setBackground(Color.white);
		pnl.setLayout(new TableLayout(new double[][] {{10, TableLayout.FILL, 10}, 
				{10,
				 TableLayout.PREFERRED, 2, TableLayout.PREFERRED, 10, 
				 TableLayout.PREFERRED, 2, TableLayout.PREFERRED, 10, 
				 TableLayout.PREFERRED, 2, TableLayout.PREFERRED, 10, 
				 TableLayout.PREFERRED, 2, TableLayout.PREFERRED, 10,  
				 TableLayout.PREFERRED, 2, TableLayout.PREFERRED, 10,
				 TableLayout.FILL} }));
		
		JLabel txtEden = new JLabel();
		txtEden.setText("<html><body>The pool from which memory is initially allocated for most objects.</body></html>");
		
		JLabel txtSurvivor = new JLabel();
		txtSurvivor.setText("<html><body>The pool containing objects that have survived the garbage collection of the Eden space.</body></html>");
		
		JLabel txtOld = new JLabel();
		txtOld.setText("<html><body>The pool containing objects that have existed for some time in the survivor space.</body></html>");
		
		JLabel txtCodeCache = new JLabel();
		txtCodeCache.setText("<html><body>The HotSpot Java VM also includes a code cache, containing memory that is used for compilation and storage of native code..</body></html>");
		
		JLabel txtPermGen = new JLabel();
		txtPermGen.setText("<html><body>The pool containing all the reflective data of the virtual machine itself, such as class and method objects. With Java VMs that use class data sharing, this generation is divided into read-only and read-write areas.</body></html>");
		
		
		pnl.add(GuiUtils.createBoldLabel("PS Eden Space"), 	"1,1");
		pnl.add(txtEden, 			"1,3");
		
		pnl.add(GuiUtils.createBoldLabel("PS Survivor Space"), "1,5");
		pnl.add(txtSurvivor, "1,7");
		
		pnl.add(GuiUtils.createBoldLabel("PS Old Gen"), "1,9");
		pnl.add(txtOld, "1,11");
		
		pnl.add(GuiUtils.createBoldLabel("Code Cache"), "1,13");
		pnl.add(txtCodeCache, "1,15");
		
		pnl.add(GuiUtils.createBoldLabel("PS Perm Gen"), "1,17");
		pnl.add(txtPermGen, "1,19");
		
		pnl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return pnl;		
	}
	
	/**
	 * Creates a 8x8 pixel square icon using the given color.
	 * @param c The color to use
	 * @return The Icon
	 */
	private Icon createIcon(Color c) {
		BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();
		g.setColor(c);
		g.fillRect(0, 0, 8, 8);
		g.setColor(Color.lightGray);
		g.drawRect(0, 0, 7, 7);
		
		ImageIcon i = new ImageIcon(bi);
		return i;
	}
	
	/** 
	 * @return The init label
	 */
	private JLabel createInitLabel() {
		return new JLabel("Init", createIcon(Color.RED), SwingConstants.LEFT);
	}
	
	/** 
	 * @return The used label
	 */
	private JLabel createUsedLabel() {
		return new JLabel("Used", createIcon(Color.YELLOW), SwingConstants.LEFT);			
	}
	
	/** 
	 * @return The committed label
	 */
	private JLabel createCommittedLabel() {
		return new JLabel("Committed", createIcon(Color.BLUE), SwingConstants.LEFT);
	}
	
	/** 
	 * @return The max label
	 */
	private JLabel createMaxLabel() {
		return new JLabel("Max", createIcon(Color.GREEN), SwingConstants.LEFT);
	}
		
	private void processSelectedDetails() {
		//	Ensure we are running in the EDT.
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					processSelectedDetails();
				}				
			});
			return;
		}	
		
		int[] selectedRows = table.getSelectedRows();
		
		if (selectedRows.length == 0) {
			detailsUsagePanel.setNoUsageStats();
			return;
		}
		
		long init 		= 0;
		long used 		= 0;
		long committed 	= 0;
		long max 		= 0;
		
		for (int i = 0; i < selectedRows.length; i++) {
			MemoryPoolRecord mr = memoryPoolTableModel.getMemoryPool(selectedRows[i]);
			init += mr.getInit();
			used += mr.getUsed();
			committed += mr.getCommitted();
			max += mr.getMax();
		}
		detailsUsagePanel.setUsage(init, used, committed, max);
	}
	
	/**
	 * Takes a byte value and converts it to an appropriatly
	 * suffixed string format.
	 * @param v The byte value
	 * @return The byte string in appropriate format.
	 */
	static String getByteShorthand(long v) {
		String retVal = "";
		
		if (v > 1000000000) {
			retVal = BYTE_FORMAT.format((double)v / 1000000000d) + " GB";
		} else if (v > 1000000) {
			retVal = BYTE_FORMAT.format((double)v / 1000000d) + " MB";
		} else 	if (v > 1000) {
			retVal = BYTE_FORMAT.format((double)v / 1000d) + " KB";
		}
		
		return retVal;
	}
	
	
	/**
	 * Panel used to display usage statistics for a MemoryPool.
	 * @author Luke Bullard
	 *
	 */
	private class MemoryUsagePanel extends JPanel {
		/**
		 * Labels for showing bytes consumed etc..
		 */
		private JLabel lblInit 		= new JLabel("", SwingConstants.RIGHT);
		private JLabel lblUsed 		= new JLabel("", SwingConstants.RIGHT);
		private JLabel lblCommitted = new JLabel("", SwingConstants.RIGHT);
		private JLabel lblMax 		= new JLabel("", SwingConstants.RIGHT);
		
		private String txtInit;
		private String txtUsed;
		private String txtCommitted;
		private String txtMax;
		
		/** The histogram. */
		private HeapHistogram histogram;
		
		/**
		 * Constructor.
		 */
		private MemoryUsagePanel() {			
			setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, 15, 60, TableLayout.FILL} }));
			
			histogram = new HeapHistogram(this.getBackground());
			add(createStatsPanel(), "0,0");
			add(histogram, "0,2");		
		}
		
		/** 
		 * @return Creates and returns the stas panel.
		 */		
		private JPanel createStatsPanel() {
			JPanel pnl = new JPanel(new TableLayout(new double[][] {{TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.FILL},{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.FILL}}));			
			
			pnl.add(createInitLabel(), "0,0");			
			pnl.add(lblInit, "2,0");
			
			pnl.add(createUsedLabel(), "0,2");			
			pnl.add(lblUsed, "2,2");
			
			pnl.add(createCommittedLabel(), "0,4");			
			pnl.add(lblCommitted, "2,4");
			
			pnl.add(createMaxLabel(), "0,6");			
			pnl.add(lblMax, "2,6");
			
			return pnl;
		}
		
		public void setNoUsageStats() {
			histogram.setNoUsageStats();
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {					
					lblInit.setEnabled(false);
					lblUsed.setEnabled(false);
					lblCommitted.setEnabled(false);
					lblMax.setEnabled(false);
				}
				
			});
		}
		
		/**
		 * Sets the usage data.
		 * @param init The initial MemoryUsage 
		 * @param used The used MemoryUsage
		 * @param committed The committed MemoryUsage
		 * @param max The max MemoryUsage
		 */
		public void setUsage(long init, long used, long committed, long max) {
			
			histogram.setUsage(init, used, committed, max);
			
			txtInit 		= getByteShorthand(init);
			txtUsed 		= getByteShorthand(used);
			txtCommitted 	= getByteShorthand(committed);
			txtMax 			= getByteShorthand(max);
			
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					lblInit.setText(txtInit);
					lblUsed.setText(txtUsed);
					lblCommitted.setText(txtCommitted);
					lblMax.setText(txtMax);
					histogram.repaint();
					lblInit.setEnabled(true);
					lblUsed.setEnabled(true);
					lblCommitted.setEnabled(true);
					lblMax.setEnabled(true);
				}
				
			});
			
		}
		
	}
	
	/**
	 * Panel used to draw the histogram.
	 * @author Luke Bullard
	 *
	 */
	private class HeapHistogram extends JPanel {
		/** Usage values. */
		private long init;
		private long used;
		private long committed;
		private long max;
		
		/** Percentage values to calculate the amount of space for each value. */
		private double initP;
		private double usedP;
		private double committedP;
		
		/** The background color to clear with. */
		private Color backgroundColor;
		
		/** The text values to display on the graphic. */
		private String txtInit;
		private String txtUsed;
		private String txtCommitted;
		private String txtMax;
		
		private boolean bUsage = false;
		
		/**
		 * Constructor.
		 * @param bc The color to use for clearing the background.
		 */
		private HeapHistogram(Color bc) {
			backgroundColor = bc;
		}

		public void setNoUsageStats() {
			bUsage = false;			
		}
		/**
		 * Sets the usage data.
		 * @param init The initial MemoryUsage 
		 * @param used The used MemoryUsage
		 * @param committed The committed MemoryUsage
		 * @param max The max MemoryUsage
		 */
		public void setUsage(long init, long used, long committed, long max) {
			
			this.init 		= init;
			this.used 		= used;
			this.committed 	= committed;
			this.max 		= max;
						
			//	Calculate the percentages
			initP 		= (double)init / (double)max;
			usedP 		= (double)used / (double)max;
			committedP 	= (double)committed / (double)max;
			
			//	Get text values
			txtInit 		= getByteShorthand(init);
			txtUsed 		= getByteShorthand(used);
			txtCommitted 	= getByteShorthand(committed);
			txtMax 			= getByteShorthand(max);
			bUsage = true;
		}
		
		/**
		 * Draws the histogram.
		 */
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D)g;
			
			int width 			= getWidth();
			int height 			= getHeight();
			int topOffset 		= 15;
			int bottomOffset 	= 15;
			
			// Clear the background first
			g.setColor(backgroundColor);
			g.fillRect(0, 0, width, height);
			
			if (!bUsage) {
				g.setColor(Color.lightGray);
				g.fillRect(0, topOffset, width, height - topOffset - bottomOffset);
				return;
			}
			
			//	Draw the maximum
			g.setColor(Color.GREEN);
			g.fillRect(0, topOffset, width, height - topOffset - bottomOffset);
			
			//	Draw the committed
			g.setColor(Color.BLUE);
			g.fillRect(0, topOffset, (int)(width * committedP), height - topOffset - bottomOffset);
			
			//	Draw the used
			g.setColor(Color.YELLOW);
			g.fillRect(0, topOffset, (int)(width * usedP), height - topOffset - bottomOffset);
			
			//	Draw the init
			g.setColor(Color.RED);
			g.fillRect(0, topOffset, (int)(width * initP), height - topOffset - bottomOffset);
			
			
			//	How wide is the max text ?			
			int maxWidth = g.getFontMetrics().stringWidth(txtMax);
			
			//	 Draw the marker lines
			g.setColor(Color.gray);
			g.drawLine(0, 0, 0, height - bottomOffset - 1);			
			g.drawLine((int)(width * initP), topOffset, (int)(width * initP), height);
			g.drawLine( (int)(width * usedP), 0,  (int)(width * usedP), height - bottomOffset - 1);
			g.drawLine((int)(width * committedP), topOffset, (int)(width * committedP), height);			
			g.drawLine(width-1, 0, width-1, height - bottomOffset - 1);
							
			//	Draw the usage text
			g2d.drawString("0", 4, 8);
			g2d.drawString(txtInit, (int)(width * initP) + 4, height -1 );
			g2d.drawString(txtUsed, (int)(width * usedP) + 4, 8 );
			g2d.drawString(txtCommitted, (int)(width * committedP) + 4, height -1 );
			g2d.drawString(txtMax, width - maxWidth - 2, 8);
		}
	}


	/**
	 * Task to collect the usage and distribute to the panels.
	 * @author Luke Bullard
	 *
	 */
	private class GetMemoryUsageTask extends TimerTask {
		private long init 		= 0;
		private long used 		= 0;
		private long committed 	= 0;
		private long max 		= 0;
		
		@Override
		public void run() {
			processPools();			
			totalUsagePanel.setUsage(init, used, committed, max);						
		}
		
		private void processPools() {

			init 		= 0;
			used 		= 0;
			committed 	= 0;
			max 		= 0;
			
			Iterator<MemoryPoolMXBean> iter = ManagementFactory.getMemoryPoolMXBeans().iterator();
			while (iter.hasNext())
			{
			    MemoryPoolMXBean item = iter.next();
			    String name = item.getName();
			    MemoryType type = item.getType();
			    MemoryUsage usage = item.getUsage();
			    
			    
			    init 		+= usage.getInit();
			    used 		+= usage.getUsed();
			    committed 	+= usage.getCommitted();
			    max 		+= usage.getMax();	
			    
			    memoryPoolTableModel.addMemoryPool(name, type, usage.getInit(), usage.getUsed(), usage.getCommitted(), usage.getMax());
			}	
			
			processSelectedDetails();
		}
	}
	
	/**
	 * Memory Pool table model.
	 * @author Luke Bullard
	 *
	 */
	private class MemoryPoolTableModel extends AbstractTableModel {
		/** The columns. */
		private String[]	columnNames = new String[] {"Name", "Type", "Init", "Used", "Committed", "Max"};
		
		/** The list of memory pools. */
		private List<MemoryPoolRecord> rows = new ArrayList<MemoryPoolRecord>();
		
		/** The map of memory pools. */
		private Map<String, MemoryPoolRecord> poolMap = new HashMap<String, MemoryPoolRecord>();
		
		/**
		 * Adds a new memory pool to the list or updates an existing one.
		 * @param name The name of the Memory Pool
		 * @param init The init value
		 * @param used The used value
		 * @param committed The committed value
		 * @param max The max value
		 */
		public void addMemoryPool(final String name, final MemoryType type, final long init, final long used, final long committed, final long max) {
			//	Ensure we are running in the EDT.
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						addMemoryPool(name, type, init, used, committed, max);
					}				
				});
				return;
			}
			
			MemoryPoolRecord mr = poolMap.get(name);
			if (mr == null) {
				mr = new MemoryPoolRecord(name, type, init, used, committed, max); 
				rows.add(mr);
				poolMap.put(name, mr);
				fireTableDataChanged();
			} else {
				mr.setUsage(init, used, committed, max);
				fireTableRowsUpdated(0, rows.size());
			}
			
						
		}

		
		public MemoryPoolRecord getMemoryPool(int i) {
			return rows.get(i);
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
			MemoryPoolRecord ner = rows.get(row);
					
			switch(col) {
				case 0:
					return ner.getName();				
				case 1:					
					return ner.getType();				
				case 2:
					return getByteShorthand(ner.getInit());				
				case 3:
					return getByteShorthand(ner.getUsed());								
				case 4:
					return getByteShorthand(ner.getCommitted());				
				case 5:
					return getByteShorthand(ner.getMax());				
			}
			return null;
		}
		
	}
	

	/**
	 * Memory Pool record. 
	 * @author Luke Bullard
	 *
	 */
	private class MemoryPoolRecord {
		private final String name;		
		private final MemoryType type;
		
		private long init;
		private long used;
		private long committed;
		private long max;		

		public MemoryPoolRecord(String name, MemoryType type, long init, long used, long committed, long max) {
			this.name 		= name;
			this.type 		= type;
			this.init 		= init;
			this.used 		= used;
			this.committed 	= committed;
			this.max 		= max;
		}
				
		/**
		 * Sets the usage data.
		 */
		public void setUsage(long init, long used, long committed, long max) {
			this.init 		= init;
			this.used 		= used;
			this.committed 	= committed;
			this.max 		= max;
		}

		public long getInit() {
			return init;
		}

		public long getUsed() {
			return used;
		}

		public long getCommitted() {
			return committed;
		}

		public long getMax() {
			return max;
		}

		public String getName() {
			return name;
		}

		public MemoryType getType() {
			return type;
		}
		
	}

	
}



