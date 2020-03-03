/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2017 Eli Lilly and Company Limited
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

/** Supplier interface to specify the preference id for the URL used by the node.
 * @author Tom Wilkin */
public interface IWebServicePreferenceSupplier {
	
	/** @return The preference id used to get the default web service URL from the 
	 * KNIME preferences. */
	public String getURLPreferenceId();

	/** @return A suffix to append to the default web service URL in the preferences. */
	public String getURLSuffix();
		
	/** @return A prefix to append to the star to all keys. Return null by default. */
	public String getKeyPrefix();

}
