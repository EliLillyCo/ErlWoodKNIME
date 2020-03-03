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

/** The Exception raised when a user is not authorised to access the resource.
 * @author Tom Wilkin */
public class NotAuthorisedException extends WebServiceException {

	/** Serialisation version id for this exception. */
	private static final long serialVersionUID = 1L;
	
	/** The message for this Exception. */
	private static final String MESSAGE = 
			"User is not authorised to access this resource, please check username and password.";
	
	/** The message when the user is running Linux. */
	private static final String LINUX_MESSAGE = 
			"Linux users must set workflow credentials for authenticated web services in KNIME.";

	/** Construct a new NotAuthorisedException. */
	public NotAuthorisedException( ) {
		super(constructMessage( ));
	}
	
	/** Construct a new NotAuthorisedException instance.
	 * @param cause The Throwable instance that caused this NotAuthorisedException. */
	public NotAuthorisedException(final Throwable cause) {
		super(constructMessage( ), cause);
	}
	
	/** Generate the message for the exception, including additional instruction if the user is
	 * running on a Linux platform. 
	 * @return The message for this exception. */
	private static String constructMessage( ) {
		if(System.getProperty("os.name").toLowerCase( ).contains("linux")) {
			return MESSAGE + " " + LINUX_MESSAGE;
		} else {
			return MESSAGE;
		}
	}
	
};
