package dfEditor.commands;

import dfEditor.command.UndoableCommand;
import java.awt.Rectangle;
import dfEditor.GraphicObject;
import dfEditor.GraphicPanel;
import java.util.ArrayList;

/**
 *
 * @author s4m20
 */
public class MoveGraphicCommand extends UndoableCommand
{
    private GraphicObject graphic;
    private Rectangle newRect;
    private Rectangle oldRect;
    private GraphicPanel panel;

    public MoveGraphicCommand(final GraphicPanel aPanel, final GraphicObject aGraphic, final Rectangle aDestRect)
    {
        graphic = aGraphic;
        newRect = aDestRect;
        oldRect = aGraphic.getSavedRect();
        oldRect = new Rectangle(oldRect.x, oldRect.y, oldRect.width, oldRect.height);
        panel = aPanel;        
    }
    
    public boolean execute()
    {
        graphic.setRect(newRect);
        graphic.saveRect();
        
        panel.notifyGraphicMoved(graphic);
        panel.repaint();

        return true;
    }

    public void undo()
    {
        graphic.setRect(oldRect);
        graphic.saveRect();
        
        panel.notifyGraphicMoved(graphic);
        panel.repaint();
    }

}
