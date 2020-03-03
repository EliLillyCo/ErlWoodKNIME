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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * Class used to provide better UI for categorised enumerations.
 * @author Luke Bullard
 *
 * @param <E> An Enumeration. Provide a String getCategory() method on the enum to enable categorisation.
 */
@SuppressWarnings("serial")
public abstract class AbstractMethodSelectionComboBox<E extends Enum<E>> extends JComboBox<Object> {
	private final Class<E> typeOfT;
	
	protected abstract void itemSelected(E item);
	
	@SuppressWarnings("unchecked")
	public AbstractMethodSelectionComboBox() {
		setRenderer(new ComboRenderer());
		
		this.typeOfT = (Class<E>)
	            ((ParameterizedType)getClass()
	            .getGenericSuperclass())
	            .getActualTypeArguments()[0];
		
		Method m = null;
		try {
			m = typeOfT.getDeclaredMethod("getCategory", new Class<?>[0]);
			m.setAccessible(true);
		} catch (Exception ex) {
			//	Do nothing
		}
		
		E[] enumArray = typeOfT.getEnumConstants();
		
		
		Map<String, List<E>> categoryMap = new TreeMap<String, List<E>>();
		
		//	Create the indexing..
		for (E e : enumArray) {
			String category = "";
			if (m != null) {
				try {
					category = (String)m.invoke(e, new Object[0]);
				} catch (Exception ex1) {
					// Do nothing					
				}
			}
			
			List<E> categoryValues  = categoryMap.get(category);
			if (categoryValues == null) {
				categoryValues = new ArrayList<E>();
				categoryMap.put(category, categoryValues);
			}
			
			categoryValues.add(e);
		}
		
		//	Ok, we now have a map of category names with associated enum values
		//	so create our actual list for the combo..
		List<Object> items = new ArrayList<Object>();
		for (Entry<String, List<E>> es : categoryMap.entrySet()) {
			String cat = es.getKey();
			if (!cat.isEmpty()) {
				items.add(cat);
			}
			for (E e : es.getValue()) {
				items.add(e);
			}
		}
		
		//	Now create the model and set it..
		DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<Object>(items.toArray(new Object[0]));
		super.setModel(model);
		
		//	Add the default lister to handle selecting a category label.
		addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                	//	If we selected a String category then bump to the next entry which will
                	//	be an enum value.
                	if (getSelectedItem() instanceof String) {
                		setSelectedIndex(getSelectedIndex() + 1);
                		return;
                	}                	 
                	itemSelected((E)getSelectedItem());
                }                
            }
        });
	}

	public void setSelectedItem(E item) {
		super.setSelectedItem(item);
	}
	
	/**
	 * Custom renderer for the ComboBox.
	 * @author Luke Bullard
	 *
	 */
	private class ComboRenderer extends DefaultListCellRenderer {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
			
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
						
			if (value instanceof String) {
				setFont(getFont().deriveFont(Font.BOLD));
				setBorder(BorderFactory.createEmptyBorder());
			} else {
				setFont(getFont().deriveFont(Font.PLAIN));
				setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 1));
			}
			return c;
		}
		
	}
}
