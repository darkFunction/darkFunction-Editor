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
