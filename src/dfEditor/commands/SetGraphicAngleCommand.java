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
public class SetGraphicAngleCommand extends UndoableCommand
{    
    private float _angle;
    private float _oldAngle;
    private GraphicObject _graphic;
    private GraphicPanel _panel;
    
    public SetGraphicAngleCommand(final GraphicObject aGraphic, final float aAngle, final GraphicPanel aPanel)
    {
        _graphic = aGraphic;
        _angle = aAngle;
        _oldAngle = _graphic.getSavedAngle();
        _panel = aPanel;
    }
    
    public boolean execute()
    {        
        _graphic.setAngle(_angle);
        _graphic.saveAngle();
        
        _panel.notifyGraphicMoved(_graphic);
        
        return true;
    }

    public void undo()
    {
        _graphic.setAngle(_oldAngle);
        _graphic.saveAngle();
        
        _panel.notifyGraphicMoved(_graphic);
    }    
}
