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
/**
 *
 * @author s4m20
 */

import javax.swing.JList;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.Component;
import dfEditor.*;
import java.util.ArrayList;

public class SpriteList extends JList implements DropTargetListener
{
    Component dragOrigin = null;
    private ArrayList<NodeDroppedListener> nodeDroppedListeners = new ArrayList<NodeDroppedListener>();

    public void addNodeDroppedListener(NodeDroppedListener aListener)
    {
        nodeDroppedListeners.add(aListener);
    }
    
    public void setDragSource(Component aDragSource)
    {
        dragOrigin = aDragSource;

        DropTarget dropTarget = new DropTarget(this, this);

    }
    
    public void dragGestureRecognized(DragGestureEvent dge)
    {

    }

    public void drop(DropTargetDropEvent dtde)
    {
        Transferable transferable = dtde.getTransferable();

        //flavor not supported, reject drop
        //DataFlavor dataFlavor =  new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=dfEditor.GraphicObject");
        if (!transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
        {
            dtde.rejectDrop();
            return;
        }
        
        if (dtde.getDropAction() == DnDConstants.ACTION_COPY)
        {
            try {
                String data = (String)transferable.getTransferData(DataFlavor.stringFlavor);
                for (int i=0; i<nodeDroppedListeners.size(); ++i)
                {
                    nodeDroppedListeners.get(i).nodeDropped(this, data, new java.awt.Point(0,0));
                }
            } catch (Exception e) {}
        }        
    }

    public void dragEnter(DropTargetDragEvent dtde)     
    {

    }

    public void dragOver(DropTargetDragEvent dtde) 
    {
       
    }

    public void dropActionChanged(DropTargetDragEvent dtde)
    {
    }

    public void dragExit(DropTargetEvent dte)
    {
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {}
}
