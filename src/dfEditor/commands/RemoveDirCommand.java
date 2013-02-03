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
import dfEditor.SpriteTree;
import dfEditor.GraphicPanel;
import dfEditor.command.UndoableCommand;
import java.util.Stack;

/**
 *
 * @author s4m20
 */
public class RemoveDirCommand extends UndoableCommand
{
    CustomNode node;
    GraphicPanel panel;
    SpriteTree tree;

    Stack<UndoableCommand> commandStack;

    public RemoveDirCommand(final SpriteTree aTree, final GraphicPanel aPanel, final CustomNode aNode)
    {
        tree = aTree;
        panel = aPanel;
        node = aNode;

        commandStack = new Stack<UndoableCommand>();
    }

    public boolean execute()
    {
        if (!node.isLeaf())
            remove(node);


        UndoableCommand command = new RemoveNodeCommand(tree, node);//aNode);
        commandStack.push(command);
        command.execute();

        return true;
    }

    private void remove(CustomNode aNode)
    {
        while (aNode.getChildCount() > 0)
        {
            CustomNode childNode = (CustomNode)aNode.getLastChild();
            UndoableCommand command;
            
            if (childNode.isLeaf())
            {
                command = new RemoveGraphicCommand(tree, panel, childNode);                
            }
            else
            {
                remove(childNode);
                command = new RemoveNodeCommand(tree, childNode);
                
            }
            commandStack.push(command);
            command.execute();
        }

    }

    public void undo()
    {
        while (commandStack.size() > 0)
        {
            UndoableCommand command = commandStack.pop();

            command.undo();
        }
    }

}
