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
package org.erlwood.knime.nodes.molfilewriter;

import javax.swing.JFileChooser;

import jp.co.infocom.cheminfo.marvin.type.MrvValue;

import org.knime.bio.types.PdbValue;
import org.knime.chem.types.CMLValue;
import org.knime.chem.types.Mol2Value;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "MolFileWriter" Node.
 * Writes chemical structures to a file.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Dimitar Hristozov
 */
public class MolFileWriterNodeDialog extends DefaultNodeSettingsPane
{

	private final SettingsModelString mFilename =
    	new SettingsModelString(MolFileWriterNodeModel.CFG_FILE_NAME, "");
	private final SettingsModelString mFileformat =
    	new SettingsModelString(MolFileWriterNodeModel.CFG_FILE_FORMAT, "sdf");
	private final SettingsModelString mMolColumn =
    	new SettingsModelString(MolFileWriterNodeModel.CFG_MOL_COLUMN, "");
	private final SettingsModelBoolean mVerbatim = 
		new SettingsModelBoolean(MolFileWriterNodeModel.CFG_VERBATIM, false);
	private final SettingsModelFilterString mProps =
		new SettingsModelFilterString(MolFileWriterNodeModel.CFG_PROPS);
	
    @SuppressWarnings("unchecked")
	protected MolFileWriterNodeDialog()
    {
    	DialogComponentFileChooser dcFc = new DialogComponentFileChooser(
    		mFilename, "MolFileWriterNodeDialog_history", 
    		JFileChooser.SAVE_DIALOG, false);
    	dcFc.setBorderTitle("File name:");
    	addDialogComponent(dcFc);
    	
    	DialogComponentStringSelection dcFmt = 
    		new DialogComponentStringSelection(mFileformat, "File format:",
    			"sdf", "smiles", "mol2", "cml", "pdb");
    	addDialogComponent(dcFmt);
    	
    	DialogComponentColumnNameSelection dcMolCol = 
    		new DialogComponentColumnNameSelection(mMolColumn, "Structures",
    			0, SdfValue.class, Mol2Value.class, MrvValue.class, 
    			PdbValue.class, CMLValue.class, SmilesValue.class, 
    			StringValue.class);
    	addDialogComponent(dcMolCol);
    	
    	DialogComponentBoolean dcVerbatim = new DialogComponentBoolean(
    		mVerbatim, "Output verbatim");
    	addDialogComponent(dcVerbatim);
    	
    	DialogComponentColumnFilter dcProps = 
    		new DialogComponentColumnFilter( mProps, 0, true);
    	addDialogComponent(dcProps);
    }
}

