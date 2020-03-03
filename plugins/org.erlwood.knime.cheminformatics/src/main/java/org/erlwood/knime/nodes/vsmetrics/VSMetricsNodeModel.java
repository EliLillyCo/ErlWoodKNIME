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
package org.erlwood.knime.nodes.vsmetrics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;

/**
 * This is the model implementation of FingerprintSimilarity.
 * 
 * 
 * @author Nikolas Fechner
 */
public class VSMetricsNodeModel extends NodeModel {
    public static final String      CFG_ACTIVITY_COLUMN = "activity_column";
    public static final String      CFG_CLUSTER_COLUMN  = "cluster";
    public static final String      CFG_ALPHA           = "alpha";
    public static final String      CFG_EF              = "ef";
    public static final String      CFG_CLUSTER         = "do_cluster";

    private SettingsModelColumnName mActCol             = new SettingsModelColumnName(CFG_ACTIVITY_COLUMN, "Activity");
    private SettingsModelColumnName mClusterCol         = new SettingsModelColumnName(CFG_CLUSTER_COLUMN, "Cluster");
    private SettingsModelBoolean    mClustering         = new SettingsModelBoolean(CFG_CLUSTER, false);
    private SettingsModelDouble     mAlpha              = new SettingsModelDouble(CFG_ALPHA, 20.0);
    private SettingsModelInteger    mEf                 = new SettingsModelInteger(CFG_EF, 10);

    private SettingsModel[]         config              = new SettingsModel[] { mActCol, mAlpha, mEf, mClusterCol, mClustering };

    private Map<Integer, Integer>   h                   = new Hashtable<Integer, Integer>();

    /**
     * Constructor for the node model.
     */
    protected VSMetricsNodeModel() {
        super(1, 1);
    }

    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {

        h = new Hashtable<Integer, Integer>();
        BufferedDataContainer container = exec.createDataContainer(createSpec());
        int actInd = inData[0].getDataTableSpec().findColumnIndex(mActCol.getStringValue());
        int cluInd = inData[0].getDataTableSpec().findColumnIndex(mClusterCol.getStringValue());

        List<Integer> activesRanks = new ArrayList<Integer>();
        RowIterator rows = inData[0].iterator();
        int c = 1;
        double n = 0;
        double n2 = 0;

        if (mClustering.getBooleanValue()) {
            initClusters(inData[0], cluInd);
        }
        List<Integer> clusters = new ArrayList<Integer>();
        while (rows.hasNext()) {
            DataRow row = rows.next();
            DataCell acell = row.getCell(actInd);
            boolean act = false;
            if (acell instanceof DoubleCell) {
                DoubleCell newName = (DoubleCell) acell;
                act = (newName.getDoubleValue() > 0.0);
            } else {
                if (acell instanceof IntCell) {
                    IntCell newName = (IntCell) acell;
                    act = (newName.getIntValue() > 0);
                }
            }
            if (act) {
                activesRanks.add(c);
                if (mClustering.getBooleanValue()) {
                    clusters.add(row.getCell(cluInd).toString().hashCode());
                }
                n++;
            }
            c++;
            n2++;
        }
        List<DataCell> cells = new ArrayList<DataCell>();
        double ef = VSMetrics.computeEF(activesRanks, ((double) mEf.getIntValue()) / 100.0, n, n2);
        exec.checkCanceled();
        exec.setProgress(0.2);
        double auroc = VSMetrics.computeAUROC(activesRanks, n, n2);
        exec.checkCanceled();
        exec.setProgress(0.4);
        double rie = VSMetrics.computeRIE(activesRanks, mAlpha.getDoubleValue(), n, n2);
        exec.checkCanceled();
        exec.setProgress(0.6);
        double bedroc = VSMetrics.computeBEDROC(rie, mAlpha.getDoubleValue(), n, n2);
        exec.checkCanceled();
        exec.setProgress(0.8);

        double roce = VSMetrics.computeROCE(activesRanks, ((double) mEf.getIntValue()) / 100.0, n, n2);
        exec.checkCanceled();

        cells.add(new DoubleCell(ef));
        cells.add(new DoubleCell(auroc));
        cells.add(new DoubleCell(rie));
        cells.add(new DoubleCell(bedroc));
        cells.add(new DoubleCell(roce));

        double awROCE = Double.NaN;
        double awAUC = Double.NaN;
        if (mClustering.getBooleanValue()) {
            awAUC = VSMetrics.computeawAUC(activesRanks, clusters, n, n2, h);
            awROCE = VSMetrics.computeawROCE(activesRanks, clusters, ((double) mEf.getIntValue()) / 100.0, n, n2, h);
            cells.add(new DoubleCell(awROCE));
            cells.add(new DoubleCell(awAUC));
        }
        exec.setProgress(1.0);
        DefaultRow drow = new DefaultRow(new RowKey("Metrics"), cells);
        container.addRowToTable(drow);

        container.close();

        return new BufferedDataTable[] { container.getTable() };
    }

    /**
     * {@inheritDoc}
     */

    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

        return new DataTableSpec[] { createSpec() };

    }

    private DataTableSpec createSpec() {
        DataColumnSpec[] specs = null;
        if (mClustering.getBooleanValue()) {
            specs = new DataColumnSpec[] { new DataColumnSpecCreator("EF", DoubleCell.TYPE).createSpec(),
                    new DataColumnSpecCreator("AUC", DoubleCell.TYPE).createSpec(),
                    new DataColumnSpecCreator("RIE", DoubleCell.TYPE).createSpec(),
                    new DataColumnSpecCreator("BEDROC", DoubleCell.TYPE).createSpec(),
                    new DataColumnSpecCreator("ROC enrichment", DoubleCell.TYPE).createSpec(),
                    new DataColumnSpecCreator("awROC enrichment", DoubleCell.TYPE).createSpec(),
                    new DataColumnSpecCreator("awAUROC", DoubleCell.TYPE).createSpec(), };
        } else {
            specs = new DataColumnSpec[] { new DataColumnSpecCreator("EF", DoubleCell.TYPE).createSpec(),
                    new DataColumnSpecCreator("AUROC", DoubleCell.TYPE).createSpec(),
                    new DataColumnSpecCreator("RIE", DoubleCell.TYPE).createSpec(),
                    new DataColumnSpecCreator("BEDROC", DoubleCell.TYPE).createSpec(),
                    new DataColumnSpecCreator("ROC enrichment", DoubleCell.TYPE).createSpec() };
        }

        return new DataTableSpec(specs);
    }

    /**
     * {@inheritDoc}
     */

    protected void saveSettingsTo(final NodeSettingsWO settings) {
        for (SettingsModel model : config) {
            model.saveSettingsTo(settings);
        }
    }

    /**
     * {@inheritDoc}
     */

    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        for (SettingsModel model : config) {
            model.loadSettingsFrom(settings);
        }
    }

    /**
     * {@inheritDoc}
     */

    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        for (SettingsModel model : config) {
            model.validateSettings(settings);
        }
    }

    /**
     * {@inheritDoc}
     */

    protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */

    protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    private void initClusters(BufferedDataTable table, int col) {
        RowIterator rows = table.iterator();

        while (rows.hasNext()) {
            DataRow row = rows.next();
            int x = row.getCell(col).toString().hashCode();
            if (!h.containsKey(x)) {
                h.put(x, 1);
            } else {
                int i = h.get(x);
                h.remove(x);
                h.put(x, i + 1);
            }
        }
    }
}
