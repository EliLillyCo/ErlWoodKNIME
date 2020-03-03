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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


@SuppressWarnings("serial")
public class SliderPanel extends JPanel implements ChangeListener {
	private RangeSlider xRange;
	private RangeSlider yRange;
	private RangeSlider zRange;
	private JLabel xLab, yLab, zLab;
	private NDGraph graph;

	public SliderPanel() {
		setBackground(Color.white);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		xRange = new RangeSlider(0, 1000);
		yRange = new RangeSlider(0, 1000);
		zRange = new RangeSlider(0, 1000);
		xRange.setName("X");
		yRange.setName("Y");
		zRange.setName("Z");
		xRange.setValue(0);
		xRange.setUpperValue(1000);
		yRange.setValue(0);
		yRange.setUpperValue(1000);
		zRange.setValue(0);
		zRange.setUpperValue(1000);
		xRange.addChangeListener(this);
		yRange.addChangeListener(this);
		zRange.addChangeListener(this);

		xLab = new JLabel();
		yLab = new JLabel();
		zLab = new JLabel();

		add(xLab, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(xRange, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		add(yLab, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(yRange, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		add(zLab, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(zRange, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

	}

	public SliderPanel(NDGraph cp) {
		graph = cp;
		
		setBackground(Color.white);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		xRange = new RangeSlider(0, 1000);
		yRange = new RangeSlider(0, 1000);
		zRange = new RangeSlider(0, 1000);
		xRange.setName("X");
		yRange.setName("Y");
		zRange.setName("Z");
		xRange.setValue(0);
		xRange.setUpperValue(1000);
		yRange.setValue(0);
		yRange.setUpperValue(1000);
		zRange.setValue(0);
		zRange.setUpperValue(1000);
		xRange.addChangeListener(this);
		yRange.addChangeListener(this);
		zRange.addChangeListener(this);

		xLab = new JLabel(graph.getXAxisTitle());
		yLab = new JLabel(graph.getYAxisTitle());
		zLab = new JLabel(graph.getZAxisTitle());

		add(xLab, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(xRange, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		add(yLab, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(yRange, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		add(zLab, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(zRange, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
	}

	public void setPanel(NDGraph cp) {
		graph = cp;
		xLab.setText(graph.getXAxisTitle());
		yLab.setText(graph.getYAxisTitle());
		zLab.setText(graph.getZAxisTitle());
	}

	public void stateChanged(ChangeEvent e) {
		RangeSlider source = (RangeSlider) e.getSource();
		double v1, v2;
		if (source.getName().compareTo("X") == 0) {
			v1 = graph.getMinX() + (double) source.getValue() / 1000.0
					* (graph.getMaxX() - graph.getMinX());
			v2 = graph.getMinX() + (double) source.getUpperValue() / 1000.0
					* (graph.getMaxX() - graph.getMinX());
			graph.setXScale(v1, v2);
		} else if (source.getName().compareTo("Y") == 0) {
			v1 = graph.getMinY() + (double) source.getValue() / 1000.0
					* (graph.getMaxY() - graph.getMinY());
			v2 = graph.getMinY() + (double) source.getUpperValue() / 1000.0
					* (graph.getMaxY() - graph.getMinY());
			graph.setYScale(v1, v2);
		} else {
			v1 = graph.getMinZ() + (double) source.getValue() / 1000.0
					* (graph.getMaxZ() - graph.getMinZ());
			v2 = graph.getMinZ() + (double) source.getUpperValue() / 1000.0
					* (graph.getMaxZ() - graph.getMinZ());
			graph.setZScale(v1, v2);
		}
	}

	public void updateSliderToZoomedRegion() {
		int x1, x2, y1, y2, z1, z2;
		x1 = (int) (1000 * (graph.getCurrMinX() - graph.getMinX()) / (graph
				.getMaxX() - graph.getMinX()));
		x2 = (int) (1000 * (graph.getCurrMaxX() - graph.getMinX()) / (graph
				.getMaxX() - graph.getMinX()));
		y1 = (int) (1000 * (graph.getCurrMinY() - graph.getMinY()) / (graph
				.getMaxY() - graph.getMinY()));
		y2 = (int) (1000 * (graph.getCurrMaxY() - graph.getMinY()) / (graph
				.getMaxY() - graph.getMinY()));
		z1 = (int) (1000 * (graph.getCurrMinZ() - graph.getMinZ()) / (graph
				.getMaxZ() - graph.getMinZ()));
		z2 = (int) (1000 * (graph.getCurrMaxZ() - graph.getMinZ()) / (graph
				.getMaxZ() - graph.getMinZ()));
		xRange.setValue(x1);
		xRange.setUpperValue(x2);
		yRange.setValue(y1);
		yRange.setUpperValue(y2);
		zRange.setValue(z1);
		zRange.setUpperValue(z2);
	}

	public void hideZSlider() {
		zLab.setVisible(false);
		zRange.setVisible(false);
	}

	public void showZSlider() {
		zLab.setVisible(true);
		zRange.setVisible(true);
	}

}
