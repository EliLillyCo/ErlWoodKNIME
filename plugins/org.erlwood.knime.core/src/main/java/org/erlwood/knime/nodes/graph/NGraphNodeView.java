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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.knime.base.node.util.DataArray;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.property.ColorAttr;
import org.knime.core.data.property.ColorHandler;
import org.knime.core.data.property.SizeHandler;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeView;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;

/**
 * <code>NodeView</code> for the "NGraph" Node.
 */
public final class NGraphNodeView extends NodeView<NGraphNodeModel> implements
		HiLiteListener, PopupComponentProvider {
	private static final NodeLogger LOG = NodeLogger
			.getLogger(NGraphNodeView.class);

	private TwoDGraph cp;
	private ThreeDGraph tdg;
	private JComboBox xChoice;
	private JComboBox yChoice;
	private JComboBox zChoice;
	private JComboBox colChoice;
	private JComboBox sizeChoice;
	private JComboBox labChoice;
	private Legend legend;
	private SliderPanel sliderPanel;
	private JPanel rPanel;
	private NGraphNodeModel dataProvider;
	private HiLiteHandler hHand;


	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel
	 *            The model (class: {@link NGraphNodeModel})
	 */

	protected NGraphNodeView(final NGraphNodeModel nodeModel) {
		super(nodeModel);
		setHiLiteHandler(getNodeModel().getInHiLiteHandler(0));
		createHiLiteMenu();
		createSelectionMenu();
		createViewMenu();
		dataProvider = (NGraphNodeModel) getNodeModel();

		cp = new TwoDGraph();
		tdg = new ThreeDGraph();

		cp.setSize(600, 600);
		cp.setPreferredSize(new Dimension(600, 600));
		cp.setPopupComponentProvider(this);
		tdg.setPreferredSize(new Dimension(600, 600));
		tdg.setSize(new Dimension(600, 600));
		tdg.setPopupComponentProvider(this);
		cp.setHiliteColor(ColorAttr.HILITE);
		tdg.setHiliteColor(ColorAttr.HILITE);

		rPanel = new JPanel(new BorderLayout());
		JPanel selPanel = new JPanel(new BorderLayout());
		JPanel selPanel1 = new JPanel();
		JPanel selPanel2 = new JPanel();
		JPanel rootPanel = new JPanel(new BorderLayout());
		xChoice = new JComboBox();
		yChoice = new JComboBox();
		zChoice = new JComboBox();
		colChoice = new JComboBox();
		sizeChoice = new JComboBox();
		labChoice = new JComboBox();

		xChoice.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				rePlotGraph(((JComboBox) e.getSource()).isPopupVisible());
			}
		});
		yChoice.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				rePlotGraph(((JComboBox) e.getSource()).isPopupVisible());
			}
		});
		zChoice.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				rePlotGraph(((JComboBox) e.getSource()).isPopupVisible());
			}
		});
		sizeChoice.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				rePlotGraph(((JComboBox) e.getSource()).isPopupVisible());
			}
		});
		colChoice.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				rePlotGraph(((JComboBox) e.getSource()).isPopupVisible());
			}
		});
		labChoice.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (zChoice.getSelectedIndex() > 0) {
					tdg.requestFocus();
				}
			}
		});

		selPanel1.add(new JLabel("x vals"));
		selPanel1.add(xChoice);
		selPanel1.add(new JLabel("y vals"));
		selPanel1.add(yChoice);
		selPanel1.add(new JLabel("z vals"));
		selPanel1.add(zChoice);
		selPanel2.add(new JLabel("color vals"));
		selPanel2.add(colChoice);
		selPanel2.add(new JLabel("size vals"));
		selPanel2.add(sizeChoice);
		selPanel2.add(new JLabel("labels"));
		selPanel2.add(labChoice);
		selPanel.add(selPanel1, BorderLayout.NORTH);
		selPanel.add(selPanel2, BorderLayout.SOUTH);

		// get in table spec and populate drop downs with cols

		setupComboBoxes();

		DataArray fda = dataProvider.getFullArray();
		DataTableSpec fInSpec = fda.getDataTableSpec();
		labChoice.addItem("--As Dialog--");
		for (int i = 0; i < fInSpec.getNumColumns(); i++) {
			labChoice.addItem(fInSpec.getColumnSpec(i).getName());
		}

		JPanel legendSliderPanel = new JPanel(new GridBagLayout());

		legend = new Legend();
		legend.setMinSize(getNodeModel().getMinSize().getIntValue());
		legend.setMaxSize(getNodeModel().getMaxSize().getIntValue());
		JScrollPane scrollPane = new JScrollPane(legend);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		sliderPanel = new SliderPanel();

		legendSliderPanel.add(scrollPane, new GridBagConstraints(0, 0, 1, 1,
				1.0, 1.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		legendSliderPanel.add(sliderPanel, new GridBagConstraints(0, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		rePlotGraph(true);

		rPanel.add(legendSliderPanel, BorderLayout.EAST);
		rootPanel.add(rPanel, BorderLayout.CENTER);
		rootPanel.add(selPanel, BorderLayout.SOUTH);
		this.setComponent(rootPanel);
	}

	public double getValueForCell(DataCell c) {
		double d = 0.0;
		if (c.getType() == DataType.getType(IntCell.class)) {
			d = ((IntCell) c).getRealValue();
		} else if (c.getType() == DataType.getType(DoubleCell.class)) {
			d = ((DoubleCell) c).getDoubleValue();
		} else {
			LOG.warn("some rows have missing values - treating as zero");
		}
		return d;
	}

	public boolean isMissingValue(DataCell c) {
		if (c.getType() == DataType.getMissingCell().getType()) {
			return true;
		}
		return false;
	}

	public void setupComboBoxes() {
		xChoice.removeAllItems();
		yChoice.removeAllItems();
		zChoice.removeAllItems();
		colChoice.removeAllItems();
		colChoice.removeAllItems();
		sizeChoice.removeAllItems();
		sizeChoice.removeAllItems();
		DataArray da = dataProvider.getDataArray(0);
		DataTableSpec inSpec = da.getDataTableSpec();
		zChoice.addItem("--No Z Values--");
		colChoice.addItem("--No Color Values--");
		colChoice.addItem("--Use Cell Color--");
		sizeChoice.addItem("--No Size Values--");
		sizeChoice.addItem("--Use Cell Size--");

		for (int i = 0; i < inSpec.getNumColumns(); i++) {
			if (inSpec.getColumnSpec(i).getType() == DataType
					.getType(IntCell.class)
					|| inSpec.getColumnSpec(i).getType() == DataType
							.getType(StringCell.class)
					|| inSpec.getColumnSpec(i).getType() == DataType
							.getType(DoubleCell.class)) {
				colChoice.addItem(inSpec.getColumnSpec(i).getName());
				sizeChoice.addItem(inSpec.getColumnSpec(i).getName());
			}
			if (inSpec.getColumnSpec(i).getType() == DataType
					.getType(IntCell.class)
					|| inSpec.getColumnSpec(i).getType() == DataType
							.getType(DoubleCell.class)) {
				xChoice.addItem(inSpec.getColumnSpec(i).getName());
				yChoice.addItem(inSpec.getColumnSpec(i).getName());
				zChoice.addItem(inSpec.getColumnSpec(i).getName());
			}
		}
	}

	public Component getComponent(String id) {

		Component c = null;
		DataArray fda = dataProvider.getFullArray();
		DataTableSpec fInSpec = fda.getDataTableSpec();
		DataRow r = null;
		for (DataRow row : fda) {
			if (row.getKey().getString().compareTo(id) == 0) {
				r = row;
			}
		}
		if (r != null) {
			if (labChoice.getSelectedIndex() == 0) {
				JPanel pp = new JPanel(new GridBagLayout());
				pp.setBackground(Color.WHITE);
				pp.setBorder(BorderFactory.createEtchedBorder());
				GridBagConstraints gridBagConstraints = new GridBagConstraints();
				gridBagConstraints.gridx = 0;
				gridBagConstraints.gridy = 0;
				gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
				gridBagConstraints.weightx = 1.0;
				gridBagConstraints.weighty = 0;
				gridBagConstraints.ipadx = 5;
				gridBagConstraints.ipady = 2;
				List<String> iList = getNodeModel().getIncExCols()
						.getIncludeList();
				int rr = 0;
				for (String s : iList) {
					gridBagConstraints.gridx = 0;
					gridBagConstraints.gridy = rr;
					DataCell dc = r.getCell(fInSpec.findColumnIndex(s));
					DataType dt = fInSpec.getColumnSpec(
							fInSpec.findColumnIndex(s)).getType();
					Component cc = dt.getRenderer(
							fInSpec.getColumnSpec(fInSpec.findColumnIndex(s)))
							.getRendererComponent(dc);
					pp.add(new JLabel(s), gridBagConstraints);
					gridBagConstraints.gridx = 1;
					pp.add(getLabel(cc), gridBagConstraints);
					pp.add(cc, gridBagConstraints);
					rr++;
				}
				return pp;
			} else {
				DataCell dc = r.getCell(fInSpec
						.findColumnIndex((String) labChoice.getSelectedItem()));
				DataType dt = fInSpec.getColumnSpec(
						fInSpec.findColumnIndex((String) labChoice
								.getSelectedItem())).getType();
				c = dt.getRenderer(
						fInSpec.getColumnSpec(fInSpec
								.findColumnIndex((String) labChoice
										.getSelectedItem())))
						.getRendererComponent(dc);
				JPanel p = new JPanel(new BorderLayout());
				p.setBackground(Color.WHITE);
				p.setBorder(BorderFactory.createEtchedBorder());
				p.add(c, BorderLayout.CENTER);
				return p;
			}
		} else {
			return new JLabel("---");
		}

	}

	public Component getLabel(Component c) {

		c.setSize(c.getPreferredSize());
		BufferedImage bi = new BufferedImage(c.getSize().width,
				c.getSize().height, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();
		Color cc = g.getColor();
		g.setColor(Color.white);
		g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		g.setColor(cc);
		c.paint(g);
		JLabel ll = new JLabel();
		ll.setIcon(new ImageIcon(bi));
		ll.setHorizontalAlignment(SwingConstants.LEFT);
		return ll;
	}

	public void rePlotGraph(boolean shouldRePlot) {
		// check if really should replot (avoids this message getting executed
		// by adding items to combo boxes when pop up not visible)
		if (shouldRePlot) {
			DataArray da = dataProvider.getDataArray(0);
			DataTableSpec inSpec = da.getDataTableSpec();
			int xIndex = inSpec.findColumnIndex((String) xChoice
					.getSelectedItem());
			int yIndex = inSpec.findColumnIndex((String) yChoice
					.getSelectedItem());
			int zIndex = inSpec.findColumnIndex((String) zChoice
					.getSelectedItem());
			int colIndex = inSpec.findColumnIndex((String) colChoice
					.getSelectedItem());
			int sizeIndex = inSpec.findColumnIndex((String) sizeChoice
					.getSelectedItem());
			double shMin = Double.MAX_VALUE;
			double shMax = Double.NEGATIVE_INFINITY;

			cp.clearPoints();
			tdg.clearPoints();

			// Try to find colorhandler if needed
			int cInd = 0;
			int sInd = 0;
			ColorHandler ch = null;
			if (colChoice.getSelectedIndex() == 1) {
				for (cInd = 0; cInd < inSpec.getNumColumns(); cInd++) {
					ch = inSpec.getColumnSpec(cInd).getColorHandler();
					if (ch != null) {
						break;
					}
				}
				if (ch == null) {
					LOG.warn("No Color Values found - using default colours");
				}
			}
			SizeHandler sh = null;
			if (sizeChoice.getSelectedIndex() == 1) {
				for (sInd = 0; sInd < inSpec.getNumColumns(); sInd++) {
					sh = inSpec.getColumnSpec(sInd).getSizeHandler();
					if (sh != null) {
						for (DataRow row : da) {
							double t = sh.getSizeFactor(row.getCell(sInd));
							if (t > shMax) {
								shMax = t;
							}
							if (t < shMin) {
								shMin = t;
							}
						}
						break;
					}
				}
				if (sh == null) {
					LOG.warn("No Size Values found - using default sizes");
				}
			}

			// create and add datapoints
			DataPoint dp;
			for (DataRow row : da) {
				double x, y, z;

				x = getValueForCell(row.getCell(xIndex));
				y = getValueForCell(row.getCell(yIndex));
				if (isMissingValue(row.getCell(xIndex))
						|| isMissingValue(row.getCell(yIndex))) {
					LOG.warn("Missing values present... Datapoints removed!");
					continue;
				}
				if (zChoice.getSelectedIndex() > 0 && zIndex >= 0) {
					z = getValueForCell(row.getCell(zIndex));
					if (isMissingValue(row.getCell(zIndex))) {
						LOG.warn("Missing values present... Datapoints removed!");
						continue;
					}
					dp = new DataPoint(x, y, z);
				} else {
					dp = new DataPoint(x, y);
				}
				dp.setID(row.getKey().getString());
				if (colChoice.getSelectedIndex() > 1 && colIndex >= 0) {
					try {
						if (inSpec.getColumnSpec(colIndex).getType() == DataType
								.getType(StringCell.class)) {
							dp.setColorLabel(((StringCell) row.getCell(colIndex))
									.getStringValue());
						}
						if (inSpec.getColumnSpec(colIndex).getType() == DataType
								.getType(DoubleCell.class)) {
							dp.setColorValue(((DoubleCell) row.getCell(colIndex))
									.getDoubleValue());
						}
						if (inSpec.getColumnSpec(colIndex).getType() == DataType
								.getType(IntCell.class)) {
							dp.setColorValue(((IntCell) row.getCell(colIndex))
									.getDoubleValue());
						}
					} catch (Exception e) {
						LOG.warn("some colours have missing values - treating as default");
					}
				}
				if (colChoice.getSelectedIndex() == 1) {
					if (ch != null) {
						try {
							dp.setColor(ch.getColorAttr(row.getCell(cInd))
									.getColor());
							if (inSpec.getColumnSpec(cInd).getType() == DataType
									.getType(StringCell.class)) {
								dp.setColorLabel(((StringCell) row.getCell(cInd))
										.getStringValue());
							} else {
								dp.setColorValue(((DoubleCell) row.getCell(cInd))
										.getDoubleValue());
							}
						} catch (Exception e) {
							LOG.warn("some colours have missing values - treating as default");
						}
					}
				}
				if (sizeChoice.getSelectedIndex() == 0) {
					dp.setPointSize(getNodeModel().getMinSize().getIntValue() + 3);
				}
				if (sizeChoice.getSelectedIndex() == 1) {
					if (sh != null) {
						try {
							int mm = getNodeModel().getMinSize().getIntValue();
							int mx = getNodeModel().getMaxSize().getIntValue();
							double sf = sh.getSizeFactor(row.getCell(sInd));
							int ps = mm
									+ (int) ((mx - mm) * (sf - shMin) / (shMax - shMin));

							dp.setPointSize(ps);
							if (inSpec.getColumnSpec(sInd).getType() == DataType
									.getType(StringCell.class)) {
								dp.setSizeLabel(((StringCell) row.getCell(sInd))
										.getStringValue());
							} else {
								dp.setSizeValue(((DoubleCell) row.getCell(sInd))
										.getDoubleValue());
							}
						} catch (Exception e) {
							LOG.warn("some sizes have missing values - treating as default");
						}
					}
				}
				if (sizeChoice.getSelectedIndex() > 1 && sizeIndex >= 0) {
					try {
						if (inSpec.getColumnSpec(sizeIndex).getType() == DataType
								.getType(StringCell.class)) {
							dp.setSizeLabel(((StringCell) row.getCell(sizeIndex))
									.getStringValue());
						}
						if (inSpec.getColumnSpec(sizeIndex).getType() == DataType
								.getType(DoubleCell.class)) {
							dp.setSizeValue(((DoubleCell) row.getCell(sizeIndex))
									.getDoubleValue());
						}
						if (inSpec.getColumnSpec(sizeIndex).getType() == DataType
								.getType(IntCell.class)) {
							dp.setSizeValue(((IntCell) row.getCell(sizeIndex))
									.getDoubleValue());
						}
					} catch (Exception e) {
						LOG.warn("some sizes have missing values - treating as default");
					}
				}
				cp.addPoint(dp);
				tdg.addPoint(dp);
			}

			if (zChoice.getSelectedIndex() > 0 && zIndex >= 0) {
				legend.setDataList(tdg.getpList());
				legend.setPanel(tdg);

				if (cp.getParent() != null) {
					rPanel.remove(cp);
				}
				if (tdg.getParent() == null) {
					rPanel.add(tdg);
				}
				sliderPanel.showZSlider();
				tdg.setXAxisTitle((String) xChoice.getSelectedItem());
				tdg.setYAxisTitle((String) yChoice.getSelectedItem());
				tdg.setZAxisTitle((String) zChoice.getSelectedItem());
				sliderPanel.setPanel(tdg);

			} else {
				legend.setDataList(cp.getPList());
				legend.setPanel(cp);

				if (tdg.getParent() != null) {
					rPanel.remove(tdg);
				}
				if (cp.getParent() == null) {
					rPanel.add(cp);
				}
				sliderPanel.hideZSlider();
				cp.setXAxisTitle((String) xChoice.getSelectedItem());
				cp.setYAxisTitle((String) yChoice.getSelectedItem());
				sliderPanel.setPanel(cp);
			}
			if (colChoice.getSelectedIndex() > 1 && colIndex >= 0) {
				if (inSpec.getColumnSpec(colIndex).getType() == DataType
						.getType(StringCell.class)) {
					legend.setColorType(Legend.CATEGORICAL);
				}
				if (inSpec.getColumnSpec(colIndex).getType() == DataType
						.getType(DoubleCell.class)) {
					legend.setColorType(Legend.CONTINUOUS);
				}
				if (inSpec.getColumnSpec(colIndex).getType() == DataType
						.getType(IntCell.class)) {
					legend.setColorType(Legend.CONTINUOUS);
				}
				legend.calcColorValues();
			} else if (colChoice.getSelectedIndex() == 1) {
				if (ch != null) {
					if (inSpec.getColumnSpec(cInd).getType() == DataType
							.getType(StringCell.class)) {
						legend.setColorType(Legend.CATEGORICAL);
					} else {
						legend.setColorType(Legend.CONTINUOUS);
					}
				}
				legend.useExternalColorModel();
			} else {
				legend.setColorType(Legend.SUPPLIED);
			}
			if (sizeChoice.getSelectedIndex() > 1 && sizeIndex >= 0) {
				if (inSpec.getColumnSpec(sizeIndex).getType() == DataType
						.getType(StringCell.class)) {
					legend.setSizeType(Legend.CATEGORICAL);
				}
				if (inSpec.getColumnSpec(sizeIndex).getType() == DataType
						.getType(DoubleCell.class)) {
					legend.setSizeType(Legend.CONTINUOUS);
				}
				if (inSpec.getColumnSpec(sizeIndex).getType() == DataType
						.getType(IntCell.class)) {
					legend.setSizeType(Legend.CONTINUOUS);
				}
				legend.calcSizeValues();
			} else if (sizeChoice.getSelectedIndex() == 1) {
				if (sh != null) {
					if (inSpec.getColumnSpec(sInd).getType() == DataType
							.getType(StringCell.class)) {
						legend.setSizeType(Legend.CATEGORICAL);
					} else {
						legend.setSizeType(Legend.CONTINUOUS);
					}
				}
			} else {
				legend.setSizeType(Legend.SUPPLIED);
			}
			cp.doSetup();
			tdg.doSetup();

			legend.generateLegend();
			if (zChoice.getSelectedIndex() > 0) {
				tdg.requestFocus();
			}
			rPanel.validate();
			setAllHiLited();
			cp.repaint();
			tdg.repaint();
			legend.repaint();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void modelChanged() {

		NGraphNodeModel nodeModel = (NGraphNodeModel) getNodeModel();
		dataProvider = (NGraphNodeModel) getNodeModel();
		setHiLiteHandler(getNodeModel().getInHiLiteHandler(0));
		setupComboBoxes();
		loadSettings();
		rePlotGraph(true);
		assert nodeModel != null;

		// be aware of a possibly not executed nodeModel! The data you retrieve
		// from your node model could be null, empty, or invalid in any kind.

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onClose() {
		saveSettings();
		setHiLiteHandler(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {
		loadSettings();
		rePlotGraph(true);
	}

	void saveSettings() {
		NGraphNodeModel nm = (NGraphNodeModel) getNodeModel();
		nm.setXLab((String) xChoice.getSelectedItem());
		nm.setYLab((String) yChoice.getSelectedItem());
		nm.setZLab((String) zChoice.getSelectedItem());
		nm.setColLab((String) colChoice.getSelectedItem());
		nm.setSizeLab((String) sizeChoice.getSelectedItem());
		nm.setLabLab((String) labChoice.getSelectedItem());
		nm.setShowFt(cp.isShowFitLine());
		
	}

	void loadSettings() {
		NGraphNodeModel nm = (NGraphNodeModel) getNodeModel();
		xChoice.setSelectedItem(nm.getXLab());
		yChoice.setSelectedItem(nm.getYLab());
		zChoice.setSelectedItem(nm.getZLab());
		colChoice.setSelectedItem(nm.getColLab());
		sizeChoice.setSelectedItem(nm.getSizeLab());
		labChoice.setSelectedItem(nm.getLabLab());
		cp.setShowFitLine(nm.isShowFt());
	}

	void createViewMenu() {
		// create the view menu
		JMenuItem zoomToSelection = new JMenuItem("Zoom To Selection");
		zoomToSelection.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (zChoice.getSelectedIndex() > 0) {
					tdg.zoomToSelection();
				} else {
					cp.zoomToSelection();
				}
				sliderPanel.updateSliderToZoomedRegion();
			}
		});
		JMenuItem zoomReset = new JMenuItem("Zoom Out");
		zoomReset.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (zChoice.getSelectedIndex() > 0) {
					tdg.resetZoom();
				} else {
					cp.resetZoom();
				}
				sliderPanel.updateSliderToZoomedRegion();
			}

		});
		JMenuItem resetView = new JMenuItem("Reset View Angles");
		resetView.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (zChoice.getSelectedIndex() > 0) {
					tdg.resetView();
					tdg.repaint();
				}
			}

		});
		JMenuItem toggleFitLine = new JMenuItem("Show/Hide Fit Line");
		toggleFitLine.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (zChoice.getSelectedIndex() == 0) {
					cp.setShowFitLine(!cp.isShowFitLine());
					cp.repaint();
				}
			}

		});
		// create the menu and all the menu items to it
		JMenu menu = new JMenu("View");
		menu.add(zoomToSelection);
		menu.add(zoomReset);
		menu.add(resetView);
		menu.add(toggleFitLine);
		// get the JMenu bar of the NodeView and add this menu to it
		getJMenuBar().add(menu);
	}

	void createSelectionMenu() {
		// create the selection menu
		JMenuItem selAll = new JMenuItem("Select All");
		selAll.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				for (DataPoint dp : legend.getDataPoints()) {
					dp.setSelected(true);
				}
				rPanel.repaint();
			}

		});
		JMenuItem selNone = new JMenuItem("Select None");
		selNone.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				for (DataPoint dp : legend.getDataPoints()) {
					dp.setSelected(false);
				}
				rPanel.repaint();
			}

		});
		JMenuItem selInv = new JMenuItem("Select Inverse");
		selInv.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				for (DataPoint dp : legend.getDataPoints()) {
					dp.setSelected(!dp.isSelected());
				}
				rPanel.repaint();
			}

		});
		JMenuItem selHiLite = new JMenuItem("Select HiLited");
		selHiLite.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				for (DataPoint dp : legend.getDataPoints()) {
					if (dp.isHilited()) {
						dp.setSelected(true);
					} else {
						dp.setSelected(false);
					}
				}
				rPanel.repaint();
			}

		});
		// create the menu and all the menu items to it
		JMenu menu = new JMenu("Select");
		menu.add(selAll);
		menu.add(selNone);
		menu.add(selInv);
		menu.add(selHiLite);
		// get the JMenu bar of the NodeView and add this menu to it
		getJMenuBar().add(menu);
	}

	void createHiLiteMenu() {
		// create the hilite menu
		// the HiliteHandler provides standard names
		JMenuItem hilite = new JMenuItem(HiLiteHandler.HILITE_SELECTED);

		hilite.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				Set<RowKey> event = new HashSet<RowKey>();
				for (DataPoint dp : legend.getDataPoints()) {
					if (dp.isSelected()) {
						event.add(new RowKey(dp.getID()));
					}
				}
				hHand.fireHiLiteEvent(event);
				rPanel.repaint();
			}

		});
		JMenuItem unhilite = new JMenuItem(HiLiteHandler.UNHILITE_SELECTED);

		unhilite.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				Set<RowKey> event = new HashSet<RowKey>();
				for (DataPoint dp : legend.getDataPoints()) {
					if (dp.isSelected()) {
						event.add(new RowKey(dp.getID()));
					}
				}
				hHand.fireUnHiLiteEvent(event);
				rPanel.repaint();
			}

		});

		JMenuItem clear = new JMenuItem(HiLiteHandler.CLEAR_HILITE);
		clear.addActionListener(new ActionListener() {

			/**
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(final ActionEvent e) {
				hHand.fireClearHiLiteEvent();
				rPanel.repaint();
			}
		});
		// create the menu and all the menu items to it
		JMenu menu = new JMenu(HiLiteHandler.HILITE);
		menu.add(hilite);
		menu.add(unhilite);
		menu.add(clear);
		// get the JMenu bar of the NodeView and add this menu to it
		getJMenuBar().add(menu);

	}

	public void setAllHiLited() {
		for (DataPoint dp : legend.getDataPoints()) {
			dp.setHilited(false);
		}
		for (RowKey rk : hHand.getHiLitKeys()) {
			for (DataPoint dp : legend.getDataPoints()) {
				if (rk.getString().compareTo(dp.getID()) == 0) {
					dp.setHilited(true);
					break;
				}
			}
		}
	}

	public void hiLite(KeyEvent event) {
		LOG.debug("hiliting " + event.keys().toString());
		for (RowKey rk : event.keys()) {
			for (DataPoint dp : legend.getDataPoints()) {
				if (rk.getString().compareTo(dp.getID()) == 0) {
					dp.setHilited(true);
					break;
				}
			}
		}
		rPanel.repaint();
	}

	public void unHiLite(KeyEvent event) {
		LOG.debug("unhiliting " + event.keys().toString());
		for (RowKey rk : event.keys()) {
			for (DataPoint dp : legend.getDataPoints()) {
				if (rk.getString().compareTo(dp.getID()) == 0) {
					dp.setHilited(false);
					break;
				}
			}
		}
		rPanel.repaint();
	}

	public void unHiLiteAll(KeyEvent event) {
		LOG.debug("unhiliting all " + event.keys().toString());
		for (DataPoint dp : legend.getDataPoints()) {
			dp.setHilited(false);
		}
		rPanel.repaint();
	}

	public void setHiLiteHandler(HiLiteHandler hiliter) {
		// handler is same, so do nothing
		if (hiliter == null || hiliter.equals(hHand)) {
			return;
		}
		// unregister from old handler
		if (hHand != null) {
			hHand.removeHiLiteListener(this);
		}
		hHand = hiliter;
		// register at new one
		if (hiliter != null) {
			hiliter.addHiLiteListener(this);
		}

	}

}
