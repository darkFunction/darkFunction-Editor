package dfEditor.commands;

import dfEditor.commands.*;
import dfEditor.*;
import dfEditor.command.UndoableCommand;
import java.awt.Color;

/**
 *
 * @author s4m20
 */
public class AddGraphicToSheetCommand extends UndoableCommand
{
    private CustomNode _parentNode;
    private GraphicPanel _graphicPanel;
    private SpriteTree _tree;
    private GraphicObject _graphic;

    private UndoableCommand _newNodeCommand;
    private CustomNode _newNode;
    
    public AddGraphicToSheetCommand(
            final SpriteTree aTree,
            final CustomNode aParentNode,
            final GraphicPanel aGraphicPanel,
            final GraphicObject aGraphic)
    {        
        _parentNode = aParentNode;
        _tree = aTree;
        _graphicPanel = aGraphicPanel;

        _newNode = new CustomNode(_parentNode.suggestNameForChildLeaf(), false);
        _newNodeCommand = new AddLeafNodeCommand(_tree, _parentNode, _newNode);

        _graphic = aGraphic;
        _graphic.addObserver(_tree);
    }

    public boolean execute()
    {        
        _graphicPanel.unselectAllGraphics();
        _newNode.setCustomObject(_graphic);

        _graphicPanel.addGraphic(_graphic);
        _newNodeCommand.execute();        
        
        return true;
    }

    public void undo()
    {
        _graphicPanel.removeGraphic(_graphic);
        _newNodeCommand.undo(); //undo add leaf node        
    }

}
