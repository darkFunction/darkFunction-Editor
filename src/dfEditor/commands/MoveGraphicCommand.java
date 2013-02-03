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
import java.awt.Rectangle;
import dfEditor.GraphicObject;
import dfEditor.GraphicPanel;
import java.util.ArrayList;

/**
 *
 * @author s4m20
 */
public class MoveGraphicCommand extends UndoableCommand
{
    private GraphicObject graphic;
    private Rectangle newRect;
    private Rectangle oldRect;
    private GraphicPanel panel;

    public MoveGraphicCommand(final GraphicPanel aPanel, final GraphicObject aGraphic, final Rectangle aDestRect)
    {
        graphic = aGraphic;
        newRect = aDestRect;
        oldRect = aGraphic.getSavedRect();
        oldRect = new Rectangle(oldRect.x, oldRect.y, oldRect.width, oldRect.height);
        panel = aPanel;        
    }
    
    public boolean execute()
    {
        graphic.setRect(newRect);
        graphic.saveRect();
        
        panel.notifyGraphicMoved(graphic);
        panel.repaint();

        return true;
    }

    public void undo()
    {
        graphic.setRect(oldRect);
        graphic.saveRect();
        
        panel.notifyGraphicMoved(graphic);
        panel.repaint();
    }

}
