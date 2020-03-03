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
package org.erlwood.knime.nodes.stringtomolecule;

import org.erlwood.knime.utils.node.AbstractErlWoodNodeFactory;
import org.erlwood.knime.utils.nodes.ChemContentsNodeModel;
import org.erlwood.knime.utils.nodes.ChemContentsNodeView;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;


/**
 * <code>NodeFactory</code> for the "StringToMolecule" Node.
 * Attempts to convert String compatible columns to a MoleculeAdapterCell object. 
 * The output can either append a column or replace the original one.
 *
 * @author Dimitar Hritozov
 */
public class StringToMoleculeNodeFactory extends AbstractErlWoodNodeFactory<ChemContentsNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ChemContentsNodeModel createNodeModel() {
        return new StringToMoleculeNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<ChemContentsNodeModel> createNodeView(final int viewIndex,
            final ChemContentsNodeModel nodeModel) {
        return new ChemContentsNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new StringToMoleculeNodeDialog();
    }

}

