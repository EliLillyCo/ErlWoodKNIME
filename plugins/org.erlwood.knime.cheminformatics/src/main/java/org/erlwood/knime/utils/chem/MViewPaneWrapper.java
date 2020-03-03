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

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import chemaxon.struc.Molecule;

/**
 * Wrapper class to handle the case where the user upgrades to the full, paid version of Marvin nodes
 * but we have compiled against the free version without a varargs setM method
 * @author Luke Bullard
 *
 */
public final class MViewPaneWrapper {
	private static final Logger LOG = Logger.getLogger(MViewPaneWrapper.class.getName());
	private static final Method SETM;
	private static final boolean OLD_METHOD;
	
	static {
		Method m = null;
		boolean b = true;
		try {
			Class<?> mviewClazz = Class.forName("chemaxon.marvin.beans.MViewPane");
			
			//	Try to resolve the non varargs version first..
			try {
				m = mviewClazz.getMethod("setM", new Class<?>[] {int.class, Molecule.class});
			} catch(NoSuchMethodException nsm) {
				m = mviewClazz.getMethod("setM", new Class<?>[] {int.class, Molecule[].class});
				b = false;
			}
		} catch(Exception ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
		
		SETM = m;
		OLD_METHOD = b;
	}

	private MViewPaneWrapper() {
	}
	
	/**
	 * Call the setM method via reflection to cope with the user upgrading to the full version
	 * of the Marvin nodes that has a varargs version. 
	 * @param mViewPane The view pane to use
	 * @param i The index
	 * @param mol The molecule
	 */
	public static void setM(Object mViewPane, int i, Molecule mol) {
		Object[] args = new Object[2];
		args[0] = i;
		args[1] = mol;
		if (!OLD_METHOD) {
			args[1] = new Molecule[] { mol };
		}
		//	Invoke the method..
		try {
			SETM.invoke(mViewPane, args);
		} catch(Exception ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
}
