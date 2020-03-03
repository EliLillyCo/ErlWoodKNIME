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

package org.erlwood.knime.utils.clients.schemefactories;

import org.apache.http.impl.auth.win.WindowsNegotiateScheme;

/**
 * Copy of WindowsNegotiateScheme objects that do not attempt to dispose() when finalised.
 * This will prevent this issue https://issues.apache.org/jira/browse/HTTPCLIENT-1681
 * @author Luke Bullard
 */
public class ErlwoodWindowsNegotiateScheme extends WindowsNegotiateScheme {
	
	public ErlwoodWindowsNegotiateScheme(final String scheme, final String servicePrincipalName) {
		super(scheme, servicePrincipalName);
	}
	
	@Override
    public void finalize() throws Throwable {
        //	Do nothing
    }
}
