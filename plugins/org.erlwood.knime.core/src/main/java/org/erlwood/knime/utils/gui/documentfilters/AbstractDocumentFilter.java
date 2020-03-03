/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2015 Eli Lilly and Company Limited
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
package org.erlwood.knime.utils.gui.documentfilters;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public abstract class AbstractDocumentFilter extends DocumentFilter {
	protected abstract void parse(String s);
	
	@Override
	public void insertString(FilterBypass filterbypass, int i, String s,
			AttributeSet attributeset) throws BadLocationException {
		try {
			parse(s);
		} catch(NumberFormatException ex) {
			return;
		}
		super.insertString(filterbypass, i, s, attributeset);
	}

	@Override
	public void replace(FilterBypass filterbypass, int offset, int length, String s,
			AttributeSet attributeset) throws BadLocationException {
		StringBuilder s1 = new StringBuilder(filterbypass.getDocument().getText(0, filterbypass.getDocument().getLength()));
		try {
			if (offset >= s1.length()) {
				s1.append(s);
			} else {
				s1.replace(offset, offset + length, s);
			}
			
			parse(s1.toString());
		} catch(NumberFormatException ex) {
			return;
		}
		super.replace(filterbypass, offset, length, s, attributeset);
	}
	
}