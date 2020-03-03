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
package org.erlwood.knime.nodes.fingerprintsimilarity;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataType;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
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
public class FingerprintSimilarityNodeDialog extends DefaultNodeSettingsPane {

	private SettingsModelString mQuery = new SettingsModelString(
			FingerprintSimilarityNodeModel.CFG_QUERY_COLUMN, "Similarity");
	private SettingsModelString mData = new SettingsModelString(
			FingerprintSimilarityNodeModel.CFG_FP_COLUMN, "Similarity");
	private SettingsModelString mSimSel = new SettingsModelString(
			FingerprintSimilarityNodeModel.CFG_SIM_SEL, "Tanimoto");
	private SettingsModelBoolean mFusion = new SettingsModelBoolean(
			FingerprintSimilarityNodeModel.CFG_FUSION, false);
	private SettingsModelString mIntegration = new SettingsModelString(
			FingerprintSimilarityNodeModel.CFG_INTEGRATION, "MaxSim");
	private SettingsModelString mNewCol = new SettingsModelString(
			FingerprintSimilarityNodeModel.CFG_NEW_COL, "Similarity");

	/**
	 * New pane for configuring the FingerprintSimilarity node.
	 */
	protected FingerprintSimilarityNodeDialog() {
		super();
		@SuppressWarnings("unchecked")
		ColumnFilter fpFilter = new DataValueColumnFilter(
				DenseBitVectorCell.TYPE.getPreferredValueClass());
		DialogComponentColumnNameSelection mQueryColSel = new DialogComponentColumnNameSelection(
				mQuery, "Query column", 0, true, false, fpFilter);
		addDialogComponent(mQueryColSel);
		DialogComponentColumnNameSelection mDataColSel = new DialogComponentColumnNameSelection(
				mData, "Data column", 1, true, false, fpFilter);
		addDialogComponent(mDataColSel);
		List<String> measures = new ArrayList<String>();
		measures.add("Tanimoto");
		measures.add("Intersection");
		DialogComponentStringSelection simSel = new DialogComponentStringSelection(
				mSimSel, "Similarity measure", measures);
		addDialogComponent(simSel);

		DialogComponentString colname = new DialogComponentString(mNewCol,
				"new column name");
		addDialogComponent(colname);

		DialogComponentBoolean fusion = new DialogComponentBoolean(mFusion,
				"Multi-query fusion");
		addDialogComponent(fusion);
		List<String> integrate = new ArrayList<String>();
		integrate.add("MaxSim");
		integrate.add("Average");
		DialogComponentStringSelection integration = new DialogComponentStringSelection(
				mIntegration, "Fusion method", integrate);
		addDialogComponent(integration);

	}

}
