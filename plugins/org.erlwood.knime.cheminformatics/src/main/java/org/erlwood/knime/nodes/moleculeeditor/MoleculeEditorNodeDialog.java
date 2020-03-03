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
package org.erlwood.knime.nodes.moleculeeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import jp.co.infocom.cheminfo.marvin.type.MrvValue;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.erlwood.knime.utils.gui.ConverterDataColumnSpecListCellRenderer;

/**
 * <code>NodeDialog</code> for the "MoleculeEditor" Node. Allows to edit
 * existing molecules and to select atoms.
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Nikolas Fechner, Dimitar Hristotov
 */
public class MoleculeEditorNodeDialog extends DefaultNodeSettingsPane {

	private final SettingsModelString mInCol = new SettingsModelString(
			MoleculeEditorNodeModel.COLUMN, "column");
	private final SettingsModelString mReplace = new SettingsModelString(
			MoleculeEditorNodeModel.CFG_REPLACE, "Append");
	private final SettingsModelString mNewColName = new SettingsModelString(
			MoleculeEditorNodeModel.CFG_NEW_COL_NAME, "Smiles");
	private final SettingsModelBoolean mResetprevious = new SettingsModelBoolean(
			MoleculeEditorNodeModel.CFG_RESET_PREVIOUS, false);

	private DialogComponentColumnNameSelection mInColSel;
	private DialogComponentButtonGroup mDoReplace;
	private DialogComponentString mNewCName;
	private DialogComponentBoolean mResetPrev;

	private JTextField mTfNewCName;
	private JCheckBox mChbRowIds;
	private JRadioButton mRbtnAppend, mRbtnReplace;
	private JComboBox mCmbInCol;
	private String mUserNewName;

	/**
	 * New pane for configuring the LsnToSmiles node.
	 */
	protected MoleculeEditorNodeDialog() {
		super();
		
		mInColSel = new DialogComponentColumnNameSelection(mInCol,
				"Molecule column", 0, true, false, MoleculeDataTypeConverter.getColumnFilter(MrvValue.class));
		
		ConverterDataColumnSpecListCellRenderer.setRenderer(mInColSel, MrvValue.class);
		
		addDialogComponent(mInColSel);
		mNewCName = new DialogComponentString(mNewColName,
				"Name of the new column");
		addDialogComponent(mNewCName);

		mDoReplace = new DialogComponentButtonGroup(mReplace, "Output", false,
				new String[] { "Append new column", "Replace source column" },
				new String[] { "Append", "Replace" });
		addDialogComponent(mDoReplace);

		mResetPrev = new DialogComponentBoolean(mResetprevious,
				"Reset previous modifications");
		addDialogComponent(mResetPrev);

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

		pnl = mInColSel.getComponentPanel();
		for (int c = 0; c < pnl.getComponentCount(); ++c) {
			if (pnl.getComponent(c) instanceof JPanel) {
				JPanel pnl2 = (JPanel) pnl.getComponent(c);
				for (int cc = 0; cc < pnl2.getComponentCount(); ++cc) {
					if (pnl2.getComponent(cc) instanceof JComboBox) {
						mCmbInCol = (JComboBox) pnl2.getComponent(cc);
						break;
					}
				}
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
							mRbtnReplace = rbtn;
							addReplaceListener(rbtn);
						} else if ("Append new column".equals(rbtn.getText())) {
							mRbtnAppend = rbtn;
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
		if (null != mChbRowIds) {
			mRbtnAppend.setSelected(mChbRowIds.isSelected());
			mRbtnReplace.setEnabled(!mChbRowIds.isSelected());
			mCmbInCol.setEnabled(!mChbRowIds.isSelected());
			if (mChbRowIds.isSelected()) {
				mTfNewCName.setEnabled(true);
			}
		}
	}

	public void saveAdditionalSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		super.saveAdditionalSettingsTo(settings);

	}
}
