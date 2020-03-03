/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
 *   Apr 8, 2009 (ohl): created
 */
package org.erlwood.knime.nodes.xlsenumeratenamedranges;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.FilesHistoryPanel;

/**
 * The dialog to the XLS sheet reader.
 *
 * @author Patrick Winter, KNIME.com, Zurich, Switzerland
 */
public class XLSEnumerateNamedRangesNodeDialog extends NodeDialogPane {

    private final FilesHistoryPanel m_fileName = new FilesHistoryPanel("XLSNamedRange", ".xls", ".xlsx");

    /**
     * Creates the dialog with its components.
     */
    public XLSEnumerateNamedRangesNodeDialog() {
     
        JPanel dlgTab = new JPanel();
        dlgTab.setLayout(new BoxLayout(dlgTab, BoxLayout.Y_AXIS));
        JComponent fileBox = getFileBox();
        fileBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Select file to read:"));
        dlgTab.add(fileBox);
        addTab("XLS Named Range Settings", dlgTab);

    }

    /**
     * Creates a box with the file selection panel.
     */
    private JComponent getFileBox() {
        Box fBox = Box.createHorizontalBox();
        fBox.add(Box.createHorizontalGlue());
        fBox.add(m_fileName);
        fBox.add(Box.createHorizontalGlue());
        return fBox;
    }

    /**
     * Creates an XLSSheetReaderSettings object based on the settings in the panels.
     *
     * @return The settings object
     */
    private XLSEnumerateNamedRangesSettings createSettingsFromComponents() {
    	XLSEnumerateNamedRangesSettings s = new XLSEnumerateNamedRangesSettings();
        s.setFileLocation(m_fileName.getSelectedFile());
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        String file = m_fileName.getSelectedFile();
        if (file == null || file.isEmpty()) {
            throw new InvalidSettingsException("Please select a file to read from.");
        }
        XLSEnumerateNamedRangesSettings s = createSettingsFromComponents();
        String errMsg = s.getStatus(true);
        if (errMsg != null) {
            throw new InvalidSettingsException(errMsg);
        }
        s.save(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
            throws NotConfigurableException {
    	XLSEnumerateNamedRangesSettings s;
        try {
            s = XLSEnumerateNamedRangesSettings.load(settings);
        } catch (InvalidSettingsException e) {
            s = new XLSEnumerateNamedRangesSettings();
        }
        m_fileName.setSelectedFile(s.getFileLocation());
    }

}
