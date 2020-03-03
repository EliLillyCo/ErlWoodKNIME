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

import org.knime.chem.types.CMLValue;
import org.knime.chem.types.Mol2Value;
import org.knime.chem.types.RxnValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataType;

import chemaxon.struc.DPoint3;
import chemaxon.struc.Molecule;
import jp.co.infocom.cheminfo.marvin.type.MrvValue;

/**
 * A number of static utility methods
 * 
 * @author Dimitar Hristozov
 */
public final class ChemistryUtils {

	public static final Class[] CHEM_DATA_TYPES = { MrvValue.class,
			SmilesValue.class, SdfValue.class, Mol2Value.class, CMLValue.class,
			SdfValue.class, RxnValue.class };

	/** No instances allowed. */
	private ChemistryUtils() {
	}

	
	/**
	 * Checks if all atoms in a given molecule have 2D coordinates.
	 * 
	 * @param mol
	 *            The molecule to check
	 * @return <code>true</code> if all atoms in <code>mol</code> have 2D
	 *         coordinates
	 */
	public static boolean has2DCoordinates(Molecule mol) {
		int zeroC = 0;
		for (int a = 0; a < mol.getAtomCount(); ++a) {
			DPoint3 c = mol.getAtom(a).getLocation();
			if (null == c) {
				return false;
			}
			if (0.0 == c.x && 0.0 == c.y) {
				zeroC += 1;
			}
			if (1 < zeroC) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether given data type is compatible to chemistry type. The
	 * chemistry types are defined inside {{@link #CHEM_DATA_TYPES}
	 * 
	 * @param dt
	 *            The data type to check. Can't be <code>null</code>
	 * @return <code>true</code> if <code>dt</code> is compatible to at least
	 *         one of the chemistry data types.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean isCompatibleToChemType(final DataType dt) {
		for (Class<?> cl : CHEM_DATA_TYPES) {
			if (dt.isCompatible((Class) cl)) {
				return true;
			}
		}
		return false;
	}

}
