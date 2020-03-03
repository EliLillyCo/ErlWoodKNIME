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
package org.erlwood.knime.nodes.desirability;

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
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.erlwood.knime.utils.KnimenodeUtils;
import org.erlwood.knime.utils.gui.DesirabilityObjectivesBean;

/**
 * This is the model implementation of Desirability. Modification of the Pareto
 * Node to perform as a Desirability Node, following standard Desirability
 * criterion.
 * 
 * @author David Thorner & Dimitar Hristozov
 */
public class DesirabilityNodeModel extends NodeModel {
	private static final NodeLogger LOG = NodeLogger
			.getLogger(DesirabilityNodeModel.class);
	static final String CFG_SETTINGS = "DesirabilityRankingNodeModel_settings";
	private SettingsModelString mSettings = new SettingsModelString(
			CFG_SETTINGS, null);
	private List<DesirabilityObjectivesBean.DesirabilityCondition> mConfProps = new ArrayList<DesirabilityObjectivesBean.DesirabilityCondition>();

	private static final int UNDESIRABLE = DesirabilityObjectivesBean.DesirabilityCondition.UNDESIRABLE;
	private static final int DESIRABLE = DesirabilityObjectivesBean.DesirabilityCondition.DESIRABLE;

	/**
	 * Constructor for the node model.
	 */
	protected DesirabilityNodeModel() {
		super(1, 1);
	}

	private static final int MIN_COL_VALUE = 0;
	private static final int MAX_COL_VALUE = 1;
	private static final int COLUMN_WEIGHT = 2;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
	
		if (0 == mConfProps.size()) {
			throw new Exception("No columns selected for optimization");
		}
		BufferedDataTable dataTable = inData[0];
		boolean[] rowHasMissingCell = new boolean[dataTable.getRowCount()];
		double[][] data = new double[dataTable.getRowCount()][mConfProps.size()];
		int[] colInd = new int[mConfProps.size()];
		double[][] dataMinMaxWeight = new double[mConfProps.size()][3];
		for (int c = 0; c < mConfProps.size(); ++c) {
			DesirabilityObjectivesBean.DesirabilityCondition cond = mConfProps
					.get(c);
			colInd[c] = dataTable.getSpec().findColumnIndex(
					cond.getPropertyName());
			dataMinMaxWeight[c] = new double[] { Double.MAX_VALUE,
					Double.MIN_VALUE, 1 };
			if (-1 == colInd[c]) {
				throw new Exception("Invalid column index for "
						+ cond.getPropertyName());
			}
		}
		double mTotalOps = dataTable.getRowCount() * dataTable.getRowCount()
				+ dataTable.getRowCount();
		int r = 0;
		for (DataRow row : dataTable) {
			for (int c = 0; c < mConfProps.size() && !rowHasMissingCell[r]; ++c) {
				DesirabilityObjectivesBean.DesirabilityCondition cond = mConfProps
						.get(c);
				if (!row.getCell(colInd[c]).isMissing()) {
					double val = ((DoubleValue) row.getCell(colInd[c]))
							.getDoubleValue();
					data[r][c] = val;
					if (val < dataMinMaxWeight[c][MIN_COL_VALUE]) {
						dataMinMaxWeight[c][MIN_COL_VALUE] = val;
					}
					if (val > dataMinMaxWeight[c][MAX_COL_VALUE]) {
						dataMinMaxWeight[c][MAX_COL_VALUE] = val;
					}
					if (UNDESIRABLE == cond.getOptimizationMethod()) {
						dataMinMaxWeight[c][COLUMN_WEIGHT] = -cond.getWeight();
					} else if (DESIRABLE == cond.getOptimizationMethod()) {
						dataMinMaxWeight[c][COLUMN_WEIGHT] = cond.getWeight();
					} else {
						throw new Exception("Something bad has happened.");
					}
				} else {
					rowHasMissingCell[r] = true;
				}
			}
			r += 1;
			exec.setProgress(r / mTotalOps);
		}
		final double[] desirabilityScores = calcDesirabilityScores(data,
				dataMinMaxWeight, mConfProps, exec, rowHasMissingCell);
		final boolean[] missingCell = rowHasMissingCell;
		ColumnRearranger colRearr = new ColumnRearranger(
				inData[0].getDataTableSpec());
		colRearr.append(new SingleCellFactory(createColSpec(inData[0]
				.getDataTableSpec())) {
			private int ind = -1;

			@Override
			public DataCell getCell(DataRow row) {
				ind += 1;
				if (!missingCell[ind]) {
					return new DoubleCell(desirabilityScores[ind]);
				} else {
					return DataType.getMissingCell();
				}
			}
		});

		return new BufferedDataTable[] { exec.createColumnRearrangeTable(
				inData[0], colRearr, exec) };

	}

	private double[] calcDesirabilityScores(double[][] data,
			double[][] minMaxWeight,
			List<DesirabilityObjectivesBean.DesirabilityCondition> conds,
			final ExecutionContext exec, boolean[] missingRow)
			throws CanceledExecutionException {
		double[] res = new double[data.length];

		double mn = Double.MAX_VALUE, mx = Double.MIN_VALUE;

		for (int row = 0; row < data.length; ++row) {
			if (missingRow[row]) {
				continue;
			}
			res[row] = 1;
			for (int col = 0; col < data[row].length; ++col) {
				if (minMaxWeight[col][COLUMN_WEIGHT] < 0) {
					res[row] *= abs(minMaxWeight[col][COLUMN_WEIGHT])
							* (data[row][col] - minMaxWeight[col][MIN_COL_VALUE])
							/ (minMaxWeight[col][MAX_COL_VALUE] - minMaxWeight[col][MIN_COL_VALUE]);
				}
				if (minMaxWeight[col][COLUMN_WEIGHT] > 0) {
					res[row] *= minMaxWeight[col][COLUMN_WEIGHT]
							* (minMaxWeight[col][MAX_COL_VALUE] - data[row][col])
							/ (minMaxWeight[col][MAX_COL_VALUE] - minMaxWeight[col][MIN_COL_VALUE]);
				}

			}

			if (res[row] < mn) {
				mn = res[row];
			}
			if (res[row] > mx) {
				mx = res[row];
			}
		}

		return res;
	}

	private double abs(double d) {
		if (d < 0) {
			d = d * -1;
		}

		return (d);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		if (0 == mConfProps.size()) {
			throw new InvalidSettingsException(
					"No columns selected for optimization");
		}
		if (mSettings != null && !mSettings.getStringValue().equals("")) {

			boolean bChanged = false;
			// Loop on the configured settings to ensure they are available in
			// the incoming
			// table spec. If not then remove the missing entities.
			DataTableSpec tableSpec = inSpecs[0];
			List<DesirabilityObjectivesBean.DesirabilityCondition> lst = new ArrayList<DesirabilityObjectivesBean.DesirabilityCondition>(
					mConfProps);
			for (DesirabilityObjectivesBean.DesirabilityCondition cond : lst) {
				// Ok, the name is missing so remove it from the config.
				if (!tableSpec.containsName(cond.getPropertyName())) {
					mConfProps.remove(cond);
					LOG.warn("Column [" + cond.getPropertyName()
							+ "] no longer in the TableSpec - removed.");
					bChanged = true;
				}
			}
			if (bChanged) {
				StringBuilder res1 = new StringBuilder();
				StringBuilder res2 = new StringBuilder();
				List<String> lstAvailable = new ArrayList<String>();
				for (String colName : tableSpec.getColumnNames()) {
					lstAvailable.add(colName);
				}
				for (DesirabilityObjectivesBean.DesirabilityCondition c : mConfProps) {
					res2.append("configured_prop=").append(c).append("\n");
					lstAvailable.remove(c.getPropertyName());
				}
				for (String colName : lstAvailable) {
					res1.append("available_prop=").append(colName).append("\n");
				}
				mSettings.setStringValue(res1.toString() + res2.toString());
				if (mConfProps.size() == 0) {

					throw new InvalidSettingsException(
							"No columns selected for optimization");
				}
			}

		}

		DataColumnSpec newColSpec = createColSpec(inSpecs[0]);
		DataTableSpec appendedSpec = new DataTableSpec(newColSpec);
		DataTableSpec outputSpec = new DataTableSpec(inSpecs[0], appendedSpec);
		return new DataTableSpec[] { outputSpec };
	}

	private DataColumnSpec createColSpec(DataTableSpec spec) {
		String name = KnimenodeUtils.createColumnName("Desirability Score",
				spec);
		return new DataColumnSpecCreator(name, DoubleCell.TYPE).createSpec();
	}

	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		mSettings.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		mSettings.loadSettingsFrom(settings);
		List<String> avProps = new java.util.ArrayList<String>();
		List<String> confProps = new java.util.ArrayList<String>();

		DesirabilityObjectivesBean.parseConfString(mSettings.getStringValue(),
				avProps, confProps);
		mConfProps.clear();
		for (String cs : confProps) {

			DesirabilityObjectivesBean.DesirabilityCondition pc = DesirabilityObjectivesBean.DesirabilityCondition
					.createCondition(cs);
			if (null == pc) {
				throw new InvalidSettingsException(
						"Invalid settings string - IMPLEMENTATION ERROR!.");
			}
			mConfProps.add(pc);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		String s = settings.getString(CFG_SETTINGS);
		List<String> avProps = new java.util.ArrayList<String>();
		List<String> confProps = new java.util.ArrayList<String>();
		DesirabilityObjectivesBean.parseConfString(s, avProps, confProps);
		if (0 == confProps.size()) {
			throw new InvalidSettingsException(
					"Please select at least one property.");
		}
		for (String str : confProps) {
			DesirabilityObjectivesBean.DesirabilityCondition cond = DesirabilityObjectivesBean.DesirabilityCondition
					.createCondition(str);
			if (null == cond || !cond.isValid()) {
				throw new InvalidSettingsException("Invalid conditions for "
						+ cond.getPropertyName());
			}
		}
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
