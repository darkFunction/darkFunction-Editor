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

import dfEditor.*;
import dfEditor.command.UndoableCommand;

/**
 *
 * @author Sam
 */
public class SetGraphicIdCommand extends UndoableCommand
{    
    private String _id;
    private String _oldId;
    private GraphicObject _graphic;
    private GraphicPanel _panel;
    
    public SetGraphicIdCommand(final GraphicObject aGraphic, final String id, final GraphicPanel aPanel)
    {
        _graphic = aGraphic;
        _id = id;
        _oldId = _graphic.getSavedId();
        _panel = aPanel;
    }
    
    public boolean execute()
    {        
        _graphic.setId(_id);
        _graphic.saveId();
        
        return true;
    }

    public void undo()
    {
        _graphic.setId(_oldId);
        _graphic.saveId();
    }    
}
