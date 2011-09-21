package dfEditor.commands;

import dfEditor.command.UndoableCommand;
import dfEditor.animation.*;
/**
 *
 * @author s4m20
 */
public class RemoveCellCommand extends UndoableCommand
{
    Animation _animation;
    AnimationCell _animationCell;

    public RemoveCellCommand(Animation aAnimation, AnimationCell aCell)
    {
        _animation = aAnimation;
        _animationCell = aCell;
    }

    public boolean execute()
    {
        _animation.removeCell(_animationCell);
        return true;
    }

    public void undo()
    {
        _animation.addCell(_animationCell);
    }
}
