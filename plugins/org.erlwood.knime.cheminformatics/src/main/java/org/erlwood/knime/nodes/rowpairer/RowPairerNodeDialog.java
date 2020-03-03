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

import java.awt.Component;
import java.awt.Container;

import javax.swing.JScrollPane;

import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "RowPairer" Node. Compares rows using user
 * specified columns. Pairs up any two rows which differ only in user specified
 * number of columns. For each pair calculates the difference between user
 * specified properties.
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Dimitar Hristozov
 */
public class RowPairerNodeDialog extends DefaultNodeSettingsPane {
    private SettingsModelFilterString mInColList        = new SettingsModelFilterString(RowPairerNodeModel.CFG_IN_COL_LIST);
    private SettingsModelFilterString mColRatio         = new SettingsModelFilterString(RowPairerNodeModel.CFG_COL_RATIO);
    private SettingsModelFilterString mColDiff          = new SettingsModelFilterString(RowPairerNodeModel.CFG_COL_DIFF);
    private SettingsModelFilterString mOutColList       = new SettingsModelFilterString(RowPairerNodeModel.CFG_OUT_COL_LIST);
    private SettingsModelBoolean      mDuplicate        = new SettingsModelBoolean(RowPairerNodeModel.CFG_DUPLICATE, true);
    private final SettingsModelString mInvertPrecedence = new SettingsModelString("precedence",
                                                                RowPairerNodeModel.PRECEDENCE_OPTIONS[0]);

    /**
     * New pane for configuring the RowPairer node.
     */
    @SuppressWarnings("unchecked")
    protected RowPairerNodeDialog() {
        DialogComponentLabel label1 = new DialogComponentLabel(
                "Select columns containing fragments  to be considered for MP analysis");
        addDialogComponent(label1);

        DialogComponentColumnFilter colFilter = new DialogComponentColumnFilter(mInColList, 0,
                StringCell.TYPE.getPreferredValueClass());
        addDialogComponent(colFilter);

        DialogComponentLabel label2 = new DialogComponentLabel("Select input columns to be included in the output");
        addDialogComponent(label2);

        colFilter = new DialogComponentColumnFilter(mOutColList, 0, true);
        addDialogComponent(colFilter);

        DialogComponentBoolean dup = new DialogComponentBoolean(mDuplicate, "also generate inverse matched pairs");
        addDialogComponent(dup);

        Container c = (Container) getTab("Options");
        Component[] clist = c.getComponents();
        c.removeAll();

        JScrollPane scr = new JScrollPane(clist[0]);

        c.add(scr);

        createNewTab("Property Comparison");

        DialogComponentLabel label3 = new DialogComponentLabel("Select columns for which ratios should be calculated");
        addDialogComponent(label3);

        DialogComponentColumnFilter ratioFilter = new DialogComponentColumnFilter(mColRatio, 0,
                DoubleCell.TYPE.getPreferredValueClass(), IntCell.TYPE.getPreferredValueClass());

        addDialogComponent(ratioFilter);
        DialogComponentLabel label4 = new DialogComponentLabel("Select columns for which differences should be calculated");

        addDialogComponent(label4);
        DialogComponentColumnFilter diffFilter = new DialogComponentColumnFilter(mColDiff, 0,
                DoubleCell.TYPE.getPreferredValueClass(), IntCell.TYPE.getPreferredValueClass());

        addDialogComponent(diffFilter);

        DialogComponentButtonGroup invertPrecedence = new DialogComponentButtonGroup(mInvertPrecedence, true,
                "Property comparison", RowPairerNodeModel.PRECEDENCE_OPTIONS);
        addDialogComponent(invertPrecedence);

        c = (Container) getTab("Property Comparison");
        clist = c.getComponents();
        c.removeAll();

        scr = new JScrollPane(clist[0]);

        c.add(scr);
    }

}
