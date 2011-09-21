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
    AnimationCell _animationCellToCopy;

    public AddCellCommand(Animation aAnimation, AnimationCell aCellToCopy)
    {
        _animation = aAnimation;
        _animationCellToCopy = aCellToCopy;
        _animationCell = new AnimationCell();
    }

    public boolean execute()
    {
        if (_animationCellToCopy != null)
        {
            ArrayList<GraphicObject> sprites = _animationCellToCopy.getGraphicList();
            for (int i=0; i<sprites.size(); ++i)
            {
                SpriteGraphic orig = (SpriteGraphic)sprites.get(i);
                CustomNode node = _animationCellToCopy.nodeForGraphic(orig);
                SpriteGraphic sprite = orig.copy();
                _animationCell.addSprite(node, sprite);
                _animationCell.setDelay(_animationCellToCopy.getDelay());
            }          
        }

        _animation.addCell(_animationCell);

        return true;
    }
    
    public void undo()
    {
        _animation.removeCell(_animationCell);
    }
}
