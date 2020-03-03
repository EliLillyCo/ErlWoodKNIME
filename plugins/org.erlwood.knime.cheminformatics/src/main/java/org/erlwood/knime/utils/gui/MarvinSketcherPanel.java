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

import java.awt.Color;
import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.NodeLogger;

import chemaxon.formats.MolImporter;
import chemaxon.marvin.beans.MSketchPane;
import chemaxon.struc.Molecule;

/**
 * Sketcher panel for Marvin.
 * @author Luke Bullard
 *
 */
@SuppressWarnings("serial")
public class MarvinSketcherPanel extends AbstractSketcherPanel {
	private static final NodeLogger LOG   = NodeLogger.getLogger(MarvinSketcherPanel.class);
	
	private final MSketchPane mSketchPane;
	
	public MarvinSketcherPanel() {
		mSketchPane = new MSketchPane();
		mSketchPane.setImplicitH("hetero");
		mSketchPane.setBackground(Color.LIGHT_GRAY);
		mSketchPane.setMolbg(Color.WHITE);
		mSketchPane.setRendering("wireframe");
		initGUI();
	}
	
	public void initFromSDString(final String cssdf) throws Exception {
		if (null == cssdf || "".equals(cssdf)) {
			onClear();
		} else {
			byte[] bytearray = cssdf.getBytes("ISO-8859-1");
			ByteArrayInputStream baos = new ByteArrayInputStream(bytearray);
			MolImporter importer = new MolImporter(baos, "cssdf");
			List<Molecule> molecules = new ArrayList<Molecule>();
			for (Molecule readMol = importer.read(); null != readMol; readMol = importer
					.read()) {
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
				molecules.add(readMol);
			}
			setMolecules(molecules);
			selectAndScrollMoleculeIntoView(molecules.size() - 1);
			
			LOG.info("SketcherPanel num mols = " + getMolecules().size());
			
		}
	}
	
	
	public String getSDString() {
		StringBuffer buff = new StringBuffer();
		for (Molecule mol : getMolecules()) {
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
			buff.append(mol.toFormat("cssdf"));
		}
		return buff.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Molecule getMol() {
		return mSketchPane.getMol();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Component getLeftHandPanel() {
		return mSketchPane;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onTopPanelCallback(String s) {
		mSketchPane.setMol(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setMol(Molecule m) {
		mSketchPane.setMol(m);
	}
	
	
}
