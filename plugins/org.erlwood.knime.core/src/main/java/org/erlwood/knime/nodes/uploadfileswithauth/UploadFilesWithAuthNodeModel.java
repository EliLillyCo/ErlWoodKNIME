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
package org.erlwood.knime.nodes.uploadfileswithauth;

import java.io.BufferedInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

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
import org.knime.core.data.def.DefaultRow;
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
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.util.pathresolve.ResolverUtil;

import com.hierynomus.smbj.auth.AuthenticationContext;

import ch.swaechter.smbjwrapper.SharedConnection;
import ch.swaechter.smbjwrapper.SharedDirectory;
import ch.swaechter.smbjwrapper.SharedFile;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Node to provide the functionality to upload files written by other KNIME
 * writers to a network share from a local/temporary directory using
 * authentication.
 * 
 * @author Tom Wilkin
 */
class UploadFilesWithAuthNodeModel extends NodeModel {
    private static final NodeLogger    LOG       = NodeLogger.getLogger(UploadFilesWithAuthNodeModel.class);

    /** The settings for this node configuration. */
    private UploadFilesWithAuthSettings nodeSettings;

    /**
     * The network credentials to use to authenticate the user against the
     * server.
     */
    private AuthenticationContext credentials;

    /** The thread to monitor the progress of the file transfer. */
    private FileTransferUpdateThread   thread;

    UploadFilesWithAuthNodeModel() {
        super(new PortType[] { BufferedDataTable.TYPE_OPTIONAL }, new PortType[] { BufferedDataTable.TYPE });
        
        nodeSettings = new UploadFilesWithAuthSettings( );
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {
    	
        // get the credentials to use to access the share
    	credentials = AuthenticationUtils.getAuthenticationContext(
        		this.getAvailableFlowVariables(org.knime.core.node.workflow.VariableType.StringType.INSTANCE, org.knime.core.node.workflow.VariableType.DoubleType.INSTANCE, org.knime.core.node.workflow.VariableType.IntType.INSTANCE), 
        		getCredentialsProvider( ),
        		nodeSettings.getCredentialsName( )
        );

        // create the file to the output directory
        String outDir = nodeSettings.getSharePath( );

        // initialise the output container
        DataTableSpec inputSpec = null;
        if (inData[0] != null) {
            inputSpec = inData[0].getSpec();
        }
        BufferedDataContainer container = exec.createDataContainer(createOutSpec(inputSpec));

        // start the progress update thread
        int rowCount = 1;
        if (inData[0] != null) {
            rowCount = inData[0].getRowCount();
        }
        double fraction = 1.0 / (double) rowCount;
        thread = new FileTransferUpdateThread(exec, fraction);
        thread.start();

        // upload the files
        try {
            if (nodeSettings.getPathUseColumn( )) {
            	// check for no input
            	if(inputSpec == null) {
            		throw new InvalidSettingsException(
            				"Cannot use column when node has no input table."
            		);
            	}
            	
                // input table
                int pathColIndex = inputSpec.findColumnIndex(nodeSettings.getPathColumn( ));

                // iterate through the input data
                for (DataRow row : inData[0]) {
                    // update
                    exec.checkCanceled();
                    exec.setProgress("Uploading file for row " + row.getKey() + ".");
                    thread.nextRow();

                    // upload the file
                    String path = row.getCell(pathColIndex).toString();
                    String uploadedFile = upload(path, outDir);

                    // output the path
                    addRow(container, row, uploadedFile);
                }
            } else {
                // flow variable
                // update
                exec.checkCanceled();
                exec.setProgress("Uploading file for variable " + nodeSettings.getPathVariable( ) + ".");
                thread.nextRow();

                // upload
                String path = getAvailableFlowVariables(org.knime.core.node.workflow.VariableType.StringType.INSTANCE, org.knime.core.node.workflow.VariableType.DoubleType.INSTANCE, org.knime.core.node.workflow.VariableType.IntType.INSTANCE).get(nodeSettings.getPathVariable( )).getStringValue();
                String uploadedFile = upload(path, outDir);
                addRow(container, null, uploadedFile);
            }
        } finally {
            thread.cancel();
        }

        // return the results
        container.close();
        return new BufferedDataTable[] { container.getTable() };
    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
    	// check if the workflow credentials have been set
		AuthenticationUtils.checkRequiredCredentials(
				getAvailableFlowVariables( ), 
				getCredentialsProvider( ), 
				nodeSettings.getCredentialsName( )
		);
		
        // validate
        String share = nodeSettings.getSharePath( );
        String column = nodeSettings.getPathColumn( );
        FlowVariable variable = getAvailableFlowVariables(org.knime.core.node.workflow.VariableType.StringType.INSTANCE, org.knime.core.node.workflow.VariableType.DoubleType.INSTANCE, org.knime.core.node.workflow.VariableType.IntType.INSTANCE).get(nodeSettings.getPathVariable( ));
        validate(share, column, variable);

        // set warning for overwrite
        if (nodeSettings.isOverwrite( )) {
            setWarningMessage("Overwrite is enabled, will overwrite files " + "without confirmation.");
        }

        return new DataTableSpec[] { createOutSpec(inSpecs[0]) };
    }

    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // not required
    }

    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // not required
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	nodeSettings.saveSettingsTo(settings);
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    	// not required
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
    	nodeSettings.loadSettingsFrom(settings);
    }

    @Override
    protected void reset() {
        // not required
    }

    /**
     * Create the output specification which specifies the output will contain
     * the original input table plus one additional column containing the path
     * to the remote copy of the file.
     * 
     * @param inSpec
     *            The original input table specification.
     * @return The output table specification.
     */
    private DataTableSpec createOutSpec(final DataTableSpec inSpec) {
        if (inSpec != null) {
            // there is existing input
            DataColumnSpec appendSpec = new DataColumnSpecCreator(
                    DataTableSpec.getUniqueColumnName(inSpec, "Remote Copy Path"), StringCell.TYPE).createSpec();
            DataTableSpec outSpec = new DataTableSpec(appendSpec);
            return new DataTableSpec(inSpec, outSpec);
        } else {
            // no input
            DataColumnSpec newSpec = new DataColumnSpecCreator("Remote Copy Path", StringCell.TYPE).createSpec();

            return new DataTableSpec(newSpec);
        }
    }

    /**
     * Validate that the configuration from the dialog is acceptable.
     * 
     * @param share
     *            The path to the share to copy the files to.
     * @param column
     *            The optional column containing the file paths to copy.
     * @param variable
     *            The optional variable containing the file path to copy.
     * @throws InvalidSettingsException
     *             If the configuration is not correct.
     */
    private void validate(final String share, final String column, final FlowVariable variable) throws InvalidSettingsException {
        // get the credentials to use to access the share
    	credentials = AuthenticationUtils.getAuthenticationContext(
        		this.getAvailableFlowVariables(org.knime.core.node.workflow.VariableType.StringType.INSTANCE, org.knime.core.node.workflow.VariableType.DoubleType.INSTANCE, org.knime.core.node.workflow.VariableType.IntType.INSTANCE), 
        		getCredentialsProvider( ),
        		nodeSettings.getCredentialsName( )
        );

        // check that the network share is set
        if (share == null || share.equals("")) {
            throw new InvalidSettingsException("The network share path is a " + "required field and must not be blank.");
        }

        // check that a column/variable selection has been made
        if ((column == null || column.equals(""))
                && (variable == null || variable.getStringValue() == null || variable.getStringValue().equals(""))) {
            throw new InvalidSettingsException("Either the path column or a " + "path flow variable must be set.");
        }

        // check if the share path exists
        try {
			LOG.debug(SambaUtility.makeURL(share, false));
			SmbFile file = new SmbFile(SambaUtility.makeURL(share, false));
            
            if (credentials != null) {
            	new SharedConnection(file.getServer(), file.getShare(), credentials).close();
            } 
            
        } catch (IOException e) {
            throw new InvalidSettingsException("Share path '" + share + "' is not a valid location.", e);
        }
    }

    /**
     * Upload the file/directory given at the specified path to the network
     * share.
     * 
     * @param path
     *            The path to the file/directory to upload.
     * @param share
     *            The path to the network share and directory to upload to.
     * @return The path on the network share to the copied file/directory.
     */
    private String upload(final String path, final String share) throws Exception {
    	
    	File file = new File(path);

    	if (path.toLowerCase().startsWith("knime:")) {
    		file = new File(ResolverUtil
					.resolveURItoLocalFile(new URI(path)).toURI());
		}

        String outPath = SambaUtility.makeURL(share, false);

        // check if the file exists
        if (!file.exists()) {
            throw new Exception("Cannot find file '" + path + "'.");
        }

        // calculate the total file size
        thread.newFile(calculateSize(file));

        // check if this is a directory
        if (file.isDirectory()) {
            return uploadDirectory(file, outPath);
        } else {
            return uploadFile(file, outPath);
        }
    }

    /**
     * Calculate the size of the given file (or recursively for a directory).
     * 
     * @param file
     *            The file/directory to get the size of.
     * @return The size of the file/directory.
     */
    private long calculateSize(final File file) {
        long size = 0;
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                size += calculateSize(f);
            }
        } else {
            size += file.length();
        }

        return size;
    }
    
    /**
     * Create the directories
     * 
     * @param sharedConnection
     *            The base connection to use.
     * @param dirs
     *            The list of folders to create.
     */
	private void mkDirs(SharedConnection sharedConnection, List<String> dirs) {

		String path = "";

		for (String s : dirs) {
			if (s.isEmpty()) {
				continue;
			}
			if (path.isEmpty()) {
				path = s;
			} else {
				path = String.join("/", path, s);
			}
			new SharedDirectory(sharedConnection, path).ensureExists();
		}
	}

    /**
     * Upload the file to the path to the network share.
     * 
     * @param file
     *            The local copy of the file to upload.
     * @param parent
     *            The parent path on the network share to upload the file to.
     * @return The path on the network share to the copied file.
     */
    private String uploadFile(final File file, final String parent) throws Exception {
        InputStream in = null;
        OutputStream out = null;
        String path = parent + "/" + file.getName();
        SharedConnection sharedConnection = null;
        try {
        	
            // open the input file
            in = new BufferedInputStream(new FileInputStream(file));

            // initialise the output file
            SmbFile outPath = new SmbFile(path);
            
            sharedConnection = new SharedConnection(outPath.getServer(), outPath.getShare(), credentials);
            String parentFolder  = StringUtils.substringAfter(outPath.getParent(), outPath.getShare());
            
            List<String> folders = Arrays.asList(parentFolder.split("/"));
            mkDirs(sharedConnection, folders);
            
			SharedFile sharedFile = new SharedFile(sharedConnection,
					SambaUtility.fixSambaPath(parentFolder) + "/" + outPath.getName());

            if (!nodeSettings.isOverwrite( ) && sharedFile.isExisting()) {
                throw new Exception("File '" + outPath.getUncPath() + "' already exists and overwrite is disabled.");
            }
            
            out = sharedFile.getOutputStream();
            
            // copy the file
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) > 0) {
                thread.increment(n);
                out.write(buffer, 0, n);
            }

            return outPath.getUncPath();
        } catch (MalformedURLException e) {
            throw new InvalidSettingsException("Network share path '" + path + "' is not a valid location.", e);
        } catch (SmbAuthException e) {
        	if(credentials != null) {
        		throw new InvalidSettingsException(
        				"User '" + credentials.getUsername() 
        				+ "' is not authorised to access share location '" + path + "'.", 
        				e
        		);
        	}
            throw new InvalidSettingsException("User is not authorised to access share location '" + path + "'.", e);
        } catch (SmbException e) {
            throw new InvalidSettingsException("Could not access share path '" + path + "'.", e);
        } finally {
            // close the streams
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore just closing stream
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // ignore just closing stream
                }
            }
            
            if(sharedConnection!=null) {
            	sharedConnection.close();
            }
        }
    }

    /**
     * Upload the directory to the network share.
     * 
     * @param dir
     *            The local copy of the directory to upload.
     * @param parent
     *            The parent path on the network share to copy the directory to.
     * @return The path on the network share to the copied directory.
     */
    private String uploadDirectory(final File dir, final String parent) throws Exception {
        // make this directory at the target
        String path = parent + "/" + dir.getName();
        try {
            SmbFile outPath = new SmbFile(path);

            // upload the subfiles/directories
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    uploadDirectory(file, path);
                } else {
                    uploadFile(file, path);
                }
            }

            return outPath.getUncPath();
        } catch (MalformedURLException e) {
            throw new InvalidSettingsException("Network share directory '" + path + "' is not a valid location.", e);
        } catch (SmbAuthException e) {
        	if(credentials != null) {
        		throw new InvalidSettingsException(
        				"User '" + credentials.getUsername() 
        				+ "' is not authorised to access share directory '" + path + "'.", 
        				e
        		);
        	}
            throw new InvalidSettingsException("User is not authorised to access share directory '" + path + "'.", e);
        } catch (SmbException e) {
            throw new InvalidSettingsException("Could not access share directory '" + path + "'.", e);
        }
    }

    /**
     * Add a the given data as a single additional row to the container.
     * 
     * @param container
     *            The container to add the row to.
     * @param row
     *            Optional row if there are input columns to match to.
     * @param path
     *            The path to add as a new column.
     */
    private void addRow(final BufferedDataContainer container, final DataRow row, final String path) {
        DataCell[] cells = new DataCell[1];
        cells[0] = new StringCell(path);
        if (row != null) {
            AppendedColumnRow outRow = new AppendedColumnRow(row, cells);
            container.addRowToTable(outRow);
        } else {
            DefaultRow outRow = new DefaultRow(getAvailableFlowVariables(org.knime.core.node.workflow.VariableType.StringType.INSTANCE, org.knime.core.node.workflow.VariableType.DoubleType.INSTANCE, org.knime.core.node.workflow.VariableType.IntType.INSTANCE).get(nodeSettings.getPathVariable( )).getName(), cells);
            container.addRowToTable(outRow);
        }
    }
    


};
