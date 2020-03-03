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
package org.erlwood.knime.utils.settings;

/** Class to wrap the web service settings that should be enabled/disabled.
 * @author Tom Wilkin */
public class WebServiceSettingsOptions {
	
	/** Value for using credentials. */
	public static final int USE_CREDENTIALS = 1;
	
	/** Value for using optional credentials. */
	public static final int USE_OPTIONAL_CREDENTIALS = 2;
	
	/** Value for using the time out. */
	public static final int USE_TIME_OUT = 4;
	
	/** Value for using the max child elements. */
	public static final int USE_MAX_CHILD_ELEMENTS = 8;
	
	/** Disable all the web service settings options. */
	public static final int NONE = 0;
	
	/** Enable all the options. */
	public static final int ALL = USE_CREDENTIALS | USE_TIME_OUT | USE_MAX_CHILD_ELEMENTS;
	
	/** Enable all the options applicable for SOAP. */
	public static final int SOAP = USE_CREDENTIALS | USE_TIME_OUT | USE_MAX_CHILD_ELEMENTS;
	
	/** Enable all the options applicable for SOAP except credentials. */
	public static final int SOAP_NO_CREDENTIALS = USE_TIME_OUT | USE_MAX_CHILD_ELEMENTS;
	
		/** Enable all the web service settings applicable for REST. */
	public static final int REST = USE_CREDENTIALS | USE_TIME_OUT;
	
	/** Enable all the web service settings applicable for REST except credentials. */
	public static final int REST_NO_CREDENTIALS = USE_TIME_OUT;
	
	
	protected WebServiceSettingsOptions( ) {
		throw new IllegalStateException("You must use one of the non-default constructors");
	}
	
	/** Return whether the workflow credentials can be set.
	 * @param options The options that are selected. 
	 * @return Whether the workflow credentials can be set. */
	public static boolean useCredentials(final int options) {
		return (options & USE_CREDENTIALS) == USE_CREDENTIALS 
				|| (options & USE_OPTIONAL_CREDENTIALS) == USE_OPTIONAL_CREDENTIALS;				
	}
	
	/** Return whether the workflow credentials can be optionally set.
	 * @param options The options that are selected. 
	 * @return Whether the workflow credentials can be optionally set. */
	public static boolean useOptionalCredentials(final int options) {
		return (options & USE_OPTIONAL_CREDENTIALS) == USE_OPTIONAL_CREDENTIALS;
	}
	
	/** Return whether the time out value can be set.
	 * @param options The options that are selected. 
	 * @return Whether the time out value can be set. */
	public static boolean useTimeOut(final int options) {
		return (options & USE_TIME_OUT) == USE_TIME_OUT;
	}
	
	/** Return whether the max child elements value can be set.
	 * @param options The options that are selected.
	 * @return Whether the max child elements can be set. */
	public static boolean useMaxChildElements(final int options) {
		return (options & USE_MAX_CHILD_ELEMENTS) == USE_MAX_CHILD_ELEMENTS;
	}	

}
