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

import org.knime.core.node.ContextAwareNodeFactory;
import org.knime.core.node.NodeModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Abstract ContextAwareNodeFactory for all Erl Wood nodes, will add the Erl Wood node version compatibility to
 * help text.
 * @author Tom Wilkin
 *
 * @param <T> The NodeModel this factory creates.
 */
public abstract class AbstractErlWoodContextAwareNodeFactory<T extends NodeModel> 
		extends ContextAwareNodeFactory<T>
		implements HelpTextUpdater.SupportInfoUpdater
{
	
	@Override
	public synchronized void init() {
		super.init();
		
		HelpTextUpdater.addBuildInformation(super.getXMLDescription(), this, getClass());
	}

	@Override
	public void addSupportInfo(final Document doc, final Element ul) {
		// by default does nothing
	}

}
