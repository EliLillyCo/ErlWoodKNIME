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
package org.erlwood.knime.nodes.openphacts;

import org.erlwood.knime.utils.node.AbstractErlWoodNodeFactory;
import org.erlwood.knime.utils.node.HelpTextUpdater;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <code>NodeFactory</code> for the "OpenPhacts" Node.
 * @author Luke Bullard 
 */
public class OpenPhactsNodeFactory 
        extends AbstractErlWoodNodeFactory<OpenPhactsNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenPhactsNodeModel createNodeModel() {
        return new OpenPhactsNodeModel();
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
    public NodeView<OpenPhactsNodeModel> createNodeView(final int viewIndex,
            final OpenPhactsNodeModel nodeModel) {
        return new OpenPhactsNodeView(nodeModel);
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
        return new OpenPhactsNodeDialog();
    }
    
    @Override
	public void addSupportInfo(final Document doc, final Element ul) {
    	HelpTextUpdater.addListItem(doc, ul, "Data Source", doc.createTextNode("OpenPHACTS Web Service"));
	}

}

