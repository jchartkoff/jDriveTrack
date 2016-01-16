package jdrivetrack;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

public class SortedComboBoxModel extends DefaultComboBoxModel<Object> {

	private static final long serialVersionUID = 7682854905383418718L;

	public SortedComboBoxModel() {
        super();
    }

    public SortedComboBoxModel(Object[] items) {
        Arrays.sort(items);
        int size = items.length;
        for (int i = 0; i < size; i++) {
            super.addElement(items[i]);
        }
        if (items.length > 0) setSelectedItem(items[0]);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public SortedComboBoxModel(Vector items) {
        Collections.sort(items);
        int size = items.size();
        for (int i = 0; i < size; i++) {
            super.addElement(items.elementAt(i));
        }
        if (items.size() > 0) setSelectedItem(items.elementAt(0));
    }

    @Override
    public void addElement(Object element) {
        insertElementAt(element, 0);
    }

    @SuppressWarnings("unchecked")
	@Override
    public void insertElementAt(Object element, int index) {
        int size = getSize();
        //  Determine where to insert element to keep model in sorted order            
        for (index = 0; index < size; index++) {
            Comparable<Object> c = (Comparable<Object>) getElementAt(index);
            if (c.compareTo(element) > 0) {
                break;
            }
        }
        super.insertElementAt(element, index);
    }

}
