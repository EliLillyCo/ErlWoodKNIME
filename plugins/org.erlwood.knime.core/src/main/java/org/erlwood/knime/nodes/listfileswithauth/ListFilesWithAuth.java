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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.erlwood.knime.utils.auth.SambaUtility;
import org.knime.base.util.WildcardMatcher;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

import com.hierynomus.smbj.auth.AuthenticationContext;

import ch.swaechter.smbjwrapper.SharedConnection;
import ch.swaechter.smbjwrapper.SharedDirectory;
import ch.swaechter.smbjwrapper.SharedFile;
import jcifs.smb.SmbFile;


/**
 * 
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @author Samba Component - Tom Wilkin
 */
public class ListFilesWithAuth {

	/** (Static) output spec. */
	public static final DataTableSpec SPEC = new DataTableSpec(
			createDataColumnSpec());

	/** Filter criterion. */
	public enum Filter {
		/** No filter. */
		None,
		/** Filter on file extension. */
		Extensions,
		/** Regular expression filter. */
		RegExp,
		/** Plain wildcard filter. */
		Wildcards
	}

	/** RowId counter. */
	private int mCurrentRowID;

	/** Output container. */
	private BufferedDataContainer mDc;

	/** counter for the all ready read Files. */
	private int mAnalyzedFiles;

	/**
	 * Help flag to allow entering the first Folder, if recursive is not
	 * checked.
	 */
	private boolean mFirstLocation = true;

	/** extensions in case of extension filter. */
	private String[] mExtensions;

	/**
	 * regular expression in case of a wildcard or regular expression.
	 */
	private Pattern mRegExpPattern;

	/** The search settings. */
	private final ListFilesWithAuthSettings mSettings;
	
	private final AuthenticationContext creds;
	
	/** Whether any errors were generated when listing files. */
	private boolean hasErrors;

	/**
	 * Init object according to argument settings.
	 * 
	 * @param settings
	 *            The node settings.
	 */
	ListFilesWithAuth(final ListFilesWithAuthSettings settings,final AuthenticationContext creds) {
		mSettings = settings;
		this.creds = creds;
		hasErrors = false;
	}
	
	/** @return Whether the file listing generated any errors. */
	public boolean hasErrors() {
		return hasErrors;
	}

	/**
	 * Searches file system according to settings.
	 * 
	 * @param exec
	 *            Progress/cancellation
	 * @return The table containing the search hits.
	 * @throws Exception
	 *             If anything goes wrong.
	 */
	public BufferedDataTable search(final ExecutionContext exec) throws Exception {
		Collection<Object> locations = mSettings
				.getDirectoriesFromLocationString(creds);
		mDc = exec.createDataContainer(SPEC);
		String extString = mSettings.getExtensionsString();
		Filter filter = mSettings.getFilter();
		switch (filter) {
		case None:
			break;
		case Extensions:
			// extensions had to be splitted
			mExtensions = extString.split(";");
			break;
		case RegExp:
			// no break;
		case Wildcards:
			String patternS;
			if (filter.equals(Filter.Wildcards)) {
				patternS = WildcardMatcher.wildcardToRegex(extString);
			} else {
				patternS = extString;
			}
			if (mSettings.isCaseSensitive()) {
				mRegExpPattern = Pattern.compile(patternS);
			} else {
				mRegExpPattern = Pattern.compile(patternS,
						Pattern.CASE_INSENSITIVE);
			}
			break;
		default:
			throw new IllegalStateException("Unknown filter: " + filter);
			// transform wildcard to regExp.
		}
		mAnalyzedFiles = 0;
		mCurrentRowID = 0;
		SharedConnection sharedConnection = null;
		for (Object o : locations) {
			mFirstLocation = true;
			if (o instanceof File) {
				addLocation((File) o, exec);
			} else if (o instanceof SmbFile) {
				try {
					SmbFile location = (SmbFile) o;
					sharedConnection = new SharedConnection(location.getServer(), location.getShare(),
							creds);
					String parentFolder = StringUtils.substringAfter(location.getPath(), location.getShare());
					SharedDirectory sharedDirectory = new SharedDirectory(sharedConnection,
							 SambaUtility.fixSambaPath(parentFolder));
					addLocation(sharedDirectory, exec);
				} finally {
					sharedConnection.close();
				}
			}
		}

		mDc.close();
		return mDc.getTable();
	}

	/**
	 * Recursive method to add all Files of a given folder to the output table.
	 * 
	 * @param loc
	 *            folder to be analyzed
	 * @throws CanceledExecutionException
	 *             if user cancelled.
	 */
	private void addLocation(final File location, final ExecutionContext exec)
			throws CanceledExecutionException {
		mAnalyzedFiles++;
		exec.setProgress(mAnalyzedFiles + " file(s) analyzed");
		exec.checkCanceled();

		if (location.isDirectory()) {
			if (mSettings.isRecursive() || mFirstLocation) {
				mFirstLocation = false;
				// if location has further files
				File[] listFiles = location.listFiles();
				if (listFiles != null) {
					for (File loc : listFiles) {
						// recursive
						addLocation(loc, exec);
					}
				}
			}
		} else {
			// check if File has to be included
			if (satisfiesFilter(location.getName())) {
				addLocationToContainer(location);
			}
		}

	}

	/** Recursive method to add all SmbFiles of a give folder to the output
	 * table.
	 * @param location Folder the be analysed.
	 * @exec ExecutionContext to use to check if the user has cancelled.
	 * @throws Exception If user cancels or an error occurs accessing a file. */
	private void addLocation(final SharedDirectory location, final ExecutionContext exec) throws Exception {
		mAnalyzedFiles++;
		exec.setProgress(mAnalyzedFiles + " file(s) analyzed");
		exec.checkCanceled();

		if (mSettings.isRecursive()) {
			mFirstLocation = false;
			List<SharedDirectory> dirs = new ArrayList<>();

			try {
				dirs = location.getDirectories();
			} catch (Exception e) {
				addLocationToContainer(null, e);
			}

			for (SharedDirectory dir : dirs) {
				addLocation(dir, exec);
			}
		}
		List<SharedFile> files = new ArrayList<>();
		try {
			files = location.getFiles();
		} catch (Exception e) {
			addLocationToContainer(null, e);
		}

		for (SharedFile sharedFile : files) {
			if (satisfiesFilter(sharedFile.getName())) {
				addLocationToContainer(sharedFile, null);
			}
		}

	}

	/**
	 * Checks if the given File name satisfies the selected filter requirements.
	 * 
	 * @param name
	 *            filename
	 * @return True if satisfies the file else False
	 */
	private boolean satisfiesFilter(final String name) {
		switch (mSettings.getFilter()) {
		case None:
			return true;
		case Extensions:
			if (mSettings.isCaseSensitive()) {
				// check if one of the extensions matches
				for (String ext : mExtensions) {
					if (name.endsWith(ext)) {
						return true;
					}
				}
			} else {
				// case insensitive check on toLowerCase
				String lowname = name.toLowerCase();
				for (String ext : mExtensions) {
					if (lowname.endsWith(ext.toLowerCase())) {
						return true;
					}
				}
			}
			return false;
		case RegExp:
			// no break;
		case Wildcards:
			Matcher matcher = mRegExpPattern.matcher(name);
			return matcher.matches();
		default:
			return false;
		}
	}

	/**
	 * Adds a File to the table.
	 * 
	 * @param file
	 */
	private void addLocationToContainer(final File file) {
		DataCell[] row = new DataCell[3];
		row[0] = new StringCell(file.getAbsolutePath());
		
		try {
			String url = file.getAbsoluteFile().toURI().toURL().toString();
			row[1] = new StringCell(url);
			row[2] = DataType.getMissingCell( );
		} catch (MalformedURLException e) {
			row[1] = DataType.getMissingCell( );
			row[2] = new StringCell(e.getMessage());
			hasErrors = true;
		}

		mDc.addRowToTable(new DefaultRow("Row " + mCurrentRowID, row));
		mCurrentRowID++;
	}
	
	/** Adds a SmbFile that caused an error to the table.
	 * @param file The file to add.
	 * @param error The error message to add. */
	private void addLocationToContainer(final SharedFile file, final Throwable error) {
		DataCell[] row = new DataCell[3];
		if(file ==null) {
			row[0] = DataType.getMissingCell();
			row[1] = DataType.getMissingCell();
			row[2] = new StringCell(error.getMessage());
			mDc.addRowToTable(new DefaultRow("Row " + mCurrentRowID, row));
			mCurrentRowID++;
			hasErrors = true;
			return;
		}
		
		row[0] = new StringCell(file.getSmbPath());
		try {
			String urlStr = "file:////" + file.getServerName() + "/" + file.getShareName() + "/" + file.getPath();
			row[1] = new StringCell(urlStr);

			// add the error to the output table
			if (error != null) {
				row[2] = new StringCell(error.getMessage());
				hasErrors = true;
			} else {
				row[2] = DataType.getMissingCell();
			}
		} catch (Exception e) {
			row[1] = DataType.getMissingCell();
			row[2] = new StringCell(e.getMessage());
			hasErrors = true;
		}

		mDc.addRowToTable(new DefaultRow("Row " + mCurrentRowID, row));
		mCurrentRowID++;
	}

	private static DataColumnSpec[] createDataColumnSpec() {
		DataColumnSpec[] dcs = new DataColumnSpec[3];
		dcs[0] = new DataColumnSpecCreator("Location", StringCell.TYPE)
				.createSpec();
		dcs[1] = new DataColumnSpecCreator("URL", StringCell.TYPE).createSpec();
		dcs[2] = new DataColumnSpecCreator("Errors", StringCell.TYPE).createSpec();
		return dcs;
	}

}
