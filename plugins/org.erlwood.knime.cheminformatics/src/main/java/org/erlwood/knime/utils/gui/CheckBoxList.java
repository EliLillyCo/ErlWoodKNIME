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
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * JList which handles selection through check boxes. Adapted from various
 * sources (http://www.javafaq.nu/java-example-code-462.html,
 * http://www.jroller.com/santhosh/entry/jlist_with_checkboxes)
 * 
 * @author Dimitar Hristozov
 */
@SuppressWarnings("serial")
public class CheckBoxList extends JList implements ActionListener,
		ListDataListener {
	private static Color listForeground, listBackground;

	static {
		UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
		listForeground = uid.getColor("List.foreground");
		listBackground = uid.getColor("List.background");
	}

	private Map<Integer, Boolean> mapState = new HashMap<Integer, Boolean>();
	private Map<Integer, Boolean> mapDisabled = new HashMap<Integer, Boolean>();
	private List<ItemStateListener> lstStateLstn = new ArrayList<ItemStateListener>();
	private String valueMatch = null;

	public CheckBoxList() {
		super();
		setCellRenderer(new CheckBoxListCellRenderer());

		registerKeyboardAction(this,
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
				JComponent.WHEN_FOCUSED);
		addMouseListener(new CheckBoxListMouseAdapter());
	}

	public void addItemStateListener(ItemStateListener l) {
		lstStateLstn.add(l);
	}

	public void removeItemStateListener(ItemStateListener l) {
		lstStateLstn.remove(l);
	}

	@Override
	public void setListData(Object[] listData) {
		ListModel currentModel = getModel();
		if (null != currentModel) {
			currentModel.removeListDataListener(this);
		}
		super.setListData(listData);
		mapState.clear();
		mapDisabled.clear();
		currentModel = getModel();
		if (null != currentModel) {
			currentModel.addListDataListener(this);
		}
	}

	@Override
	public void setListData(Vector listData) {
		ListModel currentModel = getModel();
		if (null != currentModel) {
			currentModel.removeListDataListener(this);
		}
		super.setListData(listData);
		mapState.clear();
		mapDisabled.clear();
		currentModel = getModel();
		if (null != currentModel) {
			currentModel.addListDataListener(this);
		}
	}

	@Override
	public void setModel(ListModel model) {
		ListModel currentModel = getModel();
		if (null != currentModel) {
			currentModel.removeListDataListener(this);
		}
		super.setModel(model);
		mapState.clear();
		mapDisabled.clear();
		currentModel = getModel();
		if (null != currentModel) {
			currentModel.addListDataListener(this);
		}
	}

	public Integer[] getCheckedItems() {
		return mapState.keySet().toArray(new Integer[mapState.keySet().size()]);
	}

	public boolean isItemChecked(int index) {
		Integer key = index;
		return mapState.containsKey(key) && mapState.get(key);
	}

	public void checkAllItems(boolean check) {
		if (null != this.getModel()) {
			for (int i = 0; i < this.getModel().getSize(); ++i) {
				checkItem(i, check);
			}
		}
	}

	public void checkItem(int index, boolean check) {
		if (null != this.getModel() && index < this.getModel().getSize()) {
			boolean b = mapState.containsKey(index) ? mapState.get(index) != check
					: check;
			if (b) {
				mapState.put(index, check);
				repaint(getCellBounds(index, index));
				for (ItemStateListener l : lstStateLstn) {
					l.itemStateChanged(index);
				}
			}
		}
	}

	public void enableItem(int index, boolean enable) {
		if (null != this.getModel() && index < this.getModel().getSize()) {
			mapDisabled.put(index, !enable);
			repaint(getCellBounds(index, index));
		}
	}

	public boolean isItemEnabled(int index) {
		Integer key = index;
		return !mapDisabled.containsKey(key) || !mapDisabled.get(key);
	}

	public static void main(String[] args) {
		JList list = new CheckBoxList();
		DefaultListModel defModel = new DefaultListModel();
		list.setModel(defModel);
		String[] listItems = { "Chris", "Joshua", "Daniel", "Michael", "Don",
				"Kimi", "Kelly", "Keagan" };
		for (int i = 0; i < listItems.length; ++i) {
			defModel.addElement(listItems[i]);
		}
		// show list
		JScrollPane scroller = new JScrollPane(list);
		JFrame frame = new JFrame("Checkbox JList");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(scroller);
		frame.pack();
		frame.setVisible(true);
	}

	private void toggleSelection(int index) {
		if (!mapDisabled.containsKey(index) || !mapDisabled.get(index)) {
			if (mapState.containsKey(index)) {
				mapState.put(index, Boolean.valueOf(!mapState.get(index)));
			} else {
				mapState.put(index, Boolean.TRUE);
			}
			repaint(getCellBounds(index, index));
			for (ItemStateListener l : lstStateLstn) {
				l.itemStateChanged(index);
			}
		}
	}

	private class CheckBoxListCellRenderer extends JComponent implements
			ListCellRenderer {
		private DefaultListCellRenderer defaultComp;
		private JCheckBox checkbox;

		public CheckBoxListCellRenderer() {
			setLayout(new BorderLayout());
			defaultComp = new DefaultListCellRenderer();
			checkbox = new JCheckBox();
			add(checkbox, BorderLayout.WEST);
			add(defaultComp, BorderLayout.CENTER);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			defaultComp.getListCellRendererComponent(list, value, index,
					isSelected, cellHasFocus);
			checkbox.setSelected(mapState.containsKey(index)
					&& mapState.get(index));
			checkbox.setEnabled(mapDisabled.containsKey(index) ? !mapDisabled
					.get(index) : true);
			Component[] comps = getComponents();
			for (int i = 0; i < comps.length; i++) {
				comps[i].setForeground(listForeground);
				comps[i].setBackground(listBackground);
				if (!checkbox.isEnabled() && comps[i] instanceof JLabel) {
					JLabel lbl = (JLabel) comps[i];
					lbl.setForeground(Color.gray);
					Font oldFont = lbl.getFont();
					lbl.setFont(new Font(oldFont.getName(), oldFont
							.getStyle() | Font.ITALIC, oldFont.getSize()));
				} else if (valueMatch != null
						&& value.toString().equals(valueMatch)
						&& comps[i] instanceof JLabel) {
					JLabel lbl = (JLabel) comps[i];
					lbl.setBackground(Color.yellow);
				} else {
					comps[i].setBackground(listBackground);
				}
			}
			return this;
		}
	}

	private class CheckBoxListMouseAdapter extends MouseAdapter {

		public void mouseClicked(MouseEvent me) {
			int index = locationToIndex(me.getPoint());
			if (index >= 0) {
				toggleSelection(index);
			}
		}
	}

	public void actionPerformed(ActionEvent event) {
		toggleSelection(getSelectedIndex());
	}

	public void contentsChanged(ListDataEvent event) {
		// we can ignore this as of now
	}

	public void intervalAdded(ListDataEvent event) {
		int st = event.getIndex0() < event.getIndex1() ? event.getIndex0()
				: event.getIndex1();
		int end = event.getIndex0() < event.getIndex1() ? event.getIndex1()
				: event.getIndex0();
		int numAdded = end - st + 1;
		for (int i = getModel().getSize() - 1; i >= st; --i) {
			if (mapState.containsKey(i)) {
				mapState.put(i + numAdded, mapState.get(i));
				mapState.remove(i);
			}
			if (mapDisabled.containsKey(i)) {
				mapDisabled.put(i + numAdded, mapState.get(i));
				mapDisabled.remove(i);
			}
		}
	}

	public void intervalRemoved(ListDataEvent event) {
		int st = event.getIndex0() < event.getIndex1() ? event.getIndex0()
				: event.getIndex1();
		int end = event.getIndex0() < event.getIndex1() ? event.getIndex1()
				: event.getIndex0();
		for (int i = end; i >= st; --i) {
			if (mapState.containsKey(i)) {
				mapState.remove(i);
			}
			if (mapDisabled.containsKey(i)) {
				mapDisabled.remove(i);
			}
		}
	}

	public interface ItemStateListener {
		void itemStateChanged(int index);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (null != getModel()) {
			for (int m = 0; m < getModel().getSize(); ++m) {
				enableItem(m, enabled);
			}
		}
	}

	public void setValueMatch(String match) {
		valueMatch = match;
	}
}
