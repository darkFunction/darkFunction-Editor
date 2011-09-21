/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dfEditor.commands;

import java.util.ArrayList;
import dfEditor.command.*;
import dfEditor.commands.*;
import dfEditor.*;

/**
 *
 * @author s4m20
 */
public class AddGraphicListToSheetCommand extends UndoableCommand
{
    private ArrayList<UndoableCommand> _commands = new ArrayList<UndoableCommand>();

    public AddGraphicListToSheetCommand(
            final SpriteTree aTree,
            final CustomNode aParentNode,
            final GraphicPanel aGraphicPanel,
            final ArrayList<GraphicObject> aGraphics)
    {
        for (int i=0; i<aGraphics.size(); ++i)
            _commands.add(new AddGraphicToSheetCommand(aTree, aParentNode, aGraphicPanel, aGraphics.get(i)));
    }

    public boolean execute()
    {
        for (int i=0; i<_commands.size(); ++i)
            _commands.get(i).execute();

        return true;
    }

    public void undo()
    {
        for (int i=0; i<_commands.size(); ++i)
            _commands.get(i).undo();
    }
}
