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
package org.erlwood.knime.nodes.molfilereader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import jp.co.infocom.cheminfo.marvin.type.MrvCell;
import jp.co.infocom.cheminfo.marvin.type.MrvCellFactory;

import org.erlwood.knime.nodes.rxnfilereader.RxnFileReaderNodeModel;
import org.erlwood.knime.utils.KnimenodeUtils;
import org.erlwood.knime.utils.nodes.ChemContentsNodeModel;
import org.knime.bio.types.PdbCell;
import org.knime.chem.types.CMLCell;
import org.knime.chem.types.CMLCellFactory;
import org.knime.chem.types.CtabCell;
import org.knime.chem.types.CtabCellFactory;
import org.knime.chem.types.Mol2Cell;
import org.knime.chem.types.Mol2CellFactory;
import org.knime.chem.types.SdfCell;
import org.knime.chem.types.SdfCellFactory;
import org.knime.chem.types.SmilesCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.MultiLineStringValueRenderer;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import chemaxon.struc.MoleculeGraph;

/**
 * This is the model implementation of MolFileReader. Reads chemical structure
 * files using ChemAxon library. Outputs a column of type ChemAxonMolecule.
 * 
 * @author Hina Patel, Dimitar Hristozov
 */
public class MolFileReaderNodeModel extends ChemContentsNodeModel {
	// the logger instance
	private static final NodeLogger LOG = NodeLogger
			.getLogger(MolFileReaderNodeModel.class);

	public static final String CFG_FILENAME 		= "mol_file_reader_filename";
	public static final String CFG_FILEPROPS 		= "mol_file_reader_properties";
	public static final String CFG_OUT_OPTIONS 		= "mol_file_reader_output_options";
	public static final String CFG_OUT_COLS 		= "mol_file_reader_output_columns";
	public static final String CFG_IS_SHALLOW_SCAN  = "mol_file_reader_shallow_scan";
    public static final String CFG_LAST_DIRECTORY  	= "mol_file_reader_last_directory";
    public static final String CFG_FOUND_PROPS   	= "mol_file_reader_founds_props";
    public static final String CFG_REQUIRES_SCAN   	= "mol_file_reader_requires_scan";
    
	public static final String[] ARR_DEFAULT_OPTIONS = new String[] {
			"Aromatize molecules:1", "Strip salts:1" };
	public static final String[] ARR_DEFAULT_COLS = new String[] {
			"CHEMAXON Molecule:1", "CTAB Column:0", "SDF Column:0",
			"SMILES Column:0", "MOL2 Column:0", "CML Column:0", "PDB Column:0",
			"Source Column:0" };

	public static final String COL_NAME_CHEMAXON = "Molecule";
	public static final String COL_NAME_CTAB = "CTAB String";
	public static final String COL_NAME_SDF = "SDF String";
	public static final String COL_NAME_SMILES = "SMILES String";
	public static final String COL_NAME_MOL2 = "MOL2 String";
	public static final String COL_NAME_CML = "CML String";
	public static final String COL_NAME_PDB = "PDB String";
	public static final String COL_NAME_SOURCE = "Source String";

	private static final DataColumnSpecCreator CHEMAXON_CREATOR = new DataColumnSpecCreator(
			COL_NAME_CHEMAXON, MrvCell.TYPE);
	
	private final SettingsModelString mFilename = new SettingsModelString(
			MolFileReaderNodeModel.CFG_FILENAME, "");
	private final SettingsModelStringArray mFilenames = new SettingsModelStringArray(MolFileReaderNodeModel.CFG_FILENAME, new String[] {});
	
	private final SettingsModelStringArray mProperties = new SettingsModelStringArray(
			MolFileReaderNodeModel.CFG_FILEPROPS, new String[] {});
	private final SettingsModelStringArray mOutputOptions = new SettingsModelStringArray(
			MolFileReaderNodeModel.CFG_OUT_OPTIONS, new String[] {});
	private final SettingsModelStringArray mOutputColumns = new SettingsModelStringArray(
			MolFileReaderNodeModel.CFG_OUT_COLS, new String[] {});
	
	private final SettingsModelBoolean	   mIsShallowScan	= new SettingsModelBoolean(CFG_IS_SHALLOW_SCAN, true);
	private final SettingsModelString	   mLastDirectory	= new SettingsModelString(CFG_LAST_DIRECTORY, System.getProperty("user.home"));
	private final SettingsModelBoolean	   mRequiresScan	= new SettingsModelBoolean(CFG_REQUIRES_SCAN, true);
    private final SettingsModelStringArray mFoundProps  	= new SettingsModelStringArray(CFG_FOUND_PROPS, new String[]{});
    
	private String[] files = new String[0];

	/**
	 * Constructor for the node model.
	 */
	protected MolFileReaderNodeModel() {
		// no incoming port and one outgoing port required
		super(0, 2);
	}

	private DataTableSpec createSpec() {
		List<DataColumnSpec> colSpecs = new ArrayList<DataColumnSpec>();

		String[] tmp = mOutputColumns.getStringArrayValue();
		boolean hasMol = false;
		if (null != tmp) {
			for (String s : tmp) {
				int i = s.lastIndexOf(':');
				assert (-1 != i);
				if (s.substring(i + 1).startsWith("1")) {
					String colType = s.substring(0, i);
					if ("CHEMAXON Molecule".equalsIgnoreCase(colType)) {
						colSpecs.add(CHEMAXON_CREATOR.createSpec());
						hasMol = true;
					} else if ("CTAB Column".equalsIgnoreCase(colType)) {
						colSpecs.add(new DataColumnSpecCreator(COL_NAME_CTAB,
								CtabCell.TYPE).createSpec());
					} else if ("SDF Column".equalsIgnoreCase(colType)) {
						colSpecs.add(new DataColumnSpecCreator(COL_NAME_SDF,
								SdfCell.TYPE).createSpec());
					} else if ("SMILES Column".equalsIgnoreCase(colType)) {
						colSpecs.add(new DataColumnSpecCreator(COL_NAME_SMILES,
								SmilesCell.TYPE).createSpec());
					} else if ("MOL2 Column".equalsIgnoreCase(colType)) {
						colSpecs.add(new DataColumnSpecCreator(COL_NAME_MOL2,
								Mol2Cell.TYPE).createSpec());
					} else if ("CML Column".equalsIgnoreCase(colType)) {
						colSpecs.add(new DataColumnSpecCreator(COL_NAME_CML,
								CMLCell.TYPE).createSpec());
					} else if ("PDB Column".equalsIgnoreCase(colType)) {
						colSpecs.add(new DataColumnSpecCreator(COL_NAME_PDB,
								PdbCell.TYPE).createSpec());
					} else if ("Source Column".equalsIgnoreCase(colType)) {
					    DataColumnSpecCreator dcs = new DataColumnSpecCreator(COL_NAME_SOURCE, StringCell.TYPE);
					    dcs.setProperties(new DataColumnProperties(Collections.singletonMap(DataValueRenderer.PROPERTY_PREFERRED_RENDERER,
				                                                                            new MultiLineStringValueRenderer.Factory().getDescription())));				        
						colSpecs.add(dcs.createSpec());
					}
				}
			}
		}
		if (hasMol) {
			tmp = mProperties.getStringArrayValue();
			if (null != tmp) {
				for (String s : tmp) {
					colSpecs.add(new DataColumnSpecCreator(s, StringCell.TYPE)
							.createSpec());
				}
			}
		}
		return new DataTableSpec(colSpecs.toArray(new DataColumnSpec[colSpecs
				.size()]));
	}

	
	 private DataTableSpec createErrorSpec() {
	        List<DataColumnSpec> colSpecs = new ArrayList<DataColumnSpec>();
	        colSpecs.add(new DataColumnSpecCreator("File", StringCell.TYPE).createSpec());
	        colSpecs.add(new DataColumnSpecCreator("Mol #", IntCell.TYPE).createSpec());
	        colSpecs.add(new DataColumnSpecCreator("Error", StringCell.TYPE).createSpec());
	        
	        return new DataTableSpec(colSpecs.toArray(new DataColumnSpec[colSpecs.size()]));
	    }
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] doExecute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		List<BufferedDataTable> tables = new ArrayList<BufferedDataTable>();
        
		BufferedDataContainer errorContainer = exec.createDataContainer(createErrorSpec());
		
		if (files.length == 0) {		
			throw new IllegalStateException("You must specify at least one filename");
		}
		   
        int count = 0;
        
        for (String fname : files) {
        	       
			LOG.warn("Processing file : " + fname);
	
			String error = KnimenodeUtils.checkFileName(fname);
			if (null != error) {
				throw new InvalidSettingsException(error);
			}
	
			InputStream input = null;
			try {
				URL url = new URL(fname);
				input = url.openStream();
			} catch (MalformedURLException ex) {
				input = new FileInputStream(fname);
			}
	
			BufferedDataContainer container = exec
					.createDataContainer(createSpec());
	
			boolean hasMol = container.getTableSpec().containsName(
					COL_NAME_CHEMAXON);
			boolean hasSrc = container.getTableSpec().containsName(COL_NAME_SOURCE);
	
			
			MolImporter importer = new MolImporter(input);
			Molecule readMol = null;
			String source = null;
			/*
			if (hasMol) {
				readMol = importer.read();
				source = null != readMol ? importer.getGrabbedMoleculeString()
						: null;
			} else if (hasSrc) {
				source = importer.readRecordAsText();
			} else {
				throw new RuntimeException(
						"Needs at least one of ChemaxonMolecule or Source column");
			}
	*/
			boolean aromatize = true, stripSalts = true;
			String[] tmp = mOutputOptions.getStringArrayValue();
			if (null != tmp) {
				for (String s : tmp) {
					int i = s.lastIndexOf(':');
					assert (-1 != i);
					if ("Aromatize molecules".equalsIgnoreCase(s.substring(0, i))) {
						aromatize = s.substring(i + 1).startsWith("1");
					} else if ("Strip salts".equalsIgnoreCase(s.substring(0, i))) {
						stripSalts = s.substring(i + 1).startsWith("1");
					}
				}
			}
			tmp = mProperties.getStringArrayValue();
			
			try {
				int molNumber = 0;
				while (true) {
					try {
						source = null;
						
						if (hasMol) {							
							molNumber++;
							readMol = importer.read();							
							source = null != readMol ? importer.getGrabbedMoleculeString() : null;
						} else if (hasSrc) {
							source = importer.readRecordAsText();
						}
					
						if (source == null) {
							break;
						}
						Properties props = new Properties();
						int pcount = readMol.getPropertyCount();
						for (int i = 0; i < pcount; i++) {
							String key = readMol.getPropertyKey(i);
							props.put(key, readMol.getPropertyObject(key));
						}
		
						if (hasMol) {
							if (aromatize) {
								readMol.aromatize();
							}
							if (stripSalts && readMol.getAtomCount() > 0) {
								MoleculeGraph[] frags = readMol
										.findFrags(Molecule.class);
								readMol = (Molecule) frags[0];
								for (int i = 1; i < frags.length; ++i) {
									if (readMol.getAtomCount() < frags[i]
											.getAtomCount()) {
										readMol = (Molecule) frags[i];
									}
								}
							}
						}
						List<DataCell> cells = new ArrayList<DataCell>();
						for (int i = 0; i < container.getTableSpec().getNumColumns(); ++i) {
							if (hasMol) {
								if (container.getTableSpec().getColumnSpec(i).getName()
										.equals(COL_NAME_CHEMAXON))
								{
									cells.add(MrvCellFactory.create(readMol));
								}
								if (container.getTableSpec().getColumnSpec(i).getName()
										.equals(COL_NAME_CTAB))
								{
									cells.add(CtabCellFactory.create(readMol
											.toFormat("mol")));
								}
								if (container.getTableSpec().getColumnSpec(i).getName()
										.equals(COL_NAME_SDF))
								{
									cells.add(SdfCellFactory.create(readMol
											.toFormat("sdf")));
								}
								if (container.getTableSpec().getColumnSpec(i).getName()
										.equals(COL_NAME_SMILES))
								{
									cells.add(new SmilesCell(readMol.toFormat("smiles")));
								}
								if (container.getTableSpec().getColumnSpec(i).getName()
										.equals(COL_NAME_MOL2))
								{
									cells.add(Mol2CellFactory.create(readMol
											.toFormat("mol2")));
								}
								if (container.getTableSpec().getColumnSpec(i).getName()
										.equals(COL_NAME_CML))
								{
									cells.add(CMLCellFactory.create(readMol.toFormat("cml")));
								}
								if (container.getTableSpec().getColumnSpec(i).getName()
										.equals(COL_NAME_PDB))
								{
									cells.add(new PdbCell(readMol.toFormat("pdb")));
								}
							}
							if (container.getTableSpec().getColumnSpec(i).getName()
									.equals(COL_NAME_SOURCE))
							{
								cells.add(new StringCell(source));
							}
						}
						if (hasMol) {
							for (String pkey : tmp) {
								String propValue = props.getProperty(pkey);
								cells.add(new StringCell(null != propValue ? propValue
										: ""));
							}
						}
						count += 1;
						RowKey key = new RowKey(Integer.toString(count));
						DefaultRow row = new DefaultRow(key, cells);
						container.addRowToTable(row);
					
					} catch(Throwable ex) {
						ex.printStackTrace();
						DataCell[] errorCells = new DataCell[3];
		        		errorCells[0] = new StringCell(fname);
		        		errorCells[1] = new IntCell(molNumber);	        		
		        		errorCells[2] = new StringCell(ex.getMessage() == null ? "Unknown Error" : ex.getMessage());
		        		
		        		DefaultRow errorRow = new DefaultRow(RowKey.createRowKey(errorContainer.size() + 1), errorCells);
		        		errorContainer.addRowToTable(errorRow);
		        		continue;							
					}
	
				}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				importer.close();
				input.close();
				throw e;
			}
	
			importer.close();
			input.close();
			container.close();
			tables.add(container.getTable());
        }
        
        errorContainer.close();
        return new BufferedDataTable[] { exec.createConcatenateTable(exec, tables.toArray(new BufferedDataTable[0])),  errorContainer.getTable() };        
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		return new DataTableSpec[] { createSpec(), createErrorSpec() };
	}

	/**
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		super.reset();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		mFilenames.saveSettingsTo(settings);
		mProperties.saveSettingsTo(settings);
		mOutputOptions.saveSettingsTo(settings);
		mOutputColumns.saveSettingsTo(settings);
		mIsShallowScan.saveSettingsTo(settings);
        mLastDirectory.saveSettingsTo(settings);
        mRequiresScan.saveSettingsTo(settings);
        mFoundProps.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		
		try {
    		//	If we don't have the new style the try the old
    		mFilenames.loadSettingsFrom(settings);
    		files = mFilenames.getStringArrayValue();
    		
    	} catch(InvalidSettingsException ex) {        		
    		try {
	    		mFilename.loadSettingsFrom(settings);
	    		files = new String[] {mFilename.getStringValue()};
	    		mFilenames.setStringArrayValue(files);
    		} catch(InvalidSettingsException ex2) {
    			//	Do nothing
    		}
    	}
		
		mProperties.loadSettingsFrom(settings);
		mOutputOptions.loadSettingsFrom(settings);
		if (mOutputOptions.getStringArrayValue() == null
				|| 0 == mOutputOptions.getStringArrayValue().length) {
			mOutputOptions.setStringArrayValue(ARR_DEFAULT_OPTIONS);
		}
		mOutputColumns.loadSettingsFrom(settings);
		if (mOutputColumns.getStringArrayValue() == null
				|| 0 == mOutputColumns.getStringArrayValue().length) {
			mOutputColumns.setStringArrayValue(ARR_DEFAULT_COLS);
		}
		try {
			mIsShallowScan.loadSettingsFrom(settings);
        } catch(InvalidSettingsException ex) {
        	//	Do nothing
        }
        try {
        	mLastDirectory.loadSettingsFrom(settings);
        } catch(InvalidSettingsException ex) {
        	//	Do nothing
        }
        try {
        	mRequiresScan.loadSettingsFrom(settings);
        } catch(InvalidSettingsException ex) {
        	//	Do nothing
        }
        
        try {
        	mFoundProps.loadSettingsFrom(settings);
        } catch(InvalidSettingsException ex) {
        	//	Do nothing
        }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

	}

}
