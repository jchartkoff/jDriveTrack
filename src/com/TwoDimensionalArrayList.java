package com;

import java.util.ArrayList;

public class TwoDimensionalArrayList<T> extends ArrayList<ArrayList<T>> {
	private static final long serialVersionUID = 1L;
	
	public TwoDimensionalArrayList(int indexSize) {
		setIndexSize(indexSize);
	}
	
	public void setIndexSize(int size) {
		while (size > this.size()) {
            this.add(new ArrayList<T>());
        }
		for (int i = size; i < this.size(); i--) {
            this.remove(i);
        }
    }

    public void addElement(int index, T element) {
        ArrayList<T> inner = this.get(index);
        inner.add(element);
    }

    public int getIndexSize() {
    	return size();
    }
    
    public int getElementSize(int index) {
    	ArrayList<T> inner = this.get(index);
    	return inner.size();
    }
    
    public T getElement(int index, int elementIndex) {
    	ArrayList<T> inner = this.get(index);
    	return inner.get(elementIndex);
    }
    
    public ArrayList<T> getElement(int index) {
    	return this.get(index);
    }
    
    public void removeElement(int index, int elementIndex) {
    	ArrayList<T> inner = this.get(index);
    	inner.remove(elementIndex);
    }
    
    public void clearAllElements(int index) {
    	ArrayList<T> inner = this.get(index);
    	inner.clear();
    }
    
    public boolean isElementEmpty(int index) {
    	ArrayList<T> inner = this.get(index);
    	return inner.isEmpty();
    }
    
    public boolean isIndexEmpty() {
    	return this.isEmpty();
    }
    
}
