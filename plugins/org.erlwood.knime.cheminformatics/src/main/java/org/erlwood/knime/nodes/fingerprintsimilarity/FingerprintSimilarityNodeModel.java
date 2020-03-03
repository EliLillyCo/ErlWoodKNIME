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
package org.erlwood.knime.nodes.fingerprintsimilarity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.erlwood.knime.utils.KnimenodeUtils;

/**
 * This is the model implementation of FingerprintSimilarity.
 * 
 * 
 * @author Nikolas Fechner
 */
public class FingerprintSimilarityNodeModel extends NodeModel {
	static final String CFG_QUERY_COLUMN = "query_column";
	static final String CFG_FP_COLUMN = "fp_column";
	static final String CFG_SIM_SEL = "sim_sel";
	static final String CFG_FUSION = "fusion";
	static final String CFG_INTEGRATION = "integration";
	static final String CFG_NEW_COL = "new_col";

	private SettingsModelString mQueryCol = new SettingsModelString(
			CFG_QUERY_COLUMN, "Fingerprint");
	private SettingsModelString mFpCol = new SettingsModelString(CFG_FP_COLUMN,
			"Fingerprint");
	private SettingsModelString mSimSel = new SettingsModelString(CFG_SIM_SEL,
			"Tanimoto");
	private SettingsModelBoolean mFusion = new SettingsModelBoolean(CFG_FUSION,
			false);
	private SettingsModelString mIntegration = new SettingsModelString(
			CFG_INTEGRATION, "MaxSim");
	private SettingsModelString mNewCol = new SettingsModelString(CFG_NEW_COL,
			"Similarity");

	/**
	 * Constructor for the node model.
	 */
	protected FingerprintSimilarityNodeModel() {
		super(2, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

	    if (inData[0].getRowCount() == 0) {
	        throw new IllegalStateException("Input Port 0 is empty!");
	    }
		BufferedDataContainer container = exec
				.createDataContainer(createSpec(inData[1].getDataTableSpec()));
		int queryInd = inData[0].getDataTableSpec().findColumnIndex(
				mQueryCol.getStringValue());
		int dataInd = inData[1].getDataTableSpec().findColumnIndex(
				mFpCol.getStringValue());
		BitVectorValue[] queries = new BitVectorValue[inData[0].getRowCount()];
		RowIterator rows = inData[0].iterator();
		int c = 0;
		while (rows.hasNext()) {
			DataRow row = rows.next();
			if (row.getCell(queryInd).isMissing()) {
				throw new Exception(
						"Missing values in query list are not supported");
			} else {
				queries[c] = (BitVectorValue) row.getCell(queryInd);
			}
			c++;
		}
        
		String measure = mSimSel.getStringValue();
		String method = mIntegration.getStringValue();

		RowIterator rows2 = inData[1].iterator();
		int count = 0;
		while (rows2.hasNext()) {
			DataRow row = rows2.next();
			if (row.getCell(dataInd).isMissing()) {

				List<DataCell> cells = new ArrayList<DataCell>();
				for (int i = 0; i < row.getNumCells(); i++) {
					cells.add(row.getCell(i));

				}

				cells.add(DataType.getMissingCell());

				count += 1;
				exec.checkCanceled();

				exec.setProgress((double) count
						/ (double) inData[1].getRowCount());
				DefaultRow drow = new DefaultRow(row.getKey(), cells);
				container.addRowToTable(drow);
				continue;
			}
			int n = (mFusion.getBooleanValue()) ? queries.length : 1;
			double[] similarities = new double[n];
			
			BitVectorValue vec = (BitVectorValue) row.getCell(dataInd);

			for (int i = 0; i < n; i++) {
				similarities[i] = FingerprintSimilarity.getSimilarity(
						queries[i], vec, measure);
			}
			double similarity = Double.NaN;
			if (mFusion.getBooleanValue()) {
				similarity = MultiQueryFusion.getFusedSimilarity(similarities,
						method);
			} else {
				similarity = similarities[0];
			}
			DoubleCell simcell = new DoubleCell(similarity);
			List<DataCell> cells = new ArrayList<DataCell>();
			for (int i = 0; i < row.getNumCells(); i++) {
				cells.add(row.getCell(i));

			}

			cells.add(simcell);

			count += 1;
			exec.checkCanceled();

			exec.setProgress((double) count / (double) inData[1].getRowCount());
			DefaultRow drow = new DefaultRow(row.getKey(), cells);
			container.addRowToTable(drow);
		}
		container.close();

		return new BufferedDataTable[] { container.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		// check the selected columns still exists
		String columnName = mQueryCol.getStringValue();
		if (inSpecs[0].findColumnIndex(columnName) == -1) {
			throw new InvalidSettingsException("Query column '" + columnName
					+ "' no longer exists.");
		}
		columnName = mFpCol.getStringValue();
		if (inSpecs[1].findColumnIndex(columnName) == -1) {
			throw new InvalidSettingsException("Data column '" + columnName
					+ "' no longer exists.");
		}

		return new DataTableSpec[] { createSpec(inSpecs[1]) };
	}

	private DataTableSpec createSpec(final DataTableSpec spec) {
		String newName = KnimenodeUtils.createColumnName(
				mNewCol.getStringValue(), spec);

		DataColumnSpecCreator appSpecCreator = new DataColumnSpecCreator(
				newName, DoubleCell.TYPE);
		final DataColumnSpec appSpec = appSpecCreator.createSpec();

		DataColumnSpec[] specs = new DataColumnSpec[spec.getNumColumns() + 1];

		for (int i = 0; i < spec.getNumColumns(); i++) {
			specs[i] = spec.getColumnSpec(i);
		}

		specs[spec.getNumColumns()] = appSpec;

		return new DataTableSpec(specs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		mQueryCol.saveSettingsTo(settings);
		mFpCol.saveSettingsTo(settings);
		mSimSel.saveSettingsTo(settings);
		mFusion.saveSettingsTo(settings);
		mIntegration.saveSettingsTo(settings);
		mNewCol.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			mQueryCol.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// this node instance is using the old column model
			SettingsModelColumnName temp = new SettingsModelColumnName(
					CFG_QUERY_COLUMN, "Fingerprint");
			temp.loadSettingsFrom(settings);
			if (!temp.useRowID()) {
				mQueryCol.setStringValue(temp.getStringValue());
			} else {
				mQueryCol.setStringValue(null);
			}
		}

		try {
			mFpCol.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// this node instance is using the old model
			SettingsModelColumnName temp = new SettingsModelColumnName(
					CFG_FP_COLUMN, "Fingerprint");
			temp.loadSettingsFrom(settings);
			if (!temp.useRowID()) {
				mFpCol.setStringValue(temp.getStringValue());
			} else {
				mFpCol.setStringValue(null);
			}
		}

		mSimSel.loadSettingsFrom(settings);
		mFusion.loadSettingsFrom(settings);
		mIntegration.loadSettingsFrom(settings);
		mNewCol.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			mQueryCol.validateSettings(settings);
		} catch (InvalidSettingsException e) {
			// this node instance is using the old column model
			SettingsModelColumnName temp = new SettingsModelColumnName(
					CFG_QUERY_COLUMN, "Fingerprint");
			temp.validateSettings(settings);
		}

		try {
			mFpCol.validateSettings(settings);
		} catch (InvalidSettingsException e) {
			// this node instance is using the old model
			SettingsModelColumnName temp = new SettingsModelColumnName(
					CFG_FP_COLUMN, "Fingerprint");
			temp.validateSettings(settings);
		}

		mSimSel.validateSettings(settings);
		mFusion.validateSettings(settings);
		mIntegration.validateSettings(settings);
		mNewCol.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

}
