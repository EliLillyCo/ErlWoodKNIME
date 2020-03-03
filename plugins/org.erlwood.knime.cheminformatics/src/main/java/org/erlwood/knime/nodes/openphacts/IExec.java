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
package org.erlwood.knime.nodes.openphacts;

import java.util.Map;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.FlowVariable;

import org.erlwood.knime.utils.settings.AbstractSettings;
import org.erlwood.knime.utils.settings.WebServiceSettings;
/**
 * Interface used for executing OpenPHACTS calls.
 * @author Luke Bullard
 *
 * @param <T> Deriviation of AbstractSettings
 */
@SuppressWarnings("rawtypes")
public interface IExec<T extends AbstractSettings>  {
	/**
	 * Sets up the execution.
	 * @param availableFlowVariables The flow variables
	 * @param credentialsProvider The credentials provider
	 * @param wsSettings The settings
	 * @param input The input table
	 * @param settings The AbstractSettings
	 */
	void setup(Map<String, FlowVariable> availableFlowVariables,
			CredentialsProvider credentialsProvider,
			WebServiceSettings wsSettings,
			BufferedDataTable input, T settings);
	
	/**
	 * Execute the OpenPhacts call.
	 * 
	 * @param input The input data
	 * @param exec  The ExecutionContext
	 * @return BufferedDataTable 
	 * @throws Exception On Error
	 */
	BufferedDataTable[] executeCall(final BufferedDataTable input, final ExecutionContext exec) throws Exception;
	
	/** 
	 * @return Any warning messages.
	 */
	String getWarningMessage();
	
}
