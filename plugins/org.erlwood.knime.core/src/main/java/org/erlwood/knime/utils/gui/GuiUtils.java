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

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.erlwood.knime.utils.gui.layout.TableLayout;


/**
 * General purpose GUI utilities.
 * @author Luke Bullard
 *
 */
public final class GuiUtils {
    /**
     * Private constructor for utility class.
     */
    private GuiUtils(){}
    
    /**
     * Creates and returns a JLabel with the font style set to Bold.
     * @param s The text for the JLabel
     * @return A bold JLabel
     */
    public static JLabel createBoldLabel(String s) {
    	JLabel lbl = new JLabel(s);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        return lbl;
    }
    
    /**
     * Create an underlined bold label.
     * @param label The text to use. 
     * @return A JPanel
     */
    public static JPanel getHeaderPanel(String label) {
        JPanel p = new JPanel();
        JLabel lbl = createBoldLabel(label);
        
        p.setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, 2, 2}}));
        
        p.add(lbl, "0,0");
        p.add(new HorizontalLine(), "0,2");
        
        return p;
    }
     
}
