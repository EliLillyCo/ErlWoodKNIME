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
package org.erlwood.knime.nodes.paretoranking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.erlwood.knime.utils.KnimenodeUtils;
import org.erlwood.knime.utils.gui.ParetoObjectivesBean;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.IntCell;
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

/**
 * This node performs multi-objective Pareto ranking.
 * 
 * @author Dimitar Hristozov
 */
public class ParetoRankingNodeModel extends NodeModel {
	private static final NodeLogger LOG = NodeLogger
			.getLogger(ParetoRankingNodeModel.class);
	static final String CFG_SETTINGS = "ParetoRankingNodeModel_settings";
	private SettingsModelString mSettings = new SettingsModelString(
			CFG_SETTINGS, null);
	private List<ParetoObjectivesBean.ParetoCondition> mConfProps = new ArrayList<ParetoObjectivesBean.ParetoCondition>();

	private static final int OPT_VALUE = ParetoObjectivesBean.ParetoCondition.OPT_VALUE;
	private static final int OPT_INTERVAL = ParetoObjectivesBean.ParetoCondition.OPT_INTERVAL;
	private static final int OPT_MAXIMIZE = ParetoObjectivesBean.ParetoCondition.OPT_MAXIMIZE;
	
	private double mTotalOps;

	/**
	 * Constructor for the node model.
	 */
	protected ParetoRankingNodeModel() {
		super(1, 1);
	}

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
		for (int c = 0; c < mConfProps.size(); ++c) {
			ParetoObjectivesBean.ParetoCondition cond = mConfProps.get(c);
			colInd[c] = dataTable.getSpec().findColumnIndex(
					cond.getPropertyName());
			if (-1 == colInd[c]) {
				throw new Exception("Invalid column index for "
						+ cond.getPropertyName());
			}
		}
		mTotalOps = dataTable.getRowCount() * dataTable.getRowCount()
				+ dataTable.getRowCount();
		int r = 0;
		for (DataRow row : dataTable) {
			for (int c = 0; c < mConfProps.size() && !rowHasMissingCell[r]; ++c) {
				ParetoObjectivesBean.ParetoCondition cond = mConfProps.get(c);
				if (!row.getCell(colInd[c]).isMissing()) {
					double val = ((DoubleValue) row.getCell(colInd[c]))
							.getDoubleValue();
					if (OPT_VALUE == cond.getOptimizationMethod()) {
						data[r][c] = Math.abs(val - cond.getRightValue());
					} else if (OPT_INTERVAL == cond.getOptimizationMethod()) {
						if (val >= cond.getLeftValue()
								&& val <= cond.getRightValue()) {
							data[r][c] = Double.MIN_VALUE;
						} else {
							data[r][c] = Math.min(
									Math.abs(val - cond.getLeftValue()),
									Math.abs(val - cond.getRightValue()));
						}
					} else if (OPT_MAXIMIZE == cond.getOptimizationMethod()) {
						data[r][c] = -val;
					} else {
						data[r][c] = val;
					}
				} else {
					rowHasMissingCell[r] = true;
				}
			}
			r += 1;
			exec.setProgress(r / mTotalOps);
		}
		int[] ranks = null;
		
		
		ranks = paretoRank(data, mConfProps, exec, rowHasMissingCell);
		
		
		final int[] ranks2 = ranks;
		
		
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
					return new IntCell(ranks2[ind]);
				} else {
					return DataType.getMissingCell();
				}
			}
		});
		

		return new BufferedDataTable[] { exec.createColumnRearrangeTable(
				inData[0], colRearr, exec) };
	}

	private int[] paretoRank(double[][] data,
			List<ParetoObjectivesBean.ParetoCondition> conds,
			final ExecutionContext exec, boolean[] missingRow)
			throws CanceledExecutionException {
		int[] res = new int[data.length];
		// solution at i is dominated by dominatedBy[i] other solutions
		int[] dominatedBy = new int[data.length];
		List<Integer> currFront = new ArrayList<Integer>();
		Map<Integer, List<Integer>> dominates = new HashMap<Integer, List<Integer>>();

		for (int i = 0; i < data.length; ++i) {
			if (missingRow[i]) {
				continue;
			}
			for (int j = i + 1; j < data.length; ++j) {
				if (missingRow[j]) {
					continue;
				}
				int dom1 = 0, dom2 = 0;
				for (int p = 0; p < conds.size(); ++p) {
					if (data[i][p] < data[j][p]) {
						dom1 += 1;
					} else if (data[i][p] > data[j][p]) {
						dom2 += 1;
					}
				}
				if (dom1 > 0 && dom2 == 0) {
					dominatedBy[j] += 1;
					if (null == dominates.get(i)) {
						dominates.put(i, new ArrayList<Integer>());
					}
					dominates.get(i).add(j);
				} else if (dom2 > 0 && dom1 == 0) {
					dominatedBy[i] += 1;
					if (null == dominates.get(j)) {
						dominates.put(j, new ArrayList<Integer>());
					}
					dominates.get(j).add(i);
				}
				exec.setProgress((data.length + i * data.length + j)
						/ mTotalOps);
				exec.checkCanceled();
			}
			if (0 == dominatedBy[i]) {
				currFront.add(i);
			}
		
		}

		int rank = 1;
		while (0 < currFront.size()) {
			List<Integer> newFront = new ArrayList<Integer>();
			for (int i : currFront) {
				while (missingRow[i]) {
					i++;
				}
				res[i] = rank;
				List<Integer> domInd = dominates.get(i);
				if (null != domInd) {
					for (int di : domInd) {
						dominatedBy[di] -= 1;
						if (0 == dominatedBy[di]) {
							newFront.add(di);
						}
					}
				}
			}
			currFront = newFront;
			rank += 1;
		}

		return res;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		
		for (String s : inSpecs[0].getColumnNames()) {
		    if (s.contains(";")) {
		        throw new InvalidSettingsException("This node cannot handle column names with a semi-colon (;)");
		    }
		}
		
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
			List<ParetoObjectivesBean.ParetoCondition> lst = new ArrayList<ParetoObjectivesBean.ParetoCondition>(
					mConfProps);
			for (ParetoObjectivesBean.ParetoCondition cond : lst) {
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
				for (ParetoObjectivesBean.ParetoCondition c : mConfProps) {
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
		String name = KnimenodeUtils.createColumnName("Pareto Rank", spec);
		return new DataColumnSpecCreator(name, IntCell.TYPE).createSpec();
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
		List<String> avProps = new ArrayList<String>();
		List<String> confProps = new ArrayList<String>();
		ParetoObjectivesBean.parseConfString(mSettings.getStringValue(),
				avProps, confProps);
		mConfProps.clear();
		for (String cs : confProps) {
			ParetoObjectivesBean.ParetoCondition pc = ParetoObjectivesBean.ParetoCondition
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
		List<String> avProps = new ArrayList<String>();
		List<String> confProps = new ArrayList<String>();
		ParetoObjectivesBean.parseConfString(s, avProps, confProps);
		if (0 == confProps.size()) {
			throw new InvalidSettingsException(
					"Please select at least one property.");
		}
		for (String str : confProps) {
			ParetoObjectivesBean.ParetoCondition cond = ParetoObjectivesBean.ParetoCondition
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
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}
	


	
}
