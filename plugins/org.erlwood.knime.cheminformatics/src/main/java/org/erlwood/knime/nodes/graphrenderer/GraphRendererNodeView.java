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
package org.erlwood.knime.nodes.graphrenderer;

import java.util.ArrayList;
import java.util.List;

import org.knime.base.node.util.DataArray;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeView;
import org.knime.distmatrix.type.DistanceVectorDataValue;

/**
 * <code>NodeView</code> for the "GraphRenderer" Node. A graph renderer using
 * prefuse java2d library
 * 
 * @author Swanand Gore
 */

@SuppressWarnings("all")
public class GraphRendererNodeView extends NodeView<GraphRendererNodeModel> {

	public class DoubleMatrixOnBufferedDataTableO {
		private BufferedDataTable bd = null;

		public DoubleMatrixOnBufferedDataTableO(BufferedDataTable givenBD) {
			bd = givenBD;
		}

		public List getcorr(int i, int j) {
			List ret = new ArrayList();
			int k = -1;
			for (DataRow row : bd) {
				k += 1;
				if (i != k) {
					continue;
				}
				int cellind = bd.getRowCount() + j;
				if (cellind < row.getNumCells()) {
					String corr = ((StringCell) row.getCell(cellind))
							.getStringValue();
					String[] inds = corr.split(" ");
					for (int n = 0; n < inds.length; n++) {
						ret.add(new Integer(inds[n]));
					}
				} else {
					ret.add(Integer.valueOf(0));
				}
				break;
			}
			return ret;
		}

		public double get(int i, int j) {
	
			if (bd.getRowCount() <= i) {
				throw new IndexOutOfBoundsException("BufferedDataTable "
						+ bd.getRowCount() + " indexing with " + i + " " + j);
			}
			int k = -1;
			for (DataRow row : bd) {
				k += 1;
				if (i != k) {
					continue;
				}
				if (row.getNumCells() <= j) {
					throw new IndexOutOfBoundsException("BufferedDataTable "
							+ bd.getRowCount() + " " + row.getNumCells()
							+ " indexing with " + i + " " + j);
				}
				return ((DoubleCell) row.getCell(j)).getDoubleValue();
			}
			return -999;
		}
	}

	public class DoubleMatrixOnBufferedDataTable {
		private DataArray mTable = null;
		private int mColInd = -1;
		private DistanceVectorDataValue[] mDvs;

		public DoubleMatrixOnBufferedDataTable(DataArray givenDA) {
			mTable = givenDA;
			DataTableSpec spec = mTable.getDataTableSpec();
			for (int c = 0; c < spec.getNumColumns() && -1 == mColInd; ++c) {
				if (spec.getColumnSpec(c).getType()
						.isCompatible(DistanceVectorDataValue.class))
					mColInd = c;
			}

			mDvs = new DistanceVectorDataValue[mTable.size()];
			int i = 0;
			for (DataRow row : mTable) {
				mDvs[i] = (DistanceVectorDataValue) row.getCell(mColInd);
				i += 1;
			}

		}

		public List getcorr(int i, int j) {
			List ret = new ArrayList();

			ret.add(0);
			return ret;
		}

		public double get(int i, int j) {
			return mDvs[i].getDistance(mDvs[j]);
		}
	}

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel
	 *            The model (class: {@link GraphRendererNodeModel})
	 */
	protected GraphRendererNodeView(final GraphRendererNodeModel nodeModel) {
		super(nodeModel);
		DataArray vd = nodeModel.getViewData();
		DataArray md = nodeModel.getMatData();

		List<String> nodes = new ArrayList<String>(), smiles = new ArrayList<String>();
		List<Double> properties = new ArrayList<Double>();
		double maxprop = -1e10, minprop = 1e10;
		int ri = -1;
		for (DataRow row : vd) {
			ri += 1;
			String nodename = null;
			for (int ci = 0; ci < row.getNumCells(); ci++) {
				if (ci == nodeModel.getSmilesColIndex()) {
					if (row.getCell(ci) instanceof SmilesValue) {
						SmilesValue newName = (SmilesValue) row.getCell(ci);
						smiles.add(((SmilesValue) row.getCell(ci))
								.getSmilesValue());
					} else {
						smiles.add((row.getCell(ci)).toString());
					}
				}
				if (ci == nodeModel.getNamesColIndex()) {
					nodename = ((StringCell) row.getCell(ci)).getStringValue();
					nodes.add(nodename);
				}
				if (ci == nodeModel.getPropsColIndex()) {
					Double val = null;
					try {
						val = new Double(
								((DoubleCell) row.getCell(ci)).getDoubleValue());
					} catch (Exception e) {
						val = new Double(
								((IntCell) row.getCell(ci)).getIntValue());
					}
					properties.add(val);
					if (val.doubleValue() < minprop) {
						minprop = val.doubleValue();
					}
					if (val.doubleValue() > maxprop) {
						maxprop = val.doubleValue();
					}
				}
			}
		}
		DoubleMatrixOnBufferedDataTable dmat = new DoubleMatrixOnBufferedDataTable(
				md);
		GraphView gv = new GraphView(nodes.size(), dmat);
		ri = -1;
		for (DataRow row : vd) {
			ri += 1;
			int color = 0;
			try {
				color = new Double((properties.get(ri) - minprop)
						/ (maxprop - minprop) * 100.0).intValue();
			} catch (Exception e) {
			}
			gv.addNode(nodes.get(ri), color, smiles.get(ri), properties.get(ri)
					.doubleValue());
			gv.addEdge((String) nodes.get(ri), "dummy", -1, 20);
		}

		double distcut = 0.5, cliffcutoff = 100;
		int nmols = md.size();
		for (ri = 0; ri < nmols; ++ri) {
			for (int ci = 0; ci < nmols; ++ci) {
				if (ri == ci) {
					continue;
				}
				Double val = dmat.get(ri, ci);
				if (val < nodeModel.getEdgeCutoff()) {
					double cliff = Math.abs(properties.get(ri)
							- properties.get(ci))
							/ val;
					if (cliff > nodeModel.getCliffCutoff()) {
						gv.addEdge(nodes.get(ri), nodes.get(ci), 1, val);
					} else {
						gv.addEdge(nodes.get(ri), nodes.get(ci), 0, val);
					}
				}
			}
		}
		setComponent(gv);
		gv.onclick(nodes.get(0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void modelChanged() {

		GraphRendererNodeModel nodeModel = (GraphRendererNodeModel) getNodeModel();
		assert nodeModel != null;

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

}
