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
