package dfEditor.CustomComponents;

import javax.swing.*;

// @author Santhosh Kumar T - santhosh@in.fiorano.com 
public interface MutableListModel<T> extends ListModel<T> {
    public boolean isCellEditable(int index); 
//    public void setValueAt(Object value, int index); 
} 