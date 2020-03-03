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
package org.erlwood.knime.nodes.similarityviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.knime.core.node.NodeView;
import org.erlwood.knime.icons.IconLoader;
import org.erlwood.knime.utils.gui.ext.Gradient;
import org.erlwood.knime.utils.gui.ext.HeatMap;

/**
 * <code>NodeView</code> for the "SimilarityViewer" Node. Displays similarity
 * matrix as a heat map. Also allows the sorting with a "query" molecule.
 * 
 * @author Dimitar Hristozov
 */
public class SimilarityViewerNodeView extends
		NodeView<SimilarityViewerNodeModel> implements ItemListener,
		MouseMotionListener, MouseListener {

	private HeatMap mHeatMap;
	private JComboBox gradientComboBox;

	private ImageIcon[] icons;
	private String[] names = { "GRADIENT_BLACK_TO_WHITE",
			"GRADIENT_BLUE_TO_RED", "GRADIENT_GREEN_YELLOW_ORANGE_RED",
			"GRADIENT_HEAT", "GRADIENT_HOT", "GRADIENT_MAROON_TO_GOLD",
			"GRADIENT_RAINBOW", "GRADIENT_RED_TO_GREEN", "GRADIENT_ROY" };
	private Color[][] gradients = { Gradient.GRADIENT_BLACK_TO_WHITE,
			Gradient.GRADIENT_BLUE_TO_RED,
			Gradient.GRADIENT_GREEN_YELLOW_ORANGE_RED, Gradient.GRADIENT_HEAT,
			Gradient.GRADIENT_HOT, Gradient.GRADIENT_MAROON_TO_GOLD,
			Gradient.GRADIENT_RAINBOW, Gradient.GRADIENT_RED_TO_GREEN,
			Gradient.GRADIENT_ROY };

	private JPanel mPnl1 = new JPanel(), mPnl2 = new JPanel();
	private JLabel mCmp1 = new JLabel(), mCmp2 = new JLabel();
	private JLabel mSml = new JLabel();

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel
	 *            The model (class: {@link SimilarityViewerNodeModel})
	 */
	protected SimilarityViewerNodeView(final SimilarityViewerNodeModel nodeModel) {
		super(nodeModel);
		// gui stuff to demonstrate options
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
		listPane.setBorder(BorderFactory.createTitledBorder("Options"));

		JPanel pnl = new JPanel();
		pnl.setBorder(BorderFactory.createTitledBorder("Gradient"));

		icons = new ImageIcon[names.length];
		Integer[] intArray = new Integer[names.length];
		for (int i = 0; i < names.length; i++) {
			intArray[i] = Integer.valueOf(i);
			icons[i] = IconLoader.loadIcon(names[i] + ".gif");
		}

		gradientComboBox = new JComboBox(intArray);
		ComboBoxRenderer renderer = new ComboBoxRenderer();
		gradientComboBox.setRenderer(renderer);
		gradientComboBox.setSelectedIndex(6);
		gradientComboBox.addItemListener(this);

		pnl.add(gradientComboBox, BorderLayout.CENTER);

		listPane.add(pnl);
		listPane.add(Box.createVerticalStrut(15));

		mPnl1.setBorder(BorderFactory.createTitledBorder("Compound #"));
		mCmp1.setMinimumSize(new Dimension(150, 150));
		mPnl1.add(mCmp1, BorderLayout.CENTER);
		listPane.add(mPnl1);

		mPnl2.setBorder(BorderFactory.createTitledBorder("Compound #"));
		mCmp2.setMinimumSize(new Dimension(150, 150));
		mPnl2.add(mCmp2, BorderLayout.CENTER);
		listPane.add(mPnl2);

		pnl = new JPanel();
		pnl.setBorder(BorderFactory.createTitledBorder(nodeModel.getmIsDm()
				.getBooleanValue() ? "Distance" : "Similarity"));
		pnl.add(mSml, BorderLayout.CENTER);
		listPane.add(pnl);

		listPane.add(Box.createVerticalGlue());
		// ----------------------------------------------------------------------

		// you can use a pre-defined gradient:
		mHeatMap = new HeatMap(nodeModel.getDvs(), Gradient.GRADIENT_RAINBOW);
		mHeatMap.addMouseListener(this);
		mHeatMap.addMouseMotionListener(this);

		// set miscelaneous settings
		mHeatMap.setDrawLegend(true);

		mHeatMap.setTitle("Heat Map");
		mHeatMap.setDrawTitle(true);

		mHeatMap.setDrawXAxisTitle(false);
		mHeatMap.setDrawYAxisTitle(false);

		mHeatMap.setCoordinateBounds(1, nodeModel.getDvs().length, 1,
				nodeModel.getDvs().length);
		mHeatMap.setDrawXTicks(false);
		mHeatMap.setDrawYTicks(false);

		mHeatMap.setColorForeground(Color.black);
		mHeatMap.setColorBackground(Color.white);

		JPanel fp = new JPanel(new BorderLayout());

		fp.add(listPane, BorderLayout.EAST);
		fp.add(mHeatMap, BorderLayout.CENTER);

		fp.setPreferredSize(new Dimension(800, 600));
		fp.setMinimumSize(new Dimension(800, 600));

		this.setComponent(fp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void modelChanged() {
		SimilarityViewerNodeModel nodeModel = (SimilarityViewerNodeModel) getNodeModel();
		assert nodeModel != null;
		mHeatMap.updateData2(nodeModel.getDvs());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onClose() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if (source.equals(gradientComboBox)) {
			Integer ix = (Integer) e.getItem();
			if (e.getStateChange() == ItemEvent.SELECTED) {
				mHeatMap.updateGradient2(gradients[ix]);
			}
		}
	}

	@SuppressWarnings("serial")
	class ComboBoxRenderer extends JLabel implements ListCellRenderer {
		public ComboBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			int selectedIndex = ((Integer) value).intValue();
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			ImageIcon icon = icons[selectedIndex];
			setIcon(icon);
			setText(names[selectedIndex].substring(9));
			return this;
		}
	}

	public void mouseDragged(MouseEvent arg0) {
	}

	public void mouseMoved(MouseEvent me) {
		JPanel pnl = (JPanel) me.getSource();
		int x = me.getX() - 30, y = me.getY() - 30;
		if (x > 0 && x < pnl.getWidth() - 60 && y > 0
				&& y < pnl.getHeight() - 60) {
			double pointw = 1.0 * (pnl.getWidth() - 60)
					/ getNodeModel().getDvs().length;
			double point_h = 1.0 * (pnl.getHeight() - 60)
					/ getNodeModel().getDvs().length;
			if (0 == pointw || 0 == point_h) {
				return;
			}
			int indx = (int) Math.ceil(x / pointw) - 1, indy = (int) Math
					.ceil(y / point_h) - 1;
			try {
				SimilarityViewerNodeModel m = getNodeModel();
				byte[] png = m.getMols()[indx].toBinFormat("png:w150,h150");
				ImageIcon img = new ImageIcon(png);
				mCmp1.setIcon(img);
				String t = getNodeModel().getRowIds()[indx] + " (# "
						+ (indx + 1) + ")";
				mPnl1.setBorder(BorderFactory.createTitledBorder("Compound ID "
						+ t));

				png = m.getMols()[indy].toBinFormat("png:w150,h150");
				img = new ImageIcon(png);
				mCmp2.setIcon(img);
				t = getNodeModel().getRowIds()[indy] + " (# "
						+ (indy + 1) + ")";
				mPnl2.setBorder(BorderFactory.createTitledBorder("Compound ID "
						+ t));

				mSml.setText(String.format("%.2f",
						m.getDvs()[indx].getDistance(m.getDvs()[indy])));
			} catch (Exception r) {

			}
		}
	}

	public void mouseClicked(MouseEvent me) {
		mouseMoved(me);
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}
}
