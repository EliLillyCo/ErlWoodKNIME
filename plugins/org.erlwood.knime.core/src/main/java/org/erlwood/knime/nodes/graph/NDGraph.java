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


public interface NDGraph {
    void setXScale(double min, double max);
    void setYScale(double min, double max);
    void setZScale(double min, double max);
    void rePlotGraph();
    double getCurrMaxX();
    void setCurrMaxX(double currMaxX);
    double getCurrMaxY();
    void setCurrMaxY(double currMaxY);
    double getCurrMaxZ();
    void setCurrMaxZ(double currMaxY);
    double getCurrMinX();
    void setCurrMinX(double currMinX);
    double getCurrMinY();
    void setCurrMinY(double currMinY);
    double getCurrMinZ();
    void setCurrMinZ(double currMinY);
    double getMaxX();
    double getMaxY();
    double getMaxZ();
    double getMinX();
    double getMinY();
    double getMinZ();
    void setXAxisTitle(String s);
    void setYAxisTitle(String s);
    void setZAxisTitle(String s);
    String getXAxisTitle();
    String getYAxisTitle();
    String getZAxisTitle();
}

