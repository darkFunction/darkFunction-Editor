package dfEditor.commands;

import java.util.ArrayList;
import dfEditor.command.*;
import dfEditor.commands.*;
import dfEditor.*;

public class FlipSpriteListCommand extends UndoableCommand
{
    private ArrayList<UndoableCommand> _commands = new ArrayList<UndoableCommand>();

    public FlipSpriteListCommand(ArrayList<GraphicObject> aGraphics, boolean bHoriz, final GraphicPanel aPanel)
    {
        if (aGraphics != null)
        {
            for (int i=0; i<aGraphics.size(); ++i)
            {                            
                _commands.add(new FlipSpriteCommand((SpriteGraphic)aGraphics.get(i), bHoriz, aPanel));
            }
        }
    }

    public boolean execute()
    {
        if (_commands.isEmpty())
            return false;
        
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

