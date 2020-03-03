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
import java.awt.Component;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import org.erlwood.knime.icons.IconLoader;
import org.erlwood.knime.utils.chem.ISketcherTopPanel;

/**
 * This is a panel which contains MarvinSketch instance on the left and 
 * additional set of controls on the left.
 */
@SuppressWarnings("serial")
public class MarvinSketchNodePanel extends JPanel
{	
	private static final Logger LOG =  Logger.getLogger(MarvinSketchNodePanel.class.getName());
	private JSplitPane mSplitter;
	private JPanel mBrowserButtonsPane;	
	
	private JPanel sketchPane;
	private JButton btnClear;
	private JButton btnEdit;
	private JButton btnDel;
	private JButton btnAdd;
	private JButton btnUp;
	private JButton btnDown;
	private JButton btnRight;
	private JPanel browserPane;

	private ISketcherTopPanel topPanel = null;
	
	
	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.getContentPane().add(new MarvinSketchNodePanel());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public MarvinSketchNodePanel()
	{
		super();
		initGUI();
	}
	
	private void initGUI()
	{
		try {
			setPreferredSize(new Dimension(800, 600));
			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			installTopPanel();
			
			{
				mSplitter = new JSplitPane();
				this.add(mSplitter, BorderLayout.CENTER);
				mSplitter.setPreferredSize(new java.awt.Dimension(640, 480));
				mSplitter.setResizeWeight(1.0);
				mSplitter.setDividerLocation(600);
				{
					setBrowserPane(new JPanel());
					BorderLayout sketchPaneLayout = new BorderLayout();
					getBrowserPane().setLayout(sketchPaneLayout);
					mSplitter.add(getBrowserPane(), JSplitPane.RIGHT);
					{
						mBrowserButtonsPane = new JPanel();
						BoxLayout browserButtonsLayout = new BoxLayout(
							mBrowserButtonsPane, javax.swing.BoxLayout.Y_AXIS);
						getBrowserPane().add(mBrowserButtonsPane, BorderLayout.WEST);
						mBrowserButtonsPane.setLayout(browserButtonsLayout);
						mBrowserButtonsPane.setOpaque(false);
						mBrowserButtonsPane.setEnabled(false);
						Dimension spacerDimension = new Dimension(0, 3);
						mBrowserButtonsPane.add(
							javax.swing.Box.createRigidArea(spacerDimension));
						java.awt.Dimension btnsDim = new java.awt.Dimension(32, 24);
						{
							setBtnAdd(new JButton());
							mBrowserButtonsPane.add(getBtnAdd());
							setButtonSize(getBtnAdd(), btnsDim);
														
							getBtnAdd().setIcon(IconLoader.loadIcon("add.png"));
							getBtnAdd().setToolTipText("Add current structure from the editor");
						}
						mBrowserButtonsPane.add(javax.swing.Box.createRigidArea(spacerDimension));
						{
							setBtnDel(new JButton());
							mBrowserButtonsPane.add(getBtnDel());
							setButtonSize(getBtnDel(), btnsDim);
							
							getBtnDel().setIcon(IconLoader.loadIcon("delete.png"));
							getBtnDel().setToolTipText("Delete selected structure");
						}
						mBrowserButtonsPane.add(javax.swing.Box.createRigidArea(spacerDimension));
						{
							setBtnEdit(new JButton());
							mBrowserButtonsPane.add(getBtnEdit());
							setButtonSize(getBtnEdit(), btnsDim);
							
							getBtnEdit().setIcon(IconLoader.loadIcon("page_white_edit.png"));
							getBtnEdit().setToolTipText("Edit selected structure");
						}
						mBrowserButtonsPane.add(javax.swing.Box.createRigidArea(spacerDimension));
						{
							setBtnClear(new JButton());
							mBrowserButtonsPane.add(getBtnClear());
							setButtonSize(getBtnClear(), btnsDim);
							
							getBtnClear().setIcon(IconLoader.loadIcon("page_white_del.png"));
							getBtnClear().setToolTipText("Remove all structures");
						}
						mBrowserButtonsPane.add(javax.swing.Box.createRigidArea(spacerDimension));
						{
							setBtnUp(new JButton());
							mBrowserButtonsPane.add(getBtnUp());
							setButtonSize(getBtnUp(), btnsDim);
							ImageIcon ii = IconLoader.loadIcon("arrow_up.png");
							getBtnUp().setIcon(ii);
							getBtnUp().setToolTipText("Move Up");
						}
						mBrowserButtonsPane.add(javax.swing.Box.createRigidArea(spacerDimension));
						{
							setBtnDown(new JButton());
							mBrowserButtonsPane.add(getBtnDown());
							setButtonSize(getBtnDown(), btnsDim);
							
							getBtnDown().setIcon(IconLoader.loadIcon("arrow_down.png"));
							getBtnDown().setToolTipText("Move Down");
						}
						mBrowserButtonsPane.add(javax.swing.Box.createRigidArea(spacerDimension));
						{
							setBtnRight(new JButton());
							mBrowserButtonsPane.add(getBtnRight());
							setButtonSize(getBtnRight(), btnsDim);
							
							getBtnRight().setIcon(IconLoader.loadIcon("arrow_right.png"));
							getBtnRight().setToolTipText("Set modified molecule into the list");
						}
					}
				}
				{
					sketchPane = new JPanel();
					BorderLayout browserPaneLayout = new BorderLayout();
					sketchPane.setLayout(browserPaneLayout);
					mSplitter.add(sketchPane, JSplitPane.LEFT);
				}
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private void setButtonSize(JButton b, Dimension d) {
		b.setPreferredSize(d);
		b.setSize(d);
		b.setMinimumSize(d);
		b.setMaximumSize(d);
	}
	
	/**
	 * Works out whether to install the top panel.
	 * THis MUST be done via reflection so we can build the Multi-Mol Sketcher applet. 
	 */
	private void installTopPanel(){
		Class<?> platformClass = null;
		Class<?> extensionRegistryClass = null;
		Class<?> configElementClass = null;
		
		try {
		
			platformClass = Class.forName("org.eclipse.core.runtime.Platform");
			extensionRegistryClass = Class.forName("org.eclipse.core.runtime.IExtensionRegistry");
			configElementClass = Class.forName("org.eclipse.core.runtime.IConfigurationElement");
			
			// Get IExtensionRegistry
			Object reg = platformClass.getMethod("getExtensionRegistry", new Class<?>[0]).invoke(platformClass, new Object[0]);
			 
			//IExtensionRegistry
			
			//	Get the IConfigurationElement[]
			Object[] configElements = (Object[])extensionRegistryClass.getMethod("getConfigurationElementsFor", new Class<?>[] {String.class}).invoke(reg, new Object[] { "org.erlwood.knime.cheminformatics.sketchertoppanel" });
			
			//	Add contributed panel if required
			for (Object element : configElements) {
				
				//IConfigurationElement
				
				topPanel = null;
				
				try {
					topPanel = (ISketcherTopPanel) configElementClass.getMethod("createExecutableExtension", new Class<?>[] {String.class}).invoke(element, new Object[] {"class"});
				} catch (Exception e) {
					LOG.log(Level.SEVERE, e.getMessage(), e);
				}
				if (topPanel != null) {
					Component c = topPanel.getComponent();
					if (c != null) {
						add(c, BorderLayout.NORTH);
					}
				}
			}
		
		} catch(Exception ex) {
			//	Do nothing...
			LOG.log(Level.FINE, ex.getMessage(), ex);
		}
	}
	public JPanel getBrowserPane() {
		return browserPane;
	}

	public void setBrowserPane(JPanel browserPane) {
		this.browserPane = browserPane;
	}

	public JButton getBtnAdd() {
		return btnAdd;
	}

	public void setBtnAdd(JButton btnAdd) {
		this.btnAdd = btnAdd;
	}

	public JButton getBtnDel() {
		return btnDel;
	}

	public void setBtnDel(JButton btnDel) {
		this.btnDel = btnDel;
	}

	public JButton getBtnEdit() {
		return btnEdit;
	}

	public void setBtnEdit(JButton btnEdit) {
		this.btnEdit = btnEdit;
	}

	public JButton getBtnClear() {
		return btnClear;
	}

	public void setBtnClear(JButton btnClear) {
		this.btnClear = btnClear;
	}

	public JButton getBtnUp() {
		return btnUp;
	}

	public void setBtnUp(JButton b) {
		this.btnUp = b;
	}
	
	public JButton getBtnDown() {
		return btnDown;
	}

	public void setBtnDown(JButton b) {
		this.btnDown = b;
	}
	
	public JButton getBtnRight() {
		return btnRight;
	}

	public void setBtnRight(JButton b) {
		this.btnRight = b;
	}
	
	public JPanel getSketchPane() {
		return sketchPane;
	}
	
	public ISketcherTopPanel getTopPanel() {
		return topPanel;
	}
	
}
