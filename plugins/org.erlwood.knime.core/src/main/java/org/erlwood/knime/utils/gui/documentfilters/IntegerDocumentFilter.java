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

public class IntegerDocumentFilter extends AbstractDocumentFilter {
	
	/** Whether negative values are supported by this filter. */
	private final boolean negative;
	
	public IntegerDocumentFilter(final boolean negative) {
		this.negative = negative;
	}
	
	public IntegerDocumentFilter( ) {
		this(true);
	}

	@Override
	protected void parse(String s) {
		int value = Integer.parseInt(s);
		
		// check if the value is negative and whether we need to reject it
		if(!negative && value < 0) {
			throw new NumberFormatException("Cannot have negative value.");
		}
	}

}
