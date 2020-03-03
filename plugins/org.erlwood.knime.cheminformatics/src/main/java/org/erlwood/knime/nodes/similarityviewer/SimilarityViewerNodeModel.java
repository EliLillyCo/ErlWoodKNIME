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

import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;

import jp.co.infocom.cheminfo.marvin.type.MrvValue;

import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.BufferedDataTableHolder;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.distmatrix.type.DistanceVectorDataValue;

import chemaxon.struc.Molecule;

/**
 * This is the model implementation of SimilarityViewer. Displays similarity
 * matrix as a heat map. Also allows the sorting with a "query" molecule.
 * 
 * @author Dimitar Hristozov
 */
public class SimilarityViewerNodeModel extends NodeModel implements BufferedDataTableHolder {
    public static final String         CFG_STRUCT_COL = "SimilarityViewerNode_CFG_STRUCT_COL";
    public static final String         CFG_DM_COL     = "SimilarityViewerNode_CFG_DM_COL";
    public static final String         CFG_IS_DM      = "SimilarityViewerNode_CFG_DM_TYPE";

    private final SettingsModelString  mStructCol     = new SettingsModelString(CFG_STRUCT_COL, null);
    private final SettingsModelString  mDmCol         = new SettingsModelString(CFG_DM_COL, null);
    private final SettingsModelBoolean mIsDm          = new SettingsModelBoolean(CFG_IS_DM, false);

    private String[]                   mRowIds;
    private Molecule[]                 mMols;
    private ImageIcon[]                mIcons;
    private DistanceVectorDataValue[]  mDvs;
    private BufferedDataTable          mData;

    /**
     * Constructor for the node model.
     */
    protected SimilarityViewerNodeModel() {
        super(1, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {

        setData(inData[0]);
        populateDefaultArrays(exec);
        return new BufferedDataTable[0];
    }

    private void populateDefaultArrays(ExecutionContext exec) throws Exception {
        int structCol = getData().getSpec().findColumnIndex(mStructCol.getStringValue());
        int dmCol = getData().getSpec().findColumnIndex(mDmCol.getStringValue());

        int rc = getData().getRowCount();
        setRowIds(new String[rc]);
        setMols(new Molecule[rc]);
        setIcons(new ImageIcon[rc]);
        setDvs(new DistanceVectorDataValue[rc]);

        int currRow = 0;
        for (DataRow row : getData()) {
            if (null != exec) {
                exec.checkCanceled();
                exec.setProgress((double) currRow / rc);
            }
            MrvValue mrvVal = MoleculeDataTypeConverter.getValue(row.getCell(structCol), MrvValue.class);
            getRowIds()[currRow] = row.getKey().toString();
            getMols()[currRow] = mrvVal.getMolecule();

            try {
                getIcons()[currRow] = new ImageIcon(getMols()[currRow].toBinFormat("png:h150,w150"));
            } catch (Exception e) {
                getIcons()[currRow] = new ImageIcon();
            }
            getDvs()[currRow] = (DistanceVectorDataValue) row.getCell(dmCol);
            currRow += 1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        setData(null);
        setRowIds(new String[] {});
        setDvs(new DistanceVectorDataValue[] {});
        setMols(new Molecule[] {});
        setIcons(new ImageIcon[] {});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return new DataTableSpec[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        mDmCol.saveSettingsTo(settings);
        getmIsDm().saveSettingsTo(settings);
        mStructCol.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        mDmCol.loadSettingsFrom(settings);
        getmIsDm().loadSettingsFrom(settings);
        mStructCol.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        mDmCol.validateSettings(settings);
        getmIsDm().validateSettings(settings);
        mStructCol.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

    }

    public BufferedDataTable[] getInternalTables() {
        return new BufferedDataTable[] { getData() };
    }

    public void setInternalTables(BufferedDataTable[] tables) {
        setData(tables[0]);
        try {
            populateDefaultArrays(null);
        } catch (Exception e) {
            reset();
        }
    }

    public SettingsModelBoolean getmIsDm() {
        return mIsDm;
    }

    public DistanceVectorDataValue[] getDvs() {
        return mDvs;
    }

    public void setDvs(DistanceVectorDataValue[] mDvs) {
        this.mDvs = mDvs;
    }

    public String[] getRowIds() {
        return mRowIds;
    }

    public void setRowIds(String[] mRowIds) {
        this.mRowIds = mRowIds;
    }

    public Molecule[] getMols() {
        return mMols;
    }

    public void setMols(Molecule[] mMols) {
        this.mMols = mMols;
    }

    public BufferedDataTable getData() {
        return mData;
    }

    public void setData(BufferedDataTable mData) {
        this.mData = mData;
    }

    public ImageIcon[] getIcons() {
        return mIcons;
    }

    public void setIcons(ImageIcon[] mIcons) {
        this.mIcons = mIcons;
    }
}
