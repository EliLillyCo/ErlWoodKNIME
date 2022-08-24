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
package org.erlwood.knime.utils.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.knime.core.node.NodeLogger;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import org.erlwood.knime.utils.gui.layout.*;
/**
 * A panel with controls allowing the configuration of a chemical file reader.
 * 
 * @author Dimitar Hristozov
 */
@SuppressWarnings("serial")
public class ChemFileReaderConfigPanel extends JPanel implements
		ActionListener, CheckBoxList.ItemStateListener {
	private static final NodeLogger LOG = NodeLogger.getLogger(ChemFileReaderConfigPanel.class);

	private static final String PROP_PANEL 		= "PROP_PANEL";	
	private static final String SCAN_REQUIRED 	= "SCAN_REQUIRED";
	private static final String SCANNING	 	= "SCANNING";

		
	private Dimension mDimRigidh = new Dimension(5, 0);
	private JList fileList;
	private FileListModel model = new FileListModel();
	private JButton mBtnBrowse;
	private JButton mBtnRemove;
	private JButton mBtnScan;
	private DualListboxBean<PropertyObject> mPropLists;
	private CheckBoxList mChblGeneral;
	private CheckBoxList mChblColTypesToWrite;
	private boolean mIsRxn;
	private boolean isShallow = true;
	private boolean requiresScanning = false;

	private ButtonGroup bg;

	private JRadioButton rdoShallow;

	private JRadioButton rdoDeep;
	private String lastDirectory = System.getProperty("user.home");

	private JPanel propPanel;

	private CardLayout propPanelLayout;
	private JProgressBar progressBar = new JProgressBar();
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	private JLabel lblFileName = new JLabel();
	
	private AtomicBoolean isCancelled = new AtomicBoolean(false);

	private boolean bNoScan; 
	/**
	 * Ctor.
	 */
	public ChemFileReaderConfigPanel(boolean isRxn) {
		super();		
		mIsRxn = isRxn;
		createGUI();
		setPreferredSize(new Dimension(650, 550));
	}

	public String[] getFileNames() {
		return model.getFileNames();
	}
	
	public boolean isShallowScan() {
		return isShallow;
	}
	
	public void setShallowScan(boolean b) {
		bNoScan = true;
		isShallow = b;
		rdoShallow.setSelected(isShallow);
		rdoDeep.setSelected(!isShallow);
		bNoScan = false;
	}

	public String getLastDirectory() {
		return lastDirectory;
	}
	
	public void setLastDirectory(String dir) {
		lastDirectory = dir;
	}

	
	public void setFileNames(final String[] fileNames) {
		for (String s : fileNames) {
			File f = new File(s);
			if (isValidFile(f)) {
				model.addElement(f.getAbsolutePath());			
			}		
		}				
		fileList.setSelectedIndex(model.size() - 1);
		mBtnRemove.setEnabled(model.size() > 0);
	}
	
	public List<String> getSelectedProperties() {
		List<String> res = new ArrayList<String>();
		for (int i = 0; i < mPropLists.getRightListModel().getSize(); ++i) {
			PropertyObject s = mPropLists.getRightListModel().get(i);
			//int f = s.lastIndexOf('(');
			res.add(s.getKey());
		}
		return res;
	}

	
	public Map<String, Boolean> getOutputOptions() {
		Map<String, Boolean> res = new HashMap<String, Boolean>();
		for (int i = 0; i < mChblGeneral.getModel().getSize(); ++i) {
			res.put(mChblGeneral.getModel().getElementAt(i).toString(),
					mChblGeneral.isItemChecked(i));
		}
		return res;
	}

	public void setOutputOptions(TreeMap<String, Boolean> values) {
		String[] items = values.keySet().toArray(new String[values.size()]);
		mChblGeneral.setListData(items);
		for (int i = 0; i < items.length; ++i) {
			mChblGeneral.checkItem(i, values.get(items[i]));
		}
	}

	public Map<String, Boolean> getOutputColumnTypes() {
		Map<String, Boolean> res = new HashMap<String, Boolean>();
		for (int i = 0; i < mChblColTypesToWrite.getModel().getSize(); ++i) {
			res.put(mChblColTypesToWrite.getModel().getElementAt(i).toString(),
					mChblColTypesToWrite.isItemEnabled(i)
							&& mChblColTypesToWrite.isItemChecked(i));
		}
		return res;
	}

	public void setOutputColumnTypes(TreeMap<String, Boolean> values) {
		String[] items = values.keySet().toArray(new String[values.size()]);
		mChblColTypesToWrite.setListData(items);
		for (int i = 0; i < items.length; ++i) {
			mChblColTypesToWrite.checkItem(i, values.get(items[i]));
		}
		itemStateChanged(0);
	}

	private void createGUI() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.add(createFileSelectorPanel());
		this.add(createPropsPanel());	
		this.add(createOutputOptionsPanel());
	}

	private JPanel createFileSelectorPanel() {
		
		JPanel res = new JPanel();
		res.setLayout(new TableLayout(new double[][] {{5, TableLayout.FILL, 10, TableLayout.PREFERRED}, {TableLayout.PREFERRED}}));
		res.setBorder(BorderFactory.createTitledBorder("Select file(s)"));

		fileList = new JList<String>(model);
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileList.setVisibleRowCount(5);	
		
		res.add(new JScrollPane(fileList), "1,0");
		

		JPanel rh = new JPanel();
		rh.setLayout(new TableLayout(new double[][] {{TableLayout.PREFERRED, TableLayout.FILL}, {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}}));		
		
		
		mBtnBrowse = new JButton("Add...");
		mBtnBrowse.addActionListener(this);
		
		mBtnRemove = new JButton("Remove");		
		mBtnRemove.addActionListener(this);
		mBtnRemove.setEnabled(false);
		
		
		mBtnScan = new JButton("Scan");		
		mBtnScan.addActionListener(this);
		mBtnScan.setEnabled(false);
		
		setButtonSize(mBtnBrowse, 100, 20);
		setButtonSize(mBtnRemove, 100, 20);
		setButtonSize(mBtnScan, 100, 20);
		
		bg = new ButtonGroup();
		rdoShallow = new JRadioButton("Shallow");
		rdoDeep = new JRadioButton("Deep");
		bg.add(rdoShallow);
		bg.add(rdoDeep);
		rdoShallow.setSelected(true);
		
		rh.add(mBtnBrowse, "0,0");
		rh.add(mBtnRemove, "0,1");
		rh.add(mBtnScan,   "0,3");
		
		
		JPanel rdoPanel = new JPanel(new TableLayout(new double[][] {{TableLayout.PREFERRED, 20, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL }, {TableLayout.PREFERRED}}));
		rdoPanel.add(new JLabel("Scan Depth"), "0,0");
		rdoPanel.add(rdoShallow, "2,0");
		rdoPanel.add(rdoDeep, "3,0");
				
		rh.add(rdoPanel, "0,4,1,4");
		
		rdoShallow.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					isShallow = true;
				} else {
					isShallow = false;
				}	
				mBtnScan.setEnabled(true);
				if (!bNoScan) {
					askToScan();
				}
			}
		});
		
		res.add(rh, "3,0");
		
		return res;
	}

	private void setButtonSize(JButton b, int width, int height) {
		Dimension d = new Dimension(width, height);
		b.setMaximumSize(d);
		b.setMinimumSize(d);
		b.setPreferredSize(d);
		
	}
	private JPanel createPropsPanel() {
		propPanel = new JPanel();
		propPanelLayout = new CardLayout();
		propPanel.setLayout(propPanelLayout);
		
		JPanel res = new JPanel();
		res.setLayout(new BoxLayout(res, BoxLayout.LINE_AXIS));
		res.add(javax.swing.Box.createRigidArea(mDimRigidh));
		res.setBorder(BorderFactory.createTitledBorder("Properties to keep"));

		mPropLists = new DualListboxBean<PropertyObject>();
		mPropLists.getLeftList().setCellRenderer(new PropertyObjectRenderer());
		res.add(mPropLists);
		res.add(javax.swing.Box.createRigidArea(mDimRigidh));
		
		JPanel scanRequired = new JPanel(new TableLayout(new double[][] {{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL}, {TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL}}));
		JLabel lblScanRequired = new JLabel("File list has been altered, please press the Scan button to initiate a property scan");
		lblScanRequired.setFont(lblScanRequired.getFont().deriveFont(12f).deriveFont(Font.BOLD));
		scanRequired.add(lblScanRequired, "1,1");
		
		JPanel scanning = new JPanel(new TableLayout(new double[][] {{TableLayout.FILL, 300, TableLayout.FILL}, {TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}}));
		scanning.add(new JLabel("Scanning Files"), "1,1");
		scanning.add(lblFileName , "1,2");
		scanning.add(progressBar, "1,3");
		
		propPanel.add(PROP_PANEL, res);		
		propPanel.add(SCAN_REQUIRED, scanRequired);
		propPanel.add(SCANNING, scanning);
		
		return propPanel;
	}

	private JPanel createOutputOptionsPanel() {
		JPanel res = new JPanel();
		res.setLayout(new BoxLayout(res, BoxLayout.LINE_AXIS));
		res.add(javax.swing.Box.createRigidArea(mDimRigidh));
		res.setBorder(BorderFactory.createTitledBorder("Output options"));

		mChblGeneral = new CheckBoxList();
		mChblGeneral.setBorder(BorderFactory.createTitledBorder("General"));
		mChblGeneral.checkItem(0, true);
		JScrollPane scroll = new JScrollPane(mChblGeneral);
		scroll.setAlignmentX(0);
		scroll.setAlignmentY(0);
		
		res.add(scroll);
		res.add(javax.swing.Box.createRigidArea(mDimRigidh));

		mChblColTypesToWrite = new CheckBoxList();

		mChblColTypesToWrite.addItemStateListener(this);
		mChblColTypesToWrite.setBorder(BorderFactory
				.createTitledBorder("Additional columns"));
		scroll = new JScrollPane(mChblColTypesToWrite);
		scroll.setAlignmentX(0);
		scroll.setAlignmentY(0);
		
		res.add(scroll);
		res.add(javax.swing.Box.createRigidArea(mDimRigidh));

		res.setMaximumSize(new Dimension(Integer.MAX_VALUE, res
				.getPreferredSize().height));
		return res;
	}

	/**
	 * Handles button clicks.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mBtnBrowse) {
			onBtnBrowseClick();
		} else if (e.getSource() == mBtnRemove) {
			onBtnRemoveClick();
		} else if (e.getSource() == mBtnScan) {
			onBtnScanClick();
		}
	}
	
	private void askToScan() {
		
		Object[] options = { "Scan Now", "Later" };
		int option = JOptionPane.showOptionDialog(this, 
												  "Do you wish to execute a scan now ?", 
												  "File List has Changed",
												  JOptionPane.DEFAULT_OPTION, 
												  JOptionPane.QUESTION_MESSAGE,
												  
												  null, options, options[0]);
		
		if (option == 0) {
			onBtnScanClick();
		}
	}
	private void onBtnRemoveClick() {
		int index = fileList.getSelectedIndex();
		if (index == -1) {
			return;
		}
		
		model.removeElementAt(index);
		
		requiresScanning = true;
		
		mBtnRemove.setEnabled(model.size() > 0);
		if (model.size() > 0) {
			if (index > 0) {
				index = Math.min(model.size() - 1, index);
			}
			fileList.setSelectedIndex(index);
			mBtnScan.setEnabled(true);
			askToScan();
			
		} else {
			mBtnScan.setEnabled(false);
			executeScan(new ArrayList<PropertyObject>());
			
		}
	}
		
	
	private void onBtnBrowseClick() {
		
		JFileChooser chooser = new JFileChooser(lastDirectory);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		if (!mIsRxn) {
			chooser.addChoosableFileFilter(new ChemStructureFilesFilter());
		}  else {
			chooser.addChoosableFileFilter(new ChemReactionFilesFilter());
		}
		chooser.setMultiSelectionEnabled(true);
		int returnVal = chooser.showDialog(this, null);
		if (JFileChooser.APPROVE_OPTION == returnVal) {
			lastDirectory = chooser.getCurrentDirectory().getAbsolutePath();
			List<String> lst = new ArrayList<String>();
			for (File f  : chooser.getSelectedFiles()) {
				lst.add(f.getAbsolutePath());							
			}
			setFileNames(lst.toArray(new String[0]));	
			requiresScanning = true;
			mBtnScan.setEnabled(true);
			askToScan();
		}
	}
	
	private void onBtnScanClick() {		
		executeScan(mPropLists.getRightListModel().getList());
	}
	
	public void executeScan(final List<PropertyObject> list) {
		
		executor.submit(new Runnable() {
			public void run() {
				
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						propPanelLayout.show(propPanel, SCANNING);	
						mBtnScan.setEnabled(false);
					}
					
				});
				
				
				Map<String, Integer> foundProps = scanProperties();
				int nScanned = foundProps.get("records_scanned");
				foundProps.remove("records_scanned");
								
				final List<PropertyObject> newProperties = new ArrayList<PropertyObject>();
				
				for (String key : foundProps.keySet()) {
					int n = foundProps.get(key);
					double pc = n * 100 / nScanned;
					String f = String.format(" (found in %.0f%%)", pc);
					newProperties.add(new PropertyObject(key, f));
				}
				
				//	Calculate the items to remove from the left & right hand sides
				final List<PropertyObject> newSelectedProps = new ArrayList<PropertyObject>(list);
				for (PropertyObject s : list) {
					int index = newProperties.indexOf(s);
					if (index != -1) {
						PropertyObject oldProp = newProperties.remove(index);
						s.setDescription(oldProp.getDescription());
					} else {
						newSelectedProps.remove(s);
					}
										
				}			
								
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						SortedFilteredListModel lMdl = mPropLists.getLeftListModel();
						SortedFilteredListModel rMdl = mPropLists.getRightListModel();
						
						lMdl.clear();
						lMdl.addAllItems(newProperties);
						
						rMdl.clear();
						rMdl.addAllItems(newSelectedProps);
						propPanelLayout.show(propPanel, PROP_PANEL);						
						requiresScanning = false;
					}					
				});
				
			}
		});
		
		
	}

	private Map<String, Integer> scanProperties() {
		
		int minRecsToScan = isShallow ? 10 : Integer.MAX_VALUE;
		
		Map<String, Integer> res = new HashMap<String, Integer>();
		int c = 0;
		try {
			for (int i = 0; i < model.getSize(); i++) {
				String fileName = model.getElementAt(i);
				File f = new File(fileName);
				MolImporter molImporter = new MolImporter(new CountingInputStream(f));
								
				while (true) {
					if (isCancelled.get()) {
						break;
					}
					if (c >= minRecsToScan) {
						break;
					}
					Molecule mol = null;
					try {
						mol = molImporter.read();
						if (mol == null) {
							break;
						}
					} catch(Exception ex) {
						continue;
					}
										
					String[] keys = mol.properties().getKeys();
					if (null != keys) {
						for (int k = 0; k < keys.length; k++) {
							if (res.containsKey(keys[k])) {
								res.put(keys[k], res.get(keys[k]) + 1);
							} else {
								res.put(keys[k], 1);
							}
						}
					}
					mol = molImporter.read();
					c += 1;
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		res.put("records_scanned", c);
		return res;
	}


	public boolean isValidFile(File f) {		
		return f.isFile() && f.exists();
	}

	/**
	 * Shows the GUI.
	 * 
	 * @param args
	 *            Not used.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frm = new JFrame();
				frm.setLayout(new BorderLayout());

				ChemFileReaderConfigPanel dlb = new ChemFileReaderConfigPanel(false);
				frm.add(dlb, BorderLayout.CENTER);
				frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frm.pack();
				frm.setVisible(true);
			}
		});
	}

	public abstract static class ChemFilesFilter extends FileFilter {
		private Map<String, String> mValidExtensions = new HashMap<String, String>();

		@Override
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			}
			String ext = getExtension(file);
			return (ext != null) && mValidExtensions.containsKey(ext);
		}

		public Map<String, String> getValidExtensions() {
			return mValidExtensions;
		}

		public void addExtension(String ext, String descr) {
			mValidExtensions.put(ext.toLowerCase(), descr);
		}

		private String getExtension(File f) {
			String s = f.getName();
			int i = s.lastIndexOf('.');
			if (i > 0 && i < s.length() - 1) {
				return s.substring(i + 1).toLowerCase();
			}
			return "";
		}

		protected void put(String key, String value) {
			mValidExtensions.put(key, value);
		}
	}

	public static class ChemStructureFilesFilter extends ChemFilesFilter {
		public ChemStructureFilesFilter() {
			super();
			put("mol", "MDL Mol file");
			put("mol2", "Tripos Mol2 file");
			put("sdf", "MDL SD File");
			put("smi", "Daylight Smile File");
			put("smiles", "Daylight Smile File");
			put("cml", "Chemical Markup Language File");
			put("pdb", "Protein Data Bank File");
		}

		@Override
		public String getDescription() {
			return "Chemical Structure Files";
		}
	}

	public static class ChemReactionFilesFilter extends ChemFilesFilter {
		public ChemReactionFilesFilter() {
			super();
			put("rxn", "MDL Rxn file");
			put("rdf", "MDL Rdf file");
			put("smi", "Daylight Smirks File");
			put("cml", "Chemical Markup Language File");
		}

		@Override
		public String getDescription() {
			return "Chemical Reaction Files";
		}
	}

	public void itemStateChanged(int index) {
		ListModel lmdl = mChblColTypesToWrite.getModel();
		if (null == lmdl) {
			return;
		}
		String s = lmdl.getElementAt(index).toString();
		if ("CHEMAXON Molecule".equals(s)) {
			boolean b = mChblColTypesToWrite.isItemChecked(index);
			//mBtnRemove.setEnabled(b);
			mChblGeneral.setEnabled(b);
			mPropLists.setEnabled(b);
			for (int i = 0; i < lmdl.getSize(); ++i) {
				if (i != index) {
					String ss = lmdl.getElementAt(i).toString();
					if (!"Source Column".equals(ss)) {
						mChblColTypesToWrite.enableItem(i, b);
					}
				}
			}
		}
	}
	
	private class FileListModel extends DefaultListModel<String> {
		private Set<String> files = new HashSet<String>();
		
		
		@Override
		public void addElement(String e) {
			if (files.contains(e)) {
				return;
			}
			files.add(e);
			super.addElement(e);
		}

		@Override
		public void removeElementAt(int i) {
			files.remove(get(i));
			super.removeElementAt(i);
		}
		
		public String[] getFileNames() {
			String[] retVal = new String[getSize()];
			for (int i = 0; i < getSize(); i++) {
				retVal[i] = get(i);
			}
			return retVal;
		}
	}


	public static class PropertyObject implements Comparable<PropertyObject> {
		private final String key;
		private String desc;
		public PropertyObject(String key, String desc) {
			this.key = key;
			this.desc = desc;
		}

		public void setDescription(String desc) {
			this.desc = desc;
		}

		public String getKey() {
			return key;
		}

		@Override
		public int compareTo(PropertyObject o) {
			return key.compareTo(o.key);
		}

		@Override
		public String toString() {		
			return key;
		}
		
		private String getDescription() {
			return desc;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PropertyObject other = (PropertyObject) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}
		
	}
	
	private class PropertyObjectRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			PropertyObject obj = (PropertyObject) value;
			String s = obj.getKey() + " " + obj.getDescription();
			return super.getListCellRendererComponent(list, s, index, isSelected,
					cellHasFocus);
		}		
		
	}
	
	/**
	 * This class is used to work out the progress of reading files.
	 * @author Luke Bullard
	 *
	 */
	private class CountingInputStream extends FileInputStream {
		private final long fileSize;
		private long count = 0;
		
		/**
		 * Constructor.
		 * @param f The File to read
		 * @throws FileNotFoundException If the File is not found
		 */
		private CountingInputStream(final File f) throws FileNotFoundException {
			super(f);
			fileSize = f.length();
			
			
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					lblFileName.setText(f.getAbsolutePath());
					progressBar.setValue(0);
					progressBar.setMaximum(100);					
				}
			});
		}
		
		/**
		 * Adds a number of read bytes to the count and updates the progress bar.
		 * @param add The value to add
		 */
		private void addCount(int add) {
			if (add <= 0) {
				return;
			}
			
			count+=add;
			
			final long tmpCount = count;
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					double d = ((double)tmpCount / (double)fileSize) * 100; 			
					progressBar.setValue((int) Math.round(d));					
				}
			});
			
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read() throws IOException {
			int i = super.read();
			addCount(i);
			return i;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read(byte[] b) throws IOException {
			int read = super.read(b);
			addCount(read);
			return read;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int read = super.read(b, off, len);
			addCount(read);
			return read;			
		}
	}

	public void setFoundProperties(String[] foundProps) {
		final List<PropertyObject> newProperties = new ArrayList<PropertyObject>();
		
		for (String key : foundProps) {			
			newProperties.add(new PropertyObject(key, ""));
		}
		mPropLists.getLeftListModel().clear();
		mPropLists.getLeftListModel().addAllItems(newProperties);		
	}

	public void setAllProperties(String[] foundProps, String[] selectedProps) {
		
		final List<PropertyObject> newProperties = new ArrayList<PropertyObject>();
		final List<PropertyObject> newSelectedProperties = new ArrayList<PropertyObject>();
		
		for (String key : foundProps) {			
			newProperties.add(new PropertyObject(key, ""));
		}
		for (String key : selectedProps) {			
			newSelectedProperties.add(new PropertyObject(key, ""));
		}
		mPropLists.getLeftListModel().clear();
		mPropLists.getLeftListModel().addAllItems(newProperties);
		
		mPropLists.getRightListModel().clear();
		mPropLists.getRightListModel().addAllItems(newSelectedProperties);
		
		
	}

	public void setRequiresScan(boolean b) {
		this.requiresScanning = b;
		mBtnScan.setEnabled(requiresScanning);
	}

	public boolean isRequiresScan() {
		return requiresScanning;
	}

	public List<String> getFoundProperties() {
		List<String> res = new ArrayList<String>();
		Iterator<PropertyObject> iter = mPropLists.getLeftListModel().originaIterator();
		while (iter.hasNext()) {
			PropertyObject s = iter.next();
			res.add(s.getKey());
		}
		return res;
	}
	
	public void resetCancelled() {
		isCancelled.set(false);
	}

	public void onCancel() {
		isCancelled.set(true);		
	}
}
