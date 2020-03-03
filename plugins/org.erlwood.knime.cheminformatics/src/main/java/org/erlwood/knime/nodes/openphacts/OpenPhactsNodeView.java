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

import java.awt.CardLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.erlwood.knime.utils.gui.XMLTreeViewer;
import org.knime.core.data.DataCell;
import org.knime.core.data.json.JSONBlobCell;
import org.knime.core.data.json.JSONCell;
import org.knime.core.data.xml.XMLBlobCell;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.node.NodeView;
import org.knime.core.node.tableview.TableView;

import com.doitnext.swing.widgets.json.JSONEditPanel;
import com.doitnext.swing.widgets.json.JSONEditPanel.UpdateType;

/**
 * View to display JSON or XML data in a more digestable fashion.
 * @author Luke Bullard
 *
 */
public class OpenPhactsNodeView extends NodeView<OpenPhactsNodeModel> {
	private CardLayout 		cardLayout;
	private JPanel 			cardPanel;
	private TableView 		tableView 	= new TableView();
	private JSONEditPanel 	jsonPanel 	= new JSONEditPanel();
	private XMLTreeViewer 	xmlPanel 	= new XMLTreeViewer(); 
	private JSplitPane 		splitPane;
			
	protected OpenPhactsNodeView(OpenPhactsNodeModel nodeModel) {
		super(nodeModel);
		cardLayout = new CardLayout();		
		cardPanel = new JPanel(cardLayout);
		
		cardPanel.add(new JPanel(), "");
		cardPanel.add(jsonPanel, JSONCell.class.getName());
		cardPanel.add(xmlPanel, XMLCell.class.getName());
		
		createTableView();
		ListSelectionModel selectionModel = tableView.getContentTable().getColumnModel().getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent evt) {
				if (!evt.getValueIsAdjusting()) {
					int row = evt.getFirstIndex();
					Object obj = null;
					TableModel tm = tableView.getContentTable().getModel();
					
					for (int i =0; i < tm.getColumnCount(); i++) {
						if (tm.getColumnName(i).equals("OPS_Output")) {
							obj = tm.getValueAt(row, i);
							break;
						}
					}
					if (obj == null) {
						cardLayout.show(cardPanel, "");
						return;
					}
					
					DataCell cell;
					String className = "";
					
					if (obj instanceof XMLCell) {
						
						cell = (XMLCell) obj;
						xmlPanel.setDocument(((XMLCell) cell).getDocument());
						className = XMLCell.class.getName();
						
					} else if (obj instanceof XMLBlobCell) {
						
						cell = (XMLBlobCell) obj;
						xmlPanel.setDocument(((XMLBlobCell) cell).getDocument());
						className = XMLCell.class.getName();
						
					} else if (obj instanceof JSONBlobCell){
						
						cell = (JSONBlobCell) obj;				
						className = JSONCell.class.getName();
						jsonPanel.setJson(((JSONBlobCell) cell).getStringValue(), UpdateType.REPLACE);
						
					} else {
						
						cell = (JSONCell) obj;					
						jsonPanel.setJson(((JSONCell) cell).getStringValue(), UpdateType.REPLACE);
						className = JSONCell.class.getName();
						
					}
					
					cardLayout.show(cardPanel, className);
				}
			}
		});
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableView, cardPanel);
		splitPane.setDividerLocation(300);
		setComponent(splitPane);
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
		// not required
	}
	
	/**
	 * Create the TableView and set it as the component of this view.
	 */
	private void createTableView() {
		tableView.setDataTable(getNodeModel().getRawDataTable());
	}
}
