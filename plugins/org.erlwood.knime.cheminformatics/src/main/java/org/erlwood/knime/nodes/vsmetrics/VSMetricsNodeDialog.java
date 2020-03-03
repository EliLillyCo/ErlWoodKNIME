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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.knime.core.data.DataValue;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;

/**
 * <code>NodeDialog</code> for the "FingerprintSimilarity" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Nikolas Fechner
 */
public class VSMetricsNodeDialog extends DefaultNodeSettingsPane {

    private SettingsModelColumnName     mActCol     = new SettingsModelColumnName(VSMetricsNodeModel.CFG_ACTIVITY_COLUMN,
                                                            "Activity");
    private SettingsModelBoolean        mClustering = new SettingsModelBoolean(VSMetricsNodeModel.CFG_CLUSTER, false);
    private SettingsModelColumnName     mClusterCol = new SettingsModelColumnName(VSMetricsNodeModel.CFG_CLUSTER_COLUMN,
                                                            "Cluster");
    private SettingsModelDoubleBounded  mAlpha      = new SettingsModelDoubleBounded(VSMetricsNodeModel.CFG_ALPHA, 20.0, 0.0,
                                                            1000.0);
    private SettingsModelIntegerBounded mEf         = new SettingsModelIntegerBounded(VSMetricsNodeModel.CFG_EF, 10, 1, 100);

    /**
     * New pane for configuring the FingerprintSimilarity node.
     */
    protected VSMetricsNodeDialog() {
        super();
        ColumnFilter actFilter = new DataValueColumnFilter(DoubleCell.TYPE.getPreferredValueClass(),
                IntCell.TYPE.getPreferredValueClass());
        DialogComponentColumnNameSelection mActColSel = new DialogComponentColumnNameSelection(mActCol, "Activity column", 0,
                true, false, actFilter);
        addDialogComponent(mActColSel);

        DialogComponentNumberEdit mAlphaEd = new DialogComponentNumberEdit(mAlpha, "BEDROC alpha");
        addDialogComponent(mAlphaEd);
        DialogComponentNumber mEfEd = new DialogComponentNumber(mEf, "% Enrichment Factor", 1);
        addDialogComponent(mEfEd);
        final DialogComponentBoolean clu = new DialogComponentBoolean(mClustering, "Evaluate chemotype enrichment");
        addDialogComponent(clu);
        final DialogComponentColumnNameSelection cluSel = new DialogComponentColumnNameSelection(mClusterCol,
                "Chemotype cluster cloumn", 0, DataValue.class);
        clu.getComponentPanel().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                cluSel.getModel().setEnabled(clu.getModel().isEnabled());
            }
        });
        cluSel.getModel().setEnabled(false);
        addDialogComponent(cluSel);

    }

}
