/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2017 Eli Lilly and Company Limited
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
package org.erlwood.knime.utils.clients.soap;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Utility for searching an XML document.
 * @author Tom Wilkin*/
public final class XMLUtility {
	
	/** Find children of the specified node matching the tag name.
	 * @param node The node to start searching from.
	 * @param tag The tag name to search for.
	 * @param recurse Whether to recursively search the children as well.
	 * @return The list of nodes with the specified tag name. */
	public static List<Node> findChildren(final Node node, final String tag, final boolean recurse) {
		List<Node> nodes = new ArrayList<>();
		findChildren(node, tag, nodes, recurse);
		return nodes;
	}
	
	/** Find children of the specified node matching the tag name one level deep.
	 * @param node The node to start searching from.
	 * @param tag The tag name to search for.
	 * @return The list of nodes with the specified tag name. */
	public static List<Node> findChildren(final Node node, final String tag) {
		return findChildren(node, tag, false);
	}
	
	/** Find children of the specified node matching the tag name.
	 * @param node The node to start searching from.
	 * @param tag The tag name to search for.
	 * @param nodes The list of nodes to add the matching results to.
	 * @param recurse Whether to recursively search the children as well. */
	private static void findChildren(final Node node, final String tag, final List<Node> nodes, final boolean recurse) {			
		NodeList children = node.getChildNodes();
		for(int i=0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if(tag != null && tag.equalsIgnoreCase(child.getNodeName())) {
				nodes.add(child);
			}
			
			if(recurse) {
				findChildren(child, tag, nodes, recurse);
			}
		}
	}

}
