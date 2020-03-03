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
package org.erlwood.knime.nodes.xlswriter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Adapted from class RenameNodeDialogPane written by Bernd Wiswedel, University of Konstanz
 * @author Nikolas Fechner, Dimitar Hristozov
 */

@SuppressWarnings("serial")
public class ColourPanel extends JPanel 
{

	/**
	 * Panel containing for each column in the input table a row. (with JLabel
	 * "old name", checkbox to set a new name, text field for the new name, and
	 * a combo box where the user can pick a compatible type)
	 */
	private DoubleColumnSettings[] mColSettings;
	private StringColumnSettings[] sColSettings;

	/**
	 * Constructs new dialog, inits members.
	 */
	public ColourPanel() {
		super(new GridLayout(0, 1));
	}



	/**
	 * Adds one row to m_panel.
	 * 
	 * @param colSet the settings for the column
	 * @param labelWidth the width to be used for the column's name
	 */
	void addPanelFor(final DoubleColumnSettings colSet,
			final int labelWidth) {
		final String oldColName = colSet.getName();


		final JLabel nameLabel = new JLabel(oldColName);
		int labelHeight = nameLabel.getPreferredSize().height;
		nameLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
		nameLabel.setMinimumSize(new Dimension(labelWidth, labelHeight));

		String lowtext = ""+Double.NEGATIVE_INFINITY;
		
		lowtext = "" + colSet.getLowerBound();
		final JTextField lowfield = new JTextField(lowtext, 8);
		String uptext = ""+Double.POSITIVE_INFINITY;
		
		uptext = "" + colSet.getUpperBound();
		final JTextField upfield = new JTextField(uptext, 8);

		// add listeners to all fields that can change: They will update their
		// RenameColumnSetting immediately
		lowfield.addFocusListener(new FocusListener() {
			public void focusGained(final FocusEvent e) {
			}

			public void focusLost(final FocusEvent e) {
				String newText = lowfield.getText();
				colSet.setLowerBound(Double.parseDouble(newText));
			}
		});
		upfield.addFocusListener(new FocusListener() {
			public void focusGained(final FocusEvent e) {
			}

			public void focusLost(final FocusEvent e) {
				String newText = upfield.getText();
				colSet.setUpperBound(Double.parseDouble(newText));
			}
		});
		final JCheckBox checker = new JCheckBox("Colour: ");
		final JCheckBox invert = new JCheckBox("Invert");
		checker.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				if (checker.isSelected()) {
					lowfield.setEnabled(true);
					upfield.setEnabled(true);
					invert.setEnabled(true);
					lowfield.requestFocus();
					colSet.setActive(true);
				} else {
					
					lowfield.setEnabled(false);
					
					upfield.setEnabled(false);
					colSet.setActive(false);
					invert.setEnabled(false);
				}
			}
		});
		checker.setSelected(colSet.isActive());
		lowfield.setEnabled(colSet.isActive());
		upfield.setEnabled(colSet.isActive());

		invert.setEnabled(colSet.isActive());
		invert.setSelected(colSet.isInverted());
		JPanel oneRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		oneRow.add(nameLabel);
		oneRow.add(checker);
		oneRow.add(invert);
		final JLabel green = new JLabel("               ");
		green.setOpaque(true);
		green.setBackground(Color.green);
		green.setSize(labelHeight, 3*labelHeight);
		oneRow.add(green);
		final JLabel smaller1 = new JLabel(" <= ");        
	
		oneRow.add(smaller1);
		oneRow.add(lowfield);
		final JLabel smaller2 = new JLabel(" < ");
		
		oneRow.add(smaller2);
		final JLabel yellow = new JLabel("               ");
		yellow.setOpaque(true);
		yellow.setBackground(Color.yellow);
		yellow.setSize(labelHeight, 3*labelHeight);
		oneRow.add(yellow);
		final JLabel smaller3 = new JLabel(" < ");
		
		oneRow.add(smaller3);
		oneRow.add(upfield);
		final JLabel smaller4 = new JLabel(" <= ");
		
		oneRow.add(smaller4);
		final JLabel red = new JLabel("               ");
		red.setOpaque(true);
		red.setBackground(Color.red);
		red.setSize(10, 30);
		oneRow.add(red);

		this.add(oneRow);

		if (invert.isSelected()) {
			red.setBackground(Color.green);
			green.setBackground(Color.red);
			colSet.setInverted(true);
		} else {
			red.setBackground(Color.red);
			green.setBackground(Color.green);
			colSet.setInverted(false);
		}

		invert.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				if (invert.isSelected()) {
					red.setBackground(Color.green);
					green.setBackground(Color.red);
					colSet.setInverted(true);
				} else {
					red.setBackground(Color.red);
					green.setBackground(Color.green);
					colSet.setInverted(false);
				}
			}
		});
	}

	void addPanelFor(final StringColumnSettings colSet,
			final int labelWidth)
	{
		final String oldColName = colSet.getName();
		final JLabel nameLabel = new JLabel(oldColName);
		int labelHeight = nameLabel.getPreferredSize().height;
		nameLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
		nameLabel.setMinimumSize(new Dimension(labelWidth, labelHeight));

	
		final JTextField field1 = new JTextField("" + colSet.getQuery(), 8);
		field1.addFocusListener(new FocusListener() {
			public void focusGained(final FocusEvent e) {
			}

			public void focusLost(final FocusEvent e) {
				String newText = field1.getText();
				colSet.setQuery(1, newText);
			}
		});
		final JTextField field2 = new JTextField("" + colSet.getQuery2(), 8);
		field2.addFocusListener(new FocusListener() {
			public void focusGained(final FocusEvent e) {
			}

			public void focusLost(final FocusEvent e) {
				String newText = field2.getText();
				colSet.setQuery(2,newText);
			}
		});
		final JTextField field3 = new JTextField("" + colSet.getQuery3(), 8);
		field3.addFocusListener(new FocusListener() {
			public void focusGained(final FocusEvent e) {
			}

			public void focusLost(final FocusEvent e) {
				String newText = field3.getText();
				colSet.setQuery(3,newText);
			}
		});

		final JCheckBox checker = new JCheckBox("Colour: ");
		
		checker.setSelected(colSet.isSet());
		field1.setEnabled(colSet.isSet());
		field2.setEnabled(colSet.isSet());
		field3.setEnabled(colSet.isSet());

		JPanel oneRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		oneRow.add(nameLabel);
		oneRow.add(checker);

		oneRow.add(field1);
		final JLabel smaller1 = new JLabel(" -> ");        
		oneRow.add(smaller1);
		final JComboBox box1 = getColorSelectionBox();
		box1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				Color col = (Color) box1.getSelectedItem();
				colSet.setColorX(1, col);
			}
		}
		);
		box1.setSelectedIndex(colSet.getColorIndex(colSet.getColorX(1)));
		oneRow.add(box1);

		oneRow.add(field2);
		final JLabel smaller2 = new JLabel(" -> ");        
		oneRow.add(smaller2);
		final JComboBox box2 = getColorSelectionBox();
		box2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				Color col = (Color) box2.getSelectedItem();
				colSet.setColorX(2, col);
			}
		}
		);
		box2.setSelectedIndex(colSet.getColorIndex(colSet.getColorX(2)));
		oneRow.add(box2);

		oneRow.add(field3);
		final JLabel smaller3 = new JLabel(" -> ");        
		oneRow.add(smaller3);
		final JComboBox box3 = getColorSelectionBox();
		box3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				Color col = (Color) box3.getSelectedItem();
				colSet.setColorX(3, col);
			}
		}
		);
		box3.setSelectedIndex(colSet.getColorIndex(colSet.getColorX(3)));
		oneRow.add(box3);

		final JLabel smaller4 = new JLabel("default -> ");        
		oneRow.add(smaller4);
		final JComboBox box4 = getColorSelectionBox();
		box4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				Color col = (Color) box4.getSelectedItem();
				colSet.setColorX(4, col);
			}
		}
		);
		box4.setSelectedIndex(colSet.getColorIndex(colSet.getColorX(4)));
		oneRow.add(box4);

		checker.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				if (checker.isSelected()) {
					field1.setEnabled(true);
					field2.setEnabled(true);
					field3.setEnabled(true);
					box1.setEnabled(true);
					box2.setEnabled(true);
					box3.setEnabled(true);
					box4.setEnabled(true);
					colSet.setSet(true);
				} else {
					field1.setEnabled(false);
					field2.setEnabled(false);
					field3.setEnabled(false);
					box1.setEnabled(false);
					box2.setEnabled(false);
					box3.setEnabled(false);
					box4.setEnabled(false);
					colSet.setSet(false);
				}
			}
		});
		
		this.add(oneRow);     

	}

	void addOldPanelFor(final StringColumnSettings colSet,
			final int labelWidth) {
		final String oldColName = colSet.getName();


		final JLabel nameLabel = new JLabel(oldColName);
		int labelHeight = nameLabel.getPreferredSize().height;
		nameLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
		nameLabel.setMinimumSize(new Dimension(labelWidth, labelHeight));

		String qtext = ""+Double.NEGATIVE_INFINITY;
		qtext = "" + colSet.getQuery();
		final JTextField qfield = new JTextField(qtext, 8);
		// add listeners to all fields that can change: They will update their
		// RenameColumnSetting immediately
		qfield.addFocusListener(new FocusListener() {
			public void focusGained(final FocusEvent e) {
			}

			public void focusLost(final FocusEvent e) {
				String newText = qfield.getText();
				colSet.setQuery(newText);
			}
		});

		final JCheckBox checker = new JCheckBox("Colour: ");
		final JCheckBox invert = new JCheckBox("Invert");
		checker.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				if (checker.isSelected()) {
					qfield.setEnabled(true);
					invert.setEnabled(true);
					colSet.setSet(true);
				} else {
					qfield.setEnabled(false);
					colSet.setSet(false);
					invert.setEnabled(false);
				}
			}
		});
		checker.setSelected(colSet.isSet());
		qfield.setEnabled(colSet.isSet());

		invert.setEnabled(colSet.isSet());
		invert.setSelected(colSet.isInvert());
		JPanel oneRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		oneRow.add(nameLabel);
		oneRow.add(checker);
		oneRow.add(invert);

		final JLabel smaller1 = new JLabel(", if cell-content matches ");        
		oneRow.add(smaller1);
		oneRow.add(qfield);
		final JLabel smaller2 = new JLabel(", otherwise ");
		oneRow.add(smaller2);

		this.add(oneRow);

		final JLabel green = new JLabel("               ");
		green.setOpaque(true);
		green.setBackground(Color.green);
		green.setSize(10, 30);

		final JLabel red = new JLabel("               ");
		red.setOpaque(true);
		red.setBackground(Color.red);
		red.setSize(10, 30);

		if (invert.isSelected()) {
			red.setBackground(Color.green);
			green.setBackground(Color.red);
			colSet.setInvert(true);
		} else {
			red.setBackground(Color.red);
			green.setBackground(Color.green);
			colSet.setInvert(false);
		}

		invert.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				if (invert.isSelected()) {
					red.setBackground(Color.green);
					green.setBackground(Color.red);
					colSet.setInvert(true);
				} else {
					red.setBackground(Color.red);
					green.setBackground(Color.green);
					colSet.setInvert(false);
				}
			}
		});
	}

	private JComboBox getColorSelectionBox()
	{
	JComboBox box = new JComboBox(new Color[]{Color.white,Color.red,Color.yellow,Color.green,Color.blue});
		box.setRenderer(new DefaultListCellRenderer() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void setBackground(Color col) 
			{} 

			public void setMyBackground(Color col) 
			{ 
				super.setBackground(col); 
			} 

			public Component getListCellRendererComponent(JList list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				setOpaque(true);
				if (value instanceof Color) {
					setBackground((Color) value);
					setForeground((Color) value);
					setMyBackground((Color) value);
				}
				setText("  ");
				setPreferredSize(new Dimension(30,10));

				return this;
			}
		}
		);
		return box;
	}
	public void setDoubleColSettings(DoubleColumnSettings[] mColSettings) {
		this.mColSettings = mColSettings;
	}



	public void setStringColSettings(StringColumnSettings[] sColSettings) {
		this.sColSettings = sColSettings;
	}



	public DoubleColumnSettings[] getDoubleColSettings() {
		return mColSettings;
	}



	public StringColumnSettings[] getStringColSettings() {
		return sColSettings;
	}

}
