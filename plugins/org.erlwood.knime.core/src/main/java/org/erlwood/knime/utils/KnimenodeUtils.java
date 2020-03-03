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
package org.erlwood.knime.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;

/**
 * A number of static utility methods
 * 
 * @author Dimitar Hristozov
 */
public final class KnimenodeUtils {

	/** No instances allowed. */
	private KnimenodeUtils() {
	}

	/**
	 * Checks is a given file name denotes a valid file.
	 * 
	 * @param fname
	 *            The name of the file to check.
	 * @return A bit counter intuitive return value of <code>null</code> denotes
	 *         a valid file name. Otherwise an error message is returned.
	 */
	public static String checkFileName(final String fname) {
		if (null == fname || fname.length() == 0) {
			return "No filename given.";
		}
		// Is this a URL formatted path ?
		try {
			new URL(fname);
			return null;
		} catch (MalformedURLException ex) {
			// Do nothing, not a URL
		}
		File f = new File(fname);
		if (!f.exists()) {
			return "File '" + f.getAbsolutePath() + "' does not exist.";
		}
		if (!f.isFile()) {
			return "The path '" + f.getAbsolutePath() + "' is not a file.";
		}
		return null;
	}

	/**
	 * Deletes all files from <code>folder</code>
	 * 
	 * @param folder
	 *            The folder to clear
	 */
	public static void clearFolder(final String folder) {
		File f = new File(folder);
		if (f.isDirectory()) {
			File[] toDel = f.listFiles();
			if (null != toDel) {
				for (File f2Del : toDel) {
					if (!f2Del.isDirectory()) {
						f2Del.delete();
					}
				}
			}
		}
	}

	/**
	 * Creates a column name which is guaranteed to be unique inside the given
	 * spec.
	 * 
	 * @param startName
	 *            The name to start with.
	 * @param spec
	 *            The spec for which a column name is needed
	 * @return A column name which is guaranteed to be unique inside the given
	 *         spec. If <code>start_name</code> is already unique it is
	 *         returned.
	 */
	public static String createColumnName(String startName, DataTableSpec spec) {
		String res = startName;
		while (spec.containsName(res)) {
			int st = res.length() - 1;
			String s = res.substring(st);
			while (st > 0) {
				try {
					Integer.valueOf(s);
					s = res.substring(--st);
				} catch (NumberFormatException nfe) {
					s = res.substring(++st);
					break;
				}
			}
			if (0 == s.length()) {
				res += " 2";
			} else {
				res = res.substring(0, st) + (Integer.valueOf(s) + 1);
			}
		}
		return res;
	}
	
	/**
     * Calculates the hashcode of a BufferedDataTable by enumerating the whole contents.
     * @param bt The BufferedDataTable to calculate the hashcode for.
     * @return The hashcode
     */
    public static long calculateHashCode(BufferedDataTable bt) {
    	final int prime = 31;
		int result = 1;
		
    	for (DataRow r : bt) {
    		for (DataCell c : r) {
    			result = prime * result + c.hashCode();
    		}
    	}
    	return result;
    }
	
}
