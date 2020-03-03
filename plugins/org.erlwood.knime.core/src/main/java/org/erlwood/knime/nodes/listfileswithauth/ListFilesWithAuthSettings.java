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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.erlwood.knime.nodes.listfileswithauth.ListFilesWithAuth.Filter;
import org.erlwood.knime.utils.auth.SambaUtility;
import org.erlwood.knime.utils.gui.auth.AuthenticationSettings;
import org.erlwood.knime.utils.gui.auth.AuthenticationTab;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.StringHistory;

import com.hierynomus.smbj.auth.AuthenticationContext;

import ch.swaechter.smbjwrapper.SharedConnection;
import jcifs.smb.SmbFile;

/**
 * 
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @author Samba Component - Tom Wilkin
 */
public class ListFilesWithAuthSettings extends AuthenticationSettings {

	/** ID for the file history. */
	private static final String LIST_FILES_HISTORY_ID = "List Files with Auth History ID";

	/** ID for the extension history. */
	private static final String LIST_FILES_EXT_HISTORY_ID = "LIST_FILES_WITH_AUTH_EXT_HISTORY_ID";

	/** Key to store the location settings. */
	public static final String LOCATION_SETTINGS = "FILESETTINGS";

	/** Key to store the RECURSIVE_SETTINGS. */
	public static final String RECURSIVE_SETTINGS = "RECURSIVE_SETTINGS";

	/** Key to store the Filter Settings. */
	public static final String FILTER_SETTINGS = "FILTER_SETTINGS";

	/** Key to store the case sensitive settings. */
	public static final String CASE_SENSITIVE_STRING = "CASESENSITVE";

	/** Key to store the extension_settings. */
	public static final String EXTENSIONS_SETTINGS = "EXTENSIONS";
	
	/** Key for the credentials name. */
	public static final String CREDENTIALS_NAME_SETTINGS = 
			AuthenticationTab.createCredentialsNameSettingsModel( ).getKey( );

	/** the folders to be analyzed. */
	private String mLocationString = null;

	/** contains the log-format of the files. */
	private String mExtensionsString;

	/** recursive flag. */
	private boolean mRecursive = false;

	/** Flag to switch between case sensitive and insensitive. */
	private boolean mCaseSensitive = false;

	/** Filter type. */
	private Filter mFilter = Filter.None;

	/** @return the locationString */
	public String getLocationString() {
		return mLocationString;
	}

	/**
	 * @param locationString
	 *            the locationString to set
	 */
	public void setLocationString(final String locationString) {
		mLocationString = locationString;
	}

	/** @return the extensionsString */
	public String getExtensionsString() {
		return mExtensionsString;
	}

	/**
	 * @param extensionsString
	 *            the extensionsString to set
	 */
	public void setExtensionsString(final String extensionsString) {
		mExtensionsString = extensionsString;
	}

	/** @return the recursive */
	public boolean isRecursive() {
		return mRecursive;
	}

	/**
	 * @param recursive
	 *            the recursive to set
	 */
	public void setRecursive(final boolean recursive) {
		mRecursive = recursive;
	}

	/** @return the caseSensitive */
	public boolean isCaseSensitive() {
		return mCaseSensitive;
	}

	/**
	 * @param caseSensitive
	 *            the caseSensitive to set
	 */
	public void setCaseSensitive(final boolean caseSensitive) {
		mCaseSensitive = caseSensitive;
	}

	/** @return the filter */
	public Filter getFilter() {
		return mFilter;
	}

	/**
	 * @param filter
	 *            the filter to set
	 * @throws NullPointerException
	 *             If argument is null.
	 */
	public void setFilter(final Filter filter) {
		if (filter == null) {
			throw new IllegalArgumentException("Argument must not be null.");
		}
		mFilter = filter;
	}

	/**
	 * Split location string by ";" and return individual directories.
	 * 
	 * @return A list of files representing directories.
	 * @throws InvalidSettingsException
	 *             If the argument is invalid or does not represent a list of
	 *             existing directories.
	 * @throws IOException 
	 */
	public Collection<Object> getDirectoriesFromLocationString(AuthenticationContext creds)
			throws InvalidSettingsException {
		if (mLocationString == null || mLocationString.equals("")) {
			throw new InvalidSettingsException("Please select a folder!");
		}
		SharedConnection sharedConnection = null;
		String[] subs = mLocationString.split(";");
		List<Object> result = new ArrayList<Object>();
		for (String s : subs) {
			// decide if this should be a samba URL
			s = SambaUtility.makeURL(s, false);
			if (SambaUtility.isSambaURL(s)) {
				try {
					SmbFile f = new SmbFile(URLDecoder.decode(s, "UTF-8"));
					sharedConnection = new SharedConnection(f.getServer(), f.getShare(), creds); //check auth and location
					result.add(f);
					continue;
				} catch (MalformedURLException e) {
					throw new InvalidSettingsException("\"" + s
							+ "\" does not exist or is not a directory", e);
				} catch (UnsupportedEncodingException e) {
					throw new InvalidSettingsException("\"" + s
							+ "\" does not exist or is not a directory", e);
				} catch (IOException e) {
					throw new InvalidSettingsException("\"" + s
							+ "\" does not exist or is not a directory", e);
				} finally {
					if(sharedConnection!=null) {
						try {
							sharedConnection.close();
						} catch (IOException e) {
							throw new InvalidSettingsException("Unexpected error closing connection", e);
							}
					}
				}
				
			}
			
			// samba failed, must be a local file
			File f = new File(s);
			if (!f.isDirectory()) {
				try {
					if (s.startsWith("file:")) {
						s = s.substring(5);
					}
					f = new File(URLDecoder.decode(s, "UTF-8"));
				} catch (UnsupportedEncodingException ex) {
					throw new InvalidSettingsException("\"" + s
							+ "\" does not exist or is not a directory", ex);
				}
				if (!f.isDirectory()) {
					throw new InvalidSettingsException("\"" + s
							+ "\" does not exist or is not a directory");
				}
			}
			result.add(f);
		}
		
		return result;
	}

	/**
	 * Load settings, fail if incomplete.
	 * 
	 * @param settings
	 *            To load from.
	 * @throws InvalidSettingsException
	 *             If that fails.
	 */
	protected void loadSettingsInModel(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		mLocationString = settings.getString(LOCATION_SETTINGS);
		if (mLocationString == null) {
			throw new InvalidSettingsException("No location given.");
		}
		mExtensionsString = settings.getString(EXTENSIONS_SETTINGS);
		mRecursive = settings.getBoolean(RECURSIVE_SETTINGS);
		String filterS = settings.getString(FILTER_SETTINGS);
		if (filterS == null) {
			throw new InvalidSettingsException("No filter provided");
		}
		try {
			mFilter = Filter.valueOf(filterS);
		} catch (IllegalArgumentException iae) {
			throw new InvalidSettingsException("Invalid filter: " + filterS, iae);
		}
		mCaseSensitive = settings.getBoolean(CASE_SENSITIVE_STRING);
		
		super.loadSettingsFrom(settings);
	}

	/**
	 * Load settings in dialog (no fail).
	 * 
	 * @param settings
	 *            To load from.
	 */
	protected void loadSettingsInDialog(final NodeSettingsRO settings) {
		mLocationString = settings.getString(LOCATION_SETTINGS, "");
		mExtensionsString = settings.getString(EXTENSIONS_SETTINGS, "");
		mRecursive = settings.getBoolean(RECURSIVE_SETTINGS, false);
		final Filter defFilter = Filter.None;
		String filterS = settings.getString(FILTER_SETTINGS, defFilter.name());
		if (filterS == null) {
			filterS = defFilter.name();
		}
		try {
			mFilter = Filter.valueOf(filterS);
		} catch (IllegalArgumentException iae) {
			mFilter = defFilter;
		}
		mCaseSensitive = settings.getBoolean(CASE_SENSITIVE_STRING, false);
		
		try {
			super.loadSettingsFrom(settings);
		} catch(InvalidSettingsException e) {
			// ignore
		}
	}

	/**
	 * Save settings in model & dialog.
	 * 
	 * @param settings
	 *            To save to.
	 */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		settings.addString(EXTENSIONS_SETTINGS, mExtensionsString);
		settings.addString(LOCATION_SETTINGS, mLocationString);
		settings.addBoolean(RECURSIVE_SETTINGS, mRecursive);
		settings.addString(FILTER_SETTINGS, mFilter.name());
		settings.addBoolean(CASE_SENSITIVE_STRING, mCaseSensitive);

		if (mLocationString != null) {
			StringHistory h = StringHistory.getInstance(LIST_FILES_HISTORY_ID);
			h.add(mLocationString);
		}

		if (mExtensionsString != null) {
			StringHistory h = StringHistory
					.getInstance(LIST_FILES_EXT_HISTORY_ID);
			h.add(mExtensionsString);

		}
		
		super.saveSettingsTo(settings);
	}

	/** @return the previously analyzed folders. */
	static String[] getLocationHistory() {
		StringHistory h = StringHistory.getInstance(LIST_FILES_HISTORY_ID);
		return h.getHistory();
	}

	/** @return the previous selected extension field strings. */
	static String[] getExtensionHistory() {
		StringHistory h = StringHistory.getInstance(LIST_FILES_EXT_HISTORY_ID);
		return h.getHistory();
	}

}
