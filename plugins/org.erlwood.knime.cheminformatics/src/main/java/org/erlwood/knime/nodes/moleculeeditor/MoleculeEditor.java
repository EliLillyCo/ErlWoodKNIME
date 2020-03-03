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
package org.erlwood.knime.nodes.moleculeeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.erlwood.knime.icons.IconLoader;
import org.erlwood.knime.utils.chem.MViewPaneWrapper;
import org.erlwood.knime.utils.gui.MarvinSketchNodePanel;

import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.beans.MSketchPane;
import chemaxon.marvin.beans.MViewPane;
import chemaxon.struc.Molecule;

@SuppressWarnings("serial")
public class MoleculeEditor extends JFrame {
	private static final Logger LOG = Logger.getLogger(MoleculeEditor.class.getName());
	
	private List<Molecule> rmolecules = new ArrayList<Molecule>();
	private boolean ready = false;

	public MoleculeEditor(final List<Molecule> molecules) {
		super();
		ready = false;
		final SketcherPanel editor = new SketcherPanel();
		try {
			editor.init(molecules);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		JButton done = new JButton("Done");
		done.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				rmolecules = editor.mMolecules;
				dispose();
				ready = true;
			}
		});
		setLayout(new BorderLayout());
		add(editor, BorderLayout.CENTER);
		add(done, BorderLayout.SOUTH);
		setPreferredSize(new Dimension(600, 400));
		setSize(new Dimension(600, 400));
		pack();
		setVisible(true);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent event) {
				ready = true;
				rmolecules = editor.mMolecules;
				dispose();
			}

		});
	}

	public boolean isReady() {
		return ready;
	}

	public List<Molecule> getMolecules() {
		int i = 0;
		for (Molecule mol : rmolecules) {
			StringBuilder sbr = new StringBuilder();
			for (int a = 0; a < mol.getAtomCount(); ++a) {
				if (mol.getAtom(a).isSelected()) {
					sbr.append(a + 1).append(",");
				}
			}
			int l = sbr.length();
			if (l > 0) {
				mol.setProperty("selected_atoms", sbr.substring(0, l - 1));
			}
			try {
				rmolecules.set(i, MolImporter.importMol(mol.toFormat("cssdf")));
			} catch (MolFormatException e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			}
			i++;
		}
		return rmolecules;
	}

	private class SketcherPanel extends JPanel {
		private static final String HETERO = "hetero";
		private static final String WIREFRAME = "wireframe";
		private static final long serialVersionUID = 1L;
		private static final int INITIAL_NUM_ROWS = 3;

		private MSketchPane mSketchPane;
		private MViewPane mViewPane;
		private JCheckBox cbxSplit;
		private JCheckBox cbxAddOnExit;
		private JCheckBox cbxAromatize;
		private List<Molecule> mMolecules;

		public SketcherPanel() {
			super();
			mMolecules = new ArrayList<Molecule>();
			initGUI();
		}

		public void init(final List<Molecule> molecules) throws Exception {

			mMolecules = new ArrayList<Molecule>();
			for (Molecule readMol : molecules) {
				String prop = readMol.getProperty("selected_atoms");
				if (null != prop) {
					String[] ind = prop.split(",");
					for (int a = 0; a < readMol.getAtomCount(); ++a) {
						for (int i = 0; i < ind.length; ++i) {
							if (Integer.valueOf(ind[i]) == (a + 1)) {
								readMol.getAtom(a).setSelected(true);
								break;
							}
						}
					}
				}
				mMolecules.add(readMol.cloneMoleculeWithDocument());
			}
			setViewParams(mMolecules.size());
			copyMoleculesToView(0);

		}

		private void copyMoleculesToView(int from) {
			for (int i = from; i < mMolecules.size(); ++i) {
				MViewPaneWrapper.setM(mViewPane, i, mMolecules.get(i));
				mViewPane.setL(i, Integer.toString(i + 1));
			}
		}

		private void onReplace() {
			Molecule moll = mSketchPane.getMol();
			mMolecules.set(mViewPane.getSelectedIndex(), moll);
			copyMoleculesToView(0);
		}

		private void onEdit() {
			int selMolIndex = mViewPane.getSelectedIndex();
			if (0 <= selMolIndex) {
				mSketchPane.setMol(mViewPane.getM(selMolIndex));
			}
		}

		private void setViewParams(int numRows) {
			mViewPane.setParams(
			// set total row count
					"rows=" + numRows + "\n"
							// set visible row count
							+ "visibleRows=" + 2
							+ "\n"
							// set column count
							+ "cols="
							+ 1
							+ "\n"
							// visible column count
							+ "visibleCols="
							+ 1
							+ "\n"
							// 2:1 -> 2 rows and 1 columns per cell;
							// L:0:0:1:1:c:n:0:1 -> first row and col (0:0:1:1)
							// of the
							// cell is a label (L), centered (c) ...
							// M:1:0:1:1:c:b:1:1 -> second row in the first (and
							// only)
							// column is a molecule (M), centered (c) ...
							+ "layout=:2:1:L:0:0:1:1:c:n:0:1:M:1:0:1:1:c:b:1:1\n"
							// L:10b -> label is displayed in 10pt bold fonts
							// M:200:200 -> molecule is displayed in a 200x200
							// pixels area
							+ "param=:L:10b:M:200:200\n");
			mViewPane.setBorderWidth(1);
			mViewPane.setImplicitH(HETERO);
			mViewPane.setBackground(Color.LIGHT_GRAY);
			mViewPane.setMolbg(Color.WHITE);
			mViewPane.setRendering(WIREFRAME);
		}

		private void initGUI() {
			MarvinSketchNodePanel nodePanel = new MarvinSketchNodePanel();
			nodePanel.getBtnAdd().setIcon(
					IconLoader.loadIcon("arrow_right.png"));
			nodePanel.getBtnAdd().setToolTipText("Replace selected structure");
			nodePanel.getBtnAdd().addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					onReplace();
				}
			});
			nodePanel.getBtnDel().setVisible(false);

			nodePanel.getBtnEdit().setIcon(
					IconLoader.loadIcon("arrow_left.png"));
			nodePanel.getBtnEdit().setToolTipText("Edit selected structure");
			nodePanel.getBtnEdit().addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					onEdit();
				}
			});
			nodePanel.getBtnClear().setVisible(false);

			mSketchPane = new MSketchPane();
			mSketchPane.setImplicitH(HETERO);
			mSketchPane.setBackground(Color.LIGHT_GRAY);
			mSketchPane.setMolbg(Color.WHITE);
			mSketchPane.setRendering(WIREFRAME);

			nodePanel.getSketchPane().add(mSketchPane, BorderLayout.CENTER);

			mViewPane = new MViewPane();
			mViewPane.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent arg0) {
					mViewPane.setBorderWidth(1);
					mViewPane.setImplicitH(HETERO);
					mViewPane.setBackground(Color.LIGHT_GRAY);
					mViewPane.setMolbg(Color.WHITE);
					mViewPane.setRendering(WIREFRAME);
					mViewPane.setDetachable(false);
				}
			});
			setViewParams(INITIAL_NUM_ROWS);
			mViewPane.setBorderWidth(1);
			mViewPane.setImplicitH(HETERO);
			mViewPane.setBackground(Color.LIGHT_GRAY);
			mViewPane.setMolbg(Color.WHITE);
			mViewPane.setRendering(WIREFRAME);
			mViewPane.setDetachable(false);

			nodePanel.getBrowserPane().add(mViewPane, BorderLayout.CENTER);

			Box pnlChxbx = new Box(BoxLayout.Y_AXIS);
			pnlChxbx.setBorder(BorderFactory.createTitledBorder("Options"));
			cbxSplit = new JCheckBox("Split multiple structures");
			cbxSplit.setSelected(true);
			pnlChxbx.add(cbxSplit);
			cbxAddOnExit = new JCheckBox("Add current structure on exit");
			cbxAddOnExit.setSelected(true);
			pnlChxbx.add(cbxAddOnExit);
			cbxAromatize = new JCheckBox("Aromatize structures");
			cbxAromatize.setSelected(true);
			pnlChxbx.add(cbxAromatize);
			nodePanel.getBrowserPane().add(pnlChxbx, BorderLayout.SOUTH);

			this.setLayout(new GridLayout());
			this.add(nodePanel);
		}
	}
}
