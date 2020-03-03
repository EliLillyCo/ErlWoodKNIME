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
 *   Mar 15, 2007 (ohl): created
 */
package org.erlwood.knime.nodes.xlswriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * 
 * @author ohl, University of Konstanz
 * @author Luke Bullard
 */
public class XLSWriterNodeModel extends NodeModel {

    private XLSWriterSettings      mSettings              = null;
    public static final String     CFG_DOUBLECOLOR_CONFIG = "double_columns";
    public static final String     CFG_STRINGCOLOR_CONFIG = "string_columns";
    private DoubleColumnSettings[] cSettings;
    private StringColumnSettings[] sSsettings;

    /**
	 *
	 */
    public XLSWriterNodeModel() {
        super(1, 0);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        if (mSettings == null) {
            mSettings = new XLSWriterSettings();
        }
        if (cSettings == null) {
            cSettings = new DoubleColumnSettings[inSpecs[0].getNumColumns()];
        }
        // throws an Exception if things are not okay and sets a warning
        // message if file gets overridden.
        // checkFileAccess(m_settings.getFilename());
        return new DataTableSpec[] {};
    }

    /**
     * Helper that checks some properties for the file argument.
     * 
     * @param fileName
     *            the file to check
     * @throws InvalidSettingsException
     *             if that fails
     */
    private void checkFileAccess(final File file) throws InvalidSettingsException {
        if (file.exists() && mSettings.isCreateNewFile() && !mSettings.getOverwriteOK()) {
            String throwString = "File '" + file.getAbsolutePath() + "' exists, cannot overwrite";
            throw new InvalidSettingsException(throwString);
        }

        if (file.isDirectory()) {
            throw new InvalidSettingsException("\"" + file.getAbsolutePath() + "\" is a directory.");
        }

        if (!file.exists() && !mSettings.isCreateNewFile() && mSettings.isAbortOnMissingFile()) {
            throw new InvalidSettingsException("File '" + file.getAbsolutePath() + "' does not exist");
        }
        if (file.exists()) {
            setWarningMessage("Selected output file exists and will be overwritten!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {

        File file = new File(mSettings.getFilename());
        if ((file == null) || (file.getAbsolutePath().length() == 0)) {
            throw new InvalidSettingsException("No output file specified.");
        }

        checkFileAccess(file);   

        XLSWriter xlsWriter = new XLSWriter(mSettings, cSettings, sSsettings);
        if (mSettings.getPivot()) {
            xlsWriter.writePivoted(inData[0], exec);
        } else {
            xlsWriter.write(inData[0], exec);
        }

        return new BufferedDataTable[] {};

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to save
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        mSettings = new XLSWriterSettings(settings);
        cSettings = loadDoubleColourSettings(settings);
        sSsettings = loadStringColourSettings(settings);
    }

    private DoubleColumnSettings[] loadDoubleColourSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        NodeSettingsRO subSettings = settings.getNodeSettings(CFG_DOUBLECOLOR_CONFIG);
        List<DoubleColumnSettings> result = new ArrayList<DoubleColumnSettings>();
        for (String identifier : subSettings) {
            NodeSettingsRO col = subSettings.getNodeSettings(identifier);
            result.add(DoubleColumnSettings.createFrom(col));
        }
        return result.toArray(new DoubleColumnSettings[0]);
    }

    private StringColumnSettings[] loadStringColourSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        NodeSettingsRO subSettings = settings.getNodeSettings(CFG_STRINGCOLOR_CONFIG);
        List<StringColumnSettings> result = new ArrayList<StringColumnSettings>();
        for (String identifier : subSettings) {
            NodeSettingsRO col = subSettings.getNodeSettings(identifier);
            result.add(StringColumnSettings.createFrom(col));
        }
        return result.toArray(new StringColumnSettings[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to reset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to save
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (mSettings != null) {
            mSettings.saveSettingsTo(settings);

        }
        NodeSettingsWO subSettings = settings.addNodeSettings(CFG_DOUBLECOLOR_CONFIG);
        if (mSettings != null) {
            for (DoubleColumnSettings set : cSettings) {
                NodeSettingsWO subSub = subSettings.addNodeSettings(set.getName().toString());
                set.saveSettingsTo(subSub);
            }
        }
        NodeSettingsWO subSettings2 = settings.addNodeSettings(CFG_STRINGCOLOR_CONFIG);
        if (mSettings != null) {
            for (StringColumnSettings set : sSsettings) {
                NodeSettingsWO subSub = subSettings2.addNodeSettings(set.getName().toString());
                set.saveSettingsTo(subSub);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        XLSWriterSettings xlsSettings = new XLSWriterSettings(settings);

        String filename = xlsSettings.getFilename();
        if ((filename == null) || (filename.length() == 0)) {
            throw new InvalidSettingsException("No output" + " filename specified.");
        }

        if (xlsSettings.getMergeRefs() != null && !xlsSettings.getMergeRefs().isEmpty()) {
            String[] split = xlsSettings.getMergeRefs().split(",");
            for (String s : split) {
                try {
                    CellRangeAddress.valueOf(s);
                } catch (Exception ex) {
                    throw new InvalidSettingsException("Invalid Merge References.");
                }
            }
        }
    }

}
