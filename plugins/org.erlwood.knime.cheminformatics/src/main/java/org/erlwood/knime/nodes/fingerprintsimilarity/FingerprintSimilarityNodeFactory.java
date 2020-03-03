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
package org.erlwood.knime.nodes.fingerprintsimilarity;

import org.erlwood.knime.utils.node.AbstractErlWoodNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FingerprintSimilarity" Node.
 * 
 *
 * @author Nikolas Fechner
 */
public class FingerprintSimilarityNodeFactory 
        extends AbstractErlWoodNodeFactory<FingerprintSimilarityNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FingerprintSimilarityNodeModel createNodeModel() {
        return new FingerprintSimilarityNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<FingerprintSimilarityNodeModel> createNodeView(final int viewIndex,
            final FingerprintSimilarityNodeModel nodeModel) {
        return null;
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
        return new FingerprintSimilarityNodeDialog();
    }

}

