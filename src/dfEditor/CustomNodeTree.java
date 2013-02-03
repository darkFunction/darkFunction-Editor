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

import javax.swing.tree.*;
import javax.swing.*;
import java.awt.event.*;

public class CustomNodeTree extends dfEditor.SpriteTree
{
    private JPopupMenu spritePopupMenu;
    private JMenuItem removeNodeItem;
    private JPopupMenu dirPopupMenu;
    private JMenu newMenu;
    private JMenuItem addNodeItem;
    private JMenuItem addDirItem;
    private JMenuItem removeDirItem;

    private void initComponents()
    {
        spritePopupMenu = new javax.swing.JPopupMenu();
        spritePopupMenu.setName("spritePopupMenu");

        removeNodeItem = new javax.swing.JMenuItem();
        removeNodeItem.setText("Remove");
        removeNodeItem.setName("removeNodeItem");
        removeNodeItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeNodeItemActionPerformed(evt);
            }
        });
        spritePopupMenu.add(removeNodeItem);

        dirPopupMenu = new javax.swing.JPopupMenu();
        dirPopupMenu.setName("dirPopupMenu");

        newMenu = new javax.swing.JMenu();
        newMenu.setText("New");
        newMenu.setName("newMenu");
        dirPopupMenu.add(newMenu);

        addNodeItem = new javax.swing.JMenuItem();
        addNodeItem.setText("Sprite");
        addNodeItem.setName("addNodeItem");
        addNodeItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNodeItemActionPerformed(evt);
            }
        });
        newMenu.add(addNodeItem);


        addDirItem = new javax.swing.JMenuItem();
        addDirItem.setText("Category");
        addDirItem.setName("addDirItem");
        addDirItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDirItemActionPerformed(evt);
            }
        });
        newMenu.add(addDirItem);

        removeDirItem = new javax.swing.JMenuItem();
        removeDirItem.setText("Remove");
        removeDirItem.setName("removeDirItem");
        removeDirItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDirItemActionPerformed(evt);
            }
        });
        dirPopupMenu.add(removeDirItem);
    }

    private void removeNodeItemActionPerformed(ActionEvent e)
    {

    }

    private void addNodeItemActionPerformed(ActionEvent e)
    {

    }
    
    private void addDirItemActionPerformed(ActionEvent e)
    {

    }
    
    private void removeDirItemActionPerformed(ActionEvent e)
    {

    }

    private void nameTreeMousePressed(java.awt.event.MouseEvent evt)
    {
        if (!this.isEnabled())
            return;

        TreePath clickPath = this.getPathForLocation(evt.getX(), evt.getY());
        this.setSelectionPath(clickPath);

        if (evt.isPopupTrigger())
            nameTreePopup(evt.getPoint());
    }

    private void nameTreeMouseReleased(java.awt.event.MouseEvent evt) {
         if (!this.isEnabled())
            return;

        if (evt.isPopupTrigger())
            nameTreePopup(evt.getPoint());
    }

    private void nameTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {

        TreePath lastPath = evt.getOldLeadSelectionPath();
        if (lastPath != null)
        {
            CustomNode lastSelectedNode = (CustomNode)lastPath.getLastPathComponent();
            GraphicObject graphic = (GraphicObject)lastSelectedNode.getCustomObject();
            if (graphic != null)
                graphic.setSelected(false);
        }

        TreePath parentPath = this.getSelectionPath();
        CustomNode selectedNode = null;
        if (parentPath != null)
        {
            selectedNode = (CustomNode) (parentPath.getLastPathComponent());
        }
    }

    private void nameTreePopup(java.awt.Point aPos)
    {
        TreePath selectionPath = this.getSelectionPath();
        if (selectionPath != null)
        {
            CustomNode selectedNode = (CustomNode)(selectionPath.getLastPathComponent());
            if (selectedNode != null)
            {
                if (selectedNode.isLeaf())
                    spritePopupMenu.show(this, aPos.x, aPos.y);
                else
                    dirPopupMenu.show(this, aPos.x, aPos.y);

                if (selectedNode.isRoot())
                    removeDirItem.setEnabled(false);
                else
                    removeDirItem.setEnabled(true);
            }
        }
    }


}
