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
package org.erlwood.knime.nodes.tabletostring;

import org.erlwood.knime.utils.node.AbstractErlWoodNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;

/** Node Factory for the Table To String node.
 * @author Tom Wilkin */
public class TableToStringNodeFactory extends AbstractErlWoodNodeFactory<TableToStringNodeModel> {

	@Override
	public TableToStringNodeModel createNodeModel( ) {
		return new TableToStringNodeModel( );
	}

	@Override
	protected int getNrNodeViews( ) {
		return 0;
	}

	@Override
	public NodeView<TableToStringNodeModel> createNodeView(
			final int viewIndex, final TableToStringNodeModel nodeModel)
	{
		return null;
	}

	@Override
	protected boolean hasDialog( ) {
		return false;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane( ) {
		return null;
	}

};
