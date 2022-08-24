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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.SpreadsheetVersion;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeCreationContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.util.FileUtil;

/**
 * @author Patrick Winter, KNIME.com, Zurich, Switzerland
 */
public class XLSEnumerateNamedRangesNodeModel extends NodeModel {
	private static final NodeLogger LOG = NodeLogger.getLogger(XLSEnumerateNamedRangesNodeModel.class);
    private XLSEnumerateNamedRangesSettings m_settings = new XLSEnumerateNamedRangesSettings();

    /**
     * Creates the node model.
     */
    public XLSEnumerateNamedRangesNodeModel() {
        super(0, 1);     
    }

    /**
     * Creates the node model with the given context.
     *
     * @param context The context of this node instance
     */
    XLSEnumerateNamedRangesNodeModel(final NodeCreationContext context) {
        this();
        m_settings.setFileLocation(context.getUrl().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        /*
         * This is a special "deal" for the reader: The reader, if previously
         * executed, has data at it's output - even if the file that was read
         * doesn't exist anymore. In order to warn the user that the data cannot
         * be recreated we check here if the file exists and set a warning
         * message if it doesn't.
         */
        String fName = m_settings.getFileLocation();
        if (fName == null || fName.isEmpty()) {
            return;
        }
        try {
            new URL(fName);
            // don't check URLs - don't open a stream.
            return;
        } catch (MalformedURLException mue) {
            // continue on a file
        }
        File location = new File(fName);
        if (!location.canRead() || location.isDirectory()) {
            setWarningMessage("The file '" + location.getAbsolutePath() + "' can't be accessed anymore!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings = XLSEnumerateNamedRangesSettings.load(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_settings != null) {
            m_settings.save(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    	XLSEnumerateNamedRangesSettings.load(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        if (m_settings == null) {
            throw new InvalidSettingsException("Node not configured.");
        }
        String errMsg = m_settings.getStatus(true);
        if (errMsg != null) {
            throw new InvalidSettingsException(errMsg);
        }
        return new DataTableSpec[]{createOutSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
            throws Exception {
        Workbook wb = getWorkbook(m_settings.getFileLocation());
        
        Map<String, Set<String>> namedRanges = new TreeMap<String, Set<String>>();
        
        try {
            for(Name namedRange : wb.getAllNames()) {
        		String name = namedRange.getNameName();
        		if (name.contains("Print_Area") || name.contains("PrintArea")) {
        			continue;
        		}
        		String sheetName;
        		AreaReference[] refs;
        		
        		//	Test for a cell based name
        		try {
        			namedRange.getSheetName();        			
        			AreaReference.generateContiguous(SpreadsheetVersion.EXCEL2007,namedRange.getRefersToFormula());
        		} catch(IllegalArgumentException iax) {
        			continue;
        		} catch(IllegalStateException isx) {
        			continue;
        		}
        		
    			if (AreaReference.isContiguous(namedRange.getRefersToFormula())) {
    				sheetName = namedRange.getSheetName();
    				addNamedRange(namedRanges, sheetName, name);    				    			
    			} else {
    				refs = AreaReference.generateContiguous(SpreadsheetVersion.EXCEL2007,name);
    				for (int j = 0; j < refs.length; j++) {
    					sheetName = refs[j].getFirstCell().getSheetName();
    					addNamedRange(namedRanges, sheetName, name);    					
    				}
    			}
            }
        }
        catch (Exception e) {
        	LOG.error(e.getMessage(), e);
        	throw e;
        }
        	
        DataTableSpec spec = createOutSpec();
        BufferedDataContainer outContainer = exec.createDataContainer(spec);
        int index = 0;
        for (Entry<String, Set<String>> es  : namedRanges.entrySet()) {
        	for (String name : es.getValue()) {
        		outContainer.addRowToTable(new DefaultRow(RowKey.createRowKey(index++), new StringCell(es.getKey()), new StringCell(name)));
        	}
        }
        outContainer.close(); 
        return new BufferedDataTable[]{outContainer.getTable()}; 
    }

    /**
     * Adds the sheet name and named rage name to the map.
     * @param namedRanges The Map to adjust
     * @param sheet The sheet name
     * @param name The named range name
     */
    private void addNamedRange(Map<String, Set<String>> namedRanges, String sheet, String name) {
    	if(sheet != null) {
	    	Set<String> names = namedRanges.get(sheet);
			if (names == null) {
				names= new TreeSet<String>();
				namedRanges.put(sheet, names);
			}
			names.add(name);
    	}
    }
    
    /**
     * Creates the specs of the output table.
     *
     * @return Specs of the output table.
     */
    private DataTableSpec createOutSpec() {
        return new DataTableSpec(new DataColumnSpecCreator("Sheet Name", StringCell.TYPE).createSpec(), new DataColumnSpecCreator("Named Range", StringCell.TYPE).createSpec());
    }

    /**
     * Opens and returns a new buffered input stream on the passed location. The
     * location could either be a filename or a URL.
     *
     * @param location a filename or a URL
     * @return a new opened buffered input stream.
     * @throws IOException
     */
    private static BufferedInputStream getBufferedInputStream(
            final String location) throws IOException {
        InputStream in;
        try {
            URL url = new URL(location);
            in = FileUtil.openStreamWithTimeout(url);
        } catch (MalformedURLException mue) {
            // then try a file
            in = new FileInputStream(location);
        }
        return new BufferedInputStream(in);

    }
    
    /**
     * Loads a workbook from the file system.
     *
     * @param path Path to the workbook
     * @return The workbook
     * @throws IOException If the file could not be accessed
     * @throws InvalidFormatException If the file does not have the right format
     */
    private static Workbook getWorkbook(final String path) throws IOException, InvalidFormatException {
        Workbook workbook = null;
        InputStream in = null;
        try {
            in = getBufferedInputStream(path);
            workbook = WorkbookFactory.create(in);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e2) {
                    // ignore
                }
            }
        }
        return workbook;
    }

}
