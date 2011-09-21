/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
