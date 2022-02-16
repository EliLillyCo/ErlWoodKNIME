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
 * 
 * Extended from KNIME implementation of 'List Files' node to include
 * listing of files stored on samba based network shares.
 * 
 * The KNIME license for this content is as follows:
 *  ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
*/
package org.erlwood.knime.nodes.listfileswithauth;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.lang3.StringUtils;
import org.erlwood.knime.nodes.listfileswithauth.ListFilesWithAuth.Filter;
import org.erlwood.knime.utils.auth.AuthenticationUtils;
import org.erlwood.knime.utils.auth.SambaUtility;
import org.erlwood.knime.utils.gui.auth.AuthenticationTab;
import org.knime.base.util.WildcardMatcher;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.util.ConvenientComboBoxRenderer;

import com.hierynomus.smbj.auth.AuthenticationContext;

import ch.swaechter.smbjwrapper.SharedConnection;
import ch.swaechter.smbjwrapper.SharedDirectory;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * <code>NodeDialog</code> for the "List Files" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Peter
 */
public final class ListFilesWithAuthNodeDialog extends NodeDialogPane implements
		ItemListener {

	private static final int HORIZ_SPACE = 10;

	private static final int PANEL_WIDTH = 585;
	
	private JComboBox<String> mLocations;

	private JComboBox<String> mExtensionField;

	private JCheckBox mCcaseSensitive;

	private JCheckBox mRecursive;

	private JRadioButton mFilterALLRadio;

	private JRadioButton mFilterExtensionsRadio;

	private JRadioButton mFilterRegExpRadio;

	private JRadioButton mFilterWildCardsRadio;
	
	private AuthenticationTab authentication;

	/**
	 * Creates a new List FilesNodeDialog.
	 */
	protected ListFilesWithAuthNodeDialog() {
		super();

		super.removeTab("Options");
		super.addTabAt(0, "Options", createPanel());
		
		authentication = new AuthenticationTab(false);
		addTab(AuthenticationTab.TITLE, authentication);
	}

	private JPanel createPanel() {

		createFiltersModels();
		JPanel panel = createLocationPanel();
		Box panel2 = createFilterBox();

		JPanel outer = new JPanel();
		outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
		outer.setAlignmentX(Component.LEFT_ALIGNMENT);
		outer.add(panel);
		outer.add(panel2);
		return outer;
	}

	/**
	 * This method create the Filter-Box.
	 * 
	 * @return Filter-Box
	 */
	private Box createFilterBox() {
		Box panel2 = Box.createVerticalBox();
		panel2.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Filter:"));

		// Get the same height for Location and extension field.
		int buttonHeight = new JButton("Browse...").getPreferredSize().height;

		mExtensionField = new JComboBox<String>();
		mExtensionField.setEditable(true);
		mExtensionField.setRenderer(new ConvenientComboBoxRenderer());
		mExtensionField
				.setMaximumSize(new Dimension(PANEL_WIDTH, buttonHeight));
		mExtensionField.setMinimumSize(new Dimension(250, buttonHeight));
		mExtensionField.setPreferredSize(new Dimension(250, buttonHeight));

		Box extBox = Box.createHorizontalBox();
		extBox.add(Box.createHorizontalStrut(HORIZ_SPACE));
		extBox.add(new JLabel("Extension(s) / Expression:"));
		extBox.add(Box.createHorizontalStrut(HORIZ_SPACE));
		extBox.add(mExtensionField);
		extBox.add(Box.createHorizontalStrut(HORIZ_SPACE));

		mCcaseSensitive = new JCheckBox();
		mCcaseSensitive.setText("case sensitive");

		JPanel filterBox = new JPanel(new GridLayout(2, 3));
		filterBox.add(mFilterALLRadio);
		filterBox.add(mFilterExtensionsRadio);
		filterBox.add(mCcaseSensitive);
		filterBox.add(mFilterRegExpRadio);
		filterBox.add(mFilterWildCardsRadio);

		Box filterBox2 = Box.createHorizontalBox();
		filterBox2.add(Box.createHorizontalStrut(HORIZ_SPACE));
		filterBox2.add(filterBox);
		filterBox2.add(Box.createHorizontalStrut(PANEL_WIDTH / 4));

		panel2.add(extBox);
		panel2.add(filterBox2);

		panel2.setMaximumSize(new Dimension(PANEL_WIDTH, 120));
		panel2.setMinimumSize(new Dimension(PANEL_WIDTH, 120));

		return panel2;
	}

	/**
	 * This Methods build the Location Panel.
	 * 
	 * @return Location Panel
	 */
	private JPanel createLocationPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Location:"));

		Box fileBox = Box.createHorizontalBox();

		// Creating the browse button to get its preferred height
		JButton browse = new JButton("Browse...");
		int buttonHeight = browse.getPreferredSize().height;

		mLocations = new JComboBox<String>();
		mLocations.setToolTipText("Enter Location(s) here");
		mLocations.setEditable(true);
		mLocations.setRenderer(new ConvenientComboBoxRenderer());
		mLocations.setMaximumSize(new Dimension(350, buttonHeight));
		mLocations.setMinimumSize(new Dimension(350, buttonHeight));
		mLocations.setPreferredSize(new Dimension(350, buttonHeight));

		mRecursive = new JCheckBox();
		mRecursive.setText("include sub folders");
		Box rec = Box.createHorizontalBox();
		rec.add(Box.createHorizontalStrut(HORIZ_SPACE));
		rec.add(mRecursive);
		rec.add(Box.createHorizontalStrut(PANEL_WIDTH - mRecursive.getWidth()
				- HORIZ_SPACE));

		fileBox.add(Box.createHorizontalStrut(HORIZ_SPACE));
		fileBox.add(new JLabel("Location(s):"));
		fileBox.add(Box.createHorizontalStrut(HORIZ_SPACE));
		fileBox.add(mLocations);
		fileBox.add(Box.createHorizontalStrut(HORIZ_SPACE));
		fileBox.add(browse);
		fileBox.add(Box.createHorizontalStrut(45));

		panel.add(fileBox);
		panel.add(rec);
		panel.setMaximumSize(new Dimension(PANEL_WIDTH, 80));
		panel.setMinimumSize(new Dimension(PANEL_WIDTH, 80));

		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// sets the path in the file text field.
				String[] newFile = popupFileChooser();
				if (newFile != null) {
					mLocations.getEditor().setItem(getStringForBox(newFile));

				}
			}
		});

		return panel;
	}

	/** creates the filter radio buttons. */
	private void createFiltersModels() {
		mFilterALLRadio = new JRadioButton();
		mFilterALLRadio.setText("none");
		mFilterALLRadio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				mExtensionField.setEnabled(false);
			}
		});

		mFilterExtensionsRadio = new JRadioButton();
		mFilterExtensionsRadio.setText("file extension(s)");
		mFilterExtensionsRadio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				mExtensionField.setEnabled(true);
			}
		});

		mFilterRegExpRadio = new JRadioButton();
		mFilterRegExpRadio.setText("regular expression");
		mFilterRegExpRadio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				mExtensionField.setEnabled(true);
			}
		});

		mFilterWildCardsRadio = new JRadioButton();
		mFilterWildCardsRadio.setText("wildcard pattern");
		mFilterWildCardsRadio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				mExtensionField.setEnabled(true);
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(mFilterALLRadio);
		group.add(mFilterExtensionsRadio);
		group.add(mFilterRegExpRadio);
		group.add(mFilterWildCardsRadio);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void itemStateChanged(final ItemEvent e) {
		// nothing to do
	}

	/**
	 * Pops up the file selection dialog and returns the path(s) to the selected
	 * file(s) - or <code>null</code> if the user cancelled.
	 * 
	 * @return Array containing the File locations
	 **/
	protected String[] popupFileChooser() {
		String startingDir = "";
		JFileChooser chooser;
		chooser = new JFileChooser(startingDir);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// make dialog modal
		int returnVal = chooser.showOpenDialog(getPanel().getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String[] path = new String[chooser.getSelectedFiles().length];
			for (int i = 0; i < path.length; i++) {
				try {
					path[i] = chooser.getSelectedFiles()[i].getAbsoluteFile()
							.toString();
				} catch (Exception e) {
					path[i] = "<Error: Couldn't create URL for Directory>";
				}
			}
			return path;
		}
		// user cancelled - return null
		return null;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {

		// check if all entered Locations are valid
		String location = mLocations.getEditor().getItem().toString();
		if (location.trim().isEmpty()) {
			throw new InvalidSettingsException("Please select a file!");
		}
		
		ListFilesWithAuthSettings set = new ListFilesWithAuthSettings();

		// get the authentication credentials if there are any
		authentication.saveSettingsTo(set);
		
		AuthenticationContext creds = AuthenticationUtils.getAuthenticationContext(this.getAvailableFlowVariables(org.knime.core.node.workflow.VariableType.StringType.INSTANCE, org.knime.core.node.workflow.VariableType.DoubleType.INSTANCE, org.knime.core.node.workflow.VariableType.IntType.INSTANCE), getCredentialsProvider(), set.getCredentialsName());
		SharedConnection con = null;

		String[] files = location.split(";");
		for (int i = 0; i < files.length; i++) {
			// decide if the file is remote or not
			if (creds != null) {
				String s = SambaUtility.makeURL(files[i], false);
				if (SambaUtility.isSambaURL(s)) {
					// samba file
					try {
						SmbFile currentFile = new SmbFile(URLDecoder.decode(s, "UTF-8"));
						con = new SharedConnection(currentFile.getServer(), currentFile.getShare(), creds); //check auth and location
				        String parentFolder  = StringUtils.substringAfter(currentFile.getPath(), currentFile.getShare());
				        SharedDirectory sharedDirectory = new SharedDirectory(con,  SambaUtility.fixSambaPath(parentFolder));
						if (!sharedDirectory.isDirectory()) {
							// check if it is a directory
							throw new InvalidSettingsException("\"" + s
									+ "\" does not exist or is not a directory");
						}
					} catch (UnsupportedEncodingException e) {
						throw new InvalidSettingsException("\"" + s
								+ "\" does not exist or is not a directory", e);
					} catch (MalformedURLException e) {
						throw new InvalidSettingsException("\"" + s
								+ "\" does not exist or is not a directory", e);
					} catch (SmbException e) {
						throw new InvalidSettingsException("\"" + s
								+ "\" is not accessible, do you have access?", e);
					} catch (IOException e) {
						throw new InvalidSettingsException("\"" + s
								+ "\" does not exist or is not a directory", e);
					} finally {
						if (con != null) {
							try {
							con.close();
							} catch (IOException e) {
								throw new InvalidSettingsException("Unexpected error closing connection", e);
								}
						}
					}
				}
			} else {
				// normal file
				File currentFile = new File(files[i]);
				if (!currentFile.isDirectory()) {
					// check if it was an URL;
					String s = files[i];
					try {
						if (s.startsWith("file:")) {
							s = s.substring(5);
						}
						currentFile = new File(URLDecoder.decode(s, "UTF-8"));
					} catch (UnsupportedEncodingException ex) {
						throw new InvalidSettingsException("\"" + s
								+ "\" does not exist or is not a directory", ex);
					}
					if (!currentFile.isDirectory()) {
						throw new InvalidSettingsException("\"" + s
								+ "\" does not exist or is not a directory");
					}
				}
			}

		}

		set.setLocationString(location);
		set.setRecursive(mRecursive.isSelected());
		set.setCaseSensitive(mCcaseSensitive.isSelected());
		String extensions = mExtensionField.getEditor().getItem().toString();
		set.setExtensionsString(extensions);

		// save the selected radio-Button
		Filter filter;
		if (mFilterALLRadio.isSelected()) {
			filter = Filter.None;
		} else if (mFilterExtensionsRadio.isSelected()) {
			filter = Filter.Extensions;
		} else if (mFilterRegExpRadio.isSelected()) {
			if (extensions.trim().isEmpty()) {
				throw new InvalidSettingsException(
						"Enter valid regular expressin pattern");
			}
			try {
				String pattern = extensions;
				Pattern.compile(pattern);
			} catch (PatternSyntaxException pse) {
				throw new InvalidSettingsException("Error in pattern: ('"
						+ pse.getMessage(), pse);
			}
			filter = Filter.RegExp;
		} else if (mFilterWildCardsRadio.isSelected()) {

			if ((extensions).length() <= 0) {
				throw new InvalidSettingsException(
						"Enter valid wildcard pattern");
			}
			try {
				String pattern = extensions;
				pattern = WildcardMatcher.wildcardToRegex(pattern);
				Pattern.compile(pattern);
			} catch (PatternSyntaxException pse) {
				throw new InvalidSettingsException("Error in pattern: '"
						+ pse.getMessage(), pse);
			}
			filter = Filter.Wildcards;
		} else { 
			// one button must be selected though
			filter = Filter.None;
		}
		set.setFilter(filter);
		
		set.saveSettingsTo(settings);
	}

	/**
	 * This Method creates the String for the Location text field from the given
	 * file URLs.
	 * 
	 * @param fileurls
	 * @return
	 */
	private String getStringForBox(final String[] fileurls) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < fileurls.length; i++) {
			buff.append(fileurls[i]);
			buff.append(";");
		}
		return buff.toString();
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final DataTableSpec[] specs) throws NotConfigurableException {

		ListFilesWithAuthSettings set = new ListFilesWithAuthSettings();
		set.loadSettingsInDialog(settings);

		// add previous selections to the Location text field
		String[] history = ListFilesWithAuthSettings.getLocationHistory();
		mLocations.removeAllItems();
		for (String str : history) {
			mLocations.addItem(str);
		}

		// add previous selections to the extension text field
		history = ListFilesWithAuthSettings.getExtensionHistory();
		mExtensionField.removeAllItems();
		for (String str : history) {
			mExtensionField.addItem(str);
		}

		mCcaseSensitive.setSelected(set.isCaseSensitive());
		String loc = set.getLocationString();
		mLocations.getEditor().setItem(loc == null ? "" : loc);
		mRecursive.setSelected(set.isRecursive());
		String ext = set.getExtensionsString();
		mExtensionField.getEditor().setItem(ext == null ? "" : ext);
		switch (set.getFilter()) {
		case Extensions:
			// trigger event
			mFilterExtensionsRadio.doClick(); 
			break;
		case RegExp:
			mFilterRegExpRadio.doClick();
			break;
		case Wildcards:
			mFilterWildCardsRadio.doClick();
			break;
		default:
			mFilterALLRadio.doClick();
		}
		
		authentication.loadSettingsFrom(
				set,
				getAvailableFlowVariables( ),
				getCredentialsProvider( )
		);

	}
}
