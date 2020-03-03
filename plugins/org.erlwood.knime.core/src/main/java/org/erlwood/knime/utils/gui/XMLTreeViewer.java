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
package org.erlwood.knime.utils.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Class used to visualize XML data.
 * @author Luke Bullard
 *
 */
@SuppressWarnings("serial")
public class XMLTreeViewer extends JPanel {

	
	/** The JTree to display the XML. **/
	private JTree xmlTree;

	/** The Root node. **/
	private DefaultMutableTreeNode tn;

	/**
	 * Constructor.
	 */
	public XMLTreeViewer() {
		super();
		xmlTree = new JTree();
		xmlTree.setName("XML Tree");
		setLayout(new BorderLayout());
		add(new JScrollPane(xmlTree), BorderLayout.CENTER);

		tn = new DefaultMutableTreeNode("XML");
		((DefaultTreeModel) xmlTree.getModel()).setRoot(tn);
	}

	/**
	 * Sets the XML document for this tree.
	 * @param document The new Document to visualise.
	 */
	public void setDocument(Document document) {
		tn = new DefaultMutableTreeNode("XML");
		processElement(document.getDocumentElement(), tn);
		((DefaultTreeModel) xmlTree.getModel()).setRoot(tn);
	}

	/**
	 * Process an XML element. 
	 * @param el The Element to process
	 * @param dmtn The TreeNode to add to.
	 */
	private void processElement(Element el, DefaultMutableTreeNode dmtn) {
		DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(el.getNodeName());
		
		processAttributes(el, currentNode);
		
		for (int i = 0; i < el.getChildNodes().getLength(); i++) {
			Node n = el.getChildNodes().item(i);
			if (n instanceof Element) {
				processElement((Element) n, currentNode);
			} else {
				currentNode.setUserObject(currentNode.getUserObject() + ": " + n.getTextContent());
			}
		}

		dmtn.add(currentNode);
	}

	/**
	 * Process an XML element for attribute data. 
	 * @param el The Element to process
	 * @param dmtn The TreeNode to add to.
	 */
	private void processAttributes(Element el, DefaultMutableTreeNode dmtn) {
		NamedNodeMap nnm = el.getAttributes();
		for (int i = 0; i < nnm.getLength(); i++) {
			Node n = nnm.item(i);
			DefaultMutableTreeNode attNode = new DefaultMutableTreeNode(n.getNodeName() + ": " + n.getNodeValue());			
			dmtn.add(attNode);

		}
	}
}
