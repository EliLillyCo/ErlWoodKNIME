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
package org.erlwood.knime.nodes.sdfvar2table;


import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFlowVariableNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;

/**
 * <code>NodeDialog</code> for the "SdfVariable2Table" Node.
 * 
 * @author Luke Bullard
 */
public class SdfVariable2TableNodeDialog extends DefaultNodeSettingsPane 
{
	private SettingsModelString 		mVariableName 	= new SettingsModelString(SdfVariable2TableNodeModel.CFG_VARIABLE_NAME, null); 	
    private final SettingsModelBoolean  mAromatize 		= new SettingsModelBoolean(SdfVariable2TableNodeModel.CFG_AROMATIZE, 	true);
    private final SettingsModelBoolean  mSplit	 		= new SettingsModelBoolean(SdfVariable2TableNodeModel.CFG_SPLIT, 		false);
    
    private DialogComponentFlowVariableNameSelection flowVariables;
    protected SdfVariable2TableNodeDialog()
    {
    	super();
    	    	       
    	flowVariables = new DialogComponentFlowVariableNameSelection(mVariableName, "Variable Name", 
    			getAvailableFlowVariables().values(), false, FlowVariable.Type.STRING);
    	
    	DialogComponentBoolean aromatize = new DialogComponentBoolean(mAromatize, "Aromatize structures");
    	DialogComponentBoolean split = new DialogComponentBoolean(mSplit, "Split multiple structures");
    	
    	addDialogComponent(flowVariables);
    	addDialogComponent(aromatize);
    	addDialogComponent(split);
    	    	
    }
  

	@Override
	public void onOpen( ) {
		super.onOpen( );
		
		// update the list of flow variables that can be selected
		flowVariables.replaceListItems(getAvailableFlowVariables( ).values( ), null);
                
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException
    {    	
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
    	throws InvalidSettingsException
    {    	
        
    }

	
}
