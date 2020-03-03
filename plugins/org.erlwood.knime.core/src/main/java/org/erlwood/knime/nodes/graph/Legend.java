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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.knime.core.node.NodeLogger;


@SuppressWarnings("serial")
public final class Legend extends JPanel implements MouseListener {
	private static final NodeLogger LOG = NodeLogger.getLogger(Legend.class);
	
	public static final int SUPPLIED = 0;
	public static final int CONTINUOUS = 1;
	public static final int CATEGORICAL = 2;
	public static final int CATEGORY_HEIGHT = 10;
	public static final int CONTINUOUS_HEIGHT = 60;
	public static final int EXTERNAL_COLOR_MODEL = 0;
	public static final int INTERNAL_COLOR_MODEL = 1;

	private NDGraph graph;

	private int colorModelType;
	private int colorType;
	private int sizeType;
	private int minSize;
	private int maxSize;

	private List<LegendItem> legendItems;
	private List<DataPoint> dataPoints;
	private NumberFormat nf1;

	public Legend(NDGraph g, List<DataPoint> dp) {
		setMinSize(1);
		setMaxSize(12);
		colorType = SUPPLIED;
		setSizeType(SUPPLIED);
		colorModelType = EXTERNAL_COLOR_MODEL;
		graph = g;
		setDataPoints(dp);
		legendItems = new ArrayList<LegendItem>();
		nf1 = NumberFormat.getInstance();
		nf1.setMinimumFractionDigits(4);
		nf1.setMaximumFractionDigits(4);
		setSize(100, 250);
		setPreferredSize(new Dimension(150, 250));
		addMouseListener(this);
	}

	public Legend() {
		setMinSize(1);
		setMaxSize(12);
		colorType = SUPPLIED;
		setSizeType(SUPPLIED);
		colorModelType = EXTERNAL_COLOR_MODEL;
		legendItems = new ArrayList<LegendItem>();
		nf1 = NumberFormat.getInstance();
		nf1.setMinimumFractionDigits(4);
		nf1.setMaximumFractionDigits(4);
		setSize(100, 250);
		setPreferredSize(new Dimension(150, 250));
		addMouseListener(this);
	}

	public void setDataList(List<DataPoint> dp) {
		setDataPoints(dp);
	}

	public void setPanel(NDGraph g) {
		graph = g;
	}

	public void generateLegend() {
		legendItems.clear();
		int yPos = 30;
		int maxWidth = 150;
		Font f = UIManager.getDefaults().getFont("TabbedPane.font");
		FontMetrics metrics = this.getFontMetrics(f);
		LOG.debug(f.toString());

		// first do colors
		// if continuous, get max and - create Legend Item for each
		if (colorType == CONTINUOUS) {
			DataPoint maxDp = null;
			DataPoint minDp = null;
			double max = Double.NEGATIVE_INFINITY;
			double min = Double.MAX_VALUE;
			for (DataPoint d : getDataPoints()) {
				if (d.getColorValue() > max) {
					max = d.getColorValue();
					maxDp = d;
				}
				if (d.getColorValue() < min) {
					min = d.getColorValue();
					minDp = d;
				}
			}
			try {
				LegendItem maxLi = new LegendItem(LegendItem.COLORTYPE);
				maxLi.maxColor = maxDp.getColor();
				maxLi.maxLabel = nf1.format(maxDp.getColorValue());
				if (maxLi.maxLabel != null) {
					if (metrics.stringWidth(maxLi.maxLabel) > maxWidth) {
						maxWidth = metrics.stringWidth(maxLi.maxLabel);
					}
				}
				maxLi.yPos = yPos;
				maxLi.minColor = minDp.getColor();
				maxLi.minLabel = nf1.format(minDp.getColorValue());
				if (maxLi.maxLabel != null) {
					if (metrics.stringWidth(maxLi.minLabel) > maxWidth) {
						maxWidth = metrics.stringWidth(maxLi.minLabel);
					}
				}
				legendItems.add(maxLi);
				yPos += CONTINUOUS_HEIGHT + 10;
			} catch (Exception e) {
				LOG.debug("no max and min to set colors!!");
				LOG.error(e.getMessage(), e);
			}
		}
		// if categorical get all values - create Legend Item for each
		if (colorType == CATEGORICAL || colorType == SUPPLIED) {
			List<Color> colorVals = new ArrayList<Color>();
			for (DataPoint d : getDataPoints()) {
				if (!colorVals.contains(d.getColor())) {
					LegendItem legI = new LegendItem(LegendItem.COLORTYPE);
					legI.color = d.getColor();
					legI.label = d.getColorLabel();
					if (legI.label != null) {
						if (metrics.stringWidth(legI.label) > maxWidth) {
							maxWidth = metrics.stringWidth(legI.label);
						}
					}
					legI.yPos = yPos;
					legendItems.add(legI);
					colorVals.add(d.getColor());
					yPos += CATEGORY_HEIGHT + 10;
				}
			}
		}
		yPos += 35;
		// now do sizes
		// if continuous, get max and - create Legend Item for each
		if (getSizeType() == CONTINUOUS) {
			DataPoint maxDp = null;
			DataPoint minDp = null;
			double max = Double.NEGATIVE_INFINITY;
			double min = Double.MAX_VALUE;
			for (DataPoint d : getDataPoints()) {
				if (d.getSizeValue() > max) {
					max = d.getSizeValue();
					maxDp = d;
				}
				if (d.getSizeValue() < min) {
					min = d.getSizeValue();
					minDp = d;
				}
			}
			try {
				LegendItem maxLi = new LegendItem(LegendItem.SIZETYPE);
				maxLi.maxSize = maxDp.getSize();
				maxLi.maxLabel = nf1.format(maxDp.getSizeValue());
				maxLi.minSize = minDp.getSize();
				maxLi.minLabel = nf1.format(minDp.getSizeValue());
				maxLi.yPos = yPos;
				legendItems.add(maxLi);
				yPos += CONTINUOUS_HEIGHT + 10;
			} catch (Exception e) {
				LOG.debug("no max and min to set colors!!");
				LOG.error(e.getMessage(), e);
			}
		}
		// if categorical get all values - create Legend Item for each
		if (getSizeType() == CATEGORICAL || getSizeType() == SUPPLIED) {
			List<Integer> sizeVals = new ArrayList<Integer>();
			for (DataPoint d : getDataPoints()) {
				if (!sizeVals.contains(d.getSize())) {
					LegendItem legI = new LegendItem(LegendItem.SIZETYPE);
					legI.size = d.getSize();
					legI.label = d.getSizeLabel();
					legI.yPos = yPos;
					legendItems.add(legI);
					sizeVals.add(d.getSize());
					yPos += CATEGORY_HEIGHT + 10;
					
				}
			}
		}
		int maxHeight = yPos + 20;
		setSize(maxWidth, maxHeight);
		setPreferredSize(new Dimension(maxWidth, maxHeight));
	}

	public void useExternalColorModel() {
		colorModelType = EXTERNAL_COLOR_MODEL;
	}

	public void calcColorValues() {
		if (colorType == CONTINUOUS) {
			// get max and min
			colorModelType = INTERNAL_COLOR_MODEL;
			double max = Double.NEGATIVE_INFINITY;
			double min = Double.MAX_VALUE;
			for (DataPoint d : getDataPoints()) {
				if (d.getColorValue() > max) {
					max = d.getColorValue();
				}
				if (d.getColorValue() < min) {
					min = d.getColorValue();
				}
			}
			for (DataPoint d : getDataPoints()) {
				float h = 0.8f * (float) ((d.getColorValue() - min) / (max - min));
				float s = 0.8f;
				float b = 0.8f;
				d.setColor(Color.getHSBColor(h, s, b));
			}
		} else if (colorType == CATEGORICAL) {
			// get number of categories
			List<String> colVals = new ArrayList<String>();
			for (DataPoint d : getDataPoints()) {
				if (!colVals.contains(d.getColorLabel())) {
					colVals.add(d.getColorLabel());
				}
			}
			for (DataPoint d : getDataPoints()) {
				float h = 0.8f * ((float) colVals.indexOf(d.getColorLabel()) / (float) (colVals
						.size() - 1));
				float s = 0.8f;
				float b = 0.8f;
				d.setColor(Color.getHSBColor(h, s, b));
			}
		}
	}

	public Color[] getColorScaleArray(LegendItem lI) {
		Color[] cArray = new Color[CONTINUOUS_HEIGHT];
		if (colorModelType == EXTERNAL_COLOR_MODEL) {
			float[] minVals = new float[3];
			float[] maxVals = new float[3];
			float[] outVals = new float[3];
			minVals = lI.minColor.getColorComponents(minVals);
			maxVals = lI.maxColor.getColorComponents(maxVals);
			for (int i = 0; i < cArray.length; i++) {
				for (int j = 0; j < 3; j++) {
					outVals[j] = minVals[j] + (maxVals[j] - minVals[j])
							* (float) i / ((float) CONTINUOUS_HEIGHT - 1);
				}
				cArray[i] = new Color(outVals[0], outVals[1], outVals[2]);
			}
		} else {
			for (int i = 0; i < cArray.length; i++) {
				float h = 0.8f * ((float) i / ((float) CONTINUOUS_HEIGHT - 1));
				float s = 0.8f;
				float b = 0.8f;
				cArray[i] = Color.getHSBColor(h, s, b);
			}
		}
		return cArray;
	}

	public void calcSizeValues() {
		if (getSizeType() == CONTINUOUS) {
			// get max and min
			double max = Double.NEGATIVE_INFINITY;
			double min = Double.MAX_VALUE;
			for (DataPoint d : getDataPoints()) {
				if (d.getSizeValue() > max) {
					max = d.getSizeValue();
				}
				if (d.getSizeValue() < min) {
					min = d.getSizeValue();
				}
			}
			for (DataPoint d : getDataPoints()) {
				int s = getMinSize()
						+ (int) ((getMaxSize() - getMinSize()) * (d.getSizeValue() - min) / (max - min));
				d.setPointSize(s);
			}
		} else if (getSizeType() == CATEGORICAL) {
			// get number of categories
			List<String> sizeVals = new ArrayList<String>();
			for (DataPoint d : getDataPoints()) {
				if (!sizeVals.contains(d.getSizeLabel())) {
					sizeVals.add(d.getSizeLabel());
				}
			}
			for (DataPoint d : getDataPoints()) {
				int s = getMinSize()
						+ (int) ((getMaxSize() - getMinSize())
								* sizeVals.indexOf(d.getSizeLabel()) / (sizeVals
								.size() - 1));
				d.setPointSize(s);
			}
		}
	}

	public int getNumColorItems() {
		int count = 0;
		for (LegendItem lItem : legendItems) {
			if (lItem.propertyType == LegendItem.COLORTYPE) {
				count++;
			}
		}
		return count;
	}

	public int getNumSizeItems() {
		int count = 0;
		for (LegendItem lItem : legendItems) {
			if (lItem.propertyType == LegendItem.SIZETYPE) {
				count++;
			}
		}
		return count;
	}

	public void paintComponent(Graphics ng) {
		// call super method, cast as graphics 2d and clear background
		super.paintComponent(ng);
		Insets inset = getInsets();
		Dimension d = this.getSize();
		Graphics2D g = (Graphics2D) ng;
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHints(rh);
		g.setColor(Color.white);
		g.fillRect(inset.left, inset.top, d.width - inset.left - inset.right,
				d.height - inset.top - inset.bottom);

		AffineTransform at = g.getTransform();
	
		int currType = -1;
		for (LegendItem item : legendItems) {
			if (currType != item.propertyType) {
				g.setColor(Color.black);
				if (item.propertyType == LegendItem.COLORTYPE) {
					g.drawString("Colours", 5, item.yPos - 14);
				} else {
					g.drawString("Sizes", 5, item.yPos - 14);
				}
				currType = item.propertyType;
			}
			int size;
			if (item.propertyType == LegendItem.COLORTYPE) {
				size = 8;
				g.setColor(item.color);
			} else {
				size = item.size;
				g.setColor(Color.BLACK);
			}

			if ((item.propertyType == LegendItem.COLORTYPE && colorType != Legend.CONTINUOUS)
					|| (item.propertyType == LegendItem.SIZETYPE && getSizeType() != Legend.CONTINUOUS)) {
				g.fillOval(30 - size / 2, item.yPos - size / 2, size, size);
				g.setColor(Color.BLACK);
				g.drawString(item.label, 44, item.yPos + 5);
				g.drawRect(8, item.yPos - 5, 10, 10);
				if (item.visible || item.mixedVisibility) {
					if (item.mixedVisibility) {
						g.setColor(Color.LIGHT_GRAY);
					}
					g.drawLine(8, item.yPos - 5, 18, item.yPos + 5);
					g.drawLine(18, item.yPos - 5, 8, item.yPos + 5);
				}
			} else if (item.propertyType == LegendItem.COLORTYPE
					&& colorType == Legend.CONTINUOUS) {
				Color[] cs = getColorScaleArray(item);
				g.setColor(Color.black);
				g.drawString(item.maxLabel, 44, item.yPos + 10);
				g.drawString(item.minLabel, 44, item.yPos + CONTINUOUS_HEIGHT);
				for (int j = 0; j < cs.length; j++) {
					g.setColor(cs[j]);
					g.fillRect(26, item.yPos + (CONTINUOUS_HEIGHT - 1 - j), 10,
							1);
				}
				g.setColor(Color.black);
				g.drawLine(13, item.yPos + 1, 13, item.yPos + CONTINUOUS_HEIGHT
						- 1);
				g.drawLine(10, item.yPos + 5, 13, item.yPos + 1);
				g.drawLine(16, item.yPos + 5, 13, item.yPos + 1);
				g.drawLine(10, item.yPos + CONTINUOUS_HEIGHT - 5, 13, item.yPos
						+ CONTINUOUS_HEIGHT - 1);
				g.drawLine(16, item.yPos + CONTINUOUS_HEIGHT - 5, 13, item.yPos
						+ CONTINUOUS_HEIGHT - 1);
			} else if (item.propertyType == LegendItem.SIZETYPE
					&& getSizeType() == Legend.CONTINUOUS) {
				g.setColor(Color.black);
				g.drawString(item.maxLabel, 44, item.yPos + 10);
				g.drawString(item.minLabel, 44, item.yPos + CONTINUOUS_HEIGHT);
				g.fillOval(30 - item.maxSize / 2, item.yPos, item.maxSize,
						item.maxSize);
				g.fillOval(30 - item.minSize / 2, item.yPos + CONTINUOUS_HEIGHT
						- item.minSize, item.minSize, item.minSize);
				g.drawLine(13, item.yPos + 1, 13, item.yPos + CONTINUOUS_HEIGHT
						- 1);
				g.drawLine(10, item.yPos + 5, 13, item.yPos + 1);
				g.drawLine(16, item.yPos + 5, 13, item.yPos + 1);
				g.drawLine(10, item.yPos + CONTINUOUS_HEIGHT - 5, 13, item.yPos
						+ CONTINUOUS_HEIGHT - 1);
				g.drawLine(16, item.yPos + CONTINUOUS_HEIGHT - 5, 13, item.yPos
						+ CONTINUOUS_HEIGHT - 1);

			}
		}
		g.setTransform(at);
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {
		if (e.getX() > 8 && e.getX() < 18) {
			LegendItem lItem = null;
			for (LegendItem i : legendItems) {
				if (e.getY() > i.yPos - 5 && e.getY() < i.yPos + 5) {
					lItem = i;
					break;
				}
			}
			if (lItem != null) {
				lItem.visible = !lItem.visible;
				lItem.mixedVisibility = false;
				if (getSizeType() != CONTINUOUS) {
					for (DataPoint p : getDataPoints()) {
						if (lItem.propertyType == LegendItem.SIZETYPE) {
							if (p.getSize() == lItem.size) {
								p.setVisible(lItem.visible);
							}
						}
					}
				}
				if (colorType != CONTINUOUS) {
					for (DataPoint p : getDataPoints()) {
						if (lItem.propertyType == LegendItem.COLORTYPE) {
							if (p.getColor().equals(lItem.color)) {
								p.setVisible(lItem.visible);
							}
						}
					}
				}
				// set visibility to mixed for other legend if this change leads
				// to a mix for them
				for (LegendItem ll : legendItems) {
					if (ll != lItem) {
						boolean firstVis = false;
						boolean first = true;
						for (DataPoint dp : getDataPoints()) {
							if ((ll.propertyType == LegendItem.COLORTYPE && ll.color == dp.getColor())
									|| (ll.propertyType == LegendItem.SIZETYPE && ll.size == dp.getSize())) {
								if (first) {
									firstVis = dp.isVisible();
									first = false;
								} else {
									if (dp.isVisible() != firstVis) {
										ll.mixedVisibility = true;
										break;
									}
								}
								ll.visible = dp.isVisible();
							}
							ll.mixedVisibility = false;

						}
					}
				}
				repaint();
				graph.rePlotGraph();
			}

		}
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	private class LegendItem {

		private boolean visible;
		private boolean mixedVisibility;
		private String label, minLabel, maxLabel;
		private Color color, minColor, maxColor;
		private int size, minSize, maxSize;
		private int propertyType;

		private int yPos;
		private static final int COLORTYPE = 0;
		private static final int SIZETYPE = 1;

		public LegendItem(int t) {
			propertyType = t;
			visible = true;
			mixedVisibility = false;
			label = "";
			color = DataPoint.DEFAULT_COLOR;
			size = DataPoint.getDefsize();
			yPos = 0;
		}

		public void drawLegenditem(Graphics2D g) {

		}
	}

	private class PlotLabel extends JPanel {

		private int size;
		private Color color;

		PlotLabel() {
			setSize(18, 18);
			setPreferredSize(new Dimension(18, 18));
			setMaximumSize(new Dimension(18, 18));
			setMinimumSize(new Dimension(18, 18));
		}

		public void paintComponent(Graphics ng) {
			// call super method, cast as graphics 2d and clear background
			super.paintComponent(ng);
			Graphics2D g = (Graphics2D) ng;
			RenderingHints rh = new RenderingHints(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHints(rh);
			g.setColor(color);
			g.fillOval(getSize().width / 2 - size / 2, getSize().height / 2
					- size / 2, size, size);
		}
	}

	public void setColorType(int colorType) {
		this.colorType = colorType;
	}

	public int getSizeType() {
		return sizeType;
	}

	public void setSizeType(int sizeType) {
		this.sizeType = sizeType;
	}

	public List<DataPoint> getDataPoints() {
		return dataPoints;
	}

	public void setDataPoints(List<DataPoint> dataPoints) {
		this.dataPoints = dataPoints;
	}

	public int getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
}
