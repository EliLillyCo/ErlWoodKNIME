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
package org.erlwood.knime.nodes.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.knime.base.node.util.DataArray;
import org.knime.base.node.util.DefaultDataArray;
import org.knime.base.node.viz.plotter.node.DefaultVisualizationNodeModel;
import org.knime.core.data.container.ContainerTable;
import org.knime.core.data.container.DataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.config.Config;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;

/**
 * This is the model implementation of NGraph.
 * 
 *
 * @author Gary Sharman  
 */
public class NGraphNodeModel extends DefaultVisualizationNodeModel {
    
    public String getXLab() {
		return xLab;
	}

	public void setXLab(String xLab) {
		this.xLab = xLab;
	}

	public String getYLab() {
		return yLab;
	}

	public void setYLab(String yLab) {
		this.yLab = yLab;
	}

	public String getZLab() {
		return zLab;
	}

	public void setZLab(String zLab) {
		this.zLab = zLab;
	}

	public String getColLab() {
		return colLab;
	}

	public void setColLab(String colLab) {
		this.colLab = colLab;
	}

	public String getSizeLab() {
		return sizeLab;
	}

	public void setSizeLab(String sizeLab) {
		this.sizeLab = sizeLab;
	}

	public String getLabLab() {
		return labLab;
	}

	public void setLabLab(String labLab) {
		this.labLab = labLab;
	}

	public boolean isShowFt() {
		return showFt;
	}

	public void setShowFt(boolean showFt) {
		this.showFt = showFt;
	}

	public DataArray getFullArray() {
		return fullArray;
	}

	public void setFullArray(DataArray fullArray) {
		this.fullArray = fullArray;
	}

	public SettingsModelFilterString getIncExCols() {
		return incExCols;
	}

	public SettingsModelInteger getMinSize() {
		return minSize;
	}

	public SettingsModelInteger getMaxSize() {
		return maxSize;
	}

	// the logger instance
    private static final NodeLogger LOG = NodeLogger.getLogger(NGraphNodeModel.class);
    private final SettingsModelFilterString incExCols = new SettingsModelFilterString("incExCols");
    private final SettingsModelInteger minSize = new SettingsModelInteger("minSize", 1);
    private final SettingsModelInteger maxSize = new SettingsModelInteger("maxSize",12);
    private String xLab, yLab,zLab,colLab,sizeLab,labLab;
    private boolean showFt;
    private DataArray fullArray;
    private static final String FULL_FILE_NAME = "fullInternals";
    private File intConfigFile;
        

    /**
     * Constructor for the node model.
     */
    protected NGraphNodeModel() {
        super(1, 0);
        xLab="";
        yLab="";
        zLab="";
        colLab="";
        sizeLab="";
        labLab="";
        showFt=true;
    }
    
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        // generate list of excluded columns, suppressing warning
    	BufferedDataTable[] bdt = super.execute(inData, exec);
        fullArray = new DefaultDataArray(inData[0], 1, getEndIndex(), exec);
        return bdt;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        // save settings for col selections, etc..
        super.saveSettingsTo(settings);
        incExCols.saveSettingsTo(settings);
        minSize.saveSettingsTo(settings);
        maxSize.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // load settings
        super.loadValidatedSettingsFrom(settings);
        incExCols.loadSettingsFrom(settings);
        minSize.loadSettingsFrom(settings);
        maxSize.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // validate settings
    	super.validateSettings(settings);
    	incExCols.validateSettings(settings);
    	minSize.validateSettings(settings);
    	maxSize.validateSettings(settings);

    }

    /**
     * Loads the converted {@link org.knime.base.node.util.DataArray}.
     * 
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    	try {
    		super.loadInternals(nodeInternDir, exec);
    	} catch (Exception e) {
    		
    	}
    	try {
    		File f = new File(nodeInternDir, FULL_FILE_NAME);
    		ContainerTable table = DataContainer.readFromZip(f);
    		fullArray = new DefaultDataArray(table, 1, getEndIndex(), exec);
    	} catch (Exception e) {
    		
    	}
        
        try {
        	intConfigFile = new File(nodeInternDir, "EWGraphConfig");
        	FileInputStream fis = new FileInputStream(intConfigFile);
        	NodeSettingsRO conf = NodeSettings.loadFromXML(fis);
        	fis.close();
        	xLab= conf.getString("xLabel","");
        	yLab= conf.getString("yLabel","");
        	zLab= conf.getString("zLabel","");
        	colLab= conf.getString("colLabel","");
        	sizeLab= conf.getString("sizeLabel","");
        	labLab= conf.getString("labLabel","");
        	showFt= conf.getBoolean("showFit",true);
        } catch (Exception e) {
        	NGraphNodeModel.LOG.warn("oh dear - error loading settings");
			xLab="";
			yLab="";
			zLab="";
			colLab="";
			labLab="";
			sizeLab="";
			showFt=true;
        }
    }

    /**
     * Saves the converted {@link org.knime.base.node.util.DataArray}.
     * 
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    	intConfigFile = new File(nodeInternDir, "EWGraphConfig");
    	super.saveInternals(nodeInternDir, exec);
        File f = new File(nodeInternDir, FULL_FILE_NAME);
        DataContainer.writeToZip(fullArray, f, exec);
        saveSettingsInternals();
    }
    
    void saveSettingsInternals() throws IOException{
    	Config conf = new NodeSettings("EWGraphSettings");
        conf.addString("xLabel",xLab);
        conf.addString("yLabel",yLab);
        conf.addString("zLabel",zLab);
        conf.addString("colLabel",colLab);
        conf.addString("sizeLabel",sizeLab);
        conf.addString("labLabel",labLab);
        conf.addBoolean("showFit",showFt);
        FileOutputStream fos = new FileOutputStream(intConfigFile);
        conf.saveToXML(fos);
        fos.close();
    }
}

