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
import java.lang.reflect.ParameterizedType;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.erlwood.knime.utils.settings.AbstractSettings;
import org.erlwood.knime.utils.settings.AbstractValueSettings;
import org.knime.core.node.port.PortObjectSpec;

/**
 * Panel used to configure a selection from a Enumeration.
 * @author Luke Bullard
 *
 */
public abstract class AbstractEnumValue <M extends Enum<M>, P extends Enum<P>, T extends AbstractValueSettings, V extends AbstractSettings<M>> extends AbstractSettingsPanel {
	/**
	 * Serial Id.
	 */
	private static final long serialVersionUID = 1L;
		
	/**
	 * The value combo.
	 */
	private JComboBox<P> cboEnumValue = new JComboBox<P>();

	private Class<P> typeOfT;
		
	public abstract M getMethod();
	
	protected abstract T getSettings(V settings);
	protected abstract String getLabel();
	
	/**
	 * Constructor.
	 */
	@SuppressWarnings("unchecked")
	public AbstractEnumValue() {	
				
		setLayout(new TableLayout(new double[][] {{getLabelWidth(), 20, TableLayout.FILL}, {TableLayout.PREFERRED, TableLayout.FILL}}));
		
		
		add(new JLabel(getLabel()), "0,0");
		add(cboEnumValue, 			"2,0");
		
		this.typeOfT = (Class<P>)
                ((ParameterizedType)getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[1];
		
		P[] m = typeOfT.getEnumConstants();
		
		
		cboEnumValue.setModel(new DefaultComboBoxModel(m));
        
		/*
		cboEnumValue.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                	P v = (P) cboEnumValue.getSelectedItem();
                    cardLayout.show(cardPanel, method.name());                    
                }
                
            }
        });
				
		*/
	}
	
	protected double getLabelWidth() {
		return TableLayout.PREFERRED;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void updateSettings(V settings) {
		T s = getSettings(settings);		
		
		P v = (P) cboEnumValue.getSelectedItem();
		s.setValue(v.name());		
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateUI(final V settings, final PortObjectSpec[ ] specs) {
		T s = getSettings(settings);

		P[] m = typeOfT.getEnumConstants();
		P eV = m[0];
		
		String v = s.getValue();
		if (v != null) {
			eV = Enum.valueOf(typeOfT, v);
		}
		cboEnumValue.setSelectedItem(eV);		
	}
	
	public void disableComponent(boolean b) {
		cboEnumValue.setEnabled(!b);
	}

	public void addItemListener(ItemListener itemListener) {
		cboEnumValue.addItemListener(itemListener);
	}

}
