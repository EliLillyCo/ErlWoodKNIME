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
 * 
 * Extended from KNIME implementation of 'List Files' node to include
 * listing of files stored on samba based network shares.
 * 
 * The KNIME license for this content is as follows:
 *  ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
*/
package org.erlwood.knime.nodes.listfileswithauth;

import java.io.File;
import java.io.IOException;

import org.erlwood.knime.utils.auth.AuthenticationUtils;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.hierynomus.smbj.auth.AuthenticationContext;

import jcifs.smb.NtlmPasswordAuthentication;


/**
 * This is the model implementation of List Files.
 * 
 * 
 * @author Peter
 * @author Samba Component - Tom Wilkin
 */
public class ListFilesWithAuthNodeModel extends NodeModel {

	private ListFilesWithAuthSettings mSettings;

	/**
	 * Constructor for the node model.
	 */
	protected ListFilesWithAuthNodeModel() {
		super(0, 1);
		mSettings = new ListFilesWithAuthSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		AuthenticationContext creds = AuthenticationUtils.getAuthenticationContext(
				this.getAvailableFlowVariables(org.knime.core.node.workflow.VariableType.StringType.INSTANCE, org.knime.core.node.workflow.VariableType.DoubleType.INSTANCE, org.knime.core.node.workflow.VariableType.IntType.INSTANCE),
				getCredentialsProvider( ),
				mSettings.getCredentialsName( )
		);
		ListFilesWithAuth lister = new ListFilesWithAuth(mSettings,creds);
		BufferedDataTable table = lister.search(exec);
		if(lister.hasErrors()) {
			setWarningMessage("Some or all rows returned error messages, see table for details.");
		}
		return new BufferedDataTable[] { table };
	}

	/** {@inheritDoc} */
	@Override
	protected void reset() {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		// check if the workflow credentials have been set
		AuthenticationUtils.checkRequiredCredentials(
				getAvailableFlowVariables( ), 
				getCredentialsProvider( ), 
				mSettings.getCredentialsName( )
		);
		
		AuthenticationContext creds = AuthenticationUtils.getAuthenticationContext(this.getAvailableFlowVariables(org.knime.core.node.workflow.VariableType.StringType.INSTANCE, org.knime.core.node.workflow.VariableType.DoubleType.INSTANCE, org.knime.core.node.workflow.VariableType.IntType.INSTANCE),
				getCredentialsProvider(), mSettings.getCredentialsName( ));
		if (mSettings == null) {
			throw new InvalidSettingsException("No configuration available.");
		}
		// check valid config
		mSettings.getDirectoriesFromLocationString(creds);
		return new DataTableSpec[] { ListFilesWithAuth.SPEC };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		if (mSettings != null) {
			mSettings.saveSettingsTo(settings);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		ListFilesWithAuthSettings lsSettings = new ListFilesWithAuthSettings();
		lsSettings.loadSettingsInModel(settings);
		mSettings = lsSettings;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		new ListFilesWithAuthSettings().loadSettingsInModel(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// nothing to load
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// no op
	}

}
