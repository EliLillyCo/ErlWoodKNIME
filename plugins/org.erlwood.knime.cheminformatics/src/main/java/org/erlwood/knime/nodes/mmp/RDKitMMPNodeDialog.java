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
package org.erlwood.knime.nodes.mmp;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.JScrollPane;

import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.erlwood.knime.utils.gui.ConverterDataColumnSpecListCellRenderer;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.DataValueColumnFilter;
import org.rdkit.knime.types.RDKitMolValue;

/**
 * <code>NodeDialog</code> for the "RDKitMMP" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author George
 */

public class RDKitMMPNodeDialog extends DefaultNodeSettingsPane {

    private final SettingsModelColumnName   mPvalueInCol;

    private final SettingsModelFilterString mRatioColumns     = new SettingsModelFilterString(
                                                                      RDKitMMPNodeModel.CFG_RATIO_COLUMNS);
    private final SettingsModelFilterString mDiffColumns      = new SettingsModelFilterString(
                                                                      RDKitMMPNodeModel.CFG_DIFF_COLUMNS);

    private final SettingsModelBoolean      mDuplicate        = new SettingsModelBoolean(RDKitMMPNodeModel.CFG_DUPLICATES, true);
    private final SettingsModelString       mInvertPrecedence = new SettingsModelString("precedence",
                                                                      RDKitMMPNodeModel.PRECEDENCE_OPTIONS[0]);

    protected RDKitMMPNodeDialog() {
        this.createNewGroup("");

        SettingsModelString mMolInCol = new SettingsModelString(RDKitMMPNodeModel.RDMOL_IN_COL, "");
        DialogComponentColumnNameSelection mMolInColSel = new DialogComponentColumnNameSelection(mMolInCol,
                "RDKit molecule column: ", 0, MoleculeDataTypeConverter.getColumnFilter(RDKitMolValue.class));
        
        mMolInColSel.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEADING));
        ConverterDataColumnSpecListCellRenderer.setRenderer(mMolInColSel, RDKitMolValue.class);
        
        super.addDialogComponent(mMolInColSel);

        DataValueColumnFilter stringFilter = new DataValueColumnFilter(DataValue.class);
        SettingsModelColumnName mIdInCol = new SettingsModelColumnName(RDKitMMPNodeModel.ID_IN_COL, "");
        DialogComponentColumnNameSelection mIdInColSel = new DialogComponentColumnNameSelection(mIdInCol, "ID column: ", 0,
                stringFilter);
        mIdInColSel.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEADING));
        super.addDialogComponent(mIdInColSel);

        SettingsModelString mConnectionPoints = new SettingsModelString(RDKitMMPNodeModel.CONNECTION_POINT_STR, "At");
        DialogComponentStringSelection mConnectionPointsSel = new DialogComponentStringSelection(mConnectionPoints,
                "Choose Atom type for Attachment Point : ", new String[] { "At", "*" });
        mConnectionPointsSel.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEADING));
        super.addDialogComponent(mConnectionPointsSel);

        this.closeCurrentGroup();
        DataValueColumnFilter pvalueFilter = new DataValueColumnFilter(DoubleValue.class, IntValue.class);
        mPvalueInCol = new SettingsModelColumnName(RDKitMMPNodeModel.PVALUE_IN_COL, "");

        createNewTab("Property Comparison");

        DialogComponentLabel label3 = new DialogComponentLabel("Select columns for which ratios should be calculated");
        addDialogComponent(label3);

        DialogComponentColumnFilter ratioFilter = new DialogComponentColumnFilter(mRatioColumns, 0, false, pvalueFilter);

        addDialogComponent(ratioFilter);

        DialogComponentLabel label4 = new DialogComponentLabel("Select columns for which differences should be calculated");

        addDialogComponent(label4);

        DialogComponentColumnFilter diffFilter = new DialogComponentColumnFilter(mDiffColumns, 0, false, pvalueFilter);

        addDialogComponent(diffFilter);

        DialogComponentBoolean dup = new DialogComponentBoolean(mDuplicate, "Also generate inverse (duplicate) matched pairs");
        addDialogComponent(dup);

        DialogComponentButtonGroup invertPrecedence = new DialogComponentButtonGroup(mInvertPrecedence, true,
                "Property comparison", RDKitMMPNodeModel.PRECEDENCE_OPTIONS);
        addDialogComponent(invertPrecedence);

        Container c = (Container) getTab("Property Comparison");
        Component[] clist = c.getComponents();
        c.removeAll();

        JScrollPane scr = new JScrollPane(clist[0]);

        c.add(scr);

    }

    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        assert settings != null;
        mPvalueInCol.saveSettingsTo(settings);

    }

}
