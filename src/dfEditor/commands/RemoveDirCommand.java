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
