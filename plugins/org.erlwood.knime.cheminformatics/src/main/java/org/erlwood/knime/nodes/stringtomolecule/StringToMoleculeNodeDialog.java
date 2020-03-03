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
package org.erlwood.knime.nodes.stringtomolecule;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.erlwood.knime.nodes.stringtomolecule.StringToMoleculeNodeModel.eFailureAction;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;

/**
 * <code>NodeDialog</code> for the "StringToMolecule" Node. Attempts to
 * convert String compatible columns to MoleculeAdapterCell object. The output can
 * either append a column or replace the original one.
 * 
 * @author Dimitar Hritozov
 */
public class StringToMoleculeNodeDialog extends DefaultNodeSettingsPane {

	private final SettingsModelString mInCol = new SettingsModelString(
			StringToMoleculeNodeModel.CFG_IN_COL, "");
	private final SettingsModelString mReplace = new SettingsModelString(
			StringToMoleculeNodeModel.CFG_REPLACE, "Replace");
	private final SettingsModelString mNewColName = new SettingsModelString(
			StringToMoleculeNodeModel.CFG_NEW_COL_NAME, "Conv. Molecule");
	private final SettingsModelBoolean mAromatize = new SettingsModelBoolean(
			StringToMoleculeNodeModel.CFG_AROMATIZE, true);
	private final SettingsModelString mFailureAction = new SettingsModelString(
	                                                                        StringToMoleculeNodeModel.CFG_FAILURE_ACTION, 
	                                                                        StringToMoleculeNodeModel.eFailureAction.EMPTY_MOLECULE.getDescription());

	private DialogComponentButtonGroup mDoReplace;
	private DialogComponentString mNewCName;

	private JTextField mTfNewCName;
	private String mUserNewName;

	/**
	 * New pane for configuring StringToMolecule node dialog. This is just a
	 * suggestion to demonstrate possible default dialog components.
	 */
	@SuppressWarnings("unchecked")
	public StringToMoleculeNodeDialog() {
		super();
		ColumnFilter stringFilter = new DataValueColumnFilter(StringValue.class);
		DialogComponentColumnNameSelection mInColSel = new DialogComponentColumnNameSelection(mInCol,
				"Select source column", 0, stringFilter);
		addDialogComponent(mInColSel);
		mNewCName = new DialogComponentString(mNewColName,
				"Name of the new column");
		addDialogComponent(mNewCName);
		DialogComponentBoolean mDoAroma = new DialogComponentBoolean(mAromatize, "Aromatize molecules");
		addDialogComponent(mDoAroma);
		mDoReplace = new DialogComponentButtonGroup(mReplace, "Output", false,
				new String[] { "Append new column", "Replace source column" },
				new String[] { "Append", "Replace" });
		addDialogComponent(mDoReplace);
		
		List<String> vals = new ArrayList<String>();
		for (eFailureAction ac : StringToMoleculeNodeModel.eFailureAction.values()) {
		    vals.add(ac.getDescription());
		}
		DialogComponentStringSelection failureAction = new DialogComponentStringSelection(mFailureAction, "Action on Molecule Conversion Failure.", vals);
		
        addDialogComponent(failureAction);
		addListeners();
	}

	private void addListeners() {
		JPanel pnl = mNewCName.getComponentPanel();
		for (int c = 0; c < pnl.getComponentCount(); ++c) {
			if (pnl.getComponent(c) instanceof JTextField) {
				mTfNewCName = (JTextField) pnl.getComponent(c);
				break;
			}
		}
		pnl = mDoReplace.getComponentPanel();
		for (int c = 0; c < pnl.getComponentCount(); ++c) {
			if (pnl.getComponent(c) instanceof Box) {
				Box box = (Box) pnl.getComponent(c);
				for (int cc = 0; cc < box.getComponentCount(); ++cc) {
					if (box.getComponent(cc) instanceof JRadioButton) {
						JRadioButton rbtn = (JRadioButton) box.getComponent(cc);
						if ("Replace source column".equals(rbtn.getText())) {
							addReplaceListener(rbtn);
						} else if ("Append new column".equals(rbtn.getText())) {
							addAppendListener(rbtn);
						}
					}
				}
				break;
			}
		}
	}

	private void addAppendListener(JRadioButton rbtn) {
		rbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (null != mTfNewCName) {
					JRadioButton rbtn = (JRadioButton) ae.getSource();
					if (rbtn.isSelected() && null != mUserNewName) {
						mTfNewCName.setText(mUserNewName);
					}
					mTfNewCName.setEnabled(rbtn.isSelected());
				}
			}
		});
	}

	private void addReplaceListener(JRadioButton rbtn) {
		rbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (null != mTfNewCName) {
					JRadioButton rbtn = (JRadioButton) ae.getSource();
					if (rbtn.isSelected()) {
						mUserNewName = mTfNewCName.getText();
						mTfNewCName.setText(mInCol.getStringValue());
					}
					mTfNewCName.setEnabled(!rbtn.isSelected());
				}
			}
		});
	}

	@Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
			final PortObjectSpec[] specs) throws NotConfigurableException {
		if ("Append".equals(mReplace.getStringValue())) {
			mUserNewName = mNewColName.getStringValue();
			if (null != mTfNewCName) {
				mTfNewCName.setEnabled(true);
			}
		} else if (null != mTfNewCName) {
			mTfNewCName.setEnabled(false);
		}
	}

}
