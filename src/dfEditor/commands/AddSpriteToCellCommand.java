package dfEditor.commands;

import dfEditor.commands.*;
import dfEditor.animation.*;
import dfEditor.*;
import dfEditor.command.UndoableCommand;
import java.util.ArrayList;
import java.awt.Point;

/**
 *
 * @author s4m20
 */
public class AddSpriteToCellCommand extends UndoableCommand
{
    private CustomNode _spriteNode;
    private AnimationController _animationController;
    private AnimationCell _cell;
    private Point _point;
    private ArrayList<GraphicObject> _graphics = null;
    
    public AddSpriteToCellCommand(            
            final CustomNode aSpriteNode,
            final AnimationController aController,
            final Point aPoint
            )
    {
        _spriteNode = aSpriteNode;
        _animationController = aController;
        _point = aPoint;
        _cell = aController.getWorkingCell();
    }

    public boolean execute()
    {
        if (_graphics == null)
        {
            ArrayList<GraphicObject> graphics = _animationController.addNodeToCell(_spriteNode, _cell, _point);
            _graphics = graphics;
        }
        else
        {
            for (int i=0; i<_graphics.size(); ++i)
                _cell.addSprite(_spriteNode, _graphics.get(i));
        }
        
        _animationController.setWorkingCell(_cell);        

        return true;
    }

    public void undo()
    {
        for (int i=0; i<_graphics.size(); ++i)
        {
            _cell.removeGraphic(_graphics.get(i));
        }
        _animationController.setWorkingCell(_cell);
    }

}
