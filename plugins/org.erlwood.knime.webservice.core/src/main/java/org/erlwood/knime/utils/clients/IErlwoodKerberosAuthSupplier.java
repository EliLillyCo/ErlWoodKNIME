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
package org.erlwood.knime.utils.clients;

import javax.security.auth.Subject;

import org.erlwood.knime.utils.exceptions.WebServiceException;

import jcifs.smb.NtlmPasswordAuthentication;

/**
 * @author Luke Bullard
 */
public interface IErlwoodKerberosAuthSupplier {
	/**
	 * Logs in to the Kerberos server if we have credentials.
	 * @param credentials The credentials (can be null)
	 * @return The Subject or null
	 * @throws WebServiceException On Error
	 */
	public Subject getSubject(NtlmPasswordAuthentication credentials) throws WebServiceException;
}
