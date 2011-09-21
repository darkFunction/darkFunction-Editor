package dfEditor;
import java.util.ArrayList;
/**
 *
 * @author s4m20
 */
public interface GraphicPanelChangeListener
{
    public void graphicAdded(GraphicPanel aPanel, GraphicObject aGraphic);
    public void graphicMoved(GraphicPanel aPanel, GraphicObject aGraphic);
    public void graphicSelectionChanged(GraphicPanel aPanel, GraphicObject aGraphic);
        
    public void graphicErased(GraphicPanel aPanel, GraphicObject aGraphic);
    // this is a hack for now, should remove the single and just use array version but requires fair bit of refactoring
    public void graphicsErased(GraphicPanel aPanel, ArrayList<GraphicObject> aGraphics);
   
}
