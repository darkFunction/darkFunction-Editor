/* 
 *  Copyright 2011 Samuel Taylor
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
 * SpriteImageController.java
 *
 * Created on 22-May-2011, 02:22:40
 */

package dfEditor;

import dfEditor.command.*;
import dfEditor.commands.*;
import dfEditor.commands.RemoveGraphicListCommand;
import dfEditor.io.*;
import javax.swing.tree.DefaultTreeModel;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.tree.TreePath;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.awt.Rectangle;
import javax.swing.tree.TreeSelectionModel;
/**
 *
 * @author s4m20
 */
public class SpriteImageController extends dfEditorPanel implements GraphicPanelChangeListener
{
    protected CustomNode _lastSelectedDirNode;
    private File savedImageFile = null;
    private String savedImageFormat = null;

    /** Creates new form SpriteImageController */
    public SpriteImageController(
            CommandManager aCmdManager, JLabel aHelpLabel, TaskChangeListener aListener, JFileChooser aChooser)
    {
        super(aCmdManager, aHelpLabel, aListener, aChooser);
        
        initComponents();

        viewPanel.setTextureArea(textureWidthField.getNum(), textureHeightField.getNum());
        viewPanel.addGraphicChangeListener(this);
        viewPanel.setCommandManager(aCmdManager);
        
        postInit();
    }

    public boolean save()
    {
        boolean bOK = true;
        
        if (savedImageFile != null)
        {
            int response = JOptionPane.showConfirmDialog (null,
                       "Overwrite image \""+ savedImageFile.getName() +"\"?","Image has been modified",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.YES_OPTION)
            {
                bOK &= saveImage(savedImageFile, savedImageFormat);    
            }
        }
        else
        {
            bOK &= saveImageAs();
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
        
        bOK &= saveImageAs();       
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

    public void graphicSelectionChanged(GraphicPanel aPanel, GraphicObject aGraphic)
    {
        viewPanel.bringGraphicToFront(aGraphic);
    }

    public void graphicAdded(GraphicPanel aPanel, GraphicObject aGraphic) {}
    public void graphicMoved(GraphicPanel aPanel, GraphicObject aGraphic){}
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

    private void nameTreeValueChanged(javax.swing.event.TreeSelectionEvent evt)
    {
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
        
        savedImageFile = imgFile;        
        savedImageFormat = dfEditor.io.Utils.getExtension(imgFile);

        BufferedImage bufferedImage = null;
        
        try
        {
            bufferedImage = ImageIO.read(imgFile);
            
        }
        catch (IOException e)
        {
             JOptionPane.showMessageDialog(
                this,
                "Couldn't load texture image.",
                "Error loading spritesheet!",
                JOptionPane.ERROR_MESSAGE);
        }
        
        if (bufferedImage != null)
        {
            nameTree.setModel(aModel);
            nameTree.setEnabled(true);
            _lastSelectedDirNode = null;

            // add everything to the graphic panel
            addNodeToPanel((CustomNode)aModel.getRoot(), bufferedImage);


            nameTree.setupCellRenderer();

            return true;
        }
        return false;
    }
    
    private void addNodeToPanel(CustomNode aNode, BufferedImage aImage)
    {
        if (aNode.isLeaf())
        {
            GraphicObject sb = (GraphicObject)aNode.getCustomObject();
            
            // what we have here is actually a SelectionBox - we need to change it
            // into a SpriteGraphic
            Rectangle r = sb.getRect();
            SpriteGraphic graphic = new SpriteGraphic(aImage, new java.awt.Point(r.x, r.y), r);
            aNode.setCustomObject(graphic);
            
            int w = aImage.getWidth();
            int h = aImage.getHeight();
            textureWidthField.setText(Integer.toString(w));
            textureHeightField.setText(Integer.toString(h));
            viewPanel.setTextureArea(w, h);
            
            viewPanel.addGraphic(graphic);
        }
        else
        {
            for (int i=0; i<aNode.getChildCount(); ++i)
            {
                addNodeToPanel((CustomNode)aNode.getChildAt(i), aImage);
            }
        }
    }

//    private void addNodeToPanel()
//    {
//        try
//                {
//                    BufferedImage bufferedImage = ImageIO.read(selectedFiles[i]);
//                    SpriteGraphic graphic = new SpriteGraphic(bufferedImage, new java.awt.Point(0, 0), new Rectangle(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight()));
//                    graphicList.add(graphic);
//                }
//                catch (IOException e)
//                {
//                     JOptionPane.showMessageDialog(
//                        this,
//                        "Could not import "+selectedFiles[i].getName(),
//                        "Image not added",
//                        JOptionPane.ERROR_MESSAGE);
//                }
//    }
    
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

                PixelPacker packer = new PixelPacker();
                if (! packer.packRects(viewPanel.getGraphicsBounds(), array, minSpacingField.getNum()))
                {
                    JOptionPane.showMessageDialog(
                        this,
                        "Couldn't fit all the images into the texture without overlapping. Try increasing the texture size, shrinking the spacing, or placing some sprites manually.",
                        "Texture too small",
                        JOptionPane.ERROR_MESSAGE);
                }

                viewPanel.dropAllGraphics();

                viewPanel.repaint();

                setModified(true);
                taskChangeListener.taskChanged(this);
            }
        }
    }

    private void removeSelectedSprites()
    {
        CustomNode[] selectedNodes = nameTree.getSelectedNodes();
        
        if (selectedNodes != null)
            cmdManager.execute(new RemoveGraphicListCommand(nameTree, viewPanel, selectedNodes));
    }

    private void addDirToSelectedNode()
    {
        CustomNode parentNode = (CustomNode)nameTree.getSelectedNodeDir();
        if (parentNode != null)
        {
            CustomNode newNode = new CustomNode(parentNode.suggestNameForChildDir(), true);
            cmdManager.execute(new AddDirNodeCommand(nameTree, parentNode, newNode));
        }
    }

    private File[] showSetImageChooser()
    {
        JFileChooser chooser = fileChooser;

        ImageFilter filter = new ImageFilter();
        chooser.resetChoosableFileFilters();
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Choose images to import...");
        chooser.setMultiSelectionEnabled(true);
        JFrame mainFrame = dfEditorApp.getApplication().getMainFrame();
        int returnVal = chooser.showOpenDialog(mainFrame);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
           return chooser.getSelectedFiles();
        }
        return null;
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
        spriteImageController = new javax.swing.JPanel();
        addSpriteButton = new javax.swing.JButton();
        removeSpriteButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        nameTree = new dfEditor.SpriteTree();
        viewPanel = new dfEditor.SpriteImagePanel();
        zoomInButton = new javax.swing.JButton();
        zoomOutButton = new javax.swing.JButton();
        addFolderButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        trimButton = new javax.swing.JButton();
        untrimButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        packButton = new javax.swing.JButton();
        textureWidthField = new dfEditor.IntegerTextField();
        textureHeightField = new dfEditor.IntegerTextField();
        minSpacingField = new dfEditor.IntegerTextField();

        spritePopupMenu.setName("spritePopupMenu"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(dfEditor.dfEditorApp.class).getContext().getResourceMap(SpriteImageController.class);
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

        spriteImageController.setName("spriteImageController"); // NOI18N

        addSpriteButton.setIcon(resourceMap.getIcon("addSpriteButton.icon")); // NOI18N
        addSpriteButton.setToolTipText(resourceMap.getString("addSpriteButton.toolTipText")); // NOI18N
        addSpriteButton.setBorder(null);
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

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        nameTree.setName("nameTree"); // NOI18N
        nameTree.setModel(new DefaultTreeModel(new CustomNode("/", true)));

        nameTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        nameTree.setAutoscrolls(true);

        nameTree.setDropMode(javax.swing.DropMode.ON);

        nameTree.setEditable(true);

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

        jSplitPane1.setLeftComponent(jScrollPane1);

        viewPanel.setName("viewPanel"); // NOI18N

        javax.swing.GroupLayout viewPanelLayout = new javax.swing.GroupLayout(viewPanel);
        viewPanel.setLayout(viewPanelLayout);
        viewPanelLayout.setHorizontalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 624, Short.MAX_VALUE)
        );
        viewPanelLayout.setVerticalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 345, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(viewPanel);

        zoomInButton.setIcon(resourceMap.getIcon("zoomInButton.icon")); // NOI18N
        zoomInButton.setToolTipText(resourceMap.getString("zoomInButton.toolTipText")); // NOI18N
        zoomInButton.setBorder(null);
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

        addFolderButton.setIcon(resourceMap.getIcon("addFolderButton.icon")); // NOI18N
        addFolderButton.setToolTipText(resourceMap.getString("addFolderButton.toolTipText")); // NOI18N
        addFolderButton.setBorder(null);
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

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        trimButton.setText(resourceMap.getString("trimButton.text")); // NOI18N
        trimButton.setFocusable(false);
        trimButton.setName("trimButton"); // NOI18N
        trimButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trimButtonActionPerformed(evt);
            }
        });

        untrimButton.setText(resourceMap.getString("untrimButton.text")); // NOI18N
        untrimButton.setFocusable(false);
        untrimButton.setName("untrimButton"); // NOI18N
        untrimButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                untrimButtonActionPerformed(evt);
            }
        });

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        packButton.setText(resourceMap.getString("packButton.text")); // NOI18N
        packButton.setFocusable(false);
        packButton.setName("packButton"); // NOI18N
        packButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                packButtonActionPerformed(evt);
            }
        });

        textureWidthField.setColumns(4);
        textureWidthField.setText(resourceMap.getString("textureWidthField.text")); // NOI18N
        textureWidthField.setName("textureWidthField"); // NOI18N
        textureWidthField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textureWidthFieldActionPerformed(evt);
            }
        });

        textureHeightField.setColumns(4);
        textureHeightField.setText(resourceMap.getString("textureHeightField.text")); // NOI18N
        textureHeightField.setName("textureHeightField"); // NOI18N
        textureHeightField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textureHeightFieldActionPerformed(evt);
            }
        });

        minSpacingField.setText(resourceMap.getString("minSpacingField.text")); // NOI18N
        minSpacingField.setName("minSpacingField"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(untrimButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(trimButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(textureWidthField, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textureHeightField, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(minSpacingField, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(packButton)
                .addContainerGap(328, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(trimButton)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textureWidthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(textureHeightField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(untrimButton)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minSpacingField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(packButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout spriteImageControllerLayout = new javax.swing.GroupLayout(spriteImageController);
        spriteImageController.setLayout(spriteImageControllerLayout);
        spriteImageControllerLayout.setHorizontalGroup(
            spriteImageControllerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spriteImageControllerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(spriteImageControllerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(spriteImageControllerLayout.createSequentialGroup()
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 755, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(spriteImageControllerLayout.createSequentialGroup()
                        .addComponent(addSpriteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addFolderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeSpriteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 547, Short.MAX_VALUE)
                        .addComponent(zoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10))
                    .addGroup(spriteImageControllerLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        spriteImageControllerLayout.setVerticalGroup(
            spriteImageControllerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spriteImageControllerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(spriteImageControllerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addSpriteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeSpriteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addFolderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 775, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(spriteImageController, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 509, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(spriteImageController, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addSpriteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSpriteButtonActionPerformed
        File[] selectedFiles = this.showSetImageChooser();
        if (selectedFiles != null)
        {
            CustomNode parentNode = (CustomNode)nameTree.getSelectedNodeDir();
            if (parentNode == null)
                parentNode = _lastSelectedDirNode;

            if (parentNode == null)
                parentNode = (CustomNode)nameTree.getModel().getRoot();

            ArrayList<GraphicObject> graphicList = new ArrayList();
            for (int i=0; i<selectedFiles.length; ++i)
            {
                try
                {
                    BufferedImage bufferedImage = ImageIO.read(selectedFiles[i]);
                    SpriteGraphic graphic = new SpriteGraphic(bufferedImage, new java.awt.Point(0, 0), new Rectangle(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight()));
                    graphicList.add(graphic);
                }
                catch (IOException e)
                {
                     JOptionPane.showMessageDialog(
                        this,
                        "Could not import "+selectedFiles[i].getName(),
                        "Image not added",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
            cmdManager.execute(new AddGraphicListToSheetCommand(nameTree, parentNode, viewPanel, graphicList));
        }
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

    private void addFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFolderButtonActionPerformed
       addDirToSelectedNode();
}//GEN-LAST:event_addFolderButtonActionPerformed

    private void removeSpriteItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSpriteItemActionPerformed
        removeSelectedSprites();
}//GEN-LAST:event_removeSpriteItemActionPerformed

    private void addSpriteItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSpriteItemActionPerformed
        //addSpriteAt(viewPanel.suggestVisibleSpriteRect());
}//GEN-LAST:event_addSpriteItemActionPerformed

    private void addDirItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDirItemActionPerformed

        addDirToSelectedNode();
}//GEN-LAST:event_addDirItemActionPerformed

    private void removeDirItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDirItemActionPerformed

        removeSelectedSprites();
}//GEN-LAST:event_removeDirItemActionPerformed

    private void textureWidthFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textureWidthFieldActionPerformed
        
        viewPanel.setTextureArea(textureWidthField.getNum(), textureHeightField.getNum());
        viewPanel.repaint();
    }//GEN-LAST:event_textureWidthFieldActionPerformed

    private void textureHeightFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textureHeightFieldActionPerformed
        viewPanel.setTextureArea(textureWidthField.getNum(), textureHeightField.getNum());
        viewPanel.repaint();
    }//GEN-LAST:event_textureHeightFieldActionPerformed

    private void packButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packButtonActionPerformed
        pack();
    }//GEN-LAST:event_packButtonActionPerformed

    private void trimButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trimButtonActionPerformed
        ArrayList<GraphicObject> selectedGraphics = viewPanel.selectedGraphics();

        for (int i=0; i<selectedGraphics.size(); ++i)
        {
            SpriteGraphic sprite = (SpriteGraphic)selectedGraphics.get(i);
            sprite.trim();
        }

        repaint();
    }//GEN-LAST:event_trimButtonActionPerformed

    private void untrimButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_untrimButtonActionPerformed
        ArrayList<GraphicObject> selectedGraphics = viewPanel.selectedGraphics();

        for (int i=0; i<selectedGraphics.size(); ++i)
        {
            SpriteGraphic sprite = (SpriteGraphic)selectedGraphics.get(i);
            sprite.untrim();
        }

        repaint();
    }//GEN-LAST:event_untrimButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addDirItem;
    private javax.swing.JButton addFolderButton;
    private javax.swing.JButton addSpriteButton;
    private javax.swing.JMenuItem addSpriteItem;
    private javax.swing.JPopupMenu dirPopupMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private dfEditor.IntegerTextField minSpacingField;
    private dfEditor.SpriteTree nameTree;
    private javax.swing.JMenu newMenu;
    private javax.swing.JButton packButton;
    private javax.swing.JMenuItem removeDirItem;
    private javax.swing.JButton removeSpriteButton;
    private javax.swing.JMenuItem removeSpriteItem;
    private javax.swing.JPanel spriteImageController;
    private javax.swing.JPopupMenu spritePopupMenu;
    private dfEditor.IntegerTextField textureHeightField;
    private dfEditor.IntegerTextField textureWidthField;
    private javax.swing.JButton trimButton;
    private javax.swing.JButton untrimButton;
    private dfEditor.SpriteImagePanel viewPanel;
    private javax.swing.JButton zoomInButton;
    private javax.swing.JButton zoomOutButton;
    // End of variables declaration//GEN-END:variables

}
