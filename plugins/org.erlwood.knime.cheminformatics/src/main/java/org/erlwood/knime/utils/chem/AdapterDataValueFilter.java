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

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.util.DataValueColumnFilter;

public class AdapterDataValueFilter extends DataValueColumnFilter {
	private final Class<? extends DataValue>[] mFilterClasses;

    /**Constructor for class AdapterDataValueFilter.
     * @param filterValueClasses classes derived from DataValue. 
     * All other columns will be filtered.
     */
    public AdapterDataValueFilter(
            final Class<? extends DataValue>... filterValueClasses) {
        super(filterValueClasses);
        mFilterClasses = filterValueClasses;
    }

    /**
     * {@inheritDoc}
     */
    public boolean includeColumn(final DataColumnSpec colSpec) {
        if (colSpec == null) {
            throw new NullPointerException(
                    "Column specification must not be null");
        }
        for (Class<? extends DataValue> cl : mFilterClasses) {
        	
            if (colSpec.getType().isCompatible(cl)) {
               return true;
            }
            if (colSpec.getType().isAdaptable(cl)) {
                return true;
             }
       
        }
        return false;
    }
   
}
