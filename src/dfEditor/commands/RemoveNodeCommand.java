package dfEditor.commands;

import dfEditor.commands.*;
import dfEditor.CustomNode;
import dfEditor.command.UndoableCommand;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JTree;

/**
 *
 * @author s4m20
 */
public class RemoveNodeCommand extends UndoableCommand
{
    private DefaultMutableTreeNode node = null;
    private DefaultMutableTreeNode nodeParent = null;
    private int nodeIndex = 0;
    private TreePath prevSelectedPath;
    private JTree tree;


    public RemoveNodeCommand(JTree aTree, DefaultMutableTreeNode node)
    {
        this.tree = aTree;
        this.node = node;
    }

    public boolean execute()
    {
        if(null == node)
            return false;
        
        this.prevSelectedPath = new TreePath(node.getPath());

        // select previous sibling
        DefaultMutableTreeNode siblingNode = node.getPreviousSibling();
        if (null != siblingNode)
            tree.setSelectionPath(new TreePath(siblingNode.getPath()));
        else if (! node.isRoot())
            tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode)node.getParent()).getPath()));

        // then remove the node
        if (!node.isRoot())
        {
            this.nodeParent = (CustomNode)node.getParent();
            this.nodeIndex = nodeParent.getIndex(node);
                    
            ((DefaultTreeModel)tree.getModel()).removeNodeFromParent(node);

            return true;
        }
        return false;
    }

    public void undo()
    {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        if (nodeParent != null)
            model.insertNodeInto(node, nodeParent, nodeIndex);
        tree.setSelectionPath(prevSelectedPath);
    }

}
