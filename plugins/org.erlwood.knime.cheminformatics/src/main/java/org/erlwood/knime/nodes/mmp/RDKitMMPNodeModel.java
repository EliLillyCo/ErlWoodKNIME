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
package org.erlwood.knime.nodes.mmp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.RDKit.ChemicalReaction;
import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.ROMol_Vect;
import org.RDKit.ROMol_Vect_Vect;
import org.RDKit.RWMol;
import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.erlwood.knime.utils.network.AdjacencyMatrix;
import org.knime.chem.types.RxnCell;
import org.knime.chem.types.RxnCellFactory;
import org.knime.chem.types.SmilesCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.rdkit.knime.types.RDKitMolValue;

import chemaxon.formats.MFileFormat;
import chemaxon.formats.MolImporter;

/**
 * This is the first implementation of an efficient MMP detector algorithm.
 * 
 * @author George Papadatos
 */
public class RDKitMMPNodeModel extends NodeModel {

    // the logger instance
    private static final NodeLogger           LOG                    = NodeLogger.getLogger(RDKitMMPNodeModel.class);

    /**
     * the settings key which is used to retrieve and store the settings (from
     * the dialog or from a settings file) (package visibility to be usable from
     * the dialog).
     * 
     * example value: the models count variable filled from the dialog and used
     * in the models execution method. The default components of the dialog work
     * with "SettingsModels".
     */

    public static final String                RDMOL_IN_COL           = "molecule column";
    private final SettingsModelString         mMolInCol              = new SettingsModelString(RDMOL_IN_COL, "");

    public static final String                ID_IN_COL              = "mol ID column";
    private final SettingsModelColumnName     mIdInCol               = new SettingsModelColumnName(ID_IN_COL, "");

    public static final String                CONNECTION_POINT_STR   = "connection_point_string";
    private final SettingsModelString         mConnectionPointString = new SettingsModelString(CONNECTION_POINT_STR, "At");

    public static final String                PVALUE_IN_COL          = "mol property column";
    private final SettingsModelColumnName     mPvalueInCol           = new SettingsModelColumnName(PVALUE_IN_COL, "");

    public static final String                CFG_RATIO_COLUMNS      = "ratio columns";
    private final SettingsModelFilterString   mRatioColumns          = new SettingsModelFilterString(CFG_RATIO_COLUMNS);

    public static final String                CFG_DIFF_COLUMNS       = "diff columns";
    private final SettingsModelFilterString   mDiffColumns           = new SettingsModelFilterString(CFG_DIFF_COLUMNS);

    public static final String                CFG_DUPLICATES         = "allow duplicated pairs";
    private final SettingsModelBoolean        mDuplicate             = new SettingsModelBoolean(CFG_DUPLICATES, true);

    public static final String                CFG_PRECEDENCE         = "precedence";
    private final SettingsModelString         mPrecedence            = new SettingsModelString("precedence",
                                                                             PRECEDENCE_OPTIONS[0]);

    public static final String                PRECEDENCE_OPTIONS[]   = { "R / L and R - L", "L / R and L - R" };
    private final Map<String, Set<data>>      dict                   = new Hashtable<String, Set<data>>();
    private final Map<String, Set<data>>      idd                    = new Hashtable<String, Set<data>>();
    private final Map<Integer, List<Integer>> alist                  = new Hashtable<Integer, List<Integer>>();
    private static final String               REA_SMARTS             = "[*:1]!@!=!#[*:2]>>[*:1]-[*].[*:2]-[*]";

    private Map<String, ROMol>                smiles2MolMap          = new HashMap<String, ROMol>();
    private Map<ROMol, String>                mol2SmilesMap          = new HashMap<ROMol, String>();

    /**
     * Constructor for the node model.
     */
    protected RDKitMMPNodeModel() {
        super(1, 2);
    }

    private ROMol getMol(String smiles) {
        ROMol mol = smiles2MolMap.get(smiles);
        if (mol == null) {
            mol = RWMol.MolFromSmiles(smiles);
            smiles2MolMap.put(smiles, mol);
        }
        return mol;
    }

    private String getSmiles(ROMol mol) {
        String smiles = mol2SmilesMap.get(mol);
        if (smiles == null) {
            smiles = RDKFuncs.MolToSmiles(mol, true);
            mol2SmilesMap.put(mol, smiles);
        }
        return smiles;
    }

    protected BufferedDataTable[] execute(BufferedDataTable inData[], ExecutionContext exec) throws Exception {

        smiles2MolMap.clear();
        mol2SmilesMap.clear();

        ChemicalReaction rxn = ChemicalReaction.ReactionFromSmarts(REA_SMARTS);

        List<String> ratioCols = mRatioColumns.getIncludeList();
        List<String> diffCols = mDiffColumns.getIncludeList();

        BufferedDataTable input = inData[0];

        int molIndex = input.getSpec().findColumnIndex(mMolInCol.getStringValue());
        int idIndex = input.getSpec().findColumnIndex(mIdInCol.getColumnName());

        DataColumnSpec idDataSpec = null;
        if (idIndex > -1) {
            idDataSpec = input.getSpec().getColumnSpec(idIndex);
        }

        BufferedDataContainer container = exec.createDataContainer(createSpec(idDataSpec));

        RowIterator rows = input.iterator();
        double count = 0.0D;
        while (rows.hasNext()) {
            DataRow dataRow = rows.next();
            DataCell mcell = dataRow.getCell(molIndex);

            if (mcell.isMissing()) {
                continue;
            }
            try {

                RDKitMolValue sv = MoleculeDataTypeConverter.getValue(mcell, RDKitMolValue.class);

                ROMol molecule = sv.readMoleculeValue();
                String id = dataRow.getKey().getString();

                if (!mIdInCol.useRowID()) {
                    id = dataRow.getCell(idIndex).toString();
                }
                List<Double> rprop = new ArrayList<Double>();
                List<Double> dprop = new ArrayList<Double>();

                for (String prop : ratioCols) {
                    DataCell cell = dataRow.getCell(inData[0].getSpec().findColumnIndex(prop));
                    if (cell.isMissing()) {
                        rprop.add(null);
                    } else {
                        Double d = Double.valueOf(Double.parseDouble(cell.toString()));
                        rprop.add(d);
                    }
                }

                for (String prop : diffCols) {
                    DataCell cell = dataRow.getCell(inData[0].getSpec().findColumnIndex(prop));
                    if (cell.isMissing()) {
                        dprop.add(null);
                    } else {
                        Double d = Double.valueOf(Double.parseDouble(cell.toString()));
                        dprop.add(d);
                    }
                }

                if (rprop.size() == 0 || dprop.size() == 0) {
                    LOG.debug("????");
                }

                processMolecule(molecule, count, id, rprop, dprop, rxn);
                molecule.delete();
            } catch (Exception e1) {
                setWarningMessage((new StringBuilder("Error parsing Smiles for row: ")).append((int) (count + 1.0D)).toString());
                continue;
            }
            count++;
        }

        count = 0.0D;
        double c = 0.0D;

        for (String context : dict.keySet()) {

            addHTransformation(context);
            Set<data> dvalues = dict.get(context);
            if (dvalues.size() <= 1) {
                c++;
            } else {
                
                List<data> lstDataValues = new ArrayList<data>(dvalues);
                
                for (int i = 0; i < lstDataValues.size(); i++) {                   
                    for (int j = i + 1; j < lstDataValues.size(); j++) {
                        count++;
                        
                        RowKey key = new RowKey((new StringBuilder("Row ")).append((int) count).toString());
                        data left = (data) lstDataValues.get(i).clone();
                        data right = (data) lstDataValues.get(j).clone();
                        
                        //  Ignore same fragment
                        if (left.sfrag.equals(right.sfrag)) {
                            continue;
                        }
                        
                        
                        boolean canonical = isCanonical(left, right);
                        if (!canonical) {
                            try {
                                data temp = (data) left.clone();
                                left = (data) right.clone();
                                right = (data) temp.clone();
                            } catch (CloneNotSupportedException e) {
                                LOG.error(e.getMessage(), e);
                            }
                        }
                        DataRow row = generateMP(idDataSpec, context, left, right, key, !mPrecedence.getStringValue()
                                .equalsIgnoreCase(PRECEDENCE_OPTIONS[0]));
                        container.addRowToTable(row);
                        if (mDuplicate.getBooleanValue()) {
                            RowKey key2 = new RowKey((new StringBuilder("Row ")).append((int) count).append("(inverse)")
                                    .toString());
                            DataRow row2 = generateMP(idDataSpec, context, right, left, key2, !mPrecedence.getStringValue()
                                    .equalsIgnoreCase(PRECEDENCE_OPTIONS[0]));
                            container.addRowToTable(row2);
                        }

                    }

                }

                c++;
                double progress = c / (double) dict.size();
                exec.setProgress(0.5D * progress + 0.5D);
            }
        }

        container.close();
        BufferedDataTable out = container.getTable();
        BufferedDataContainer container2 = exec.createDataContainer(createSpec1(inData[0].getDataTableSpec(), molIndex));

        RowIterator rows2 = input.iterator();
        for (int k = 0; rows2.hasNext(); k++) {
            DataRow row = rows2.next();
            double w[] = new double[inData[0].getRowCount()];
            Arrays.fill(w, 0.0D);
            List<Integer> v = alist.get(Integer.valueOf(k));
            if (v != null) {
                for (int i : v) {
                    w[i] = 1.0D;
                }
            }
            AdjacencyMatrix amatrix = new AdjacencyMatrix(k, false, w);
            List<DataCell> cells = new ArrayList<DataCell>();

            for (DataCell dc : row) {
                cells.add(dc);
            }

            cells.add(amatrix);
            container2.addRowToTable(new DefaultRow(row.getKey(), cells));
        }

        container2.close();

        return (new BufferedDataTable[] { out, container2.getTable() });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Models build during execute are cleared here.
        // Also data handled in load/saveInternals will be erased here.
        dict.clear();
        idd.clear();
        alist.clear();
        smiles2MolMap.clear();
        mol2SmilesMap.clear();
    }

    private DataTableSpec createSpec1(DataTableSpec in, int colIndex) {

        DataColumnSpec[] newColumnSpec = new DataColumnSpec[1];

        newColumnSpec[0] = new DataColumnSpecCreator("Network", AdjacencyMatrix.TYPE).createSpec();

        DataTableSpec outSpec = new DataTableSpec(newColumnSpec);
        return new DataTableSpec(in, outSpec);

    }

    private DataTableSpec createSpec(DataColumnSpec idColumnSpec) {
        List<String> ratioCols = mRatioColumns.getIncludeList();
        List<String> diffCols = mDiffColumns.getIncludeList();

        List<DataColumnSpec> dcs = new ArrayList<DataColumnSpec>();
        dcs.add((new DataColumnSpecCreator("Molecule_L", SmilesCell.TYPE)).createSpec());
        dcs.add((new DataColumnSpecCreator("Molecule_R", SmilesCell.TYPE)).createSpec());
        dcs.add((new DataColumnSpecCreator("ID_pair", StringCell.TYPE)).createSpec());

        if (idColumnSpec != null && idColumnSpec.getType().isCollectionType()
                && idColumnSpec.getType().getCollectionElementType() == IntCell.TYPE) {
            dcs.add((new DataColumnSpecCreator("ID_L", idColumnSpec.getType())).createSpec());
            dcs.add((new DataColumnSpecCreator("ID_R", idColumnSpec.getType())).createSpec());
        } else {
            dcs.add((new DataColumnSpecCreator("ID_L", StringCell.TYPE)).createSpec());
            dcs.add((new DataColumnSpecCreator("ID_R", StringCell.TYPE)).createSpec());
        }

        dcs.add((new DataColumnSpecCreator("Transformation", RxnCell.TYPE)).createSpec());
        dcs.add((new DataColumnSpecCreator("Context", SmilesCell.TYPE)).createSpec());
        dcs.add((new DataColumnSpecCreator("Fragment_L", SmilesCell.TYPE)).createSpec());
        dcs.add((new DataColumnSpecCreator("Fragment_R", SmilesCell.TYPE)).createSpec());

        for (String prop : ratioCols) {
            dcs.add((new DataColumnSpecCreator((new StringBuilder("Ratio (")).append(prop).append(")").toString(),
                    DoubleCell.TYPE)).createSpec());
            dcs.add((new DataColumnSpecCreator((new StringBuilder(String.valueOf(prop))).append("_L").toString(),
                    DoubleCell.TYPE)).createSpec());
            dcs.add((new DataColumnSpecCreator((new StringBuilder(String.valueOf(prop))).append("_R").toString(),
                    DoubleCell.TYPE)).createSpec());
        }

        for (String prop : diffCols) {

            dcs.add((new DataColumnSpecCreator((new StringBuilder("Difference (")).append(prop).append(")").toString(),
                    DoubleCell.TYPE)).createSpec());
            if (!ratioCols.contains(prop)) {
                dcs.add((new DataColumnSpecCreator((new StringBuilder(String.valueOf(prop))).append("_L").toString(),
                        DoubleCell.TYPE)).createSpec());
                dcs.add((new DataColumnSpecCreator((new StringBuilder(String.valueOf(prop))).append("_R").toString(),
                        DoubleCell.TYPE)).createSpec());
            }
        }

        dcs.add((new DataColumnSpecCreator("MCS Distance", DoubleCell.TYPE)).createSpec());
        dcs.add((new DataColumnSpecCreator("Trans_Atom_Count", IntCell.TYPE)).createSpec());
        return new DataTableSpec((DataColumnSpec[]) dcs.toArray(new DataColumnSpec[dcs.size()]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

        DataColumnSpec columnSpecRd = ((DataTableSpec) inSpecs[0]).getColumnSpec(mMolInCol.getStringValue());

        int molIndex = inSpecs[0].findColumnIndex(mMolInCol.getStringValue());

        if (columnSpecRd == null || !MoleculeDataTypeConverter.isConvertible(columnSpecRd.getType(), RDKitMolValue.class)) {
            boolean found = false;
            for (int i = inSpecs[0].getNumColumns() - 1; i >= 0 && !found; i--) {
                if (MoleculeDataTypeConverter.isConvertible(inSpecs[0].getColumnSpec(i).getType(), RDKitMolValue.class)) {
                    mMolInCol.setStringValue(inSpecs[0].getColumnSpec(i).getName());
                    molIndex = i;
                    found = true;
                }
            }
            if (!found) {
                throw new InvalidSettingsException("Input requires table containing RDKit molecules.");
            }
        }
        List<String> dataCols = new ArrayList<String>();
        for (int i = 0; i < ((DataTableSpec) inSpecs[0]).getNumColumns(); i++) {
            if (inSpecs[0].getColumnSpec(i).getType().isCompatible(DoubleValue.class)
                    || inSpecs[0].getColumnSpec(i).getType().isCompatible(IntValue.class)) {
                dataCols.add(inSpecs[0].getColumnSpec(i).getName());
            }
        }

        return new DataTableSpec[] { null, createSpec1(inSpecs[0], molIndex) };
    }

    private boolean isCanonical(data left, data right) {
        ROMol tmol2 = getMol(new StringBuilder(left.sfrag).append(".").append(right.sfrag).toString());
        String cantrans = getSmiles(tmol2);
        tmol2.delete();
        ROMol tmol3 = getMol(cantrans.split("[.]")[0]);
        String canFragL = getSmiles(tmol3);
        tmol3.delete();
        return canFragL.equals(left.sfrag);
    }

    private List<IntCell> parseIdString(String idString) {
        List<IntCell> retVal = new ArrayList<IntCell>();

        idString = idString.replace("[", "");
        idString = idString.replace("]", "");
        idString = idString.replace(" ", "");

        String[] split = idString.split(",");
        for (int i = 0; i < split.length; i++) {
            int iVal = Integer.parseInt(split[i]);
            retVal.add(new IntCell(iVal));
        }

        return retVal;
    }

    private String replaceCP(String smiles) {
        if (mConnectionPointString.getStringValue().equals("At")) {
            smiles = smiles.replace("*", "[At]");
            smiles = smiles.replace("*[H]", "[At][H]");
        }

        return smiles;
    }

    private DataRow generateMP(DataColumnSpec idDataSpec,
                               String context,
                               data left,
                               data right,
                               RowKey key,
                               boolean inversePrecedence) throws CloneNotSupportedException {
        List<String> ratioCols = mRatioColumns.getIncludeList();
        List<String> diffCols = mDiffColumns.getIncludeList();
        String fragL = left.sfrag;
        String idL = left.id;
        List<Double> prL = left.ratioProp;
        List<Double> pdL = left.diffProp;
        String smiL = left.origSmi;
        int ridL = left.index;
        int hatomsL = countFirstFragmentSize(smiL);
        String fragR = right.sfrag;
        String idR = right.id;
        int ridR = right.index;

        if (!alist.containsKey(ridL)) {
            alist.put(ridL, new ArrayList<Integer>());
        }

        alist.get(ridL).add(ridR);

        if (!alist.containsKey(ridR)) {
            alist.put(ridR, new ArrayList<Integer>());
        }
        alist.get(ridR).add(ridL);

        List<Double> prR = right.ratioProp;
        List<Double> pdR = right.diffProp;
        String smiR = right.origSmi;
        int hatomsR = countFirstFragmentSize(smiR);

        List<DataCell> cells = new ArrayList<DataCell>();
        cells.add(new SmilesCell(smiL));
        cells.add(new SmilesCell(smiR));
        cells.add(new StringCell((new StringBuilder(String.valueOf(idL.replace(' ', '_')))).append(">>")
                .append(idR.replace(' ', '_')).toString()));

        if (idDataSpec != null && idDataSpec.getType().isCollectionType()
                && idDataSpec.getType().getCollectionElementType() == IntCell.TYPE) {
            cells.add(CollectionCellFactory.createListCell(parseIdString(idL)));
            cells.add(CollectionCellFactory.createListCell(parseIdString(idR)));
        } else {
            cells.add(new StringCell(idL.replace(' ', '_')));
            cells.add(new StringCell(idR.replace(' ', '_')));
        }

        String rxnString = "";
        try {
            String smiles = (new StringBuilder(String.valueOf(fragL.replace("[*H]", "*[H]")))).append(">>")
                    .append(fragR.replace("[*H]", "*[H]")).toString();
            smiles = replaceCP(smiles);
            rxnString = MolImporter.importMol(smiles).exportToFormat(MFileFormat.RXN.getName());

            // Transformation
            cells.add(RxnCellFactory.create(rxnString));
            // Context
            cells.add(new SmilesCell(replaceCP(context)));
            // Fragment_L
            cells.add(new SmilesCell(replaceCP(fragL.replace("[*H]", "*[H]"))));
            // Fragment_R
            cells.add(new SmilesCell(replaceCP(fragR.replace("[*H]", "*[H]"))));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        for (int p = 0; p < prR.size(); p++) {
            Double pR = prR.get(p);
            Double pL = prL.get(p);

            if (pR == null || pL == null) {
                cells.add(DataType.getMissingCell());
            } else {
                cells.add(inversePrecedence ? new DoubleCell(pL / pR) : new DoubleCell(pR / pL));
            }
            if (pL == null) {
                cells.add(DataType.getMissingCell());
            } else {
                cells.add(new DoubleCell(pL));
            }
            if (pR == null) {
                cells.add(DataType.getMissingCell());
            } else {
                cells.add(new DoubleCell(pR));
            }
        }

        for (int p = 0; p < pdR.size(); p++) {
            Double pR = pdR.get(p);
            Double pL = pdL.get(p);
            if (pR == null || pL == null) {
                cells.add(DataType.getMissingCell());
            } else {
                cells.add(inversePrecedence ? new DoubleCell(pL - pR) : new DoubleCell(pR - pL));
            }
            if (!ratioCols.contains(diffCols.get(p))) {
                if (pL == null) {
                    cells.add(DataType.getMissingCell());
                } else {
                    cells.add(new DoubleCell(pL));
                }
                if (pR == null) {
                    cells.add(DataType.getMissingCell());
                } else {
                    cells.add(new DoubleCell(pR));
                }
            }
        }

        cells.add(new DoubleCell(1.0D - (2D * (double) (countFirstFragmentSize(context) - 1)) / (double) (hatomsL + hatomsR)));
        cells.add(new IntCell(countFirstFragmentSize((new StringBuilder(String.valueOf(fragL))).append(fragR).toString())));
        return new DefaultRow(key, cells);
    }

    private int processMolecule(ROMol mol, double count, String id, List<Double> rprop, List<Double> dprop, ChemicalReaction rxn) {
        ROMol_Vect_Vect prods = null;
        ROMol frag1 = null;
        ROMol frag2 = null;
        ROMol_Vect rs = new ROMol_Vect(1L);
        rs.set(0, mol);
        int fragsize = 0;
        String origSmi = "";
        try {
            prods = rxn.runReactants(rs);
            fragsize = (int) prods.size();
            origSmi = getSmiles(mol);
            Set<data> iddl = idd.get(origSmi);
            if (iddl == null) {
                iddl = new HashSet<data>();
                idd.put(origSmi, iddl);
            }
            data d = new data((int) count, id, rprop, dprop);
            // if (!iddl.contains(d)) {
            iddl.add(d);
            // }
        } catch (Exception e) {
            return -1;
        }
        if (!prods.isEmpty()) {
            for (int psetidx = 0; (long) psetidx < prods.size(); psetidx++) {
                try {
                    frag1 = prods.get(psetidx).get(0);
                    frag2 = prods.get(psetidx).get(1);

                    Long natoms1 = Long.valueOf(frag1.getNumAtoms() - 1L);
                    Long natoms2 = Long.valueOf(frag2.getNumAtoms() - 1L);

                    float ratio = (float) natoms1.longValue() / (float) natoms2.longValue();

                    String smi1 = getSmiles(frag1);
                    String smi2 = getSmiles(frag2);

                    if ((double) natoms2.longValue() <= 15D && (double) ratio >= 1.0D) {
                        Set<data> vec = dict.get(smi1);

                        if (vec == null) {
                            vec = new HashSet<data>();
                            dict.put(smi1, vec);
                        }
                        vec.add(new data((int) count, smi2, id, rprop, dprop, origSmi, smi1));

                    }
                } catch (Exception e) {
                    continue;
                }
                frag1.delete();
                frag2.delete();
            }

        }
        rs.get(0).delete();
        rs.delete();
        prods.delete();
        return fragsize;
    }

    private void addHTransformation(String smi) {
        RWMol tmol1 = RWMol.MolFromSmiles(smi.replace("*", "[H]"));
        String contextH = getSmiles(tmol1);
        tmol1.delete();
        Set<data> lst = idd.get(contextH);
        if (lst != null) {
            for (data d : lst) {
                dict.get(smi).add(new data(d.index, "[*H]", d.id, d.ratioProp, d.diffProp, contextH, smi));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        mMolInCol.saveSettingsTo(settings);
        mIdInCol.saveSettingsTo(settings);
        mPvalueInCol.saveSettingsTo(settings);
        mDiffColumns.saveSettingsTo(settings);
        mRatioColumns.saveSettingsTo(settings);
        mDuplicate.saveSettingsTo(settings);
        mPrecedence.saveSettingsTo(settings);
        mConnectionPointString.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

        mIdInCol.loadSettingsFrom(settings);
        try {
            mPvalueInCol.loadSettingsFrom(settings);
            mMolInCol.loadSettingsFrom(settings);
            mConnectionPointString.loadSettingsFrom(settings);
        } catch (Exception e) {
        }
        try {
            mDuplicate.loadSettingsFrom(settings);
            mRatioColumns.loadSettingsFrom(settings);
            mDiffColumns.loadSettingsFrom(settings);
            mPrecedence.loadSettingsFrom(settings);
        } catch (Exception e) {
            LOG.warn("Please re-configure the node with the required property columns (new functionality)");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

        mIdInCol.validateSettings(settings);
        try {
            mPvalueInCol.validateSettings(settings);
            mMolInCol.validateSettings(settings);
        } catch (Exception e) {

        }
        try {
            mDuplicate.validateSettings(settings);
            mRatioColumns.validateSettings(settings);
            mDiffColumns.validateSettings(settings);
            mPrecedence.validateSettings(settings);

            mConnectionPointString.validateSettings(settings);
        } catch (Exception e) {

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

        // Everything handed to output ports is loaded automatically (data
        // returned by the execute method, models loaded in loadModelContent,
        // and user settings set through loadSettingsFrom - is all taken care
        // of). Load here only the other internals that need to be restored
        // (e.g. data used by the views).

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

        // Everything written to output ports is saved automatically (data
        // returned by the execute method, models saved in the saveModelContent,
        // and user settings saved through saveSettingsTo - is all taken care
        // of). Save here only the other internals that need to be preserved
        // (e.g. data used by the views).

    }

    private class data implements Cloneable {
        private String       sfrag;
        private List<Double> ratioProp;
        private List<Double> diffProp;
        private String       id;
        private String       origSmi;
        private int          index;
        private String       contextFrag;

        public data(int index, String sfrag, String id, List<Double> rprop, List<Double> dprop, String origSmi,
                String contextFrag) {
            this.index = index;
            this.sfrag = sfrag;
            this.ratioProp = rprop;
            this.diffProp = dprop;
            this.id = id;
            this.origSmi = origSmi;
            this.contextFrag = contextFrag;
        }

        public data(int index, String id, List<Double> rprop, List<Double> dprop) {
            this.index = index;
            this.id = id;
            this.ratioProp = rprop;
            this.diffProp = dprop;
            this.sfrag = "";
            this.origSmi = "";
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            List<Double> rtemp = new ArrayList<Double>(ratioProp);
            List<Double> dtemp = new ArrayList<Double>(diffProp);

            return new data(index, sfrag, id, rtemp, dtemp, origSmi, contextFrag);
        }

        private String getKey() {
            return id + sfrag + contextFrag;
        }

        @Override
        public boolean equals(Object other) {
            data dOther = (data) other;
            return getKey().equals(dOther.getKey());
        }

        @Override
        public int hashCode() {
            return getKey().hashCode();
        }
    }

    public int countFirstFragmentSize(String smiles) {
        int c = 0;
        for (int j = 0; j < smiles.length(); j++) {
            char x = smiles.charAt(j);
            if (x == '.') {
                break;
            }
            if (x == '[') {
                c++;
                while (smiles.charAt(j) != ']') {
                    j++;
                }
                continue;
            }

            if (Character.isLetter(x)) {
                if (Character.isLowerCase(x) && (x == 'c' || x == 'o' || x == 'n' || x == 's')) {
                    c++;
                }
                if (Character.isUpperCase(x)) {
                    c++;
                }
            }
        }
        return c;
    }

}
