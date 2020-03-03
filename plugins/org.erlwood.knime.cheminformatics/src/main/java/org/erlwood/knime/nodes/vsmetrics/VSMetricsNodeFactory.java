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
package org.erlwood.knime.nodes.vsmetrics;

import org.erlwood.knime.utils.node.AbstractErlWoodNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "VSMetrics" Node.
 * 
 *
 * @author Nikolas Fechner
 */
public class VSMetricsNodeFactory 
        extends AbstractErlWoodNodeFactory<VSMetricsNodeModel> {

    /**
     * {@inheritDoc}
     */
   public VSMetricsNodeModel createNodeModel() {
        return new VSMetricsNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public NodeView<VSMetricsNodeModel> createNodeView(final int viewIndex,
            final VSMetricsNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public NodeDialogPane createNodeDialogPane() {
        return new VSMetricsNodeDialog();
    }

}

