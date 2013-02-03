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
