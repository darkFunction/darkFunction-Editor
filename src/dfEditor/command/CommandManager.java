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

package dfEditor.command;

import java.util.Stack;
import java.awt.Component;
import dfEditor.CommandManagerListener;

/**
 *
 * @author s4m20
 */
public class CommandManager
{
    private Stack undoStack;
    private Stack redoStack;
    
    private Component undoComponent;
    private Component redoComponent;

    private CommandManagerListener listener = null;

    public CommandManager()
    {
        init();
    }

    public CommandManager(Component aUndoComponent, Component aRedoComponent)
    {
        init();
        setUndoComponent(aUndoComponent);
        setRedoComponent(aRedoComponent);
    }

    public void setListener(CommandManagerListener aListener)
    {
        listener = aListener;
    }

    private void init()
    {
        undoStack = new Stack();
        redoStack = new Stack();
    }

    public void setUndoComponent(Component aUndoComponent)
    {
        undoComponent = aUndoComponent;
        undoComponent.setEnabled(undoStack.size() > 0);
    }

    public void setRedoComponent(Component aRedoComponent)
    {
        redoComponent = aRedoComponent;
        redoComponent.setEnabled(redoStack.size() > 0);
    }

    public void execute(Command command)
    {
        if (command == null)
            return;
        
        execute(command, true);
        
    }

    public void execute(Command command, boolean bClearRedoStack)
    {
        if (command.execute())
        {
            if (bClearRedoStack)
            {
                redoStack.clear();
                redoComponent.setEnabled(false);
            }
            
            if (command instanceof UndoableCommand) // TODO: bad?
            {
                undoStack.push(command);
                if (undoComponent != null)
                    undoComponent.setEnabled(true);
            }
        }

        listener.commandStackChanged(this);
    }

    public void undo()
    {
        if (undoStack.size() > 0)
        {
            UndoableCommand command = (UndoableCommand)undoStack.pop();
            command.undo();

            redoStack.push(command);

            if (redoComponent != null)
                redoComponent.setEnabled(true);

            if (undoComponent != null && undoStack.size() == 0)
                undoComponent.setEnabled(false);
        }
        listener.commandStackChanged(this);
    }

    public void redo()
    {
        if (redoStack.size() > 0)
        {
            UndoableCommand command = (UndoableCommand)redoStack.pop();

            execute(command, false);

            if (redoComponent != null && redoStack.size() == 0)
                redoComponent.setEnabled(false);
        }
    }

    public int sizeOfUndoStack()
    {
        return undoStack.size();
    }

    public void clear()
    {
        undoStack.clear();
        redoStack.clear();
        undoComponent.setEnabled(false);
        redoComponent.setEnabled(false);
        listener.commandStackChanged(this);
    }

    public void refreshComponents()
    {
        undoComponent.setEnabled(undoStack.size() > 0);
        redoComponent.setEnabled(redoStack.size() > 0);
    }
}
