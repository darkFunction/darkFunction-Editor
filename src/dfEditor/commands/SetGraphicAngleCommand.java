package dfEditor.commands;

import dfEditor.*;
import dfEditor.command.UndoableCommand;

/**
 *
 * @author Sam
 */
public class SetGraphicAngleCommand extends UndoableCommand
{    
    private float _angle;
    private float _oldAngle;
    private GraphicObject _graphic;
    private GraphicPanel _panel;
    
    public SetGraphicAngleCommand(final GraphicObject aGraphic, final float aAngle, final GraphicPanel aPanel)
    {
        _graphic = aGraphic;
        _angle = aAngle;
        _oldAngle = _graphic.getSavedAngle();
        _panel = aPanel;
    }
    
    public boolean execute()
    {        
        _graphic.setAngle(_angle);
        _graphic.saveAngle();
        
        _panel.notifyGraphicMoved(_graphic);
        
        return true;
    }

    public void undo()
    {
        _graphic.setAngle(_oldAngle);
        _graphic.saveAngle();
        
        _panel.notifyGraphicMoved(_graphic);
    }    
}
