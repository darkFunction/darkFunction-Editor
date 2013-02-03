/* 
 *  Copyright 2009 Samuel Taylor
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

/*
 * SpritesheetController.java
 *
 * Created on 06-Dec-2009, 16:39:41
 */

package dfEditor;

import dfEditor.commands.*;
import dfEditor.command.*;
import dfEditor.io.*;
import javax.swing.tree.DefaultTreeModel;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.tree.TreePath;
import javax.swing.*;
import org.openide.awt.DropDownButtonFactory;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.awt.Rectangle;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Owner
 */
public class SpritesheetController extends dfEditorPanel implements ImageModifiedListener, GraphicPanelChangeListener
{    
    private File savedImageFile = null;
    private String savedImageFormat = null;
    private boolean bImageModified = false;   
    protected CustomNode _lastSelectedDirNode;
    private JPopupMenu imagePopup;
    private JMenuItem setImageItem = null;
    private JMenuItem changeColourItem = null;
    private JMenuItem packItem = null;

    /** Creates new form SpritesheetController */
    public SpritesheetController(CommandManager aCmdManager, boolean aNew, JLabel aHelpLabel, TaskChangeListener aListener, JFileChooser aChooser)
    {
        super(aCmdManager, aHelpLabel, aListener, aChooser);

        initImagePopupMenu();
        initComponents();

        viewPanel.addGraphicChangeListener(this);

        if (aNew)
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showSetImageChooser();
                }
            });
        }             
        
        postInit();
    }

    public void graphicSelectionChanged(GraphicPanel aPanel, GraphicObject aGraphic)
    {
        viewPanel.bringGraphicToFront(aGraphic);
    }
    
    public void graphicAdded(GraphicPanel aPanel, GraphicObject aGraphic)
    {
    }
    
    public void graphicMoved(GraphicPanel aPanel, GraphicObject aGraphic)
    {
        this.refreshSpriteInfoPanel();
    }
    
    public void graphicErased(GraphicPanel aPanel, GraphicObject aGraphic){}
    public void graphicsErased(GraphicPanel aPanel, ArrayList<GraphicObject> aGraphics)
    {
        ArrayList<CustomNode> erasedNodes = new ArrayList<CustomNode>();
        for (int i=0; i<aGraphics.size(); ++i)
        {
            GraphicObject graphic = aGraphics.get(i);
            
            erasedNodes.add(nameTree.nodeForObject(graphic));                        
        }
        
        ArrayList<UndoableCommand> commands = new ArrayList<UndoableCommand>();
        for (int i=0; i<erasedNodes.size(); ++i)        
            commands.add(new RemoveGraphicCommand(nameTree, viewPanel, erasedNodes.get(i)));
        
        GroupedUndoableCommand groupedCommand = new GroupedUndoableCommand(commands);
        if (cmdManager != null)
            cmdManager.execute(groupedCommand);
        else
            groupedCommand.execute();        
    }

    private void initImagePopupMenu()
    {
        imagePopup = new JPopupMenu();


        setImageItem = new JMenuItem("Set image...");
        changeColourItem = new JMenuItem("Make colour transparent...");
        packItem = new JMenuItem("Optimally pack sprites");

        java.net.URL imgURL = this.getClass().getResource("resources/main_icons/Edit.png");
        ImageIcon icon = new ImageIcon(imgURL);
        setImageItem.setIcon(icon);

        imgURL = this.getClass().getResource("resources/main_icons/paint.png");
        icon = new ImageIcon(imgURL);
        changeColourItem.setIcon(icon);

        imgURL = this.getClass().getResource("resources/main_icons/pack.png");
        icon = new ImageIcon(imgURL);
        packItem.setIcon(icon);

        java.awt.event.ActionListener actionListener = new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                imageItemActionPerformed(evt);
            }
        };

        setImageItem.addActionListener(actionListener);
        changeColourItem.addActionListener(actionListener);
        packItem.addActionListener(actionListener);
        
        setImageItem.setEnabled(true);
        changeColourItem.setEnabled(false);
        packItem.setEnabled(false);

        imagePopup.add(setImageItem);
        imagePopup.add(changeColourItem);
        imagePopup.add(packItem);


    }

    private void imageItemActionPerformed(ActionEvent aEvt)
    {
        if (aEvt.getSource() == setImageItem)
        {
            showSetImageChooser();
        }
        else if (aEvt.getSource() == changeColourItem)
        {
            viewPanel.enterColourPickerMode(this);
            helpLabel.setText("Select a pixel of the colour you wish to make transparent");
        }
        else if (aEvt.getSource() == packItem)
        {
            pack();
        }
    }

    private void pack()
    {
        CustomNode rootNode = (CustomNode)nameTree.getModel().getRoot();

        if (rootNode != null)
        {            
            ArrayList<Rectangle> rectList = new ArrayList<Rectangle>();
            addNodeToRectList(rootNode, rectList);

            if (rectList.size() > 0)
            {
                Rectangle[] array = new Rectangle[rectList.size()];
                for (int i=0; i<rectList.size(); ++i)
                {
                    array[i] = rectList.get(i);
                }

                String[] choices = {" Power of two ", " Smallest " };

                int choice = JOptionPane.showOptionDialog(
                                   this                        // Center in window.
                                 , "Would you like the resulting image to use power of two dimensions (eg, 256x512)?"              // Message
                                 , "Image dimensions?"                  // Title in titlebar
                                 , JOptionPane.YES_NO_OPTION    // Option type
                                 , JOptionPane.QUESTION_MESSAGE    // messageType
                                 , null                         // Icon (none)
                                 , choices                      // Button text as above.
                                 , " "      // Default button's label
                               );


                PixelPacker packer = new PixelPacker();
                BufferedImage newImage = packer.packPixels(viewPanel.getImage(), array, (choice == 0));
                viewPanel.setImage(newImage);

                bImageModified = true;
                viewPanel.repaint();

                setModified(true);
                taskChangeListener.taskChanged(this);
            }
        }
    }

    private void addNodeToRectList(final CustomNode aNode, final ArrayList aList)
    {
        if (aNode.isLeaf())
        {
            aList.add(((GraphicObject)aNode.getCustomObject()).getRect());
        }
        else
        {
            for (int i=0; i<aNode.getChildCount(); ++i)
            {
                addNodeToRectList((CustomNode)aNode.getChildAt(i), aList);
            }
        }
    }
    
    public void imageModified()
    {
        bImageModified = true;
        taskChangeListener.taskChanged(this);

        setTip();
    }

    private void setTip()
    {
        if (helpLabel != null)
        {
            if (viewPanel.hasTransparentPixels())
                helpLabel.setText("Tip: double click on a sprite to automatically select it");
            else
                helpLabel.setText("Tip: Use the transparency tool to make a colour transparent");

        }
    }

    public boolean load(String aFileName, DefaultTreeModel aModel)
    {        
        if (aFileName == null)        
        {
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading sprites",
                    "Corrupt sprites definition file",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        File imgFile = new File(aFileName);            
        if (!imgFile.exists())
        {
            JOptionPane.showMessageDialog(
                   this,
                   "Could not find image: \n\n"+aFileName,
                   "Image not found",
                   JOptionPane.ERROR_MESSAGE);
            return false;
        }
        

        setImage(imgFile);
        
        nameTree.setModel(aModel);
        nameTree.setEnabled(true);
        _lastSelectedDirNode = null;
        
        // add everything to the graphic panel
        addNodeToPanel((CustomNode)aModel.getRoot());

        nameTree.setupCellRenderer();
        setGraphicColoursFromTree(nameTree);

        return true;
    }

    public void setGraphicColoursFromTree(SpriteTree aTree)
    {        
        setGraphicColoursOnNode((CustomNode)aTree.getModel().getRoot());
    }

    private void setGraphicColoursOnNode(CustomNode aNode)
    {
        if (aNode.isLeaf())
        {
            SelectionBox sb = (SelectionBox)aNode.getCustomObject();
            if (sb != null)
            {
                java.awt.Color col = aNode.getColour();
                sb.setColour(col);
            }
        }
        else
        {
            for (int i=0; i<aNode.getChildCount(); ++i)
                setGraphicColoursOnNode((CustomNode)aNode.getChildAt(i));
        }
    }
    
    private void addNodeToPanel(CustomNode aNode)
    {
        if (aNode.isLeaf())
        {
            GraphicObject sb = (GraphicObject)aNode.getCustomObject();
            viewPanel.addGraphic(sb);
        }
        else
        {
            for (int i=0; i<aNode.getChildCount(); ++i)
            {
                addNodeToPanel((CustomNode)aNode.getChildAt(i));
            }
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        spritePopupMenu = new javax.swing.JPopupMenu();
        removeSpriteItem = new javax.swing.JMenuItem();
        dirPopupMenu = new javax.swing.JPopupMenu();
        newMenu = new javax.swing.JMenu();
        addSpriteItem = new javax.swing.JMenuItem();
        addDirItem = new javax.swing.JMenuItem();
        removeDirItem = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        addSpriteButton = new javax.swing.JButton();
        removeSpriteButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        viewPanel = new dfEditor.SpritesheetPanel();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        nameTree = new dfEditor.SpriteTree();
        spriteInfoPanel = new dfEditor.SpriteInfoPanel();
        zoomInButton = new javax.swing.JButton();
        zoomOutButton = new javax.swing.JButton();
        java.net.URL imgURL = this.getClass().getResource("resources/main_icons/Edit.png");
        ImageIcon icon = new ImageIcon(imgURL);
        imageButton = DropDownButtonFactory.createDropDownButton(icon, imagePopup)
        ;
        addFolderButton = new javax.swing.JButton();

        spritePopupMenu.setName("spritePopupMenu"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(dfEditor.dfEditorApp.class).getContext().getResourceMap(SpritesheetController.class);
        removeSpriteItem.setText(resourceMap.getString("removeSpriteItem.text")); // NOI18N
        removeSpriteItem.setName("removeSpriteItem"); // NOI18N
        removeSpriteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSpriteItemActionPerformed(evt);
            }
        });
        spritePopupMenu.add(removeSpriteItem);

        dirPopupMenu.setName("dirPopupMenu"); // NOI18N

        newMenu.setText(resourceMap.getString("newMenu.text")); // NOI18N
        newMenu.setName("newMenu"); // NOI18N

        addSpriteItem.setText(resourceMap.getString("addSpriteItem.text")); // NOI18N
        addSpriteItem.setName("addSpriteItem"); // NOI18N
        addSpriteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSpriteItemActionPerformed(evt);
            }
        });
        newMenu.add(addSpriteItem);

        addDirItem.setText(resourceMap.getString("addDirItem.text")); // NOI18N
        addDirItem.setName("addDirItem"); // NOI18N
        addDirItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDirItemActionPerformed(evt);
            }
        });
        newMenu.add(addDirItem);

        dirPopupMenu.add(newMenu);

        removeDirItem.setText(resourceMap.getString("removeDirItem.text")); // NOI18N
        removeDirItem.setName("removeDirItem"); // NOI18N
        removeDirItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDirItemActionPerformed(evt);
            }
        });
        dirPopupMenu.add(removeDirItem);

        setName("Untitled"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        addSpriteButton.setIcon(resourceMap.getIcon("addSpriteButton.icon")); // NOI18N
        addSpriteButton.setToolTipText(resourceMap.getString("addSpriteButton.toolTipText")); // NOI18N
        addSpriteButton.setBorder(null);
        addSpriteButton.setEnabled(false);
        addSpriteButton.setFocusable(false);
        addSpriteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addSpriteButton.setMaximumSize(new java.awt.Dimension(40, 40));
        addSpriteButton.setMinimumSize(new java.awt.Dimension(0, 0));
        addSpriteButton.setName("addSpriteButton"); // NOI18N
        addSpriteButton.setPreferredSize(new java.awt.Dimension(38, 38));
        addSpriteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSpriteButtonActionPerformed(evt);
            }
        });

        removeSpriteButton.setIcon(resourceMap.getIcon("removeSpriteButton.icon")); // NOI18N
        removeSpriteButton.setToolTipText(resourceMap.getString("removeSpriteButton.toolTipText")); // NOI18N
        removeSpriteButton.setBorder(null);
        removeSpriteButton.setEnabled(false);
        removeSpriteButton.setFocusable(false);
        removeSpriteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeSpriteButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        removeSpriteButton.setMaximumSize(new java.awt.Dimension(100, 100));
        removeSpriteButton.setMinimumSize(new java.awt.Dimension(0, 0));
        removeSpriteButton.setName("removeSpriteButton"); // NOI18N
        removeSpriteButton.setPreferredSize(new java.awt.Dimension(38, 38));
        removeSpriteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSpriteButtonActionPerformed(evt);
            }
        });

        jSplitPane1.setDividerLocation(125);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        viewPanel.setController(this);
        viewPanel.setEnabled(true);
        viewPanel.setName("viewPanel"); // NOI18N
        viewPanel.setCommandManager(this.cmdManager);

        javax.swing.GroupLayout viewPanelLayout = new javax.swing.GroupLayout(viewPanel);
        viewPanel.setLayout(viewPanelLayout);
        viewPanelLayout.setHorizontalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 678, Short.MAX_VALUE)
        );
        viewPanelLayout.setVerticalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 450, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(viewPanel);

        jPanel2.setName("jPanel2"); // NOI18N

        jSplitPane2.setDividerLocation(this.getBounds().height - 150);
        jSplitPane2.setDividerSize(3);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.9);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        nameTree.setName("nameTree"); // NOI18N
        nameTree.setModel(new DefaultTreeModel(new CustomNode("/", true)));

        nameTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        nameTree.setAutoscrolls(true);

        nameTree.setEditable(true);

        nameTree.setEnabled(false);

        nameTree.setInvokesStopCellEditing(true);

        nameTree.setupCellRenderer();

        nameTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                nameTreeMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                nameTreeMouseReleased(evt);
            }
        });
        nameTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                nameTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(nameTree);

        jSplitPane2.setLeftComponent(jScrollPane1);

        spriteInfoPanel.setFocusable(false);
        spriteInfoPanel.setMinimumSize(new java.awt.Dimension(80, 100));
        spriteInfoPanel.setName("spriteInfoPanel"); // NOI18N
        jSplitPane2.setRightComponent(spriteInfoPanel);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(jPanel2);

        zoomInButton.setIcon(resourceMap.getIcon("zoomInButton.icon")); // NOI18N
        zoomInButton.setToolTipText(resourceMap.getString("zoomInButton.toolTipText")); // NOI18N
        zoomInButton.setBorder(null);
        zoomInButton.setEnabled(false);
        zoomInButton.setFocusable(false);
        zoomInButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomInButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        zoomInButton.setMaximumSize(new java.awt.Dimension(100, 100));
        zoomInButton.setMinimumSize(new java.awt.Dimension(0, 0));
        zoomInButton.setName("zoomInButton"); // NOI18N
        zoomInButton.setPreferredSize(new java.awt.Dimension(38, 38));
        zoomInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInButtonActionPerformed(evt);
            }
        });

        zoomOutButton.setIcon(resourceMap.getIcon("zoomOutButton.icon")); // NOI18N
        zoomOutButton.setToolTipText(resourceMap.getString("zoomOutButton.toolTipText")); // NOI18N
        zoomOutButton.setBorder(null);
        zoomOutButton.setEnabled(false);
        zoomOutButton.setFocusable(false);
        zoomOutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomOutButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        zoomOutButton.setMaximumSize(new java.awt.Dimension(100, 100));
        zoomOutButton.setMinimumSize(new java.awt.Dimension(0, 0));
        zoomOutButton.setName("zoomOutButton"); // NOI18N
        zoomOutButton.setPreferredSize(new java.awt.Dimension(38, 38));
        zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutButtonActionPerformed(evt);
            }
        });

        imageButton.setToolTipText(resourceMap.getString("imageButton.toolTipText")); // NOI18N
        imageButton.setBorder(null);
        //imageButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        imageButton.setFocusable(false);
        imageButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        imageButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        imageButton.setMaximumSize(new java.awt.Dimension(100, 100));
        imageButton.setMinimumSize(new java.awt.Dimension(0, 0));
        imageButton.setName("imageButton"); // NOI18N
        imageButton.setPreferredSize(new java.awt.Dimension(38, 38));
        imageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageButtonActionPerformed(evt);
            }
        });

        addFolderButton.setIcon(resourceMap.getIcon("addFolderButton.icon")); // NOI18N
        addFolderButton.setToolTipText(resourceMap.getString("addFolderButton.toolTipText")); // NOI18N
        addFolderButton.setBorder(null);
        addFolderButton.setEnabled(false);
        addFolderButton.setFocusable(false);
        addFolderButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addFolderButton.setMaximumSize(new java.awt.Dimension(40, 40));
        addFolderButton.setMinimumSize(new java.awt.Dimension(0, 0));
        addFolderButton.setName("addFolderButton"); // NOI18N
        addFolderButton.setPreferredSize(new java.awt.Dimension(38, 38));
        addFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFolderButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(addSpriteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addFolderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeSpriteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 534, Short.MAX_VALUE)
                        .addComponent(imageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(zoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 811, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addSpriteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeSpriteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addFolderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(imageButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 835, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 520, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addSpriteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSpriteButtonActionPerformed
        addSpriteAt(viewPanel.suggestVisibleSpriteRect());
}//GEN-LAST:event_addSpriteButtonActionPerformed

    private void removeSpriteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSpriteButtonActionPerformed
        removeSelectedSprites();
}//GEN-LAST:event_removeSpriteButtonActionPerformed

    private void zoomInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInButtonActionPerformed
        viewPanel.setZoom(viewPanel.getZoom() + 0.5f);
}//GEN-LAST:event_zoomInButtonActionPerformed

    private void zoomOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutButtonActionPerformed
        viewPanel.setZoom(viewPanel.getZoom() - 0.5f);
}//GEN-LAST:event_zoomOutButtonActionPerformed

    private void removeSpriteItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSpriteItemActionPerformed
        removeSelectedSprites();
}//GEN-LAST:event_removeSpriteItemActionPerformed

    private void addSpriteItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSpriteItemActionPerformed
        addSpriteAt(viewPanel.suggestVisibleSpriteRect());
}//GEN-LAST:event_addSpriteItemActionPerformed

    public void addSpriteAt(java.awt.Rectangle aRect)
    {
        SelectionBox box = new SelectionBox(aRect, java.awt.Color.blue);

        CustomNode parentNode = (CustomNode)nameTree.getSelectedNodeDir();
        if (parentNode == null)
            parentNode = _lastSelectedDirNode;

        if (parentNode == null)
            parentNode = (CustomNode)nameTree.getModel().getRoot();

        cmdManager.execute(new AddGraphicToSheetCommand(nameTree, parentNode, viewPanel, box));
        box.setColour(((CustomNode)parentNode.getLastChild()).getColour());
        viewPanel.selectGraphic(box);
    }
    
    private void addDirItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDirItemActionPerformed

        addDirToSelectedNode();
}//GEN-LAST:event_addDirItemActionPerformed

    private void removeDirItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDirItemActionPerformed

        removeSelectedSprites();
}//GEN-LAST:event_removeDirItemActionPerformed

    private void addFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFolderButtonActionPerformed
        addDirToSelectedNode();
    }//GEN-LAST:event_addFolderButtonActionPerformed

    private void imageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageButtonActionPerformed
        imagePopup.show(imageButton, 0, imageButton.getHeight());
    }//GEN-LAST:event_imageButtonActionPerformed

    private void addDirToSelectedNode()
    {
        CustomNode parentNode = (CustomNode)nameTree.getSelectedNodeDir();
        if (parentNode != null)
        {
            CustomNode newNode = new CustomNode(parentNode.suggestNameForChildDir(), true);
            cmdManager.execute(new AddDirNodeCommand(nameTree, parentNode, newNode));
        }
    }

    private void showSetImageChooser()
    {
        JFileChooser chooser = fileChooser;

        ImageFilter filter = new ImageFilter();
        chooser.resetChoosableFileFilters();
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Choose an image...");
        JFrame mainFrame = dfEditorApp.getApplication().getMainFrame();
        int returnVal = chooser.showOpenDialog(mainFrame);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
           setImage(chooser.getSelectedFile());
        }
    }

    private void setImage(File aFile)
    {
        try 
        {
            BufferedImage image = ImageIO.read(aFile);
            boolean bImage = (image != null);

            zoomInButton.setEnabled(bImage);
            zoomOutButton.setEnabled(bImage);
            addSpriteButton.setEnabled(bImage);
            addFolderButton.setEnabled(bImage);
            nameTree.setEnabled(bImage);
            packItem.setEnabled(bImage);
            changeColourItem.setEnabled(bImage);
            savedImageFile = aFile;
            savedImageFormat = dfEditor.io.Utils.getExtension(aFile);
            
            if (bImage)
            {                
                bImageModified = false;
                viewPanel.setImage(image);
                viewPanel.repaint();
            
                // start on root node
                CustomNode rootNode = (CustomNode)nameTree.getModel().getRoot();
                nameTree.setSelectionPath(new TreePath(rootNode.getPath()));

                setTip();
            }
        }
        catch (IOException e)
        {
            // TODO: show a dialog or something
        }
    }

    private void removeSelectedSprites()
    {
        CustomNode[] selectedNodes = nameTree.getSelectedNodes();
        cmdManager.execute(new RemoveGraphicListCommand(nameTree, viewPanel, selectedNodes));
    }

    @Override
    public boolean hasBeenModified()
    {        
        return super.hasBeenModified() || bImageModified;
    }
    
    public boolean save()
    {
        boolean bOK = true;
        
        if (bImageModified)
        {
            if (savedImageFile != null && savedImageFile.exists())
            {
                int response = JOptionPane.showConfirmDialog (null,
                           "The image \"" + savedImageFile.getName() + "\" has been modified, would you like to overwrite it?", "Overwrite image?",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                switch (response)
                {
                   case JOptionPane.CANCEL_OPTION:
                       bOK = false;
                       break;
                   case JOptionPane.YES_OPTION:
                       bOK &= saveImage(savedImageFile, savedImageFormat);            
                       break;
                }
            }
            else
            {
                JOptionPane.showMessageDialog(
                    this,
                    "The image has been altered. You will now be prompted to save it.",
                    "Image has been modified",
                    JOptionPane.INFORMATION_MESSAGE); 
                
                 bOK &= saveImageAs();
            }
        }
        
        if (savedFile != null)
        {
            bOK &= saveCoords(savedFile);        
        }
        else
        {
            bOK &= saveCoordsAs();
        }
        
        return bOK;
    }
    
    public boolean saveAs()
    {    
        boolean bOK = true;
        
        if (bImageModified)
        {    
            JOptionPane.showMessageDialog(
                this,
                "The image has been altered. You will now be prompted to save it.",
                "Image has been modified",
                JOptionPane.INFORMATION_MESSAGE);            
              
            bOK &= saveImageAs();       
        }
        bOK &= saveCoordsAs();        
        
        return bOK;
    }
    
    public boolean saveCoords(File aFile)
    {     
        SpritesheetWriter writer = new SpritesheetWriter();
        try 
        {
            writer.createSpriteSheet(aFile, savedImageFile.getName(), nameTree, viewPanel.getImage().getWidth(), viewPanel.getImage().getHeight());
        } 
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(
                this,
                "Could not save the spritesheet!\n\n"+e.getMessage(),
                "Spritesheet not saved",
                JOptionPane.ERROR_MESSAGE);
            {
                return false;                
            }
        }
        
        this.setName(aFile.getName());
        
        if (helpLabel != null)
            helpLabel.setText("Spritesheet definition saved as " + aFile.toString());
        
        setModified(false);
        savedFile = aFile;
        
        return true;
    }
    

    public boolean saveCoordsAs()
    {
        boolean bOK = false;
        
        java.io.File f = null;
        
        JFileChooser chooser = fileChooser;
        
        CustomFilter filter = new CustomFilter();
        filter.addExtension(CustomFilter.EXT_SPRITE);
        chooser.resetChoosableFileFilters();
        chooser.setFileFilter(filter);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setApproveButtonText("Save coordinates");
        chooser.setDialogTitle("Save spritesheet");
        chooser.setSelectedFile(new File("newSpriteSheet.sprites"));
        JFrame mainFrame = dfEditorApp.getApplication().getMainFrame();
        while (true)
        {
            int returnVal = chooser.showSaveDialog(mainFrame);
            if(returnVal == JFileChooser.APPROVE_OPTION)
            {
                f = chooser.getSelectedFile();
                if(null == dfEditor.io.Utils.getExtension(f))
                {
                    f = new java.io.File(new String(f.getAbsolutePath() + "." + filter.getExtension()));
                }

                if (f.exists())
                {
                    //Custom button text
                    int response = JOptionPane.showConfirmDialog (null,
                       "Overwrite existing coordinates?","Confirm Overwrite",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    if (response == JOptionPane.CANCEL_OPTION)
                        continue;
                }

                bOK = saveCoords(f);                
            }
            break;            
        }   
        return bOK;
    }

    private boolean saveImageAs()
    {      
        boolean bOK = false;
        
        JFileChooser chooser = fileChooser;

        ImageFilter filter = new ImageFilter();
        chooser.resetChoosableFileFilters();
        chooser.setFileFilter(filter);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setApproveButtonText("Save image");
        chooser.setDialogTitle("Save modified image");
        chooser.setSelectedFile(new File("newSpriteSheet.png"));
        
        JFrame mainFrame = dfEditorApp.getApplication().getMainFrame();
        while (true)
        {
            int returnVal = chooser.showSaveDialog(mainFrame);
            if(returnVal == JFileChooser.APPROVE_OPTION)
            {                
                java.io.File f = chooser.getSelectedFile();

                if (f.exists())
                {
                    //Custom button text
                    int response = JOptionPane.showConfirmDialog (null,
                       "Overwrite existing image?","Confirm Overwrite",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    if (response == JOptionPane.CANCEL_OPTION)
                        continue;                   
                }
                String[] supportedFormats = ImageIO.getWriterFormatNames();
                ArrayList<String> list = new ArrayList<String>();
                
                for(int i=0; i<supportedFormats.length; ++i)
                {
                    boolean bGotAlready = false;
                    for (int j=0; j<list.size(); ++j)
                    {
                        if (supportedFormats[i].compareToIgnoreCase(list.get(j)) == 0)
                        {
                            bGotAlready = true;
                            break;
                        }
                    }
                    if (!bGotAlready)
                    {
                        list.add(supportedFormats[i].toLowerCase());
                    }
                }
                String formatName = null;

                for (int i=0; i<supportedFormats.length; ++i)
                {                    
                    if (Utils.getExtension(f) != null &&
                        Utils.getExtension(f).equals(supportedFormats[i]))
                    {
                        formatName = supportedFormats[i];
                    }
                }
                if (formatName != null)
                {
                    bOK = saveImage(f, formatName);
                }
                else
                {
                    String formatsWithCommas = new String();
                    for (int i=0; i<list.size(); ++i)
                    {
                        formatsWithCommas += list.get(i);
                        if (i < list.size() - 1)
                            formatsWithCommas += ", ";
                    }

                    JOptionPane.showMessageDialog(
                            this,
                            "Unsupported image format. The following formats are supported:\n\n" + formatsWithCommas,
                            "Image save failed",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            break;
        }
        
        return bOK;
    }
    
    private boolean saveImage(File aFile, String aFormat)
    {
        boolean bOK = false;
        try
        {
            ImageIO.write(viewPanel.getImage(), aFormat, aFile);                    
            savedImageFile = aFile;
            savedImageFormat = aFormat;
            bImageModified = false;
            bOK = true;
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(
                this,
                "Could not save the image!\n\n"+e.getMessage(),
                "Image not saved",
                JOptionPane.ERROR_MESSAGE);
        }   
        return bOK;
    }
    
    private void nameTreePopup(java.awt.Point aPos)
    {
        TreePath selectionPath = nameTree.getSelectionPath();
        if (selectionPath != null)
        {
            CustomNode selectedNode = (CustomNode)(selectionPath.getLastPathComponent());
            if (selectedNode != null)
            {
                if (selectedNode.isLeaf())
                    spritePopupMenu.show(nameTree, aPos.x, aPos.y);
                else
                    dirPopupMenu.show(nameTree, aPos.x, aPos.y);

                if (selectedNode.isRoot())
                    removeDirItem.setEnabled(false);
                else
                    removeDirItem.setEnabled(true);
            }
        }
        else
        {
             //helpLabel.setText("You must make a selection first!"); //TODO
        }
    }

    private void nameTreeMousePressed(java.awt.event.MouseEvent evt) {
        if (!nameTree.isEnabled())
            return;

        if (evt.isPopupTrigger())
            nameTreePopup(evt.getPoint());
    }

    private void nameTreeMouseReleased(java.awt.event.MouseEvent evt) {
         if (!nameTree.isEnabled())
            return;

        if (evt.isPopupTrigger())
            nameTreePopup(evt.getPoint());
    }

    private void refreshSpriteInfoPanel()
    {        
        spriteInfoPanel.setSprite(" ", null, null);
        
        TreePath[] parentPaths = nameTree.getSelectionPaths();
        CustomNode selectedNode = null;
        if (parentPaths != null)
        {
            for (int i=0; i<parentPaths.length; ++i)
            {
                TreePath parentPath = parentPaths[i];
                selectedNode = (CustomNode) (parentPath.getLastPathComponent());
                if (selectedNode.isLeaf() && parentPaths.length == 1)
                {
                    SelectionBox spriteArea = (SelectionBox)selectedNode.getCustomObject();
                    SpriteGraphic graphic = new SpriteGraphic(viewPanel.getImage(), new java.awt.Point(0,0), spriteArea.getRect());                   
                    spriteInfoPanel.setSprite(selectedNode.getFullPathName(), spriteArea.getRect(), graphic);                
                }
            }
        }
    }
    
    private void nameTreeValueChanged(javax.swing.event.TreeSelectionEvent evt)
    {        
        refreshSpriteInfoPanel();
        
        ArrayList<GraphicObject> nowSelected = new ArrayList<GraphicObject>();
        
        TreePath[] parentPaths = nameTree.getSelectionPaths();
        CustomNode selectedNode = null;
        if (parentPaths != null)
        {
            for (int i=0; i<parentPaths.length; ++i)
            {
                TreePath parentPath = parentPaths[i];
                selectedNode = (CustomNode) (parentPath.getLastPathComponent());
                if (selectedNode.isLeaf())
                {
                    GraphicObject selectedGraphic = (GraphicObject)selectedNode.getCustomObject();
                    if (selectedGraphic != null)
                    {
                        selectedGraphic.setSelected(true);
                        viewPanel.bringGraphicToFront(selectedGraphic);
                        nowSelected.add(selectedGraphic);
                    }
                }
                else
                {
                    _lastSelectedDirNode = (CustomNode)nameTree.getSelectedNodeDir();                    
                }                
            }
           
            removeSpriteButton.setEnabled(true);
            addSpriteButton.setEnabled(true);
            addFolderButton.setEnabled(true);
        }
        else
        {
            addSpriteButton.setEnabled(false);
            addFolderButton.setEnabled(false);
            removeSpriteButton.setEnabled(false);            
        }

        ArrayList<GraphicObject> graphics = viewPanel.selectedGraphics();
        for (int i=0; i<graphics.size(); ++i)
        {
            GraphicObject go = graphics.get(i);
            if (!nowSelected.contains(go))
                go.setSelected(false);
        }

        viewPanel.repaint();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addDirItem;
    private javax.swing.JButton addFolderButton;
    private javax.swing.JButton addSpriteButton;
    private javax.swing.JMenuItem addSpriteItem;
    private javax.swing.JPopupMenu dirPopupMenu;
    private javax.swing.JButton imageButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private dfEditor.SpriteTree nameTree;
    private javax.swing.JMenu newMenu;
    private javax.swing.JMenuItem removeDirItem;
    private javax.swing.JButton removeSpriteButton;
    private javax.swing.JMenuItem removeSpriteItem;
    private dfEditor.SpriteInfoPanel spriteInfoPanel;
    private javax.swing.JPopupMenu spritePopupMenu;
    private dfEditor.SpritesheetPanel viewPanel;
    private javax.swing.JButton zoomInButton;
    private javax.swing.JButton zoomOutButton;
    // End of variables declaration//GEN-END:variables

}
