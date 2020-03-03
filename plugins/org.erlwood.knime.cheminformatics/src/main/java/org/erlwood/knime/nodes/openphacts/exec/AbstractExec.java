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
package org.erlwood.knime.nodes.openphacts.exec;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.erlwood.knime.nodes.openphacts.IExec;
import org.erlwood.knime.nodes.openphacts.OpenPhactsNodeModel.eFormat;
import org.erlwood.knime.nodes.openphacts.settings.OpenPhactsSettings;
import org.erlwood.knime.utils.clients.RESTWebServiceClient;
import org.erlwood.knime.utils.clients.RESTWebServiceClient.ParameterType;
import org.erlwood.knime.utils.clients.RESTWebServiceClient.RESTParameter;
import org.erlwood.knime.utils.exceptions.NotFoundException;
import org.erlwood.knime.utils.exceptions.WebServiceException;
import org.erlwood.knime.utils.iotiming.IOTiming;
import org.erlwood.knime.utils.settings.WebServiceSettings;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellFactory;
import org.knime.core.data.DataCellFactory.FromString;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.json.JSONCell;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.FlowVariable;

/**
 * Abstract base class for the OpenPHACTS executions.
 * @author Luke Bullard
 *
 */
public abstract class AbstractExec implements IExec<OpenPhactsSettings>, IOTiming {
	/** Logger. **/
	private static final NodeLogger LOG = NodeLogger.getLogger(AbstractExec.class);
	
	/** The TLS client. **/
	private ThreadLocal<RESTWebServiceClient> client = new ThreadLocal<RESTWebServiceClient>();

	/**
	 * The timing value.
	 */
	private AtomicLong timing = new AtomicLong();

	/** 
	 * @return The API method to use.
	 */
	protected abstract String getMethod();

	/**
	 * Returns a list of parameters given a DataRow. 
	 * @param row The DataRow to use.
	 * @return The list of parameters.
	 */
	protected abstract List<RESTParameter<?>> getParams(DataRow row);

	/**
	 * Initialises the Execution object.
	 * @param input The table of input data.
	 * @param settings The settings.
	 */
	protected abstract void initialiseSettings(BufferedDataTable input, OpenPhactsSettings settings);

	private Map<String, FlowVariable> availableFlowVariables;

	private CredentialsProvider credentialsProvider;

	private WebServiceSettings wsSettings;

	private String appId;

	private String appKey;

	private eFormat format;

	private boolean append;

	private String warningMessage;

	protected static enum DataType {
		String(1), Integer(2), Double(4);

		private final int bitwisePos;

		private DataType(int bwp) {
			bitwisePos = bwp;
		}

		public int getBitwisePos() {
			return bitwisePos;
		}

	}

	/** 
	 * @return Gets a REST web service client.
	 * @throws WebServiceException On Error
	 */
	protected RESTWebServiceClient getClient() throws WebServiceException {
		RESTWebServiceClient retVal = client.get();
		if (retVal == null) {
			retVal = RESTWebServiceClient.createClient(this, availableFlowVariables, credentialsProvider, wsSettings);
			retVal.setContentType("application/json");
			client.set(retVal);
		}
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup(Map<String, FlowVariable> availableFlowVariables, CredentialsProvider credentialsProvider,
			WebServiceSettings wsSettings, BufferedDataTable input, OpenPhactsSettings settings) {
		this.availableFlowVariables = availableFlowVariables;
		this.credentialsProvider = credentialsProvider;
		this.wsSettings = wsSettings;

		this.appId = settings.getApplicationIdSetting().getValue();
		this.appKey = settings.getApplicationKeySetting().getValue();
		this.format = eFormat.valueOf(settings.getReturnFormatSetting().getValue());
		
		this.append = settings.isAppend();
		initialiseSettings(input, settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedDataTable[] executeCall(final BufferedDataTable input, final ExecutionContext exec)
			throws Exception {

		// Do we have an input table ?
		// If not then simply execute the call.
		if (input == null) {
			return execRow(exec, null, null, 0);
		}

		ExecutorService executor = Executors.newFixedThreadPool(20);
		
		// Ok, we have an input so use it..
		BufferedDataTable[] outputs = new BufferedDataTable[(int) input.size()];
		BufferedDataTable[] rawOutputs = new BufferedDataTable[(int) input.size()];

		List<Callable<BufferedDataTable[]>> tasks = new ArrayList<Callable<BufferedDataTable[]>>();

		final ExecutionMonitor em = exec.createSubProgress(1);

		int count = 0;
		final AtomicInteger progress = new AtomicInteger();

		for (final DataRow row : input) {
			final int callNumber = count++;
			exec.checkCanceled();
			tasks.add(new Callable<BufferedDataTable[]>() {

				@Override
				public BufferedDataTable[] call() throws Exception {
					BufferedDataTable[] bt = execRow(exec, input.getDataTableSpec(), row, callNumber);
					em.setProgress((double) progress.getAndIncrement() / (double) input.size());
					return bt;
				}

			});
		}
		List<Future<BufferedDataTable[]>> results = executor.invokeAll(tasks);

		count = 0;
		for (Future<BufferedDataTable[]> f : results) {
			BufferedDataTable[] r = f.get();
			outputs[count] = r[0];
			rawOutputs[count] = r[1];
			count++;
		}

		return new BufferedDataTable[] { 
				exec.createConcatenateTable(exec, outputs),
				format.equals(eFormat.NONE) ? null : exec.createConcatenateTable(exec, rawOutputs) };
	}
	

	/**
	 * Creates a container for the raw data.
	 * @param exec The ExecutionContext
	 * @param dataTableSpec The spec of the input table (may be null)
	 * @param row The DataRow in question
	 * @param callNumber The number of the interation on the input table
	 * @param dt The DataType 
	 * @param cells The cells to add.
	 * @return A valid BufferedDataContainer
	 * @throws Exception On Error
	 */
	private BufferedDataContainer createRawContainer(ExecutionContext exec, 
													 DataTableSpec dataTableSpec, 
													 DataRow row, 
													 int callNumber, 
													 org.knime.core.data.DataType dt, 
													 List<DataCell> cells)
			throws Exception {
		
		DataTableSpecCreator dtsc = null;
		
		if (dataTableSpec != null && append) {
			dtsc = new DataTableSpecCreator(dataTableSpec);
		} else {
			dtsc = new DataTableSpecCreator();
		}
		dtsc.addColumns(new DataColumnSpecCreator("OPS_Output", dt).createSpec());
		
		BufferedDataContainer rawOutputContainer = exec.createDataContainer(dtsc.createSpec());
		
		int innerCount = 0;
		for (DataCell newCell : cells) {
			List<DataCell> rawCells = new ArrayList<DataCell>();
			
			if (dataTableSpec != null && append) {
				for (DataCell oldCell : row) {
					rawCells.add(oldCell);
				}
			} 
			rawCells.add(newCell); 
			DataRow rawRow = new DefaultRow(new RowKey("Row_" + callNumber + "_" + innerCount), rawCells);
			rawOutputContainer.addRowToTable(rawRow);
			innerCount++;
		}
		
		rawOutputContainer.close();
		return rawOutputContainer;
	}

	/**
	 * Executes the OpenPHACTS call with the given parameters.
	 * @param exec The ExecutionContext
	 * @param params The parameters
	 * @return A byte array
	 * @throws Exception On Error
	 */
	protected byte[] executeCall(ExecutionContext exec, List<RESTParameter<?>> params) throws Exception {
		
		InputStream is = null;
		try {
			is = getClient().invokeGet(exec, getMethod(), ParameterType.QUERY, (Object[]) params.toArray(new RESTParameter<?>[0]));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len = -1;
			byte[] buffer = new byte[2048];
			while ((len = is.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}

			baos.close();
			return baos.toByteArray();			
		} catch(Throwable ex) {
			LOG.error(ex.getMessage(), ex);
			throw ex;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
	
	/**
	 * Executes the OpenPHACTS call with the input row and format
	 * @param exec The ExecutionContext
	 * @param row The DataRow
	 * @param format The format to use.
	 * @return A list of byte arrays
	 * @throws Exception On Error
	 */
	protected List<byte[]> executeCall(ExecutionContext exec, DataRow row, String format) throws Exception {
		List<RESTParameter<?>> params = getParams(row);

		params.add(0, new RESTParameter<String>("app_id", appId));
		params.add(1, new RESTParameter<String>("app_key", appKey));
		params.add(2, new RESTParameter<String>("_format", format));
		
		List<byte[]> retVal = new ArrayList<byte[]>();
		try {
			retVal.add(executeCall(exec, params));
		} catch (NotFoundException nfe) {
			//	Swallow 404, so we get empty output table.	
		}
		return retVal;
	}
	
	
	/**
	 * Execute the DataRow.
	 * @param input	The input data
	 * @param exec	The ExecutionContext
	 * @param dataTableSpec 
	 * @return BufferedDataTable
	 * @throws Exception On Error
	 */
	private BufferedDataTable[] execRow(ExecutionContext exec, DataTableSpec dataTableSpec, DataRow row, int callNumber) throws Exception {

		Map<Integer, Integer> determinedTypes = new HashMap<Integer, Integer>();
		String[] columnNames = null;
		String[] columnDataTypes = null;

		exec.checkCanceled();
		
		ExecutorService pool = Executors.newFixedThreadPool(2);
		
		Future<List<byte[]>> primaryFuture = pool.submit(new Callable<List<byte[]>>() {

			@Override
			public List<byte[]> call() throws Exception {
				return executeCall(exec, row, "tsv");
			}
			
		});
		
				
		BufferedDataContainer rawOutputContainer = null;

		final String secondaryFormat;
		org.knime.core.data.DataType dt = null;
		
		switch(format) {
			case JSON:
				secondaryFormat = eFormat.JSON.name().toLowerCase();
				dt = JSONCell.TYPE;				
				break;
			case XML:	
				secondaryFormat = eFormat.XML.name().toLowerCase();
				dt = XMLCell.TYPE;	
				break;	
			default:
				secondaryFormat  = null;
				break;
					
		}
		
		if (secondaryFormat != null) {
			Future<List<byte[]>> secondaryFuture = pool.submit(new Callable<List<byte[]>>() {

				@Override
				public List<byte[]> call() throws Exception {
					return executeCall(exec, row, secondaryFormat);
				}
				
			});
			Optional<DataCellFactory> dcf = dt.getCellFactory(exec);
			
			List<DataCell> cells = new ArrayList<DataCell>();
			for (byte[] s : secondaryFuture.get()) {
				cells.add(((FromString)dcf.get()).createCell(new String(s)));
			}
		
			rawOutputContainer = createRawContainer(exec, dataTableSpec, row, callNumber, dt, cells);
		}
		pool.shutdown();
		
		List<List<Object>> extractedRows = new ArrayList<List<Object>>();
		
		StringBuilder dataBuffer = new StringBuilder();
		for (byte[] s : primaryFuture.get()) {
			dataBuffer.append(new String(s));
		}
		
		if (dataBuffer.length() > 0) {
			String[] dataLines = dataBuffer.toString().split("\n");
			
			// Loop on the data
			for (String m : dataLines) {
				String[] columns = m.split("\\t");
				
				//	Grab the headers
				if (columnNames == null) {
					columnNames = columns;
					columnDataTypes = new String[columnNames.length];
					Arrays.fill(columnDataTypes, DataType.String.name());
					continue;
				}
				
				// Fill up the current row with nulls
				List<Object> currentRow = new ArrayList<Object>(columnNames.length);
				for (int i = 0; i < columnNames.length; i++) {
					currentRow.add(null);
				}
	
				int index = 0;
				for (String cell : columns) {
					
					Integer val = determinedTypes.get(index);
					if (val == null) {
						val = 0;
					}
					val = val | calculateDataType(cell);
					determinedTypes.put(index, val);
	
					currentRow.set(index, cell);
					index++;
				}
				extractedRows.add(currentRow);
			}
		}
		
		// Iterate through the data, changing anything that needs changing..
		for (Entry<Integer, Integer> es : determinedTypes.entrySet()) {
			//Integer colIndex = columnNameMap.get(es.getKey());

			int typeCheck = es.getValue();
			// If we can use String then do nothing
			if ((typeCheck & DataType.String.getBitwisePos()) == DataType.String.getBitwisePos()) {
				continue;
			}
			// If we can use Double then convert
			if ((typeCheck & DataType.Double.getBitwisePos()) == DataType.Double.getBitwisePos()) {
				convertColumn(columnDataTypes, extractedRows, es.getValue(), DataType.Double);
				continue;
			}
			// If we can use Integer then convert
			if ((typeCheck & DataType.Integer.getBitwisePos()) == DataType.Integer.getBitwisePos()) {
				convertColumn(columnDataTypes, extractedRows, es.getValue(), DataType.Integer);
				continue;
			}

		}
		// Now create the BufferedDataTable
		DataTableSpec dts = getSpec(dataTableSpec, columnNames, columnDataTypes);

		BufferedDataContainer c = exec.createDataContainer(dts);

		long rowIndex = 0;
		for (List<Object> cr : extractedRows) {
			List<DataCell> cells = new ArrayList<DataCell>();
			
			if (dataTableSpec != null && append) {
				for (DataCell oldCell : row) {
					cells.add(oldCell);
				}
			}
			for (Object o : cr) {
				if (o == null) {
					cells.add(org.knime.core.data.DataType.getMissingCell());
					continue;
				}
				if (o instanceof Integer) {
					cells.add(new IntCell((Integer) o));
					continue;
				}
				if (o instanceof Double) {
					cells.add(new DoubleCell((Double) o));
					continue;
				}
				cells.add(new StringCell(o.toString()));
			}
			c.addRowToTable(new DefaultRow(new RowKey("Row_" + callNumber + "_" + rowIndex++), cells));
		}

		c.close();
		
		return new BufferedDataTable[] { c.getTable(), rawOutputContainer == null ? null : rawOutputContainer.getTable() };
		
	}

	/**
	 * Gets the DataTableSpec for a new table
	 * @param dataTableSpec The original spec to take account of if appending
	 * @param columnNames The column names of the new spec
	 * @param columnDataTypes The column data typess of the new spec
	 * @return A valid DataTableSpec
	 */
	private DataTableSpec getSpec(DataTableSpec dataTableSpec, String[] columnNames, String[] columnDataTypes) {
		DataTableSpecCreator dtsc = null;
		
		if (dataTableSpec != null && append) {
			dtsc = new DataTableSpecCreator(dataTableSpec);			
		} else {
			dtsc = new DataTableSpecCreator();
		}
		if (columnNames != null) {
			
			DataColumnSpec[] newColumnSpec = new DataColumnSpec[columnNames.length];
			for (int i = 0; i < columnNames.length; i++) {
				// Specify the name
				String name = columnNames[i];
	
				if (dataTableSpec != null && append) {
					name = DataTableSpec.getUniqueColumnName(dataTableSpec, name);
				}
	
				// Specify the type
				org.knime.core.data.DataType type = null;
	
				if (type == null) {
					if (columnDataTypes[i].equalsIgnoreCase("integer")) {
						type = IntCell.TYPE;
					} else if (columnDataTypes[i].equalsIgnoreCase("double")) {
						type = DoubleCell.TYPE;
					} else {
						type = StringCell.TYPE;
					}
				}
	
				// create the column spec
				newColumnSpec[i] = new DataColumnSpecCreator(name, type).createSpec();
			}
			
			
			dtsc.addColumns(newColumnSpec);
		}
		
		return dtsc.createSpec();
	}

	private void convertColumn(String[] columnDataTypes, List<List<Object>> rows, int colIndex, DataType newType) {
		columnDataTypes[colIndex] = newType.name();

		for (List<Object> row : rows) {
			Object obj = row.get(colIndex);
			if (obj == null) {
				continue;
			}
			String s = (String) row.get(colIndex);

			switch (newType) {
			case Double:
				row.set(colIndex, Double.parseDouble(s));
				break;
			case Integer:
				row.set(colIndex, Integer.parseInt(s));
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Works out the DataType for a given String. Only works for Double &
	 * Integer
	 * 
	 * @param s
	 *            The String data to check
	 * @return The correct DataType for the data
	 */
	private int calculateDataType(String s) {

		try {
			Integer.parseInt(s);
			return DataType.Integer.getBitwisePos();
		} catch (NumberFormatException nfe) {
			// Can't do an int, so try a double
			try {
				Double.parseDouble(s);
				return DataType.Double.getBitwisePos();
			} catch (NumberFormatException nfe2) {
				// Can't do a double either...
			}
		}
		return DataType.String.getBitwisePos();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTiming(long time) {
		this.timing.set(time);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTiming() {
		return timing.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addTiming(long t) {
		timing.addAndGet(t);
	}

	public String getAppId() {
		return appId;
	}

	public String getAppKey() {
		return appKey;
	}
	
	protected void setWarningMesssage(String s) {
		warningMessage = s;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getWarningMessage() {
		return warningMessage;
	}
}
