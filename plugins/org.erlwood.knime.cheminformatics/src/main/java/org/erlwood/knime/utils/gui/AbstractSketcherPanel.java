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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.erlwood.knime.utils.chem.ISketcherTopPanel;
import org.erlwood.knime.utils.chem.ISketcherTopPanelCallback;

import chemaxon.marvin.beans.MolRenderer;
import chemaxon.struc.Molecule;

/**
 * Base class for sketcher panels.
 * @author Luke Bullard
 *
 */
public abstract class AbstractSketcherPanel extends JPanel {
		
	private static final long serialVersionUID = 1L;

	private MyMoleculeTableModel moleculeTableModel = new MyMoleculeTableModel();	
	private JTable moleculeTable;

	private JCheckBox cbxSplit;
	private JCheckBox cbxAddOnExit;
	private JCheckBox cbxAromatize;
	private JCheckBox cbxAddSdfColumn;
	
	private List<MoleculeListener> mListenerList = new ArrayList<MoleculeListener>();

	private boolean bAddOnExitDone;
	
	/**
	 * The Sketcher component for the left hand panel.
	 * @return Component
	 */
	protected abstract Component getLeftHandPanel();
	
	/**
	 * Callback used when a top panel is provided by extension point.
	 * @param s The entered data.
	 */
	protected abstract void onTopPanelCallback(String s);
	
	/**
	 * Gets the current molecule on the sketcher.
	 * @return The Molecule on the sketcher.
	 */
	protected abstract Molecule getMol();
	
	/**
	 * Sets a molecule on the sketcher.
	 * @param m The molecule to place on the sketcher.
	 */
	protected abstract void setMol(Molecule m);
	
	/**
	 * Constructor.
	 */
	public AbstractSketcherPanel() {
		super();		
	}

	/** 
	 * @return The list of Molecules in the list. 
	 */
	public List<Molecule> getMolecules() {
		return moleculeTableModel.getMolecules();
	}

	/**
	 * Sets the moecules into the list.
	 * @param l The list of molecules
	 */
	public void setMolecules(List<Molecule> l) {
		bAddOnExitDone = false;
		moleculeTableModel.setMolecules(l);
	}
	
	/** 
	 * @return Whether we should add an SDF column
	 */
	public boolean isAddSDFColumn() {
		return cbxAddSdfColumn.isSelected();
	}

	/**
	 * Sets whether we should add an SDF column
	 * @param b true if we add column
	 */
	public void setAddSDFColumn(boolean b) {
		cbxAddSdfColumn.setSelected(b);
	}
	
	/** 
	 * @return Whether we should aromatise molecules
	 * when added
	 */
	public boolean isAromatize() {
		return cbxAromatize.isSelected();
	}

	/**
	 * Sets whether we should aromatise molecules
	 * @param b true if we aromatise
	 */
	public void setAromatize(boolean b) {
		cbxAromatize.setSelected(b);
	}

	/** 
	 * @return Should we add the molecule on the sketcher to the list on exit ?
	 */
	public boolean isAddOnExit() {
		return !bAddOnExitDone && cbxAddOnExit.isSelected();
	}

	/**
	 * Executed when the add button is pressed.
	 */
	protected void onAdd() {		
		Molecule moll = getMol();
		
		if (moll == null) {
			return;
		}
		Molecule[] mols = moll.isReaction() || !cbxSplit.isSelected() ? new Molecule[] { moll }
				: moll.convertToFrags();
		for (Molecule mol : mols) {
			if (!mol.isEmpty()) {
				
				// aromatise the structure before adding
				if(isAromatize( )) {
					mol.aromatize();
				}
				
				moleculeTableModel.addMolecule(mol);
				fireMoleculeAdded(mol);
				
				selectAndScrollMoleculeIntoView(moleculeTableModel.getRowCount() - 1);
								
			}
		}
	}

	/**
	 * Selects a molecule in thr list and scrolls it into view.
	 * @param i The Molecule to scroll into view
	 */
	protected void selectAndScrollMoleculeIntoView(int i) {
		moleculeTable.getSelectionModel().setSelectionInterval(i, i);
		int y = i * moleculeTable.getRowHeight();
		Rectangle r = new Rectangle(0, y, 200, y + 200);				
		moleculeTable.scrollRectToVisible(r);
	}
	
	/**
	 * Executed when the delete button is pressed.
	 */
	protected void onDel() {
		int selMolIndex = moleculeTable.getSelectedRow();
		if (selMolIndex == -1) {
			return;
		}
		Molecule mol = (Molecule) moleculeTableModel.getValueAt(selMolIndex, 0);
		moleculeTableModel.removeRow(selMolIndex);
		fireMoleculeRemoved(mol);
		
		if (selMolIndex > moleculeTableModel.getRowCount() - 1) {
			selMolIndex = moleculeTableModel.getRowCount() - 1;
		}
		
		selectAndScrollMoleculeIntoView(selMolIndex);
			
	}

	/**
	 * Executed when the edit button is pressed.
	 */
	protected void onEdit() {
		int selMolIndex = moleculeTable.getSelectedRow();
		if (selMolIndex == -1) {
			return;
		}
		Molecule mol = (Molecule) moleculeTableModel.getValueAt(selMolIndex, 0);
		setMol(mol);		
	}
	
	/**
	 * Executed when the up button is pressed.
	 */
	protected void onUp() {
		int selMolIndex = moleculeTable.getSelectedRow();
		if (selMolIndex == -1) {
			return;
		}
		if (moleculeTableModel.moveUp(selMolIndex)) {
			selectAndScrollMoleculeIntoView(selMolIndex - 1);
		}
	}
	
	/**
	 * Executed when the down button is pressed.
	 */
	protected void onDown() {
		int selMolIndex = moleculeTable.getSelectedRow();
		if (selMolIndex == -1) {
			return;
		}
		if (moleculeTableModel.moveDown(selMolIndex)) {
			selectAndScrollMoleculeIntoView(selMolIndex + 1);
		}
	}
	
	/**
	 * Executed when the right button is pressed.
	 */
	protected void onRight() {
		Molecule newMol = getMol();
		int selMolIndex = moleculeTable.getSelectedRow();
		Molecule oldMol = (Molecule) moleculeTableModel.getValueAt(selMolIndex, 0);
		moleculeTableModel.setMolecule(selMolIndex, newMol);
		fireMoleculeUpdated(newMol, oldMol);
		
	}

	/**
	 * Executed when the clear button is pressed.
	 */
	protected void onClear() {
		for(Molecule m : moleculeTableModel.getMolecules()) {
			fireMoleculeRemoved(m);
		}
		moleculeTableModel.clear();
			
	}
	
	/**
	 * Initialise the GUI.
	 */
	protected void initGUI() {
		MarvinSketchNodePanel mNodePanel = new MarvinSketchNodePanel();

		mNodePanel.getBtnAdd().addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				onAdd();
			}
		});
		mNodePanel.getBtnDel().addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				onDel();
			}
		});
		mNodePanel.getBtnEdit().addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				onEdit();
			}
		});
		mNodePanel.getBtnClear().addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				onClear();
			}
		});
		
		mNodePanel.getBtnUp().addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				onUp();
			}
		});
		
		mNodePanel.getBtnDown().addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				onDown();
			}
		});
		
		mNodePanel.getBtnRight().addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				onRight();
			}
		});

		ISketcherTopPanel tp = mNodePanel.getTopPanel();
		if (tp != null) {
			tp.setCallback(new ISketcherTopPanelCallback() {
	
				@Override
				public void onCallback(Object s) {
					if (s instanceof String) {
						onTopPanelCallback((String)s);						
					}
				}
				
			});
		}
		mNodePanel.getSketchPane().add(getLeftHandPanel(), BorderLayout.CENTER);

	
		moleculeTable = new JTable(moleculeTableModel);		
		JScrollPane scr = new JScrollPane(moleculeTable);
		scr.getViewport().setBackground(Color.white);
		
		moleculeTable.setRowHeight(200);
		moleculeTable.getColumnModel().getColumn(0).setWidth(200);
		moleculeTable.getColumnModel().getColumn(0).setCellRenderer(new MyMolRenderer());
		moleculeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		mNodePanel.getBrowserPane().add(scr, BorderLayout.CENTER);

		Box pnlChxbx = new Box(BoxLayout.Y_AXIS);
		pnlChxbx.setBorder(BorderFactory.createTitledBorder("Options"));
		cbxSplit = new JCheckBox("Split multiple structures on add");
		cbxSplit.setSelected(true);
		pnlChxbx.add(cbxSplit);
		cbxAddOnExit = new JCheckBox("Add current structure on exit");
		cbxAddOnExit.setSelected(true);
		pnlChxbx.add(cbxAddOnExit);
		cbxAromatize = new JCheckBox("Aromatize structures on add");
		cbxAromatize.setSelected(true);
		pnlChxbx.add(cbxAromatize);
		
		cbxAddSdfColumn = new JCheckBox("Add SDF Column");
		cbxAddSdfColumn.setSelected(false);
		pnlChxbx.add(cbxAddSdfColumn);
		
		mNodePanel.getBrowserPane().add(pnlChxbx, BorderLayout.SOUTH);

		this.setLayout(new GridLayout());
		this.add(mNodePanel);
	}


	/**
	 * Adds the current structure on the sketcher to the list.
	 */
	public void addLastStructure() {
		if (isAddOnExit()) {
			Molecule mol = getMol();
			if (mol != null && !mol.isEmpty()) {
				Molecule[] mols = null;
				if (cbxSplit.isSelected() && !mol.isReaction()) {
					mols = mol.convertToFrags();
				} else {
					mols = new Molecule[] { mol };
				}
				boolean found = false;
				for (Molecule ml : mols) {
					String sm = ml.toFormat("smiles:0us");
					
					
					for (int c = 0; c < getMolecules().size() && !found; ++c) {
						String sm2 = getMolecules().get(c).toFormat("smiles:0us");
						found = sm.equals(sm2);
					}
					
				}
				if (!found) {
					try {
						onAdd();
						bAddOnExitDone = true;
					} catch (Exception t) {
						// ignore
					}
				}
			}
		}
	}
	
	/**
	 * Fire Molecule Added event to all the listeners.
	 */
	private void fireMoleculeAdded(Molecule mol) {
		for (MoleculeListener l : mListenerList) {
			l.moleculeAdded(mol);
		}
	}

	/**
	 * Fire Molecule Removed event to all the listeners.
	 */
	private void fireMoleculeRemoved(Molecule mol) {
		for (MoleculeListener l : mListenerList) {
			l.moleculeRemoved(mol);
		}
	}

	/**
	 * Fire Molecule Updated event to all the listeners.
	 */
	private void fireMoleculeUpdated(Molecule newMol, Molecule oldMol) {
		for (MoleculeListener l : mListenerList) {
			l.moleculeUpdated(newMol, oldMol);
		}
	}
	
	/**
	 * Adds a listener.
	 * @param listener The listener to add
	 */
	public void addMoleculeListener(MoleculeListener listener) {
		mListenerList.add(listener);

	}

	/**
	 * Removes a listener.
	 * @param listener The listener to remove
	 */
	public void removeMoleculeListener(MoleculeListener listener) {
		mListenerList.remove(listener);

	}

	/**
	 * Listener Inteface for Molecule adding, deleting and updating.
	 * @author Luke Bullard
	 *
	 */
	public interface MoleculeListener extends EventListener {
		void moleculeAdded(Molecule mol);

		void moleculeRemoved(Molecule mol);
		
		void moleculeUpdated(Molecule newMol, Molecule oldMol);
	}
	
	/**
	 * Table Mode for holding Molecules
	 * @author Luke Bullard
	 *
	 */
	@SuppressWarnings("serial")
	private class MyMoleculeTableModel extends AbstractTableModel {
		private List<Molecule> mMolecules = new ArrayList<Molecule>();
		
		
		@Override
		public int getColumnCount() {			
			return 1;
		}

		public boolean moveUp(int i) {
			if (i == 0) {
				return false;
			}
			Molecule top = mMolecules.get(i -1);
			Molecule bottom = mMolecules.get(i);
			mMolecules.set(i - 1, bottom);
			mMolecules.set(i, top);
			fireTableRowsUpdated(i - 1, i);
			return true;
		}

		public boolean moveDown(int i) {
			if (i == mMolecules.size() - 1) {
				return false;
			}
			Molecule top = mMolecules.get(i);
			Molecule bottom = mMolecules.get(i + 1);
			mMolecules.set(i + 1, top);
			mMolecules.set(i, bottom);
			fireTableRowsUpdated(i, i + 1);
			return true;
		}

		public void setMolecule(int i, Molecule moll) {
			mMolecules.set(i, moll);
			fireTableRowsUpdated(i, i);
		}

		public void removeRow(int i) {
			mMolecules.remove(i);
			fireTableRowsDeleted(i, i);
		}

		public void addMolecule(Molecule mol) {			
			mMolecules.add(mol);
			fireTableRowsInserted(mMolecules.size()-1, mMolecules.size()-1);
		}

		public void setMolecules(List<Molecule> molecules) {
			mMolecules.clear();
			mMolecules.addAll(molecules);
			fireTableDataChanged();
		}

		@Override
		public String getColumnName(int arg0) {
			return "Molecules";
		}

		@Override
		public int getRowCount() {			
			return mMolecules.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			return mMolecules.get(row);
		}
	
				
		public List<Molecule> getMolecules() {
			List<Molecule> retVal = new ArrayList<Molecule>();
			for (Molecule m : mMolecules) {
				retVal.add(m);
			}
			return retVal;
		}
		
		public void clear() {
			mMolecules.clear();
			fireTableDataChanged();
		}
	}
	
	/**
	 * Molecule Renderer handling selected outlines
	 * @author Luke Bullard
	 *
	 */
	@SuppressWarnings("serial")
	public class MyMolRenderer extends MolRenderer  {
		private boolean bIsSelected = false;
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			bIsSelected = isSelected;
			return super.getTableCellRendererComponent(table, value, false, hasFocus, row, column);						
		}
		
		@Override
		public void paintComponent(Graphics g) {			
			super.paintComponent(g);
			
			if (bIsSelected) {
				g.setColor(Color.blue);			
				Graphics2D g2 = (Graphics2D) g;
	            g2.setStroke(new BasicStroke(2));
				g.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
			}
			
		}
	}
}