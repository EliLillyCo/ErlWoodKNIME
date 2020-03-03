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
package org.erlwood.knime.utils.chem;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;

public final class CTabNormaliser {
	/**
	 * The expected line length for an M SDD property.
	 */
	private static final int SDD_LINE_LENGTH = 56;

	/**
	 * Private constructor for utility class.
	 */
	private CTabNormaliser() {
	}

	private static String createRepeatString(int length, char c) {
		char[] bytes = new char[length];
		Arrays.fill(bytes, c);
		return new String(bytes);
	}

	public static String normalise(String input) {
		BufferedReader reader = new BufferedReader(new StringReader(input));
		String line = "";
		StringBuffer sb = new StringBuffer();
		boolean bIgnore = false;
		try {
			while ((line = reader.readLine()) != null) {
				
				/**
				 * To ensure the downstream processing will work (Marvin) we
				 * have to make sure an SDD line has the correct line length. M
				 * SDD sss xxxxx.xxxxyyyyy.yyyy eeefgh i jjjkkk ll m noo (56
				 * chars)
				 */
				if (line.startsWith("M  SDD")
						&& line.length() < SDD_LINE_LENGTH) {
					line += createRepeatString(SDD_LINE_LENGTH - line.length(), ' ');
				}
				
				if (line.startsWith("M  V30 FIELDDISP")) {
					//	Extract out the start, middle and end sections
					int start = line.indexOf("\"");
					int end = line.lastIndexOf("\"");
					String middle = line.substring(start + 1, end);
					if (middle.length() < SDD_LINE_LENGTH - 7) {
						middle += createRepeatString(SDD_LINE_LENGTH - middle.length()  -7, ' ');
						line = line.substring(0, start + 1) + middle + line.substring(end);
						
					}
				}

				//	Strip all V300 Collection info
				if (line.startsWith("M  V30 BEGIN COLLECTION")) {
					bIgnore = true;
					continue;
				}
				if (line.startsWith("M  V30 END COLLECTION")) {
					bIgnore = false;
					continue;
				}
				
				if (bIgnore) {
					continue;
				}
				
				sb.append(line);
				sb.append("\n");
			}
			reader.close();
		} catch (Exception ex) {
			return null;
		}
		return sb.toString();
	}
}
