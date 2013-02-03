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

import java.util.ArrayList;
import dfEditor.command.*;
import dfEditor.commands.*;
import dfEditor.*;

public class RotateGraphicListCommand extends UndoableCommand
{
    private ArrayList<UndoableCommand> _commands = new ArrayList<UndoableCommand>();

    public RotateGraphicListCommand(ArrayList<GraphicObject> aGraphics, float aAngle, final GraphicPanel aPanel)
    {
        if (aGraphics != null)
        {
            for (int i=0; i<aGraphics.size(); ++i)
            {            
                float angle = aGraphics.get(i).getAngle() + aAngle;
                if (angle < 0)
                    angle = 360+angle;
                angle %= 360;
                _commands.add(new SetGraphicAngleCommand(aGraphics.get(i), angle, aPanel));
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

