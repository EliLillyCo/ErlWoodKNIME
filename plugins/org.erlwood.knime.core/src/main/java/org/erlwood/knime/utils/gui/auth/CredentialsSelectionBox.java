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
package org.erlwood.knime.utils.gui.auth;

import java.awt.FlowLayout;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.erlwood.knime.utils.auth.AuthenticationUtils;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.FlowVariable;

/** A JComboBox which can be pre-populated by the available workflow credentials.
 * @author Tom Wilkin */
@SuppressWarnings("serial")
public class CredentialsSelectionBox extends JPanel {
	
	/** Whether to display the 'no credentials' option. */
	private boolean allowNone;
	
	/** Whether the credentials entered in this box should be used or not. */
	private JCheckBox useCredentials;
	
	/** The drop-down containing credentials. */
	private JComboBox<String> credentials;
	
	public CredentialsSelectionBox(final boolean allowNone) {
		this.allowNone = allowNone;
		createGUI( );
	}
	
	/** Update the items in the combo box with the current set of workflow credentials.
	 * @param provider The credentials provider to extract the workflow credential names from. */
	public void updateItems(final Map<String, FlowVariable> flowMap,
			final CredentialsProvider provider) 
	{
		credentials.removeAllItems( );
		
		// add the flow variable credential if set
		boolean flowVarAdded = false;
		if(AuthenticationUtils.isFlowVariableCredentialsSet(flowMap)) {
			credentials.addItem(AuthenticationUtils.getDefaultCredentialsName( ));
			flowVarAdded = true;
		}
		
		// add the user specified credentials
		if(provider != null) {
			for(String str : provider.listNames( )) {
				if(!flowVarAdded || !str.equals(AuthenticationUtils.getDefaultCredentialsName( ))) {
					credentials.addItem(str);
				}
			}
		}
	}

	/** @return The currently selected credential item, or null if disabled. */
	public String getSelectedItem( ) {
		String item = (String)credentials.getSelectedItem( );
		
		if(allowNone) {
			if(!useCredentials.isSelected( )) {
				return null;
			}
		}
		
		return item;
	}
	
	/** @return Whether the credential selection is currently enabled. */
	public boolean isUseCredentials( ) {
		if(allowNone) {
			return useCredentials.isSelected( );
		}
		
		return true;
	}
	
	/** Set the selected item as the item with the specified name.
	 * @param enabled Whether the credentials selection should be enabled.
	 * @param name The name of the currently selected workflow credential. */
	public void setSelectedItem(final boolean enabled, final String name) {
		if(name != null) {
			credentials.setSelectedItem(name);
		}
		
		if(allowNone) {
			useCredentials.setSelected(enabled);
		}
	}
	
	/** Create the Credentials Box UI. */
	private void createGUI( ) {
		setLayout(new FlowLayout( ));
		
		// create the credentials
		credentials = new JComboBox<String>( );
		
		// add the check-box, if allowed
		if(allowNone) {
			useCredentials = new JCheckBox( );
			useCredentials.addChangeListener(new ChangeListener( ) {

				@Override
				public void stateChanged(final ChangeEvent event) {
					// enable/disable the drop-down based on the check box
					if(useCredentials.isSelected( )) {
						credentials.setEnabled(true);
					} else {
						credentials.setEnabled(false);
					}
				} 
				
			});
			
			credentials.setEnabled(false);
		}
		
		// add the tool tip text
		String toolTip = "Enable to allow selection of workflow credentials to use with this node.";
		setToolTipText(toolTip);
		credentials.setToolTipText(toolTip);
		
		// add to the panel
		if(allowNone) {
			useCredentials.setToolTipText(toolTip);
			add(useCredentials);
		}
		add(credentials);
	}
	
}
