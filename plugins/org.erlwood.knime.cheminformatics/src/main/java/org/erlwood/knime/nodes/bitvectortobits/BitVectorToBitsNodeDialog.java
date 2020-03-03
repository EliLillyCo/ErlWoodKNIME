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
package org.erlwood.knime.nodes.bitvectortobits;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "BitVectorToBits" Node.
 * Splits column with BitVector values to a number of individual columns with 
 * one bit in each of them. That is, 256 new columns will be created for a 
 * BitVector with 256 bits.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Dimitar Hristozov
 */
public class BitVectorToBitsNodeDialog extends DefaultNodeSettingsPane
{
	private SettingsModelString mColName = new SettingsModelString(
		BitVectorToBitsNodeModel.CFG_COLNAME, "");
    /**
     * New pane for configuring the BitVectorToBits node.
     */
    @SuppressWarnings({"unchecked", "deprecation"})
	protected BitVectorToBitsNodeDialog()
    {
    	DialogComponentColumnNameSelection colSel = 
    		new DialogComponentColumnNameSelection(mColName, "Select column",0,
    			org.knime.core.data.vector.bitvector.BitVectorValue.class,
    			org.knime.base.data.bitvector.BitVectorValue.class);
    	addDialogComponent(colSel);
    }    
}

