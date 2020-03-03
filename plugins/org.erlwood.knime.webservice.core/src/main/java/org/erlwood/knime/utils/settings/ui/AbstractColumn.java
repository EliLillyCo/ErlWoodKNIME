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
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.DataColumnSpecListCellRenderer;
import org.erlwood.knime.utils.settings.AbstractColumnNameSettings;
import org.erlwood.knime.utils.settings.AbstractSettings;

/**
 * Panel used to configure the column selection.
 * @author Luke Bullard
 *
 */
public abstract class AbstractColumn <M extends Enum<M>, T extends AbstractColumnNameSettings, V extends AbstractSettings<M>> extends AbstractSettingsPanel {
	/**
	 * Serial Id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The  column selector.
	 */
	private final JComboBox<DataColumnSpec> cboColumnName = new JComboBox<DataColumnSpec>();

	private JLabel columnLabel;
	
	public abstract M getMethod();
	
	protected abstract T getSettings(V settings);
	protected abstract String getLabel();
		
	/**
	 * Constructor.
	 */
	public AbstractColumn() {	
		
		if (useLabel()) {
			setLayout(new TableLayout(new double[][] {{getLabelWidth(), 20, TableLayout.FILL}, {TableLayout.PREFERRED, TableLayout.FILL}}));
			columnLabel = new JLabel(getLabel());
			add(columnLabel, 		"0,0");
			add(cboColumnName, 		"2,0");
		} else {
			setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, TableLayout.FILL}}));					
			add(cboColumnName, 				"0,0");
		}
		cboColumnName.setRenderer(new DataColumnSpecListCellRenderer());
	}
	
	protected JComboBox<DataColumnSpec> getComboBox() {
		return cboColumnName;
	}
	
	public void addItemListener(ItemListener l) {
		cboColumnName.addItemListener(l);
	}
	
	protected boolean useLabel() {
		return true;
	}
	
	protected double getLabelWidth() {
		return TableLayout.PREFERRED;
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
		
		s.setColumnName(getComboName(cboColumnName));	
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateUI(final V settings, final PortObjectSpec[ ] specs) {
		T s = getSettings(settings);
		
		if (specs[getInputPortNumber()] == null) {
			cboColumnName.setSelectedIndex(-1);
			cboColumnName.setEnabled(false);
			return;
		}
		cboColumnName.setEnabled(true);
		Map<String, DataColumnSpec> columnMap = getColumnSpecMap(specs, getDataValues());
		
		cboColumnName.setModel(new DefaultComboBoxModel<DataColumnSpec>(columnMap.values().toArray(new DataColumnSpec[0])));
		
		if (s.getColumnName() != null) {
			cboColumnName.setSelectedItem(columnMap.get(s.getColumnName()));
		} else {
			cboColumnName.setSelectedIndex(-1);
		}
	}
	
	
	public void setLabelText(String txt) {
		if (columnLabel != null) {
			columnLabel.setText(txt);
		}
		
	}

	public void disableComponent(boolean b) {
		cboColumnName.setEnabled(!b);
	}
	
	public String getColumnName() {
		return getComboName(cboColumnName);
	}

}
