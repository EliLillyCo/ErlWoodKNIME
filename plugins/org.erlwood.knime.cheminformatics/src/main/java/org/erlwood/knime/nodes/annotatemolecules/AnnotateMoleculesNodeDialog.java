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
package org.erlwood.knime.nodes.annotatemolecules;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jp.co.infocom.cheminfo.marvin.type.MrvValue;

import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.erlwood.knime.utils.KnimenodeUtils;
import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import chemaxon.marvin.beans.MViewPane;
import chemaxon.marvin.beans.MolRenderer;
import chemaxon.struc.Molecule;

/**
 * <code>NodeDialog</code> for the "AnnotateMolecules" Node.
 * 
 * @author Luke Bullard
 */
public final class AnnotateMoleculesNodeDialog extends DataAwareNodeDialogPane implements ListSelectionListener {
	
	private static final String NO_STRUCTURE 	= "NO_STRUCTURE";
	private static final String STRUCTURE 		= "STRUCTURE";
  
    private JComboBox cboMoleculeColumn = new JComboBox();    
	private MViewPane mView;

	private MoleculeTableModel 	tableModel 			= new MoleculeTableModel();
	private JTable 				listTable 			= new JTable(tableModel);
	private BufferedDataTable	bufferedDataTable 	= null;
	private CardLayout			cardLayout;
	private JPanel				cardPanel;
	private JPanel				structurePanel;
	private Map<RowKey, String>	comments 			= new HashMap<RowKey, String>();
	private long tableHashCode;
	
    /**
     * New pane for configuring the SelectAnnotate node.
     */
    protected AnnotateMoleculesNodeDialog() {
        super();
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BorderLayout());
        
        optionsPanel.add(getMoleculeColumnSelectionPanel(), BorderLayout.NORTH);
        optionsPanel.add(getMainPanel(), BorderLayout.CENTER);
                 
        addTabAt(0, "Options", optionsPanel);
		setSelected("Options");
    }

    /** 
     * @return The molecule selection panel
     */
    private JPanel getMoleculeColumnSelectionPanel() {
    	JPanel panel = new JPanel();
    	panel.setLayout(new TableLayout(new double[][] {{TableLayout.PREFERRED, 20, TableLayout.PREFERRED}, {TableLayout.PREFERRED}}));
    	
    	panel.add(new JLabel("Select molecule column:"), "0,0");
    	panel.add(cboMoleculeColumn, "2,0");

    	cboMoleculeColumn.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					setTableData();
				}
			}
		});
    	
    	panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    	return panel;
    }
    
    private void setTableData() {
    	DataColumnSpec dcs = (DataColumnSpec)cboMoleculeColumn.getSelectedItem();
    	
    	int index = bufferedDataTable.getDataTableSpec().findColumnIndex(dcs.getName());
    	
    	tableModel.setData(bufferedDataTable, index);
    }
    
    /** 
     * @return The main panel
     */
    private JPanel getMainPanel() {
    	JPanel panel = new JPanel();
    	
    	panel.setLayout(new BorderLayout());
    	
    	panel.add(getMarvinViewPanel(), BorderLayout.CENTER);
       	return panel;
    }
    
    /** 
     * @return The marvin view panel
     */
    private JPanel getMarvinViewPanel() {
    	cardLayout = new CardLayout();
    	cardPanel = new JPanel(cardLayout);
    	cardPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    	
    	JPanel noStructureSelected = new JPanel(new TableLayout(new double[][] {{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL}, {TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL}}));
    	noStructureSelected.add(new JLabel("No Structure Selected"), "1, 1");

    	structurePanel = new JPanel();
    	structurePanel.setLayout(new BorderLayout());
    	
    	cardPanel.add(noStructureSelected,  NO_STRUCTURE);
    	cardPanel.add(structurePanel, 		STRUCTURE);
    	
    	Dimension d = new Dimension(400, 400);
    	cardPanel.setMinimumSize(d);
    	cardPanel.setMaximumSize(d);
    	cardPanel.setPreferredSize(d);
    
    	JPanel panel = new JPanel();
    	
    	panel.setLayout(new BorderLayout());
    	
    	panel.add(cardPanel, BorderLayout.CENTER);    	
    	panel.add(getListPanel(), BorderLayout.EAST);
    	
    	return panel;
    }
    
    /** 
     * @return The list panel
     */
    private JPanel getListPanel() {
    	listTable.getColumnModel().getColumn(0).setCellRenderer(new MolRenderer());
    	listTable.getColumnModel().getColumn(0).setMaxWidth(100);
    	listTable.getColumnModel().getColumn(0).setPreferredWidth(100);

    	listTable.getColumnModel().getColumn(1).setWidth(400);

    	listTable.getColumnModel().getColumn(1)
				.setCellEditor(new CommentEditor());
    	listTable.getColumnModel().getColumn(1)
				.setCellRenderer(new CommentRenderer());
    	listTable.getSelectionModel().addListSelectionListener(this);
    	listTable.setRowHeight(100);
    	listTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	listTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    	
		JScrollPane scrollPane = new JScrollPane(listTable);
		scrollPane.getViewport().setBackground(Color.white);
		
    	JPanel panel = new JPanel();
    	
    	panel.setLayout(new BorderLayout());
    	
    	panel.add(scrollPane, BorderLayout.CENTER);    	

    	return panel;
    } 
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final BufferedDataTable[] input) throws NotConfigurableException {
    	bufferedDataTable = input[0];
    	    	
    	DataTableSpec ds = bufferedDataTable.getDataTableSpec();
    	List<DataColumnSpec> columns = new ArrayList<DataColumnSpec>();
    	
    	
    	Map<String, DataColumnSpec> colSpecmap = new HashMap<String, DataColumnSpec>();
    	for (DataColumnSpec dcs : ds) {
			if (MoleculeDataTypeConverter.isConvertible(dcs.getType(), SmilesValue.class)) {
				columns.add(dcs);
				colSpecmap.put(dcs.getName(), dcs);
			}			
		}
    	
    	cboMoleculeColumn.setModel(new DefaultComboBoxModel(columns.toArray(new DataColumnSpec[0])));
    	
    	//	Calculate the hashcode of the incoming datatable.
    	long tc = KnimenodeUtils.calculateHashCode(bufferedDataTable);
    	
    	    	
    	try {
    		//	Get the stored hashcode
    		tableHashCode = settings.getLong(AnnotateMoleculesNodeModel.CFG_TABLE_HASHCODE);
    		comments.clear();
    		
    		//	If they are the same then re-use the stored comments.
    		if (tableHashCode == tc) {
				RowKey[] rowKeys = settings.getRowKeyArray(AnnotateMoleculesNodeModel.CFG_ROWKEYS);
				String[] cmts = settings.getStringArray(AnnotateMoleculesNodeModel.CFG_COMMENTS);
				
				for (int i = 0; i < rowKeys.length; i++) {
					comments.put(rowKeys[i], cmts[i]);
				}
    		}
			
		} catch (InvalidSettingsException e) {
			throw new NotConfigurableException(e.getMessage(), e);			
		}
    	
    	
    	tableHashCode = tc;
    	setTableData();
    	
    	     
    }
    
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// Make sure the table has finished editing.
		TableCellEditor ced = listTable.getCellEditor();
		if (ced != null) {
			ced.stopCellEditing();
		}
		//	Update the comments map.
		comments.clear();
		for(ItemData id : tableModel.rows) {
			if (id.comment != null) {
				comments.put(id.rowKey, id.comment);
			}
		}
		RowKey[] rowKeys = new RowKey[comments.size()];
		String[] cmts = new String[comments.size()];
		int i = 0;
		for (Entry<RowKey, String> es : comments.entrySet()) {
			rowKeys[i] = es.getKey();
			cmts[i] = es.getValue();
			i++;
		}
		
		settings.addRowKeyArray(AnnotateMoleculesNodeModel.CFG_ROWKEYS,  rowKeys);
		settings.addStringArray(AnnotateMoleculesNodeModel.CFG_COMMENTS, cmts);
		settings.addLong(AnnotateMoleculesNodeModel.CFG_TABLE_HASHCODE, tableHashCode);
	}
	
	/**
	 * Handles the selection on the table.
	 */
	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if (!evt.getValueIsAdjusting()) {
			Molecule m = (Molecule)tableModel.getValueAt(listTable.getSelectedRow(), 0);		
			
			if (m != null) {							
				MViewPane mview = new MViewPane();
				mview.setEditable(MViewPane.VIEW_ONLY);
				mview.setDetachable(false);
				structurePanel.removeAll();
				structurePanel.add(mview, BorderLayout.CENTER);
				mview.setM(0, m);
				structurePanel.validate();
				cardLayout.show(cardPanel, STRUCTURE);
			} else {
				cardLayout.show(cardPanel, NO_STRUCTURE);
			}
		}
		
	}
	
	/**
	 * TableModel used to hold the list of molecules and comments.
	 * @author Luke Bullard
	 *
	 */
	@SuppressWarnings("serial")
	private class MoleculeTableModel extends AbstractTableModel {
		/**
		 * Column Names for the model.
		 */
		private final String[] columnNames = new String[] {"Molecule", "Comment"};
		
		/**
		 * The list of molecules and comments.
		 */
		private List<ItemData> rows = new ArrayList<ItemData>();
		
		/**
		 * Reads the data table and populates the model.
		 * @param bt The BufferedDataTable to read
		 * @param columnIndex The column index of the molecule column
		 */
		private void setData(BufferedDataTable bt, int columnIndex) {
			rows.clear();
			for (DataRow row : bt) {
				RowKey r = row.getKey();
				MrvValue m = MoleculeDataTypeConverter.getValue(row.getCell(columnIndex), MrvValue.class);
				String c = comments.get(r);
				rows.add(new ItemData(r, m.getMolecule(), c));
			}
			fireTableDataChanged();
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getColumnName(int index) {
			return columnNames[index];
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex > 0;
		}

		/**
		 * {@inheritDoc}
		 */
		public java.lang.Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
				case 0:
					return Molecule.class;
				default:
					return String.class;
			}
			
		};
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRowCount() {
			return rows.size();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getValueAt(int row, int col) {
			if (row == -1) {
				return null;
			}
			ItemData id = rows.get(row);
			switch(col) {
				case 0:
					return id.molecule;
				case 1:
					return id.comment;
			}
			return null;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setValueAt(Object value, int row, int col) {
			ItemData id = rows.get(row);
			id.comment = (String)value;
			fireTableCellUpdated(row, col);
		}
		
		
	}
	
	/**
	 * Cell Editor for the comments.
	 * @author Luke Bullard
	 *
	 */
	@SuppressWarnings("serial")
	private final class CommentEditor extends AbstractCellEditor implements TableCellEditor {
		private JScrollPane mScroll;
		private JTextArea component = new JTextArea();

		CommentEditor() {
			component.setWrapStyleWord(true);
			component.setLineWrap(true);
			component.setFont(new JLabel().getFont());
			component.setRows(6);
			mScroll = new JScrollPane(component);
			mScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int rowIndex, int vColIndex) {

			((JTextArea) component).setText((String) value);

			return mScroll;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getCellEditorValue() {
			return ((JTextArea) component).getText();
		}
	}

	/**
	 * Cell Renderer for the comments.
	 * @author Luke Bullard
	 *
	 */
	class CommentRenderer implements TableCellRenderer {

		private JLabel mComponent = new JLabel();

		CommentRenderer() {
			mComponent.setOpaque(true);
			mComponent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (value == null) {
				mComponent.setText("");				
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("<html>");
				sb.append(((String) value).replace("\n", "<br>"));
				sb.append("</html>");
				mComponent.setText(sb.toString());							
			}
			
			mComponent.setBackground(isSelected ? table
					.getSelectionBackground() : table.getBackground());
			mComponent.setForeground(isSelected ? table
					.getSelectionForeground() : table.getForeground());
			return mComponent;
		}
	}

	/**
	 * Data holder class for the table model. 
	 * @author Luke Bullard
	 *
	 */
	private static class ItemData {
		private RowKey rowKey;
		private Molecule molecule;
		private String comment;
		
		public ItemData(RowKey r, Molecule m, String c) {
			rowKey 		= r;
			molecule 	= m;
			comment 	= c;
		}			
	}
}
