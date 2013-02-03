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
import dfEditor.CustomNode;
import dfEditor.GraphicObject;
import dfEditor.SpriteTree;
import dfEditor.GraphicPanel;
import dfEditor.command.UndoableCommand;

/**
 *
 * @author s4m20
 */
public class RemoveGraphicCommand extends UndoableCommand
{
    UndoableCommand _removeNodeCommand;
    CustomNode _node;
    GraphicPanel _panel;

    public RemoveGraphicCommand(SpriteTree aTree, GraphicPanel aPanel, CustomNode aNode)
    {
        _panel = aPanel;
        _node = aNode;
        _removeNodeCommand = new RemoveNodeCommand(aTree, _node);
    }

    public boolean execute()
    {       
        _panel.removeGraphic((GraphicObject)_node.getCustomObject());
        _removeNodeCommand.execute();
        return true;
    }

    public void undo()
    {
        _panel.addGraphic((GraphicObject)_node.getCustomObject());
        _removeNodeCommand.undo();       
    }

}
