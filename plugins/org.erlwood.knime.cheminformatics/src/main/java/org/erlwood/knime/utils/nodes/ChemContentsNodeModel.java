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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ContainerTable;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeModel;
import org.knime.core.node.port.PortType;
import org.erlwood.knime.utils.chem.ChemistryUtils;

/**
 * This is an abstract base model implementation for all nodes which need a view
 * for molecules. This class handles the storage of the view data, i.e. the
 * molecules, in a way which allows this data to be read back when the node is
 * restored from the disk. While this at least duplicates (most times probably
 * triplicates) the storage of the molecules, it is the only way provided by
 * Knime to store/restore view data.
 *
 * @author Dimitar Hristozov
 */
public abstract class ChemContentsNodeModel extends NodeModel {
	private BufferedDataTable mExecData = null;
	private ContainerTable mViewData = null;

	/**
	 * @return The data table which the view will use.
	 */
	public final DataTable getViewTable() {
		return mExecData != null ? mExecData : mViewData;
	}

	/**
	 * @return The number of rows in the data table which the view will use.
	 */
	public final int getViewTableRowCount() {
		return null != mExecData ? mExecData.getRowCount()
				: (null != mViewData ? mViewData.getRowCount() : 0);
	}

	/**
	 * {@inheritDoc}
	 */
	protected ChemContentsNodeModel(int nrIn, int nrOut) {
		super(nrIn, nrOut);
	}

	protected ChemContentsNodeModel(PortType[] in, PortType[] out) {
		super(in, out);
	}

	/**
	 * This is an abstract method which replaces
	 * {@link #execute(BufferedDataTable[], ExecutionContext)}. All subclasses
	 * should implement this one and should not override
	 * {@link #execute(BufferedDataTable[], ExecutionContext)}.
	 * 
	 * @param inData
	 *            passed to
	 *            {@link #execute(BufferedDataTable[], ExecutionContext)}
	 * @param exec
	 *            passed to
	 *            {@link #execute(BufferedDataTable[], ExecutionContext)}
	 * @return see {@link #execute(BufferedDataTable[], ExecutionContext)}
	 * @throws Exception
	 *             see {@link #execute(BufferedDataTable[], ExecutionContext)}
	 */
	protected abstract BufferedDataTable[] doExecute(
			final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception;

	/**
	 * Please do not override this method in derived classes! {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {		
     	
		BufferedDataTable[] res = doExecute(inData, exec);
		if (0 < res.length) {
			mExecData = res[0];
		} else if (0 < inData.length) {
			mExecData = inData[0];
		} else {
			mExecData = null;
		}
		mViewData = null;
		return res;
	}

	/**
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		mExecData = null;
		mViewData = null;
	}

	public static final int DEF_NUM_VIS_ROWS = 3;

	/**
	 * Subclasses should override this method if they want different number of
	 * always visible rows inside the MarvinView widget. The default value is 3.
	 * 
	 * @return 3 (three).
	 */
	protected int getNumberOfVisibleRows() {
		return DEF_NUM_VIS_ROWS;
	}

	public static final int DEF_NUM_COLS = 3;

	/**
	 * Subclasses should override this method if they want different number of
	 * columns inside the MarvinView widget. The default value is 3.
	 * 
	 * @return 3 (three).
	 */
	protected int getNumberOfCols() {
		return DEF_NUM_COLS;
	}

	public static final int DEF_NUM_VIS_COLS = 3;

	/**
	 * Subclasses should override this method if they want different number of
	 * always visible columns inside the MarvinView widget. The default value is
	 * three, i.e., same as the default total number of columns. Note that the
	 * total number of rows is calculated based in the actual data.
	 * 
	 * @return 3 (three).
	 */
	protected int getNumberOfVisibleCols() {
		return DEF_NUM_VIS_COLS;
	}

	public static final int DEF_CELL_WIDTH = 200;

	/**
	 * Subclasses should override this method if they want different width of
	 * the cells inside the MarvinView widget. The default value is 200 pixels.
	 * 
	 * @return 200 (two hundred).
	 */
	protected int getCellWidth() {
		return DEF_CELL_WIDTH;
	}

	public static final int DEF_CELL_HEIGHT = 200;

	/**
	 * Subclasses should override this method if they want different height of
	 * the cells inside the MarvinView widget. The default value is 200 pixels.
	 * 
	 * @return 200 (two hundred).
	 */
	protected int getCellHeight() {
		return DEF_CELL_HEIGHT;
	}

	/**
	 * Override to specify which column the view should display initially.
	 * 
	 * @return <code>null</code>.
	 */
	protected String getCurrentColumnName() {
		return null;
	}

	/**
	 * Override when the view itself may change the current column. This default
	 * implementation is empty.
	 * 
	 * @param value
	 *            The name of the current column.
	 */
	protected void setCurrentColumnName(final String value) {
	}

	private static final String CFG_VIEW_FILENAME = "my_chem_view_zip_file.zip";

	/**
	 * Restores the view data from disk. {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		File f = new File(internDir, CFG_VIEW_FILENAME);
		mViewData = f.exists() ? DataContainer.readFromZip(f) : null;
	}

	/**
	 * Writes the view data to disk. This are in fact all chemical structures
	 * contained in the table, if any. While these are restored with the data
	 * table when Knime reconstruct the node from file, there is no way to
	 * access them, so this duplicated storage is needed. {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		File f = new File(internDir, CFG_VIEW_FILENAME);
		if (null == mViewData) {
			DataTableSpec origSpec = mExecData.getDataTableSpec();
			List<DataColumnSpec> newColSpecs = new ArrayList<DataColumnSpec>();
			List<Integer> newColInd = new ArrayList<Integer>();
			for (int c = 0; c < origSpec.getNumColumns(); ++c) {
				DataType dtype = origSpec.getColumnSpec(c).getType();
				if (ChemistryUtils.isCompatibleToChemType(dtype)) {
					newColSpecs.add(origSpec.getColumnSpec(c));
					newColInd.add(c);
				}
			}
			DataContainer dc = new DataContainer(new DataTableSpec(
					newColSpecs.toArray(new DataColumnSpec[newColSpecs
							.size()])));
			DataCell[] newRow = new DataCell[newColInd.size()];
			for (DataRow row : mExecData) {
				int j = 0;
				for (int i : newColInd) {
					newRow[j++] = row.getCell(i);
				}
				dc.addRowToTable(new DefaultRow(row.getKey(), newRow));
			}
			dc.close();
			mViewData = (ContainerTable) dc.getTable();
			DataContainer.writeToZip(mViewData, f, exec);
		}
	}
}
