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
package org.erlwood.knime.nodes.molfilewriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.co.infocom.cheminfo.marvin.type.MrvValue;

import org.knime.bio.types.PdbCell;
import org.knime.chem.types.CMLCell;
import org.knime.chem.types.Mol2Cell;
import org.knime.chem.types.MolCell;
import org.knime.chem.types.SdfCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

/**
 * This is the model implementation of MolFileWriter. Writes chemical structures
 * to a file.
 * 
 * @author Dimitar Hristozov
 */
public class MolFileWriterNodeModel extends NodeModel {

	public static final String CFG_FILE_NAME = "MolFileWriter_file_name";
	public static final String CFG_FILE_FORMAT = "MolFileWriter_file_format";
	public static final String CFG_MOL_COLUMN = "MolFileWriter_mol_column";
	public static final String CFG_PROPS = "MolFileWriter_props_to_write";
	public static final String CFG_VERBATIM = "MolFileWriter_write_verbatim";

	private final SettingsModelString mFilename = new SettingsModelString(
			MolFileWriterNodeModel.CFG_FILE_NAME, "");
	private final SettingsModelString mFileformat = new SettingsModelString(
			MolFileWriterNodeModel.CFG_FILE_FORMAT, "sdf");
	private final SettingsModelString mMolColumn = new SettingsModelString(
			MolFileWriterNodeModel.CFG_MOL_COLUMN, "");
	private final SettingsModelBoolean mVerbatim = new SettingsModelBoolean(
			MolFileWriterNodeModel.CFG_VERBATIM, false);
	private final SettingsModelFilterString mProps = new SettingsModelFilterString(
			MolFileWriterNodeModel.CFG_PROPS);

	/**
	 * Constructor for the node model.
	 */
	protected MolFileWriterNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		BufferedDataTable objInData = inData[0];
		DataTableSpec inSpec = objInData.getDataTableSpec();
		int molColi = inSpec.findColumnIndex(mMolColumn.getStringValue());

		if (!inSpec.getColumnSpec(molColi).getType().isCompatible(MrvValue.class)) {
			if (mVerbatim.getBooleanValue()) {
				writeVerbatim(objInData, molColi, exec);
			} else {
				writeThroughChemaxon(objInData, molColi, exec);
			}
		} else {
			writeThroughChemaxon(objInData, molColi, exec);
		}

		return new BufferedDataTable[] { objInData };
	}

	private void writeVerbatim(final BufferedDataTable inData, int molCol,
			final ExecutionContext exec) throws Exception {
		BufferedWriter out = new BufferedWriter(new FileWriter(
				mFilename.getStringValue()));
		double nr = inData.getRowCount();
		int cr = 0;
		for (DataRow row : inData) {
			out.write(row.getCell(molCol).toString());
			out.newLine();
			cr += 1;
			exec.setProgress(cr / nr);
		}
		out.flush();
		out.close();
	}

	private void writeThroughChemaxon(final BufferedDataTable inData,
			int molCol, final ExecutionContext exec) throws Exception {
		DataTableSpec inSpec = inData.getDataTableSpec();
		List<Integer> iPropCols = new ArrayList<Integer>();
		for (String col : mProps.getIncludeList()) {
			int toadd = inSpec.findColumnIndex(col);
			if (toadd != molCol) {
				iPropCols.add(toadd);
			}
		}
		double nr = inData.getRowCount();
		int cr = 0;
		DataType inMolType = inSpec.getColumnSpec(molCol).getType();

		FileOutputStream ous = new FileOutputStream(mFilename.getStringValue());
		MolExporter exp = new MolExporter(ous, mFileformat.getStringValue());
		for (DataRow row : inData) {
			DataCell molCell = row.getCell(molCol);
			Molecule mol = null;
			if (inMolType.isCompatible(MrvValue.class)) {
				mol = ((MrvValue) molCell).getMolecule();
			} else {
				String frm = null;
				if (inMolType == SdfCell.TYPE) {
					frm = "sdf";
				} else if (inMolType == MolCell.TYPE) {
					frm = "mol";
				} else if (inMolType == PdbCell.TYPE) {
					frm = "pdb";
				} else if (inMolType == Mol2Cell.TYPE) {
					frm = "mol2";
				} else if (inMolType == CMLCell.TYPE) {
					frm = "cml";
				}
				mol = MolImporter.importMol(molCell.toString(), frm, null);
			}

			for (int i : iPropCols) {
				mol.setProperty(inSpec.getColumnSpec(i).getName(),
						row.getCell(i).toString());
			}

			exp.write(mol);
			cr += 1;
			exec.setProgress(cr / nr);
		}
		exp.flush();
		exp.close();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		return inSpecs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		mFilename.saveSettingsTo(settings);
		mFileformat.saveSettingsTo(settings);
		mMolColumn.saveSettingsTo(settings);
		mProps.saveSettingsTo(settings);
		mVerbatim.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		mFilename.loadSettingsFrom(settings);
		mFileformat.loadSettingsFrom(settings);
		mMolColumn.loadSettingsFrom(settings);
		mProps.loadSettingsFrom(settings);
		mVerbatim.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		mFilename.validateSettings(settings);
		mFileformat.validateSettings(settings);
		mMolColumn.validateSettings(settings);
		mProps.validateSettings(settings);
		mVerbatim.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

}
