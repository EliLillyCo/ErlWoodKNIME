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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.knime.core.node.NodeLogger;


public final class GenGraph {
	private static final NodeLogger LOG = NodeLogger.getLogger(GenGraph.class);
	
	private GenGraph() {
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		List<DataPoint> al = new ArrayList<DataPoint>();
		for (int i = 0; i < 200; i++) {
			DataPoint pp = new DataPoint(50 + i / 100.0 + Math.random() / 4, 50
					+ i / 50.0 + Math.random() / 2, 50 + i / 10.0
					+ Math.random() * 2);
			pp.setColorLabel("colour 1");
			pp.setColorValue((double) i);
			pp.setSizeLabel("a small size");
			pp.setSizeValue((double) i);
			al.add(pp);

		}

		demo3Dgraph(al);
		demo2DGraph(al);

	}

	public static void createGraphView(List<DataPoint> al) {
		JFrame jf = new JFrame("corrPlot");
		jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		TwoDGraph cp = new TwoDGraph();
		ThreeDGraph tdg = new ThreeDGraph();

		// test datpoints
		for (DataPoint dp : al) {
			cp.addPoint(dp);
			tdg.addPoint(dp);
		}
		cp.setFitType(TwoDGraph.FORCE_ORIGIN);

		cp.doSetup();
		Legend l = new Legend(tdg, tdg.getpList());

		l.setColorType(Legend.CONTINUOUS);
		l.calcColorValues();
		l.generateLegend();

		cp.setSize(400, 400);
		cp.setPreferredSize(new Dimension(400, 400));
		tdg.doSetup();
		tdg.setPreferredSize(new Dimension(400, 400));
		tdg.setSize(new Dimension(400, 400));

		JPanel rPanel = new JPanel(new BorderLayout());
		JPanel selPanel = new JPanel();
		JComboBox xChoice = new JComboBox();
		JComboBox yChoice = new JComboBox();
		JComboBox zChoice = new JComboBox();
		JComboBox colChoice = new JComboBox();
		JComboBox sizeChoice = new JComboBox();
		xChoice.addItem("col1");

		selPanel.add(new JLabel("x values"));
		selPanel.add(xChoice);
		selPanel.add(new JLabel("y values"));
		selPanel.add(yChoice);
		selPanel.add(new JLabel("z values"));
		selPanel.add(zChoice);
		selPanel.add(new JLabel("color values"));
		selPanel.add(colChoice);
		selPanel.add(new JLabel("size values"));
		selPanel.add(sizeChoice);

		rPanel.add(tdg, BorderLayout.CENTER);
		rPanel.add(l, BorderLayout.EAST);

		jf.add(rPanel, BorderLayout.CENTER);
		jf.add(selPanel, BorderLayout.SOUTH);
		jf.pack();
		jf.setVisible(true);
	}

	public static void demo2DGraph(List<DataPoint> al) {
		JFrame jf = new JFrame("corrPlot");
		jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		TwoDGraph cp = new TwoDGraph();

		// test datpoints
		for (DataPoint dp : al) {
			cp.addPoint(dp);
		}
		cp.doSetup();
		Legend l = new Legend(cp, cp.getPList());

		l.setColorType(Legend.CONTINUOUS);
		l.calcColorValues();
		l.generateLegend();

		LOG.debug("slope is " + Double.toString(cp.getSlope()));
		LOG.debug("intercept is " + Double.toString(cp.getIntercept()));
		LOG.debug("r2 is " + Double.toString(cp.getR2()));
		cp.setSize(250, 250);
		cp.setPreferredSize(new Dimension(250, 250));
		jf.add(cp, BorderLayout.CENTER);
		JScrollPane sp = new JScrollPane(l);
		sp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		SliderPanel slidP = new SliderPanel(cp);
		slidP.hideZSlider();
		JPanel pp = new JPanel(new GridBagLayout());
		pp.add(sp, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		pp.add(slidP, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		jf.add(pp, BorderLayout.EAST);

		jf.pack();
		jf.setVisible(true);
	}

	public static void demo3Dgraph(List<DataPoint> al) {
		JFrame jf = new JFrame("3d graph test");
		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ThreeDGraph tdg = new ThreeDGraph();

		for (DataPoint dp : al) {
			tdg.addPoint(dp);
		}
		tdg.doSetup();
		tdg.setPreferredSize(new Dimension(400, 400));
		tdg.setSize(new Dimension(400, 400));
		Legend l = new Legend(tdg, tdg.getpList());

		l.setColorType(Legend.CONTINUOUS);
		l.calcColorValues();
		l.generateLegend();

		jf.add(tdg, BorderLayout.CENTER);
		JScrollPane sp = new JScrollPane(l);
		sp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		SliderPanel slidP = new SliderPanel(tdg);
		JPanel pp = new JPanel(new GridBagLayout());
		pp.add(sp, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		pp.add(slidP, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		jf.add(pp, BorderLayout.EAST);

		jf.pack();
		jf.setVisible(true);
	}

}
