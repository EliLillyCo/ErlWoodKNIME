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
package org.erlwood.knime.nodes.marvinsketch;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.chem.types.SdfCell;
import org.knime.chem.types.SdfCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
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

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import jp.co.infocom.cheminfo.marvin.type.MrvCell;
import jp.co.infocom.cheminfo.marvin.type.MrvCellFactory;

public class MarvinSketchNodeModel extends NodeModel {
	/** The config key for the string representation of the drawn molecules. */
	public static final String CFGKEY_MOLS_SD_STRING = "molecules_sd_string";

	private final SettingsModelString mMolSDString = new SettingsModelString(
			CFGKEY_MOLS_SD_STRING, null);
	private List<Molecule> mMolecules;
	private List<Molecule> mRxns;

	public static final String CFG_AROMATIZE 		= "aromatize";
	public static final String CFG_ADD_SDF_COLUMN 	= "add_Sdf_column";
	
	
	
	private final SettingsModelBoolean mAromatize = new SettingsModelBoolean(
			CFG_AROMATIZE, true);
	private final SettingsModelBoolean mAddSdfColumn = new SettingsModelBoolean(
			CFG_ADD_SDF_COLUMN, false);

	/**
	 * Constructor for the node model.
	 */
	protected MarvinSketchNodeModel() {
		// no incoming port and two outgoing ports - one for structures and one
		// for reactions required
		super(0, 2);
		mMolecules = new ArrayList<Molecule>();
		mRxns = new ArrayList<Molecule>();
	}

	private DataTableSpec getMolSpec() {
		DataTableSpecCreator molTableSpecCreator = new DataTableSpecCreator();
		molTableSpecCreator.addColumns(new DataColumnSpecCreator("Molecule", MrvCell.TYPE).createSpec());
		
		if (mAddSdfColumn.getBooleanValue()) {
			molTableSpecCreator.addColumns(new DataColumnSpecCreator("Molecule_Sdf", SdfCell.TYPE).createSpec());
		} 
		return molTableSpecCreator.createSpec();
	}
	
	private DataTableSpec getRxnSpec() {
		DataTableSpecCreator rxnTableSpecCreator = new DataTableSpecCreator();
		rxnTableSpecCreator.addColumns(new DataColumnSpecCreator("Reaction", MrvCell.TYPE).createSpec());
		
		if (mAddSdfColumn.getBooleanValue()) {
			rxnTableSpecCreator.addColumns(new DataColumnSpecCreator("Reaction_Sdf", SdfCell.TYPE).createSpec());
		} 
		return rxnTableSpecCreator.createSpec();
	}
	
	/**
	 * This node has no configuration. Always ready to draw a structure.
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		
		return new DataTableSpec[] { getMolSpec(), getRxnSpec() };
	}

	public String getMolsAsString() {
		return mMolSDString.getStringValue();
	}

	public List<Molecule> getMolecules() {
		return mMolecules;
	}

	public List<Molecule> getReactions() {
		return mRxns;
	}

	protected void populateLists() {
		try {
			mMolecules = new ArrayList<Molecule>();
			mRxns = new ArrayList<Molecule>();
			String cssdf = mMolSDString.getStringValue();
			byte[] bytearray = cssdf.getBytes("ISO-8859-1");
			ByteArrayInputStream baos = new ByteArrayInputStream(bytearray);
			MolImporter importer = new MolImporter(baos, "cssdf");
			for (Molecule readMol = importer.read(); null != readMol; readMol = importer
					.read()) {
				readMol.aromatize();
				if (readMol.isReaction()) {
					mRxns.add(readMol);
				} else {
					mMolecules.add(readMol);
				}
			}
		} catch (Exception e) {
			// skip
		}
	}

	/**
	 * Parses the SD string stored inside m_molSDString setting (obtained during
	 * configure with Marvin Sketch) and outputs the molecules and reactions to
	 * the two output ports.
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {

		BufferedDataContainer molContainer = exec.createDataContainer(getMolSpec());
		BufferedDataContainer rxnContainer = exec.createDataContainer(getRxnSpec());
		
		mMolecules = new ArrayList<Molecule>();
		mRxns = new ArrayList<Molecule>();

		String cssdf = mMolSDString.getStringValue();
		byte[] bytearray = cssdf.getBytes("ISO-8859-1");
		ByteArrayInputStream baos = new ByteArrayInputStream(bytearray);
		MolImporter importer = new MolImporter(baos, "cssdf");
		int molCount = 0, rxnCount = 0;
		for (Molecule readMol = importer.read(); null != readMol; readMol = importer
				.read()) {
			if (mAromatize.getBooleanValue()) {
				readMol.aromatize();
			}
			List<DataCell> cells = new ArrayList<DataCell>();
			
			cells.add((MrvCell) MrvCellFactory.create(readMol));
			
			if (mAddSdfColumn.getBooleanValue()) {
				cells.add(SdfCellFactory.create(MolExporter.exportToFormat(readMol, "SDF")));
			}
			if (readMol.isReaction()) {
				rxnCount += 1;
				RowKey key = new RowKey(Integer.toString(rxnCount));
				DefaultRow row = new DefaultRow(key, cells);
				rxnContainer.addRowToTable(row);
				mRxns.add(readMol);
			} else {
				molCount += 1;
				RowKey key = new RowKey(Integer.toString(molCount));
				DefaultRow row = new DefaultRow(key, cells);
				molContainer.addRowToTable(row);
				mMolecules.add(readMol);
			}
		}

		molContainer.close();
		rxnContainer.close();
		return new BufferedDataTable[] { molContainer.getTable(),
				rxnContainer.getTable() };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException { 
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		mMolSDString.loadSettingsFrom(settings);
		mAromatize.loadSettingsFrom(settings);
		try {
			mAddSdfColumn.loadSettingsFrom(settings);
		} catch(Exception ex) {
			//	Do nothing
		}
		populateLists();
	}

	@Override
	protected void reset() {
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException { 
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		mMolSDString.saveSettingsTo(settings);
		mAromatize.saveSettingsTo(settings);
		mAddSdfColumn.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		mMolSDString.validateSettings(settings);
		mAromatize.validateSettings(settings);
		
		try {
			mAddSdfColumn.validateSettings(settings);
		} catch(Exception ex) {
			//	Do nothing
		}
	}

}
