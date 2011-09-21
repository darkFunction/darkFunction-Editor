package dfEditor.commands;

import dfEditor.CustomNode;
import dfEditor.commands.*;
import dfEditor.*;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeModel;
import dfEditor.command.UndoableCommand;
/**
 *
 * @author s4m20
 */
public abstract class AddNodeCommand extends UndoableCommand
{
    protected JTree nameTree;
    protected CustomNode parentNode;    
    protected CustomNode newNode;
    private TreePath prevSelectedPath;

    public AddNodeCommand(JTree aTree, CustomNode parentNode, CustomNode newNode)
    {        
        this.parentNode = parentNode;
        this.nameTree = aTree;
        this.newNode = newNode;
    }

    public boolean execute()
    {
        if (parentNode != null)
        {
            DefaultMutableTreeNode selectedNode = null;
            TreePath treePath = nameTree.getSelectionPath();
            if (treePath != null)
                selectedNode = (DefaultMutableTreeNode)treePath.getLastPathComponent();

            if (selectedNode != null)
                this.prevSelectedPath = new TreePath (selectedNode.getPath());
            else
                this.prevSelectedPath = null;

            insertNewNode(parentNode, newNode);
            return true;
        }
        return false;
    }
    
    public abstract void insertNewNode(CustomNode parentNode, DefaultMutableTreeNode newNode);

    public void undo()
    {
        ((DefaultTreeModel)nameTree.getModel()).removeNodeFromParent(newNode);
        nameTree.setSelectionPath(prevSelectedPath);
    }
}
