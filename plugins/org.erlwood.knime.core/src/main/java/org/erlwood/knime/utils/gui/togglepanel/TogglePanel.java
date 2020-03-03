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
package org.erlwood.knime.utils.gui.togglepanel;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.erlwood.knime.utils.gui.togglebutton.ToggleButton;
import org.erlwood.knime.icons.IconLoader;

/**
 * Panel designed to show a single component at a time, controlled by a ToggleButton (iOS style),
 * with a small amount of text/icon on the left & right side of the toggle.
 * @author Luke Bullard
 *
 */
@SuppressWarnings("serial")
public class TogglePanel extends JPanel {
	
	/** Enum containing the predefined types for the TogglePanel view.
	 * @author Tom Wilkin */
	public static enum InputType {
		/** Option for manually entered content. */
		MANUAL("M", "manual.png", "Manual user entry."),
		
		/** Option for a drop-down of options. */
		DROP_DOWN("D", "combo_box.png", "Select the option from a list of options."),
		
		//** Option for a list box of options. */
		LIST_BOX("L", "list_box.png", "Select multiple options from a list of options."),
		
		/** Option for content populated from a selected table column. */
		TABLE("T", "table.png", "Input table column selection."),
		
		/** Option for content populated from a selected flow variable. */
		FLOW_VARIABLE("V", "variable.png", "Flow variable selection.");
		
		/** The text to display if an icon is not available. */
		private String text;
		
		/** The icon to display on the panel. */
		private Image icon;
		
		/** The tooltip explaining the icon to the user. */
		private String tooltip;
		
		private InputType(final String text, final String iconPath, final String tooltip) {
			this.text = text;
			if(iconPath != null) {
				ImageIcon imageIcon = IconLoader.loadIcon(iconPath);
				if(imageIcon != null) {
					icon = imageIcon.getImage( );
				}
			} else {
				icon = null;
			}
			this.tooltip = tooltip;
		}
		
		/** @return The text to display for this option. */
		public String getText( ) {
			return text;
		}
		
		/** @return The icon to display for this option. */
		public Image getIcon( ) {
			return icon;
		}
		
		/** @return The tool tip help text for this option. */
		public String getToolTipText( ) {
			return tooltip;
		}
		
	}
	
	/**
	 * LEFT Constant.
	 */
	private static final String LEFT = "LEFT";
	
	/**
	 * RIGHT Constant.
	 */
	private static final String RIGHT = "RIGHT";
	
	/**
	 * The CardLayout.
	 */
	private final CardLayout		cardLayout 	= new CardLayout();
	
	/**
	 * The card panel.
	 */
	private final JPanel 			cardPanel 	= new JPanel(cardLayout);
	
	/**
	 * The toggle.
	 */
	private final ToggleButton		componentSelector; 
	
	/**
	 * Constructor.
	 * @param leftComponent	The left component.
	 * @param left	The toggle left type.
	 * @param rightComponent	The right component.
	 * @param right The toggle right type.
	 * @param toggleButtonWidth	The amount of space to allocate to the toggle button.
	 */
	public TogglePanel(Component leftComponent, InputType left, Component rightComponent, InputType right, double toggleButtonWidth) {
		
		
        setLayout(new TableLayout(new double[][] {{TableLayout.FILL, 5, toggleButtonWidth}, {TableLayout.FILL}}));
        
        cardPanel.add(leftComponent, 	LEFT);
        cardPanel.add(rightComponent, 	RIGHT);
        
        
        componentSelector = new ToggleButton(right, left);
        
        componentSelector.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {   
                	cardLayout.show(cardPanel, RIGHT);
                } else {
                	cardLayout.show(cardPanel, LEFT);
                }
                
            }
        });
        
        add(cardPanel,  		"0,0");
        add(createToggleButtonPanel( ),  "2,0");
        
        cardLayout.show(cardPanel, LEFT);
	}
	
	private JPanel createToggleButtonPanel( ) {       
        JPanel panel = new JPanel(new TableLayout(new double[ ][ ] {
        		{ TableLayout.FILL },
        		{ TableLayout.PREFERRED, TableLayout.FILL }
        }));
        panel.add(componentSelector, "0,0");
        return panel;
	}

	/** 
	 * @return Are we in a selected state ?
	 */
	public boolean isSelected() {
		return componentSelector.isSelected();
	}

	/**
	 * Sets the selection state.
	 * @param selected The new selection state.
	 */
	public void setSelected(boolean selected) {
		componentSelector.setSelected(selected);
	}
	
	/** Add a ChangeListener to the ToggleButton control.
	 * @param listener The listener to add to the ToggleButton. */
	public void addToggleChangeListener(final ChangeListener listener) {
		componentSelector.addChangeListener(listener);
	}
	
}
