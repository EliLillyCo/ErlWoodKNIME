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
package org.erlwood.knime.utils.auth;

import java.net.MalformedURLException;
import java.net.URL;

import jcifs.smb.SmbFile;

/**
 * Utility class to provide common functionality that is required when using the
 * JCIFS library to access files on a Windows network share.
 * 
 * @author Tom Wilkin
 */
public final class SambaUtility {
	
	/** No instantiation for static class. */
	private SambaUtility() {
	}
	
	private static String SLASH_AT_START_REGEX = "^/+";
	private static String DOUBLE_SLASH = "\\/\\/+";
	private static String SLASH_AT_END_REGEX = "\\/$";
	
	/**
	 * Remove the forward slashes at the beginning and the end of the string, 
	 * replace double forward slashes with single forward slash.
	 * 
	 * @param path
	 *            The String to manipulate.
	 * @return The fixed String.
	 */
	public static String fixSambaPath(String path) {
		return path.replaceAll(SambaUtility.SLASH_AT_START_REGEX, "").replaceAll(SambaUtility.DOUBLE_SLASH, "/")
				.replaceAll(SLASH_AT_END_REGEX, "");
	}

	/**
	 * Create a samba URL from the given UNC or file protocol path. The path
	 * will be returned unmodified if it is not a UNC or file protocol path.
	 * 
	 * @param path
	 *            The path to convert to a samba URL.
	 * @return The samba URL created from this path.
	 */
	public static String makeURL(final String path, boolean addTrailingSlash) {
		String output = path;

		// decide if this should be a samba URL
		if (output.startsWith("file:////")) {
			output = output.replace("file:////", "smb://");
		} else if (output.startsWith("\\\\")) {
			output = "smb:" + output.replace("\\", "/");
		}

		// Samba URLs require a trailing /
		if (addTrailingSlash && output.startsWith("smb://") && !output.endsWith("/")) {
			output += "/";
		}

		return output;
	}

	/**
	 * Get the file name and full path of the specified samba file without the
	 * server name and share.
	 * 
	 * @param smbFile
	 *            The samba file to get the file name and path for.
	 * @return The file name and path of the file without the server name and
	 *         share.
	 */
	public static String stripServer(final SmbFile smbFile) {
		String filePath = smbFile.getPath()
				.replace(
						"smb://" + smbFile.getServer() + "/"
								+ smbFile.getShare() + "/", "");
		return filePath;
	}
	
	/**
	 * Return whether the given URL is a Samba URL or not.
	 * 
	 * @param url
	 *            The URL to check if it's a Samba URL.
	 * @return Whether the given URL is a Samba URL or not.
	 */
	public static boolean isSambaURL(final String url) {
		return url.startsWith("smb://");
	}
	
	/** Return whether the given path is a UNC path or not.
	 * @param path The path to check if it's a UNC path.
	 * @return Whether the given path is a UNC path or not. */
	public static boolean isUNC(final String path) {
		return path.startsWith("\\\\");
	}
	
};
