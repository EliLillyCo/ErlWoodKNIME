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
package org.erlwood.knime.utils.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.erlwood.knime.utils.IWebServicePreferenceSupplier;
import org.erlwood.knime.utils.exceptions.WebServiceException;
import org.erlwood.knime.utils.gui.auth.CredentialsSelectionBox;
import org.erlwood.knime.utils.gui.documentfilters.IntegerDocumentFilter;
import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.erlwood.knime.utils.settings.WebServiceSettings;
import org.erlwood.knime.utils.settings.WebServiceSettingsOptions;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.FlowVariable;

/** Tab that can be added to a NodeDialog that includes all the configuration that should be 
 * common across all the web service node implementations.
 * @author Tom Wilkin */
@SuppressWarnings("serial")
public class WebServiceDialogTab extends JPanel implements PropertyChangeListener {
	
	/** The node logger instance. */
	private static final NodeLogger LOGGER = NodeLogger.getLogger(WebServiceDialogTab.class);
	
	/** The title for this tab in the NodeDialog. */
	public static final String TITLE = "Web Service";
	
	/** The settings that stores the additional properties that this tab adds. */
	private WebServiceSettings wsSettings;
	
	/** The selection for workflow credentials. */
	private CredentialsSelectionBox credentialsName;
	
	/** The panel containing the advanced options. */
	private JPanel advancedPanel;
	
	/** The text field for the time out. */
	private JTextField timeOut;
	
	/** The text field for the max child elements. */
	private JTextField maxChildElements;
	
	/** The checkbox to enable the URL override. */
	private JCheckBox useUrlOverride;
	
	/** The value for the URL override. */
	private JTextField urlOverride;
	
	/**
	 * Creates and returns a new instance of a WebServiceDialogTab.
	 * This is performed via reflection by examination of the System property
	 * org.erlwood.knime.utils.gui.WebServiceDialogTab.CLASS_NAME
	 * @param defaultUrl The preference supplier to get the URL preference id.
	 * @param options The options.
	 * @return A WebServiceDialogTab instance
	 */
	public static WebServiceDialogTab createTab(final IWebServicePreferenceSupplier prefSupplier, final int options) {
		WebServiceDialogTab retVal = null;
        
		IExtensionRegistry reg = Platform.getExtensionRegistry();
        // 	Gets the Factory.
        try {
	    	for (IConfigurationElement element : reg.getConfigurationElementsFor("org.erlwood.knime.webservice.core.ErlwoodWebServiceTabFactory")) {	    		
	    		retVal = (WebServiceDialogTab) element.createExecutableExtension("class");	
	    		break;
	    	}
        } catch(Exception ex) {
            // Do nothing
        	LOGGER.error(ex.getMessage(), ex);
        }    
	  	
        if (retVal == null) {
        	retVal = new WebServiceDialogTab();
        }
        
        retVal.initialise(prefSupplier, options);
		return retVal;
	}
	
	protected void initialise(final IWebServicePreferenceSupplier prefSupplier, final int options) 
	{		
		wsSettings = createWebServiceSettings(prefSupplier, options);
		createGUI( );
	}
	
	/**
	 * Creates the WebServiceSettings object.
	 * @param defaultUrl The URL to use
	 * @param options The options.
	 * @return A WebServiceSettings object 
	 */
	protected WebServiceSettings createWebServiceSettings(final IWebServicePreferenceSupplier prefSupplier, final int options) {
		// create the settings
		WebServiceSettings wss = new WebServiceSettings(prefSupplier);
		if(options > -1) {
			wss.setOptions(options);
		}
		return wss;
	}
		

	/** @return The settings stored in this tab. */
	public WebServiceSettings getSettings( ) { return wsSettings; }
	
	/** Save the settings of this dialog tab.
	 * @param settings The settings to store the configuration in. */
	public void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		try {
			updateValues( );
		} catch(WebServiceException e) {
			throw new InvalidSettingsException(e.getMessage( ), e);
		}
		
		wsSettings.saveSettingsTo(settings);
	}
	
	/** Load the settings to this dialog tab.
	 * @param settings The settings to load the configuration from. */
	public void loadSettingsFrom(final NodeSettingsRO settings,
			final Map<String, FlowVariable> flowMap, final CredentialsProvider credentials) 
	{
		// load the settings
		wsSettings.loadSettingsFrom(settings);
		
		// append the credential names to the list
		if(WebServiceSettingsOptions.useCredentials(wsSettings.getOptions( ))) {
			credentialsName.updateItems(flowMap, credentials);
			
			// set the selected credentials
			credentialsName.setSelectedItem(
					wsSettings.isUseCredentials( ),
					wsSettings.getCredentialsName( )
			);
		}
		
		// set the values in the GUI entities from the settings
		if(timeOut != null) {
			timeOut.setText(Integer.toString(wsSettings.getTimeOut( )));
		}
		
		if(maxChildElements != null) {
			maxChildElements.setText(Integer.toString(wsSettings.getMaxChildElements( )));
		}
		
		useUrlOverride.setSelected(wsSettings.isUseURLOverride( ));
		try {
			if (wsSettings.getURLOverride( ) != null) {
				urlOverride.setText(wsSettings.getURLOverride( ).toString( ));
			}
		} catch(WebServiceException e) {
			LOGGER.error(e);
		}
		
		// show the advanced panel if one of the values has been changed		
		if(wsSettings.isDefault( )) {
			advancedPanel.setVisible(false);
		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		try {
			updateValues( );
		} catch(WebServiceException e) {
			LOGGER.error(e);
		}
	}
	
	/** Create the GUI for the tab. */
	private void createGUI( ) {
		// set the layout
		TableLayout layout = new TableLayout(new double[ ][ ] {
				{ TableLayout.FILL },
				{ 0 }
		});
		setLayout(layout);
		setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		
		// add any custom content boxes
		JPanel customPanel = createCustomPanel();
		if(customPanel != null) {
			layout.insertRow(layout.getNumRow(), TableLayout.PREFERRED);
			layout.insertRow(layout.getNumRow(), 10);
			add(customPanel, "0," + (layout.getNumRow() - 2));
		}
		
		// add the drop-down for credentials
		createCredentialsPanel(layout);
		
		// create the advanced button
		layout.insertRow(layout.getNumRow( ), TableLayout.PREFERRED);
		layout.insertRow(layout.getNumRow( ), 10);
		JPanel buttonPanel = new JPanel( );
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		JButton advancedButton = new JButton("Advanced");
		buttonPanel.add(advancedButton);
		buttonPanel.add(Box.createHorizontalGlue( ));
		add(buttonPanel, "0," + (layout.getNumRow( ) - 2));
		
		// create the advanced panel
		layout.insertRow(layout.getNumRow( ), TableLayout.PREFERRED);
		layout.insertRow(layout.getNumRow( ), TableLayout.FILL);
		add(createAdvancedPanel( ), "0," + (layout.getNumRow( ) - 2));
		
		// enable the advanced button
		advancedButton.addActionListener(new ActionListener( ) {

			@Override
			public void actionPerformed(final ActionEvent event) {
				boolean isVisible = advancedPanel.isVisible( );
				advancedPanel.setVisible(!isVisible);
			}
			
		});
	}
	
	/** Update the stored values in the settings from the GUI fields. 
	 * @throws WebServiceException If an error occurred creating the URL override. */
	protected void updateValues( ) throws WebServiceException {
		try {
			if(timeOut != null) {
				wsSettings.setTimeOut(Integer.parseInt(timeOut.getText( )));
			}
			
			if(maxChildElements != null) {
				wsSettings.setMaxChildElements(Integer.parseInt(maxChildElements.getText( )));
			}
		} catch(NumberFormatException e) {
			// ignore (we're enforcing the number type)
		}
		
		// set the credentials
		if(WebServiceSettingsOptions.useCredentials(wsSettings.getOptions( ))) {
			wsSettings.setUseCredentials(credentialsName.isUseCredentials( ));
			wsSettings.setCredentialsName(credentialsName.getSelectedItem( ));
		}
		
		// set the URL override
		wsSettings.setUseURLOverride(useUrlOverride.isSelected( ));
		wsSettings.setURLOverride(urlOverride.getText( ));
				
		checkEnabled( );
	}
	
	/** Check whether the URL override box should be made visible or not. */
	private void checkEnabled( ) {
		if(useUrlOverride.isSelected( )) {
			urlOverride.setEnabled(true);
		} else {
			urlOverride.setEnabled(false);
		}
	}
	
	/** Create the panel containing the credentials options.
	 * @param layout The layout to add the panel to.
	 * @return The credentials panel. */
	private void createCredentialsPanel(final TableLayout layout) {
		int options = wsSettings.getOptions( );
		
		if(WebServiceSettingsOptions.useCredentials(options)) {
			layout.insertRow(layout.getNumRow( ), TableLayout.PREFERRED);
			layout.insertRow(layout.getNumRow( ), 10);
			JPanel credentialsPanel = new JPanel( );
			credentialsPanel.setLayout(new TableLayout(new double[ ][ ] {
					{ TableLayout.PREFERRED, 10, TableLayout.FILL },
					{ 
						TableLayout.PREFERRED
					}
			}));
			JLabel label = new JLabel("Workflow Credentials:");
			credentialsPanel.add(label, "0,0");
			credentialsName = new CredentialsSelectionBox(
					WebServiceSettingsOptions.useOptionalCredentials(options)
			);
			credentialsName.addPropertyChangeListener(this);
			label.setToolTipText(credentialsName.getToolTipText( ));
			credentialsPanel.add(credentialsName, "2,0");
			
			add(credentialsPanel, "0," + (layout.getNumRow( ) - 2));
		}
	}
	
	/** Create a custom panel at the top of the UI, by default does nothing.
	 * @return The custom panel, or null if not custom panel is created. */
	protected JPanel createCustomPanel() {
		return null;
	}
	
	/** Create the advanced panel options.
	 * @return The panel containing the advanced options. */
	protected JPanel createAdvancedPanel( ) {
		int options = wsSettings.getOptions( );
		
		advancedPanel = new JPanel( );
		TableLayout layout = new TableLayout(new double[ ][ ] {
				{ TableLayout.PREFERRED, 10, TableLayout.FILL },
				{ TableLayout.PREFERRED, 10 }
		});
		advancedPanel.setLayout(layout);
		
		// add the warning
		JLabel warning = new JLabel(
				"These options are intended for advanced users only."
				+ "Only modify them when instructed to do so."
		);
		advancedPanel.add(warning, "0,0,2");
		
		// add the text field for the time out
		if(WebServiceSettingsOptions.useTimeOut(options)) {
			layout.insertRow(layout.getNumRow( ), TableLayout.PREFERRED);
			layout.insertRow(layout.getNumRow( ), 10);
			String toolTip = "Enter the number of seconds to wait until the web service request times out.";
			JLabel label = new JLabel("Time Out (seconds):");
			label.setToolTipText(toolTip);
			advancedPanel.add(label, "0," + (layout.getNumRow( ) - 2));
			timeOut = new JTextField( );
			((AbstractDocument)timeOut.getDocument( )).setDocumentFilter(
					new IntegerDocumentFilter( )
			);
			timeOut.setToolTipText(toolTip);
			timeOut.addPropertyChangeListener(this);
			advancedPanel.add(timeOut, "2," + (layout.getNumRow( ) - 2));
		}
		
		// add the text field for the max child elements
		if(WebServiceSettingsOptions.useMaxChildElements(options)) {
			layout.insertRow(layout.getNumRow( ), TableLayout.PREFERRED);
			layout.insertRow(layout.getNumRow( ), 10);
			String toolTip = "Enter the number of allowable elements in an XML response.";
			JLabel label = new JLabel("Max Child Elements:");
			label.setToolTipText(toolTip);
			advancedPanel.add(label, "0," + (layout.getNumRow( ) - 2));
			maxChildElements = new JTextField( );
			((AbstractDocument)maxChildElements.getDocument( )).setDocumentFilter(
					new IntegerDocumentFilter( )
			);
			maxChildElements.setToolTipText(toolTip);
			maxChildElements.addPropertyChangeListener(this);
			advancedPanel.add(maxChildElements, "2," + (layout.getNumRow( ) - 2));
		}
		
		// add the options for the web service URL override
		createURLOverridePanel(layout);
		
		
		layout.insertRow(layout.getNumRow( ), TableLayout.FILL);
		layout.invalidateLayout(advancedPanel);
		
		return advancedPanel;
	}
	
	/** Create the panel containing the URL override option.
	 * @param layout The TableLayout the panel should be added to. */
	private void createURLOverridePanel(final TableLayout layout) {
		layout.insertRow(layout.getNumRow( ), TableLayout.PREFERRED);
		layout.insertRow(layout.getNumRow( ), 10);
		
		String toolTip = "Override the URL from the preferences with a custom value.";
		
		JLabel label = new JLabel("URL Override:");
		label.setToolTipText(toolTip);
		advancedPanel.add(label, "0," + (layout.getNumRow( ) - 2));
		
		final JPanel urlOverridePanel = new JPanel( );
		urlOverridePanel.setLayout(new TableLayout(new double[ ][ ] {
				{ TableLayout.PREFERRED, TableLayout.FILL },
				{ TableLayout.FILL }
		}));
		setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		advancedPanel.add(urlOverridePanel, "2," + (layout.getNumRow( ) - 2));
		
		useUrlOverride = new JCheckBox( );
		useUrlOverride.setToolTipText(toolTip);
		useUrlOverride.addPropertyChangeListener(this);
		urlOverridePanel.add(useUrlOverride, "0,0");
		
		urlOverride = new JTextField( );
		urlOverride.setToolTipText(toolTip);
		urlOverride.addPropertyChangeListener(this);
		urlOverridePanel.add(urlOverride, "1,0");
		
		// show/hide the URL box based on the checkbox
		useUrlOverride.addChangeListener(new ChangeListener( ) {

			@Override
			public void stateChanged(final ChangeEvent event) {
				checkEnabled( );
			} 
			
		});
		
		// warn the user about the URL override when they click it
		useUrlOverride.addActionListener(new ActionListener( ) {

			@Override
			public void actionPerformed(final ActionEvent event) {
				if(useUrlOverride.isSelected( )) {
					int choice = JOptionPane.showConfirmDialog(
							urlOverridePanel,
							"The URL from the node dialog configuration will override the value"
							+ " from the web service preferences.\n This may cause issues if the "
							+ "URL changes in the future.\n Are you sure you want to do this?",
							"URL Override", 
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE
					);
					if(choice == JOptionPane.YES_OPTION) {
						useUrlOverride.setSelected(true);
					} else {
						useUrlOverride.setSelected(false);
					}
				}
			} 
			
		});
	}
	
	
	
};
