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
package org.erlwood.knime.nodes.openphacts.ui.general;

import org.erlwood.knime.nodes.openphacts.OpenPhactsNodeModel;
import org.erlwood.knime.nodes.openphacts.OpenPhactsNodeModel.eMethod;
import org.erlwood.knime.nodes.openphacts.settings.OpenPhactsSettings;
import org.erlwood.knime.nodes.openphacts.settings.general.FreeTextSearchSettings;
import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.erlwood.knime.utils.settings.ui.AbstractMoleculeColumnAndValue;
import org.erlwood.knime.utils.settings.ui.AbstractSettingsPanel;
import org.erlwood.knime.utils.settings.ui.ISettingsPanel;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.port.PortObjectSpec;

/**
 * Panel used to configure the GetAssay OpenPhacts call.
 * @author Luke Bullard
 *
 */
public class FreeTextSearch extends AbstractSettingsPanel implements ISettingsPanel<OpenPhactsNodeModel.eMethod, OpenPhactsSettings> {
	/**
	 * Serial Id.
	 */
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("serial")
	private AbstractMoleculeColumnAndValue<OpenPhactsNodeModel.eMethod, FreeTextSearchSettings.FreeTextSetting, OpenPhactsSettings> text = new AbstractMoleculeColumnAndValue<OpenPhactsNodeModel.eMethod, FreeTextSearchSettings.FreeTextSetting, OpenPhactsSettings>() {

		@Override
		public eMethod getMethod() {
			return null;
		}

		@Override
		protected FreeTextSearchSettings.FreeTextSetting getSettings(OpenPhactsSettings settings) {
			return settings.getFreeTextSearchSettings().getFreeTextSettings();
		}

		@Override
		protected String getLabel() {
			return "Search Text";
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getHintText() {
			return "Please enter a Search Term";
		}
		
		protected Class<? extends DataValue> getDefaultDataValue() {
			return StringValue.class;
		};
		
		@SuppressWarnings("unchecked")
		protected Class<? extends DataValue>[] getDataValues() {
			return new Class[] { StringValue.class };			
		};
		
		protected double getLabelWidth() {
			return 110d;
		};
	};
	
	
	
	/**
	 * Constructor.
	 */
	public FreeTextSearch() {	
		
		setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.FILL}}));
				
		add(text, "0,0");
		
	}
	
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public eMethod getMethod() {
		return eMethod.FreeTextSearch;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateSettings(OpenPhactsSettings settings) {
		text.updateSettings(settings);			
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateUI(final OpenPhactsSettings settings, final PortObjectSpec[ ] specs) {
		text.updateUI(settings, specs);		
	}

}
