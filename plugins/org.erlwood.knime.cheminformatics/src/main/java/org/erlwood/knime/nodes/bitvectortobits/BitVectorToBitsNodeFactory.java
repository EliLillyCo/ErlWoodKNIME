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

import org.erlwood.knime.utils.node.AbstractErlWoodNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "BitVectorToBits" Node.
 * Splits column with BitVector values to a number of individual columns with 
 * one bit in each of them. That is, 256 new columns will be created for a 
 * BitVector with 256 bits.
 *
 * @author Dimitar Hristozov
 */
public class BitVectorToBitsNodeFactory 
        extends AbstractErlWoodNodeFactory<BitVectorToBitsNodeModel>
{

    /**
     * {@inheritDoc}
     */
    @Override
    public BitVectorToBitsNodeModel createNodeModel()
    {
        return new BitVectorToBitsNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() { return 0; }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<BitVectorToBitsNodeModel> createNodeView(final int viewIndex,
    	final BitVectorToBitsNodeModel nodeModel)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() { return true; }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane()
    {
        return new BitVectorToBitsNodeDialog();
    }
}

