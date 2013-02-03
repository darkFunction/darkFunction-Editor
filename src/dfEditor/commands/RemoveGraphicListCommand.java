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

/**
 *
 * @author s4m20
 */

import java.util.ArrayList;
import dfEditor.command.*;
import dfEditor.commands.*;
import javax.swing.tree.DefaultMutableTreeNode;
import dfEditor.*;

public class RemoveGraphicListCommand extends UndoableCommand
{
    private ArrayList<UndoableCommand> _commands = new ArrayList<UndoableCommand>();

    public RemoveGraphicListCommand(
            SpriteTree aTree, GraphicPanel aPanel, CustomNode[] aNodes)
    {
        for (int i=0; i<aNodes.length; ++i)
        {
            if (aNodes[i].isLeaf())
                _commands.add(new RemoveGraphicCommand(aTree, aPanel, aNodes[i]));
            else
                _commands.add(new RemoveDirCommand(aTree, aPanel, aNodes[i]));
        }
    }

    public boolean execute()
    {
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
