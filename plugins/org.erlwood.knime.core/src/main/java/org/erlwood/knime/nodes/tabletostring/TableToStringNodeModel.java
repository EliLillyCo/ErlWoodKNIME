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
package org.erlwood.knime.nodes.tabletostring;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/** Node which produces a string format for an entire KNIME table which can be used for storage
 * and transfer.
 * @author Tom Wilkin */
public class TableToStringNodeModel extends NodeModel {

	protected TableToStringNodeModel( ) {
		super(1, 1);
	}
	
	@Override
	protected BufferedDataTable[ ] execute(
			final BufferedDataTable[ ] inData, final ExecutionContext exec) throws Exception
	{
		// access the input table
		BufferedDataTable inputTable = inData[0];
		
		// read the table into the stream
		ByteArrayOutputStream baos = new ByteArrayOutputStream( );
		DataContainer.writeToStream(inputTable, baos, exec);
		
		// create the string
		String str = Base64.encodeBase64String(baos.toByteArray( ));
		str = str.replaceAll("\n|\r", "");
		
		// create the output table
		DataTableSpec outSpec = createOutSpec( );
		BufferedDataContainer container = exec.createDataContainer(outSpec);
		DataCell[ ] cells = new DataCell[1];
		cells[0] = new StringCell(str);
		DataRow row = new DefaultRow(new RowKey("Table"), cells);
		container.addRowToTable(row);
		container.close( );
		return new BufferedDataTable[ ] { container.getTable( ) };	
	}
	
	@Override
	protected DataTableSpec[ ] configure(final DataTableSpec[ ] inSpecs) 
			throws InvalidSettingsException
	{
		return new DataTableSpec[ ] { createOutSpec( ) };
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
		// not required
	}

	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException
	{
		// not required
	}

	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException
	{
		// not required
	}

	@Override
	protected void reset( ) {
		// not required
	}
	
	/** Create the output table specification.
	 * @return The output table specification. */
	private DataTableSpec createOutSpec( ) {
		DataColumnSpec[ ] newColumnSpec = new DataColumnSpec[1];
		newColumnSpec[0] = new DataColumnSpecCreator(
				"String",
				StringCell.TYPE
		).createSpec( );
		DataTableSpec outSpec = new DataTableSpec(newColumnSpec);
		return outSpec;
	}

};
