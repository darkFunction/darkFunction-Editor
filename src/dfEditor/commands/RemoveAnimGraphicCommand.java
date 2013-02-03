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

import dfEditor.command.UndoableCommand;
import dfEditor.animation.*;
import java.util.ArrayList;
import dfEditor.*;
/**
 *
 * @author s4m20
 */
public class RemoveAnimGraphicCommand extends UndoableCommand
{
    private Animation _animation;
    private AnimationCell _animationCell;
    private GraphicObject _graphic;
    private CustomNode _node;
    private int _zOrder;    
    private GraphicPanel _panel;

    public RemoveAnimGraphicCommand(
            final Animation aAnimation,
            final AnimationCell aCell,
            final GraphicObject aGraphic,
            final GraphicPanel aPanel)
    {
        _animation = aAnimation;
        _animationCell = aCell;
        _graphic = aGraphic;
        _panel = aPanel;       
        _zOrder = 0;
    }

    public boolean execute()
    {
        if (_animation == null || _animationCell == null || _graphic == null)
            return false;

        _zOrder = _animationCell.zOrderOfGraphic(_graphic);
        _node = (_animationCell.nodeForGraphic(_graphic));
        _animationCell.removeGraphic(_graphic);
        _panel.removeGraphic(_graphic);
        
        return true;
    }

    public void undo()
    {
        if (_animation == null || _animationCell == null || _graphic == null)
            return ;
        
        _animationCell.addSprite(_node, _graphic);
        _animationCell.setZOrder(_graphic, _zOrder);
        _panel.addGraphic(_graphic);        
    }
}
