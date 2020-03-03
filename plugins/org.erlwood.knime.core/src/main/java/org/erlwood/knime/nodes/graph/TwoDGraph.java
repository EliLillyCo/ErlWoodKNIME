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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import org.knime.core.node.NodeLogger;


@SuppressWarnings("serial")
public class TwoDGraph extends JPanel implements MouseListener,
		MouseMotionListener, NDGraph {
	private static final NodeLogger LOG = NodeLogger.getLogger(TwoDGraph.class);
	
	public static final int INCLUDE_ORIGIN = 1;
	public static final int FORCE_ORIGIN = 2;
	public static final int EXCLUDE_ORIGIN = 0;

	private static final int AXISOFF = 32, TITLEOFFX = 30, TITLEOFFY = -23,
			TICKOFFX = 8, TICKOFFY = -8, LABOFFX = 20, LABOFFY = -10;

	private int startx, starty, nowx, nowy;
	private boolean drawBox, plotGrid = true;
	private double maxX, maxY, minX, minY, currMinX, currMaxX, currMinY,
			currMaxY;
	private double slope;
	private double intercept;
	private double r2;
	private PopupComponentProvider popupComponentProvider;
	private Popup popup;

	private PlotScale xScal, yScal;
	private List<DataPoint> pList;
	private NumberFormat nf1;
	private int fitType;
	private boolean originIsMin, showFitLine;

	private Color hiliteColor;
	private TransformMatrix tr1, currTr;

	public static void main(String[] args) {
	}

	public TwoDGraph() {
		super();
		originIsMin = false;
		drawBox = false;
		pList = new ArrayList<DataPoint>();
		addMouseMotionListener(this);
		addMouseListener(this);
		setSize(240, 240);
		setPreferredSize(new Dimension(240, 240));
		showFitLine = true;
		xScal = new PlotScale(0.0, 1.0, "x axis", PlotScale.X_AXIS);
		yScal = new PlotScale(0.0, 1.0, "y axis", PlotScale.Y_AXIS);
		nf1 = NumberFormat.getInstance();
		nf1.setMinimumFractionDigits(4);
		nf1.setMaximumFractionDigits(4);
		hiliteColor = Color.yellow;
	}

	public DataPoint addPoint(double x, double y) {
		DataPoint pp = new DataPoint(x, y);
		pList.add(pp);
		return pp;
	}

	public void addPoint(DataPoint d) {
		pList.add(d);
	}

	public void clearPoints() {
		pList.clear();
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
		// do nothing for 2D plot
	}

	public String getXAxisTitle() {
		return xScal.getLabel();
	}

	public String getYAxisTitle() {
		return yScal.getLabel();
	}

	public String getZAxisTitle() {
		return "";
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
			nowx = e.getX();
			nowy = e.getY();
			startx = e.getX();
			starty = e.getY();
			drawBox = true;
		}
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		drawBox = false;
		if (e.isPopupTrigger()) {
			makeAndShowPopup(e);
		} else {
			selectPointsInBox(e.isShiftDown());
		}
		repaint();
	}

	public void makeAndShowPopup(MouseEvent e) {
		if (popupComponentProvider != null) {
			PopupFactory factory = PopupFactory.getSharedInstance();
			DataPoint dp = getPointClosestToClick(e.getX() - AXISOFF, e.getY());
			Component c = popupComponentProvider.getComponent(dp.getID());
			if (popup != null) {
				popup.hide();
			}
			popup = factory.getPopup(this, c, (int) dp.getScreenX()
					+ getLocationOnScreen().x + AXISOFF, (int) dp.getScreenY()
					+ this.getLocationOnScreen().y);
			popup.show();
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		nowx = e.getX();
		nowy = e.getY();

		repaint();
	}

	public DataPoint getPointClosestToClick(int x, int y) {
		double minDist = Double.MAX_VALUE;
		DataPoint rp = null;
		for (int i = pList.size() - 1; i >= 0; i--) {
			DataPoint dp = pList.get(i);
			double dist = Math.sqrt((dp.getScreenX() - x) * (dp.getScreenX() - x)
					+ (dp.getScreenY() - y) * (dp.getScreenY() - y));
			if (dist < minDist) {
				minDist = dist;
				rp = dp;
			}
		}
		return rp;
	}

	public void selectPointsInBox(boolean add) {
		int bx1, bx2, by1, by2;
		Insets inset = getInsets();
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
			for (DataPoint p : pList) {
				p.setSelected(false);
			}
		}
		for (DataPoint p : pList) {
			if (p.getScreenX() + AXISOFF + inset.left + p.getSize() / 2 > bx1
					&& p.getScreenX() + AXISOFF + inset.left - p.getSize() / 2 < bx2
					&& p.getScreenY() + p.getSize() / 2 > by1
					&& p.getScreenY() - p.getSize() / 2 < by2) {
				p.setSelected(true);
			}
		}
	}

	public void zoomToSelection() {
		// find max and min of points in selection
		List<DataPoint> selPList = new ArrayList<DataPoint>();
		for (DataPoint p : pList) {
			if (p.isSelected()) {
				p.setInZoomRegion(true);
				selPList.add(p);
			} else {
				p.setInZoomRegion(false);
			}
		}
		if (selPList.size() > 1) {
			currMinX = Double.MAX_VALUE;
			currMinY = Double.MAX_VALUE;
			currMaxX = Double.NEGATIVE_INFINITY;
			currMaxY = Double.NEGATIVE_INFINITY;
			for (DataPoint dp : selPList) {
				if (dp.getRawX() > currMaxX) {
					currMaxX = dp.getRawX();
				}
				if (dp.getRawY() > currMaxY) {
					currMaxY = dp.getRawY();
				}
				if (dp.getRawX() < currMinX) {
					currMinX = dp.getRawX();
				}
				if (dp.getRawY() < currMinY) {
					currMinY = dp.getRawY();
				}
			}
		}

		// set scale limits to this
		scaleToCurrentLimits();
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
		return 1000;
	}

	public void setCurrMaxZ(double currMaxY) {
		// do nothing for 2D;
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
		return 0;
	}

	public void setCurrMinZ(double currMinY) {
		// do nothing for 2D
	}

	public double getMaxX() {
		return maxX;
	}

	public double getMaxY() {
		return maxY;
	}

	public double getMaxZ() {
		return 1000;
	}

	public double getMinX() {
		return minX;
	}

	public double getMinY() {
		return minY;
	}

	public double getMinZ() {
		return 0;
	}

	public void scaleToCurrentLimits() {
		// check data points in limits
		for (DataPoint p : pList) {
			if (p.getRawX() >= currMinX && p.getRawX() <= currMaxX
					&& p.getRawY() >= currMinY && p.getRawY() <= currMaxY) {
				p.setInZoomRegion(true);
			} else {
				p.setInZoomRegion(false);
			}
		}
		try {
			tr1 = TransformMatrix.multiply(TransformMatrix.getScale(
					2.0 / (currMaxX - currMinX), 2.0 / (currMaxY - currMinY),
					1.0), TransformMatrix.getTranslation(
					-(currMinX + currMaxX) / 2.0, -(currMinY + currMaxY) / 2.0,
					0.0));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		setCurrMatrix();
		xScal.updateScale(currMinX, currMaxX);
		yScal.updateScale(currMinY, currMaxY);
		// repaint view
		repaint();
	}

	public void resetZoom() {
		for (DataPoint p : pList) {
			p.setInZoomRegion(true);
		}
		doSetup();
		repaint();
	}

	public TransformMatrix getStandardDataTransformMatrix() {
		if (pList.size() == 0) {
			minX = 0.0;
			minY = 0.0;
			maxX = 1.0;
			maxY = 1.0;
		} else if (pList.size() == 1) {
			minX = pList.get(0).getRawX() - 1.0;
			minY = pList.get(0).getRawY() - 1.0;
			maxX = pList.get(0).getRawX() + 1.0;
			maxY = pList.get(0).getRawY() + 1.0;
		} else {

			minX = Double.MAX_VALUE;
			minY = Double.MAX_VALUE;
			maxX = Double.NEGATIVE_INFINITY;
			maxY = Double.NEGATIVE_INFINITY;
			for (DataPoint dp : pList) {
				if (dp.getRawX() > maxX) {
					maxX = dp.getRawX();
				}

				if (dp.getRawY() > maxY) {
					maxY = dp.getRawY();
				}

				if (dp.getRawX() < minX) {
					minX = dp.getRawX();
				}

				if (dp.getRawY() < minY) {
					minY = dp.getRawY();
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
		}
		if (originIsMin) {
			minX = 0.0;
			minY = 0.0;
		}
		currMinX = minX;
		currMinY = minY;
		currMaxX = maxX;
		currMaxY = maxY;
		try {
			return TransformMatrix.multiply(TransformMatrix.getScale(
					2.0 / (maxX - minX), 2.0 / (maxY - minY), 1.0),
					TransformMatrix.getTranslation(-(minX + maxX) / 2.0,
							-(minY + maxY) / 2.0, 0.0));
		} catch (Exception e) {
			return null;
		}

	}

	public void setCurrMatrix() {
		try {
			currTr = tr1;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

	}

	public void doSetup() {
		tr1 = getStandardDataTransformMatrix();
		setCurrMatrix();
		xScal.updateScale(minX, maxX);
		yScal.updateScale(minY, maxY);
		calcFit();
	}

	public void calcFit() {
		double sumX = 0;
		double sumX2 = 0;
		double sumY2 = 0;
		double sumY = 0;
		double sumXY = 0;
		int n = pList.size();

		for (DataPoint p : pList) {
			sumX += p.getRawX();
			sumX2 += (p.getRawX() * p.getRawX());
			sumY2 += (p.getRawY() * p.getRawY());
			sumY += p.getRawY();
			sumXY += (p.getRawY() * p.getRawX());
		}

		if (fitType == FORCE_ORIGIN) {
			slope = sumXY / sumX2;
			intercept = 0.0;
			double yAv = sumY / n;
			double ssTot = 0.0;
			double ssErr = 0.0;
			for (DataPoint p : pList) {
				ssTot += (p.getRawY() - yAv) * (p.getRawY() - yAv);
				ssErr += (p.getRawY() - p.getRawX() * slope)
						* (p.getRawY() - p.getRawX() * slope);
			}
			r2 = 1.0 - ssErr / ssTot;
		} else {
			if (fitType == INCLUDE_ORIGIN) {
				n++;
			}
			slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
			intercept = (sumY - slope * sumX) / n;
			double r = (n * sumXY - sumX * sumY)
					/ (Math.sqrt((n * sumX2 - sumX * sumX)
							* (n * sumY2 - sumY * sumY)));
			r2 = r * r;
		}
	}

	public void paintComponent(Graphics ng) {
		// call super method, cast as graphics 2d and clear background
		super.paintComponent(ng);
		Dimension d = this.getSize();
		Insets inset = getInsets();
		Graphics2D g = (Graphics2D) ng;
		g.setFont(new Font("sansserif", Font.PLAIN, 10));
		RenderingHints rh = new RenderingHints(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHints(rh);
		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHints(rh);

		g.setColor(Color.white);
		g.fillRect(inset.left, inset.top, d.width - inset.left - inset.right,
				d.height - inset.top - inset.bottom);
		g.setColor(Color.black);
		g.drawRect(inset.left + AXISOFF, inset.top, d.width - inset.left
				- inset.right - AXISOFF - 1, d.height - inset.top
				- inset.bottom - AXISOFF);

		AffineTransform at = g.getTransform();

		// plot horizontal scale
		ng.setColor(Color.black);
		int width = (int) (d.width - inset.left - inset.right - AXISOFF);
		int height = (int) (d.height - inset.top - inset.bottom - AXISOFF);

		for (DataPoint pp : pList) {
			pp.projectPoint(currTr, width, height);
		}

		g.translate(inset.left + AXISOFF, d.height - AXISOFF + inset.top);
		plotScale(g, xScal, width, TICKOFFX, LABOFFX, TITLEOFFX);
		g.setTransform(at);

		// plot vertical scale
		g.translate(inset.left + AXISOFF, d.height - AXISOFF + inset.top);
		g.rotate(-Math.PI / 2);
		plotScale(g, yScal, height, TICKOFFY, LABOFFY, TITLEOFFY);
		g.setTransform(at);

		// set origin
		g.translate(AXISOFF, 0.0);

		// plot grid
		if (plotGrid) {
			GeneralPath p2 = new GeneralPath();
			g.setColor(Color.DARK_GRAY);
			Stroke oldStroke = g.getStroke();
			float dash[] = { 5.0f };
			BasicStroke stroke = new BasicStroke(0.4f, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER, 1.0f, dash, 0.0f);
			g.setStroke(stroke);
			if (yScal.getTick() > 0.0 && yScal.getHighLimit() > yScal.getLowLimit()) {
				for (double i = yScal.getFirstTick(); i < yScal.getHighLimit(); i = i
						+ yScal.getTick()) {
					int ploty = height
							- (int) ((yScal.getLowLimit() - i)
									/ (yScal.getLowLimit() - yScal.getHighLimit()) * height)
							+ inset.top;
					p2.moveTo(0, ploty);
					p2.lineTo(width, ploty);
				}
			}
			if (xScal.getTick() > 0.0 && xScal.getHighLimit() > xScal.getLowLimit()) {
				for (double j = xScal.getFirstTick(); j < xScal.getHighLimit(); j = j
						+ xScal.getTick()) {
					int plotx = (int) ((xScal.getLowLimit() - j)
							/ (xScal.getLowLimit() - xScal.getHighLimit()) * width);
					p2.moveTo(plotx, 0);
					p2.lineTo(plotx, height);
				}
			}
			g.draw(p2);
			g.setStroke(oldStroke);
			g.setColor(Color.black);
		}

		// plot points
		Shape c = g.getClip();
		g.setClip(0, 0, d.width - AXISOFF, d.height - AXISOFF);

		for (DataPoint pp : pList) {
			if (pp.isHilited() && pp.isVisible() && pp.isInZoomRegion()) {
				g.setColor(hiliteColor);
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
		g.setTransform(at);

		if (showFitLine) {
			// plot line
			DataPoint p1 = new DataPoint(minX, minX * slope + intercept);
			p1.projectPoint(currTr, width, height);
			DataPoint p2 = new DataPoint(maxX, maxX * slope + intercept);
			p2.projectPoint(currTr, width, height);
			g.setColor(Color.blue);
			g.translate(AXISOFF, 0.0);
			GeneralPath p = new GeneralPath();
			p.moveTo(p1.getScreenX(), p1.getScreenY());
			p.lineTo(p2.getScreenX(), p2.getScreenY());
			g.draw(p);
			g.setTransform(at);

			// add r2 annotation
			g.drawString("R2 = " + nf1.format(r2) + ", y= " + nf1.format(slope)
					+ "x + " + nf1.format(intercept), 35, 20);
		}
		g.setClip(c);

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

	public void plotScale(Graphics2D g, PlotScale scal, int width,
			int tickBottom, int fontBottom, int titleBottom) {
		g.drawLine(0, 0, width, 0);
		FontMetrics fm = g.getFontMetrics();
		if (scal.getTick() > 0.0 && scal.getHighLimit() > scal.getLowLimit()) {
			for (double i = scal.getFirstTick(); i < scal.getHighLimit(); i = i
					+ scal.getTick()) {
				int plotx = (int) ((scal.getLowLimit() - i)
						/ (scal.getLowLimit() - scal.getHighLimit()) * width);
				g.drawLine(plotx, 0, plotx, tickBottom);
				String label = scal.getNf().format(i);
				int halfStringWidth = fm.stringWidth(label) / 2;
				g.drawString(label, plotx - halfStringWidth, fontBottom);
			}
		}

		int halfStringWidth = fm.stringWidth(scal.getLabel()) / 2;
		g.drawString(scal.getLabel(), width / 2 - halfStringWidth, titleBottom);
	}

	public void setShowFitLine(boolean showFt) {
		this.showFitLine = showFt;
	}

	public boolean isShowFitLine() {
		return this.showFitLine;
	}

	public void setPopupComponentProvider(PopupComponentProvider popup) {
		this.popupComponentProvider = popup;
	}

	public void setHiliteColor(Color hilite) {
		this.hiliteColor = hilite;
	}

	public void setFitType(int fitType) {
		this.fitType = fitType;
	}

	public double getSlope() {
		return this.slope;
	}

	public List<DataPoint> getPList() {
		return this.pList;
	}

	public double getR2() {
		return this.r2;
	}

	public double getIntercept() {
		return this.intercept;
	}
	
	public void setOriginIsMin(final boolean originIsMin) {
		this.originIsMin = originIsMin;
	}
}
