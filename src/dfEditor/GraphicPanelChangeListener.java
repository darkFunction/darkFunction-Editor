/* 
 *  Copyright 2012 Samuel Taylor
 * 
 *  This file is part of darkFunction Editor
 *
 *  darkFunction Editor is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  darkFunction Editor is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with darkFunction Editor.  If not, see <http://www.gnu.org/licenses/>.
 */

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
