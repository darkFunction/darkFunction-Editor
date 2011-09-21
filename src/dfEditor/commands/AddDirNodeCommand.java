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
public class AddDirNodeCommand extends AddNodeCommand
{
    public AddDirNodeCommand(JTree aTree, CustomNode aParentNode, CustomNode aNewNode)
    {
        super(aTree, aParentNode, aNewNode);
    }

    public void insertNewNode(CustomNode parentNode, DefaultMutableTreeNode aNewNode)
    {       
        int insertIndex = parentNode.getChildCount();
        while(insertIndex > 0)
        {
            if (parentNode.getChildAt(insertIndex-1).isLeaf())
                insertIndex--;
            else
                break;
        }

        ((DefaultTreeModel)nameTree.getModel()).insertNodeInto(newNode, parentNode, insertIndex);

        TreePath newPath = new TreePath(newNode.getPath());
        nameTree.scrollPathToVisible(newPath);
        nameTree.setSelectionPath(newPath);
        nameTree.startEditingAtPath(newPath);
    }
}
