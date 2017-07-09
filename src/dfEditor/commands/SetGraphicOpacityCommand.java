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
public class SetGraphicOpacityCommand extends UndoableCommand
{    
    private float _opacity;
    private float _oldOpacity;
    private GraphicObject _graphic;
    private GraphicPanel _panel;
    
    public SetGraphicOpacityCommand(final GraphicObject aGraphic, final float opacity, final GraphicPanel aPanel)
    {
        _graphic = aGraphic;
        _opacity = opacity;
        _oldOpacity = _graphic.getSavedOpacity();
        _panel = aPanel;
    }
    
    public boolean execute()
    {        
        _graphic.setOpacity(_opacity);
        _graphic.saveOpacity();
        
        _panel.notifyGraphicMoved(_graphic);
        
        return true;
    }

    public void undo()
    {
        _graphic.setOpacity(_oldOpacity);
        _graphic.saveOpacity();
        
        _panel.notifyGraphicMoved(_graphic);
    }    
}
