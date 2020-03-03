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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.erlwood.knime.utils.gui.HintTextField;
import org.erlwood.knime.utils.gui.layout.TableLayout;



/**
 * A Bean which contains two lists - the "Available" items are in the list on
 * the left-hand side and the "Selected" items are in the list on the right-hand
 * side. The user may select items from the "Available" list and move them to
 * the "Selected" list.
 * 
 * @author Dimitar Hristozov
 */
@SuppressWarnings("serial")
public final class DualListboxBean<T extends Comparable<? super T>> extends JPanel implements ActionListener,
		ListSelectionListener, ListDataListener {
	private JList leftList, rightList;
	private JPanel pnlButtons;
	private JButton btnAddSelected, btnRemSelected, btnAddAll, btnRemAll;
	private final SortedFilteredListModel<T> leftListModel = new SortedFilteredListModel<T>();
	private final SortedFilteredListModel<T> rightListModel = new SortedFilteredListModel<T>();
	private HintTextField	filterBox;
	private boolean mAllowDuplicates = false;
	
	/**
	 * Ctor.
	 */
	public DualListboxBean() {
		super();
		createGUI();
	}

	/**
	 * Ctor.
	 * 
	 * @param doubleBuffer
	 *            @see JPanel
	 */
	public DualListboxBean(boolean doubleBuffer) {
		super(doubleBuffer);
		createGUI();
	}

	/**
	 * @return <code>true</code> if the "Selected" list can contain duplicates,
	 *         that is, an item from the left-hand side list may be selected
	 *         more than once. Note that this parameter changes the behaviour of
	 *         the "Add" action. When no duplicates are allowed selected items
	 *         are removed from the "Available" list upon addition to the
	 *         "Selected" one.
	 */
	public boolean areDuplicatesAllowed() {
		return mAllowDuplicates;
	}

	/**
	 * Sets the desired policy for handling duplicates in the "Selected" list.
	 * 
	 * @see {@link #areDuplicatesAllowed()}
	 * @param allow
	 *            set to <code>true</code> to allow items to be selected twice.
	 */
	public void setDuplicatesPolicy(final boolean allow) {
		if (allow != mAllowDuplicates) {
			mAllowDuplicates = allow;
			if (!allow) {
				for (int s = 0; s < getRightListModel().getSize(); ++s) {
					int i = getRightListModel().indexOf(
							getRightListModel().get(s), s + 1);
					while (-1 != i) {
						getRightListModel().remove(i);
						i = getRightListModel().indexOf(
								getRightListModel().get(s), i);
					}
				}
			}
		}
	}

	
	
	private void createGUI() {
		this.setLayout(new TableLayout(new double[][] {{2, TableLayout.FILL, 5, 55, 5, TableLayout.FILL, 2}, { TableLayout.PREFERRED, 5, TableLayout.FILL }}));
		Dimension minSize = new Dimension(100, 250);
		Dimension dimSpacer = new Dimension(5, 5);
		
		filterBox = new HintTextField("Enter filter text");
		filterBox.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent evt) {
				leftListModel.filter(filterBox.getText());
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				leftListModel.filter(filterBox.getText());
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				leftListModel.filter(filterBox.getText());
			}
		});
		
		add(filterBox, "1,0");
		
		DualListboxMouseAdapter listsMouseAdapter = new DualListboxMouseAdapter();
		DualListboxTransferHandler trsfHandler = new DualListboxTransferHandler();

		leftList = new JList();
		leftList.setBorder(BorderFactory.createTitledBorder("Available"));
		leftList.setDragEnabled(true);
		
		getLeftListModel().addListDataListener(this);
		leftList.setModel(getLeftListModel());
		leftList.addMouseListener(listsMouseAdapter);
		leftList.addListSelectionListener(this);
		leftList.setTransferHandler(trsfHandler);
		JScrollPane scroll = new JScrollPane(leftList);
		scroll.setMinimumSize(minSize);
		scroll.setPreferredSize(minSize);
		scroll.getViewport().setView(leftList);
		
		add(scroll, "1,2");
		//add(Box.createRigidArea(dimSpacer));

		pnlButtons = new JPanel();

		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.PAGE_AXIS));
		Dimension dimBtns = new Dimension(55, 22);

		btnAddSelected = new JButton(">");
		btnAddSelected.setPreferredSize(dimBtns);
		btnAddSelected.setMinimumSize(dimBtns);
		btnAddSelected.setMaximumSize(dimBtns);
		btnAddSelected.addActionListener(this);
		pnlButtons.add(btnAddSelected);
		pnlButtons.add(Box.createRigidArea(dimSpacer));

		btnRemSelected = new JButton("<");
		btnRemSelected.setPreferredSize(dimBtns);
		btnRemSelected.setMinimumSize(dimBtns);
		btnRemSelected.setMaximumSize(dimBtns);
		btnRemSelected.addActionListener(this);
		pnlButtons.add(btnRemSelected);
		pnlButtons.add(Box.createRigidArea(dimSpacer));

		btnAddAll = new JButton(">>");
		btnAddAll.setPreferredSize(dimBtns);
		btnAddAll.setMinimumSize(dimBtns);
		btnAddAll.setMaximumSize(dimBtns);
		btnAddAll.addActionListener(this);
		pnlButtons.add(btnAddAll);
		pnlButtons.add(Box.createRigidArea(dimSpacer));

		btnRemAll = new JButton("<<");
		btnRemAll.setPreferredSize(dimBtns);
		btnRemAll.setMinimumSize(dimBtns);
		btnRemAll.setMaximumSize(dimBtns);
		btnRemAll.addActionListener(this);
		pnlButtons.add(btnRemAll);
		pnlButtons.add(Box.createRigidArea(dimSpacer));

		pnlButtons.add(Box.createVerticalGlue());

		add(pnlButtons, "3,2");
		//this.add(Box.createRigidArea(dimSpacer));

		rightList = new JList();
		rightList.setBorder(BorderFactory.createTitledBorder("Selected"));
		rightList.setDragEnabled(true);
		
		getRightListModel().addListDataListener(this);
		rightList.setModel(getRightListModel());
		rightList.addMouseListener(listsMouseAdapter);
		rightList.addListSelectionListener(this);
		rightList.setTransferHandler(trsfHandler);
		scroll = new JScrollPane(rightList);
		scroll.setMinimumSize(minSize);
		scroll.setPreferredSize(minSize);
		
		add(scroll, "5,2");

		enableButtons();
	}

	/**
	 * Shows the GUI.
	 * 
	 * @param args
	 *            Not used.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frm = new JFrame();
				frm.setLayout(new BorderLayout());

				DualListboxBean dlb = new DualListboxBean();
				frm.add(dlb, BorderLayout.CENTER);
				frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frm.pack();
				frm.setVisible(true);
			}
		});
	}

	/**
	 * Handles button clicks.
	 */
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == btnAddSelected) {
			addSelected();
		} else if (event.getSource() == btnRemSelected) {
			removeSelected();
		} else if (event.getSource() == btnAddAll) {
			addAll();
		} else if (event.getSource() == btnRemAll) {
			remAll();
		}
	}

	/**
	 * Adds the currently selected items in the left-hand side list view to the
	 * right-hand side list view. If @see {@link #areDuplicatesAllowed()}
	 * returns <code>false</code> the items are removed from the left-hand side
	 * list view.
	 */
	public void addSelected() {
		Object[] selected = leftList.getSelectedValues();
		for (int i = 0; i < selected.length; ++i) {
			if (mAllowDuplicates
					|| -1 == getRightListModel().indexOf(selected[i])) {
				getRightListModel().addElement((T) selected[i]);
			}
		}
		if (!mAllowDuplicates) {
			int[] selInd = leftList.getSelectedIndices();
			for (int i = selInd.length - 1; i >= 0; --i) {
				getLeftListModel().remove(selInd[i]);
			}
		}
		// enableButtons();
	}

	/**
	 * Removes the currently selected items in the right-hand side list view
	 * from it. That is, the user "unselects" these items.
	 * 
	 */
	public void removeSelected() {
		Object[] selected = rightList.getSelectedValues();
		for (int i = 0; i < selected.length; ++i) {
			if (-1 == getLeftListModel().indexOf(selected[i])) {
				getLeftListModel().addElement((T) selected[i]);
			}
		}
		int[] selInd = rightList.getSelectedIndices();
		for (int i = selInd.length - 1; i >= 0; --i) {
			getRightListModel().remove(selInd[i]);
		}

	}

	/**
	 * Adds all "Available" items to the "Selected" list view.
	 */
	public void addAll() {
		for (int i = 0; i < getLeftListModel().getSize(); ++i) {
			if (mAllowDuplicates
					|| -1 == getRightListModel().indexOf(
							getLeftListModel().get(i))) {
				getRightListModel().addElement(getLeftListModel().get(i));
			}
		}
		if (!mAllowDuplicates) {
			getLeftListModel().clear();
		}

	}

	/**
	 * Clears the "Selected" list view.
	 */
	public void remAll() {
		for (int i = 0; i < getRightListModel().getSize(); ++i) {
			if (-1 == getLeftListModel().indexOf(getRightListModel().get(i))) {
				getLeftListModel().addElement(getRightListModel().get(i));
			}
		}
		getRightListModel().clear();

	}

	private void enableButtons() {
		btnAddAll.setEnabled(0 < getLeftListModel().getSize());
		btnRemAll.setEnabled(0 < getRightListModel().getSize());
		btnAddSelected.setEnabled(btnAddAll.isEnabled()
				&& null != leftList.getSelectedValue());
		btnRemSelected.setEnabled(btnRemAll.isEnabled()
				&& null != rightList.getSelectedValue());
	}

	/**
	 * Updates the (enable/disable) the buttons.
	 */
	public void valueChanged(ListSelectionEvent event) {
		enableButtons();
	}

	private class DualListboxMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2 && (e.getSource() instanceof JList)) {
				int index = ((JList) e.getSource()).locationToIndex(e
						.getPoint());
				if (-1 != index) {
					if (e.getSource() == leftList) {
						addSelected();
					} else if (e.getSource() == rightList) {
						removeSelected();
					}
				}
			}
		}
	}

	private static final String DND_LEFT = "DualListboxD&DLeft";
	private static final String DND_RIGHT = "DualListboxD&DRight";

	private class DualListboxTransferHandler extends TransferHandler {
		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			for (int i = 0; i < transferFlavors.length; ++i) {
				if (DND_LEFT.equals(transferFlavors[i]
						.getHumanPresentableName()) && comp.equals(rightList)) {
					return true;
				} else if (DND_RIGHT.equals(transferFlavors[i]
						.getHumanPresentableName()) && comp.equals(leftList)) {
					return true;
				}
			}
			return false;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			JList list = (JList) c;
			Object[] values = list.getSelectedValues();
			StringBuffer buff = new StringBuffer();
			for (int i = 0; i < values.length; i++) {
				Object val = values[i];
				buff.append(val == null ? "" : val.toString());
				if (i != values.length - 1) {
					buff.append("\n");
				}
			}
			String flavName = c.equals(leftList) ? DND_LEFT : DND_RIGHT;
			return new LBTransferable(buff.toString(), flavName);
		}

		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY_OR_MOVE;
		}

		@Override
		public boolean importData(JComponent comp, Transferable t) {
			if (comp.equals(leftList)) {
				removeSelected();
			} else if (comp.equals(rightList)) {
				addSelected();
			}
			return true;
		}

		private class LBTransferable implements Transferable {
			private String mData;
			private String mFlavorName;
			private DataFlavor[] mSupportedFlavors;

			public LBTransferable(String data, String flavorName) {
				mData = data;
				mFlavorName = flavorName;
				mSupportedFlavors = new DataFlavor[] {
						new DataFlavor(String.class, mFlavorName),
						DataFlavor.stringFlavor };
			}

			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {
				if (mFlavorName.equals(flavor.getHumanPresentableName())
						|| DataFlavor.stringFlavor == flavor) {
					return mData;
				}
				throw new UnsupportedFlavorException(flavor);
			}

			public DataFlavor[] getTransferDataFlavors() {
				return mSupportedFlavors;
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return mFlavorName.equals(flavor.getHumanPresentableName())
						|| DataFlavor.stringFlavor == flavor;
			}
		}
	}

	public void contentsChanged(ListDataEvent lde) {
		enableButtons();
	}

	public void intervalAdded(ListDataEvent lde) {
		enableButtons();
	}

	public void intervalRemoved(ListDataEvent lde) {
		enableButtons();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		btnAddAll.setEnabled(enabled);
		btnAddSelected.setEnabled(enabled);
		btnRemAll.setEnabled(enabled);
		btnRemSelected.setEnabled(enabled);
		leftList.setEnabled(enabled);
		rightList.setEnabled(enabled);
		if (enabled) {
			enableButtons();
		}
	}

	public SortedFilteredListModel<T> getLeftListModel() {
		return leftListModel;
	}

	
	public SortedFilteredListModel<T> getRightListModel() {
		return rightListModel;
	}

	public JList getLeftList() {
		return leftList;
	}

}
