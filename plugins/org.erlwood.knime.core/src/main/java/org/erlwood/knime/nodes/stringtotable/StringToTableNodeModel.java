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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.DataContainer;

/** Node which converts a KNIME table in a string format back to its original table form.
 * @author Tom Wilkin */
public class StringToTableNodeModel extends NodeModel {
	
	/** Settings model for the user column selection. */
	private final SettingsModelString strCol;
	
	/** Settings model for the user variable selection. */
	private final SettingsModelString strVar;
	
	StringToTableNodeModel( ) {
		super(
				new PortType[ ] { BufferedDataTable.TYPE_OPTIONAL },
				new PortType[ ] { BufferedDataTable.TYPE }
		);
		
		// initialise
		strCol = StringToTableNodeDialog.createSettingsModelStringColumn( );
		strVar = StringToTableNodeDialog.createSettingsModelStringVariable( );
	}
	
	@Override
	protected BufferedDataTable[ ] execute(
			final BufferedDataTable[ ] inData, final ExecutionContext exec) throws Exception
	{
		// access the input table
		BufferedDataTable inputTable = inData[0];

		// read the string from the input
		String data = null;
		if(inData[0] != null) {
			DataTableSpec inputSpec = inData[0].getSpec( );
			int strColIndex = inputSpec.findColumnIndex(strCol.getStringValue( ));
			if(strColIndex != -1) {
				for(DataRow row : inputTable) {
					data = ((StringValue)row.getCell(strColIndex)).getStringValue( );
					break;
				}
			}
		}
		if(data == null) {
			data = getAvailableFlowVariables( ).get(strVar.getStringValue( )).getStringValue( );
		}
		
		// create the table from the serialised string
		byte[ ] bytes = Base64.decodeBase64(data);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		BufferedDataTable table = exec.createBufferedDataTable(
				DataContainer.readFromStream(bais), 
				exec
		);
		return new BufferedDataTable[ ] { table };
	}
	
	@Override
	protected DataTableSpec[ ] configure(final DataTableSpec[ ] inSpec) 
			throws InvalidSettingsException
	{
		// cannot generate output spec as it is entirely dependent on the data table read from the
		// string
		return null;
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
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		strCol.saveSettingsTo(settings);
		strVar.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException
	{
		strCol.validateSettings(settings);
		strVar.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException
	{
		strCol.loadSettingsFrom(settings);
		strVar.loadSettingsFrom(settings);
	}

	@Override
	protected void reset( ) {
		// not required
	}

};
