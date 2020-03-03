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
package org.erlwood.knime.nodes.downloadfileswithauth;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.erlwood.knime.utils.FileTransferUpdateThread;
import org.erlwood.knime.utils.auth.AuthenticationUtils;
import org.erlwood.knime.utils.auth.SambaUtility;
import org.knime.base.data.append.column.AppendedColumnRow;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.StringCell;
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
import org.knime.core.util.FileUtil;

import com.hierynomus.smbj.auth.AuthenticationContext;

import ch.swaechter.smbjwrapper.SharedConnection;
import ch.swaechter.smbjwrapper.SharedDirectory;
import ch.swaechter.smbjwrapper.SharedFile;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Node to provide the functionality to download files from a network share to a
 * temporary directory on the local machine where the files can subsequently be
 * read from using the existing KNIME file readers.
 * 
 * @author Tom Wilkin
 */
public class DownloadFilesWithAuthNodeModel extends NodeModel {
	
	/** The logger instance. */
	private static final NodeLogger LOGGER = NodeLogger
			.getLogger(DownloadFilesWithAuthNodeModel.class);
	
	/** The base name for the temporary directory. */
	private static final String TEMP_DIR_BASENAME = "knime_download_files_with_auth_";
	
	/** The settings for the node configuration. */
	private DownloadFilesWithAuthSettings nodeSettings;

	/**
	 * The location of the temporary directory to store the files in after
	 * download.
	 */
	private File outDir;

	/**
	 * The network credentials to use to authenticate the user against the
	 * server.
	 */
	private AuthenticationContext credentials;
	
	/** A thread used to update the progress and check for cancellation while the
	 * download is underway. */
	private FileTransferUpdateThread thread;
	
	DownloadFilesWithAuthNodeModel() {
		super(1, 1);

		// initialise
		nodeSettings = new DownloadFilesWithAuthSettings( );
		outDir = null;
		credentials = null;
	}

	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		// create a new temporary directory
		if (nodeSettings.isOutputDirectoryEnabled()) {
			// use this path instead
			outDir = new File(nodeSettings.getOutputDirectory());
			if (!outDir.exists()) {
				outDir.mkdirs();
				LOGGER.debug("Created output directory '" + outDir.getAbsolutePath() + "'.");
			} else {
				LOGGER.debug("Using output directory '" + outDir.getAbsolutePath() + "'.");
			}
		} else {
			// create a temporary path
			do {
				outDir = computeFileName(UUID.randomUUID());
			} while (outDir.exists());
			outDir.mkdir();
			LOGGER.debug("Created temporary directory '" + outDir.getAbsolutePath() + "'.");
		}

		// get the credentials to use to access the files
		credentials = AuthenticationUtils.getAuthenticationContext(this.getAvailableFlowVariables(),
				getCredentialsProvider(), nodeSettings.getCredentialsName());

		// find the index for the URL column
		DataTableSpec inputSpec = inData[0].getSpec();
		int urlColIndex = inputSpec.findColumnIndex(nodeSettings.getUrlColumn());

		// start the progress update thread
		int rowCount = 1;
		if (inData[0] != null) {
			rowCount = inData[0].getRowCount();
		}
		double fraction = 1.0 / (double) rowCount;
		thread = new FileTransferUpdateThread(exec, fraction);
		thread.start();

		// iterate through the input data
		BufferedDataContainer container = exec.createDataContainer(createOutSpec(inputSpec));
		for (DataRow row : inData[0]) {
			// update the output
			String path;
			DataCell cell = row.getCell(urlColIndex);
			if (cell.getType().isCompatible(StringValue.class)) {
				path = ((StringValue) cell).getStringValue();
			} else {
				throw new InvalidSettingsException("Cannot convert path to string for row '" + row.getKey() + "'.");
			}
			exec.setProgress("Downloading file '" + path + "'.");
			exec.checkCanceled();
			thread.nextRow();

			// download the file from samba
			File out = null;
			try {
				out = download(path);
			} catch (SmbAuthException e) {
				if (credentials != null) {
					throw new InvalidSettingsException(
							"User '" + credentials.getUsername() + "' is not authorised to access file '" + path + "'.",
							e);
				}
				throw new InvalidSettingsException("User is not authorised to access" + " file '" + path + "'.", e);
			} catch (FileNotFoundException e) {
				throw new Exception("Cannot find file '" + path + "'.", e);
			} catch (MalformedURLException e) {
				throw new Exception("Cannot create URL to file '" + path + "'", e);
			} catch (IOException e) {
				throw new Exception("Cannot download file '" + path + "', do you have access?", e);
			}

			// output the path
			DataCell[] cells = new DataCell[2];
			cells[0] = new StringCell(out.getAbsolutePath());
			cells[1] = new StringCell(out.toURI().toURL().toString());
			AppendedColumnRow outRow = new AppendedColumnRow(row, cells);
			container.addRowToTable(outRow);
		}

		// create the output
		container.close();
		BufferedDataTable output = container.getTable();
		return new BufferedDataTable[] { output };
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		// check if the workflow credentials have been set
		AuthenticationUtils.checkRequiredCredentials(
				getAvailableFlowVariables( ), 
				getCredentialsProvider( ), 
				nodeSettings.getCredentialsName( )
		);
		
		// if the custom output is configured, ensure it is valid
		if(nodeSettings.isOutputDirectoryEnabled( )) {
			String outputDirectory = nodeSettings.getOutputDirectory( );
			if(outputDirectory == null || "".equals(outputDirectory)) {
				throw new InvalidSettingsException(
						"Custom output directory is enabled, but no path is set."
				);
			}
			
			if(SambaUtility.isUNC(outputDirectory)) {
				throw new InvalidSettingsException(
						"Custom output directory is on a remote server, please use a local path."
				);
			}
		}

		return new DataTableSpec[] { createOutSpec(inSpecs[0]) };
	}

	@Override
	protected void loadInternals(final File nodeInternDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		if (!nodeSettings.isOutputDirectoryEnabled( ) && nodeSettings.isDeleteTemp( )) {
			setWarningMessage("Did not restore content; consider re-executing!");
		}
	}

	@Override
	protected void saveInternals(final File nodeInternDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		nodeSettings.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		nodeSettings.loadSettingsFrom(settings);
		
		// check if the column has been set
		String urlColumnName = nodeSettings.getUrlColumn( );
		if (urlColumnName == null || "".equals(urlColumnName)) {
			throw new InvalidSettingsException(
					"The URL column has not been set.");
		}


		nodeSettings.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		nodeSettings.loadSettingsFrom(settings);
	}

	/** {@inheritDoc} */
	@Override
	protected void onDispose() {
		super.onDispose();
		reset();
	}

	@Override
	protected void reset() {
		// delete the temporary directory
		if (!nodeSettings.isOutputDirectoryEnabled( ) && outDir != null 
				&& nodeSettings.isDeleteTemp( )) 
		{
			if (FileUtil.deleteRecursively(outDir)) {
				LOGGER.debug("Deleted temporary directory '"
						+ outDir.getAbsolutePath() + "'");
			} else {
				StringBuilder message = new StringBuilder();
				message.append("Did not delete temporary directory '");
				message.append(outDir.getAbsolutePath());
				message.append("'");
				if (outDir.exists()) {
					message.append(".");
				} else {
					message.append(" as it does not exist.");
				}
				LOGGER.debug(message);
			}
		} else {
			LOGGER.debug("Did not delete output directory.");
		}
	}

	/**
	 * Create the output specification which specifies the output will contain
	 * the original input table plus one additional column containing the path
	 * to the local copy of the file.
	 * 
	 * @param inSpec
	 *            The original input table specification.
	 * @return The output table specification.
	 */
	private DataTableSpec createOutSpec(final DataTableSpec inSpec) {
		DataColumnSpec[ ] newColumnSpec = new DataColumnSpec[2];
		newColumnSpec[0] = new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(inSpec, "Local Copy Path"),
				StringCell.TYPE).createSpec();
		newColumnSpec[1] = new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(inSpec, "Local Copy URL"),
				StringCell.TYPE).createSpec();
		
		DataTableSpec outSpec = new DataTableSpec(newColumnSpec);
		return new DataTableSpec(inSpec, outSpec);
	}

	/**
	 * Adapted from Create Temp Dir Node. Generates a unique temporary directory
	 * name.
	 * 
	 * @param id
	 *            The unique ID generated for the directory name.
	 * @return The directory to create as a temporary directory.
	 */
	private File computeFileName(final UUID id) {
		return new File(new File(System.getProperty("java.io.tmpdir")),
				TEMP_DIR_BASENAME + id.toString());
	}
	
	/** Download the file/directory at the specified path.
	 * @param path The path to the file/directory to download.
	 * @return The downloaded file/directory on the local file system. */
	private File download(final String path) 
			throws Exception
	{
		SharedConnection sharedConnection = null;
		String inPath = SambaUtility.makeURL(path, false);
		if(SambaUtility.isSambaURL(inPath)) {
			try {
				SmbFile file = new SmbFile(inPath);
				
				if (credentials != null) {
					sharedConnection = new SharedConnection(file.getServer(), file.getShare(), credentials);
				} else {
					AuthenticationContext authenticationContext = AuthenticationContext.anonymous();
					sharedConnection = new SharedConnection(file.getServer(), file.getShare(), authenticationContext);
				}
				
				String parentFolder = SambaUtility.fixSambaPath(StringUtils.substringAfter(file.getPath(), file.getShare()));
				SharedDirectory sharedDirectory = new SharedDirectory(sharedConnection, parentFolder);
	
				// calculate the total file size
				thread.newFile(calculateSize(sharedDirectory, parentFolder,sharedConnection));
				
				if(sharedDirectory.isDirectory( )) {
					return downloadDirectory(sharedDirectory, outDir);
				} else {
					SharedFile sharedFile = new SharedFile(sharedConnection, parentFolder);
					return downloadFile(sharedFile, outDir);
				}
				
			} finally {
				if(sharedConnection != null) {
					sharedConnection.close();
				}
			}
		} else {
			throw new InvalidSettingsException("Download of local file '"
					+ path + "' is not supported.");
		}
	}
	
	/** Download the specified directory and content into the given out
	 * directory.
	 * @param path The path to the directory to download.
	 * @param outDir The out directory to download to.
	 * @return The directory downloaded on the local file system. */
	private File downloadDirectory(final SharedDirectory sharedDirectory, final File outDir) 
			throws InvalidSettingsException, IOException
	{
		// ensure the output directory exists
		if(!outDir.exists( )) {
			outDir.mkdirs( );
		}
		
		try {
			// create an empty directory if necessary
			if(sharedDirectory.getFiles().isEmpty()) {
				File outFile = new File(outDir.getAbsolutePath() 
						+ File.separator + sharedDirectory.getPath());
				outFile.mkdirs( );
			}
	        
			for (SharedDirectory dir : sharedDirectory.getDirectories()) {
				downloadDirectory(dir, outDir);
    		}
			
		    for (SharedFile sharedFile : sharedDirectory.getFiles()) {
		    	downloadFile(sharedFile, outDir);
		    }
			 
			return outDir;
		} catch (SmbAuthException e) {
			if(credentials != null) {
        		throw new InvalidSettingsException(
        				"User '" + credentials.getUsername() 
        				+ "' is not authorised to access share directory '" + sharedDirectory.getPath() + "'.", 
        				e
        		);
        	}
			throw new InvalidSettingsException(
					"User is not authorised to access share directory '" + sharedDirectory.getPath()
							+ "'.", e);
		} catch (SmbException e) {
			throw new InvalidSettingsException(
					"Could not access share directory '" + sharedDirectory.getPath() + "'.", e);
		}
	}
	
	/** Download the specified file and copy it to the specified out directory.
	 * @param path The path to the file to download
	 * @param outDir The directory to download the file to.
	 * @return The output file that was downloaded. */
	private File downloadFile(final SharedFile sharedFile, final File outDir) 
			throws InvalidSettingsException, IOException
	{
		InputStream in = null;
		OutputStream out = null;
		
		try {
			in = sharedFile.getInputStream();
			
			// open the output file and copy the file to it
			File outFile = new File(outDir.getAbsolutePath() + File.separator
					+ sharedFile.getPath());
			outFile.getParentFile().mkdirs();
			out = new BufferedOutputStream(new FileOutputStream(outFile));
			byte[] buffer = new byte[4096];
			int n;
			while ((n = in.read(buffer)) > 0) {
				thread.increment(n);
				out.write(buffer, 0, n);
			}

			return outFile;
		} catch (MalformedURLException e) {
			throw new InvalidSettingsException("Network share path '" + sharedFile.getPath()
					+ "' is not a valid location.", e);
		} catch (SmbAuthException e) {
			if(credentials != null) {
        		throw new InvalidSettingsException(
        				"User '" + credentials.getUsername() 
        				+ "' is not authorised to access share location '" + sharedFile.getPath() + "'.", 
        				e
        		);
        	}
			throw new InvalidSettingsException(
					"User is not authorised to access share location '" + sharedFile.getPath()
							+ "'.", e);
		} catch (SmbException e) {
			throw new InvalidSettingsException("Could not access share path '"
					+ sharedFile.getPath() + "'.", e);
		} finally {
			// close the streams
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				LOGGER.error(e);
			}

			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
	}
	
	/**
	 * Calculate the size of the given remote file (or recursively for a 
	 * directory).
	 * 
	 * @param file
	 *            The file/directory to get the size of.
	 * @return The size of the file/directory.
	 * @throws SmbException 
	 */
	private long calculateSize(final SharedDirectory sharedDirectory, String parentFolder,
			SharedConnection sharedConnection) throws Exception {
		long size = 0;

		if (sharedDirectory.isFile()) {
			SharedFile sharedFile = new SharedFile(sharedConnection, parentFolder);
			return sharedFile.getFileSize();
		} else {
			for (SharedFile file : sharedDirectory.getFiles()) {
				size += file.getFileSize();
			}
			for (SharedDirectory dir : sharedDirectory.getDirectories()) {
				size += calculateSize(dir, "", sharedConnection);
			}
		}

		return size;
	}

};
