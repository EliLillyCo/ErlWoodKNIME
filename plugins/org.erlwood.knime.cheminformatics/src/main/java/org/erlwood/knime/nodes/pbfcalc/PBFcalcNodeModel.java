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
package org.erlwood.knime.nodes.pbfcalc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.co.infocom.cheminfo.marvin.type.MrvValue;

import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DoubleCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;

import JSci.maths.LinearMath;
import JSci.maths.MaximumIterationsExceededException;
import JSci.maths.matrices.AbstractDoubleSquareMatrix;
import JSci.maths.matrices.DoubleSquareMatrix;
import JSci.maths.vectors.AbstractDoubleVector;
import JSci.maths.vectors.Double3Vector;
import chemaxon.calculations.hydrogenize.Hydrogenize;
import chemaxon.struc.MolAtom;
import chemaxon.struc.Molecule;

/**
 * This is the model implementation of PBFcalc. Node to locally calculate Plane
 * of Best fit values from an SDF
 * 
 * @author Roger Robinson
 */

public class PBFcalcNodeModel extends NodeModel {

    // setup dialog settings string
    private final SettingsModelFilterString incExCols = new SettingsModelFilterString("incExCols");
    private final SettingsModelBoolean      remH      = new SettingsModelBoolean("rem_H", true);

    private static final NodeLogger         LOG       = NodeLogger.getLogger(PBFcalcNodeModel.class);

    // Ports in Ports out
    protected PBFcalcNodeModel() {
        super(1, 1);
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {

        // Get list of included SDF Columns
        List<String> incCols = incExCols.getIncludeList();
        Iterator<String> incColsIter = incCols.iterator();

        // Simplify Data Table Names
        BufferedDataTable data = inData[0];
        DataTableSpec inSpec = inData[0].getDataTableSpec();
        BufferedDataTable outTable = data;
        double totalSDF = incCols.size() * data.getRowCount();
        double currentSDF = 0;

        // iterate over all columns
        while (incColsIter.hasNext()) {

            // List to store PBF values for current Column
            List<Double> pbfValues = new ArrayList<Double>();
            // Get Current Column Name
            String colName = incColsIter.next();
            // Get Column Index
            int colInd = inSpec.findColumnIndex(colName);

            // and rows
            for (DataRow row : data) {

                // access data cell and check if data is missing
                DataCell cell = row.getCell(colInd);
                if (cell.isMissing()) {
                    pbfValues.add(9999.9);
                    LOG.warn("Missing Cell for molecule " + Double.toString(currentSDF));
                } else {
                    // Import Sdf using ChemAxon
                    MrvValue mrvVal = MoleculeDataTypeConverter.getValue(cell, MrvValue.class);
                    Molecule mol = mrvVal.getMolecule();

                    // Remove Hydrogens if respective Tick Box selected
                    if (remH.getBooleanValue()) {
                        Hydrogenize.removeHAtoms(mol);
                    }

                    // Extract Atomic Coordinates in JSci format for
                    // Calculations
                    int atomN = mol.getAtomCount();
                    AbstractDoubleVector coords[] = new AbstractDoubleVector[atomN];

                    for (int atomInd = 0; atomInd < atomN; atomInd++) {
                        MolAtom atomC = mol.getAtom(atomInd);
                        double xCoord = atomC.getX();
                        double yCoord = atomC.getY();
                        double zCoord = atomC.getZ();
                        coords[atomInd] = new Double3Vector(xCoord, yCoord, zCoord);
                    }

                    // Calculate PBF values and fill list for full column
                    pbfValues.add(pbfCalc(coords, currentSDF));
                    // Show Progress on Execute Monitor
                    currentSDF++;
                    exec.setProgress(currentSDF / totalSDF);
                }
            }
            // Put values for current Column into Table
            ColumnRearranger colRearr = createColumnRearranger(outTable.getDataTableSpec(), pbfValues, colName);
            outTable = exec.createColumnRearrangeTable(outTable, colRearr, exec);

        }

        return new BufferedDataTable[] { outTable };
    }

    // ----------------------------------------------------------------------------------------------------------------------

    private static double pbfCalc(AbstractDoubleVector[] coords, double currentSDF) {

        // initialise variables
        int nAtoms = coords.length;
        AbstractDoubleVector origin = new Double3Vector(0.0, 0.0, 0.0);

        // locate Origin
        for (int i = 0; i < nAtoms; i++) {
            origin = origin.add(coords[i]);
        }
        origin = origin.scalarDivide(nAtoms);

        // Calculate distance from Origin for each Atom
        AbstractDoubleVector delta[] = new AbstractDoubleVector[nAtoms];
        for (int i = 0; i < nAtoms; i++) {
            delta[i] = new Double3Vector();
            delta[i] = coords[i].subtract(origin);
        }

        double sumXX = 0;
        double sumXY = 0;
        double sumXZ = 0;
        double sumYY = 0;
        double sumYZ = 0;
        double sumZZ = 0;

        for (int i = 0; i < nAtoms; i++) {
            sumXX += (delta[i].getComponent(0) * delta[i].getComponent(0));
            sumXY += (delta[i].getComponent(0) * delta[i].getComponent(1));
            sumXZ += (delta[i].getComponent(0) * delta[i].getComponent(2));
            sumYY += (delta[i].getComponent(1) * delta[i].getComponent(1));
            sumYZ += (delta[i].getComponent(1) * delta[i].getComponent(2));
            sumZZ += (delta[i].getComponent(2) * delta[i].getComponent(2));
        }

        sumXX /= nAtoms;
        sumXY /= nAtoms;
        sumXZ /= nAtoms;
        sumYY /= nAtoms;
        sumYZ /= nAtoms;
        sumZZ /= nAtoms;

        // Create Matrix and Fill it
        DoubleSquareMatrix mat = new DoubleSquareMatrix(3);
        AbstractDoubleVector eigenstates[] = new AbstractDoubleVector[3];

        double eigenvalues[] = null;

        mat.setElement(0, 0, sumXX);
        mat.setElement(1, 0, sumXY);
        mat.setElement(2, 0, sumXZ);

        mat.setElement(0, 1, sumXY);
        mat.setElement(1, 1, sumYY);
        mat.setElement(2, 1, sumYZ);

        mat.setElement(0, 2, sumXZ);
        mat.setElement(1, 2, sumYZ);
        mat.setElement(2, 2, sumZZ);

        final AbstractDoubleSquareMatrix mat1 = mat;

        // Calculate eigenvalue using JSci
        try {
            eigenvalues = LinearMath.eigenSolveSymmetric(mat1, eigenstates);
        } catch (MaximumIterationsExceededException e) {
            System.err.println(e.getMessage());
            LOG.warn("PBF value can not be calculated, possibly bad structure for molecule " + Double.toString(currentSDF));
            return 9999.9;
            // System.exit(-1);
        }

        // sort eigenvalues in terms of significance
        int n = 0;
        if (eigenvalues[1] < eigenvalues[0]) {
            n = 1;
            if (eigenvalues[2] < eigenvalues[1]) {
                n = 2;
            }
        } else if (eigenvalues[2] < eigenvalues[0]) {
            n = 2;
        }

        // Plane of Best Fit given in the form Ax+By+Cz+D=0
        // A = plane[0] B = plane[1] C = plane[2] D = plane[3]=0
        double[] plane = new double[4];
        plane[0] = eigenstates[n].getComponent(0);
        plane[1] = eigenstates[n].getComponent(1);
        plane[2] = eigenstates[n].getComponent(2);
        double dotp = -1.00 * origin.scalarProduct(eigenstates[n]);
        plane[3] = dotp;

        // Calculate denominator for final equation
        // Same for all atoms so done outside of method distanceFromAPlane
        // SqRoot(A^2+B^2+C^2)
        double denom = 0.0;
        for (int i = 0; i < 3; ++i) {
            denom += plane[i] * plane[i];
        }
        denom = Math.pow(denom, 0.5);

        // Calculate distance from plane of best fit for each Atom and Sum value
        // together
        double res = 0.0;
        for (int i = 0; i < nAtoms; ++i) {
            res += distanceFromAPlane(coords[i], plane, denom);
        }

        // Calculate average distance of from Plane of Best Fit to give Final
        // PBF value
        res /= nAtoms;

        return res;
    }

    // ----------------------------------------------------------------------------------------------------------------------

    private static double distanceFromAPlane(AbstractDoubleVector pt, double[] plane1, double denom) {
        double x1 = pt.getComponent(0);
        double y1 = pt.getComponent(1);
        double z1 = pt.getComponent(2);
        // Delta = Absolute(Axi+Byi+Czi+D)/SquareRoot(A^2+B^2+C^2)
        double numer = Math.abs(x1 * plane1[0] + y1 * plane1[1] + z1 * plane1[2] + plane1[3]);
        return numer / denom;
    }

    // ----------------------------------------------------------------------------------------------------------------------

    private ColumnRearranger createColumnRearranger(final DataTableSpec spec, final List<Double> pbfValues, String colName)
            throws InvalidSettingsException {

        ColumnRearranger result = new ColumnRearranger(spec);
        result.append(new SingleCellFactory(createColSpec(spec, colName)) {
            private int n = -1;

            public DataCell getCell(DataRow row) {
                n++;
                if (pbfValues.get(n) == 9999.9) {
                    return DataType.getMissingCell();
                } else {
                    return new DoubleCell(pbfValues.get(n));
                }

            }
        });

        return result;
    }

    // ----------------------------------------------------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected void reset() {
    }

    // ----------------------------------------------------------------------------------------------------------------------

    /** {@inheritDoc} */
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        // user setting are checked in createColumnRearranger method
        ColumnRearranger rearranger = null;
        DataTableSpec outSpecs = inSpecs[0];
        if (incExCols.getIncludeList().size() > 0) {
            List<String> incCols = incExCols.getIncludeList();
            rearranger = createColumnRearranger(inSpecs[0], null, incCols.get(0));
            for (int i = 1; i < incCols.size(); i++) {
                rearranger = createColumnRearranger(rearranger.createSpec(), null, incCols.get(i));
            }
            outSpecs = rearranger.createSpec();
        }

        return new DataTableSpec[] { outSpecs };
    }

    // ----------------------------------------------------------------------------------------------------------------------

    private DataColumnSpec createColSpec(DataTableSpec spec, String colName) {
        return new DataColumnSpecCreator("PBF " + colName, DoubleCell.TYPE).createSpec();
    }

    // ----------------------------------------------------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        incExCols.saveSettingsTo(settings);
        remH.saveSettingsTo(settings);
    }

    // ----------------------------------------------------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        incExCols.loadSettingsFrom(settings);
        remH.loadSettingsFrom(settings);
    }

    // ----------------------------------------------------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        incExCols.validateSettings(settings);
        remH.validateSettings(settings);
    }

    // ----------------------------------------------------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    // ----------------------------------------------------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

    }

    // ----------------------------------------------------------------------------------------------------------------------

}
