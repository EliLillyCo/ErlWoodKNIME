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
package org.erlwood.knime.utils.settings.ui;

import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.knime.core.node.port.PortObjectSpec;

import org.erlwood.knime.utils.settings.AbstractSettings;
import org.erlwood.knime.utils.settings.AbstractValueSettings;

/**
 * Panel used to configure a boolean value.
 * @author Luke Bullard
 *
 */
public abstract class AbstractBooleanValue <M extends Enum<M>, T extends AbstractValueSettings, V extends AbstractSettings<M>> extends AbstractSettingsPanel {
	/**
	 * Serial Id.
	 */
	private static final long serialVersionUID = 1L;
		
	/**
	 * The value text column.
	 */
	private final JCheckBox chkValue;
		
	public abstract M getMethod();
	
	protected abstract T getSettings(V settings);
	protected abstract String getLabel();
	
	
	/**
	 * Constructor.
	 */
	public AbstractBooleanValue() {	
		chkValue = new JCheckBox(getLabel());
				
		
		setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, TableLayout.FILL}}));
		
		add(chkValue, 				"0,0");				
		
	}
	
	public void setEnabled(boolean b) {
		chkValue.setEnabled(b);
	}
	
	public boolean isSelected() {
		return chkValue.isSelected();
	}
	
	public void setSelected(boolean b) {
		chkValue.setSelected(b);
	}
	
	protected double getLabelWidth() {
		return TableLayout.PREFERRED;
	}
	
	
	
	/**
	 * {@inheritDoc}
	 */
	public void updateSettings(V settings) {
		T s = getSettings(settings);				
		s.setValue(Boolean.toString(chkValue.isSelected()));		
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateUI(final V settings, final PortObjectSpec[ ] specs) {
		T s = getSettings(settings);
					
		chkValue.setSelected(Boolean.parseBoolean(s.getValue()));		
	}

	public void addItemListener(ItemListener itemListener) {
		chkValue.addItemListener(itemListener);
	}
	

}
