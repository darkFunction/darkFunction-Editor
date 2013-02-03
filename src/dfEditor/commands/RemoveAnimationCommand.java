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
import javax.swing.JList;
import dfEditor.CustomComponents.DefaultMutableListModel;


/**
 *
 * @author s4m20
 */
public class RemoveAnimationCommand extends UndoableCommand
{
    private Animation _animation;
    JList _animationList;
    int index = -1;

    public RemoveAnimationCommand(JList aAnimationList, Animation aAnimation)
    {
        _animation = aAnimation;
        _animationList = aAnimationList;
    }

    public boolean execute()
    {
        if (_animation == null)
            return false;

        index = getIndexOfObjectInList(_animation, _animationList);
        if (index >= 0 && index < _animationList.getModel().getSize())
        {
            ((DefaultMutableListModel)_animationList.getModel()).removeElement(_animation);
            return true;
        }

        return false;
    }

    public void undo()
    {
        if (_animation == null)
            return;
            
        ((DefaultMutableListModel)_animationList.getModel()).add(index, _animation);
    }

    private int getIndexOfObjectInList(Object aObj, JList aList)
    {
        for (int i=0; i<aList.getModel().getSize(); ++i)
        {
            if (aList.getModel().getElementAt(i) == aObj)
            {
                return i;
            }
        }
        return -1;
    }
}
