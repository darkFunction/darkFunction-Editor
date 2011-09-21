package dfEditor.commands;

import dfEditor.CustomNode;
import dfEditor.commands.*;
import dfEditor.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTree;

/**
 *
 * @author s4m20
 */
public class AddLeafNodeCommand extends AddNodeCommand
{
    public AddLeafNodeCommand(JTree aTree, CustomNode aParentNode, CustomNode aNewNode)
    {
        super(aTree, aParentNode, aNewNode);
    }

    public void insertNewNode(CustomNode parentNode, DefaultMutableTreeNode newNode)
    {
        ((DefaultTreeModel)nameTree.getModel()).insertNodeInto(newNode, parentNode, parentNode.getChildCount());
        
        TreePath newPath = new TreePath(newNode.getPath());
        nameTree.scrollPathToVisible(newPath);
        nameTree.setSelectionPath(newPath);
        nameTree.startEditingAtPath(newPath);
    }
}