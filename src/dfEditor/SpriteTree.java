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

import dfEditor.*;
import javax.swing.JTree;
import javax.swing.tree.*;
import java.util.Observer;
import java.util.Observable;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.*;
import java.util.ArrayList;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.Cursor;
import java.awt.datatransfer.StringSelection;
/**
 *
 * @author s4m20
 */
public class SpriteTree extends JTree implements Observer, DragGestureListener, DragSourceListener
{
    private ArrayList<SpriteTreeListener> treeListeners = new ArrayList<SpriteTreeListener>();
    private DragSource dragSource = null;
    

    public SpriteTree()
    {
        super();
        initComponents();

        dragSource = DragSource.getDefaultDragSource();

        DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(
            this,                             //DragSource
            DnDConstants.ACTION_COPY, //specifies valid actions
            this                              //DragGestureListener
        );


        // Eliminates right mouse clicks as valid actions
        dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);


        final SpriteTree self = this;
        MouseListener ml = new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                int selRow = self.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = self.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1)
                {
                    if(e.getClickCount() == 1)
                    {

                    }
                    else if(e.getClickCount() == 2)
                    {
                        self.doubleClick(selRow, selPath);
                    }
                }
            }
        };
        this.addMouseListener(ml);
        //setupCellRenderer(); // can't do this here because of Matisse.... called from creator
    }

    public void addTreeListener(SpriteTreeListener aListener)
    {
        treeListeners.add(aListener);
    }

    public void removeListener(SpriteTreeListener aListener)
    {
        if (treeListeners.contains(aListener))
            treeListeners.remove(aListener);
    }

     public void setColours(CustomNode aNode)
     {
         if (aNode.isLeaf())
         {
             Color col = ((CustomNode)aNode.getParent()).getColour();
             aNode.setColour(col);
         }
         else
         {
            if (aNode.isRoot())
            {
                aNode.setColour(Color.BLUE);
            }
            else
            {
                int siblingIndex = 0;
                int numSiblings = 0;
                for (int i=0; i<aNode.getParent().getChildCount(); i++)
                {
                    if (aNode.getParent().getChildAt(i) == aNode)
                    {
                        siblingIndex = numSiblings;
                    }
                    if (!aNode.getParent().getChildAt(i).isLeaf())
                    {
                        numSiblings++;
                    }
                }

                int depth = aNode.getLevel();

                final Color[][] colArray =
                {
                    { Color.BLUE,
                    Color.GREEN,
                    Color.RED,
                    Color.YELLOW,
                    Color.MAGENTA },

                    { Color.ORANGE,
                    new Color(0x66, 0x66, 0xff),
                    new Color(0x33, 0xff, 0x66),
                    new Color(0xdd, 0x33, 0x00),
                    new Color(0x00, 0x33, 0x33) }
                };

                int a = depth % colArray.length;
                Color bgCol = colArray[a][siblingIndex % colArray[a].length];
                aNode.setColour(bgCol);
            }

            for (int i=0; i<aNode.getChildCount(); i++)
            {
                setColours((CustomNode)aNode.getChildAt(i));
            }
         }
     }

    public void setupCellRenderer()
    {
        setColours((CustomNode)this.getModel().getRoot());

        setCellRenderer(new DefaultTreeCellRenderer()
        {
            public Component getTreeCellRendererComponent(javax.swing.JTree pTree,
                     Object pValue, boolean pIsSelected, boolean pIsExpanded,
                     boolean pIsLeaf, int pRow, boolean pHasFocus)
            {
                CustomNode node = (CustomNode)pValue;
                setColours(node);
                super.getTreeCellRendererComponent(pTree, pValue, pIsSelected,
                         pIsExpanded, pIsLeaf, pRow, pHasFocus);
                    
                setBackgroundSelectionColor(node.getColour());
                
                
                return (this);
            }
        });
    }

    public void update(Observable aSender, Object aArg)
    {
        GraphicObject graphic = (GraphicObject)aSender;

        CustomNode node = findFirstNodeContainingCustomObject(
                (CustomNode)this.getModel().getRoot(),
                graphic);

        if (node != null) // found corresponding node
        {            
            TreePath treePath = new TreePath(node.getPath());
            if (graphic.isSelected())
            {
                this.addSelectionPath(treePath);
            }
            else
            {
                this.removeSelectionPath(treePath);
            }            
        }
    }

    protected void fireValueChanged(javax.swing.event.TreeSelectionEvent e)
    {
          super.fireValueChanged(e);
    }

    public CustomNode nodeForObject(Object aObj)
    {
        return this.findFirstNodeContainingCustomObject((CustomNode)this.getModel().getRoot(), aObj);
    }

    private CustomNode findFirstNodeContainingCustomObject(CustomNode aStartNode, Object aObj)
    {

       if (aStartNode.isLeaf())
       {           
           if (aStartNode.getCustomObject() == aObj)
               return aStartNode;
       }
       else
       {
           for (int i=0; i<aStartNode.getChildCount(); ++i)
           {            
               CustomNode childNode = (CustomNode)aStartNode.getChildAt(i);
               CustomNode node = findFirstNodeContainingCustomObject(childNode, aObj);
               if (node != null)
                   return node;
           }
       }

       return null;
        
    }

    public DefaultMutableTreeNode getSelectedNode()
    {
        DefaultMutableTreeNode selectedNode = null;
        TreePath treePath = getSelectionPath();
        if (treePath != null)
            selectedNode = (DefaultMutableTreeNode)treePath.getLastPathComponent();

        return selectedNode;
    }

    public CustomNode[] getSelectedNodes()
    {
        TreePath[] treePaths = getSelectionPaths();
        CustomNode[] selectedNodes = null;
        if (treePaths != null)
        {
            selectedNodes = new CustomNode[treePaths.length];

            if (selectedNodes != null)
                for (int i=0; i<treePaths.length; ++i)
                {
                    TreePath path = treePaths[i];
                    selectedNodes[i] = (CustomNode)treePaths[i].getLastPathComponent();
                }
        }
        return selectedNodes;
    }

    public DefaultMutableTreeNode getSelectedNodeDir()
    {
        DefaultMutableTreeNode node = getSelectedNode();

        if (node == null)
            return null;

        if (node.isLeaf())
                node = (DefaultMutableTreeNode)node.getParent();

        return (CustomNode)node;
    }

    public void clear()
    {
        CustomNode root = (CustomNode)this.getModel().getRoot();
        root.removeAllChildren();
        ((DefaultTreeModel)this.getModel()).reload();
        root = new CustomNode("/", true); // reset name suggestions
    }

    public CustomNode getNodeForPath(String aPath)
    {
        // possible optimisation, fuck it
//        CustomNode node = (CustomNode)this.getModel().getRoot();
//        String[] pathElements = aPath.split("/");
//        for (int i=0; i<pathElements.length; ++i)
//        {
//            if (pathElements[i].equals(""))
//                pathElements[i] = "/";
//
//            if (i > 0) // !root
//            {
//                for (int n=0; n<node.getChildCount(); ++n)
//                {
//                    CustomNode childNode = (CustomNode)node.getChildAt(n);
//                    if (childNode.get)
//                }
//            }
//        }

        CustomNode node = (CustomNode)this.getModel().getRoot();
        CustomNode foundNode = findSubNodeWithPath(node, aPath);
        return foundNode;       
    }

    private CustomNode findSubNodeWithPath(CustomNode aParentNode, String aPath)
    {
        CustomNode foundNode = null;
        String fullPathName = aParentNode.getFullPathName();

        if (fullPathName.equals(aPath))
                return aParentNode;

        if (!aParentNode.isLeaf())
        {
            for (int i=0; i<aParentNode.getChildCount(); ++i)
            {
                foundNode = findSubNodeWithPath((CustomNode)aParentNode.getChildAt(i), aPath);
                if (foundNode != null)
                    return foundNode;
            }
        }

        return null;
    }

    private void doubleClick(int selRow, TreePath selPath)
    {
        for (int i=0; i<treeListeners.size(); ++i)
            treeListeners.get(i).spriteTreeDoubleClicked((CustomNode)selPath.getLastPathComponent());
    }

    public void dragGestureRecognized(DragGestureEvent e)
    {
        CustomNode dragNode = (CustomNode)getSelectedNode();
     
        if (dragNode != null)
        {
            String nodeString = (String)dragNode.getFullPathName();
            StringSelection transferable = new StringSelection(nodeString);
            Cursor cursor = DragSource.DefaultCopyNoDrop;
            dragSource.startDrag(e, cursor, transferable, this);
            //System.out.println("Dragging node: " + nodeString);
        }
    }

    public void dragEnter(DragSourceDragEvent e)
    {
        DragSourceContext ctx = e.getDragSourceContext();
        
        int action = e.getDropAction();
        if ((action & DnDConstants.ACTION_COPY) != 0)
            ctx.setCursor(DragSource.DefaultCopyDrop);
        else
            ctx.setCursor(DragSource.DefaultCopyNoDrop);
    }
    
    public void dragOver(DragSourceDragEvent dsde)
    {

    }

    public void dragExit(DragSourceEvent e)
    {
        DragSourceContext ctx = e.getDragSourceContext();

        ctx.setCursor(DragSource.DefaultCopyNoDrop);
        
    }
    public void dropActionChanged(DragSourceDragEvent dsde) {}
    public void dragDropEnd(DragSourceDropEvent dsde) {}
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setName("Form"); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
