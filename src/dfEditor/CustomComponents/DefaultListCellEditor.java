package dfEditor.CustomComponents;

import javax.swing.*;
import java.awt.*;

// @author Santhosh Kumar T - santhosh@in.fiorano.com 
public class DefaultListCellEditor extends DefaultCellEditor implements ListCellEditor{ 
    public DefaultListCellEditor(final JCheckBox checkBox){ 
        super(checkBox); 
    } 
 
    public DefaultListCellEditor(final JComboBox comboBox){ 
        super(comboBox); 
    } 
 
    public DefaultListCellEditor(final JTextField textField){ 
        super(textField); 
    } 
 
    public Component getListCellEditorComponent(JList list, Object value, boolean isSelected, int index){ 
        delegate.setValue(value); 
        return editorComponent; 
    } 
}