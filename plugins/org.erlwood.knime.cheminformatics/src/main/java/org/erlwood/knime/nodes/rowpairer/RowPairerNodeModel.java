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
package org.erlwood.knime.nodes.rowpairer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.chem.types.SmilesCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of RowPairer. Compares rows using user
 * specified columns. Pairs up any two rows which differ only in user specified
 * number of columns. For each pair calculates the difference between user
 * specified properties.
 * 
 * @author Dimitar Hristozov
 */
public class RowPairerNodeModel extends NodeModel {
    private static final NodeLogger   LOG                  = NodeLogger.getLogger(RowPairerNodeModel.class);
    public static final String        CFG_IN_COL_LIST      = "RowPairerNodeModel_InColumnList";
    public static final String        CFG_COL_RATIO        = "RowPairerNodeModel_ColumnRatios";
    public static final String        CFG_COL_DIFF         = "RowPairerNodeModel_ColumnDiffs";
    public static final String        CFG_OUT_COL_LIST     = "RowPairerNodeModel_OutColumnList";
    public static final String        CFG_DUPLICATE        = "duplicate";
    public static final String        CFG_PRECEDENCE       = "precedence";

    public static final String        PRECEDENCE_OPTIONS[] = { "R / L and R - L", "L / R and L - R" };

    private SettingsModelFilterString mInColList           = new SettingsModelFilterString(CFG_IN_COL_LIST);
    private SettingsModelFilterString mColRatio            = new SettingsModelFilterString(CFG_COL_RATIO);
    private SettingsModelFilterString mColDiff             = new SettingsModelFilterString(CFG_COL_DIFF);
    private SettingsModelFilterString mOutColList          = new SettingsModelFilterString(CFG_OUT_COL_LIST);
    private SettingsModelBoolean      mDuplicate           = new SettingsModelBoolean(CFG_DUPLICATE, true);
    private final SettingsModelString mPrecedence          = new SettingsModelString("precedence", PRECEDENCE_OPTIONS[0]);

    /**
     * Constructor for the node model.
     */
    protected RowPairerNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {

        try {
            BufferedDataContainer outPairs = exec.createDataContainer(createOutPortOneSpec(inData[0].getDataTableSpec()));
            findPairs(inData[0], outPairs, exec);
            outPairs.close();
            return new BufferedDataTable[] { outPairs.getTable() };
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private class ParamsStruct {
        private List<String>  selColsN;
        private int[]         selCols;
        private DataTableSpec inSpec;
        private DataTableSpec outSpec;
        private int[]         mRatCol;
        private int[]         mDiffCol;
        private List<String>  mActualDiffs;
        private int           leftRow;

        public ParamsStruct(List<String> selColsN, int[] selCols, DataTableSpec inSpec, DataTableSpec outSpec, int outCol,
                int[] mRatCol, int[] mDiffCol) {
            this.selColsN = selColsN;
            this.selCols = selCols;
            this.inSpec = inSpec;
            this.outSpec = outSpec;
            this.mDiffCol = mDiffCol;
            this.mRatCol = mRatCol;
            this.leftRow = -1;
        }
    }

    private ParamsStruct initParams(DataTableSpec inSpec, DataTableSpec outSpec) {
        List<String> selColsN = mInColList.getIncludeList();
        int[] selCols = new int[selColsN.size()];
        for (int i = 0; i < selColsN.size(); ++i) {
            selCols[i] = inSpec.findColumnIndex(selColsN.get(i));
        }
        int[] rcols = new int[mColRatio.getIncludeList().size()];
        for (int i = 0; i < mColRatio.getIncludeList().size(); i++) {
            rcols[i] = inSpec.findColumnIndex(mColRatio.getIncludeList().get(i));
        }
        int[] dcols = new int[mColDiff.getIncludeList().size()];
        for (int i = 0; i < mColDiff.getIncludeList().size(); i++) {
            dcols[i] = inSpec.findColumnIndex(mColDiff.getIncludeList().get(i));
        }
        return new ParamsStruct(selColsN, selCols, inSpec, outSpec, -1, rcols, dcols);
    }

    private void findPairs(BufferedDataTable dataTable, BufferedDataContainer outPairs, ExecutionContext exec) {
        ParamsStruct prms = initParams(dataTable.getSpec(), outPairs.getTableSpec());
        double c = 0.0;
        List<DataRow> rows = new ArrayList<DataRow>();

        for (DataRow row1 : dataTable) {
            for (DataRow row2 : rows) {
                int nDiff = compareRows(row1, row2, prms);
                if (1 == nDiff) {
                    if (mDuplicate.getBooleanValue()) {
                        DataRow[] tr = processPairedRows(row1, row2, prms, mDuplicate.getBooleanValue());
                        outPairs.addRowToTable(tr[0]);
                        outPairs.addRowToTable(tr[1]);
                    } else {
                        outPairs.addRowToTable(processPairedRows(row1, row2, prms, mDuplicate.getBooleanValue())[0]);
                    }
                }
                c++;
                exec.setProgress(c / (0.5 * dataTable.getRowCount() * dataTable.getRowCount() - dataTable.getRowCount()));
            }
            rows.add(row1);

        }
    }

    private int[] compareCells(DataCell c1, DataCell c2, List<String> actualDiffs, int leftRow) {
        int[] res = new int[2];
        if (!c1.equals(c2)) {
            res[0] = 0;
            String s1 = c1.toString();
            String s2 = c2.toString();
            if (-1 == leftRow) {

                if (c1.getType().isCompatible(DoubleValue.class)) {
                    if (Double.valueOf(s1) < Double.valueOf(s2)) {
                        leftRow = 1;
                    } else {
                        leftRow = 2;
                    }
                } else if (0 < s1.compareTo(s2)) {
                    leftRow = 1;
                } else {
                    leftRow = 2;
                }
            }
            if (1 == leftRow) {
                actualDiffs.add(s1);
                actualDiffs.add(s2);
            } else if (2 == leftRow) {
                actualDiffs.add(s2);
                actualDiffs.add(s1);
            }
        } else {
            res[0] = 1;
        }
        res[1] = leftRow;
        return res;
    }

    private int copyColsForOnePairMember(DataRow row, DataTableSpec spec, DataCell[] cells, int i) {
        List<String> outCols = mOutColList.getIncludeList();

        cells[i] = new StringCell(row.getKey().toString());
        i += 2;
        for (String cn : outCols) {
            if (!mColRatio.getIncludeList().contains(cn) && !mColDiff.getIncludeList().contains(cn)) {
                cells[i] = row.getCell(spec.findColumnIndex(cn));
                i += 2;
            }
        }
        for (int j = 0; j < mColRatio.getIncludeList().size(); j++) {
            String colDiff = mColRatio.getIncludeList().get(j);
            cells[i] = row.getCell(spec.findColumnIndex(colDiff));
            i += 2;
        }
        for (int j = 0; j < mColDiff.getIncludeList().size(); j++) {
            String colDiff = mColDiff.getIncludeList().get(j);

            if (!mColRatio.getIncludeList().contains(colDiff)) {
                cells[i] = row.getCell(spec.findColumnIndex(colDiff));
                i += 2;
            }
        }
        return i - 1;
    }

    private DataCell[] buildOutPortOneRow(DataRow row1, DataRow row2, ParamsStruct prms, boolean invert) {
        DataCell[] cells = new DataCell[prms.outSpec.getNumColumns()];
        int i;
        if (!invert) {
            /* int i = */copyColsForOnePairMember(row1, prms.inSpec, cells, 0);
            i = copyColsForOnePairMember(row2, prms.inSpec, cells, 1);

            cells[i] = new SmilesCell(prms.mActualDiffs.get(0));
            i += 1;
            cells[i] = new SmilesCell(prms.mActualDiffs.get(1));
            i += 1;
            cells[i] = new StringCell(prms.mActualDiffs.get(2));
            i += 1;

            for (int j = 0; j < prms.mRatCol.length; j++) {
                double d = Double.NaN, absd = Double.NaN;
                try {
                    double d1, d2;
                    if(row1.getCell(prms.mRatCol[j]).isMissing( )) {
                    	d1 = Double.NaN;
                    } else {
                    	d1 = ((DoubleValue) row1.getCell(prms.mRatCol[j])).getDoubleValue();
                    }
                    if(row2.getCell(prms.mRatCol[j]).isMissing( )) {
                    	d2 = Double.NaN;
                    } else {
                    	d2 = ((DoubleValue) row2.getCell(prms.mRatCol[j])).getDoubleValue();
                    }
                    if(Double.isNaN(d1) || Double.isNaN(d2)) {
                    	cells[i++] = DataType.getMissingCell( );
                    	cells[i++] = DataType.getMissingCell( );
                    	continue;
                    }
                    if (d2 != 0.0) {
                        if (mPrecedence.getStringValue().equals(PRECEDENCE_OPTIONS[1])) {
                            d = d1 / d2;
                        } else {
                            d = d2 / d1;
                        }
                    }
                    absd = Math.abs(d);

                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    cells[i] = DataType.getMissingCell();
                    i += 1;
                } else {
                    cells[i] = new DoubleCell(d);
                    i += 1;
                }
                if (Double.isNaN(absd) || Double.isInfinite(absd)) {
                    cells[i] = DataType.getMissingCell();
                    i += 1;
                } else {
                    cells[i] = new DoubleCell(absd);
                    i += 1;
                }
            }

            for (int j = 0; j < prms.mDiffCol.length; j++) {
                double d = Double.NaN, absd = Double.NaN;
                try {
                    double d1, d2;
                    if(row1.getCell(prms.mDiffCol[j]).isMissing( )) {
                    	d1 = Double.NaN;
                    } else {
                    	d1 = ((DoubleValue) row1.getCell(prms.mDiffCol[j])).getDoubleValue();
                    }
                    if(row2.getCell(prms.mDiffCol[j]).isMissing( )) {
                    	d2 = Double.NaN;
                    } else {
                    	d2 = ((DoubleValue) row2.getCell(prms.mDiffCol[j])).getDoubleValue();
                    }
                    if(Double.isNaN(d1) || Double.isNaN(d2)) {
                    	cells[i++] = DataType.getMissingCell( );
                    	cells[i++] = DataType.getMissingCell( );
                    	continue;
                    }
                    if (mPrecedence.getStringValue().equals(PRECEDENCE_OPTIONS[1])) {
                        d = d1 - d2;
                    } else {
                        d = d2 - d1;
                    }
                    
                    absd = Math.abs(d);

                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    cells[i] = DataType.getMissingCell();
                    i += 1;
                } else {
                    cells[i] = new DoubleCell(d);
                    i += 1;
                }
                if (Double.isNaN(absd) || Double.isInfinite(absd)) {
                    cells[i] = DataType.getMissingCell();
                    i += 1;
                } else {
                    cells[i] = new DoubleCell(absd);
                    i += 1;
                }
            }
        } else {
            copyColsForOnePairMember(row1, prms.inSpec, cells, 1);
            i = copyColsForOnePairMember(row2, prms.inSpec, cells, 0) + 1;
            cells[i] = new SmilesCell(prms.mActualDiffs.get(1));
            i += 1;
            cells[i] = new SmilesCell(prms.mActualDiffs.get(0));
            i += 1;
            cells[i] = new StringCell(prms.mActualDiffs.get(2));
            i += 1;

            for (int j = 0; j < prms.mRatCol.length; j++) {
                double d = Double.NaN, absd = Double.NaN;
                try {
                	double d1, d2;
                	if(row1.getCell(prms.mRatCol[j]).isMissing( )) {
                		d1 = Double.NaN;
                	} else {
                		d1 = ((DoubleValue) row1.getCell(prms.mRatCol[j])).getDoubleValue();
                	}
                	if(row2.getCell(prms.mRatCol[j]).isMissing( )) {
                		d2 = Double.NaN;
                	} else {
                		d2 = ((DoubleValue) row2.getCell(prms.mRatCol[j])).getDoubleValue();
                	}
                	if(Double.isNaN(d1) || Double.isNaN(d2)) {
                		cells[i++] = DataType.getMissingCell( );
                		cells[i++] = DataType.getMissingCell( );
                		continue;
                	}
                   
                    if (d2 != 0.0) {
                        if (mPrecedence.getStringValue().equals(PRECEDENCE_OPTIONS[1])) {
                            d = d2 / d1;
                        } else {
                            d = d1 / d2;                            
                        }
                    }
                    absd = d > 1.0 ? d : 1 / d;

                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    cells[i] = DataType.getMissingCell();
                    i += 1;
                } else {
                    cells[i] = new DoubleCell(d);
                    i += 1;
                }
                if (Double.isNaN(absd) || Double.isInfinite(absd)) {
                    cells[i] = DataType.getMissingCell();
                    i += 1;
                } else {
                    cells[i] = new DoubleCell(absd);
                    i += 1;
                }
            }

            for (int j = 0; j < prms.mDiffCol.length; j++) {
                double d = Double.NaN, absd = Double.NaN;
                try {
                    double d1, d2;
                    if(row1.getCell(prms.mDiffCol[j]).isMissing( )) {
                    	d1 = Double.NaN;
                    } else {
                    	d1 = ((DoubleValue) row1.getCell(prms.mDiffCol[j])).getDoubleValue();
                    }
                    if(row2.getCell(prms.mDiffCol[j]).isMissing( )) {
                    	d2 = Double.NaN;
                    } else {
                    	d2 = ((DoubleValue) row2.getCell(prms.mDiffCol[j])).getDoubleValue();
                    }
                    if(Double.isNaN(d1) || Double.isNaN(d2)) {
                    	cells[i++] = DataType.getMissingCell( );
                    	cells[i++] = DataType.getMissingCell( );
                    	continue;
                    }
                    
                    if (mPrecedence.getStringValue().equals(PRECEDENCE_OPTIONS[1])) {
                        d = d2 - d1;
                    } else {
                        d = d1 - d2;                        
                    }
                    absd = Math.abs(d);

                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    cells[i] = DataType.getMissingCell();
                    i += 1;
                } else {
                    cells[i] = new DoubleCell(d);
                    i += 1;
                }
                if (Double.isNaN(absd) || Double.isInfinite(absd)) {
                    cells[i] = DataType.getMissingCell();
                    i += 1;
                } else {
                    cells[i] = new DoubleCell(absd);
                    i += 1;
                }
            }
        }

        return cells;
    }

    private int compareRows(DataRow row1, DataRow row2, ParamsStruct prms) {
        int nDiff = 0;
        prms.mActualDiffs = new ArrayList<String>();
        prms.leftRow = -1;
        for (int i = 0; i < prms.selCols.length; ++i) {
            DataCell c1 = row1.getCell(prms.selCols[i]);
            DataCell c2 = row2.getCell(prms.selCols[i]);
            int[] r = compareCells(c1, c2, prms.mActualDiffs, prms.leftRow);
            prms.leftRow = r[1];
            if (0 == r[0]) {
                nDiff += 1;
                prms.mActualDiffs.add(prms.selColsN.get(i));

            }
        }
        return nDiff;
    }

    private DataRow[] processPairedRows(DataRow row1, DataRow row2, ParamsStruct prms, boolean duplicate) {
        if (!duplicate) {
            DataCell[] cells = (2 != prms.leftRow) ? buildOutPortOneRow(row1, row2, prms, false) : buildOutPortOneRow(row2,
                                                                                                                      row1,
                                                                                                                      prms,
                                                                                                                      false);
            return new DefaultRow[] { new DefaultRow(row1.getKey().getString() + "->" + row2.getKey().getString(), cells) };
        } else {
            DataCell[] cells1 = (2 != prms.leftRow) ? buildOutPortOneRow(row1, row2, prms, false) : buildOutPortOneRow(row2,
                                                                                                                       row1,
                                                                                                                       prms,
                                                                                                                       false);
            DataCell[] cells2 = (2 != prms.leftRow) ? buildOutPortOneRow(row1, row2, prms, true) : buildOutPortOneRow(row2,
                                                                                                                      row1,
                                                                                                                      prms,
                                                                                                                      true);
            return new DefaultRow[] { new DefaultRow(row1.getKey().getString() + "->" + row2.getKey().getString(), cells1),
                    new DefaultRow(row2.getKey().getString() + "->" + row1.getKey().getString(), cells2) };
        }
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
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return new DataTableSpec[] { createOutPortOneSpec(inSpecs[0]) };
    }

    private void addSelectedOutCols(DataTableSpec inSpec, List<String> names, List<DataType> types, String num) {
        List<String> outCols = mOutColList.getIncludeList();

        names.add("ID 1" + num);
        types.add(StringCell.TYPE);
        names.add("ID 2" + num);
        types.add(StringCell.TYPE);
        for (String cn : outCols) {
            if (!mColRatio.getIncludeList().contains(cn) && !mColDiff.getIncludeList().contains(cn)) {
                names.add(cn + " 1" + num);
                types.add(inSpec.getColumnSpec(cn).getType());
                names.add(cn + " 2" + num);
                types.add(inSpec.getColumnSpec(cn).getType());
            }
        }
        for (String col : mColRatio.getIncludeList()) {
            names.add(col + " 1" + num);
            types.add(inSpec.getColumnSpec(col).getType());
            names.add(col + " 2" + num);
            types.add(inSpec.getColumnSpec(col).getType());
        }
        for (String colDiff : mColDiff.getIncludeList()) {
            if (!mColRatio.getIncludeList().contains(colDiff)) {
                names.add(colDiff + " 1" + num);
                types.add(inSpec.getColumnSpec(colDiff).getType());
                names.add(colDiff + " 2" + num);
                types.add(inSpec.getColumnSpec(colDiff).getType());
            }
        }
    }

    private DataTableSpec createOutPortOneSpec(DataTableSpec inSpec) {
        List<String> names = new ArrayList<String>();
        List<DataType> types = new ArrayList<DataType>();
        addSelectedOutCols(inSpec, names, types, "");

        names.add("From");
        types.add(SmilesCell.TYPE);
        names.add("To");
        types.add(SmilesCell.TYPE);
        names.add("At");
        types.add(StringCell.TYPE);

        for (String s : mColRatio.getIncludeList()) {
            names.add("Ratio (" + s + ")");
            types.add(DoubleCell.TYPE);
            names.add("Absolute Ratio (" + s + ")");
            types.add(DoubleCell.TYPE);
        }
        for (String s : mColDiff.getIncludeList()) {
            names.add("Difference (" + s + ")");
            types.add(DoubleCell.TYPE);
            names.add("Absolute Difference (" + s + ")");
            types.add(DoubleCell.TYPE);
        }
        return new DataTableSpec(names.toArray(new String[names.size()]), types.toArray(new DataType[types.size()]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        mInColList.saveSettingsTo(settings);
        mColRatio.saveSettingsTo(settings);
        mColDiff.saveSettingsTo(settings);
        mOutColList.saveSettingsTo(settings);
        mDuplicate.saveSettingsTo(settings);
        mPrecedence.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        mInColList.loadSettingsFrom(settings);
        mColRatio.loadSettingsFrom(settings);
        mColDiff.loadSettingsFrom(settings);
        mOutColList.loadSettingsFrom(settings);
        mDuplicate.loadSettingsFrom(settings);
        mPrecedence.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        mInColList.validateSettings(settings);
        mColRatio.validateSettings(settings);
        mColDiff.validateSettings(settings);
        mOutColList.validateSettings(settings);
        mDuplicate.validateSettings(settings);
        mPrecedence.validateSettings(settings);
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
}
