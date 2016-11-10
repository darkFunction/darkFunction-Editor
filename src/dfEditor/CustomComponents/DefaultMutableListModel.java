package dfEditor.CustomComponents;

import javax.swing.*;


// @author Santhosh Kumar T - santhosh@in.fiorano.com 
public class DefaultMutableListModel<T> extends DefaultListModel<T> implements MutableListModel<T> {
    private static final long serialVersionUID = 461469498415839069L;

    public boolean isCellEditable(int index){
        return true;
    }

//    public void setValueAt(Object value, int index){
//        super.setElementAt(value, index);
//    }
}
