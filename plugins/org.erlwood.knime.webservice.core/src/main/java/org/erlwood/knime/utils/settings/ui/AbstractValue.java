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

import javax.swing.JLabel;

import org.erlwood.knime.utils.gui.HintTextField;
import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.port.PortObjectSpec;

import org.erlwood.knime.utils.settings.AbstractSettings;
import org.erlwood.knime.utils.settings.AbstractValueSettings;

/**
 * Panel used to configure a single value.
 * @author Luke Bullard
 *
 */
public abstract class AbstractValue <M extends Enum<M>, T extends AbstractValueSettings, V extends AbstractSettings<M>> extends AbstractSettingsPanel {
	/**
	 * Serial Id.
	 */
	private static final long serialVersionUID = 1L;
		
	/**
	 * The value text column.
	 */
	private final HintTextField txtValue;
		
	public abstract M getMethod();
	
	protected abstract T getSettings(V settings);
	protected abstract String getLabel();
	protected abstract String getHintText();
	
	/**
	 * Constructor.
	 */
	public AbstractValue() {	
		txtValue = getValueTextField();
		txtValue.setHint(getHintText());
		
		
		setLayout(new TableLayout(new double[][] {{getLabelWidth(), 20, TableLayout.FILL}, {TableLayout.PREFERRED, TableLayout.FILL}}));
		
		
		add(new JLabel(getLabel()), "0,0");
		add(txtValue, 				"2,0");
				
		
	}
	
	protected double getLabelWidth() {
		return TableLayout.PREFERRED;
	}
	
	protected HintTextField getValueTextField() {		
		return new HintTextField();
	}
	
	protected Class<? extends DataValue> getDefaultDataValue() {
		return StringValue.class;
	}
	
	@SuppressWarnings("unchecked")
	protected Class<? extends DataValue>[] getDataValues() {
		return new Class[] { StringValue.class };
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void updateSettings(V settings) {
		T s = getSettings(settings);				
		s.setValue(txtValue.getText());		
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateUI(final V settings, final PortObjectSpec[ ] specs) {
		T s = getSettings(settings);
					
		txtValue.setText(s.getValue());		
	}
	
	public void disableComponent(boolean b) {
		txtValue.setEnabled(!b);
	}

}
