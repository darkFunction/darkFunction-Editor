package dfEditor.CustomComponents;

import javax.swing.*;
import java.awt.*;

// @author Santhosh Kumar T - santhosh@in.fiorano.com 
public interface ListCellEditor<T> extends CellEditor{ 
    Component getListCellEditorComponent(JList<T> list, Object value, 
                                          boolean isSelected, 
                                          int index); 
} 