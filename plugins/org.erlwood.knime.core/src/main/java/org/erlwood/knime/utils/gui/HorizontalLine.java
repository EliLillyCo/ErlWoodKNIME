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

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Draws a horizontal line.
 * @author Luke Bullard
 *
 */
@SuppressWarnings("serial")
public class HorizontalLine extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.lightGray);
        g.drawLine(0, 0, getWidth() - 1, 0);
        
        g.setColor(Color.white);
        g.drawLine(0, 1, getWidth() - 1, 1);
    }
}