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
package org.erlwood.knime.nodes.openphacts.ui.chemicalstructuresearch;

import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataValue;
import org.knime.core.node.port.PortObjectSpec;

import org.erlwood.knime.nodes.openphacts.OpenPhactsNodeModel;
import org.erlwood.knime.nodes.openphacts.OpenPhactsNodeModel.eMethod;
import org.erlwood.knime.nodes.openphacts.exec.chemicalstructuresearch.GetCompoundsBySubstructureExec;
import org.erlwood.knime.nodes.openphacts.settings.OpenPhactsSettings;
import org.erlwood.knime.nodes.openphacts.settings.chemicalstructuresearch.GetCompoundsBySubstructureSettings;
import org.erlwood.knime.utils.settings.ui.AbstractMoleculeColumnAndValue;
import org.erlwood.knime.utils.settings.ui.AbstractEnumValue;
import org.erlwood.knime.utils.settings.ui.AbstractSettingsPanel;
import org.erlwood.knime.utils.settings.ui.ISettingsPanel;

/**
 * Panel used to configure the GetCompoundsBySimilarity OpenPhacts call.
 * @author Luke Bullard
 *
 */
public class GetCompoundsBySubstructure extends AbstractSettingsPanel implements ISettingsPanel<OpenPhactsNodeModel.eMethod, OpenPhactsSettings> {
	/**
	 * Serial Id.
	 */
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("serial")
	private AbstractMoleculeColumnAndValue<OpenPhactsNodeModel.eMethod, GetCompoundsBySubstructureSettings.SmilesSetting, OpenPhactsSettings> smiles = new AbstractMoleculeColumnAndValue<OpenPhactsNodeModel.eMethod, GetCompoundsBySubstructureSettings.SmilesSetting, OpenPhactsSettings>() {

		@Override
		public eMethod getMethod() {
			return null;
		}

		@Override
		protected GetCompoundsBySubstructureSettings.SmilesSetting getSettings(OpenPhactsSettings settings) {
			return settings.getCompoundsBySubstructureSettings().getSmilesSettings();
		}

		@Override
		protected String getLabel() {
			return "Smiles";
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getHintText() {
			return "Please enter Smiles string";
		}
		
		protected Class<? extends DataValue> getDefaultDataValue() {
			return SmilesValue.class;
		};
		
		protected Class<? extends DataValue>[] getDataValues() {
			return MoleculeDataTypeConverter.getConvertibleTypes(SmilesValue.class);			
		};
		
		protected double getLabelWidth() {
			return 110d;
		};
	};
	
	@SuppressWarnings("serial")
	private AbstractEnumValue<OpenPhactsNodeModel.eMethod, GetCompoundsBySubstructureExec.eMolType, GetCompoundsBySubstructureSettings.MoleculeTypeSetting, OpenPhactsSettings> molType = new AbstractEnumValue<OpenPhactsNodeModel.eMethod, GetCompoundsBySubstructureExec.eMolType, GetCompoundsBySubstructureSettings.MoleculeTypeSetting, OpenPhactsSettings>() {

		@Override
		public eMethod getMethod() {
			return null;
		}

		@Override
		protected GetCompoundsBySubstructureSettings.MoleculeTypeSetting getSettings(OpenPhactsSettings settings) {
			return settings.getCompoundsBySubstructureSettings().getMoleculeTypeSettings();
		}

		@Override
		protected String getLabel() {
			return "Molecule Type";
		}
		
		protected double getLabelWidth() {
			return 110d;
		};
	};
	
	
	
	/**
	 * Constructor.
	 */
	public GetCompoundsBySubstructure() {	
		
		setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.FILL}}));
				
		add(smiles,					"0,0");
		add(molType,		 		"0,2");
		
	}
	
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public eMethod getMethod() {
		return eMethod.GetCompoundsBySubstructure;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateSettings(OpenPhactsSettings settings) {
		smiles.updateSettings(settings);
		molType.updateSettings(settings);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateUI(final OpenPhactsSettings settings, final PortObjectSpec[ ] specs) {
		smiles.updateUI(settings, specs);
		molType.updateUI(settings, specs);		
	}

}
