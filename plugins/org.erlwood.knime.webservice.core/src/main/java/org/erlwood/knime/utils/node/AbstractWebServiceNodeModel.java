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
package org.erlwood.knime.utils.node;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import org.erlwood.knime.utils.IWebServicePreferenceSupplier;
import org.erlwood.knime.utils.iotiming.IOTiming;
import org.knime.base.node.preproc.joiner.Joiner;
import org.knime.base.node.preproc.joiner.Joiner2Settings;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import org.erlwood.knime.utils.settings.WebServiceSettings;

/** A node model that implements the required methods for storing the web service settings tab 
 * content.
 * Implementing classes must call super. on the following methods to save/retrieve the web service
 * settings:
 * -- configure
 * -- saveSettingsTo
 * -- loadValidatedSettingsFrom
 * -- validateSettings
 * 
 * @author Tom Wilkin */
public abstract class AbstractWebServiceNodeModel extends NodeModel implements IOTiming, IWebServicePreferenceSupplier {
	
	/** The web service additional properties. */
	protected WebServiceSettings wsSettings;
	private long ioTiming;
	private boolean bFirstTime = true;
	

	protected AbstractWebServiceNodeModel(final int nrInDataPorts, final int nrOutDataPorts)
	{
		this(nrInDataPorts, nrOutDataPorts, -1);		
	}
	
	
	protected AbstractWebServiceNodeModel(final int nrInDataPorts, final int nrOutDataPorts,
			final int options)
	{
		super(nrInDataPorts, nrOutDataPorts);
		wsSettings = new WebServiceSettings(this);
		
		if(options > -1) {
			wsSettings.setOptions(options);
		}

	}
	
	protected AbstractWebServiceNodeModel(final int nrInDataPorts, final int nrOutDataPorts, 
			final WebServiceSettings settings) 
	{
		super(nrInDataPorts, nrOutDataPorts);
		wsSettings = settings;
	}

	
	protected AbstractWebServiceNodeModel(final PortType[ ] inPortTypes, 
			final PortType[ ] outPortTypes)
	{
		this(inPortTypes, outPortTypes, -1);
	}
	
	
	protected AbstractWebServiceNodeModel(final PortType[ ] inPortTypes, 
			final PortType[ ] outPortTypes, final WebServiceSettings settings) 
	{
		super(inPortTypes, outPortTypes);
		wsSettings = settings;
	}
	
	protected AbstractWebServiceNodeModel(final PortType[ ] inPortTypes, final PortType[ ] outPortTypes, final int options) 
	{
		super(inPortTypes, outPortTypes);
		wsSettings = new WebServiceSettings(this);
		
		if(options > -1) {
			wsSettings.setOptions(options);
		}
	}
	
	@Override
	public String getURLSuffix() {
		// by default most web service nodes have no suffix
		return null;
	}
	
	@Override
	public String getKeyPrefix() {
		return null;
	}
	
	@Override
	protected final DataTableSpec[ ] configure(final DataTableSpec[ ] inSpecs) 
			throws InvalidSettingsException 
	{
		// do not override this method as it will never be called because PortObjectSpec version
		// is overridden
		throw new InvalidSettingsException(
				"DataTableSpec configure method should not be called or implemented, use PortObjectSpec version."
		);
	}
	
	@Override
	protected PortObjectSpec[ ] configure(final PortObjectSpec[ ] inSpecs) 
		throws InvalidSettingsException 
	{
		if(wsSettings.isUseURLOverride( )) {
			appendWarningMessage(
					"URL from node configuration is overriding the default value from the preferences."
			);
		}
		
		//	If this is the first time through the configure then
		//	perform a validate to check if we can simply allow the node to execute.
		if (bFirstTime) {
			bFirstTime = false;
			NodeSettings sett = new NodeSettings("node settings");
			saveSettingsTo(sett);
			validateSettings(sett);			
		}
		
		// cannot configure as output spec is dependent on returned columns from web service
		return new DataTableSpec[ ] { null };
	}
	
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		wsSettings.saveSettingsTo(settings);
	}
	
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException 
	{
		wsSettings.loadSettingsFrom(settings);
	}
	
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException 
	{
		wsSettings.validateSettings(settings);
	}
	
	@Override
	protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException 
	{
		// not required
	}

	@Override
	protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException 
	{
		// not required
	}

	@Override
	protected void reset( ) {
		// not required		
	}

	public void setTiming(long time) {
		this.ioTiming = time;
	}
	
	public void addTiming(long time) {
		this.ioTiming += time;
	}
	
	public long getTiming() {
		return ioTiming;
	}
	
	/**
	 * Append an additional line to the current warning message for this node.
	 * @param message The message string to append to the current warning message.
	 */
	protected final void appendWarningMessage(final String message) {
		String warning = getWarningMessage();
		if(warning != null) {
			warning += System.getProperty("line.separator") + message;
		} else {
			warning = message;
		}
		
		setWarningMessage(warning);
	}
	
	/**
	 * Join two tables together with an inner join.
	 * @param exec The execution context
	 * @param inTable The left hand table
	 * @param resultTable The right hand table
	 * @param leftJoinColumn The column for the left hand join
	 * @param rightJoinColumn The column for the right hand join
	 * @return A BufferedDataTable.
	 * @throws Exception On Error
	 */
	protected BufferedDataTable joinTable(ExecutionContext exec, BufferedDataTable inTable, BufferedDataTable resultTable, String leftJoinColumn, String rightJoinColumn) throws Exception {
		//	We need to ensure that we have compatible columns
		int iLeftIndex = inTable.getDataTableSpec().findColumnIndex(leftJoinColumn);
		final int iRightIndex = resultTable.getDataTableSpec().findColumnIndex(rightJoinColumn);
		
		//	If not then modify the right hand table appropriately
		if (iLeftIndex != -1 && iRightIndex != -1 
				&& !inTable.getDataTableSpec().getColumnSpec(iLeftIndex).equalStructure(resultTable.getDataTableSpec().getColumnSpec(iRightIndex)))
		{
			final DataType dt = inTable.getDataTableSpec().getColumnSpec(iLeftIndex).getType();
			
			ColumnRearranger rearranger = new ColumnRearranger(resultTable.getDataTableSpec());
		    rearranger.replace(new SingleCellFactory(new DataColumnSpecCreator(rightJoinColumn, dt).createSpec()) {
				
				@Override
				public DataCell getCell(DataRow row) {
					DataCell dc = row.getCell(iRightIndex);
					String sv = dc.toString();
					
					if (dt.equals(StringCell.TYPE)) {
						return new StringCell(sv);
					}
					if (dt.equals(DoubleCell.TYPE)) {
						return new DoubleCell(new BigDecimal(sv).doubleValue());
					}
					if (dt.equals(IntCell.TYPE)) {
						return new IntCell(new BigDecimal(sv).intValue());
					}
					return dc;
				}
			}, iRightIndex);
		    
		    resultTable = exec.createColumnRearrangeTable(resultTable, rearranger, exec);
		}
		
		Joiner2Settings jSettings = new Joiner2Settings();	    
	    jSettings.setLeftJoinColumns(new String[]  { leftJoinColumn  });
	    jSettings.setRightJoinColumns(new String[] { rightJoinColumn });
	    jSettings.setJoinMode(Joiner2Settings.JoinMode.InnerJoin);
	    
	    Joiner joiner = new Joiner(inTable.getDataTableSpec(), resultTable.getDataTableSpec(), jSettings);
	    
	    return joiner.computeJoinTable(inTable, resultTable, exec);
	}
};
