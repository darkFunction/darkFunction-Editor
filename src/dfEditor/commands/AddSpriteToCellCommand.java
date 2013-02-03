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

import dfEditor.commands.*;
import dfEditor.animation.*;
import dfEditor.*;
import dfEditor.command.UndoableCommand;
import java.util.ArrayList;
import java.awt.Point;

/**
 *
 * @author s4m20
 */
public class AddSpriteToCellCommand extends UndoableCommand
{
    private CustomNode _spriteNode;
    private AnimationController _animationController;
    private AnimationCell _cell;
    private Point _point;
    private ArrayList<GraphicObject> _graphics = null;
    
    public AddSpriteToCellCommand(            
            final CustomNode aSpriteNode,
            final AnimationController aController,
            final Point aPoint
            )
    {
        _spriteNode = aSpriteNode;
        _animationController = aController;
        _point = aPoint;
        _cell = aController.getWorkingCell();
    }

    public boolean execute()
    {
        if (_graphics == null)
        {
            ArrayList<GraphicObject> graphics = _animationController.addNodeToCell(_spriteNode, _cell, _point);
            _graphics = graphics;
        }
        else
        {
            for (int i=0; i<_graphics.size(); ++i)
                _cell.addSprite(_spriteNode, _graphics.get(i));
        }
        
        _animationController.setWorkingCell(_cell);        

        return true;
    }

    public void undo()
    {
        for (int i=0; i<_graphics.size(); ++i)
        {
            _cell.removeGraphic(_graphics.get(i));
        }
        _animationController.setWorkingCell(_cell);
    }

}
