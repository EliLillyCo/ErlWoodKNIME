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
package org.erlwood.knime.icons;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * A class with one static method which loads the icon specified by its file
 * name.
 * 
 * @author Dimitar Hristozov
 */
public final class IconLoader {
	public static ImageIcon loadIcon(final String name) {		
		URL url = IconLoader.class.getResource(name);
		return null != url ? new ImageIcon(url) : null;
	}

	public static InputStream loadIconStream(final String name) throws IOException {
		URL url = IconLoader.class.getResource(name);
		return null != url ? url.openStream() : null;
	}

	private IconLoader() {
	}
}
