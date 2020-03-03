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
package org.erlwood.knime.utils.exceptions;

/** The Exception raised when there is a general issue using a web service.
 * @author Tom Wilkin */
public class WebServiceException extends Exception {

	/** Unique serialisation id for this exception. */
	private static final long serialVersionUID = 1L;

	/** Construct a new WebserviceException.
	 * @param message The message describing this exception. */
	public WebServiceException(final String message) {
		super(message);
	}
	
	/** Construct a new WebserviceException.
	 * @param message The message describing this exception.
	 * @param cause The throwable that caused this exception to be thrown. */
	public WebServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
};
