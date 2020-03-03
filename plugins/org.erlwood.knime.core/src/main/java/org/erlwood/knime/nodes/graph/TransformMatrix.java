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
package org.erlwood.knime.nodes.graph;

import java.text.NumberFormat;


class TransformMatrix {

    private final int rows, columns;
    private final double[][] data;

    /** Creates a new instance of simpleMatrix */
    public TransformMatrix(int r, int c) {
        rows = r;
        columns = c;
        data = new double[rows][columns];
    }

    public static TransformMatrix getRotation(String axis, double ang) {
        TransformMatrix m = new TransformMatrix(4, true);
        if (axis.equals("x")) {
            m.getData()[0][0] = 1.0;
            m.getData()[1][1] = Math.cos(ang);
            m.getData()[2][1] = Math.sin(ang);
            m.getData()[1][2] = -Math.sin(ang);
            m.getData()[2][2] = Math.cos(ang);
            m.getData()[3][3] = 1.0;
        } else if (axis.equals("y")) {
            m.getData()[0][0] = Math.cos(ang);
            m.getData()[0][2] = Math.sin(ang);
            m.getData()[2][0] = -Math.sin(ang);
            m.getData()[2][2] = Math.cos(ang);
            m.getData()[1][1] = 1.0;
            m.getData()[3][3] = 1.0;
        } else {
            m.getData()[0][0] = Math.cos(ang);
            m.getData()[0][1] = -Math.sin(ang);
            m.getData()[1][0] = Math.sin(ang);
            m.getData()[1][1] = Math.cos(ang);
            m.getData()[2][2] = 1.0;
            m.getData()[3][3] = 1.0;
        }

        return m;
    }

    public static TransformMatrix getTranslation(double x, double y, double z) {
        TransformMatrix m = new TransformMatrix(4, true);

        m.getData()[0][3] = x;
        m.getData()[1][3] = y;
        m.getData()[2][3] = z;

        return m;
    }

    public static TransformMatrix getScale(double x, double y, double z) {
        TransformMatrix m = new TransformMatrix(4, true);

        m.getData()[0][0] = x;
        m.getData()[1][1] = y;
        m.getData()[2][2] = z;

        return m;
    }

    public static TransformMatrix getPerspective(double x, double y, double z) {
        TransformMatrix m = new TransformMatrix(4, true);

        m.getData()[0][3] = x;
        m.getData()[1][3] = y;
        m.getData()[3][2] = 1 / z;
        m.getData()[3][3] = 0.0;

        return m;
    }

    public static TransformMatrix getScale(double x, double y, double z, double w) {
        TransformMatrix m = new TransformMatrix(4, true);

        m.getData()[0][0] = x;
        m.getData()[1][1] = y;
        m.getData()[2][2] = z;
        m.getData()[3][3] = w;

        return m;
    }

    public TransformMatrix(int r, boolean identity) {
        rows = r;
        columns = r;
        data = new double[rows][columns];
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                if (i == j && identity) {
                    getData()[j][j] = 1;
                } else {
                    getData()[j][i] = 0;
                }
            }
        }
    }

    public TransformMatrix(double[][] re) {
        rows = re.length;
        columns = re[0].length;
        data = new double[rows][columns];
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                getData()[j][i] = re[j][i];
            }
        }
    }

    static TransformMatrix multiply(TransformMatrix m1, TransformMatrix m2) throws Exception {
        if (m1.columns != m2.rows) {
            throw new Exception("row column mismatch");
        }
        TransformMatrix cm = new TransformMatrix(m1.rows, m2.columns);
        double tmpr;
        for (int i = 0; i < m1.rows; i++) {
            for (int j = 0; j < m2.columns; j++) {
                tmpr = 0.0;
                for (int k = 0; k < m1.columns; k++) {
                    tmpr += m1.getData()[i][k] * m2.getData()[k][j];
                }
                cm.getData()[i][j] = tmpr;
            }
        }
        return cm;
    }
    
    static void multiply(TransformMatrix m1, TransformMatrix m2, TransformMatrix result) throws Exception {
		if (m1.columns != m2.rows || m2.columns != result.columns
				|| m1.rows != result.rows) {
            throw new Exception("row column mismatch");
        }
        double tmpr;
        for (int i = 0; i < m1.rows; i++) {
            for (int j = 0; j < m2.columns; j++) {
                tmpr = 0.0;
                for (int k = 0; k < m1.columns; k++) {
                    tmpr += m1.getData()[i][k] * m2.getData()[k][j];
                }
                result.getData()[i][j] = tmpr;
            }
        }
    }

    public double trace() {
        double trace = 0;
        for (int k = 0; k < columns; k++) {
            trace += getData()[k][k];
        }
        return trace;
    }

    public void multiplyMatrix(TransformMatrix m2) throws Exception {
        if (columns != m2.rows) {
            throw new Exception("row column mismatch");
        }
        if (columns != m2.columns) {
            throw new Exception("row column mismatch - output different size to input");
        }
        double[][] cm = new double[rows][columns];
        double tmpr;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < m2.columns; j++) {
                tmpr = 0.0;
                for (int k = 0; k < columns; k++) {
                    tmpr += getData()[i][k] * m2.getData()[k][j];
                }
                cm[i][j] = tmpr;
            }
        }

        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                getData()[j][i] = cm[j][i];
            }
        }
    }

    public void preMultiplyMatrix(TransformMatrix m2) throws Exception {
        if (m2.columns != rows) {
            throw new Exception("row column mismatch");
        }
        if (rows != m2.rows) {
            throw new Exception("row column mismatch - output different size to input");
        }
        double[][] cm = new double[rows][columns];
        double tmpr;
        for (int i = 0; i < m2.rows; i++) {
            for (int j = 0; j < columns; j++) {
                tmpr = 0.0;
                for (int k = 0; k < m2.columns; k++) {
                    tmpr += m2.getData()[i][k] * getData()[k][j];
                }
                cm[i][j] = tmpr;
            }
        }

        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                getData()[j][i] = cm[j][i];
            }
        }
    }

    public void multiply(double r) {
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                getData()[j][i] *= r;
            }
        }
    }

    public void addMatrix(TransformMatrix cm) throws Exception {
        if (rows != cm.rows || columns != cm.columns) {
            throw new Exception("row column mismatch");
        }
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                getData()[j][i] = getData()[j][i] + cm.getData()[j][i];
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(5);
        nf.setMaximumFractionDigits(5);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                sb.append(nf.format(getData()[i][j]));
                sb.append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void transpose() {
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < columns; j++) {
                double tmp = getData()[i][j];
                getData()[i][j] = getData()[j][i];
                getData()[j][i] = tmp;
            }
        }
    }

	public double[][] getData() {
		return data;
	}
}

