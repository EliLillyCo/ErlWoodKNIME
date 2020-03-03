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
package org.erlwood.knime.nodes.bitvectortobits;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.MissingCell;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of BitVectorToBits. Splits column with
 * BitVector values to a number of individual columns with one bit in each of
 * them. That is, 256 new columns will be created for a BitVector with 256 bits.
 * 
 * @author Dimitar Hristozov
 */
public class BitVectorToBitsNodeModel extends NodeModel {
	public static final String CFG_COLNAME = "BitVectorToBitsNodeModel_colName";
	public static final String CFG_NUMBITS = "BitVectorToBitsNodeModel_numBits";

	private SettingsModelString mColName = new SettingsModelString(CFG_COLNAME,
			"");

	/**
	 * Constructor for the node model.
	 */
	protected BitVectorToBitsNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		final int cInd = inData[0].getSpec().findColumnIndex(
				mColName.getStringValue());
		DataRow r = inData[0].iterator().next();
        long numBits = 0;
        if (null != r) {
            if (r.getCell(cInd) instanceof BitVectorValue) {
                BitVectorValue bv = (BitVectorValue) r.getCell(cInd);
                numBits = bv.length();
            } else {
                org.knime.base.data.bitvector.BitVectorValue bvo = (org.knime.base.data.bitvector.BitVectorValue) r
                        .getCell(cInd);
                if (null != bvo) {
                    numBits = bvo.getNumBits();
                }
            }
        }
        final int fnumBits = (int) numBits;
		
		ColumnRearranger colRearr = new ColumnRearranger(
				inData[0].getDataTableSpec());
		colRearr.append(new CellFactory() {
		    
			public DataCell[] getCells(DataRow row) {
				
				DataValue dv = row.getCell(cInd);
				
				BitVectorValue bv = null;
				org.knime.base.data.bitvector.BitVectorValue bvo = null;
				
				if (dv instanceof BitVectorValue) {
					bv = (BitVectorValue)dv;
				} else if (dv instanceof org.knime.base.data.bitvector.BitVectorValue) {
					bvo = (org.knime.base.data.bitvector.BitVectorValue) dv;
				}
				
				IntCell[] res = new IntCell[(int) fnumBits];
				
				if (null != dv || !(dv instanceof MissingCell)) {
				    int index = 0;
					for (int i = 0; i < fnumBits; i++) {
					  
					    if (dv instanceof BitVectorValue) {
					        res[index++] = new IntCell(bv.get(i) ? 1 : 0);
		                } else if (dv instanceof org.knime.base.data.bitvector.BitVectorValue) {		                    
		                    res[index++] = new IntCell(bvo.getBitSet().get(i) ? 1 : 0);
		                }
					    
					}
				} else {
					for (int i = 0; i < fnumBits; ++i) {
						res[i] = new IntCell(-1);
					}
				}
				
				return res;
			}

			public DataColumnSpec[] getColumnSpecs() {
				DataColumnSpec[] res = new DataColumnSpec[(int) fnumBits];
				for (int i = 0; i < fnumBits; ++i) {
					res[i] = new DataColumnSpecCreator("Bit " + (i + 1),
							IntCell.TYPE).createSpec();
				}
				return res;
			}

			public void setProgress(int curRowNr, int rowCount, RowKey lastKey,
					ExecutionMonitor exec) {
				exec.setProgress((double) curRowNr / rowCount);
			}

		});
		return new BufferedDataTable[] { exec.createColumnRearrangeTable(
				inData[0], colRearr, exec) };
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
	@SuppressWarnings("deprecation")
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		String cName = mColName.getStringValue();

		DataColumnSpec inColSpec = inSpecs[0].getColumnSpec(cName);
		if (null == inColSpec) {
			throw new InvalidSettingsException("No spec for column " + cName);
		}
		if (!inColSpec.getType().isCompatible(BitVectorValue.class)
				& !inColSpec.getType().isCompatible(
						org.knime.base.data.bitvector.BitVectorValue.class)) {
			throw new InvalidSettingsException("No BitVector in column "
					+ cName);
		}

		return new DataTableSpec[] { null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		mColName.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		mColName.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		mColName.validateSettings(settings);
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
