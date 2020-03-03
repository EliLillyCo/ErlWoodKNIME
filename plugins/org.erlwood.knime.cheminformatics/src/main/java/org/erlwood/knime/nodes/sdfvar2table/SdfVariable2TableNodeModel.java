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
package org.erlwood.knime.nodes.sdfvar2table;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.workflow.FlowVariable;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import jp.co.infocom.cheminfo.marvin.type.MrvCell;
import jp.co.infocom.cheminfo.marvin.type.MrvCellFactory;

public class SdfVariable2TableNodeModel extends NodeModel {
	/** The config key for the string representation of the drawn molecules. */
	public static final String CFGKEY_MOLS_SD_STRING = "molecules_sd_string";

	private static final DataColumnSpecCreator MOL_CREATOR = new DataColumnSpecCreator("Molecule", MrvCell.TYPE);
	private static final DataTableSpec MOL_SPEC = new DataTableSpec(MOL_CREATOR.createSpec());
	
	private static final DataColumnSpecCreator RXN_CREATOR = new DataColumnSpecCreator("Reaction", MrvCell.TYPE);
	private static final DataTableSpec RXN_SPEC = new DataTableSpec(RXN_CREATOR.createSpec());
	
	public static final String CFG_VARIABLE_NAME 	= "variable_name";
	public static final String CFG_AROMATIZE 		= "aromatize";
	public static final String CFG_SPLIT	 		= "split";
	
	private final SettingsModelString  mVariableName = new SettingsModelString(CFG_VARIABLE_NAME, 	null);
	private final SettingsModelBoolean mAromatize 	 = new SettingsModelBoolean(CFG_AROMATIZE, 		true);
	private final SettingsModelBoolean mSplit	 	 = new SettingsModelBoolean(CFG_SPLIT, 			false);

	/**
	 * Constructor for the node model.
	 */
	protected SdfVariable2TableNodeModel() {
		// no incoming port and two outgoing ports - one for structures and one
		// for reactions required
		super(0, 2);		
	}

	/**
	 * This node has no configuration. Always ready to draw a structure.
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		if (mVariableName.getStringValue() == null) {
			throw new InvalidSettingsException("You must set a flow varible to use.");
		}
		return new DataTableSpec[] { MOL_SPEC, RXN_SPEC };
	}


	/**
	 * Parses the SD string stored inside m_molSDString setting (obtained during
	 * configure with Marvin Sketch) and outputs the molecules and reactions to
	 * the two output ports.
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {

		BufferedDataContainer molContainer = exec.createDataContainer(MOL_SPEC);
		BufferedDataContainer rxnContainer = exec.createDataContainer(RXN_SPEC);
		 
		FlowVariable fv = getAvailableFlowVariables().get(mVariableName.getStringValue());
		
		if (fv == null) {
			throw new IllegalStateException("Flow variable name '" + mVariableName.getStringValue() + "' is missing !");
		}
		
		String cssdf = fv.getStringValue();
		
		byte[] bytearray = cssdf.getBytes("ISO-8859-1");
		ByteArrayInputStream baos = new ByteArrayInputStream(bytearray);
		MolImporter importer = new MolImporter(baos, "cssdf");
		int molCount = 0, rxnCount = 0;
		
		for (Molecule readMol = importer.read(); null != readMol; readMol = importer.read()) {
			
			Molecule[] mols =  new Molecule[] { readMol };
			
			if (!readMol.isReaction() && mSplit.getBooleanValue()) {
				mols =  readMol.convertToFrags();
			}
			
			for (Molecule mol : mols) {
				if (!mol.isEmpty()) {
					
					if (mAromatize.getBooleanValue()) {
						mol.aromatize();
					}
					MrvCell st = (MrvCell) MrvCellFactory.create(mol);
					
					if (mol.isReaction()) {
						rxnCount += 1;
						RowKey key = new RowKey(Integer.toString(rxnCount));
						DefaultRow row = new DefaultRow(key, st);
						rxnContainer.addRowToTable(row);
						
					} else {
						molCount += 1;
						RowKey key = new RowKey(Integer.toString(molCount));
						DefaultRow row = new DefaultRow(key, st);
						molContainer.addRowToTable(row);
						
					}
				}
			}
		}
		
		importer.close();
		molContainer.close();
		rxnContainer.close();
		
		return new BufferedDataTable[] { molContainer.getTable(), rxnContainer.getTable() };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException { 
	}	

	@Override
	protected void reset() {
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException { 
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		
		mVariableName.loadSettingsFrom(settings);
		mAromatize.loadSettingsFrom(settings);
		mSplit.loadSettingsFrom(settings);
	}
	
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		
		mVariableName.saveSettingsTo(settings);
		mAromatize.saveSettingsTo(settings);
		mSplit.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		
		mVariableName.validateSettings(settings);
		mAromatize.validateSettings(settings);
		mSplit.validateSettings(settings);
	}

}
