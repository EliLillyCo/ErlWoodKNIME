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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import org.knime.core.node.NodeLogger;


@SuppressWarnings("serial")
public final class ThreeDGraph extends JPanel implements MouseWheelListener,
		KeyListener, MouseListener, MouseMotionListener, NDGraph {
	private static final NodeLogger LOG = NodeLogger.getLogger(ThreeDGraph.class);
	private int prevx, prevy, startx, starty, nowx, nowy;
	private boolean drawBox;
	private Popup popup;
	private PopupComponentProvider popupComponentProvider;
	private double minX, maxX, minY, maxY, minZ, maxZ, currMinX, currMaxX,
			currMinY, currMaxY, currMinZ, currMaxZ;
	private double tx = 0.0;
	private double ty = 0.0;
	private double tz = 0.0;
	private double camZ = 5.0;
	private double eyeZ = 5.0;
	private PlotScale xScal, yScal, zScal;
	private List<DataPoint> pList;
	private List<AxisFace> axesFaces;
	private TransformMatrix tr1, rot1, tr2, proj, currTr;
	private List<Edge> edges;
	private List<DataPoint> vertices;
	private Color hiliteColor;

	static final Comparator<DataPoint> ZORDER = new Comparator<DataPoint>() {

		public int compare(DataPoint p1, DataPoint p2) {
			return new Double(p2.getTrZ()).compareTo(new Double(p1.getTrZ()));
		}
	};
	static final Comparator<AxisFace> ZORDERAF = new Comparator<AxisFace>() {

		public int compare(AxisFace a1, AxisFace a2) {
			return new Double(a1.getMidZ()).compareTo(new Double(a2.getMidZ()));
		}
	};

	public static void main(String[] args) {

		JFrame jf = new JFrame("3d graph test");
		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ThreeDGraph tdg = new ThreeDGraph();
		// test datpoints
		for (int i = 75; i < 200; i++) {
			tdg.addPoint(i / 100.0 + Math.random() / 4,
					i / 50.0 + Math.random() / 2, i / 10.0 + Math.random());
		}
		for (int i = 0; i < 125; i++) {
			DataPoint pp = tdg.addPoint(i / 100.0 + Math.random() / 2, i / 50.0
					+ Math.random(), i / 10.0 + Math.random() * 2);
			pp.setColor(Color.blue);
			tdg.addPoint(pp);
		}
		for (int i = 0; i < 50; i++) {
			DataPoint pp = tdg.addPoint((50 - i) / 100.0 + Math.random() / 2, i
					* i / 2000.0 + Math.random(), i / 10.0 + Math.random() * 2);
			pp.setPointSize(8);
			pp.setColor(Color.red);
			tdg.addPoint(pp);
		}
		tdg.doSetup();
		tdg.setPreferredSize(new Dimension(400, 400));
		tdg.setSize(new Dimension(400, 400));
		jf.add(tdg);
		jf.pack();
		jf.setVisible(true);

	}

	public DataPoint addPoint(double x, double y, double z) {
		DataPoint pp = new DataPoint(x, y, z);
		getpList().add(pp);
		return pp;
	}

	public void addPoint(DataPoint d) {
		getpList().add(d);
	}

	public void clearPoints() {
		getpList().clear();
	}

	public void setXAxisTitle(String s) {
		xScal.setLabel(s);
		repaint();
	}

	public void setYAxisTitle(String s) {
		yScal.setLabel(s);
		repaint();
	}

	public void setZAxisTitle(String s) {
		zScal.setLabel(s);
		repaint();
	}

	public String getXAxisTitle() {
		return xScal.getLabel();
	}

	public String getYAxisTitle() {
		return yScal.getLabel();
	}

	public String getZAxisTitle() {
		return zScal.getLabel();
	}

	public void setXScale(double min, double max) {
		// set scale limits to this
		currMinX = min;
		currMaxX = max;
		scaleToCurrentLimits();
	}

	public void setYScale(double min, double max) {
		// set scale limits to this
		currMinY = min;
		currMaxY = max;
		scaleToCurrentLimits();
	}

	public void setZScale(double min, double max) {
		// Do Nothing
		currMinZ = min;
		currMaxZ = max;
		scaleToCurrentLimits();
	}

	public void rePlotGraph() {
		repaint();
	}

	public double getCurrMaxX() {
		return currMaxX;
	}

	public void setCurrMaxX(double currMaxX) {
		this.currMaxX = currMaxX;
	}

	public double getCurrMaxY() {
		return currMaxY;
	}

	public void setCurrMaxY(double currMaxY) {
		this.currMaxY = currMaxY;
	}

	public double getCurrMaxZ() {
		return currMaxZ;
	}

	public void setCurrMaxZ(double currMaxY) {
		this.currMaxZ = currMaxY;
	}

	public double getCurrMinX() {
		return currMinX;
	}

	public void setCurrMinX(double currMinX) {
		this.currMinX = currMinX;
	}

	public double getCurrMinY() {
		return currMinY;
	}

	public void setCurrMinY(double currMinY) {
		this.currMinY = currMinY;
	}

	public double getCurrMinZ() {
		return currMinZ;
	}

	public void setCurrMinZ(double currMinY) {
		this.currMinZ = currMinY;
	}

	public double getMaxX() {
		return maxX;
	}

	public double getMaxY() {
		return maxY;
	}

	public double getMaxZ() {
		return maxZ;
	}

	public double getMinX() {
		return minX;
	}

	public double getMinY() {
		return minY;
	}

	public double getMinZ() {
		return minZ;
	}

	public ThreeDGraph() {
		// general set up
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		setFocusable(true);
		drawBox = false;
		setpList(new ArrayList<DataPoint>());
		xScal = new PlotScale(0.0, 1.0, "x axis", PlotScale.X_AXIS);
		yScal = new PlotScale(0.0, 1.0, "y axis", PlotScale.Y_AXIS);
		zScal = new PlotScale(0.0, 1.0, "z axis", PlotScale.Z_AXIS);
		setHiliteColor(Color.yellow);
	}

	public void resetView() {
		camZ = 5.0;
		eyeZ = 5.0;
		rot1 = TransformMatrix.getRotation("x", 0.0);
		proj = TransformMatrix.getPerspective(0.0, 0.0, eyeZ);
		tr1 = getStandardDataTransformMatrix();
		tr2 = TransformMatrix.getTranslation(0.0, 0.0, camZ);
		setCurrMatrix();
	}

	public void setupFacesAxes(double minx, double maxx, double miny,
			double maxy, double minz, double maxz) {
		// set up axis vertices
		DataPoint p000 = new DataPoint(minx, miny, minz);
		DataPoint p100 = new DataPoint(maxx, miny, minz);
		DataPoint p010 = new DataPoint(minx, maxy, minz);
		DataPoint p001 = new DataPoint(minx, miny, maxz);
		DataPoint p011 = new DataPoint(minx, maxy, maxz);
		DataPoint p101 = new DataPoint(maxx, miny, maxz);
		DataPoint p110 = new DataPoint(maxx, maxy, minz);
		DataPoint p111 = new DataPoint(maxx, maxy, maxz);

		// add to global list
		vertices = new ArrayList<DataPoint>();
		vertices.add(p000);
		vertices.add(p100);
		vertices.add(p010);
		vertices.add(p001);
		vertices.add(p011);
		vertices.add(p101);
		vertices.add(p110);
		vertices.add(p111);

		// set up scales
		xScal.updateScale(minx, maxx);
		yScal.updateScale(miny, maxy);
		zScal.updateScale(minz, maxz);

		// create edges
		Edge e000To100 = new Edge();
		Edge e000To010 = new Edge();
		Edge e100To110 = new Edge();
		Edge e010To110 = new Edge();

		Edge e001To101 = new Edge();
		Edge e001To011 = new Edge();
		Edge e101To111 = new Edge();
		Edge e011To111 = new Edge();

		Edge e000To001 = new Edge();
		Edge e100To101 = new Edge();
		Edge e110To111 = new Edge();
		Edge e010To011 = new Edge();

		// add points to edges using vertices and scales
		e000To100.addScalePoints(p000, p100, xScal);
		e000To010.addScalePoints(p000, p010, yScal);
		e100To110.addScalePoints(p100, p110, yScal);
		e010To110.addScalePoints(p010, p110, xScal);

		e001To101.addScalePoints(p001, p101, xScal);
		e001To011.addScalePoints(p001, p011, yScal);
		e101To111.addScalePoints(p101, p111, yScal);
		e011To111.addScalePoints(p011, p111, xScal);

		e000To001.addScalePoints(p000, p001, zScal);
		e100To101.addScalePoints(p100, p101, zScal);
		e110To111.addScalePoints(p110, p111, zScal);
		e010To011.addScalePoints(p010, p011, zScal);

		// add to global list
		edges = new ArrayList<Edge>();
		edges.add(e000To100);
		edges.add(e000To010);
		edges.add(e100To110);
		edges.add(e010To110);

		edges.add(e001To101);
		edges.add(e001To011);
		edges.add(e101To111);
		edges.add(e011To111);

		edges.add(e000To001);
		edges.add(e100To101);
		edges.add(e110To111);
		edges.add(e010To011);

		// create e faces
		axesFaces = new ArrayList<AxisFace>(6);

		for (int i = 0; i < 6; i++) {
			axesFaces.add(new AxisFace());
		}

		// add vertices and edges
		axesFaces.get(0).addVertices(p000, p001, p011, p010);
		axesFaces.get(1).addVertices(p000, p100, p101, p001);
		axesFaces.get(2).addVertices(p000, p010, p110, p100);
		axesFaces.get(3).addVertices(p100, p110, p111, p101);
		axesFaces.get(4).addVertices(p010, p011, p111, p110);
		axesFaces.get(5).addVertices(p001, p101, p111, p011);

		axesFaces.get(0).addEdges(e000To001, e001To011, e010To011, e000To010);
		axesFaces.get(1).addEdges(e000To100, e100To101, e001To101, e000To001);
		axesFaces.get(2).addEdges(e000To010, e010To110, e100To110, e000To100);
		axesFaces.get(3).addEdges(e100To110, e110To111, e101To111, e100To101);
		axesFaces.get(4).addEdges(e010To011, e011To111, e110To111, e010To110);
		axesFaces.get(5).addEdges(e001To101, e101To111, e011To111, e001To011);

	}

	public void doSetup() {

		// set up matrices
		if (rot1 == null) {
			rot1 = TransformMatrix.getRotation("x", 0.0);
		}
		proj = TransformMatrix.getPerspective(0.0, 0.0, eyeZ);
		tr1 = getStandardDataTransformMatrix();
		tr2 = TransformMatrix.getTranslation(0.0, 0.0, camZ);
		setCurrMatrix();
		setupFacesAxes(minX, maxX, minY, maxY, minZ, maxZ);

	}

	public boolean doesEdgeBelongToVisibleFace(Edge edge) {
		for (AxisFace af : axesFaces) {
			if (!af.bf) {
				for (int i = 0; i < 4; i++) {
					if (af.edges[i] == edge) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public AxisFace getInvisibleFaceForEdge(Edge edge) {
		for (AxisFace af : axesFaces) {
			if (af.bf) {
				for (int i = 0; i < 4; i++) {
					if (af.edges[i] == edge) {
						return af;
					}
				}
			}
		}
		return null;
	}

	public boolean doesEdgeBelongToInvisibleFace(Edge edge) {
		for (AxisFace af : axesFaces) {
			if (af.bf) {
				for (int i = 0; i < 4; i++) {
					if (af.edges[i] == edge) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// gets a transform matrix that will fit all data points in the display
	public TransformMatrix getStandardDataTransformMatrix() {
		if (getpList().size() == 0) {
			minX = 0.0;
			minY = 0.0;
			minZ = 0.0;
			maxX = 1.0;
			maxY = 1.0;
			maxZ = 1.0;
		} else if (getpList().size() == 1) {
			minX = getpList().get(0).getRawX() - 1.0;
			minY = getpList().get(0).getRawY() - 1.0;
			minZ = getpList().get(0).getRawZ() - 1.0;
			maxX = getpList().get(0).getRawX() + 1.0;
			maxY = getpList().get(0).getRawY() + 1.0;
			maxZ = getpList().get(0).getRawZ() + 1.0;
		} else {
			minX = Double.MAX_VALUE;
			minY = Double.MAX_VALUE;
			minZ = Double.MAX_VALUE;
			maxX = Double.NEGATIVE_INFINITY;
			maxY = Double.NEGATIVE_INFINITY;
			maxZ = Double.NEGATIVE_INFINITY;
			for (DataPoint dp : getpList()) {
				if (dp.getRawX() > maxX) {
					maxX = dp.getRawX();
				}

				if (dp.getRawY() > maxY) {
					maxY = dp.getRawY();
				}

				if (dp.getRawZ() > maxZ) {
					maxZ = dp.getRawZ();
				}

				if (dp.getRawX() < minX) {
					minX = dp.getRawX();
				}

				if (dp.getRawY() < minY) {
					minY = dp.getRawY();
				}

				if (dp.getRawZ() < minZ) {
					minZ = dp.getRawZ();
				}
			}
			if (minX != maxX) {
				minX = minX - (maxX - minX) / 20.0;
				maxX = maxX + (maxX - minX) / 20.0;
			} else {
				if (minX == 0) {
					minX = -0.1;
					maxX = 0.1;
				} else {
					minX = minX * 0.99;
					maxX = maxX * 1.01;
				}
			}
			if (minY != maxY) {
				minY = minY - (maxY - minY) / 20.0;
				maxY = maxY + (maxY - minY) / 20.0;
			} else {
				if (minY == 0) {
					minY = -0.1;
					maxY = 0.1;
				} else {
					minY = minY * 0.99;
					maxY = maxY * 1.01;
				}
			}
			if (minZ != maxZ) {
				minZ = minZ - (maxZ - minZ) / 20.0;
				maxZ = maxZ + (maxZ - minZ) / 20.0;
			} else {
				if (minZ == 0 && maxZ == 0) {
					minZ = -0.1;
					maxZ = 0.1;
				} else {
					minZ = minZ * 0.99;
					maxZ = maxZ * 1.01;
				}
			}
		}
		currMinX = minX;
		currMinY = minY;
		currMinZ = minZ;
		currMaxX = maxX;
		currMaxY = maxY;
		currMaxZ = maxZ;
		try {
			return TransformMatrix.multiply(TransformMatrix.getScale(
					1.0 / (maxX - minX), 1.0 / (maxY - minY),
					1.0 / (maxZ - minZ)), TransformMatrix.getTranslation(
					-(minX + maxX) / 2.0, -(minY + maxY) / 2.0,
					-(minZ + maxZ) / 2.0));
		} catch (Exception e) {
			return null;
		}

	}

	public void zoomToSelection() {
		// find max and min of points in selection
		List<DataPoint> selPList = new ArrayList<DataPoint>();
		for (DataPoint p : getpList()) {
			if (p.isSelected() && p.isInZoomRegion()) {
				p.setInZoomRegion(true);
				selPList.add(p);
			} else {
				p.setInZoomRegion(false);
			}
		}
		if (selPList.size() > 1) {
			currMinX = Double.MAX_VALUE;
			currMinY = Double.MAX_VALUE;
			currMinZ = Double.MAX_VALUE;
			currMaxX = Double.NEGATIVE_INFINITY;
			currMaxY = Double.NEGATIVE_INFINITY;
			currMaxZ = Double.NEGATIVE_INFINITY;
			for (DataPoint dp : selPList) {
				if (dp.getRawX() > currMaxX) {
					currMaxX = dp.getRawX();
				}
				if (dp.getRawY() > currMaxY) {
					currMaxY = dp.getRawY();
				}
				if (dp.getRawZ() > currMaxZ) {
					currMaxZ = dp.getRawZ();
				}
				if (dp.getRawX() < currMinX) {
					currMinX = dp.getRawX();
				}
				if (dp.getRawY() < currMinY) {
					currMinY = dp.getRawY();
				}
				if (dp.getRawZ() < currMinZ) {
					currMinZ = dp.getRawZ();
				}
			}
			// set scale limits to this
			scaleToCurrentLimits();
		}

	}

	public void scaleToCurrentLimits() {
		// check data points in limits
		for (DataPoint p : getpList()) {
			if (p.getRawX() >= currMinX && p.getRawX() <= currMaxX
					&& p.getRawY() >= currMinY && p.getRawY() <= currMaxY
					&& p.getRawZ() >= currMinZ && p.getRawZ() <= currMaxZ) {
				p.setInZoomRegion(true);
			} else {
				p.setInZoomRegion(false);
			}
		}
		try {
			tr1 = TransformMatrix.multiply(TransformMatrix.getScale(
					1.0 / (currMaxX - currMinX), 1.0 / (currMaxY - currMinY),
					1.0 / (currMaxZ - currMinZ)), TransformMatrix
					.getTranslation(-(currMinX + currMaxX) / 2.0,
							-(currMinY + currMaxY) / 2.0,
							-(currMinZ + currMaxZ) / 2.0));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		setCurrMatrix();
		setupFacesAxes(currMinX, currMaxX, currMinY, currMaxY, currMinZ,
				currMaxZ);
		// repaint view
		repaint();
	}

	public void resetZoom() {
		for (DataPoint p : getpList()) {
			p.setInZoomRegion(true);
		}
		tr1 = getStandardDataTransformMatrix();
		setCurrMatrix();
		setupFacesAxes(minX, maxX, minY, maxY, minZ, maxZ);
		repaint();
	}

	public void setCurrMatrix() {
		try {
			currTr = TransformMatrix.multiply(proj, tr2);
			currTr.multiplyMatrix(rot1);
			currTr.multiplyMatrix(tr1);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (popup != null) {
			popup.hide();
		}
		if (e.isPopupTrigger()) {
			makeAndShowPopup(e);
		} else {
			prevx = e.getX();
			prevy = e.getY();
			if (e.isControlDown()) {
				nowx = e.getX();
				nowy = e.getY();
				startx = e.getX();
				starty = e.getY();
				drawBox = true;
			}
		}
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			resetView();
		}
	}

	public void mouseReleased(MouseEvent e) {
		drawBox = false;
		if (e.isPopupTrigger()) {
			makeAndShowPopup(e);
		} else {
			if (e.isControlDown()) {
				selectPointsInBox(e.isShiftDown());
			}
		}
		repaint();
	}

	public void makeAndShowPopup(MouseEvent e) {
		if (popupComponentProvider != null) {
			PopupFactory factory = PopupFactory.getSharedInstance();
			DataPoint dp = getPointClosestToClick(e.getX(), e.getY());
			Component c = popupComponentProvider.getComponent(dp.getID());
			if (popup != null) {
				popup.hide();
			}
			popup = factory.getPopup(this, c, (int) dp.getScreenX()
					+ getLocationOnScreen().x,
					(int) dp.getScreenY() + this.getLocationOnScreen().y);
			popup.show();
		}
	}

	public void selectPointsInBox(boolean add) {
		int bx1, bx2, by1, by2;
		if (startx < nowx) {
			bx1 = startx;
			bx2 = nowx;
		} else {
			bx2 = startx;
			bx1 = nowx;
		}
		if (starty < nowy) {
			by1 = starty;
			by2 = nowy;
		} else {
			by2 = starty;
			by1 = nowy;
		}
		if (!add) {
			for (DataPoint p : getpList()) {
				p.setSelected(false);
			}
		}
		for (DataPoint p : getpList()) {
			if (p.getScreenX() + p.getSize() / 2 > bx1 && p.getScreenX() - p.getSize() / 2 < bx2
					&& p.getScreenY() + p.getSize() / 2 > by1
					&& p.getScreenY() - p.getSize() / 2 < by2) {
				p.setSelected(true);
			}
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isShiftDown()) {
			camZ += e.getWheelRotation() / 20.0;
		} else {
			eyeZ += e.getWheelRotation() / 20.0;
		}
		proj = TransformMatrix.getPerspective(0.0, 0.0, eyeZ);
		tr2 = TransformMatrix.getTranslation(0.0, 0.0, camZ);
		setCurrMatrix();
		repaint();
	}

	public void keyPressed(KeyEvent e) {
		// Invoked when a key has been pressed.

		if (e.getKeyChar() == 's') {
			camZ += 0.08;
		}

		if (e.getKeyChar() == 'a') {
			camZ -= 0.08;
		}

		if (e.getKeyChar() == 'q') {
			eyeZ += 0.08;

		}

		if (e.getKeyChar() == 'w') {
			eyeZ -= 0.08;
		}

		proj = TransformMatrix.getPerspective(0.0, 0.0, eyeZ);
		tr2 = TransformMatrix.getTranslation(0.0, 0.0, camZ);
		setCurrMatrix();

		repaint();

	}

	public void keyReleased(KeyEvent e) {
		// Invoked when a key has been released.
	}

	public void keyTyped(KeyEvent e) {
		// Invoked when a key has been typed.
	}

	public void mouseDragged(MouseEvent e) {
		nowx = e.getX();
		nowy = e.getY();
		if (!e.isControlDown()) {
			if (e.isShiftDown()) {
				tz = (-Math.PI * 0.005 * (nowy - prevy));
				try {
					rot1.preMultiplyMatrix(TransformMatrix.getRotation("z", tz));
				} catch (Exception ex) {
					LOG.error(ex.getMessage(), ex);
				}

			} else {
				tx = (-Math.PI * 0.005 * (nowx - prevx));
				ty = (-Math.PI * 0.005 * (nowy - prevy));
				try {
					rot1.preMultiplyMatrix(TransformMatrix.getRotation("x", ty));
					rot1.preMultiplyMatrix(TransformMatrix.getRotation("y", tx));
				} catch (Exception ex) {
					LOG.error(ex.getMessage(), ex);
				}
			}
			setCurrMatrix();
		}
		prevx = e.getX();
		prevy = e.getY();
		repaint();
	}

	public DataPoint getPointClosestToClick(int x, int y) {
		double minDist = Double.MAX_VALUE;
		DataPoint rp = null;
		for (int i = getpList().size() - 1; i >= 0; i--) {
			DataPoint dp = getpList().get(i);
			double dist = Math.sqrt((dp.getScreenX() - x) * (dp.getScreenX() - x)
					+ (dp.getScreenY() - y) * (dp.getScreenY() - y));
			if (dist < minDist) {
				minDist = dist;
				rp = dp;
			}
		}
		return rp;
	}

	public void sortZOrder() {
		Collections.sort(getpList(), ZORDER);
		Collections.sort(axesFaces, ZORDERAF);
	}

	public void paintComponent(Graphics ng) {
		Dimension d = getSize();
		Graphics2D g = (Graphics2D) ng;
		g.setFont(new Font("sansserif", Font.PLAIN, 10));
		RenderingHints rh = new RenderingHints(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHints(rh);
		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHints(rh);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, d.width, d.height);

		// transform various points
		for (DataPoint pp : getpList()) {
			pp.projectPoint(currTr, d.width, d.height);
		}

		for (DataPoint pp : vertices) {
			pp.projectPoint(currTr, d.width, d.height);
		}

		for (Edge e : edges) {
			for (DataPoint p : e.scalPoints) {
				p.projectPoint(currTr, d.width, d.height);
			}
		}

		// sort z order of points and figure out which faces are facing back
		sortZOrder();
		for (AxisFace af : axesFaces) {
			af.setBackFacing();
		}
		// plot back faces first
		for (AxisFace af : axesFaces) {
			if (af.bf) {
				af.plotFace(g);
			}
		}
		// plot datapoints
		for (DataPoint pp : getpList()) {
			if (pp.isHilited() && pp.isVisible() && pp.isInZoomRegion()) {
				g.setColor(getHiliteColor());
				g.fillOval((int) (pp.getScreenX() - (pp.getSize() + 4) / 2),
						(int) (pp.getScreenY() - (pp.getSize() + 4) / 2), pp.getSize() + 4,
						pp.getSize() + 4);
			}
			if (pp.isSelected()) {
				g.setColor(pp.getSelColor());
			} else {
				g.setColor(pp.getColor());
			}
			if (pp.isVisible() && pp.isInZoomRegion()) {
				g.fillOval((int) (pp.getScreenX() - pp.getSize() / 2),
						(int) (pp.getScreenY() - pp.getSize() / 2), pp.getSize(), pp.getSize());
			}

		}
		// plot axis faces for forward facing
		for (AxisFace af : axesFaces) {
			if (!af.bf) {
				af.plotFace(g);
			}
		}

		// plot labels
		for (Edge e : edges) {
			FontMetrics fm = g.getFontMetrics();
			if (doesEdgeBelongToVisibleFace(e)
					&& doesEdgeBelongToInvisibleFace(e)) {
				// get angle on screen of this edge
				double offs = 10.0;
				double dx = (double) (Math.abs(e.vertices[1].getScreenX()
						- e.vertices[0].getScreenX()));
				double dy = (double) (Math.abs(e.vertices[1].getScreenY()
						- e.vertices[0].getScreenY()));
				double ang = Math.abs(Math.atan2(dy, dx));
				double sx, sy;
				sx = 1.0;
				sy = 1.0;
				AxisFace af = getInvisibleFaceForEdge(e);

				if (af.isEdgeOnRightSide(e)) {
					sx = -1.0;
				}
				if (af.isEdgeOnTopSide(e)) {
					sy = -1.0;
				}

				for (int i = 0; i < e.scalPoints.size(); i++) {
					String lab = e.getVal(i);
					DataPoint dp = e.scalPoints.get(i);

					float yoffs = (float) (-sy * offs * Math.cos(ang));
					float xoffs = (float) (sx * offs * Math.sin(ang));
					AffineTransform at = g.getTransform();
					g.translate(dp.getScreenX() + xoffs, dp.getScreenY() + yoffs);
					double ang2 = Math.atan2(e.vertices[1].getScreenY()
							- e.vertices[0].getScreenY(), e.vertices[1].getScreenX()
							- e.vertices[0].getScreenX());
					if (ang2 > Math.PI / 2) {
						ang2 -= Math.PI;
					}
					if (ang2 < -Math.PI / 2) {
						ang2 += Math.PI;
					}
					g.rotate(ang2);
					g.translate(-fm.stringWidth(lab) / 2, (fm.getAscent()) / 3);
					g.drawString(lab, 0, 0);
					g.setTransform(at);

				}
				// plot axis title
				offs = 25;
				float xp = (e.vertices[0].getScreenX() + e.vertices[1].getScreenX()) / 2;
				float yp = (e.vertices[0].getScreenY() + e.vertices[1].getScreenY()) / 2;
				String lab = e.scal.getLabel();

				float yoffs = (float) (-sy * offs * Math.cos(ang));
				float xoffs = (float) (sx * offs * Math.sin(ang));
				AffineTransform at = g.getTransform();
				g.translate(xp + xoffs, yp + yoffs);
				double ang2 = Math.atan2(e.vertices[1].getScreenY()
						- e.vertices[0].getScreenY(), e.vertices[1].getScreenX()
						- e.vertices[0].getScreenX());
				if (ang2 > Math.PI / 2) {
					ang2 -= Math.PI;
				}
				if (ang2 < -Math.PI / 2) {
					ang2 += Math.PI;
				}
				g.rotate(ang2);
				g.translate(-fm.stringWidth(lab) / 2, (fm.getAscent()) / 3);
				g.drawString(lab, 0, 0);
				g.setTransform(at);
			}

		}
		// plot box for selection
		if (drawBox) {
			int bx1, bx2, by1, by2;
			if (startx < nowx) {
				bx1 = startx;
				bx2 = nowx;
			} else {
				bx2 = startx;
				bx1 = nowx;
			}
			if (starty < nowy) {
				by1 = starty;
				by2 = nowy;
			} else {
				by2 = starty;
				by1 = nowy;
			}
			g.setColor(Color.RED);
			g.drawRect(bx1, by1, bx2 - bx1, by2 - by1);
		}
	}

	public void setPopupComponentProvider(NGraphNodeView popupComponentProvider) {
		this.popupComponentProvider = popupComponentProvider;
	}

	public Color getHiliteColor() {
		return hiliteColor;
	}

	public void setHiliteColor(Color hiliteColor) {
		this.hiliteColor = hiliteColor;
	}

	public List<DataPoint> getpList() {
		return pList;
	}

	public void setpList(List<DataPoint> pList) {
		this.pList = pList;
	}

	private class Edge {

		private DataPoint[] vertices;
		private List<DataPoint> scalPoints;
		private PlotScale scal;

		Edge() {
			vertices = new DataPoint[2];
			scalPoints = new ArrayList<DataPoint>();
		}

		public void addScalePoints(DataPoint p1, DataPoint p2, PlotScale s1) {
			vertices[0] = p1;
			vertices[1] = p2;
			scal = s1;
			if (s1.getTick() > 0.0 && s1.getHighLimit() > s1.getLowLimit()) {
				for (double i = s1.getFirstTick(); i < s1.getHighLimit(); i += s1.getTick()) {
					if (s1.getType() == PlotScale.X_AXIS) {
						scalPoints.add(new DataPoint(i, p1.getRawY(), p1
								.getRawZ()));

					} else if (s1.getType() == PlotScale.Y_AXIS) {
						scalPoints.add(new DataPoint(p1.getRawX(), i, p1
								.getRawZ()));

					} else {
						scalPoints.add(new DataPoint(p1.getRawX(),
								p1.getRawY(), i));

					}

				}
			}

		}

		public String getVal(int i) {
			String ss = "";
			if (scal.getType() == PlotScale.X_AXIS) {
				ss = scal.getNf().format(scalPoints.get(i).getRawX());
			} else if (scal.getType() == PlotScale.Y_AXIS) {
				ss = scal.getNf().format(scalPoints.get(i).getRawY());
			} else {
				ss = scal.getNf().format(scalPoints.get(i).getRawZ());
			}
			return ss;
		}
	}

	private class AxisFace {

		private DataPoint[] vertices;
		private Edge[] edges;
		private boolean bf;

		AxisFace() {
			vertices = new DataPoint[4];
			edges = new Edge[4];
		}

		public void addVertices(DataPoint p1, DataPoint p2, DataPoint p3,
				DataPoint p4) {
			vertices[0] = p1;
			vertices[1] = p2;
			vertices[2] = p3;
			vertices[3] = p4;
		}

		public void addEdges(Edge e1, Edge e2, Edge e3, Edge e4) {
			edges[0] = e1;
			edges[1] = e2;
			edges[2] = e3;
			edges[3] = e4;
		}

		public double getMidZ() {
			return (vertices[0].getTrZ() + vertices[1].getTrZ()
					+ vertices[2].getTrZ() + vertices[3].getTrZ()) / 4.0;
		}

		public void setBackFacing() {
			bf = backFacing();
		}

		public double normalXVal() {
			double[] v1 = new double[3];
			double[] v2 = new double[3];

			v1[0] = vertices[1].getTrX() - vertices[0].getTrX();
			v1[1] = vertices[1].getTrY() - vertices[0].getTrY();
			v1[2] = vertices[1].getTrZ() - vertices[0].getTrZ();

			v2[0] = vertices[2].getTrX() - vertices[0].getTrX();
			v2[1] = vertices[2].getTrY() - vertices[0].getTrY();
			v2[2] = vertices[2].getTrZ() - vertices[0].getTrZ();

			return (v1[1] * v2[2]) - (v1[2] * v2[1]);
		}

		public boolean normalYVal() {
			double[] v1 = new double[3];
			double[] v2 = new double[3];

			v1[0] = vertices[1].getTrX() - vertices[0].getTrX();
			v1[1] = vertices[1].getTrY() - vertices[0].getTrY();
			v1[2] = vertices[1].getTrZ() - vertices[0].getTrZ();

			v2[0] = vertices[2].getTrX() - vertices[0].getTrX();
			v2[1] = vertices[2].getTrY() - vertices[0].getTrY();
			v2[2] = vertices[2].getTrZ() - vertices[0].getTrZ();

			double y = (v1[2] * v2[0]) - (v1[0] * v2[2]);
			if (y >= 0.0) {
				return true;
			}
			return false;
		}

		public boolean backFacing() {
			// calculate vector perpendicular to face
			// first create vectors for two sides
			double[] normal = new double[3];
			double[] v1 = new double[3];
			double[] v2 = new double[3];

			v1[0] = vertices[1].getTrX() - vertices[0].getTrX();
			v1[1] = vertices[1].getTrY() - vertices[0].getTrY();
			v1[2] = vertices[1].getTrZ() - vertices[0].getTrZ();

			v2[0] = vertices[2].getTrX() - vertices[0].getTrX();
			v2[1] = vertices[2].getTrY() - vertices[0].getTrY();
			v2[2] = vertices[2].getTrZ() - vertices[0].getTrZ();

			normal[0] = (v1[1] * v2[2]) - (v1[2] * v2[1]);
			normal[1] = (v1[2] * v2[0]) - (v1[0] * v2[2]);
			normal[2] = (v1[0] * v2[1]) - (v1[1] * v2[0]);

			// calc angle between this and point of view
			// get point of view to face vector
			double[] v3 = new double[3];
			v3[0] = vertices[0].getTrX();
			v3[1] = vertices[0].getTrY();
			v3[2] = vertices[0].getTrZ();

			// find scalar product
			double sp = 0.0;
			for (int i = 0; i < 3; i++) {
				sp += v3[i] * normal[i];
			}
			// get product of magnitudes
			double mp = Math
					.sqrt(v3[0] * v3[0] + v3[1] * v3[1] + v3[2] * v3[2])
					* Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1]
							+ normal[2] * normal[2]);
			// get angle
			double angle = Math.acos(sp / mp);
			
			return angle < Math.PI / 2.0;
		}

		public void transformFace(TransformMatrix currTr, int width, int height) {
			for (int i = 0; i < 3; i++) {
				vertices[i].projectPoint(currTr, width, height);
			}
		}

		public boolean isEdgeOnTopSide(Edge ee) {
			float y1 = ee.vertices[0].getScreenY();
			float x1 = ee.vertices[0].getScreenX();
			Edge e = oppositeEdge(ee);
			float y2 = (x1 - e.vertices[0].getScreenX())
					/ (e.vertices[1].getScreenX() - e.vertices[0].getScreenX())
					* (e.vertices[1].getScreenY() - e.vertices[0].getScreenY())
					+ e.vertices[0].getScreenY();
			if (y2 < y1) {
				return true;
			}
			return false;
		}

		public boolean isEdgeOnRightSide(Edge ee) {
			float y1 = ee.vertices[0].getScreenY();
			float x1 = ee.vertices[0].getScreenX();
			Edge e = oppositeEdge(ee);
			float x2 = (y1 - e.vertices[0].getScreenY())
					/ (e.vertices[1].getScreenY() - e.vertices[0].getScreenY())
					* (e.vertices[1].getScreenX() - e.vertices[0].getScreenX())
					+ e.vertices[0].getScreenX();
			if (x2 > x1) {
				return true;
			}
			return false;
		}

		public Edge oppositeEdge(Edge e) {
			for (int i = 0; i < 4; i++) {
				if (edges[i] == e) {
					return edges[(i + 2) % 4];
				}
			}
			return null;
		}

		public void plotFace(Graphics2D g) {
			GeneralPath p = new GeneralPath();
			p.moveTo(vertices[0].getScreenX(), vertices[0].getScreenY());
			p.lineTo(vertices[1].getScreenX(), vertices[1].getScreenY());
			p.lineTo(vertices[2].getScreenX(), vertices[2].getScreenY());
			p.lineTo(vertices[3].getScreenX(), vertices[3].getScreenY());
			p.lineTo(vertices[0].getScreenX(), vertices[0].getScreenY());

			g.setColor(Color.gray);
			if (bf) {

				GeneralPath p2 = new GeneralPath();
				g.setColor(Color.lightGray);
				g.fill(p);

				g.setColor(new Color(0.7f, 0.7f, 0.7f));

				for (int j = 0; j < 4; j++) {
					for (int i = 0; i < edges[j].scalPoints.size(); i++) {
						p2.moveTo(edges[j].scalPoints.get(i).getScreenX(),
								edges[j].scalPoints.get(i).getScreenY());
						p2.lineTo(edges[(j + 2) % 4].scalPoints.get(i).getScreenX(),
								edges[(j + 2) % 4].scalPoints.get(i).getScreenY());
					}
				}

				g.draw(p2);

				g.setColor(Color.gray);
				g.draw(p);
				g.setColor(Color.black);

			} else {
				g.setColor(Color.black);
				g.draw(p);

			}

		}
	}
}
