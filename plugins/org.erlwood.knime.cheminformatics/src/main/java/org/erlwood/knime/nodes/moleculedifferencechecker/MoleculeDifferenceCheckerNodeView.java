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
package org.erlwood.knime.nodes.moleculedifferencechecker;

import org.knime.core.node.NodeView;
import org.knime.core.node.tableview.TableView;

/** Molecule Difference Checker Node View provides a way to visualise the differences between the
 * equivalent molecules in the two input tables.
 * @author Tom Wilkin */
public class MoleculeDifferenceCheckerNodeView extends NodeView<MoleculeDifferenceCheckerNodeModel> {

	/** Construct a new MoleculeDifferenceCheckerView for the given model.
	 * @param nodeModel The MoleculeDifferenceCheckerModel instance to create the view for. */
	MoleculeDifferenceCheckerNodeView(final MoleculeDifferenceCheckerNodeModel nodeModel) {
		super(nodeModel);

		createTableView(nodeModel);
	}

	@Override
	protected void onClose( ) {
		// not required
	}

	@Override
	protected void onOpen( ) {
		// not required
	}

	@Override
	protected void modelChanged( ) {
		createTableView(getNodeModel( ));
	}
	
	/** Create the TableView and set it as the component of this view.
	 * @param nodeModel The Model to extract the differences data from. */
	private void createTableView(final MoleculeDifferenceCheckerNodeModel nodeModel) {
		TableView tableView = new TableView(nodeModel.getTableContentModel( ));
		setComponent(tableView);
	}

};
