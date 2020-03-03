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
package org.erlwood.knime.nodes.similarityviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.AbstractTableModel;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeView;
import org.knime.distmatrix.type.DistanceVectorDataValue;

import chemaxon.struc.Molecule;

/**
 * <code>NodeView</code> for the "SimilarityViewer" Node. Displays similarity
 * matrix as a heat map. Also allows the sorting with a "query" molecule.
 * 
 * @author Dimitar Hristozov
 */
public final class SimilarityViewerNodeViewTwo extends
		NodeView<SimilarityViewerNodeModel> implements ItemListener,
		ActionListener {
	private static final NodeLogger LOG = NodeLogger.getLogger(SimilarityViewerNodeViewTwo.class);
	private JComboBox mCmbMol = new JComboBox();
	private JTable mTbl;

	private Map<String, Object[]> mColData = new HashMap<String, Object[]>();

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel
	 *            The model (class: {@link SimilarityViewerNodeModel})
	 */
	@SuppressWarnings("serial")
	protected SimilarityViewerNodeViewTwo(
			final SimilarityViewerNodeModel nodeModel) {
		super(nodeModel);
		JPanel mainPnl = new JPanel(new BorderLayout());

		JPanel selPnl = new JPanel();
		selPnl.setLayout(new BoxLayout(selPnl, BoxLayout.X_AXIS));
		selPnl.setBorder(BorderFactory.createTitledBorder("Query molecule"));

		JLabel lbl = new JLabel("Enter identifier or select molecule");
		selPnl.add(lbl);
		selPnl.add(Box.createHorizontalStrut(10));
		selPnl.add(lbl);
		selPnl.add(Box.createHorizontalStrut(10));
		selPnl.add(mCmbMol);
		populateTargetComboBox();
		ComboBoxRenderer renderer = new ComboBoxRenderer();
		mCmbMol.setRenderer(renderer);
		Dimension d = new Dimension(200, 22);
		mCmbMol.setPreferredSize(d);
		mCmbMol.setMinimumSize(d);
		mCmbMol.setEditable(true);
		selPnl.add(Box.createHorizontalGlue());

		mainPnl.add(selPnl, BorderLayout.NORTH);

		selPnl = new JPanel(new BorderLayout());
		selPnl.setBorder(BorderFactory.createTitledBorder("Ranked list"));
		mTbl = new JTable(new TblMdl());
		mTbl.getColumnModel().getColumn(2).setPreferredWidth(150);
		mTbl.setRowHeight(150);
		JScrollPane scrollPane = new JScrollPane(mTbl);
		mTbl.setFillsViewportHeight(true);
		selPnl.add(scrollPane, BorderLayout.CENTER);
		mainPnl.add(selPnl, BorderLayout.CENTER);

		queryChanged(0);
		this.setComponent(mainPnl);

		JMenu m = this.getJMenuBar().getMenu(0);
		m.insertSeparator(2);
		m.insert(new AbstractAction("Save as SDF") {
			public void actionPerformed(ActionEvent arg0) {
				exportSDF();
			}
		}, 3);
		m.insert(new AbstractAction("Save as CSV") {
			public void actionPerformed(ActionEvent arg0) {
				exportCSV();
			}
		}, 4);
		m.insertSeparator(5);
		buildColumnsMenu();
	}

	private void buildColumnsMenu() {
		if (null == getNodeModel().getData()) {
			return;
		}
		final DataTableSpec spec = getNodeModel().getData().getDataTableSpec();
		JMenu m = (1 == this.getJMenuBar().getMenuCount()) ? this.getJMenuBar()
				.add(new JMenu("Columns")) : this.getJMenuBar().getMenu(1);
		m.removeAll();
		for (int c = 0; c < spec.getNumColumns(); ++c) {
			final JCheckBoxMenuItem cbx = new JCheckBoxMenuItem(spec
					.getColumnSpec(c).getName());
			cbx.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					String colName = cbx.getText();
					int coli = spec.findColumnIndex(colName);
					TblMdl mdl = (TblMdl) mTbl.getModel();
					LOG.debug(cbx.getText() + " ? " + cbx.getState());
					if (cbx.getState()) {
						Class<?> cls = spec.getColumnSpec(colName).getType()
								.isCompatible(IntValue.class) ? Integer.class
								: spec.getColumnSpec(colName).getType()
										.isCompatible(DoubleValue.class) ? Double.class
										: String.class;

						if (!mColData.containsKey(colName)) {
							mColData.put(colName, new Object[getNodeModel()
									.getData().getRowCount()]);
							int rowi = 0;
							for (DataRow row : getNodeModel().getData()) {
								mColData.get(colName)[rowi] = row.getCell(coli)
										.toString();
								rowi += 1;
							}
						}
						mdl.addColumn(new TblColumn(colName, cls));
					} else {
						if (mColData.containsKey(colName)) {
							mdl.removeColumn(colName);
						}
					}
				}
			});
			m.add(cbx);
		}
	}

	private void exportSDF() {
		if (null == getNodeModel() || null == getNodeModel().getData()) {
			return;
		}
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this.getComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (fc.getSelectedFile().exists()) {
				int r = JOptionPane.showConfirmDialog(this.getComponent(),
						"File " + fc.getSelectedFile()
								+ " exists. Do you want to overwrite it?",
						"Overwrite File", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (r != JOptionPane.YES_OPTION) {
					return;
				}
			}
			try {
				Molecule[] mols = getNodeModel().getMols();
				BufferedWriter bwr = new BufferedWriter(new FileWriter(
						fc.getSelectedFile()));
				for (int i = 0; i < mols.length; ++i) {
					Molecule mol = mols[(int) mTblVals[i][0]];
					mol.setPropertyObject("Rank", Integer.valueOf(i + 1));
					mol.setPropertyObject("Identifier", getNodeModel()
							.getRowIds()[(int) mTblVals[i][0]]);
					mol.setPropertyObject("Distance_to_target",
							String.format("%.3f", mTblVals[i][1]));
					TblMdl mdl = (TblMdl) mTbl.getModel();
					for (int c = 4; c < mdl.getColumnCount(); ++c) {
						String fmt = "%.3f";
						if (mdl.mColumns.get(c).getColumnClass()
								.equals(Integer.class)) {
							fmt = "%d";
						} else if (mdl.mColumns.get(c).getColumnClass()
								.equals(String.class)) {
							fmt = "%s";
						}
						mol.setPropertyObject(
								mdl.getColumnName(c),
								String.format(
										fmt,
										mColData.get(mdl.getColumnName(c))[(int) mTblVals[i][0]]));
					}
					bwr.write(mol.toFormat("sdf"));
				}
				bwr.flush();
				bwr.close();
			} catch (IOException io) {
				JOptionPane.showMessageDialog(this.getComponent(),
						"Error while writing to " + fc.getSelectedFile(),
						"Error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				JOptionPane.showMessageDialog(this.getComponent(),
						"Error while converting to SD format.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void exportCSV() {
		if (null == getNodeModel() || null == getNodeModel().getData()) {
			return;
		}
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this.getComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (fc.getSelectedFile().exists()) {
				int r = JOptionPane.showConfirmDialog(this.getComponent(),
						"File " + fc.getSelectedFile()
								+ " exists. Do you want to overwrite it?",
						"Overwrite File", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (r != JOptionPane.YES_OPTION) {
					return;
				}
			}
			try {
				Molecule[] mols = getNodeModel().getMols();
				BufferedWriter bwr = new BufferedWriter(new FileWriter(
						fc.getSelectedFile()));
				TblMdl mdl = (TblMdl) mTbl.getModel();
				bwr.write(mdl.getColumnName(0));
				for (int c = 1; c < mdl.getColumnCount(); ++c) {
					bwr.write(",");
					bwr.write(mdl.getColumnName(c));
				}
				bwr.newLine();
				for (int i = 0; i < mols.length; ++i) {
					bwr.write(mdl.getValueAt(i, 0).toString());
					for (int c = 1; c < mdl.getColumnCount(); ++c) {
						bwr.write(",");
						if ("Structure".equals(mdl.getColumnName(c))) {
							try {
								bwr.write(mols[(int) mTblVals[i][0]]
										.toFormat("smiles:-H"));
							} catch (Exception ex) {
								bwr.write("Unable to convert to SMILES");
							}
						} else {
							bwr.write(mdl.getValueAt(i, c).toString());
						}
					}
					bwr.newLine();
				}
				bwr.flush();
				bwr.close();
			} catch (IOException io) {
				JOptionPane.showMessageDialog(this.getComponent(),
						"Error while writing to " + fc.getSelectedFile(),
						"Error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				JOptionPane.showMessageDialog(this.getComponent(),
						"Error while converting to SD format.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void populateTargetComboBox() {
		if (null == getNodeModel().getData()) {
			return;
		}
		mCmbMol.removeItemListener(this);
		mCmbMol.removeAllItems();
		for (int i = 0; i < getNodeModel().getMols().length; ++i) {
			mCmbMol.addItem(getNodeModel().getRowIds()[i]);
		}
		mCmbMol.addItemListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void modelChanged() {
		SimilarityViewerNodeModel nodeModel = (SimilarityViewerNodeModel) getNodeModel();
		assert nodeModel != null;
		if (null != nodeModel.getData()) {
			((TblMdl) mTbl.getModel()).resetColumns();
			buildColumnsMenu();
			populateTargetComboBox();
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

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if (source.equals(mCmbMol)) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				queryChanged(mCmbMol.getSelectedIndex());
			}
		}
	}

	private int mQuery = -1;
	private double[][] mTblVals;
	private boolean mAsc;

	public void queryChanged(int newq) {
		if (mQuery != newq) {
			mQuery = newq;
			DistanceVectorDataValue[] dst = getNodeModel().getDvs();
			DistanceVectorDataValue qDst = dst[newq];

			mTblVals = new double[dst.length][2];

			for (int i = 0; i < dst.length; ++i) {
				mTblVals[i][0] = i;
				mTblVals[i][1] = qDst.getDistance(dst[i]);
			}

			mAsc = getNodeModel().getmIsDm().getBooleanValue();
			Arrays.sort(mTblVals, new ArComparator(1, !mAsc));

			mTbl.invalidate();
			mTbl.repaint();
		}
	}

	private class ArComparator implements Comparator<double[]> {
		private int mCol;
		private boolean mDesc;

		public ArComparator(int onCol, boolean desc) {
			mCol = onCol;
			mDesc = desc;
		}

		public int compare(double[] o1, double[] o2) {
			return (o1[mCol] == o2[mCol] ? 0
					: mDesc ? (o1[mCol] > o2[mCol] ? -1 : 1)
							: (o1[mCol] > o2[mCol] ? 1 : -1));
		}
	}

	@SuppressWarnings("serial")
	class ComboBoxRenderer extends JLabel implements ListCellRenderer {
		public ComboBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (null != value) {
				int selectedIndex = index;
				if (isSelected) {
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
				} else {
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}

				ImageIcon icon = getNodeModel().getIcons()[selectedIndex];
				setIcon(icon);
			}
			setAlignmentX(RIGHT_ALIGNMENT);
			setText(value.toString());
			return this;
		}
	}

	public void actionPerformed(ActionEvent ae) {
	}

	private class TblColumn {
		private String mName;
		private Class<?> mCls;

		public TblColumn(String name, Class<?> cls) {
			mName = name;
			mCls = cls;
		}

		public String getName() {
			return mName;
		}

		public Class<?> getColumnClass() {
			return mCls;
		}

	}

	@SuppressWarnings("serial")
	private class TblMdl extends AbstractTableModel {
		private List<TblColumn> mColumns;

		public TblMdl() {
			super();
			createDefaultColumns();
		}

		public TblMdl(List<TblColumn> additionalColumns) {
			super();
			createDefaultColumns();
			mColumns.addAll(additionalColumns);
		}

		private void createDefaultColumns() {
			mColumns = new ArrayList<TblColumn>();
			mColumns.add(new TblColumn("Rank", Integer.class));
			mColumns.add(new TblColumn("Id", String.class));
			mColumns.add(new TblColumn("Structure", ImageIcon.class));
			mColumns.add(new TblColumn(getNodeModel().getmIsDm()
					.getBooleanValue() ? "Distance" : "Similarity",
					Double.class));
		}

		public void addColumn(TblColumn col) {
			mColumns.add(col);
			fireTableStructureChanged();
		}

		public void removeColumn(TblColumn col) {
			mColumns.remove(col);
			fireTableStructureChanged();
		}

		public void removeColumn(String name) {
			Iterator<TblColumn> iter = mColumns.iterator();
			boolean removed = false;
			int c = 0;
			while (iter.hasNext() && !removed) {
				if (c > 3 && iter.next().getName().equals(name)) {
					iter.remove();
					removed = true;
				}
				c += 1;
			}
			if (removed) {
				fireTableStructureChanged();
			}
		}

		public void resetColumns() {
			for (int i = mColumns.size() - 1; i > 3; --i) {
				mColumns.remove(i);
			}
			fireTableStructureChanged();
		}

		public int getColumnCount() {
			return mColumns.size();
		}

		public int getRowCount() {
			return getNodeModel().getDvs().length;
		}

		public Object getValueAt(int row, int col) {
			if (0 == col) {
				return row + 1;
			} else if (1 == col) {
				return getNodeModel().getRowIds()[(int) mTblVals[row][0]];
			} else if (2 == col) {
				return getNodeModel().getIcons()[(int) mTblVals[row][0]];
			} else if (3 == col) {
				return mTblVals[row][1];
			} else {
				return mColData.get(mColumns.get(col).getName())[(int) mTblVals[row][0]];
			}
		}

		public String getColumnName(int col) {
			return mColumns.get(col).getName();
		}

		public Class<?> getColumnClass(int col) {
			return mColumns.get(col).getColumnClass();
		}

	}

}
