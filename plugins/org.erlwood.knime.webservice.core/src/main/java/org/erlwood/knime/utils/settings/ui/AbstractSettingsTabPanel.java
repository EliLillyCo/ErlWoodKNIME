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

import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.erlwood.knime.utils.gui.GuiUtils;
import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;

import org.erlwood.knime.utils.settings.AbstractSettings;

/**
 * Abstract tab panel to add to the settings dialogs.
 * @author Luke Bullard
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractSettingsTabPanel <M extends Enum<M>, T extends AbstractSettings<M>> extends JPanel {
	
	private final T	settings;
	
	private JComboBox<M> cboMethodType = new JComboBox<M>();
	
	private CardLayout	cardLayout 	= new CardLayout();
	private JPanel 		cardPanel 	= new JPanel(cardLayout);
	
	private final ISettingsPanel<M, T>[]	panels;
	
	protected abstract T createSettings();
	protected abstract ISettingsPanel<M, T>[]	createSettingsPanels();
	
	protected AbstractSettingsTabPanel() {
		settings = createSettings();
		panels = createSettingsPanels();		
		
		setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.FILL}}));    	
    	
    	add(getMethodPanel(),             "0,0");
     	    	
    	setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
   
	}
	
	private JPanel getMethodPanel() {
        JPanel methodPanel = new JPanel();
        methodPanel.setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.FILL}}));
        
        List<M> methods = new ArrayList<M>();
        for (ISettingsPanel<M, T> p : panels) {
        	cardPanel.add(p.getPanel(),	p.getMethod().name());
        	methods.add(p.getMethod());
        }
        
        Object[] ms = methods.toArray();
        
        cboMethodType.setModel(new DefaultComboBoxModel(ms));
        
        cboMethodType.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                	M method = (M) cboMethodType.getSelectedItem();
                    cardLayout.show(cardPanel, method.name());                    
                }
                
            }
        });
        
        methodPanel.add(GuiUtils.getHeaderPanel("Select Method Type"),  "0,0");
        methodPanel.add(cboMethodType,                              	"0,2");
        methodPanel.add(cardPanel,                                  	"0,4");
      
        return methodPanel;
    }
	
	@SuppressWarnings("unchecked")
	private void updateSettings() {
		settings.setMethod((M) cboMethodType.getSelectedItem());
		for (ISettingsPanel<M, T> p : panels) {
        	p.updateSettings(settings);
        }
	}
		
	private void updateUI(final PortObjectSpec[ ] specs) {	
		cboMethodType.setSelectedItem(settings.getMethod());
		
		for (ISettingsPanel<M, T> p : panels) {
        	p.updateUI(settings, specs);
        }
	}
	
	/** Load the settings to this dialog tab.
	 * @param ns The settings to load the configuration from. 
	 * @throws InvalidSettingsException */
	public void loadSettingsFrom(final NodeSettingsRO ns, final PortObjectSpec[ ] specs) throws InvalidSettingsException {		
		// load the settings
		settings.loadSettingsFrom(ns);
		updateUI(specs);
	}
	
	/** Save the settings of this dialog tab.
	 * @param ns The settings to store the configuration in. */
	public void saveSettingsTo(final NodeSettingsWO ns) {
		updateSettings();
		settings.saveSettingsTo(ns);
	}

}