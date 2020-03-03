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
package org.erlwood.knime.nodes.annotatemolecules;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.erlwood.knime.utils.KnimenodeUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.MultiLineStringValueRenderer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;

/**
 * This is the model implementation of SelectAnnotate.
 * 
 * 
 * @author Luke Bullard
 */
public class AnnotateMoleculesNodeModel extends NodeModel {

    // the logger instance
    private static final NodeLogger LOG                        = NodeLogger.getLogger(AnnotateMoleculesNodeModel.class);

    public static final String CFG_ROWKEYS 			= "CFG_ROWKEYS";
    public static final String CFG_COMMENTS 		= "CFG_COMMENTS";
	public static final String CFG_TABLE_HASHCODE 	= "CFG_TABLE_HASHCODE";
    

	private Map<RowKey, String> 	comments = new HashMap<RowKey, String>();

	private long tableHashCode;
    /**
     * Constructor for the node model.
     */
    protected AnnotateMoleculesNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final PortObject[] inPort, final ExecutionContext exec) throws Exception {
    	long hc = KnimenodeUtils.calculateHashCode((BufferedDataTable) inPort[0]);
    	
    	if (hc != tableHashCode) {
    		throw new IllegalStateException("You must run node configure before executing!");
    	}
    	
        final Set<RowKey> rk = comments.keySet();
        CellFactory cellFactory = new SingleCellFactory(createSpec()) {
            @Override
            public DataCell getCell(DataRow row) {
                if (rk.contains(row.getKey())) {
                    return new StringCell(comments.get(row.getKey()));
                } else {
                    return new StringCell("");
                }
            }
        };
        
        // create the column rearranger
        ColumnRearranger outputTable = new ColumnRearranger(((BufferedDataTable) inPort[0]).getDataTableSpec());
        
        // append the new column
        outputTable.append(cellFactory);

        return new BufferedDataTable[] { exec.createColumnRearrangeTable((BufferedDataTable) inPort[0], outputTable, exec) };
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {

    }

    private DataColumnSpec createSpec() {
    	DataColumnSpecCreator dcs = new DataColumnSpecCreator("Comments", StringCell.TYPE);
    	dcs.setProperties(new DataColumnProperties(Collections.singletonMap(DataValueRenderer.PROPERTY_PREFERRED_RENDERER,
                new MultiLineStringValueRenderer.Factory().getDescription())));

    	return dcs.createSpec();
    }
    
    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
    	DataTableSpecCreator c = new DataTableSpecCreator(inSpecs[0]);
    	    	
    	c.addColumns(createSpec());
    	
    	return new DataTableSpec[] { c.createSpec() };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	RowKey[] rowKeys = new RowKey[comments.size()];
		String[] cmts = new String[comments.size()];
		int i = 0;
		for (Entry<RowKey, String> es : comments.entrySet()) {
			rowKeys[i] = es.getKey();
			cmts[i] = es.getValue();
			i++;
		}
		
		settings.addRowKeyArray(CFG_ROWKEYS,  rowKeys);
		settings.addStringArray(CFG_COMMENTS, cmts);
		settings.addLong(CFG_TABLE_HASHCODE, tableHashCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
    	
			RowKey[] rowKeys = settings.getRowKeyArray(CFG_ROWKEYS);
			String[] cmts = settings.getStringArray(CFG_COMMENTS);
			
			comments.clear();
			for (int i = 0; i < rowKeys.length; i++) {
				comments.put(rowKeys[i], cmts[i]);
			}
			tableHashCode = settings.getLong(CFG_TABLE_HASHCODE);
	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
       
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
       
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
      
    }

    

}
