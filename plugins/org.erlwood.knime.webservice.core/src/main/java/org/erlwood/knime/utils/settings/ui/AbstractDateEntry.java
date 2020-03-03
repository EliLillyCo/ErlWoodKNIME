/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2017 Eli Lilly and Company Limited
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

import java.awt.Component;
import java.awt.Container;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.erlwood.knime.utils.gui.togglepanel.TogglePanel;
import org.erlwood.knime.utils.settings.AbstractDateSetting;
import org.erlwood.knime.utils.settings.AbstractSettings;
import org.erlwood.knime.utils.settings.ui.AbstractSettingsPanel;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.DateInputDialog;

/** UI for date column selection/value entry.
 * @author Tom Wilkin */
@SuppressWarnings("serial")
public abstract class AbstractDateEntry<T extends AbstractDateSetting, S extends AbstractSettings<?>> extends AbstractSettingsPanel {
	
	/** The combo box containing the column name selection. */
	private JComboBox<DataColumnSpec> columnName;
	
	/** The date selection dialog. */
	private DateInputDialog value;
	
	/** The toggle panel allowing the selection between the manual and table based input. */
	private TogglePanel togglePanel;
	
	protected AbstractDateEntry( ) {
		createGUI( );
	}
	
	/** Update the settings that store the values for this UI.
	 * @param settings The settings to store the values from this UI in. */
	public void updateSettings(final S settings) {
		T s = getSettings(settings);
		
		if(columnName.getSelectedItem( ) == null) {
			s.setColumnName(null);
		} else {
			s.setColumnName(((DataColumnSpec)columnName.getSelectedItem( )).getName( ));
		}
		
		s.setDate(value.getSelectedDate( ));
		s.setUseColumn(togglePanel.isSelected( ));
	}
	
	/** Update the UI based on the values from the settings.
	 * @param settings The settings to retrieve the UI values from.
	 * @param specs The specs to populate the column selection drop down. */
	public void updateUI(final S settings, final PortObjectSpec[ ] specs) {
		T s = getSettings(settings);
		
		// update the selected value
		try {
			// Issue #17498 adjust the date if the timezone has a negative offset to UTC
			Date date = s.getDate();
			if(TimeZone.getDefault().getOffset(System.currentTimeMillis()) < 0) {
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTime(date);
				cal.add(Calendar.DATE, 1);
				date = cal.getTime();
			}
			
			value.setDateAndMode(date, DateInputDialog.Mode.NOTIME);
		} catch(InvalidSettingsException e) {
			// do not update UI with value
		}
		
		// update the column name selection
		@SuppressWarnings("unchecked")
		Map<String, DataColumnSpec> columnMap = getColumnSpecMap(specs, DateAndTimeValue.class);
		columnName.setModel(new DefaultComboBoxModel<DataColumnSpec>(
				columnMap.values( ).toArray(new DataColumnSpec[0])
		));		
		if(s.getColumnName( ) != null) {
			columnName.setSelectedItem(columnMap.get(s.getColumnName( )));
		}
		
		togglePanel.setSelected(s.isUseColumn( ));
	}
	
	/** Create the GUI for this entry UI. */
	protected void createGUI( ) {
		setLayout(new TableLayout(new double[ ][ ] {
				{ getLabelWidth( ), 20, TableLayout.FILL },
				{ TableLayout.FILL }
		}));
		
		columnName = new JComboBox<DataColumnSpec>( );
		
		// create date dialog, and retrieve date container to improve display
		value = new DateInputDialog(DateInputDialog.Mode.NOTIME, false);
		Component c = ((Container)value.getComponent(0)).getComponent(0);
		
		togglePanel = new TogglePanel(
				c, TogglePanel.InputType.MANUAL, columnName, TogglePanel.InputType.TABLE, 50
		);
		
		add(new JLabel(getLabel( )), "0,0");
		add(togglePanel, "2,0");
	}
	
	/** Retrieve the settings for the date entry.
	 * @param settings The settings to retrieve the date entry specific settings from.
	 * @return The the settings for the date entry. */
	protected abstract T getSettings(final S settings);

	/** @return The label for this date entry. */
	protected abstract String getLabel( );
	
	/**@return The width for the label in the GUI. */
	protected double getLabelWidth( ) {
		return TableLayout.PREFERRED;
	}
	
}
