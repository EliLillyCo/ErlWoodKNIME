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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.co.infocom.cheminfo.marvin.type.MrvCell;
import jp.co.infocom.cheminfo.marvin.type.MrvCellFactory;
import jp.co.infocom.cheminfo.marvin.type.MrvValue;

import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.erlwood.knime.utils.KnimenodeUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
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
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import chemaxon.struc.Molecule;

/**
 * This is the model implementation of MoleculeEditor. Allows to edit existing
 * molecules and to select atoms.
 * 
 * @author Nikolas Fechner, Dimitar Hristotov
 */
public class MoleculeEditorNodeModel extends NodeModel {
	/** The config key for the string representation of the drawn molecules. */
	public static final String CFGKEY_MOLS_SD_STRING = "molecules_sd_string";
	public static final String COLUMN = "column";

	static final String CFG_REPLACE = "replace_input";
	static final String CFG_NEW_COL_NAME = "new_col_name";
	static final String CFG_UNLIKELY_COL_NAME = "justacolumnwithveryunlikelynameFCUK";
	static final String CFG_RESET_PREVIOUS = "reset_previous";
	private final SettingsModelString mReplace = new SettingsModelString(
			CFG_REPLACE, "Append");
	private final SettingsModelString mNewColName = new SettingsModelString(
			CFG_NEW_COL_NAME, "Molecule");
	private final SettingsModelBoolean mResetPrevious = new SettingsModelBoolean(
			CFG_RESET_PREVIOUS, false);

	private static final DataColumnSpecCreator MOL_CREATOR = new DataColumnSpecCreator(
			"Molecule", MrvCell.TYPE);
	private static final DataTableSpec MOL_SPEC = new DataTableSpec(
			MOL_CREATOR.createSpec());
	private static final DataColumnSpecCreator RXN_CREATOR = new DataColumnSpecCreator(
			"Reaction", MrvCell.TYPE);
	private static final DataTableSpec RXN_SPEC = new DataTableSpec(
			RXN_CREATOR.createSpec());

	private final SettingsModelString mColumn = new SettingsModelString(COLUMN,
			"Molecule");
	private List<Molecule> mMolecules;
	private List<Molecule> mRxns;
	private Map<RowKey, Integer> h = new HashMap<RowKey, Integer>();
	private Map<RowKey, Molecule> mPreviousMols = new HashMap<RowKey, Molecule>();

	public static final String CFG_AROMATIZE = "aromatize";

	/**
	 * Constructor for the node model.
	 */
	protected MoleculeEditorNodeModel() {
		// no incoming port and two outgoing ports - one for structures and one
		// for reactions required
		super(1, 1);
		mMolecules = new ArrayList<Molecule>();
		mRxns = new ArrayList<Molecule>();
	}

	/**
	 * This node has no configuration. Always ready to draw a structure.
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		String newName = mNewColName.getStringValue();
		if (!"Append".equals(mReplace.getStringValue())) {
			newName = mColumn.getStringValue();
		} else {
			if ("".equals(newName)) {
				newName = "Molecule";
			}
			newName = KnimenodeUtils.createColumnName(newName, inSpecs[0]);
		}
		mNewColName.setStringValue(newName);
		DataColumnSpecCreator appSpecCreator = new DataColumnSpecCreator(
				newName, MrvCell.TYPE);
		final DataColumnSpec appSpec = appSpecCreator.createSpec();
		List<DataColumnSpec> cspecs = new ArrayList<DataColumnSpec>();
		for (int i = 0; i < inSpecs[0].getNumColumns(); i++) {
			cspecs.add(inSpecs[0].getColumnSpec(i));
		}

		// get the index for the selected column
		final int colInd = inSpecs[0].findColumnIndex(mColumn.getStringValue());
		if (colInd == -1) {
			throw new InvalidSettingsException("Cannot find column '"
					+ mColumn.getStringValue() + "' in input.");
		}

		if (!"Append".equals(mReplace.getStringValue())) {
			cspecs.set(colInd, appSpec);
		} else {
			cspecs.add(appSpec);
		}
		return new DataTableSpec[] { new DataTableSpec(
				cspecs.toArray(new DataColumnSpec[cspecs.size()])) };
	}

	public List<Molecule> getMolecules() {
		return mMolecules;
	}

	public List<Molecule> getReactions() {
		return mRxns;
	}

	/**
	 * Parses the SD string stored inside m_molSDString setting (obtained during
	 * configure with Marvin Sketch) and outputs the molecules and reactions to
	 * the two output ports.
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {

		h.clear();
		BufferedDataContainer molContainer = exec.createDataContainer(MOL_SPEC);
		BufferedDataContainer rxnContainer = exec.createDataContainer(RXN_SPEC);
		mMolecules = new ArrayList<Molecule>();
		mRxns = new ArrayList<Molecule>();
		int index = inData[0].getDataTableSpec().findColumnIndex(
				mColumn.getStringValue());
		RowIterator rows = inData[0].iterator();

		int molCount = 0;
		while (rows.hasNext()) {
			DataRow row = rows.next();
			MrvValue val = MoleculeDataTypeConverter.getValue(row.getCell(index), MrvValue.class);
			
			Molecule readMol = val.getMolecule();

			RowKey rowkey = row.getKey();
			if (!mResetPrevious.getBooleanValue()
					&& mPreviousMols.containsKey(rowkey)) {
				readMol = mPreviousMols.get(rowkey);
			}
			if (readMol.isReaction()) {
				mRxns.add(readMol);
			} else {
				h.put(row.getKey(), molCount);
				molCount += 1;
				mMolecules.add(readMol);

			}

		}
		MoleculeEditor editor = new MoleculeEditor(mMolecules);

		while (!editor.isReady()) {
			Thread.sleep(1000);
		}

		molContainer.close();
		rxnContainer.close();

		mMolecules = editor.getMolecules();

		mPreviousMols.clear();

		ColumnRearranger c = createColumnRearranger(inData[0]
				.getDataTableSpec());
		BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], c,
				exec);
		return new BufferedDataTable[] { out };
	}

	private ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {
		ColumnRearranger result = new ColumnRearranger(spec);
		String newName = mNewColName.getStringValue();
		if (!"Append".equals(mReplace.getStringValue())) {
			newName = mColumn.getStringValue();
		} else {
			if ("".equals(newName)) {
				newName = "Molecule";
			}
			newName = KnimenodeUtils.createColumnName(newName, spec);
		}
		mNewColName.setStringValue(newName);
		DataColumnSpecCreator appSpecCreator = new DataColumnSpecCreator(
				newName, MrvCell.TYPE);
		final DataColumnSpec appSpec = appSpecCreator.createSpec();
		final int colInd = result.indexOf(mColumn.getStringValue());
		SingleCellFactory scFactory = new SingleCellFactory(appSpec) {
			public DataCell getCell(final DataRow row) {
				RowKey key = row.getKey();
				Molecule mol = mMolecules.get(h.get(key));
				MrvCell chime = (MrvCell) MrvCellFactory.create(mol);
				if (mol != null) {
					mPreviousMols.put(key, mol);
				}

				return chime;

			}
		};
		if ("Append".equals(mReplace.getStringValue())) {
			result.append(scFactory);
		} else {
			result.replace(scFactory, mColumn.getStringValue());
		}
		return result;
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		try {
			File f = new File(nodeInternDir, "AtomSelector");
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream in = new ObjectInputStream(fis);
			Object[][] obj = (Object[][]) in.readObject();
			mPreviousMols = arrayToHashmap(obj);
			in.close();
		} catch (Exception e) {

		}
	}

	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		try {
			mColumn.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// this node instance is using the old column model
			SettingsModelColumnName temp = new SettingsModelColumnName(COLUMN,
					"Molecule");
			temp.loadSettingsFrom(settings);
			if (!temp.useRowID()) {
				mColumn.setStringValue(temp.getStringValue());
			} else {
				mColumn.setStringValue(null);
			}
		}

		mReplace.loadSettingsFrom(settings);
		mNewColName.loadSettingsFrom(settings);
		mResetPrevious.loadSettingsFrom(settings);

	}

	@Override
	protected void reset() {
		if (mResetPrevious.getBooleanValue()) {
			mPreviousMols.clear();
		}
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		try {
			Object[][] obj = hashmapToArray(mPreviousMols);
			File f = new File(nodeInternDir, "AtomSelector");
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(obj);
			out.close();
		} catch (Exception e) {
			throw new IOException(
					"Unable to save internals: " + e.getMessage(), e);
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		mColumn.saveSettingsTo(settings);
		mReplace.saveSettingsTo(settings);
		mNewColName.saveSettingsTo(settings);
		mResetPrevious.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			mColumn.validateSettings(settings);
		} catch (InvalidSettingsException e) {
			// this node instance is using the old column model
			SettingsModelColumnName temp = new SettingsModelColumnName(COLUMN,
					"Molecule");
			temp.validateSettings(settings);
		}

		mReplace.validateSettings(settings);
		mNewColName.validateSettings(settings);
		mResetPrevious.validateSettings(settings);
	}

	private Object[][] hashmapToArray(Map<RowKey, Molecule> map) {
		Object[][] obj = new Object[2][map.size()];
		Set<RowKey> keys = map.keySet();
		int r = 0;
		for (RowKey k : keys) {
			obj[0][r] = k.toString();
			obj[1][r] = map.get(k);
			r++;
		}
		return obj;
	}

	private Map<RowKey, Molecule> arrayToHashmap(Object[][] obj) {
		Map<RowKey, Molecule> map = new HashMap<RowKey, Molecule>();
		for (int i = 0; i < obj[0].length; i++) {
			map.put(new RowKey((String) obj[0][i]), (Molecule) obj[1][i]);
		}
		return map;
	}

}
