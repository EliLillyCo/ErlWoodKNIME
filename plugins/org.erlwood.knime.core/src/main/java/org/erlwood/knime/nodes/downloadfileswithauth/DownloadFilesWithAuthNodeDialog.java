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
package org.erlwood.knime.nodes.downloadfileswithauth;

import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.erlwood.knime.utils.gui.auth.AuthenticationTab;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;

/** Dialog for the Download Files with Authentication Node.
 * @author Tom Wilkin */
public class DownloadFilesWithAuthNodeDialog extends DefaultNodeSettingsPane {
	
	/** The settings. */
	DownloadFilesWithAuthSettings nodeSettings;
    
    /** The authentication tab containing the credentials drop-down. */
    private AuthenticationTab authentication;
    
	@SuppressWarnings("unchecked")
	DownloadFilesWithAuthNodeDialog( ) {
		nodeSettings = new DownloadFilesWithAuthSettings( );
		
		// add authentication tab
		authentication = new AuthenticationTab(false);
		addTab(AuthenticationTab.TITLE, authentication);
		
		// create URL column selection
		DialogComponent urlColComponent = new DialogComponentColumnNameSelection(
				nodeSettings.getUrlColumnModel( ), "File URL Column:", 0, false, false, 
				StringCell.TYPE.getPreferredValueClass( ));
		urlColComponent.setToolTipText("Select the column containing the URLs to download.");
		addDialogComponent(urlColComponent);
		
		// option for deleting temporary directory on reset
		DialogComponent resetComponent = new DialogComponentBoolean(
				nodeSettings.getDeleteTempModel( ), 
				"Delete on Reset:"
		);
		resetComponent.setToolTipText("Whether the temporary directory contents should be deleted on reset. (Only used when a directory is not specfied.)");
		addDialogComponent(resetComponent);
		
		// option for setting a output directory path
		createNewGroup("Custom Output Directory:");
		DialogComponent outDirBoolComponent = new DialogComponentBoolean(
				nodeSettings.getOutputDirectoryEnabledModel( ), 
				"Enable Custom Output Directory"
		);
		outDirBoolComponent.setToolTipText("Enable the selection of a custom output directory.");
		addDialogComponent(outDirBoolComponent);
		DialogComponent outDirComponent = new DialogComponentFileChooser(
				nodeSettings.getOutputDirectoryModel( ), 
				"DownloadFilesWithAuthHistory", 
				JFileChooser.OPEN_DIALOG, 
				true
		);
		outDirComponent.setToolTipText("Select the output directory where the files should be downloaded to.");
		addDialogComponent(outDirComponent);
		closeCurrentGroup( );
		
		// add listeners to prevent invalid options
		ChangeListener listener = new ChangeListener( ) {
			@Override
			public void stateChanged(final ChangeEvent event) {
				checkEnabled( );
			}
		};
		nodeSettings.getDeleteTempModel( ).addChangeListener(listener);
		nodeSettings.getOutputDirectoryEnabledModel( ).addChangeListener(listener);
	}
	
	@Override
	public void onOpen( ) {
		checkEnabled( );
	}
	
	@Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[ ] specs) throws NotConfigurableException 
	{
		try {
			nodeSettings.loadSettingsFrom(settings);
		} catch(InvalidSettingsException e) {
			throw new NotConfigurableException(e.getMessage( ), e);
		}
		authentication.loadSettingsFrom(
				nodeSettings,
				getAvailableFlowVariables( ),
				getCredentialsProvider( )
		);
	}
	
	@Override
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException 
	{
		authentication.saveSettingsTo(nodeSettings);
		nodeSettings.saveSettingsTo(settings);
	}
	
	/** Check whether the custom directory selection is usable. */
	private void checkEnabled( ) {
		if(nodeSettings.getOutputDirectoryEnabledModel( ).getBooleanValue( )) {
			nodeSettings.getDeleteTempModel( ).setEnabled(false);
			nodeSettings.getOutputDirectoryModel( ).setEnabled(true);
		} else {
			nodeSettings.getDeleteTempModel( ).setEnabled(true);		
			nodeSettings.getOutputDirectoryModel( ).setEnabled(false);
		}
	}
    
};
