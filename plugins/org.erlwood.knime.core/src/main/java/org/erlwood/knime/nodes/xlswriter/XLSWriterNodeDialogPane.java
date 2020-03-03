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
 * Extended from KNIME implementation of 'XLS Writer' node to include
 * further functionality.
 * 
 * The KNIME license for this content is as follows:
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * -------------------------------------------------------------------
 *
 * History
 *   Mar 16, 2007 (ohl): created
 */
package org.erlwood.knime.nodes.xlswriter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * 
 * @author ohl, University of Konstanz
 * @author Luke Bullard
 */
class XLSWriterNodeDialogPane extends NodeDialogPane {

    private ColourPanel                      colpan;

    private final SettingsModelString        mFilename            = new SettingsModelString("FOO", "");

    private final DialogComponentFileChooser mFileComponent       = new DialogComponentFileChooser(mFilename, "XLSWRITER",
                                                                          JFileChooser.SAVE_DIALOG, false,
                                                                          new String[] { ".xls" , ".xlsx"});

    private final JCheckBox                  mWriteColHdr         = new JCheckBox();

    private final JCheckBox                  mWriteRowHdr         = new JCheckBox();

    private final JTextField                 mMissValue           = new JTextField();

    private final JCheckBox                  mOverwriteOK         = new JCheckBox("Overwrite existing file");

    private final JCheckBox                  mAutostart           = new JCheckBox("Start Excel when finished");
    private final JCheckBox                  mPivot               = new JCheckBox("Write transposed table");

    private final JTextField                 mMergeCellReferences = new JTextField();

    private final JRadioButton              rdoCreateNewFile    =   new JRadioButton("Always create a new file");
    private final JRadioButton              rdoAppendFile       =   new JRadioButton("Create new file if required, else append");
    private final JCheckBox                 chkAbortNoExists    =   new JCheckBox("Abort if the file does not exist");
    private final JCheckBox                 chkAbortSheetExists =   new JCheckBox("Abort if the sheet already exists");
    
    private final ButtonGroup               btnGroup = new ButtonGroup();

    private final JTextField txtSheetName = new JTextField();
    
    /**
     * Creates a new dialog for the XLS writer node.
     */
    public XLSWriterNodeDialogPane() {
        JPanel tab = new JPanel();
        tab.setLayout(new BoxLayout(tab, BoxLayout.Y_AXIS));
        tab.add(createFileBox());
        tab.add(createFileOverwriteBox());
        tab.add(createAutoStartBox());
        tab.add(createSheetNameBox());
              
        tab.add(Box.createVerticalStrut(5));
        tab.add(createHeadersBox());
        tab.add(createMissingBox());
        tab.add(createMergeBox());
        tab.add(Box.createVerticalGlue());
        tab.add(Box.createVerticalGlue());
        addTab("writer options", tab);
        colpan = new ColourPanel();
        JScrollPane spane = new JScrollPane(colpan);
        addTab("Colouring", spane);
    }

    private JPanel createFileBox() {
        JPanel p = new JPanel(new BorderLayout());
        BoxLayout bl = (BoxLayout)mFileComponent.getComponentPanel().getLayout();
        //fl.setAlignment(FlowLayout.LEFT);
        bl.layoutContainer(mFileComponent.getComponentPanel());
        p.add(mFileComponent.getComponentPanel(), BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        return p;
    }

    private JComponent createFileOverwriteBox() {
        JPanel panel = new JPanel();
        TableLayout tl = new TableLayout(new double[][] {
                { 30, TableLayout.FILL },
                { TableLayout.PREFERRED, TableLayout.PREFERRED,
                        TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED } });
                        
        panel.setLayout(tl);
        
        btnGroup.add(rdoCreateNewFile);
        btnGroup.add(rdoAppendFile);
        rdoCreateNewFile.setSelected(true);
        
        rdoCreateNewFile.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    mOverwriteOK.setEnabled(true);
                    
                    chkAbortNoExists.setEnabled(false);
                    chkAbortSheetExists.setEnabled(false);
                }
                
            }
        });
        
        rdoAppendFile.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    mOverwriteOK.setEnabled(false);
                    
                    chkAbortNoExists.setEnabled(true);
                    chkAbortSheetExists.setEnabled(true);
                }
                
            }
        });
        
        panel.add(rdoCreateNewFile, "0,0,1,0");
        panel.add(mOverwriteOK,     "1,1");
        
        panel.add(rdoAppendFile,        "0,2,1,2");
        panel.add(chkAbortNoExists,     "1,3");
        panel.add(chkAbortSheetExists,  "1,4");
        
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        mOverwriteOK.setEnabled(true);
        
        chkAbortNoExists.setEnabled(false);
        chkAbortSheetExists.setEnabled(false);
        return panel;
    }

    private JComponent createAutoStartBox() {
        JPanel panel = new JPanel();
        TableLayout tl = new TableLayout(new double[][] {
                { TableLayout.FILL },
                { TableLayout.PREFERRED, TableLayout.PREFERRED} });
                        
        panel.setLayout(tl);
        panel.add(mAutostart,   "0,0");
        panel.add(mPivot,       "0,1");
      
        mPivot.setToolTipText("Writes columns as rows and vice versa");
        
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        return panel;
    }

  
    private JPanel createSheetNameBox() {

        txtSheetName.setToolTipText("This is the sheet name." + "If unsure, leave empty and it will defaul to the name of the incoming table.");
        txtSheetName.setPreferredSize(new Dimension(275, 25));
        txtSheetName.setMaximumSize(new Dimension(275, 25));
        Box missBox = Box.createHorizontalBox();
        missBox.add(Box.createHorizontalStrut(20));
        missBox.add(new JLabel("New Sheet Name:"));
        missBox.add(Box.createHorizontalStrut(5));
        missBox.add(txtSheetName);
        missBox.add(Box.createHorizontalGlue());

        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        result.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sheet Name"));
        result.add(missBox);
        return result;

    }

    private JPanel createHeadersBox() {

        mWriteColHdr.setText("add column headers");
        mWriteColHdr.setToolTipText("Column names are stored in the first row" + " of the datasheet");
        mWriteRowHdr.setText("add row ids");
        mWriteRowHdr.setToolTipText("Row IDs are stored in the first column" + " of the datasheet");

        Box colHdrBox = Box.createHorizontalBox();
        colHdrBox.add(Box.createHorizontalStrut(20));
        colHdrBox.add(mWriteColHdr);
        colHdrBox.add(Box.createHorizontalGlue());

        Box rowHdrBox = Box.createHorizontalBox();
        rowHdrBox.add(Box.createHorizontalStrut(20));
        rowHdrBox.add(mWriteRowHdr);
        rowHdrBox.add(Box.createHorizontalGlue());

        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        result.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Add names and IDs"));
        result.add(colHdrBox);
        result.add(rowHdrBox);

        return result;

    }

    private JPanel createMissingBox() {

        mMissValue.setToolTipText("This text will be set for missing values." + "If unsure, leave empty");
        mMissValue.setPreferredSize(new Dimension(75, 25));
        mMissValue.setMaximumSize(new Dimension(75, 25));
        Box missBox = Box.createHorizontalBox();
        missBox.add(Box.createHorizontalStrut(20));
        missBox.add(new JLabel("For missing values write:"));
        missBox.add(Box.createHorizontalStrut(5));
        missBox.add(mMissValue);
        missBox.add(Box.createHorizontalGlue());

        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        result.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Missing value pattern"));
        result.add(missBox);
        return result;

    }

    private JPanel createMergeBox() {

        mMergeCellReferences.setToolTipText("These are the merge cells references (A1:B1 style), separated by commas.");
        mMergeCellReferences.setPreferredSize(new Dimension(275, 25));
        mMergeCellReferences.setMaximumSize(new Dimension(275, 25));
        Box missBox = Box.createHorizontalBox();
        missBox.add(Box.createHorizontalStrut(20));
        missBox.add(new JLabel("To merge cells enter references:"));
        missBox.add(Box.createHorizontalStrut(5));
        missBox.add(mMergeCellReferences);
        missBox.add(Box.createHorizontalGlue());

        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        result.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Merge Cells References"));
        result.add(missBox);

    
        return result;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs) throws NotConfigurableException {
        XLSWriterSettings newVals;
        try {
            newVals = new XLSWriterSettings(settings);
        } catch (InvalidSettingsException ise) {
            // keep the defaults.
            newVals = new XLSWriterSettings();
        }
        mFilename.setStringValue(newVals.getFilename());
        mMissValue.setText(newVals.getMissingPattern());
        mWriteColHdr.setSelected(newVals.writeColHeader());
        mWriteRowHdr.setSelected(newVals.writeRowID());
        mOverwriteOK.setSelected(newVals.getOverwriteOK());
        mAutostart.setSelected(newVals.getAutostart());
        mPivot.setSelected(newVals.getPivot());
        mMergeCellReferences.setText(newVals.getMergeRefs());

        if (newVals.isCreateNewFile()) {
            rdoCreateNewFile.setSelected(true);
        } else {
            rdoAppendFile.setSelected(true);
        }
        chkAbortSheetExists.setSelected(newVals.isAbortOnExistingSheet());
        chkAbortNoExists.setSelected(newVals.isAbortOnMissingFile());
        txtSheetName.setText(newVals.getSheetname());
        
        // Colouring
        DataTableSpec spec = specs[0];
        if (spec.getNumColumns() == 0) {
            throw new NotConfigurableException("No columns at input.");
        }
        // construct for each column the default settings, i.e. where nothing
        // will change, neither name nor type.
        colpan.setDoubleColSettings(new DoubleColumnSettings[spec.getNumColumns()]);
        colpan.setStringColSettings(new StringColumnSettings[spec.getNumColumns()]);
        for (int i = 0; i < spec.getNumColumns(); i++) {
            DataColumnSpec colSpec = spec.getColumnSpec(i);

            // double
            colpan.getDoubleColSettings()[i] = new DoubleColumnSettings(colSpec);

            // string
            colpan.getStringColSettings()[i] = new StringColumnSettings(colSpec);
        }
        NodeSettingsRO subSettings;
        try {
            // this node settings object must contain only entry of type
            // NodeSetting
            subSettings = settings.getNodeSettings(XLSWriterNodeModel.CFG_DOUBLECOLOR_CONFIG);
        } catch (InvalidSettingsException ise) {
            subSettings = null;
        }
        if (subSettings != null) {
            // process settings for individual column
            for (String id : subSettings) {
                NodeSettingsRO idSettings;
                String nameForSettings;
                try {
                    // idSettigs address the settings for one particular column
                    idSettings = subSettings.getNodeSettings(id);
                    // the name of the column - must match
                    nameForSettings = idSettings.getString(DoubleColumnSettings.CFG_OLD_COLNAME);
                } catch (InvalidSettingsException is) {
                    continue;
                }
                // does the data table spec contain this name?
                for (DoubleColumnSettings colSet : colpan.getDoubleColSettings()) {
                    // update the column settings
                    if (colSet.getName().equals(nameForSettings)) {
                        if (colSet.isColorable()) {
                            colSet.loadSettingsFrom(
                            		idSettings, 
                            		spec.getColumnSpec(colSet.getName( ))
                            );
                        }
                        break;
                    }
                }

            }
        }

        NodeSettingsRO subSettings2;
        try {
            // this node settings object must contain only entry of type
            // NodeSetting
            subSettings2 = settings.getNodeSettings(XLSWriterNodeModel.CFG_STRINGCOLOR_CONFIG);
        } catch (InvalidSettingsException ise) {
            subSettings2 = null;
        }
        if (subSettings2 != null) {
            // process settings for individual column
            for (String id : subSettings2) {
                NodeSettingsRO idSettings;
                String nameForSettings;
                try {
                    // idSettigs address the settings for one particular column
                    idSettings = subSettings2.getNodeSettings(id);
                    // the name of the column - must match
                    nameForSettings = idSettings.getString(StringColumnSettings.CFG_OLD_COLNAME);
                } catch (InvalidSettingsException is) {
                    continue;
                }
                // does the data table spec contain this name?
                for (StringColumnSettings colSet : colpan.getStringColSettings()) {
                    // update the column settings
                    if (colSet.getName().equals(nameForSettings)) {
                        if (colSet.isColourable()) {
                            colSet.loadSettingsFrom(
                            		idSettings, 
                            		spec.getColumnSpec(colSet.getName( ))
                            );
                        }
                        break;
                    }
                }

            }
        }

        // layout the panel, add new components - one for each column
        colpan.removeAll();
        // determine the longest column name, used to make the dialog look nice
        int max = 0;
        for (DoubleColumnSettings colSet : colpan.getDoubleColSettings()) {
            JLabel label = new JLabel(colSet.getName().toString());
            max = Math.max(max, label.getPreferredSize().width);
        }
        // hm, don't let it look squeezed
        max += 10;
        for (int i = 0; i < colpan.getDoubleColSettings().length; i++) {
            DoubleColumnSettings colSet = colpan.getDoubleColSettings()[i];
            if (colSet.isColorable()) {
                colpan.addPanelFor(colSet, max);
            } else {
                if (colpan.getStringColSettings()[i].isColourable()) {
                    colpan.addPanelFor(colpan.getStringColSettings()[i], max);
                }
            }
        }
        // Colouring
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {

        // kind of a hack: the component only saves the history when
        // we allow it to save its value.
        NodeSettingsWO foo = new NodeSettings("foo");
        mFileComponent.saveSettingsTo(foo);

        String filename = mFilename.getStringValue();
        if ((filename == null) || (filename.length() == 0)) {
            throw new InvalidSettingsException("Please specify an output" + " filename.");
        }

        XLSWriterSettings vals = new XLSWriterSettings();
        vals.setFilename(mFilename.getStringValue());
        vals.setMissingPattern(mMissValue.getText());
        vals.setWriteColHeader(mWriteColHdr.isSelected());
        vals.setWriteRowID(mWriteRowHdr.isSelected());
        vals.setOverwriteOK(mOverwriteOK.isSelected());
        vals.setAutostart(mAutostart.isSelected());
        vals.setPivot(mPivot.isSelected());
        vals.setMergeRefs(mMergeCellReferences.getText());
        vals.setCreateNewFile(rdoCreateNewFile.isSelected());
        vals.setAbortOnExistingSheet(chkAbortSheetExists.isSelected());
        vals.setAbortOnMissingFile(chkAbortNoExists.isSelected());
        vals.setSheetname(txtSheetName.getText());
        vals.saveSettingsTo(settings);

        // Colouring
        NodeSettingsWO subSettings = settings.addNodeSettings(XLSWriterNodeModel.CFG_DOUBLECOLOR_CONFIG);

        for (int i = 0; i < colpan.getDoubleColSettings().length; i++) {
            DoubleColumnSettings colSet = colpan.getDoubleColSettings()[i];

            NodeSettingsWO subSub = subSettings.addNodeSettings(colSet.getName());
            colSet.saveSettingsTo(subSub);

        }
        NodeSettingsWO subSettings2 = settings.addNodeSettings(XLSWriterNodeModel.CFG_STRINGCOLOR_CONFIG);
        for (int i = 0; i < colpan.getStringColSettings().length; i++) {
            StringColumnSettings colSet = colpan.getStringColSettings()[i];

            NodeSettingsWO subSub = subSettings2.addNodeSettings(colSet.getName());
            colSet.saveSettingsTo(subSub);

        }

        // Colouring
    }

}
