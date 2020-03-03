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

import org.knime.core.node.NodeLogger;


public final class DataPoint {
	private static final NodeLogger LOG = NodeLogger.getLogger(DataPoint.class);
	private TransformMatrix dPoint, trDPoint;
	private float screenX, screenY;
	private Color color, selColor;
	public static final Color DEFAULT_COLOR = Color.ORANGE;
	private int size;
	private static final int DEF_SIZE = 4;
	private double sizeValue, colorValue;
	private String sizeLabel, colorLabel, id;
	private boolean selected, hilited;
	private boolean visible, inZoomRegion;

	public DataPoint(double x, double y, double z) {
		dPoint = getPoint(x, y, z);
		trDPoint = new TransformMatrix(4, 1);
		setSize(getDefsize());
		setColor(DEFAULT_COLOR);
		setSelected(false);
		setHilited(false);
		setVisible(true);
		setInZoomRegion(true);
		setSizeValue(0.0);
		setColorValue(0.0);
		setSizeLabel("all sizes");
		setColorLabel("all colors");
		setID("");
	}

	public DataPoint(double x, double y) {
		dPoint = getPoint(x, y, 1.0);
		trDPoint = new TransformMatrix(4, 1);
		setSize(4);
		setColor(Color.orange);
		setSelected(false);
		setHilited(false);
		setVisible(true);
		setInZoomRegion(true);
		setSizeValue(0.0);
		setColorValue(0.0);
		setSizeLabel("size");
		setColorLabel("color");
		setID("");
	}

	public static TransformMatrix getPoint(double x, double y, double z) {
		TransformMatrix m = new TransformMatrix(4, 1);

		m.getData()[0][0] = x;
		m.getData()[1][0] = y;
		m.getData()[2][0] = z;
		m.getData()[3][0] = 1.0;

		return m;
	}

	public void setPointSize(int s) {
		setSize(s);
		if (getSize() < 2) {
			setSize(2);
		}
		if (getSize() > 12) {
			setSize(12);
		}
	}

	public void setColor(Color c) {
		color = c;
		
		int r = c.getRed() / 2;
		int g = c.getGreen() / 2;
		int b = c.getBlue() / 2;
		setSelColor(new Color(r, g, b));
	}

	public double getRawX() {
		return dPoint.getData()[0][0];
	}

	public double getRawY() {
		return dPoint.getData()[1][0];
	}

	public double getRawZ() {
		return dPoint.getData()[2][0];
	}

	public void setRawX(double d) {
		dPoint.getData()[0][0] = d;
	}

	public void setRawY(double d) {
		dPoint.getData()[1][0] = d;
	}

	public void setRawZ(double d) {
		dPoint.getData()[2][0] = d;
	}

	public double getTrX() {
		return trDPoint.getData()[0][0];
	}

	public double getTrY() {
		return trDPoint.getData()[1][0];
	}

	public double getTrZ() {
		return trDPoint.getData()[2][0];
	}

	public double getTrW() {
		return trDPoint.getData()[3][0];
	}

	public void projectPoint(TransformMatrix currTr, int width, int height) {
		try {
			TransformMatrix.multiply(currTr, dPoint, trDPoint);
			setScreenX((float) ((getTrX() / getTrW() + 1.0) * width / 2));
			setScreenY((float) ((1.0 - getTrY() / getTrW()) * height / 2));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public double getColorValue() {
		return colorValue;
	}

	public void setColorValue(double colorValue) {
		this.colorValue = colorValue;
	}

	public Color getColor() {
		return color;
	}

	public String getColorLabel() {
		return colorLabel;
	}

	public void setColorLabel(String colorLabel) {
		this.colorLabel = colorLabel;
	}

	public double getSizeValue() {
		return sizeValue;
	}

	public void setSizeValue(double sizeValue) {
		this.sizeValue = sizeValue;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getSizeLabel() {
		return sizeLabel;
	}

	public void setSizeLabel(String sizeLabel) {
		this.sizeLabel = sizeLabel;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public static int getDefsize() {
		return DEF_SIZE;
	}

	public float getScreenY() {
		return screenY;
	}

	public void setScreenY(float screenY) {
		this.screenY = screenY;
	}

	public float getScreenX() {
		return screenX;
	}

	public void setScreenX(float screenX) {
		this.screenX = screenX;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isInZoomRegion() {
		return inZoomRegion;
	}

	public void setInZoomRegion(boolean inZoomRegion) {
		this.inZoomRegion = inZoomRegion;
	}

	public String getID() {
		return id;
	}

	public void setID(String iD) {
		id = iD;
	}

	public Color getSelColor() {
		return selColor;
	}

	public void setSelColor(Color selColor) {
		this.selColor = selColor;
	}

	public boolean isHilited() {
		return hilited;
	}

	public void setHilited(boolean hilited) {
		this.hilited = hilited;
	}

}
