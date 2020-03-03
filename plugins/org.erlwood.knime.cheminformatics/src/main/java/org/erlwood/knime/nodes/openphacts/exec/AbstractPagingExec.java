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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import org.erlwood.knime.utils.clients.RESTWebServiceClient.ParameterType;
import org.erlwood.knime.utils.clients.RESTWebServiceClient.RESTParameter;
import org.knime.core.data.DataRow;
import org.knime.core.node.ExecutionContext;

/**
 * Execution class used to handle paging.
 * @author Luke Bullard
 *
 */
public abstract class AbstractPagingExec extends AbstractExec {

	/** 
	 * @return The count method.
	 */
	protected abstract String getCountMethod();
	
	/**
	 * {@inheritDoc}
	 */
	protected List<byte[]> executeCall(ExecutionContext exec, DataRow row, String format) throws Exception {
		List<RESTParameter<?>> params = getParams(row);

		params.add(0, new RESTParameter<String>("app_id", getAppId()));
		params.add(1, new RESTParameter<String>("app_key", getAppKey()));
		params.add(2, new RESTParameter<String>("_format", format));	
		
		int totalSize = getCount(exec, params);
		if (totalSize == 0) {
			return Collections.emptyList();
		}
						
		int iterations = totalSize / 500;
		
		ExecutorService pool = Executors.newFixedThreadPool(Math.min(5, Math.max(iterations, 1)));
		
		List<Callable<byte[]>> tasks = new ArrayList<Callable<byte[]>>();
		
		for (int i = 0; i <= iterations; i++) {
			final List<RESTParameter<?>> taskParams = new ArrayList<RESTParameter<?>>(params);			
			taskParams.add(new RESTParameter<String>("_page", Integer.toString(i + 1)));
			taskParams.add(new RESTParameter<String>("_pageSize", "500"));
			
			tasks.add(new Callable<byte[]>() {
				
				@Override
				public byte[] call() throws Exception {
					exec.checkCanceled();
					return executeCall(exec, taskParams);
				}
			});
		}
		
				
		List<Future<byte[]>> results = pool.invokeAll(tasks);
		pool.shutdown();
		
		List<byte[]> retVal = new ArrayList<byte[]>();		
		for (Future<byte[]> f : results) {
			retVal.add(f.get());			
		}
		return retVal;
	}


	/**
	 * Gets the number of items for this paging call.
	 * @param exec The Execution Context
	 * @param params The params to use.
	 * @return The number of items available for this page call.
	 * @throws Exception On Error
	 */
	private int getCount(ExecutionContext exec, List<RESTParameter<?>> params) throws Exception {
		final List<RESTParameter<?>> taskParams = new ArrayList<RESTParameter<?>>(params);			

		taskParams.add(new RESTParameter<String>("_pageSize", "all"));
		
		
		//	Remove the format argument.
		taskParams.removeIf(new Predicate<RESTParameter<?>>() {

			@Override
			public boolean test(RESTParameter<?> arg) {
				return arg.getName().equalsIgnoreCase("_format");
			}
		});
		
		//	Ensure we are using TSV for this call.
		taskParams.add(new RESTParameter<String>("_format", "tsv"));
		
		InputStream is = null;
		try {
			is = getClient().invokeGet(exec, getCountMethod(), ParameterType.QUERY, (Object[]) taskParams.toArray(new RESTParameter<?>[0]));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len = -1;
			byte[] buffer = new byte[2048];
			while ((len = is.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}

			baos.close();
			String s = new String(baos.toByteArray());
			
			String[] split = s.split(":");			
			return Integer.parseInt(split[split.length - 1].trim());
			
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
}
