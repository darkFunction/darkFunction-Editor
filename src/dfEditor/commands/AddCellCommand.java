package dfEditor.commands;

import dfEditor.command.UndoableCommand;
import dfEditor.animation.*;
import java.util.ArrayList;
import dfEditor.GraphicObject;
import dfEditor.SpriteGraphic;
import dfEditor.CustomNode;

/**
 *
 * @author s4m20
 */
public class AddCellCommand extends UndoableCommand
{

    Animation _animation;
    AnimationCell _animationCell;

    public AddCellCommand(Animation aAnimation, AnimationCell aCellToCopy)
    {
        _animation = aAnimation;
        _animationCell = aCellToCopy.copy();
    }

    public boolean execute()
    {        
        _animation.addCell(_animationCell);

        return true;
    }
    
    public void undo()
    {
        _animation.removeCell(_animationCell);
    }
}
