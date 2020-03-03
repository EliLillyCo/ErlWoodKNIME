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

import jp.co.infocom.cheminfo.marvin.type.MrvValue;

import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.erlwood.knime.utils.gui.ConverterDataColumnSpecListCellRenderer;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "SimilarityViewer" Node. Displays similarity
 * matrix as a heat map. Also allows the sorting with a "query" molecule.
 * 
 * @author Dimitar Hristozov
 */
public class SimilarityViewerNodeDialog extends DefaultNodeSettingsPane {
    private final SettingsModelString  mStructCol = new SettingsModelString(SimilarityViewerNodeModel.CFG_STRUCT_COL, null);
    private final SettingsModelString  mDmCol     = new SettingsModelString(SimilarityViewerNodeModel.CFG_DM_COL, null);
    private final SettingsModelBoolean mIsDm      = new SettingsModelBoolean(SimilarityViewerNodeModel.CFG_IS_DM, false);

    @SuppressWarnings("unchecked")
    protected SimilarityViewerNodeDialog() {
        super();

        DialogComponentColumnNameSelection molComponent = new DialogComponentColumnNameSelection(mStructCol,
                "Structures column", 0, MoleculeDataTypeConverter.getColumnFilter(MrvValue.class));

        ConverterDataColumnSpecListCellRenderer.setRenderer(molComponent, MrvValue.class);
        addDialogComponent(molComponent);

        addDialogComponent(new DialogComponentColumnNameSelection(mDmCol, "Distance/Similarity matrix", 0,
                org.knime.distmatrix.type.DistanceVectorDataValue.class, CollectionDataValue.class));
        addDialogComponent(new DialogComponentBoolean(mIsDm, "Distance matrix"));
    }
}
