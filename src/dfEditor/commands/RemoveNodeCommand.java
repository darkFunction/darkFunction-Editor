/* 
 *  Copyright 2012 Samuel Taylor
 * 
 *  This file is part of darkFunction Editor
 *
 *  darkFunction Editor is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  darkFunction Editor is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with darkFunction Editor.  If not, see <http://www.gnu.org/licenses/>.
 */

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
