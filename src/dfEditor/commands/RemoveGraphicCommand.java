package dfEditor.commands;

import dfEditor.commands.*;
import dfEditor.CustomNode;
import dfEditor.GraphicObject;
import dfEditor.SpriteTree;
import dfEditor.GraphicPanel;
import dfEditor.command.UndoableCommand;

/**
 *
 * @author s4m20
 */
public class RemoveGraphicCommand extends UndoableCommand
{
    UndoableCommand _removeNodeCommand;
    CustomNode _node;
    GraphicPanel _panel;

    public RemoveGraphicCommand(SpriteTree aTree, GraphicPanel aPanel, CustomNode aNode)
    {
        _panel = aPanel;
        _node = aNode;
        _removeNodeCommand = new RemoveNodeCommand(aTree, _node);
    }

    public boolean execute()
    {       
        _panel.removeGraphic((GraphicObject)_node.getCustomObject());
        _removeNodeCommand.execute();
        return true;
    }

    public void undo()
    {
        _panel.addGraphic((GraphicObject)_node.getCustomObject());
        _removeNodeCommand.undo();       
    }

}
