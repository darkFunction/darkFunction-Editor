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

import javax.swing.DefaultListModel;
import javax.swing.JList;
import dfEditor.CustomComponents.DefaultMutableListModel;

/**
 *
 * @author s4m20
 */
public class AddAnimationCommand extends UndoableCommand
{
    Animation _animation;
    JList<Animation> _animationList;    

    public AddAnimationCommand(JList<Animation> aAnimationList, Animation aAnimation)
    {
        _animation = aAnimation;
        _animationList = aAnimationList;
    }

    public boolean execute()
    {
        ((DefaultListModel<Animation>)_animationList.getModel()).addElement(_animation);
        _animationList.setSelectedValue(_animation, true);
        
        return true;
    }

    public void undo()
    {
        DefaultMutableListModel<Animation> model = (DefaultMutableListModel<Animation>)_animationList.getModel();
        model.removeElement(_animation);
    }
}
