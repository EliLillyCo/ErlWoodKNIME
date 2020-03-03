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
package org.erlwood.knime.nodes.stringtotable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFlowVariableNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.workflow.FlowVariable;

/** Dialog for the String to Table Node.
 * @author Tom Wilkin */
public class StringToTableNodeDialog extends DefaultNodeSettingsPane implements ChangeListener {
	
	/** Settings model for the user column selection. */
	private final SettingsModelString strCol;
	
	/** Settings model for the user variable selection. */
	private final SettingsModelString strVar;
	
	/** UI component for the flow variables to be populated in onOpen. */
	private DialogComponentFlowVariableNameSelection strVarComponent;
	
	StringToTableNodeDialog( ) {
		// initialise
		strCol = createSettingsModelStringColumn( );
		strVar = createSettingsModelStringVariable( );
		
		// column filter
		ColumnFilter stringFilter = new ColumnFilter( ) {

            @Override
            public boolean includeColumn(final DataColumnSpec colSpec) {
                return colSpec.getType( ).isCompatible(StringValue.class);
            }

            @Override
            public String allFilteredMsg( ) {
                return "No compatible path string field in input.";
            }

        };
		
		// column selection
		DialogComponentColumnNameSelection strColComponent = new DialogComponentColumnNameSelection(
				strCol, "String Column:", 0, false, true, stringFilter
		);
		strColComponent.setToolTipText(
				"Select the column (if it exists) containing the string of the table to convert."
		);
		addDialogComponent(strColComponent);
		strCol.addChangeListener(this);
		
		// variable selection
		strVarComponent = new DialogComponentFlowVariableNameSelection(
				strVar, 
				"String Variable:", 
				getAvailableFlowVariables( ).values( ), 
				true, 
				FlowVariable.Type.STRING
		);
		strVarComponent.setToolTipText(
				"Select the variable containing the string of the table to convert."
		);
		addDialogComponent(strVarComponent);
		strVar.addChangeListener(this);
	}
	
	@Override
	public void onOpen( ) {
		super.onOpen( );
		
		// update the list of flow variables that can be selected
        strVarComponent.replaceListItems(getAvailableFlowVariables( ).values( ), null);
        
        // enable/disable the selections
        checkEnabled( );
	}
	
	@Override
    public void stateChanged(final ChangeEvent e) {
        checkEnabled( );
    }
	
	/** Check and enable/disable the drop-down boxes based on the user selection and the available
	 * inputs. */
	private void checkEnabled() {
        // disable column selection if there is not input table
        if (getNodeContext( ).getWorkflowManager( ).getIncomingConnectionFor(
        		getNodeContext( ).getNodeContainer( ).getID( ), 1) == null) 
        {
            strCol.setEnabled(false);
            strVar.setEnabled(true);
            return;
        }

        // disable variable if column is selected
        if (strCol.getStringValue( ) == null || strCol.getStringValue( ).equals("")) {
            strVar.setEnabled(true);
        } else {
            strVar.setEnabled(false);
        }

        // disable column if variable is selected
        if (strVar.getStringValue( ) == null 
        		|| strVar.getStringValue( ).equals("") 
        		|| strVar.getStringValue( ).equals("NONE"))
        {
            strCol.setEnabled(true);
        } else {
            strCol.setEnabled(false);
        }
    }

	/** @return The settings model for the user column selection. */
	static SettingsModelString createSettingsModelStringColumn( ) {
		return new SettingsModelString("strCol", "");
	}
	
	/** @return The settings model for the user variable selection. */
	static SettingsModelString createSettingsModelStringVariable( ) {
		return new SettingsModelString("strVar", "");
	}
	
};
