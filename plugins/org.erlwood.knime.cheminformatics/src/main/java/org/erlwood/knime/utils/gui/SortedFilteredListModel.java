package org.erlwood.knime.utils.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;

/**
 * Sorted and Filtered List model for use in the DualListboxBean.
 * @author Luke Bullard
 * 
 */
@SuppressWarnings("serial")
public class SortedFilteredListModel<T extends Comparable<? super T>> extends DefaultListModel<T> implements Iterable<T>{

	private final Vector<T> originalList = new Vector<T>();
	private final Vector<T> filteredList = new Vector<T>();	
	private String lastFilterText = null;
	
	/**
	 * Constructor.
	 */
	public SortedFilteredListModel() {		
	}

	/**
	 * Filters the list based on the text given. Will filter in a case in-sensitive manner
	 * with matching occurring if the given text occurs anywhere in the list.
	 * @param text The text to filter on.
	 */
	public synchronized void filter(String text) {
		
		final String lCaseText = text == null ? null : text.toLowerCase();
		
								
		final Vector<T> tmpVector = new Vector<T>();
		
		for (T obj : originalList) {			
			if (lCaseText == null || obj.toString().toLowerCase().contains(lCaseText)) {
				tmpVector.add(obj);
			}
		}
		
																	
		filteredList.clear();
		filteredList.addAll(tmpVector);
		lastFilterText = lCaseText;								
		fireContentsChanged(this, 0, filteredList.size());								
	
		
	}
	
	private synchronized void resetList() {		
		filteredList.clear();
		filteredList.addAll(originalList);
	}
	
	/**
     * Removes from this List all of its elements that are contained in the
     * specified Collection.
     *
     * @param lst a collection of elements to be removed from the List
     */
	public synchronized void removeAll(Collection<String> lst) {
		originalList.removeAll(lst);
		filter(lastFilterText);			
	}
	
	
	 /**
     * Appends all of the elements in the specified Collection to the end of
     * this List, in the order that they are returned by the specified
     * Collection's Iterator.  
     *
     * @param obj elements to be inserted into this List
     */
	public synchronized void addAll(Collection<T> obj) {
		originalList.addAll(obj);
		Collections.sort(originalList);
		filter(lastFilterText);		
	}
	
	/**
     * Returns the String at the specified index of the underliying (not filtered) list.
     *
     * <p>This method is identical in functionality to the {@link #get(int)}
     * method (which is part of the {@link List} interface).
     *
     * @param      index   an index into this vector
     * @return     the String at the specified index
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     */
	public synchronized T getOriginal(int i) {
		return originalList.elementAt(i);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int getSize() {
		return filteredList.size();
	}
	
	public synchronized int getOriginalSize() {
		return originalList.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized T getElementAt(int i) {		
		return filteredList.get(i);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void copyInto(Object aobj[]) {
		filteredList.copyInto(aobj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void trimToSize() {
		filteredList.trimToSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void ensureCapacity(int i) {
		filteredList.ensureCapacity(i);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void setSize(int i) {
		int j = filteredList.size();
		filteredList.setSize(i);
		if (j > i)
			fireIntervalRemoved(this, i, j - 1);
		else if (j < i)
			fireIntervalAdded(this, j, i - 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int capacity() {
		return filteredList.capacity();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int size() {
		return filteredList.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean isEmpty() {
		return filteredList.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Enumeration elements() {
		return filteredList.elements();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean contains(Object obj) {
		return filteredList.contains(obj);
	}
	
	
	public synchronized boolean containsOriginal(Object obj) {
		return originalList.contains(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int indexOf(Object obj) {
		return filteredList.indexOf(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int indexOf(Object obj, int i) {
		return filteredList.indexOf(obj, i);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int lastIndexOf(Object obj) {
		return filteredList.lastIndexOf(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int lastIndexOf(Object obj, int i) {
		return filteredList.lastIndexOf(obj, i);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized T elementAt(int i) {
		return filteredList.elementAt(i);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized T firstElement() {
		return filteredList.firstElement();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized T lastElement() {
		return filteredList.lastElement();
	}

	/**
	 * Modification methods..
	 */
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void setElementAt(T obj, int i) {
		originalList.setElementAt(obj, i);		
		Collections.sort(originalList);
		filter(lastFilterText);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void removeElementAt(int i) {
		originalList.removeElementAt(i);
		filter(lastFilterText);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void insertElementAt(T obj, int i) {
		originalList.insertElementAt(obj, i);
		Collections.sort(originalList);
		filter(lastFilterText);	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void addElement(T obj) {
		
		originalList.addElement(obj);
		Collections.sort(originalList);
		filter(lastFilterText);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean removeElement(Object obj) {
		int i = indexOf(obj);
		boolean flag = originalList.removeElement(obj);
		if (i >= 0) {
			filter(lastFilterText);			
		}
		return flag;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void removeAllElements() {
		int i = originalList.size() - 1;
		originalList.removeAllElements();
		if (i >= 0) {
			resetList();
			fireIntervalRemoved(this, 0, i);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized String toString() {
		return originalList.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Object[] toArray() {
		Object aobj[] = new Object[originalList.size()];
		originalList.copyInto(aobj);
		return aobj;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized T get(int i) {
		return filteredList.elementAt(i);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized T set(int i, T obj) {
		T obj1 = originalList.elementAt(i);
		originalList.setElementAt(obj, i);
		Collections.sort(originalList);
		filter(lastFilterText);
		
		return obj1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void add(int i, T obj) {
		originalList.insertElementAt(obj, i);
		Collections.sort(originalList);
		filter(lastFilterText);
	}	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized T remove(int i) {
		T obj = filteredList.elementAt(i);
		originalList.remove(obj);
		filter(lastFilterText);	
		return obj;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void clear() {
		int i = originalList.size() - 1;
		originalList.removeAllElements();
		if (i >= 0) {
			resetList();
			fireIntervalRemoved(this, 0, i);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void removeRange(int i, int j) {
		if (i > j)
			throw new IllegalArgumentException("fromIndex must be <= toIndex");
		for (int k = j; k >= i; k--) {
			T objectToRemove = filteredList.get(i);
			originalList.removeElement(objectToRemove);
		}
		filter(lastFilterText);	
	}

	public synchronized List<T> getList() {
		return filteredList;
	}

	@Override
	public synchronized Iterator<T> iterator() {
		return filteredList.iterator();
	}
	
	public synchronized Iterator<T> originaIterator() {
		return originalList.iterator();
	}
}
