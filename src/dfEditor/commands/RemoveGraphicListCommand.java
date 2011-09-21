/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dfEditor.commands;

/**
 *
 * @author s4m20
 */

import java.util.ArrayList;
import dfEditor.command.*;
import dfEditor.commands.*;
import javax.swing.tree.DefaultMutableTreeNode;
import dfEditor.*;

public class RemoveGraphicListCommand extends UndoableCommand
{
    private ArrayList<UndoableCommand> _commands = new ArrayList<UndoableCommand>();

    public RemoveGraphicListCommand(
            SpriteTree aTree, GraphicPanel aPanel, CustomNode[] aNodes)
    {
        for (int i=0; i<aNodes.length; ++i)
        {
            if (aNodes[i].isLeaf())
                _commands.add(new RemoveGraphicCommand(aTree, aPanel, aNodes[i]));
            else
                _commands.add(new RemoveDirCommand(aTree, aPanel, aNodes[i]));
        }
    }

    public boolean execute()
    {
        for (int i=0; i<_commands.size(); ++i)
            _commands.get(i).execute();

        return true;
    }

    public void undo()
    {
        for (int i=_commands.size()-1; i>=0; --i)
            _commands.get(i).undo();
    }

}
