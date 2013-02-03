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
import dfEditor.command.*;
import dfEditor.*;
import dfEditor.animation.*;

public class SetGraphicZOrderCommand extends UndoableCommand
{
    private GraphicObject graphic;    
    private AnimationController controller;
    private AnimationCell cell;
    private int zValue;
    private int oldValue;

    public SetGraphicZOrderCommand(
            final AnimationCell aCell,
            final GraphicObject aGraphic, 
            final AnimationController aController,
            final int aValue)
    {
        graphic = aGraphic;        
        controller = aController;
        cell = aCell;
        zValue = aValue;
        oldValue = aCell.zOrderOfGraphic(aGraphic);
    }
    
    public boolean execute()
    {
        cell.setZOrder(graphic, zValue);
        controller.zOrdersChanged(cell);
        
        return true;
    }

    public void undo()
    {
        cell.setZOrder(graphic, oldValue);
        controller.zOrdersChanged(cell);
    }

}
