/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2018 Eli Lilly and Company Limited
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
package org.erlwood.knime.utils.node;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Utility class for updating node help text with dynamic content.
 * @author Tom Wilkin
 */
public final class HelpTextUpdater {
	
	/**
	 * Interface for the dynamic SupportInfo update.
	 */
	public static interface SupportInfoUpdater {
		
		/**
		 * Add additional support information to the list.
		 * @param doc The XML document to update.
		 * @param ul The ul element to add li elements to.
		 */
		public void addSupportInfo(final Document doc, final Element ul);
		
	}
	
	/** The string for the support information header. */
	private static final String SUPPORT_INFO_LABEL = "Support Information:";
	
	/** Cannot instantiate utiliti class. */
	private HelpTextUpdater() { }
	
	/**
	 * Add an list item (li) to the specified list (ul).
	 * @param doc The document the list item is added to.
	 * @param ul The list to add the item to.
	 * @param heading The optional heading for the list item.
	 * @param content The optional content for the list item.
	 * @return The list item added to the list.
	 */
	public static Element addListItem(final Document doc, final Element ul, final String heading, final Node content) {
		Element li = doc.createElement("li");
		
		if(heading != null) {
			Element b = doc.createElement("b");
			b.appendChild(doc.createTextNode(heading + ": "));
			li.appendChild(b);
		}
		
		if(content != null) {
			li.appendChild(content);
		}
		
		ul.appendChild(li);
		return li;
	}
	
	/**
	 * Add the build information to the help text.
	 * @param root The root of the document to update.
	 * @param updater The SupportInfoUpdater to delegate updates to.
	 * @param clazz The class of the plugin bundle to retrieve for.
	 */
	static void addBuildInformation(final Element root, final SupportInfoUpdater updater, final Class<?> clazz) {
		if(root != null) {
			Document doc = root.getOwnerDocument();
			
			// check if this has already run
			NodeList nodes = root.getElementsByTagName("h4");
			for(int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if(node != null && node.hasChildNodes() 
						&& SUPPORT_INFO_LABEL.equals(node.getFirstChild().getNodeValue()))
				{
					// it has already run, so don't add it again
					return;
				}
			}
			
			// add the support info
			Element supportInfo = doc.createElement("span");
			
			// add the heading
			Element header = doc.createElement("h4");
			header.appendChild(doc.createTextNode(SUPPORT_INFO_LABEL));
			supportInfo.appendChild(header);
			
			// add the list
			Element ul = doc.createElement("ul");
			if(updater != null) {
				updater.addSupportInfo(doc, ul);
			}
			Bundle bundle = FrameworkUtil.getBundle(clazz);
			Version version = bundle.getVersion();
			addListItem(doc, ul, null, 
					doc.createTextNode(
						"Optimised for KNIME " + version
					)
			);
			supportInfo.appendChild(ul);
			
			// add to the intro
			nodes = root.getElementsByTagName("intro");
			if(nodes.item(0) != null) {
				nodes.item(0).appendChild(supportInfo);
			}
		}
	}

}
