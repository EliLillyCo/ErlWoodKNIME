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


final class PlotScale {

    private double lowLimit;
    private double highLimit;
    private double tick, firstTick;
    private String label;
    private NumberFormat nf;
    private int type;
    
    static final int X_AXIS = 0;
    static final int Y_AXIS = 1;
    static final int Z_AXIS = 2;

    PlotScale(double low, double high, String l, int t) {
        setLowLimit(low);
        setLabel(l);
        setHighLimit(high);
        setNf(NumberFormat.getInstance());
        doTick();
        setType(t);
    }

    public void updateScale(double low, double high) {
        setLowLimit(low);
        setHighLimit(high);
        doTick();
    }

    public void doTick() {
        double x = Math.abs(getLowLimit() - getHighLimit());
        if (x == 0) {
            x = Math.abs(getLowLimit() / 100);
        }
        int index = 0;
        while (x > 10) {
            x = x / 10;
            index++;
        }
        while (x < 1) {
            x = x * 10;
            index--;
        }
        setTick(2.000001 * Math.pow(10.000, index));
        if (x < 5) {
            setTick(1.000001 * Math.pow(10.000, index));
        }
        if (x < 2.5) {
            setTick(5.000001 * Math.pow(10.000, index - 1));
        }
        if (x < 1.25) {
            setTick(2.000001 * Math.pow(10.000, index - 1));
        }
        setFirstTick(getTick() * (int) ((getLowLimit()) / getTick()));
		if (getFirstTick() < getLowLimit()) {
			setFirstTick(getFirstTick() + getTick());
		}
        getNf().setMinimumFractionDigits(-(index - 1));
        getNf().setMaximumFractionDigits(-(index - 1));
    }

	public double getFirstTick() {
		return firstTick;
	}

	public void setFirstTick(double firstTick) {
		this.firstTick = firstTick;
	}

	public double getHighLimit() {
		return highLimit;
	}

	public void setHighLimit(double highLimit) {
		this.highLimit = highLimit;
	}

	public double getLowLimit() {
		return lowLimit;
	}

	public void setLowLimit(double lowLimit) {
		this.lowLimit = lowLimit;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public NumberFormat getNf() {
		return nf;
	}

	public void setNf(NumberFormat nf) {
		this.nf = nf;
	}

	public double getTick() {
		return tick;
	}

	public void setTick(double tick) {
		this.tick = tick;
	}
}
