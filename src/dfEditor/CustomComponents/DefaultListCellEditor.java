package dfEditor.CustomComponents;

import javax.swing.*;
import java.awt.*;

// @author Santhosh Kumar T - santhosh@in.fiorano.com 
public class DefaultListCellEditor<T> extends DefaultCellEditor implements ListCellEditor<T> {
    private static final long serialVersionUID = -1814870371135163156L;

    public DefaultListCellEditor(final JCheckBox checkBox){ 
        super(checkBox); 
    } 
 
    public DefaultListCellEditor(final JComboBox<T> comboBox){ 
        super(comboBox); 
    } 
 
    public DefaultListCellEditor(final JTextField textField){ 
        super(textField); 
    } 
 
    public Component getListCellEditorComponent(JList<T> list, Object value, boolean isSelected, int index){ 
        delegate.setValue(value); 
        return editorComponent; 
    } 
}