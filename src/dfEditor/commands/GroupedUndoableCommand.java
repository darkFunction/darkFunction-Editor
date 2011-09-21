package dfEditor.commands;

import java.util.ArrayList;
import dfEditor.command.*;

public class GroupedUndoableCommand extends UndoableCommand
{
    private ArrayList<UndoableCommand> _commands = new ArrayList<UndoableCommand>();

    public GroupedUndoableCommand(ArrayList<UndoableCommand> aCommands)
    {
        for (int i=0; i<aCommands.size(); ++i)
        {                            
            _commands.add(aCommands.get(i));
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
        {
            UndoableCommand c = _commands.get(i);
            c.undo();
        }
    }
}

