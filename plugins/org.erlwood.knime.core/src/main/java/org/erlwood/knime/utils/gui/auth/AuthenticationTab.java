package org.erlwood.knime.utils.gui.auth;

import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.erlwood.knime.utils.auth.AuthenticationUtils;
import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.FlowVariable;

/** Additional dialog tab providing the workflow credentials drop-down.
 * @author Tom Wilkin */
@SuppressWarnings("serial")
public class AuthenticationTab extends JPanel {
	
	/** The name of the tab to add. */
	public static final String TITLE = "Authentication";
	
	/** The drop-down box for the credentials selection. */
	private CredentialsSelectionBox credentialsNameBox;
	
	public AuthenticationTab(final boolean useNone) {		
		setLayout(new TableLayout(new double[ ][ ] {
				{ TableLayout.PREFERRED, 10, TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.FILL }
		}));
		setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		
		// add the credentials selection box
		add(new JLabel("Workflow Credentials:"), "0,0");
		credentialsNameBox = new CredentialsSelectionBox(useNone);
		add(credentialsNameBox, "2,0");	
	}
	
	/** @return The currently selected credentials name. */
	public String getSelectedCredentials( ) {
		return credentialsNameBox.getSelectedItem( );
	}

	/** Save the settings of this dialog tab.
	 * @param settings The settings to store the configuration in. */
	public void saveSettingsTo(final AuthenticationSettings settings) {
		settings.setCredentialsName(credentialsNameBox.getSelectedItem( ));
	}
	
	/** Load the settings to this dialog tab.
	 * @param settings The settings to load the configuration from.
	 * @param credentials The credentials provider to retrieve the credential list from.
	 * @throws InvalidSettingsException If an error occurred loading the settings. */
	public void loadSettingsFrom(final AuthenticationSettings settings, 
			final Map<String, FlowVariable> flowMap, final CredentialsProvider credentials) 
	{
		credentialsNameBox.updateItems(flowMap, credentials);
		
		credentialsNameBox.setSelectedItem(
				settings.isUseCredentials( ),
				settings.getCredentialsName( )
		);
	}
	
	/** @return The Settings Model for the credentials name. */
	public static SettingsModelString createCredentialsNameSettingsModel( ) {
		return new SettingsModelString(
				"credentialsName", 
				AuthenticationUtils.getDefaultCredentialsName( )
		);
	}
	
}
