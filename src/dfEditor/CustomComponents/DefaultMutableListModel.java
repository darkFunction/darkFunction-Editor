package dfEditor.CustomComponents;

import javax.swing.*;


// @author Santhosh Kumar T - santhosh@in.fiorano.com 
public class DefaultMutableListModel extends DefaultListModel implements MutableListModel{ 
    public boolean isCellEditable(int index){ 
        return true; 
    } 
 
    public void setValueAt(Object value, int index){ 
        super.setElementAt(value, index); 
    } 
} 