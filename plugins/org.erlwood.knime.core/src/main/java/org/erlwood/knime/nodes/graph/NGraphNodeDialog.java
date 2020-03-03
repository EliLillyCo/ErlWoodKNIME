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
package org.erlwood.knime.nodes.graph;

import org.knime.base.node.viz.plotter.node.DefaultVisualizationNodeDialog;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;


public class NGraphNodeDialog extends DefaultVisualizationNodeDialog {

    /**
     * New pane for configuring NGraph node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected NGraphNodeDialog() {
        super();
        DialogComponentColumnFilter cf;
		cf = new DialogComponentColumnFilter(new SettingsModelFilterString(
				"incExCols"), 0, true);
        createNewGroup("Columns to show in popup details");
        addDialogComponent(cf);
        closeCurrentGroup();
        addDialogComponent(new DialogComponentNumber(new SettingsModelInteger("minSize", 1),"Minimum Point Size ",1));
        addDialogComponent(new DialogComponentNumber(new SettingsModelInteger("maxSize", 12),"Maximum Point Size ",1));

    }
}

