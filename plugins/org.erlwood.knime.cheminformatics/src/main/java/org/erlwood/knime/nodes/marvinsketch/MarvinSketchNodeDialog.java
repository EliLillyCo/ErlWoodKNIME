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
package org.erlwood.knime.nodes.marvinsketch;


import javax.swing.JPanel;

import org.erlwood.knime.utils.gui.MarvinSketcherPanel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

/**
 * <code>NodeDialog</code> for the "MarvinSketch" Node.
 * Allows the drawing of structure/reactions using Marvin Sketch.
 * 
 * @author Hina Patel, Dimitar Hristozov
 */
public class MarvinSketchNodeDialog extends DefaultNodeSettingsPane 
{
	
	private final SettingsModelString mMolSDString =
    	new SettingsModelString(MarvinSketchNodeModel.CFGKEY_MOLS_SD_STRING, null);
    private MarvinSketcherPanel mSketcherPanel;
    
    private final SettingsModelBoolean mAromatize = new SettingsModelBoolean(MarvinSketchNodeModel.CFG_AROMATIZE, true);
    private final SettingsModelBoolean mAddSDFColumn = new SettingsModelBoolean(MarvinSketchNodeModel.CFG_ADD_SDF_COLUMN, false);
    
    
    protected MarvinSketchNodeDialog()
    {
    	super();
    	mSketcherPanel = new MarvinSketcherPanel();    
    	this.setDefaultTabTitle("Draw");
    	JPanel tab = (JPanel)this.getTab("Draw");
    	if (null != tab) {
    		tab.add(mSketcherPanel);
    	}
    	
    }
  
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException
    {
    	try
    	{
    		mMolSDString.loadSettingsFrom(settings);
    		try {
				mAromatize.loadSettingsFrom(settings);
				mSketcherPanel.setAromatize(mAromatize.getBooleanValue());
				
				mAddSDFColumn.loadSettingsFrom(settings);
				mSketcherPanel.setAddSDFColumn(mAddSDFColumn.getBooleanValue());
			} catch (Exception e) {				
			}
    		String cssdf = mMolSDString.getStringValue();
    		mSketcherPanel.initFromSDString(cssdf);
    		
    	}
    	catch (Exception e)
    	{
    		throw new NotConfigurableException(e.getMessage());
    	}
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
    	throws InvalidSettingsException
    {    	
    	mAromatize.setBooleanValue(mSketcherPanel.isAromatize());
    	mAromatize.saveSettingsTo(settings);
    	
    	mAddSDFColumn.setBooleanValue(mSketcherPanel.isAddSDFColumn());
    	mAddSDFColumn.saveSettingsTo(settings);
    	
    	mSketcherPanel.addLastStructure();
        mMolSDString.setStringValue(mSketcherPanel.getSDString());
        mMolSDString.saveSettingsTo(settings);
    }

	
}
