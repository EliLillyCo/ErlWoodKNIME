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
package org.erlwood.knime.nodes.openphacts;

import java.awt.CardLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.erlwood.knime.nodes.openphacts.OpenPhactsNodeModel.eMethod;
import org.erlwood.knime.nodes.openphacts.settings.OpenPhactsSettings;
import org.erlwood.knime.nodes.openphacts.ui.assay.GetAssay;
import org.erlwood.knime.nodes.openphacts.ui.chemicalstructuresearch.GetCompoundURI;
import org.erlwood.knime.nodes.openphacts.ui.chemicalstructuresearch.GetCompoundsBySimilarity;
import org.erlwood.knime.nodes.openphacts.ui.chemicalstructuresearch.GetCompoundsBySubstructure;
import org.erlwood.knime.nodes.openphacts.ui.classes.GetChildClasses;
import org.erlwood.knime.nodes.openphacts.ui.classes.GetParentClasses;
import org.erlwood.knime.nodes.openphacts.ui.classes.GetRootClasses;
import org.erlwood.knime.nodes.openphacts.ui.compound.GetCompound;
import org.erlwood.knime.nodes.openphacts.ui.compound.GetCompoundClasses;
import org.erlwood.knime.nodes.openphacts.ui.compound.GetCompoundPharmacology;
import org.erlwood.knime.nodes.openphacts.ui.compound.GetCompoundsByClass;
import org.erlwood.knime.nodes.openphacts.ui.disease.GetDisease;
import org.erlwood.knime.nodes.openphacts.ui.disease.GetDiseaseAssociations;
import org.erlwood.knime.nodes.openphacts.ui.disease.GetDiseasesByTarget;
import org.erlwood.knime.nodes.openphacts.ui.disease.GetTargetAssociations;
import org.erlwood.knime.nodes.openphacts.ui.disease.GetTargetsByDisease;
import org.erlwood.knime.nodes.openphacts.ui.general.FreeTextSearch;
import org.erlwood.knime.nodes.openphacts.ui.target.GetTarget;
import org.erlwood.knime.nodes.openphacts.ui.target.GetTargetClasses;
import org.erlwood.knime.nodes.openphacts.ui.target.GetTargetCompoundClasses;
import org.erlwood.knime.nodes.openphacts.ui.target.GetTargetPharmacology;
import org.erlwood.knime.nodes.openphacts.ui.target.GetTargetPharmacologyByClass;
import org.erlwood.knime.nodes.openphacts.ui.target.GetTargetsByClass;
import org.erlwood.knime.utils.gui.AbstractMethodSelectionComboBox;
import org.erlwood.knime.utils.gui.GuiUtils;
import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.erlwood.knime.utils.settings.ui.AbstractEnumValue;
import org.erlwood.knime.utils.settings.ui.AbstractValue;
import org.erlwood.knime.utils.settings.ui.ISettingsPanel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;

/**
 * The tab to add to the OpenPhacts dialog.
 * @author Luke Bullard
 *
 */
@SuppressWarnings("serial")
final class OpenPhactsTab extends JPanel {
	
	private OpenPhactsSettings	openPhactsSettings = new OpenPhactsSettings();
	
	private class MyMethodSelectionComboBox extends AbstractMethodSelectionComboBox<OpenPhactsNodeModel.eMethod> {

		@Override
		protected void itemSelected(eMethod item) {
			cardLayout.show(cardPanel, item.name());
		}
		
	};
	
	private MyMethodSelectionComboBox cboMethodType = new MyMethodSelectionComboBox(); 
	private CardLayout	cardLayout 	= new CardLayout();
	private JPanel 		cardPanel 	= new JPanel(cardLayout);

	private JCheckBox	chkAppend = new JCheckBox("Append");
	
	@SuppressWarnings("unchecked")
	private ISettingsPanel<OpenPhactsNodeModel.eMethod, OpenPhactsSettings>[]	panels = new ISettingsPanel[] {
			new GetAssay(),
			
			new GetCompoundURI(),
			new GetCompoundsBySimilarity(),
			new GetCompoundsBySubstructure(),
			
			new GetCompound(),
			new GetCompoundClasses(),
			new GetCompoundsByClass(),
			new GetCompoundPharmacology(),
			
			new GetRootClasses(),
			new GetChildClasses(),
			new GetParentClasses(),
			
			new GetDisease(),
			new GetDiseaseAssociations(),
			new GetTargetAssociations(),
			new GetDiseasesByTarget(),
			new GetTargetsByDisease(),
			
			new GetTarget(),
			new GetTargetClasses(),
			new GetTargetCompoundClasses(),
			new GetTargetsByClass(),
			new GetTargetPharmacology(),
			new GetTargetPharmacologyByClass(),
			new FreeTextSearch(),
		
	};
	
	
	private AbstractValue<OpenPhactsNodeModel.eMethod, OpenPhactsSettings.ApplicationIdSetting, OpenPhactsSettings> appId = new AbstractValue<OpenPhactsNodeModel.eMethod, OpenPhactsSettings.ApplicationIdSetting, OpenPhactsSettings>() {

		@Override
		public eMethod getMethod() {
			return null;
		}

		@Override
		protected OpenPhactsSettings.ApplicationIdSetting getSettings(OpenPhactsSettings settings) {
			return settings.getApplicationIdSetting();
		}

		@Override
		protected String getLabel() {
			return "Application Id";
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getHintText() {
			return "Please enter OpenPhacts Application Id";
		}
		
		protected double getLabelWidth() { return 120d; };
	};
	
	private AbstractValue<OpenPhactsNodeModel.eMethod, OpenPhactsSettings.ApplicationKeySetting, OpenPhactsSettings> appKey = new AbstractValue<OpenPhactsNodeModel.eMethod, OpenPhactsSettings.ApplicationKeySetting, OpenPhactsSettings>() {

		@Override
		public eMethod getMethod() {
			return null;
		}

		@Override
		protected OpenPhactsSettings.ApplicationKeySetting getSettings(OpenPhactsSettings settings) {
			return settings.getApplicationKeySetting();
		}

		@Override
		protected String getLabel() {
			return "Application Key";
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getHintText() {
			return "Please enter OpenPhacts Application Key";
		}
		
		protected double getLabelWidth() { return 120d; };
		
	};
	
	private AbstractEnumValue<OpenPhactsNodeModel.eMethod, OpenPhactsNodeModel.eFormat, OpenPhactsSettings.ReturnFormatTypeSetting, OpenPhactsSettings> format = new AbstractEnumValue<OpenPhactsNodeModel.eMethod, OpenPhactsNodeModel.eFormat, OpenPhactsSettings.ReturnFormatTypeSetting, OpenPhactsSettings>() {

		@Override
		public eMethod getMethod() {
			return null;
		}

		@Override
		protected OpenPhactsSettings.ReturnFormatTypeSetting getSettings(OpenPhactsSettings settings) {
			return settings.getReturnFormatSetting();
		}

		@Override
		protected String getLabel() {
			return "2nd Output Port Format";
		}
					
		protected double getLabelWidth() { return 120d; };
		
	};
	
	OpenPhactsTab() {
		setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, 15, TableLayout.PREFERRED, 15, TableLayout.PREFERRED, TableLayout.FILL}}));    	
    	
    	add(getKeysPanel(),             "0,0");    	
    	add(getMethodPanel(),           "0,2");
    	add(getReturnFormatPanel(),     "0,4");
     	    	
    	setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
   
	}
	
	private JPanel getKeysPanel() {
        JPanel keysPanel = new JPanel();
        keysPanel.setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.FILL}}));
        
        keysPanel.add(GuiUtils.getHeaderPanel("OpenPhacts API Keys"),  "0,0");
        keysPanel.add(appId,                              	"0,2");
        keysPanel.add(appKey,                              	"0,4");        
      
        return keysPanel;
    }
	
	private JPanel getReturnFormatPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.FILL}}));
        
        pnl.add(GuiUtils.getHeaderPanel("Extra Return Format"), "0,0");
        pnl.add(format,  	       						   		"0,2");               
      
        return pnl;
    }
	
	private JPanel getMethodPanel() {
        JPanel methodPanel = new JPanel();
        methodPanel.setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.FILL}}));
        
       // List<eMethod> methods = new ArrayList<eMethod>();
        for (ISettingsPanel<OpenPhactsNodeModel.eMethod, OpenPhactsSettings> p : panels) {
        	cardPanel.add(p.getPanel(),	p.getMethod().name());
        //	methods.add(p.getMethod());
        }
        //cboMethodType.setModel(new DefaultComboBoxModel<OpenPhactsNodeModel.eMethod>(methods.toArray(new eMethod[0])));
        
        /*
        cboMethodType.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                	OpenPhactsNodeModel.eMethod method = (eMethod) cboMethodType.getSelectedItem();
                    cardLayout.show(cardPanel, method.name());                    
                }
                
            }
        });
        */
        
        methodPanel.add(GuiUtils.getHeaderPanel("Select Method Type"),  "0,0");
        methodPanel.add(cboMethodType,                              	"0,2");
        methodPanel.add(cardPanel,                                  	"0,4");
        methodPanel.add(chkAppend,                                  	"0,6");
      
        return methodPanel;
    }
	
	private void updateSettings() {
		appId.updateSettings(openPhactsSettings);
		appKey.updateSettings(openPhactsSettings);
		format.updateSettings(openPhactsSettings);
		
		openPhactsSettings.setMethod((eMethod) cboMethodType.getSelectedItem());
		for (ISettingsPanel<OpenPhactsNodeModel.eMethod, OpenPhactsSettings> p : panels) {
        	p.updateSettings(openPhactsSettings);
        }
		openPhactsSettings.setAppend(chkAppend.isSelected());
	}
		
	private void updateUI(final PortObjectSpec[ ] specs) {
		appId.updateUI(openPhactsSettings, specs);
		appKey.updateUI(openPhactsSettings, specs);
		format.updateUI(openPhactsSettings, specs);
		
		cboMethodType.setSelectedItem(openPhactsSettings.getMethod());
		
		for (ISettingsPanel<OpenPhactsNodeModel.eMethod, OpenPhactsSettings> p : panels) {
        	p.updateUI(openPhactsSettings, specs);
        }
		chkAppend.setSelected(openPhactsSettings.isAppend());
	}
	
	/** Load the settings to this dialog tab.
	 * @param settings The settings to load the configuration from. 
	 * @throws InvalidSettingsException */
	public void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[ ] specs) throws InvalidSettingsException {		
		// load the settings
		openPhactsSettings.loadSettingsFrom(settings);
		updateUI(specs);
	}
	
	/** Save the settings of this dialog tab.
	 * @param settings The settings to store the configuration in. */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		updateSettings();
		openPhactsSettings.saveSettingsTo(settings);
	}

}