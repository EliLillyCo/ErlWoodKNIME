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
package org.erlwood.knime.utils.nodes;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import jp.co.infocom.cheminfo.marvin.type.MrvValue;

import org.knime.core.data.AdapterCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.node.NodeView;
import org.erlwood.knime.nodes.molfilereader.MolFileReaderNodeModel;
import org.erlwood.knime.utils.chem.ChemistryUtils;
import org.erlwood.knime.utils.chem.MViewPaneWrapper;

import chemaxon.formats.MolImporter;
import chemaxon.marvin.beans.MViewPane;
import chemaxon.struc.Molecule;

/**
 * A parent for all views which display molecules or reactions using ChemAxon's
 * MViewPane class
 * 
 * @author Dimitar Hristozov
 */
public class ChemContentsNodeView extends NodeView<ChemContentsNodeModel>
		implements ActionListener {
	private MViewPane mViewPane;
	private JMenu mMenuColumn;
	private ButtonGroup btngrColumn;
	private String strCurrent;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel
	 *            The model (class: {@link MolFileReaderNodeModel})
	 */
	public ChemContentsNodeView(final ChemContentsNodeModel nodeModel) {
		super(nodeModel);
		btngrColumn = new ButtonGroup();
		mMenuColumn = new JMenu("Column", true);
		mMenuColumn.setMnemonic(KeyEvent.VK_C);
		this.getJMenuBar().add(mMenuColumn);
		mViewPane = new MViewPane();
		mViewPane.setBorderWidth(1);
		mViewPane.setBackground(Color.LIGHT_GRAY);
		mViewPane.setMolbg(Color.WHITE);
		mViewPane.setDetachable(false);
		this.setComponent(mViewPane);
		strCurrent = nodeModel.getCurrentColumnName();
	}

	/**
	 * Sets the parameters for the underlying MViewPane.
	 * 
	 * @param nMols
	 *            The number of molecules (rows) in the input table. The final
	 *            number of rows in the View widget is calculated based on the
	 *            desired number of columns.
	 */
	protected void setViewParams(int nMols) {
		ChemContentsNodeModel model = getNodeModel();
		int viRows = model.getNumberOfVisibleRows();
		int nCols = model.getNumberOfCols();
		int nRows = nMols / nCols;
		while (nRows * nCols < nMols) {
			nRows += 1;
		}
		
		int visCols = model.getNumberOfVisibleCols();
		mViewPane.setParams(
		// set total row count
				"rows=" + (nRows >= viRows ? nRows : viRows) + "\n"
						// set visible row count
						+ "visibleRows="
						+ viRows
						+ "\n"
						// set column count
						+ "cols="
						+ nCols
						+ "\n"
						// visible column count
						+ "visibleCols="
						+ visCols
						+ "\n"
						// 2:1 -> 2 rows and 1 columns per cell;
						// L:0:0:1:1:c:n:0:1 -> first row and col (0:0:1:1) of
						// the
						// cell is a label (L), centered (c) ...
						// M:1:0:1:1:c:b:1:1 -> second row in the first (and
						// only)
						// column is a molecule (M), centered (c) ...
						+ "layout=:2:1:L:0:0:1:1:c:n:0:1:M:1:0:1:1:c:b:1:1\n"
						// L:10b -> label is displayed in 10pt bold fonts
						// M:200:200 -> molecule is displayed in a 200x200
						// pixels area
						+ "param=:L:10b:M:" + model.getCellWidth() + ": "
						+ model.getCellHeight() + "\n");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void modelChanged() {
		ChemContentsNodeModel model = getNodeModel();
		if (null == model) {
			return;
		}
		updateGUI(model.getViewTableRowCount(), model.getViewTable(), true);
	}

	private void updateGUI(int rowCount, DataTable dataTable,
			boolean updateMenu) {
		if (null != dataTable) {
			if (updateMenu) {
				populateMenu(dataTable);
			}
			populateView(rowCount, dataTable);
		}
	}

	private void populateMenu(DataTable dataTable) {
		DataTableSpec spec = dataTable.getDataTableSpec();
		int nCol = spec.getNumColumns(), nItems = btngrColumn
				.getButtonCount();
		JRadioButtonMenuItem rdItem = null;
		boolean wasCurrentSet = false;
		for (int i = 0; i < nCol; ++i) {
			if (ChemistryUtils.isCompatibleToChemType(spec.getColumnSpec(i)
					.getType())) {
				String text = spec.getColumnSpec(i).getName();
				if (i < nItems) {
					rdItem = (JRadioButtonMenuItem) mMenuColumn.getItem(i);
				} else {
					rdItem = new JRadioButtonMenuItem(text);
					rdItem.addActionListener(this);
					btngrColumn.add(rdItem);
					mMenuColumn.add(rdItem);
				}
				rdItem.setText(text);
				if (text.equals(strCurrent)) {
					rdItem.setSelected(true);
					wasCurrentSet = true;
				}
			}
		}
		if (!wasCurrentSet && 0 < mMenuColumn.getItemCount()) {
			rdItem = (JRadioButtonMenuItem) mMenuColumn.getItem(0);
			rdItem.setSelected(true);
			strCurrent = rdItem.getText();
		}
		getNodeModel().setCurrentColumnName(strCurrent);
		for (int i = nItems - 1; i >= nCol; --i) {
			rdItem = (JRadioButtonMenuItem) mMenuColumn.getItem(i);
			btngrColumn.remove(rdItem);
			mMenuColumn.remove(rdItem);
		}
	}

	private void populateView(int rowCount, DataTable dataTable) {
		setViewParams(rowCount);
		DataTableSpec spec = dataTable.getDataTableSpec();
		int colInd = spec.findColumnIndex(strCurrent);
		if (-1 == colInd) {
			return;
		}
		int m = 0;
		RowIterator rowIter = dataTable.iterator();
		while (rowIter.hasNext()) {
			Molecule mol = null;
			DataRow row = rowIter.next();
			DataCell cell = row.getCell(colInd);
			if (cell instanceof AdapterCell && ((AdapterCell)cell).isAdaptable(MrvValue.class)) {
				mol = ((AdapterCell)cell).getAdapter(MrvValue.class).getMolecule();
			} else {
				try {
					mol = MolImporter.importMol(cell.toString());
				} catch (Exception ex) {
					mol = null;
				}
			}
			mViewPane.setL(m, String.valueOf(m + 1));
			MViewPaneWrapper.setM(mViewPane, m, mol);
			m += 1;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onClose() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {
	}

	/**
	 * Changes the displayed column as result of activating the corresponding
	 * menu item.
	 * 
	 * @param e
	 *            The event fired by Swing.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JRadioButtonMenuItem) {
			JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
			if (!item.getText().equals(strCurrent)) {
				strCurrent = item.getText();
				ChemContentsNodeModel model = getNodeModel();
				model.setCurrentColumnName(strCurrent);
				updateGUI(model.getViewTableRowCount(), model.getViewTable(),
						false);
			}
		}
	}

}
