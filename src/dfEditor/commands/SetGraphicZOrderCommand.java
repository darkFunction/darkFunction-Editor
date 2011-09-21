package dfEditor.commands;
import dfEditor.command.*;
import dfEditor.*;
import dfEditor.animation.*;

public class SetGraphicZOrderCommand extends UndoableCommand
{
    private GraphicObject graphic;    
    private AnimationController controller;
    private AnimationCell cell;
    private int zValue;
    private int oldValue;

    public SetGraphicZOrderCommand(
            final AnimationCell aCell,
            final GraphicObject aGraphic, 
            final AnimationController aController,
            final int aValue)
    {
        graphic = aGraphic;        
        controller = aController;
        cell = aCell;
        zValue = aValue;
        oldValue = aCell.zOrderOfGraphic(aGraphic);
    }
    
    public boolean execute()
    {
        cell.setZOrder(graphic, zValue);
        controller.zOrdersChanged(cell);
        
        return true;
    }

    public void undo()
    {
        cell.setZOrder(graphic, oldValue);
        controller.zOrdersChanged(cell);
    }

}
