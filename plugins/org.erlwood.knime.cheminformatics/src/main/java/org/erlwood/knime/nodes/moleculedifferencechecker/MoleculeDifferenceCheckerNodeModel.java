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
package org.erlwood.knime.nodes.moleculedifferencechecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jp.co.infocom.cheminfo.marvin.type.MrvCell;
import jp.co.infocom.cheminfo.marvin.type.MrvCellFactory;
import jp.co.infocom.cheminfo.marvin.type.MrvValue;

import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.MultiLineStringValueRenderer;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.tableview.TableContentModel;

import chemaxon.formats.MolFormatException;
import chemaxon.struc.Molecule;

/**
 * This is the model implementation of MoleculeDifferenceChecker.
 * 
 * @author Tom Wilkin 
*/
public class MoleculeDifferenceCheckerNodeModel extends NodeModel {

	/** The node logger instance. */
	private static final NodeLogger LOGGER = NodeLogger
			.getLogger(MoleculeDifferenceCheckerNodeModel.class);
	
	/** Whether to throw an exception and stop after the first error. */
	private final SettingsModelBoolean mFailOnFirstError =
			MoleculeDifferenceCheckerNodeDialog.createSettingsModelFailOnFirstError( );
	
	/** The content model for the differences view. */
	private TableContentModel tableContentModel;

	/** Constructor for the node model. */
	MoleculeDifferenceCheckerNodeModel() {
		super(2, 2);
		tableContentModel = new TableContentModel( );
	}
	
	// accessors	
	/** @return The TableContentModel for the view. */
	public TableContentModel getTableContentModel( ) { return tableContentModel; }

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		
		// create the view output
		BufferedDataContainer viewContainer = createViewContainer(exec);
		tableContentModel = new TableContentModel( );

		// load the input table
		BufferedDataTable resultInput = inData[0];
		DataTableSpec resultInputSpec = resultInput.getSpec();
		BufferedDataTable gtInput = inData[1];
		DataTableSpec gtInputSpec = gtInput.getSpec();

		// fail if the input tables are not the same size (rows
		if (resultInput.getRowCount() != gtInput.getRowCount()) {
			throw new InvalidSettingsException("Input tables do not have the "
					+ "same number of rows.");
		}
		
		// pass through if both input tables are empty
		if(resultInput.getRowCount( ) == 0 && gtInput.getRowCount( ) == 0) {
			return inData;
		}

		// decide which input columns are molecules
		Boolean[] columns = null;
		int confirmed = 0;
		rowLoop: {
		for (DataRow row : resultInput) {
			// iterate through the columns
			if(columns == null) {
				columns = new Boolean[row.getNumCells()];
			}
				
			for (int i = 0; i < row.getNumCells(); i++) {
				// update the output
				exec.checkCanceled();
				
				// check if this entry is blank, or already decided
				if(row.getCell(i).isMissing( ) || columns[i] != null) {
					continue;
				}

				// attempt to create a molecule
				try {
					if(convert(row.getCell(i)) == null) {
						// don't include the column
						columns[i] = false;
					} else {
						// include this column
						columns[i] = true;
						LOGGER.info("Including molecule column "
								+ resultInputSpec.getColumnNames()[i]);
					}
				} catch (Exception e) {
					// don't include the column
					columns[i] = false;
				}
				confirmed++;

				// check if we have a value for every column
				if(confirmed == row.getNumCells( )) {
					break rowLoop;
				}
			}
		}}

		// iterate through the rows
		Iterator<DataRow> resultIterator = resultInput.iterator();
		Iterator<DataRow> gtIterator = gtInput.iterator();
		DataRow resultRow, gtRow;
		Molecule result = null, gt = null;
		for (int i = 0; i < resultInput.getRowCount(); i++) {
			// get the next elements
			resultRow = (DataRow) resultIterator.next();
			gtRow = (DataRow) gtIterator.next();

			// update the output
			exec.checkCanceled();
			exec.setProgress(i / (float) resultInput.getRowCount(),
					"Processing result row '" + resultRow.getKey()
							+ "' and golden table row '" + gtRow.getKey()
							+ "'.");

			// iterate through the columns
			for (int j = 0; j < resultRow.getNumCells(); j++) {
				// check if this is a molecule column that we should compare
				if (columns[j] == null || !columns[j]) {
					continue;
				}

				// check if they're empty
				if (resultRow.getCell(j).isMissing()
						&& gtRow.getCell(j).isMissing()) {
					// these cells are fine as both molecules are missing
					continue;
				}
				if (resultRow.getCell(j).isMissing() != gtRow.getCell(j)
						.isMissing()) {
					// one is missing and the other isn't, that's an error
					if(mFailOnFirstError.getBooleanValue( )) {
						throw createException(resultRow, i, j,
								resultInputSpec);
					} else {
						addRowToView(viewContainer,
								resultRow.getKey( ),
								resultInputSpec.getColumnNames( )[i],
								resultRow.getCell(j), 
								gtRow.getCell(j)
						);
					}
				}

				// create the molecules
				try {
					result = convert(resultRow.getCell(j));
					gt = convert(gtRow.getCell(j));
				} catch (MolFormatException e) {
					// remove this column as it isn't a structure
					LOGGER.info("Ignoring non-molecule column "
							+ resultInputSpec.getColumnNames()[j]);
					columns[j] = false;
					continue;
				}

				// compare the molecules
				if (!result.isSimilarTo(gt)) {
					if(mFailOnFirstError.getBooleanValue( )) {
						throw createException(resultRow, i, j,
								resultInputSpec);
					} else {
						addRowToView(viewContainer,
								resultRow.getKey( ),
								resultInputSpec.getColumnNames( )[j],
								resultRow.getCell(j), 
								gtRow.getCell(j)
						);
					}
				}
			}
		}
		
		// create the output for the difference view
		viewContainer.close( );
		tableContentModel = new TableContentModel(viewContainer.getTable( ));

		// return the input table after removing the molecule columns
		ColumnRearranger[] rearranger = createColumnRearranger(resultInputSpec,
				gtInputSpec, columns);
		return new BufferedDataTable[] {
				exec.createColumnRearrangeTable(inData[0], rearranger[0], exec),
				exec.createColumnRearrangeTable(inData[1], rearranger[1], exec) 
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// not required
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		// fail if the input tables are not the same size (columns)
		if (inSpecs[0].getNumColumns() != inSpecs[1].getNumColumns()) {
			throw new InvalidSettingsException("Input tables do not have the "
					+ "same number of columns.");
		}

		// compare the columns
		for (int i = 0; i < inSpecs[0].getNumColumns(); i++) {
			if (!inSpecs[0].getColumnSpec(i).equalStructure(
					inSpecs[1].getColumnSpec(i))) {
				// these columns do not match
				throw new InvalidSettingsException("Input tables do not have "
						+ "matching columns. Column (InputPort1) " + "'"
						+ inSpecs[0].getColumnSpec(i).getName() + "' "
						+ "does not match with column (InputPort2) " + "'"
						+ inSpecs[1].getColumnSpec(i).getName() + "'.");
			}
		}

		// cannot identify remaining output columns until execution time
		return new DataTableSpec[] { null, null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		mFailOnFirstError.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			mFailOnFirstError.loadSettingsFrom(settings);
		} catch(InvalidSettingsException e) {
			// if the node does not contain the dialog, set it to the default
			mFailOnFirstError.setBooleanValue(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			mFailOnFirstError.validateSettings(settings);
		} catch(InvalidSettingsException e) {
			//
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// not required
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// not required
	}
	
	/**
	 * Create an Exception containing the error message that should be generated
	 * when a comparison has failed.
	 * 
	 * @param resultRow
	 *            The row for the result from the first input.
	 * @param rowIndex
	 *            The row index in the tables.
	 * @param colIndex
	 *            The column index in the tables.
	 * @param spec
	 *            One of the input specifications, just to get the name.
	 * @return The exception to throw containing the error message.
	 */
	private Exception createException(final DataRow resultRow,
			final int rowIndex, final int colIndex,	final DataTableSpec spec) {
		return new Exception("Cell content differs in row #" + rowIndex + "('"
				+ resultRow.getKey() + "') column #" + colIndex + "('"
				+ spec.getColumnNames()[colIndex] + "').");
	}

	/**
	 * Create the Column Rearranger which will remove the columns that have
	 * already been checked by this difference checker.
	 * 
	 * @param input1
	 *            The specification for the first result input column.
	 * @param input2
	 *            The specification for the second golden table column.
	 * @param columns
	 *            The array of columns that are or are not molecule columns.
	 * @return The array containing the Column Rearrangers for output one and
	 *         two respectively.
	 */
	private ColumnRearranger[] createColumnRearranger(
			final DataTableSpec input1, final DataTableSpec input2,
			final Boolean[] columns) {
		ColumnRearranger[] rearrangers = { new ColumnRearranger(input1),
				new ColumnRearranger(input2) };

		// create an array of the indices to remove
		List<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < columns.length; i++) {
			if (columns[i] != null && columns[i]) {
				indices.add(i);
			}
		}

		// remove these indices
		if(indices.size( ) > 0) {
			int[] toRemove = new int[indices.size()];
			for (int i = 0; i < indices.size(); i++) {
				toRemove[i] = indices.get(i);
			}
			rearrangers[0].remove(toRemove);
			rearrangers[1].remove(toRemove);
		}

		return rearrangers;
	}
	
	/** Convert the given DataCell to a Molecule if possible.
	 * @param cell The DataCell content to convert.
	 * @return The Molecule if the conversion was possible.
	 * @throws MolFormatException Thrown if the conversion was not possible. */
	private Molecule convert(final DataCell cell) throws MolFormatException {
		if(!cell.getType( ).equals(StringCell.TYPE) 
				&& MoleculeDataTypeConverter.isConvertible(cell.getType( ), MrvValue.class))
		{
			MrvValue value = MoleculeDataTypeConverter.getValue(cell, MrvValue.class);
			return value.getMolecule( );
		}
		
		return null;
	}
	
	/** Create a new container for the data that is to be shown on the view.
	 * @param exec The ExecutionContext to create the view table from.
	 * @return The container for the view data table. */
	private BufferedDataContainer createViewContainer(final ExecutionContext exec) {
		// create the table
		DataColumnSpec[ ] columnSpec = new DataColumnSpec[6];
		columnSpec[0] = new DataColumnSpecCreator("Original Row Id", StringCell.TYPE).createSpec( );		
		columnSpec[1] = new DataColumnSpecCreator("Column Name", StringCell.TYPE).createSpec( );
		columnSpec[2] = new DataColumnSpecCreator("Port 1 Render", MrvCell.TYPE).createSpec( );
		columnSpec[3] = new DataColumnSpecCreator("Port 2 Render", MrvCell.TYPE).createSpec( );
		
		DataColumnSpecCreator port1 = new DataColumnSpecCreator("Port 1 Value", StringCell.TYPE); 
		port1.setProperties(new DataColumnProperties(Collections.singletonMap(DataValueRenderer.PROPERTY_PREFERRED_RENDERER,
		                                                                      new MultiLineStringValueRenderer.Factory().getDescription())));
		
		DataColumnSpecCreator port2 = new DataColumnSpecCreator("Port 2 Value", StringCell.TYPE);
		port2.setProperties(new DataColumnProperties(Collections.singletonMap(DataValueRenderer.PROPERTY_PREFERRED_RENDERER,
		                                                                      new MultiLineStringValueRenderer.Factory().getDescription())));
		
		columnSpec[4] = port1.createSpec();
		columnSpec[5] = port2.createSpec();
		
		DataTableSpec spec = new DataTableSpec(columnSpec);
		BufferedDataContainer container = exec.createDataContainer(spec);
		return container;
	}

	/** Add a row with differences to the view.
	 * @param viewContainer The container to add the view data to.
	 * @param key The key for the row the differences occur in.
	 * @param columnName The name of the column the differences occur in.
	 * @param cell1 The cell in input port 1.
	 * @param cell2 The cell in input port 2. */
	private void addRowToView(final BufferedDataContainer viewContainer, final RowKey key, 
			final String columnName, final DataCell cell1, final DataCell cell2)
	{
		DataCell[ ] cells = new DataCell[6];
		cells[0] = new StringCell(key.getString( ));
		cells[1] = new StringCell(columnName);
		cells[2] = MrvCellFactory.create(MoleculeDataTypeConverter.getValue(cell1, MrvValue.class).getMolecule( ));
		cells[3] = MrvCellFactory.create(MoleculeDataTypeConverter.getValue(cell2, MrvValue.class).getMolecule( ));
		cells[4] = new StringCell(cell1.toString( ));
		cells[5] = new StringCell(cell2.toString( ));
		RowKey newKey = new RowKey(key.getString( ) + " " + columnName);
		DefaultRow row = new DefaultRow(newKey, cells);
		viewContainer.addRowToTable(row);
	}

};
