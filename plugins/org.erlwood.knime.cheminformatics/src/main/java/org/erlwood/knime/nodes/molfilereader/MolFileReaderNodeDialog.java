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
package org.erlwood.knime.nodes.molfilereader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.erlwood.knime.utils.gui.ChemFileReaderConfigPanel;
import org.erlwood.knime.utils.gui.ChemFileReaderConfigPanel.PropertyObject;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;

/**
 * <code>NodeDialog</code> for the "MolFileReader" Node. Reads MDL Mol and SD
 * files using ChemAxon library. Outputs a column of ChemAxonMolCells.
 * 
 * @author Hina Patel, Dimitar Hristozov
 */
public class MolFileReaderNodeDialog extends DefaultNodeSettingsPane {
    private final SettingsModelString      mFilename;
    private final SettingsModelStringArray mFilenames;
    private final SettingsModelStringArray mProperties;
    private final SettingsModelStringArray mOutputOptions;
    private final SettingsModelStringArray mOutputColumns;
    private final SettingsModelBoolean	   mIsShallowScan;
    private final SettingsModelString	   mLastDirectory;
    private final SettingsModelStringArray mFoundProperties;
    private final SettingsModelBoolean	   mIsRequiresScan;
    
    private final String[]                 mDefOutOpt;
    private final String[]                 mDefOutCols;
    private ChemFileReaderConfigPanel      mPnlConfig;

    public MolFileReaderNodeDialog(final String cfgFileName, final String cfgFileProps, final String cfgOutOptions,
            final String cfgOutCols, final String cfgIsShallowScan, final String cfgLastDirectory, final String cfgFoundProperties, final String cfgRequiresScan, boolean isReactions, final String[] defOutOps, final String[] defOutCols) {
        mFilename = new SettingsModelString(cfgFileName, "");
        mFilenames= new SettingsModelStringArray(cfgFileName, new String[] {});
        mProperties = new SettingsModelStringArray(cfgFileProps, new String[] {});
        mOutputOptions = new SettingsModelStringArray(cfgOutOptions, new String[] {});
        mOutputColumns = new SettingsModelStringArray(cfgOutCols, new String[] {});
        mIsShallowScan = new SettingsModelBoolean(cfgIsShallowScan, true);
        mLastDirectory = new SettingsModelString(cfgLastDirectory, System.getProperty("user.home"));
        
        mFoundProperties = new SettingsModelStringArray(cfgFoundProperties, new String[] {});
        mIsRequiresScan = new SettingsModelBoolean(cfgRequiresScan, true);
        
        mDefOutOpt = defOutOps;
        mDefOutCols = defOutCols;
        mPnlConfig = new ChemFileReaderConfigPanel(isReactions);
        ((JPanel) this.getTab("Options")).add(mPnlConfig);
    }

    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
            throws NotConfigurableException {
        try {
        	String[] files = new String[0];
        	mPnlConfig.resetCancelled();
        	
        	try {
        		//	If we don't have the new style the try the old
        		mFilenames.loadSettingsFrom(settings);
        		files = mFilenames.getStringArrayValue();
        		
        	} catch(InvalidSettingsException ex) {
        		try {
	        		mFilename.loadSettingsFrom(settings);
	        		files = new String[] {mFilename.getStringValue()};
	        		mFilenames.setStringArrayValue(files);
        		} catch(InvalidSettingsException ex2) {
        			//	Do nothing
        		}
        	}
            
        	try {
	        	mIsShallowScan.loadSettingsFrom(settings);
	        	mPnlConfig.setShallowScan(mIsShallowScan.getBooleanValue());
        	} catch(InvalidSettingsException ex) {
        		//	Do nothing
        	}
        	
        	try {
        		mLastDirectory.loadSettingsFrom(settings);
        		mPnlConfig.setLastDirectory(mLastDirectory.getStringValue());
	        } catch(InvalidSettingsException ex) {
	    		//	Do nothing
	    	}
        	
        	try {
        		mIsRequiresScan.loadSettingsFrom(settings);
        		mPnlConfig.setRequiresScan(mIsRequiresScan.getBooleanValue());
	        } catch(InvalidSettingsException ex) {
	    		//	Do nothing
	    	}
        	
        	try {
        		mFoundProperties.loadSettingsFrom(settings);        		
	        } catch(InvalidSettingsException ex) {
	    		//	Do nothing
	    	}
        	             
        	mProperties.loadSettingsFrom(settings);
        	
            mPnlConfig.setFileNames(files);
            
            
            
            if (mIsRequiresScan.getBooleanValue()) {
            	List<PropertyObject> selectedProps = new ArrayList<PropertyObject>();
                for (String s : mProperties.getStringArrayValue()) {
                	selectedProps.add(new PropertyObject(s, null));
                }
	            if (files.length > 0) {
	            	mPnlConfig.executeScan(selectedProps);
	            }
            } else {
            	mPnlConfig.setAllProperties(mFoundProperties.getStringArrayValue(), mProperties.getStringArrayValue());
            }
            
            mOutputOptions.loadSettingsFrom(settings);
            String[] opt = mOutputOptions.getStringArrayValue();
            if (null == opt || 0 == opt.length) {
                opt = mDefOutOpt;
            }
            TreeMap<String, Boolean> parsedOpt = new TreeMap<String, Boolean>();
            for (String s : opt) {
                int i = s.lastIndexOf(':');
                assert (-1 != i);
                assert (s.length() > i);
                parsedOpt.put(s.substring(0, i), s.substring(i + 1).startsWith("1"));
            }
            mPnlConfig.setOutputOptions(parsedOpt);
            mOutputColumns.loadSettingsFrom(settings);
            opt = mOutputColumns.getStringArrayValue();
            if (null == opt || 0 == opt.length) {
                opt = mDefOutCols;
            }
            parsedOpt.clear();
            for (String s : opt) {
                int i = s.lastIndexOf(':');
                assert (-1 != i);
                assert (s.length() > i);
                parsedOpt.put(s.substring(0, i), s.substring(i + 1).startsWith("1"));
            }
            mPnlConfig.setOutputColumnTypes(parsedOpt);
        } catch (InvalidSettingsException ise) {
            throw new NotConfigurableException(ise.getMessage());
        }
    }

    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        mFilenames.setStringArrayValue(mPnlConfig.getFileNames());
        mFilenames.saveSettingsTo(settings);
        List<String> props = mPnlConfig.getSelectedProperties();
        mProperties.setStringArrayValue(props.toArray(new String[props.size()]));
        mProperties.saveSettingsTo(settings);
        Map<String, Boolean> opt = mPnlConfig.getOutputOptions();
        String[] data = new String[opt.size()];
        int i = 0;
        for (String key : opt.keySet()) {
            data[i] = key + (opt.get(key) ? ":1" : ":0");
            i += 1;
        }
        mOutputOptions.setStringArrayValue(data);
        mOutputOptions.saveSettingsTo(settings);
        opt = mPnlConfig.getOutputColumnTypes();
        data = new String[opt.size()];
        i = 0;
        int falseCount = 0;
        for (String key : opt.keySet()) {
            data[i] = key + (opt.get(key) ? ":1" : ":0");
            if (!opt.get(key)) {
                falseCount += 1;
            }
            i += 1;
        }
        if (opt.size() == falseCount) {
            throw new InvalidSettingsException("Please select at least one output column.");
        }
        mOutputColumns.setStringArrayValue(data);
        mOutputColumns.saveSettingsTo(settings);
        
        mIsShallowScan.setBooleanValue(mPnlConfig.isShallowScan());
        mIsShallowScan.saveSettingsTo(settings);
        
        mLastDirectory.setStringValue(mPnlConfig.getLastDirectory());
        mLastDirectory.saveSettingsTo(settings);
        
        mIsRequiresScan.setBooleanValue(mPnlConfig.isRequiresScan());
        mIsRequiresScan.saveSettingsTo(settings);
        
        props = mPnlConfig.getFoundProperties();
        mFoundProperties.setStringArrayValue(props.toArray(new String[props.size()]));        
        mFoundProperties.saveSettingsTo(settings);
        
    }
    
    public void onCancel() {
    	mPnlConfig.onCancel();
    }
}
