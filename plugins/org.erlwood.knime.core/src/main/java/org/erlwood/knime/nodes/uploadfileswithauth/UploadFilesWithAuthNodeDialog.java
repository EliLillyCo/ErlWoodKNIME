/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2015 Eli Lilly and Company Limited
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
package org.erlwood.knime.nodes.uploadfileswithauth;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.erlwood.knime.utils.gui.auth.AuthenticationTab;
import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.erlwood.knime.utils.gui.togglepanel.TogglePanel;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentFlowVariableNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.core.node.util.DataValueColumnFilter;
import org.knime.core.node.workflow.FlowVariable;

/**
 * Dialog for the Upload Files with Authentication Node.
 * 
 * @author Tom Wilkin
 */
class UploadFilesWithAuthNodeDialog extends NodeDialogPane {
	
	/** The settings for this node. */
    private UploadFilesWithAuthSettings nodeSettings;
    
    /** The authentication tab containing the workflow credentials drop-down. */
    private AuthenticationTab authentication;
    
    /** The text entry for the network share path. */
    private JTextField networkSharePath;
    
    /** The drop-down for the input path column selection. */
    private ColumnSelectionPanel pathColumn;
    
    /** The drop-down for the input path variable selection. */
    private DialogComponentFlowVariableNameSelection pathVariable;
    
    /** The toggle panel which allows the user to switch between column and variable selection. */
    private TogglePanel pathTogglePanel;
    
    /** The option for overwriting the target files. */
    private JCheckBox overwrite;
    
    UploadFilesWithAuthNodeDialog( ) {
    	nodeSettings = new UploadFilesWithAuthSettings( );
    	createGUI( );
    }

    @Override
	public void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[ ] specs) 
			throws NotConfigurableException 
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
		
		// update the UI with the loaded values		
		networkSharePath.setText(nodeSettings.getSharePath( ));
		if(specs != null && specs[0] != null) {
			pathColumn.update(specs[0], nodeSettings.getPathColumn( ));
		}
		pathVariable.loadSettingsFrom(settings, specs);
		pathVariable.replaceListItems(
				getAvailableFlowVariables( ).values( ), 
				nodeSettings.getPathVariable( )
		);
		pathTogglePanel.setSelected(nodeSettings.getPathUseColumn( ));
		overwrite.setSelected(nodeSettings.isOverwrite( ));
	}
	
	@Override
	public void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		// pull the settings from the components
		nodeSettings.setSharePath(networkSharePath.getText( ));
		nodeSettings.setPathColumn(pathColumn.getSelectedColumn( ));
		nodeSettings.setPathVariable(
				((SettingsModelString)pathVariable.getModel( )).getStringValue( )
		);
		nodeSettings.setPathUseColumn(pathTogglePanel.isSelected( ));
		nodeSettings.setOverwrite(overwrite.isSelected( ));
		
		// save the settings		
		authentication.saveSettingsTo(nodeSettings);
		nodeSettings.saveSettingsTo(settings);
	}
	
	/** Create the GUI elements. */
	private void createGUI( ) {		
    	// initialise the options tab
    	JPanel panel = new JPanel( );
    	panel.setLayout(new TableLayout(new double[ ][ ] {
    			{ TableLayout.PREFERRED, 10, TableLayout.FILL },
    			{ 
    				TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 
    				TableLayout.FILL 
    			}
    	}));
    	panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    	
    	// add the network share path
    	panel.add(new JLabel("Network Share Path:"), "0,0");
    	networkSharePath = new JTextField( );
    	networkSharePath.setToolTipText(
    			"The path to the network share and directory that the files should be copied to."
    	);
    	panel.add(networkSharePath, "2,0");
    	
    	// path column selection
    	@SuppressWarnings("unchecked")
		ColumnFilter stringFilter = new DataValueColumnFilter(StringValue.class);
    	pathColumn = new ColumnSelectionPanel((Border)null, stringFilter, true);
    	pathColumn.setToolTipText(
    			"Column in the input (if it exists) containing the paths to local files that "
    			+ "should be copied to the remote location."
        );
    	
    	// path variable selection
    	pathVariable = new DialogComponentFlowVariableNameSelection(
    			nodeSettings.getPathVariableModel( ),
    			null,
    			getAvailableFlowVariables( ).values( ),
    			true,
    			FlowVariable.Type.STRING    			
    	);
    	pathVariable.setToolTipText(
    			"Flow variable that can be used to set a single path that should be copied to the "
    			+ "remote location."
    	);
    	
    	// add the path selection
    	panel.add(new JLabel("Path Selection:"), "0,2");
    	pathTogglePanel = new TogglePanel(
    			pathVariable.getComponentPanel( ).getComponent(0), 
    			TogglePanel.InputType.FLOW_VARIABLE,
    			pathColumn, 
    			TogglePanel.InputType.TABLE,
    			50
    	);
    	panel.add(pathTogglePanel, "2,2");
    	
    	// add the overwrite option
    	overwrite = new JCheckBox("Overwrite existing files.");
    	panel.add(overwrite, "2,4");
    	
    	// add the options panel
    	addTab("Options", panel);
    	
    	// add the authentication tab
    	authentication = new AuthenticationTab(false);
    	addTab(AuthenticationTab.TITLE, authentication);
	}

}
