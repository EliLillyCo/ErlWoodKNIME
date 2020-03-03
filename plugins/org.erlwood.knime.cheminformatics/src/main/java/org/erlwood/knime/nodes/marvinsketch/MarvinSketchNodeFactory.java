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
package org.erlwood.knime.nodes.marvinsketch;

import org.erlwood.knime.utils.node.AbstractErlWoodNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;

public class MarvinSketchNodeFactory extends AbstractErlWoodNodeFactory<MarvinSketchNodeModel> {
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new MarvinSketchNodeDialog();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MarvinSketchNodeModel createNodeModel() {
		return new MarvinSketchNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<MarvinSketchNodeModel> createNodeView(int viewIndex,
			MarvinSketchNodeModel nodeModel) {
		if (0 == viewIndex) {
			return new MarvinSketchNodeMolView(nodeModel, false);
		} else if (1 == viewIndex) {
			return new MarvinSketchNodeMolView(nodeModel, true);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getNrNodeViews() {
		return 2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean hasDialog() {
		return true;
	}

}
