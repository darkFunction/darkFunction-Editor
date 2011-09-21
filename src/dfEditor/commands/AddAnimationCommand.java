package dfEditor.commands;

import dfEditor.command.UndoableCommand;
import dfEditor.animation.*;
import javax.swing.JList;
import dfEditor.CustomComponents.DefaultMutableListModel;

/**
 *
 * @author s4m20
 */
public class AddAnimationCommand extends UndoableCommand
{
    Animation _animation;
    JList _animationList;    

    public AddAnimationCommand(JList aAnimationList, Animation aAnimation)
    {
        _animation = aAnimation;
        _animationList = aAnimationList;        
    }

    public boolean execute()
    {
        ((DefaultMutableListModel)_animationList.getModel()).addElement(_animation);
        _animationList.setSelectedValue(_animation, true);
        
        return true;
    }

    public void undo()
    {
        DefaultMutableListModel model = (DefaultMutableListModel)_animationList.getModel();
        model.removeElement(_animation);
    }
}
