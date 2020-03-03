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
package org.erlwood.knime.nodes.marvinsketch;

import java.awt.Color;
import java.util.List;

import org.knime.core.node.NodeView;
import org.erlwood.knime.utils.chem.MViewPaneWrapper;

import chemaxon.marvin.beans.MViewPane;
import chemaxon.struc.Molecule;

/**
 * A view which displays the molecules created with the MarvinSketch node.
 * 
 * @author Dimitar Hristozov
 */
public class MarvinSketchNodeMolView extends NodeView<MarvinSketchNodeModel> {
	private MViewPane mViewPane;
	private boolean mIsRxn;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel
	 *            The model (class: {@link MolFileReaderNodeModel})
	 */
	protected MarvinSketchNodeMolView(final MarvinSketchNodeModel nodeModel,
			boolean isRxn) {
		super(nodeModel);
		mIsRxn = isRxn;
		mViewPane = new MViewPane();
		mViewPane.setBorderWidth(1);
		mViewPane.setBackground(Color.LIGHT_GRAY);
		mViewPane.setMolbg(Color.WHITE);
		mViewPane.setDetachable(false);
		this.setComponent(mViewPane);
	}

	/**
	 * Sets the parameters for the underlying MViewPane. The number of columns
	 * is fixed to 3.
	 * 
	 * @param nRows
	 *            The desired number of rows.
	 */
	protected void setViewParams(int nRows) {
		mViewPane.setParams(
		// set total row count
				"rows=" + (nRows >= 3 ? nRows : 3) + "\n"
				// set visible row count
						+ "visibleRows=" + 3 + "\n"
						// set column count
						+ "cols=" + 3 + "\n"
						// visible column count
						+ "visibleCols=" + 3 + "\n"
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
						+ "param=:L:10b:M:200:200\n");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void modelChanged() {
		MarvinSketchNodeModel mrvModel = (MarvinSketchNodeModel) getNodeModel();
		assert (mrvModel != null);
		List<Molecule> molsList = null;
		if (!mIsRxn) {
			mrvModel.getMolecules();
		} else {
			molsList = mrvModel.getReactions();
		}
		if (null == molsList) {
			mrvModel.populateLists();
			if (!mIsRxn) {
				molsList = mrvModel.getMolecules();
			} else {
				molsList = mrvModel.getReactions();
			}
		}
		int nrows = (molsList.size() / 3);
		while (molsList.size() > nrows * 3) {
			nrows += 1;
		}
		setViewParams(nrows);
		for (int m = 0; m < molsList.size(); ++m) {
			mViewPane.setL(m, String.valueOf(m + 1));
			MViewPaneWrapper.setM(mViewPane, m, molsList.get(m));
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
}
