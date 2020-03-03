/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2015 Eli Lilly and Company Limited
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
package org.erlwood.knime.utils.gui.documentfilters;

/** Document Filter to allow comma separated lists of integers.
 * @author Tom Wilkin */
public class MultipleIntegerDocumentFilter extends AbstractDocumentFilter {
	
	/** The delimiter to use to split the number list. */
	private String delimiter;
	
	public MultipleIntegerDocumentFilter(final String delimiter) {
		this.delimiter = delimiter;
	}
	
	public MultipleIntegerDocumentFilter( ) {
		this(",");
	}

	@Override
	protected void parse(final String str) {
		String[ ] values = str.split(delimiter);
		for(String value : values) {
			Integer.parseInt(value);
		}
	}

}
