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

import org.erlwood.knime.utils.exceptions.WebServiceException;

/** Interface for Erlwood Web Service URL Supplier extension point.
 * @author Tom Wilkin */
public interface IWebServiceURLSupplier {
	
	/** Retrieve the URL from the extension point for the given web service preference id.
	 * @param prferenceId The web service preference id for the URL to retrieve.
	 * @return The URL for the web service if it can be found.
	 * @throws WebServiceException If the preference cannot be found. */
	public String getURL(final String prferenceId) throws WebServiceException;
	
	/** Check whether the URL preference for the id should be visible to the user.
	 * @param prferenceId The web service preference id to check.
	 * @return Whether the preference is visible, or null if it cannot be found. */
	public Boolean isVisible(final String prferenceId);

}
