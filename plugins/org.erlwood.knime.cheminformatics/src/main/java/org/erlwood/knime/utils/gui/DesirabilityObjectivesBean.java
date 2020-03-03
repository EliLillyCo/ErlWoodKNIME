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
package org.erlwood.knime.utils.gui;

import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.erlwood.knime.icons.IconLoader;

/**
 * A panel used to configure the Pareto ranking node.
 * 
 * A list box on the left is populated with all columns compatible to Double.
 * Any of the columns can be used for ranking using the controls.
 * 
 * @author Dimitar Hristozov
 */
@SuppressWarnings("serial")
public class DesirabilityObjectivesBean extends JPanel implements
		ActionListener, ListSelectionListener, ListDataListener {
	private JButton btnAddSelected, btnAddAll, btnRemAll;
	private DefaultListModel listModel;
	private JList mLbxProps;
	private JPanel mPnlProps;

	private List<ParetoObjectivePanel> mPropPanels;
	
	private RankingNodeDialog dialog;

	/**
	 * 
	 */
	public DesirabilityObjectivesBean(final RankingNodeDialog dialog) {
		super();
		this.dialog = dialog;
		createGUI(dialog.getConfigString( ));
	}

	/**
	 * @param lyt
	 */
	public DesirabilityObjectivesBean(LayoutManager lyt, final String conf) {
		super(lyt);
		createGUI(conf);
	}

	/**
	 * @param doubleBuffer
	 */
	public DesirabilityObjectivesBean(boolean doubleBuffer, final String conf) {
		super(doubleBuffer);
		createGUI(conf);
	}

	/**
	 * @param lyt
	 * @param doubleBuffer
	 */
	public DesirabilityObjectivesBean(LayoutManager lyt, boolean doubleBuffer,
			final String conf) {
		super(lyt, doubleBuffer);
		createGUI(conf);
	}

	/**
	 * Adds the currently selected items in the left-hand side list view to the
	 * right-hand side panel. The items are removed from the left-hand side list
	 * view.
	 */
	public void addSelected() {
		Object[] selected = mLbxProps.getSelectedValues();
		for (int i = 0; i < selected.length; ++i) {
			createRegisterAndShowParettoPanel(selected[i].toString());
		}
		int[] selInd = mLbxProps.getSelectedIndices();
		for (int i = selInd.length - 1; i >= 0; --i) {
			listModel.remove(selInd[i]);
		}
		this.validate();
	}

	/**
	 * Adds all "Available" items to the "Selected" list view.
	 */
	public void addAll() {
		for (int i = 0; i < listModel.getSize(); ++i) {
			createRegisterAndShowParettoPanel(listModel.get(i).toString());
		}
		listModel.clear();
		this.validate();
	}

	/**
	 * Clears the all selected properties.
	 */
	public void remAll() {
		for (int i = 0; i < mPropPanels.size(); ++i) {
			if(dialog.containsColumn(mPropPanels.get(i).getPropertyName())) {
				listModel.addElement(mPropPanels.get(i).getPropertyName());
			}
			mPropPanels.get(i).setVisible(false);
		}
		mPropPanels.clear();
		mPnlProps.removeAll();
		mPnlProps.validate();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnAddSelected) {
			addSelected();
		} else if (e.getSource() == btnAddAll) {
			addAll();
		} else if (e.getSource() == btnRemAll) {
			remAll();
		} else if (e.getSource() instanceof javax.swing.JButton) {
			javax.swing.JButton btn = (javax.swing.JButton) e.getSource();
			if (null != btn.getParent()
					&& btn.getParent().getParent() instanceof ParetoObjectivePanel) {
				ParetoObjectivePanel pnl = (ParetoObjectivePanel) btn
						.getParent().getParent();
				if(dialog.containsColumn(pnl.getPropertyName( ))) {
					listModel.addElement(pnl.getPropertyName());
				}
				pnl.setVisible(false);
				pnl.btnClose.removeActionListener(this);
				mPropPanels.remove(pnl);
				mPnlProps.remove(pnl);
				mPnlProps.validate();
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < listModel.size(); ++i) {
			res.append("available_prop=").append(listModel.get(i).toString())
					.append("\n");
		}
		for (int i = 0; i < mPropPanels.size(); ++i) {
			res.append("configured_prop=")
					.append(mPropPanels.get(i).toString()).append("\n");
		}
		return res.toString();
	}

	public void fromString(final String s) {
		mLbxProps.removeListSelectionListener(this);
		listModel.removeListDataListener(this);
		remAll();
		listModel.clear();
		List<String> avProps = new java.util.ArrayList<String>();
		List<String> confProps = new java.util.ArrayList<String>();
		DesirabilityObjectivesBean.parseConfString(s, avProps, confProps);
		for (String p : avProps) {
			listModel.addElement(p);
		}
		for (String p : confProps) {
			createRegisterAndShowParettoPanel(p);
		}
		mLbxProps.addListSelectionListener(this);
		listModel.addListDataListener(this);
		enableButtons();
	}

	private void createRegisterAndShowParettoPanel(final String prop) {
		ParetoObjectivePanel p = new ParetoObjectivePanel(prop);
		mPropPanels.add(p);
		java.awt.Dimension dim = new java.awt.Dimension(640, 50);
		p.setMinimumSize(dim);
		p.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 50));
		p.btnClose.addActionListener(this);
		mPnlProps.add(p);
		mPnlProps.validate();

	}

	public static void parseConfString(final String conf,
			List<String> avProps, List<String> confProps) {
		if (null != conf) {

			String[] props = conf.split("configured_prop=", 2);
			String[] av = props[0].split("available_prop=");
			for (String s : av) {
				if (s != null && !s.trim().equals("")) {
					avProps.add(s.substring(0, s.length() - 1));
				}
			}
			if (props.length > 1) {
				String[] c = props[1].split("configured_prop=");
				for (String s : c) {
					if (s != null && !s.trim().equals("")) {
						confProps.add(s.substring(0, s.length() - 1));
					}
				}
			}

		}
	}

	private void createGUI(final String conf) {
		List<String> avProps = new java.util.ArrayList<String>();
		List<String> confProps = new java.util.ArrayList<String>();
		DesirabilityObjectivesBean.parseConfString(conf, avProps, confProps);

		mPropPanels = new java.util.ArrayList<ParetoObjectivePanel>();
		setLayout(new javax.swing.BoxLayout(this,
				javax.swing.BoxLayout.LINE_AXIS));
		java.awt.Dimension minSize = new java.awt.Dimension(150, 200);
		java.awt.Dimension dimSpacer = new java.awt.Dimension(5, 5);

		mLbxProps = new javax.swing.JList();
		mLbxProps.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Available"));

		listModel = new javax.swing.DefaultListModel();
		for (String s : avProps) {
			listModel.addElement(s);
		}
		mLbxProps.setModel(listModel);
		mLbxProps.addMouseListener(new ListboxMouseAdapter());
		javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(mLbxProps);
		scroll.setMinimumSize(minSize);
		scroll.setMaximumSize(new java.awt.Dimension(150, Integer.MAX_VALUE));
		scroll.setPreferredSize(minSize);
		scroll.getViewport().setView(mLbxProps);
		this.add(scroll);
		this.add(javax.swing.Box.createRigidArea(dimSpacer));

		JPanel mPnlButtons = new javax.swing.JPanel();
		mPnlButtons.setLayout(new javax.swing.BoxLayout(mPnlButtons,
				javax.swing.BoxLayout.PAGE_AXIS));
		java.awt.Dimension dimBtns = new java.awt.Dimension(55, 22);

		btnAddSelected = new javax.swing.JButton(">");
		btnAddSelected.setPreferredSize(dimBtns);
		btnAddSelected.setMinimumSize(dimBtns);
		btnAddSelected.setMaximumSize(dimBtns);
		btnAddSelected.addActionListener(this);
		mPnlButtons.add(btnAddSelected);
		mPnlButtons.add(javax.swing.Box.createRigidArea(dimSpacer));

		btnAddAll = new javax.swing.JButton(">>");
		btnAddAll.setPreferredSize(dimBtns);
		btnAddAll.setMinimumSize(dimBtns);
		btnAddAll.setMaximumSize(dimBtns);
		btnAddAll.addActionListener(this);
		mPnlButtons.add(btnAddAll);
		mPnlButtons.add(javax.swing.Box.createRigidArea(dimSpacer));

		btnRemAll = new javax.swing.JButton("<<");
		btnRemAll.setPreferredSize(dimBtns);
		btnRemAll.setMinimumSize(dimBtns);
		btnRemAll.setMaximumSize(dimBtns);
		btnRemAll.addActionListener(this);
		mPnlButtons.add(btnRemAll);
		mPnlButtons.add(javax.swing.Box.createRigidArea(dimSpacer));

		mPnlButtons.add(javax.swing.Box.createVerticalGlue());

		this.add(mPnlButtons);
		this.add(javax.swing.Box.createRigidArea(dimSpacer));

		mPnlProps = new javax.swing.JPanel();
		mPnlProps.setLayout(new javax.swing.BoxLayout(mPnlProps,
				javax.swing.BoxLayout.PAGE_AXIS));
		mPnlProps.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Selected"));

		scroll = new javax.swing.JScrollPane(mPnlProps);
		scroll.setMinimumSize(new java.awt.Dimension(600, 200));
		scroll.setPreferredSize(new java.awt.Dimension(600, 200));
		this.add(scroll);

		listModel.addListDataListener(this);
		mLbxProps.addListSelectionListener(this);

		for (String s : confProps) {
			createRegisterAndShowParettoPanel(s);
		}
		enableButtons();
	}

	private void enableButtons() {
		btnAddAll.setEnabled(0 < listModel.getSize());
		btnRemAll.setEnabled(0 < mPropPanels.size());
		btnAddSelected.setEnabled(btnAddAll.isEnabled()
				&& -1 != mLbxProps.getMinSelectionIndex());
	}

	private class ListboxMouseAdapter extends java.awt.event.MouseAdapter {
		@Override
		public void mouseClicked(java.awt.event.MouseEvent e) {
			if (e.getClickCount() == 2
					&& (e.getSource() instanceof javax.swing.JList)) {
				int index = ((javax.swing.JList) e.getSource())
						.locationToIndex(e.getPoint());
				if (-1 != index) {
					if (e.getSource() == mLbxProps) {
						addSelected();
					}
				}
			}
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		enableButtons();
	}

	public void intervalAdded(ListDataEvent e) {
		enableButtons();
	}

	public void intervalRemoved(ListDataEvent e) {
		enableButtons();
	}

	public void contentsChanged(ListDataEvent e) {
		enableButtons();
	}

	/**
	 * Simple collection of all properties which define a condition for Paretto
	 * optimisation.
	 * 
	 * @author Dimitar Hristozov
	 */
	public static class DesirabilityCondition implements Cloneable {
		public static final int UNDESIRABLE = 1;
		public static final int DESIRABLE = 0;

		private String mPropName;
		private int mOptMethod;
		private Double mWeight;

		public static DesirabilityCondition createCondition(final String s) {
			DesirabilityCondition res = new DesirabilityCondition();
			try {
				res.fromString(s);
			} catch (IllegalArgumentException e) {
				res = null;
			}
			return res;
		}

		public DesirabilityCondition(final String prop) {
			this(prop, 0, null);
		}

		public DesirabilityCondition(final String prop, int optMethod,
				Double weight) {
			mPropName = prop;
			mOptMethod = optMethod;
			if (mOptMethod < UNDESIRABLE || mOptMethod > DESIRABLE) {
				mOptMethod = DESIRABLE;
			}
			mWeight = weight;
		}

		private DesirabilityCondition() {
		}

		public String getPropertyName() {
			return mPropName;
		}

		public int getOptimizationMethod() {
			return mOptMethod;
		}

		public Double getWeight() {
			return mWeight;
		}

		public boolean isValid() {
			return null != mWeight;
		}

		@Override
		public String toString() {
			return mPropName + ";" + mOptMethod + ";"
					+ (null != mWeight ? mWeight.toString() : "null");
		}

		public void fromString(final String s) {
			StringTokenizer tknz = new StringTokenizer(s, ";");
			try {
				mPropName = tknz.nextToken();
				mOptMethod = Integer.valueOf(tknz.nextToken());
				if (mOptMethod < DESIRABLE || mOptMethod > UNDESIRABLE) {
					mOptMethod = DESIRABLE;
				}
				String tkn = tknz.nextToken();
				mWeight = "null".equalsIgnoreCase(tkn) ? 1.0 : Double
						.valueOf(tkn);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid format: " + s, e);
			}
		}

		@Override
		public Object clone() throws CloneNotSupportedException {
			DesirabilityCondition res = new DesirabilityCondition();
			res.mWeight = this.mWeight;
			res.mOptMethod = this.mOptMethod;
			res.mPropName = this.mPropName;
			return res;
		}
	}

	/**
	 * This panel allows the configuration of a single field. Four types of
	 * optimisation are provided: minimize, maximize, optimal value, and optimal
	 * interval.
	 * 
	 * @author Dimitar Hristozov
	 */
	public static class ParetoObjectivePanel extends javax.swing.JPanel
			implements java.awt.event.ActionListener {
		private javax.swing.JLabel mLblMethod;
		private javax.swing.JComboBox cmbMethod;
		private javax.swing.JTextField edtLeftCondition;
		private javax.swing.JButton btnChart;
		private javax.swing.JButton btnClose;

		private DesirabilityCondition mCondition;

		public String getPropertyName() {
			return mCondition.getPropertyName();
		}

		public DesirabilityCondition getCondition() {
			String txt = edtLeftCondition.getText();
			mCondition.mWeight = 0 == txt.length() ? 1.0 : Double.valueOf(txt);
			mCondition.mOptMethod = cmbMethod.getSelectedIndex();
			return mCondition;
		}

		public ParetoObjectivePanel(final String prop) {
			super();
			mCondition = DesirabilityCondition.createCondition(prop);
			if (null == mCondition) {
				mCondition = new DesirabilityCondition(prop);
			}
			buildGUI();
		}

		public ParetoObjectivePanel(DesirabilityCondition condition) throws CloneNotSupportedException {
			mCondition = (DesirabilityCondition) condition.clone();
		}

		@Override
		public String toString() {
			return getCondition().toString();
		}

		private void buildGUI() {
			setBorder(javax.swing.BorderFactory.createTitledBorder(mCondition
					.getPropertyName()));
			setLayout(new java.awt.BorderLayout());
			javax.swing.Box box = new javax.swing.Box(
					javax.swing.BoxLayout.LINE_AXIS);
			add(box, java.awt.BorderLayout.CENTER);
			box.add(javax.swing.Box.createHorizontalStrut(5));
			mLblMethod = new javax.swing.JLabel();
			mLblMethod.setText("Desirability");
			box.add(mLblMethod);
			box.add(javax.swing.Box.createHorizontalStrut(5));
			java.awt.Dimension dmnCmb = new java.awt.Dimension(150,
					Integer.MAX_VALUE);
			cmbMethod = new javax.swing.JComboBox();
			cmbMethod.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "Minimise", "Maximise" }));
			cmbMethod.addActionListener(this);
			cmbMethod.setSize(dmnCmb);
			cmbMethod.setMinimumSize(dmnCmb);
			cmbMethod.setMaximumSize(dmnCmb);
			box.add(cmbMethod);
			box.add(javax.swing.Box.createHorizontalStrut(5));
			java.awt.Dimension dmnEdits = new java.awt.Dimension(65,
					Integer.MAX_VALUE);
			edtLeftCondition = new javax.swing.JTextField();
			edtLeftCondition.setSize(dmnEdits);
			edtLeftCondition.setMinimumSize(dmnEdits);
			edtLeftCondition.setMaximumSize(dmnEdits);
			edtLeftCondition.setDocument(new DoubleDocument());
			edtLeftCondition
					.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
			edtLeftCondition
					.setText(null != mCondition.getWeight() ? mCondition
							.getWeight().toString() : "1.0");
			box.add(edtLeftCondition);
			box.add(javax.swing.Box.createHorizontalStrut(10));
			java.awt.Dimension dmnBtns = new java.awt.Dimension(24, 24);
			btnChart = new javax.swing.JButton();
			btnChart.setIcon(IconLoader.loadIcon("chart_bar.png"));
			btnChart.setSize(dmnBtns);
			btnChart.setMinimumSize(dmnBtns);
			btnChart.setMaximumSize(dmnBtns);
			btnChart.setToolTipText("View the distribution of values");
			btnChart.setVisible(false);
			box.add(btnChart);

			btnClose = new javax.swing.JButton();
			btnClose.setIcon(IconLoader.loadIcon("delete.png"));
			btnClose.setSize(dmnBtns);
			btnClose.setMinimumSize(dmnBtns);
			btnClose.setMaximumSize(dmnBtns);
			btnClose.setToolTipText("Remove");
			box.add(btnClose);
			box.add(javax.swing.Box.createHorizontalStrut(5));

			cmbMethod.setSelectedIndex(mCondition.getOptimizationMethod());
		}

		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == cmbMethod) {
				cmbMethodActionPerformed();
			}

		}

		private void cmbMethodActionPerformed() {
		}

		class DoubleDocument extends PlainDocument {
			public void insertString(int offset, String value,
					AttributeSet attributes) throws BadLocationException {
				if (null != value) {
					String newValue;
					int length = getLength();
					if (length == 0) {
						newValue = value;
					} else {
						String currentContent = getText(0, length);
						StringBuffer currentBuffer = new StringBuffer(
								currentContent);
						currentBuffer.insert(offset, value);
						newValue = currentBuffer.toString();
					}
					try {
						Double.parseDouble(newValue);
						super.insertString(offset, value, attributes);
					} catch (NumberFormatException exception) {
						Toolkit.getDefaultToolkit().beep();
					}
				}
			}
		} // DoubleDocument

	} // ParettoObjectivePanel
}
