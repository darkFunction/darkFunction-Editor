package dfEditor.commands;

import dfEditor.*;
import dfEditor.command.UndoableCommand;

/**
 *
 * @author Sam
 */
public class FlipSpriteCommand extends UndoableCommand
{    
    private boolean _bHoriz;
    private SpriteGraphic _graphic;
    private GraphicPanel _panel;
    
    public FlipSpriteCommand(final SpriteGraphic aGraphic, final boolean aHoriz, final GraphicPanel aPanel)
    {
        _graphic = aGraphic;
        _bHoriz = aHoriz;
        _panel = aPanel;
    }
    
    public boolean execute()
    {
        _graphic.flip(_bHoriz);
        
        _panel.notifyGraphicMoved(_graphic);
        
        return true;
    }

    public void undo()
    {
        _graphic.flip(_bHoriz);
        _panel.notifyGraphicMoved(_graphic);
    }    
}
