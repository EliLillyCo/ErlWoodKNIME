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
package org.erlwood.knime.nodes.stringtorxn;

import jp.co.infocom.cheminfo.marvin.type.MrvCell;
import jp.co.infocom.cheminfo.marvin.type.MrvCellFactory;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.erlwood.knime.nodes.stringtomolecule.StringToMoleculeNodeModel;
import org.erlwood.knime.utils.KnimenodeUtils;
import org.erlwood.knime.utils.nodes.ChemContentsNodeModel;

import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

/**
 * This is the model implementation of StringToRxn. Converts string columns to
 * ReactionAdapterCell objects.
 * 
 * @author Dimitar Hristozov
 */
public class StringToRxnNodeModel extends ChemContentsNodeModel {

    private final SettingsModelString mInCol      = new SettingsModelString(StringToMoleculeNodeModel.CFG_IN_COL,
                                                          StringToMoleculeNodeModel.CFG_UNLIKELY_COL_NAME);
    private final SettingsModelString mReplace    = new SettingsModelString(StringToMoleculeNodeModel.CFG_REPLACE, "Replace");
    private final SettingsModelString mNewColName = new SettingsModelString(StringToMoleculeNodeModel.CFG_NEW_COL_NAME,
                                                          "Conv. Reaction");

    /**
     * Constructor for the node model.
     */
    protected StringToRxnNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] doExecute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {
        ColumnRearranger c = createColumnRearranger(inData[0].getDataTableSpec());
        BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], c, exec);
        return new BufferedDataTable[] { out };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        if (StringToMoleculeNodeModel.CFG_UNLIKELY_COL_NAME.equals(mInCol.getStringValue())) {
            if (!inSpecs[0].containsCompatibleType(StringValue.class)) {
                throw new InvalidSettingsException("At least one string-compatible column is required.");
            }
            for (int c = 0; c < inSpecs[0].getNumColumns(); ++c) {
                if (inSpecs[0].getColumnSpec(c).getType().isCompatible(StringValue.class)) {
                    mInCol.setStringValue(inSpecs[0].getColumnSpec(c).getName());
                    break;
                }
            }
        }
        DataTableSpec spec = createColumnRearranger(inSpecs[0]).createSpec();
        return new DataTableSpec[] { spec };
    }

    private ColumnRearranger createColumnRearranger(DataTableSpec spec) throws InvalidSettingsException {
        ColumnRearranger result = new ColumnRearranger(spec);
        String newName = mNewColName.getStringValue();
        if (!"Append".equals(mReplace.getStringValue())) {
            newName = mInCol.getStringValue();
        } else {
            if ("".equals(newName)) {
                newName = "Conv. Column";
            }
            newName = KnimenodeUtils.createColumnName(newName, spec);
        }
        mNewColName.setStringValue(newName);
        DataColumnSpecCreator appSpecCreator = new DataColumnSpecCreator(newName, MrvCell.TYPE);
        final DataColumnSpec appSpec = appSpecCreator.createSpec();
        final int colInd = result.indexOf(mInCol.getStringValue());
        SingleCellFactory scFactory = new SingleCellFactory(appSpec) {
            public DataCell getCell(final DataRow row) {
                if (row.getCell(colInd).isMissing()) {
                    return DataType.getMissingCell();
                }
                String source = ((StringValue) row.getCell(colInd)).getStringValue();
                Molecule mol = null;
                try {
                    mol = MolImporter.importMol(source);
                } catch (MolFormatException mfe) {
                    mol = new Molecule();
                }
                return MrvCellFactory.create(mol);
            }
        };
        if ("Append".equals(mReplace.getStringValue())) {
            result.append(scFactory);
        } else {
            result.replace(scFactory, mInCol.getStringValue());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        mInCol.saveSettingsTo(settings);
        mReplace.saveSettingsTo(settings);
        mNewColName.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        mInCol.loadSettingsFrom(settings);
        mReplace.loadSettingsFrom(settings);
        mNewColName.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        mInCol.validateSettings(settings);
        mReplace.validateSettings(settings);
        mNewColName.validateSettings(settings);
    }

}
