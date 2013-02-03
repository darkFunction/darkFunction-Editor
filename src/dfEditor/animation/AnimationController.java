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
 * AnimationController.java
 *
 * Created on 06-Dec-2009, 23:39:51
 */

package dfEditor.animation;

import dfEditor.*;
import dfEditor.command.*;
import dfEditor.commands.*;
import dfEditor.io.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.Point;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import dfEditor.CustomComponents.*; 
import javax.swing.JTextField;
import javax.swing.plaf.basic.*;
import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.awt.*;
import java.awt.event.FocusListener;
/**
 *
 * @author Owner
 */
public class AnimationController extends dfEditorPanel implements
            ListSelectionListener,
            GraphicPanelChangeListener,
            AnimationStripListener,
            AnimationDataListener,
            SpriteTreeListener,
            NodeDroppedListener,
            ActionListener,
            InternalFrameListener
{
    private BufferedImage bufferedImage = null;
    private AnimationCell workingCell = null;    
    private File loadedSpritesheetFile = null;
    private Timer spinnerStateTimer = null;
    ArrayList<GraphicObject> _rotatingGraphics = null;

    /** Creates new form AnimationController */
    public AnimationController(CommandManager aCmdManager, boolean aNew, JLabel aHelpLabel, TaskChangeListener aListener, JFileChooser aChooser)
    {
        super(aCmdManager, aHelpLabel, aListener, aChooser);

        initComponents();

        viewPanel.addGraphicChangeListener(this);
        animationStripPanel.setController(this);
        
        animationStripPanel.setCommandManager(aCmdManager);
        viewPanel.setCommandManager(aCmdManager);

        animationList.addListSelectionListener(this);   
        
        spriteList.addListSelectionListener(this);
        spriteList.addNodeDroppedListener(this);
        spriteList.setDragSource(spriteTree);
        
        spriteTree.addTreeListener(this);        
        viewPanel.addNodeDroppedListener(this);
        viewPanel.setDragSource(spriteTree);

        controlPanel.addInternalFrameListener(this);
        spriteListControlPanel.addInternalFrameListener(this);
        
        setWorkingCell(null);

        if (aNew)
        {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showSpritesheetChooser();
                }
            });
        }
        
        viewPanel.requestFocus();        
        
        postInit();      
    }

    private void showSpritesheetChooser()
    {
        JFileChooser chooser = fileChooser;

        CustomFilter filter = new CustomFilter();
        filter.addExtension(CustomFilter.EXT_SPRITE);
        chooser.resetChoosableFileFilters();
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Select spritesheet");
        
        JFrame mainFrame = dfEditorApp.getApplication().getMainFrame();
        int returnVal = chooser.showOpenDialog(mainFrame);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
           setSpritesheetFile(chooser.getSelectedFile());
        }
    }

    private void setSpritesheetFile(File aFile)
    {
        SpritesheetReader reader = new SpritesheetReader(aFile);
        loadedSpritesheetFile = aFile;
       
        try
        {
            String imgPath = reader.getImagePath();
            if (imgPath != null)
                bufferedImage = ImageIO.read(new File(imgPath));
        }
        catch (IOException e)
        {
            showParseError();
            return;
        }

        DefaultTreeModel model = reader.getTreeModel();

        if (model != null)
        {
            spriteTree.setModel(model);
            addAnimationButton.setEnabled(true);
        }
        else
        {
            showParseError();
            return;
        }
    }
    
    private void buildAnimatedGif(String filePath)
    {
        Animation animation = this.getWorkingAnimation();
        
        if (animation != null && animation.numCells() > 0)
        {
            Rectangle firstCellRect = animation.getCellAtIndex(0).getSpreadRect();
            Point topLeft = new Point(firstCellRect.x, firstCellRect.y);
            Point bottomRight = new Point(firstCellRect.x + firstCellRect.width, firstCellRect.y + firstCellRect.height);
            
            for (int i=1; i<animation.numCells(); i++)
            {
                Rectangle r = animation.getCellAtIndex(i).getSpreadRect();
                
                if (r.x < topLeft.x)
                    topLeft.x = r.x;
                if (r.y < topLeft.y)
                    topLeft.y = r.y;
                if (r.x + r.width > bottomRight.x)
                    bottomRight.x = r.x + r.width;
                if (r.y + r.height > bottomRight.y)
                    bottomRight.y = r.y + r.height;             
            }
                
            AnimatedGifEncoder e = new AnimatedGifEncoder();
            e.setTransparent(new Color(0,0,0,0));
            e.start(filePath);            
            e.setRepeat(animation.getLoops());
            e.setQuality(1);
            e.setSize(bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
            
            for (int i=0; i<animation.numCells(); i++)
            {
                BufferedImage image = new BufferedImage(bottomRight.x - topLeft.x, bottomRight.y - topLeft.y, BufferedImage.TYPE_INT_ARGB);
                Graphics ig = image.getGraphics();
            
                AnimationCell cell = animation.getCellAtIndex(i);                
                cell.rebuild();
                
                Rectangle r = cell.getSpreadRect();
                r.x -= topLeft.x;
                r.y -= topLeft.y; 
                cell.draw(ig, r);              
                
                e.addFrame(image);
                e.setDelay(cell.getDelay());
            }
            
            e.finish();           
        }
    }
    
    private void showParseError()
    {        
        JOptionPane.showMessageDialog(this, "File could not be loaded", "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void spriteTreeDoubleClicked(CustomNode aNode)
    {
        if (aNode != null && getWorkingCell() != null && aNode.isLeaf())
            addNodeToCell(aNode, getWorkingCell(), new Point(0,0));
    }

    public void setWorkingCell(final AnimationCell aCell)
    {
        // commented out to force repaint
        //if(aCell == workingCell)
        //    return;

        if (workingCell != aCell)
            viewPanel.unselectAllGraphics();
        
        workingCell = aCell;
        viewPanel.setCell(workingCell);
        animationStripPanel.selectCell(aCell);

        boolean bActiveCell = (aCell != null);

        zoomInButton.setEnabled(bActiveCell);
        zoomOutButton.setEnabled(bActiveCell);
        onionSkinsCheckBox.setEnabled(bActiveCell);
        spriteTree.setEnabled(bActiveCell);
        viewPanel.setEnabled(bActiveCell);
        delaySpinner.setEnabled(bActiveCell);
        //addToFrameButton.setEnabled(bActiveCell);
        spriteList.setEnabled(bActiveCell);
        try {
            spriteListControlPanel.setSelected(bActiveCell);
        } catch (java.beans.PropertyVetoException e) {}       
        
        populateSpriteListFromCell(aCell);

        if (bActiveCell)
            delaySpinner.setValue(workingCell.getDelay());

        if (aCell != null)
        {
            aCell.rebuild();
            animationStripPanel.repaint();
        }
        
        setOnionSkins(onionSkinsCheckBox.isSelected());         
        
        this.repaint();
    }

    public void cellAdded(Animation aAnimation, AnimationCell aCell)
    {
        if (aAnimation == this.getWorkingAnimation())
        {
            boolean bPlayable = aAnimation.numCells() > 1;
            if (!bPlayable)
            {
                animationStripPanel.stop();
            }
            playButton.setEnabled(bPlayable);
        }
    }

    public void cellRemoved(Animation aAnimation, AnimationCell aCell)
    {
        if (aAnimation == this.getWorkingAnimation())
        {
            boolean bPlayable = aAnimation.numCells() > 1;
            if (!bPlayable)
            {
                animationStripPanel.stop();
            }
            playButton.setEnabled(bPlayable);

            setWorkingCell(aAnimation.getCurrentCell());

        }
    }
    
    public void cellOrderChanged(Animation aAnimation)
    {
        if (aAnimation == this.getWorkingAnimation())
        {
            
        }
    }

    public AnimationCell getWorkingCell()
    {
        return workingCell;
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        animationList = new JListMutable(new DefaultMutableListModel());
        addAnimationButton = new javax.swing.JButton();
        removeAnimationButton = new javax.swing.JButton();
        duplicateAnimationButton = new javax.swing.JButton();
        exportGifButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        animationPanel1 = new dfEditor.animation.AnimationPanel();
        playButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        delaySpinner = new javax.swing.JSpinner();
        addCellButton = new javax.swing.JButton();
        removeCellButton = new javax.swing.JButton();
        animationStripScrollPane = new javax.swing.JScrollPane();
        animationStripPanel = new dfEditor.animation.AnimationStripPanel();
        loopSpinner = new javax.swing.JSpinner();
        loopLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        spriteTree = new dfEditor.SpriteTree();
        spritePreviewPanel = new dfEditor.SimpleGraphicPanel();
        addToFrameButton = new javax.swing.JButton();
        spritePreviewTitle = new javax.swing.JLabel();
        viewPanel = new dfEditor.animation.AnimationPanel();
        zoomInButton = new javax.swing.JButton();
        zoomOutButton = new javax.swing.JButton();
        controlPanel = new javax.swing.JInternalFrame();
        jToolBar1 = new javax.swing.JToolBar();
        removeSpriteButton = new javax.swing.JButton();
        rotateCWButton = new javax.swing.JButton();
        rotateACWButton = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        flipVCheckBox = new javax.swing.JCheckBox();
        flipHCheckBox = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        angleLabel = new javax.swing.JLabel();
        angleSpinner = new javax.swing.JSpinner(new RollOverSpinModel(0, 0, 360, 1));
        jPanel5 = new javax.swing.JPanel();
        zOrderLabel = new javax.swing.JLabel();
        zOrderSpinner = new javax.swing.JSpinner(new CustomSpinModel());
        spriteListControlPanel = new javax.swing.JInternalFrame();
        jScrollPane3 = new javax.swing.JScrollPane();
        spriteList = new dfEditor.SpriteList();
        onionSkinsCheckBox = new javax.swing.JCheckBox();
        spriteListToggle = new javax.swing.JToggleButton();
        modifySpriteToggle = new javax.swing.JToggleButton();

        setName("Untitled"); // NOI18N
        setPreferredSize(new java.awt.Dimension(800, 600));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(dfEditor.dfEditorApp.class).getContext().getResourceMap(AnimationController.class);
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setFocusable(false);
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(180, 179));

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        animationList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        animationList.setName("animationList"); // NOI18N
        animationList.setPreferredSize(null);
        jScrollPane2.setViewportView(animationList);
        animationList.setModel(new DefaultMutableListModel());
        ((JListMutable)animationList).setListCellEditor(new DefaultListCellEditor(new JTextField()));

        addAnimationButton.setIcon(resourceMap.getIcon("addAnimationButton.icon")); // NOI18N
        addAnimationButton.setText(resourceMap.getString("addAnimationButton.text")); // NOI18N
        addAnimationButton.setToolTipText(resourceMap.getString("addAnimationButton.toolTipText")); // NOI18N
        addAnimationButton.setAlignmentX(0.5F);
        addAnimationButton.setContentAreaFilled(false);
        addAnimationButton.setEnabled(false);
        addAnimationButton.setFocusable(false);
        addAnimationButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addAnimationButton.setMaximumSize(new java.awt.Dimension(30, 30));
        addAnimationButton.setMinimumSize(new java.awt.Dimension(30, 30));
        addAnimationButton.setName("addAnimationButton"); // NOI18N
        addAnimationButton.setPreferredSize(new java.awt.Dimension(30, 30));
        addAnimationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAnimationButtonActionPerformed(evt);
            }
        });

        removeAnimationButton.setIcon(resourceMap.getIcon("removeAnimationButton.icon")); // NOI18N
        removeAnimationButton.setText(resourceMap.getString("removeAnimationButton.text")); // NOI18N
        removeAnimationButton.setToolTipText(resourceMap.getString("removeAnimationButton.toolTipText")); // NOI18N
        removeAnimationButton.setAlignmentX(0.5F);
        removeAnimationButton.setContentAreaFilled(false);
        removeAnimationButton.setEnabled(false);
        removeAnimationButton.setFocusable(false);
        removeAnimationButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeAnimationButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        removeAnimationButton.setMaximumSize(new java.awt.Dimension(100, 100));
        removeAnimationButton.setMinimumSize(new java.awt.Dimension(0, 0));
        removeAnimationButton.setName("removeAnimationButton"); // NOI18N
        removeAnimationButton.setPreferredSize(new java.awt.Dimension(30, 30));
        removeAnimationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAnimationButtonActionPerformed(evt);
            }
        });

        duplicateAnimationButton.setIcon(resourceMap.getIcon("duplicateAnimationButton.icon")); // NOI18N
        duplicateAnimationButton.setText(resourceMap.getString("duplicateAnimationButton.text")); // NOI18N
        duplicateAnimationButton.setToolTipText(resourceMap.getString("duplicateAnimationButton.toolTipText")); // NOI18N
        duplicateAnimationButton.setAlignmentX(0.5F);
        duplicateAnimationButton.setContentAreaFilled(false);
        duplicateAnimationButton.setEnabled(false);
        duplicateAnimationButton.setFocusable(false);
        duplicateAnimationButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        duplicateAnimationButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        duplicateAnimationButton.setMaximumSize(new java.awt.Dimension(100, 100));
        duplicateAnimationButton.setMinimumSize(new java.awt.Dimension(0, 0));
        duplicateAnimationButton.setName("duplicateAnimationButton"); // NOI18N
        duplicateAnimationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateAnimationButtonActionPerformed(evt);
            }
        });

        exportGifButton.setIcon(resourceMap.getIcon("exportGif.icon")); // NOI18N
        exportGifButton.setText(resourceMap.getString("exportGifButton.text")); // NOI18N
        exportGifButton.setToolTipText(resourceMap.getString("exportGifButton.toolTipText")); // NOI18N
        exportGifButton.setAlignmentX(0.5F);
        exportGifButton.setContentAreaFilled(false);
        exportGifButton.setEnabled(false);
        exportGifButton.setFocusable(false);
        exportGifButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportGifButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        exportGifButton.setMaximumSize(new java.awt.Dimension(100, 100));
        exportGifButton.setMinimumSize(new java.awt.Dimension(0, 0));
        exportGifButton.setName("exportGifButton"); // NOI18N
        exportGifButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportGifButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(addAnimationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeAnimationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(duplicateAnimationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportGifButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addAnimationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeAnimationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(duplicateAnimationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exportGifButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setFocusable(false);
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(173, 173));

        animationPanel1.setBackground(resourceMap.getColor("animationPanel1.background")); // NOI18N
        animationPanel1.setName("animationPanel1"); // NOI18N
        animationPanel1.setEditable(false);

        playButton.setIcon(resourceMap.getIcon("playButton.icon")); // NOI18N
        playButton.setText(resourceMap.getString("playButton.text")); // NOI18N
        playButton.setToolTipText(resourceMap.getString("playButton.toolTipText")); // NOI18N
        playButton.setAlignmentX(0.5F);
        playButton.setContentAreaFilled(false);
        playButton.setEnabled(false);
        playButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        playButton.setMaximumSize(new java.awt.Dimension(30, 30));
        playButton.setMinimumSize(new java.awt.Dimension(30, 30));
        playButton.setName("playButton"); // NOI18N
        playButton.setPreferredSize(new java.awt.Dimension(38, 38));
        playButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout animationPanel1Layout = new javax.swing.GroupLayout(animationPanel1);
        animationPanel1.setLayout(animationPanel1Layout);
        animationPanel1Layout.setHorizontalGroup(
            animationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, animationPanel1Layout.createSequentialGroup()
                .addContainerGap(131, Short.MAX_VALUE)
                .addComponent(playButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        animationPanel1Layout.setVerticalGroup(
            animationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, animationPanel1Layout.createSequentialGroup()
                .addContainerGap(115, Short.MAX_VALUE)
                .addComponent(playButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(animationPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(animationPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setFocusable(false);
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        delaySpinner.setEnabled(false);
        delaySpinner.setMaximumSize(new java.awt.Dimension(3000, 3000));
        delaySpinner.setName("delaySpinner"); // NOI18N
        delaySpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                delaySpinnerStateChanged(evt);
            }
        });

        addCellButton.setIcon(resourceMap.getIcon("addCellButton.icon")); // NOI18N
        addCellButton.setText(resourceMap.getString("addCellButton.text")); // NOI18N
        addCellButton.setToolTipText(resourceMap.getString("addCellButton.toolTipText")); // NOI18N
        addCellButton.setAlignmentX(0.5F);
        addCellButton.setContentAreaFilled(false);
        addCellButton.setEnabled(false);
        addCellButton.setFocusable(false);
        addCellButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addCellButton.setMaximumSize(new java.awt.Dimension(30, 30));
        addCellButton.setMinimumSize(new java.awt.Dimension(0, 0));
        addCellButton.setName("addCellButton"); // NOI18N
        addCellButton.setPreferredSize(new java.awt.Dimension(30, 30));
        addCellButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCellButtonActionPerformed(evt);
            }
        });

        removeCellButton.setIcon(resourceMap.getIcon("removeAnimationButton.icon")); // NOI18N
        removeCellButton.setText(resourceMap.getString("removeCellButton.text")); // NOI18N
        removeCellButton.setToolTipText(resourceMap.getString("removeCellButton.toolTipText")); // NOI18N
        removeCellButton.setAlignmentX(0.5F);
        removeCellButton.setContentAreaFilled(false);
        removeCellButton.setEnabled(false);
        removeCellButton.setFocusable(false);
        removeCellButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeCellButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        removeCellButton.setMaximumSize(new java.awt.Dimension(100, 100));
        removeCellButton.setMinimumSize(new java.awt.Dimension(0, 0));
        removeCellButton.setName("removeCellButton"); // NOI18N
        removeCellButton.setPreferredSize(new java.awt.Dimension(30, 30));
        removeCellButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeCellButtonActionPerformed(evt);
            }
        });

        animationStripScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        animationStripScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        animationStripScrollPane.setAutoscrolls(true);
        animationStripScrollPane.setName("animationStripScrollPane"); // NOI18N

        animationStripPanel.setName("animationStripPanel"); // NOI18N
        animationStripPanel.setPreferredSize(new java.awt.Dimension(430, 106));

        javax.swing.GroupLayout animationStripPanelLayout = new javax.swing.GroupLayout(animationStripPanel);
        animationStripPanel.setLayout(animationStripPanelLayout);
        animationStripPanelLayout.setHorizontalGroup(
            animationStripPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 430, Short.MAX_VALUE)
        );
        animationStripPanelLayout.setVerticalGroup(
            animationStripPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 106, Short.MAX_VALUE)
        );

        animationStripScrollPane.setViewportView(animationStripPanel);

        loopSpinner.setEnabled(false);
        loopSpinner.setMaximumSize(new java.awt.Dimension(3000, 3000));
        loopSpinner.setName("loopSpinner"); // NOI18N
        loopSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                loopSpinnerStateChanged(evt);
            }
        });

        loopLabel.setText(resourceMap.getString("loopLabel.text")); // NOI18N
        loopLabel.setName("loopLabel"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(animationStripScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(addCellButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeCellButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 63, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(delaySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(loopLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loopSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(animationStripScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addCellButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeCellButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(delaySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(loopLabel)
                            .addComponent(loopSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(35, 35, 35))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel4.border.title"))); // NOI18N
        jPanel4.setFocusable(false);
        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(180, 392));

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        spriteTree.setModel(new javax.swing.tree.DefaultTreeModel(new CustomNode("/", true)));
        spriteTree.setToolTipText(resourceMap.getString("spriteTree.toolTipText")); // NOI18N
        spriteTree.setAutoscrolls(true);
        spriteTree.setEnabled(false);
        spriteTree.setName("spriteTree"); // NOI18N
        spriteTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                spriteTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(spriteTree);

        spritePreviewPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        spritePreviewPanel.setName("spritePreviewPanel"); // NOI18N

        addToFrameButton.setIcon(resourceMap.getIcon("addToFrameButton.icon")); // NOI18N
        addToFrameButton.setText(resourceMap.getString("addToFrameButton.text")); // NOI18N
        addToFrameButton.setToolTipText(resourceMap.getString("addToFrameButton.toolTipText")); // NOI18N
        addToFrameButton.setContentAreaFilled(false);
        addToFrameButton.setEnabled(false);
        addToFrameButton.setFocusable(false);
        addToFrameButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addToFrameButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        addToFrameButton.setMaximumSize(new java.awt.Dimension(100, 100));
        addToFrameButton.setMinimumSize(new java.awt.Dimension(0, 0));
        addToFrameButton.setName("addToFrameButton"); // NOI18N
        addToFrameButton.setPreferredSize(new java.awt.Dimension(34, 34));
        addToFrameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToFrameButtonActionPerformed(evt);
            }
        });

        spritePreviewTitle.setBackground(new java.awt.Color(0,0,0,0));
        spritePreviewTitle.setText(resourceMap.getString("spritePreviewTitle.text")); // NOI18N
        spritePreviewTitle.setAlignmentX(0.5F);
        spritePreviewTitle.setName("spritePreviewTitle"); // NOI18N

        javax.swing.GroupLayout spritePreviewPanelLayout = new javax.swing.GroupLayout(spritePreviewPanel);
        spritePreviewPanel.setLayout(spritePreviewPanelLayout);
        spritePreviewPanelLayout.setHorizontalGroup(
            spritePreviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, spritePreviewPanelLayout.createSequentialGroup()
                .addContainerGap(129, Short.MAX_VALUE)
                .addComponent(addToFrameButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(spritePreviewTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
        );
        spritePreviewPanelLayout.setVerticalGroup(
            spritePreviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, spritePreviewPanelLayout.createSequentialGroup()
                .addComponent(spritePreviewTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
                .addComponent(addToFrameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spritePreviewPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spritePreviewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        viewPanel.setBackground(resourceMap.getColor("viewPanel.background")); // NOI18N
        viewPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        viewPanel.setName("viewPanel"); // NOI18N

        zoomInButton.setIcon(resourceMap.getIcon("zoomInButton.icon")); // NOI18N
        zoomInButton.setText(resourceMap.getString("zoomInButton.text")); // NOI18N
        zoomInButton.setToolTipText(resourceMap.getString("zoomInButton.toolTipText")); // NOI18N
        zoomInButton.setContentAreaFilled(false);
        zoomInButton.setEnabled(false);
        zoomInButton.setFocusable(false);
        zoomInButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomInButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        zoomInButton.setMaximumSize(new java.awt.Dimension(34, 34));
        zoomInButton.setMinimumSize(new java.awt.Dimension(34, 34));
        zoomInButton.setName("zoomInButton"); // NOI18N
        zoomInButton.setPreferredSize(new java.awt.Dimension(34, 34));
        zoomInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInButtonActionPerformed(evt);
            }
        });

        zoomOutButton.setIcon(resourceMap.getIcon("zoomOutButton.icon")); // NOI18N
        zoomOutButton.setText(resourceMap.getString("zoomOutButton.text")); // NOI18N
        zoomOutButton.setToolTipText(resourceMap.getString("zoomOutButton.toolTipText")); // NOI18N
        zoomOutButton.setContentAreaFilled(false);
        zoomOutButton.setEnabled(false);
        zoomOutButton.setFocusable(false);
        zoomOutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomOutButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        zoomOutButton.setMaximumSize(new java.awt.Dimension(34, 34));
        zoomOutButton.setMinimumSize(new java.awt.Dimension(34, 34));
        zoomOutButton.setName("zoomOutButton"); // NOI18N
        zoomOutButton.setPreferredSize(new java.awt.Dimension(34, 34));
        zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutButtonActionPerformed(evt);
            }
        });

        controlPanel.setClosable(true);
        controlPanel.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        controlPanel.setTitle(resourceMap.getString("controlPanel.title")); // NOI18N
        controlPanel.setFocusCycleRoot(false);
        controlPanel.setFocusable(false);
        controlPanel.setFont(resourceMap.getFont("controlPanel.font")); // NOI18N
        controlPanel.setFrameIcon(resourceMap.getIcon("controlPanel.frameIcon")); // NOI18N
        controlPanel.setName("controlPanel"); // NOI18N
        controlPanel.setRequestFocusEnabled(false);
        controlPanel.setVisible(true);
        controlPanel.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setAlignmentY(0.5F);
        jToolBar1.setMinimumSize(new java.awt.Dimension(106, 30));
        jToolBar1.setName("jToolBar1"); // NOI18N

        removeSpriteButton.setIcon(resourceMap.getIcon("removeSpriteButton.icon")); // NOI18N
        removeSpriteButton.setText(resourceMap.getString("removeSpriteButton.text")); // NOI18N
        removeSpriteButton.setToolTipText(resourceMap.getString("removeSpriteButton.toolTipText")); // NOI18N
        removeSpriteButton.setAlignmentX(0.5F);
        removeSpriteButton.setEnabled(false);
        removeSpriteButton.setFocusable(false);
        removeSpriteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeSpriteButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        removeSpriteButton.setMaximumSize(new java.awt.Dimension(34, 34));
        removeSpriteButton.setMinimumSize(new java.awt.Dimension(34, 34));
        removeSpriteButton.setName("removeSpriteButton"); // NOI18N
        removeSpriteButton.setPreferredSize(new java.awt.Dimension(34, 34));
        removeSpriteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSpriteButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(removeSpriteButton);

        rotateCWButton.setIcon(resourceMap.getIcon("rotateCWButton.icon")); // NOI18N
        rotateCWButton.setToolTipText(resourceMap.getString("rotateCWButton.toolTipText")); // NOI18N
        rotateCWButton.setAlignmentX(0.5F);
        rotateCWButton.setEnabled(false);
        rotateCWButton.setFocusable(false);
        rotateCWButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rotateCWButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rotateCWButton.setMaximumSize(new java.awt.Dimension(34, 34));
        rotateCWButton.setMinimumSize(new java.awt.Dimension(34, 34));
        rotateCWButton.setName("rotateCWButton"); // NOI18N
        rotateCWButton.setPreferredSize(new java.awt.Dimension(34, 34));
        rotateCWButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rotateCWButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateCWButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(rotateCWButton);

        rotateACWButton.setIcon(resourceMap.getIcon("rotateACWButton.icon")); // NOI18N
        rotateACWButton.setToolTipText(resourceMap.getString("rotateACWButton.toolTipText")); // NOI18N
        rotateACWButton.setAlignmentX(0.5F);
        rotateACWButton.setEnabled(false);
        rotateACWButton.setFocusable(false);
        rotateACWButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rotateACWButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rotateACWButton.setMaximumSize(new java.awt.Dimension(34, 34));
        rotateACWButton.setMinimumSize(new java.awt.Dimension(34, 34));
        rotateACWButton.setName("rotateACWButton"); // NOI18N
        rotateACWButton.setPreferredSize(new java.awt.Dimension(34, 34));
        rotateACWButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rotateACWButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateACWButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(rotateACWButton);

        jPanel7.setName("jPanel7"); // NOI18N

        flipVCheckBox.setText(resourceMap.getString("flipVCheckBox.text")); // NOI18N
        flipVCheckBox.setEnabled(false);
        flipVCheckBox.setName("flipVCheckBox"); // NOI18N
        flipVCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flipVCheckBoxActionPerformed(evt);
            }
        });

        flipHCheckBox.setText(resourceMap.getString("flipHCheckBox.text")); // NOI18N
        flipHCheckBox.setEnabled(false);
        flipHCheckBox.setName("flipHCheckBox"); // NOI18N
        flipHCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flipHCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(flipHCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flipVCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(flipVCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(flipHCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jToolBar1.add(jPanel7);

        jPanel6.setName("jPanel6"); // NOI18N

        angleLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        angleLabel.setText(resourceMap.getString("angleLabel.text")); // NOI18N
        angleLabel.setName("angleLabel"); // NOI18N

        angleSpinner.setModel(new RollOverSpinModel(0, 0, 360, 1));
        angleSpinner.setToolTipText(resourceMap.getString("angleSpinner.toolTipText")); // NOI18N
        angleSpinner.setEnabled(false);
        angleSpinner.setMaximumSize(new java.awt.Dimension(3000, 3000));
        angleSpinner.setName("angleSpinner"); // NOI18N
        ((javax.swing.JSpinner.DefaultEditor) zOrderSpinner.getEditor()).getTextField().setEditable(true);
        angleSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                angleSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addComponent(angleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                .addGap(3, 3, 3)
                .addComponent(angleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(angleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(angleLabel))
        );

        jToolBar1.add(jPanel6);

        jPanel5.setName("jPanel5"); // NOI18N

        zOrderLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        zOrderLabel.setText(resourceMap.getString("zOrderLabel.text")); // NOI18N
        zOrderLabel.setName("zOrderLabel"); // NOI18N

        zOrderSpinner.setToolTipText(resourceMap.getString("zOrderSpinner.toolTipText")); // NOI18N
        zOrderSpinner.setEnabled(false);
        zOrderSpinner.setMaximumSize(new java.awt.Dimension(3000, 3000));
        zOrderSpinner.setName("zOrderSpinner"); // NOI18N
        ((javax.swing.JSpinner.DefaultEditor) zOrderSpinner.getEditor()).getTextField().setEditable(true);
        zOrderSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                zOrderSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(zOrderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(zOrderSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(zOrderLabel)
                .addComponent(zOrderSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jToolBar1.add(jPanel5);

        controlPanel.getContentPane().add(jToolBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 30));

        spriteListControlPanel.setClosable(true);
        spriteListControlPanel.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        spriteListControlPanel.setTitle(resourceMap.getString("spriteListControlPanel.title")); // NOI18N
        spriteListControlPanel.setFocusCycleRoot(false);
        spriteListControlPanel.setFocusable(false);
        spriteListControlPanel.setFont(resourceMap.getFont("spriteListControlPanel.font")); // NOI18N
        spriteListControlPanel.setName("spriteListControlPanel"); // NOI18N
        spriteListControlPanel.setRequestFocusEnabled(false);
        spriteListControlPanel.setVerifyInputWhenFocusTarget(false);
        spriteListControlPanel.setVisible(true);
        spriteListControlPanel.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        spriteList.setName("spriteList"); // NOI18N
        jScrollPane3.setViewportView(spriteList);
        spriteList.setModel(new DefaultListModel());

        spriteListControlPanel.getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 110, 110));

        javax.swing.GroupLayout viewPanelLayout = new javax.swing.GroupLayout(viewPanel);
        viewPanel.setLayout(viewPanelLayout);
        viewPanelLayout.setHorizontalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(viewPanelLayout.createSequentialGroup()
                        .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                        .addComponent(zoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(viewPanelLayout.createSequentialGroup()
                        .addComponent(spriteListControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(438, Short.MAX_VALUE))))
        );
        viewPanelLayout.setVerticalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewPanelLayout.createSequentialGroup()
                .addGroup(viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(zoomOutButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(zoomInButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(viewPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 126, Short.MAX_VALUE)
                .addComponent(spriteListControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        //JComponent c = (BasicInternalFrameTitlePane)((BasicInternalFrameUI) controlPanel.getUI()).getNorthPane();
        //c.setPreferredSize(new Dimension(c.getWidth(), 15));
        controlPanel.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        //JComponent c = (BasicInternalFrameTitlePane)((BasicInternalFrameUI) controlPanel.getUI()).getNorthPane();
        //c.setPreferredSize(new Dimension(c.getWidth(), 15));
        spriteListControlPanel.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);

        onionSkinsCheckBox.setSelected(true);
        onionSkinsCheckBox.setText(resourceMap.getString("onionSkinsCheckBox.text")); // NOI18N
        onionSkinsCheckBox.setEnabled(false);
        onionSkinsCheckBox.setName("onionSkinsCheckBox"); // NOI18N
        onionSkinsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onionSkinsCheckBoxActionPerformed(evt);
            }
        });

        spriteListToggle.setSelected(true);
        spriteListToggle.setText(resourceMap.getString("spriteListToggle.text")); // NOI18N
        spriteListToggle.setFocusable(false);
        spriteListToggle.setName("spriteListToggle"); // NOI18N
        spriteListToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spriteListToggleActionPerformed(evt);
            }
        });

        modifySpriteToggle.setSelected(true);
        modifySpriteToggle.setText(resourceMap.getString("modifySpriteToggle.text")); // NOI18N
        modifySpriteToggle.setFocusable(false);
        modifySpriteToggle.setName("modifySpriteToggle"); // NOI18N
        modifySpriteToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifySpriteToggleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(viewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(modifySpriteToggle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spriteListToggle, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(onionSkinsCheckBox)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(onionSkinsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spriteListToggle, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(modifySpriteToggle, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(viewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, 0, 173, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1.getAccessibleContext().setAccessibleName(resourceMap.getString("jPanel1.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void zoomInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInButtonActionPerformed
        viewPanel.setZoom(viewPanel.getZoom() + 0.5f);
}//GEN-LAST:event_zoomInButtonActionPerformed

    private void zoomOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutButtonActionPerformed
        viewPanel.setZoom(viewPanel.getZoom() - 0.5f);
}//GEN-LAST:event_zoomOutButtonActionPerformed

    public ArrayList<GraphicObject> addNodeToCell(CustomNode aNode, AnimationCell aCell, Point aPoint)
    {
        ArrayList<GraphicObject> graphics = new ArrayList<GraphicObject>();
        return this.addNodeToCell(aNode, aCell, graphics, aPoint);
    }

    private ArrayList<GraphicObject> addNodeToCell(CustomNode aNode, AnimationCell aCell, ArrayList<GraphicObject> aGraphics, Point aPoint)
    {
        if (aNode == null)
            return null;
        
        if (aNode.isLeaf())
        {
            if (aCell != null)
            {
                SelectionBox spriteArea = (SelectionBox)aNode.getCustomObject();
                SpriteGraphic graphic = new SpriteGraphic(bufferedImage, aPoint, spriteArea.getRect());
                graphic.setAnchor(GraphicObject.Anchor.CENTRE);
                aCell.addSprite(aNode, graphic);
                graphic.setDescription(aNode.getFullPathName());
                ((DefaultListModel)spriteList.getModel()).addElement(graphic);

                if (aCell == workingCell)
                {
                    viewPanel.addGraphic(graphic);
                    viewPanel.unselectAllGraphics();
                    viewPanel.selectGraphic(graphic);
                }

                aGraphics.add(graphic);
            }
        }
        else
        {
            for (int i=0; i<aNode.getChildCount(); ++i)
            {
                addNodeToCell((CustomNode)aNode.getChildAt(i), aCell, aGraphics, aPoint);
            }
        }

        if (aCell != null)
            aCell.rebuild();
        
        animationStripPanel.repaint();
        
        return aGraphics;
        
        //
    }

    public void nodeDropped(java.awt.Component aComponent, String aNodePath, Point aPoint)
    {
        CustomNode node = spriteTree.getNodeForPath(aNodePath);

        if (node != null)
        {
            cmdManager.execute(new AddSpriteToCellCommand(node, this, aPoint));
        }
    }

    // appends unique number to animation name before adding to list
    private void addAnimation(final Animation aAnimation)
    {
        boolean bExists = false;
        String name = aAnimation.getName();      
        int animationNameCounter = 1;
        do
        {           
            bExists = false;
            for (int i=0; i<animationList.getModel().getSize(); ++i)
            {
                 String nameInList =((Animation)(animationList.getModel().getElementAt(i))).getName();
                 if (nameInList.equals(name) )
                 {
                    bExists = true;
                    break;
                 }
            }
            if (bExists)
            {                
                name = aAnimation.getName() + " " + animationNameCounter;
                animationNameCounter++;
            }
        }
        while(bExists == true);
        
        aAnimation.setName(name);
        aAnimation.addAnimationListener(this);

        if (aAnimation != null)
        {           
            animationStripPanel.setAnimation(aAnimation);
            cmdManager.execute(new AddAnimationCommand(animationList, aAnimation));            
        }
    }
    
    private void addAnimationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAnimationButtonActionPerformed

        this.addAnimation(new Animation("Animation"));
        dfEditor.command.Command c = (new AddCellCommand(getWorkingAnimation(), new AnimationCell()));
        c.execute();
}//GEN-LAST:event_addAnimationButtonActionPerformed


    private void removeAnimationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAnimationButtonActionPerformed
        
        int index = animationList.getSelectedIndex();
        if (index >= 0)
        {
            animationStripPanel.stop();
            cmdManager.execute(new RemoveAnimationCommand(animationList, (Animation)animationList.getModel().getElementAt(index)));
        }        
}//GEN-LAST:event_removeAnimationButtonActionPerformed

    private Animation getWorkingAnimation()
    {
        DefaultMutableListModel model = (DefaultMutableListModel)animationList.getModel();
        int index = animationList.getSelectedIndex();
        if (index >= 0)
        {
            return (Animation)model.get(index);
        }
        return null;
    }

    private void addCellButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCellButtonActionPerformed
        Animation animation = getWorkingAnimation();

        if (animation != null)
        {
            cmdManager.execute(new AddCellCommand(animation, this.getWorkingCell()));
            this.getWorkingCell().rebuild();
            animationStripPanel.repaint();
        }
    }//GEN-LAST:event_addCellButtonActionPerformed

    private void removeCellButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeCellButtonActionPerformed
        viewPanel.clear();
        //animationStripPanel.removeSelected();

        Animation animation = getWorkingAnimation();
        AnimationCell cell =  animationStripPanel.selectedCell();
        if (animation != null)
        {
            cmdManager.execute(new RemoveCellCommand(animation, cell));
        }
    }//GEN-LAST:event_removeCellButtonActionPerformed

    private void delaySpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_delaySpinnerStateChanged
        JSpinner spinner = (JSpinner)evt.getSource();

        if (workingCell != null)
        {
            int value = (Integer)spinner.getValue();        
            workingCell.setDelay(value);
            spinner.setValue(workingCell.getDelay());
        }
    }//GEN-LAST:event_delaySpinnerStateChanged

    private void playButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playButtonActionPerformed
       Animation animation = getWorkingAnimation();

       animationStripPanel.addAnimationStripListener(this);

       boolean playing = animationStripPanel.isPlaying();

       if (!playing)
           animationStripPanel.play();
       else
           animationStripPanel.stop();

       removeCellButton.setEnabled(!playing);
       addCellButton.setEnabled(!playing);

    }//GEN-LAST:event_playButtonActionPerformed

    private void spriteTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_spriteTreeValueChanged
        
        int numSelected = 0;
        if (spriteTree.getSelectedNodes() != null)
            numSelected = spriteTree.getSelectedNodes().length;
                
        CustomNode node = (CustomNode)spriteTree.getLastSelectedPathComponent();

        boolean bSelected = (node != null);
        
        addToFrameButton.setEnabled(bSelected);
        
        if (bSelected)
        {
            if (numSelected == 1 && node.isLeaf())
            {
                spritePreviewTitle.setHorizontalTextPosition(JLabel.LEADING);
                SelectionBox spriteArea = (SelectionBox)node.getCustomObject();
                SpriteGraphic graphic = new SpriteGraphic(bufferedImage, new Point(0, 0), spriteArea.getRect());
                spritePreviewPanel.setGraphic(graphic);
                spritePreviewTitle.setText(node.getFullPathName());
            }
            else if (numSelected > 1 || !node.isLeaf())
            {               
                spritePreviewTitle.setHorizontalTextPosition(JLabel.CENTER);
                spritePreviewTitle.setText("<multiple sprites selected>");
                spritePreviewPanel.setGraphic(null);
            }
        }
        else
        {
            spritePreviewTitle.setText(" ");
            spritePreviewPanel.setGraphic(null);
        }
        
        spritePreviewPanel.repaint();
    }//GEN-LAST:event_spriteTreeValueChanged

    private void addToFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToFrameButtonActionPerformed

        // get selected node
        CustomNode selectedNode = (CustomNode)spriteTree.getSelectedNode();

        cmdManager.execute(new AddSpriteToCellCommand(selectedNode, this, new Point(0,0)));
        //addNodeToCell(selectedNode, workingCell);
    }//GEN-LAST:event_addToFrameButtonActionPerformed

    private void loopSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_loopSpinnerStateChanged
        JSpinner spinner = (JSpinner)evt.getSource();

        Animation animation = getWorkingAnimation();
        if (animation != null)
        {
            int value = (Integer)spinner.getValue();
            animation.setLoops(value);
            spinner.setValue(animation.getLoops()); // verify
        }
    }//GEN-LAST:event_loopSpinnerStateChanged

    private void removeSpriteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSpriteButtonActionPerformed
        Animation animation = getWorkingAnimation();
        AnimationCell cell =  animationStripPanel.selectedCell();
        ArrayList<GraphicObject> selectedGraphics = viewPanel.selectedGraphics();

        ArrayList<UndoableCommand> commands = new ArrayList<UndoableCommand>();
        for (int i=0; i<selectedGraphics.size(); ++i)        
            commands.add(new RemoveAnimGraphicCommand(animation, cell, selectedGraphics.get(i), viewPanel));
        
        GroupedUndoableCommand groupedCommand = new GroupedUndoableCommand(commands);
        if (cmdManager != null)
            cmdManager.execute(groupedCommand);
        else
            groupedCommand.execute();
    }//GEN-LAST:event_removeSpriteButtonActionPerformed

private void rotateCWButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateCWButtonActionPerformed
    
    ArrayList<GraphicObject> selectedGraphics = viewPanel.selectedGraphics();
    
    cmdManager.execute(new RotateGraphicListCommand(selectedGraphics, 90.0f, viewPanel));
    
    this.getWorkingCell().rebuild();
    repaint();
}//GEN-LAST:event_rotateCWButtonActionPerformed

private void rotateACWButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateACWButtonActionPerformed
    ArrayList<GraphicObject> selectedGraphics = viewPanel.selectedGraphics();
     
    cmdManager.execute(new RotateGraphicListCommand(selectedGraphics, -90.0f, viewPanel));
    
    this.getWorkingCell().rebuild();
    repaint();}//GEN-LAST:event_rotateACWButtonActionPerformed

private void zOrderSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_zOrderSpinnerStateChanged
    
    ArrayList<GraphicObject> selectedGraphics = viewPanel.selectedGraphics();
    AnimationCell cell = this.getWorkingCell();
        
    if (cell != null && selectedGraphics != null && selectedGraphics.size() > 0)
    {
        ArrayList<UndoableCommand> commands = new ArrayList<UndoableCommand>();
        
        for (int i=0; i<selectedGraphics.size(); ++i)
        {
            Integer val = null;
            
            try 
            { 
                val = new Integer((String)zOrderSpinner.getValue());
            }
            catch (NumberFormatException exc){}
            
            if (val != null) 
            {
                int value = val.intValue();
                GraphicObject graphic = selectedGraphics.get(i);
                
                if (value != cell.zOrderOfGraphic(graphic))
                {   
                    commands.add(new SetGraphicZOrderCommand(cell, graphic, this, value));                    
                }
            }            
        }
        
        GroupedUndoableCommand groupedCommand = new GroupedUndoableCommand(commands);
        if (cmdManager != null)
            cmdManager.execute(groupedCommand);
        else
            groupedCommand.execute();
    }         
}//GEN-LAST:event_zOrderSpinnerStateChanged

    public void zOrdersChanged(final AnimationCell aCell)
    {
        if (this.getWorkingCell() == aCell)
        {
            populateSpriteListFromCell(aCell);      
            updateControlPanel(viewPanel);
            viewPanel.setCell(aCell); // refresh            
        }
        setModified(true); 
    }

    private void populateSpriteListFromCell(AnimationCell aCell)
    {
        ArrayList<GraphicObject> selected = null;
        
        if (aCell == getWorkingCell())
            selected = viewPanel.selectedGraphics();
        
        DefaultListModel model = (DefaultListModel)spriteList.getModel();
        model.clear();
        
        if (aCell != null)
        {
            ArrayList<GraphicObject> array = aCell.getGraphicList();
            for (int i=0; i<array.size(); ++i)
            {
                array.get(i).setDescription(aCell.nodeForGraphic(array.get(i)).getFullPathName());
                model.addElement(array.get(i));
                array.get(i).setDescription(aCell.nodeForGraphic(array.get(i)).getFullPathName());                
            }
                        
            if (selected != null)
            {
                int[] indices = new int[selected.size()];            
                for (int i=0; i<selected.size(); ++i)
                {
                    indices[i] = model.indexOf(selected.get(i));
                }                    
                spriteList.setSelectedIndices(indices);
            }
            
            
        }      
    }

private void onionSkinsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onionSkinsCheckBoxActionPerformed
    setWorkingCell(this.getWorkingCell());    
}//GEN-LAST:event_onionSkinsCheckBoxActionPerformed

    /**
     * Timer callback
     * @param e 
     */
    public void actionPerformed(ActionEvent e)
    {
        ArrayList<UndoableCommand> commands = new ArrayList<UndoableCommand>();
        AnimationCell cell = this.getWorkingCell();
    
        if (cell != null && _rotatingGraphics != null && _rotatingGraphics.size() > 0)
        {        
            for (int i=0; i<_rotatingGraphics.size(); ++i)
            {
                Integer val = null;

                try 
                { 
                    val = (Integer)angleSpinner.getValue();
                }
                catch (NumberFormatException exc){}

                if (val != null) 
                {
                    int value = val.intValue();
                    GraphicObject graphic = _rotatingGraphics.get(i);

                    if (value != graphic.getSavedAngle())
                    {   
                        commands.add(new SetGraphicAngleCommand(graphic, value, viewPanel));                    
                    }
                }
            }
        }
        
        GroupedUndoableCommand groupedCommand = new GroupedUndoableCommand(commands);
        if (cmdManager != null)
            cmdManager.execute(groupedCommand);
        else
            groupedCommand.execute();
        
        this.setModified(true);              
        
        getWorkingCell().rebuild();
        animationStripPanel.repaint();
        
        _rotatingGraphics = null;
    }
    
private void angleSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_angleSpinnerStateChanged
    
    if (spinnerStateTimer == null)
    {
        spinnerStateTimer = new Timer(400, this);
        spinnerStateTimer.setRepeats(false);
        spinnerStateTimer.setActionCommand(null);
    }
   
    if (!spinnerStateTimer.isRunning())
    {        
        spinnerStateTimer.start();        
    }
    else
        spinnerStateTimer.restart();
    
    _rotatingGraphics = viewPanel.selectedGraphics();
    AnimationCell cell = this.getWorkingCell();        
    if (cell != null && _rotatingGraphics != null && _rotatingGraphics.size() > 0)
    {           
        for (int i=0; i<_rotatingGraphics.size(); ++i)
        {
            Integer val = null;
            
            try 
            { 
                val = (Integer)angleSpinner.getValue();
            }
            catch (NumberFormatException exc){}
            
            if (val != null) 
            {
                int value = val.intValue();
                GraphicObject graphic = _rotatingGraphics.get(i);
                
                if (value != graphic.getAngle())
                {   
                    graphic.setAngle(value);
                }
            }
        }
        viewPanel.repaint();
    }
}//GEN-LAST:event_angleSpinnerStateChanged

private void flipHCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flipHCheckBoxActionPerformed
    ArrayList<GraphicObject> selectedGraphics = viewPanel.selectedGraphics();    
    
    if (selectedGraphics != null)
        cmdManager.execute(new FlipSpriteListCommand(selectedGraphics, true, viewPanel));
    
    AnimationCell cell = this.getWorkingCell();
    if (cell != null)
        cell.rebuild();
    
    repaint();
}//GEN-LAST:event_flipHCheckBoxActionPerformed

private void flipVCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flipVCheckBoxActionPerformed
    ArrayList<GraphicObject> selectedGraphics = viewPanel.selectedGraphics();    
    
    if (selectedGraphics != null)
        cmdManager.execute(new FlipSpriteListCommand(selectedGraphics, false, viewPanel));
    
    AnimationCell cell = this.getWorkingCell();
    if (cell != null)
        cell.rebuild();
    
    repaint();
}//GEN-LAST:event_flipVCheckBoxActionPerformed

private void modifySpriteToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifySpriteToggleActionPerformed
    controlPanel.setVisible(modifySpriteToggle.isSelected());
}//GEN-LAST:event_modifySpriteToggleActionPerformed

private void spriteListToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spriteListToggleActionPerformed
    spriteListControlPanel.setVisible(spriteListToggle.isSelected());
}//GEN-LAST:event_spriteListToggleActionPerformed

private void duplicateAnimationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateAnimationButtonActionPerformed
    Animation currentAnimation = this.getWorkingAnimation();
    
    if (currentAnimation != null)
    {
        Animation copy = currentAnimation.copy();    
        this.addAnimation(copy);   
    }
}//GEN-LAST:event_duplicateAnimationButtonActionPerformed

private void exportGifButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportGifButtonActionPerformed
    saveGifAs();
}//GEN-LAST:event_exportGifButtonActionPerformed

    public void saveGifAs()
    {        
        JFileChooser chooser = fileChooser;

        CustomFilter filter = new CustomFilter();
        filter.addExtension(CustomFilter.EXT_GIF);
        chooser.resetChoosableFileFilters();
        chooser.setFileFilter(filter);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setApproveButtonText("Export");
        chooser.setDialogTitle("Export animation as GIF");
        chooser.setSelectedFile(new File(this.getWorkingAnimation().getName() + "." + CustomFilter.EXT_GIF));
        
        JFrame mainFrame = dfEditorApp.getApplication().getMainFrame();
        while (true)
        {
            int returnVal = chooser.showSaveDialog(mainFrame);
            if(returnVal == JFileChooser.APPROVE_OPTION)
            {
                java.io.File f = chooser.getSelectedFile();
                if(null == dfEditor.io.Utils.getExtension(f))
                {
                    f = new java.io.File(new String(f.getAbsolutePath() + "." + filter.getExtension()));
                }

                if (f.exists())
                {
                    //Custom button text
                    int response = JOptionPane.showConfirmDialog (null,
                       "Overwrite existing file?","Confirm Overwrite",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    if (response == JOptionPane.CANCEL_OPTION)
                        continue;
                }

                buildAnimatedGif(f.getAbsolutePath());            
            }
            break;
        }           
    }
    
    public void animatedToCell(AnimationCell aCell)
    {
        animationPanel1.setCell(aCell);
    }
    
    private void setOnionSkins(boolean bOn)
    {
        if (!bOn)
        {
            viewPanel.setOnionSkins(null);    
            return;
        }
        
        Animation animation = this.getWorkingAnimation();
        AnimationCell[] cells = null;
        
        if (animation != null)
        {        
            int currentIndex = animation.indexOfCell(workingCell);
            if (currentIndex > 0)
            {                
                cells = new AnimationCell[currentIndex];

                for (int i=0; i<currentIndex; ++i)
                {
                    AnimationCell cell = animation.getCellAtIndex((currentIndex-1)-i);
                    cells[i] = cell;
                }               
            }
        }
        viewPanel.setOnionSkins(cells);
    }
    
    public boolean saveCoords(File aFile)
    {       
        this.setName(aFile.getName());

        DefaultMutableListModel model = (DefaultMutableListModel)animationList.getModel();
        ArrayList<Animation> list = new ArrayList<Animation>();
        for (int i=0; i<model.getSize(); ++i)
        {
            list.add((Animation)model.elementAt(i));
        }

        AnimationSetWriter writer = new AnimationSetWriter();                
        writer.createAnimationSet(aFile, loadedSpritesheetFile.getName(), list);

        if (helpLabel != null)
            helpLabel.setText("Animation set saved as " + aFile.toString());

        savedFile = aFile;
        this.setModified(false);
        return true;
    }

    public boolean save()
    {
        //buildAnimatedGif(); // test
        
        if (savedFile != null)
            return saveCoords(savedFile);
        
        return saveAs();
    }
    
    public boolean saveAs()
    {
        boolean bOK = false;
        
        JFileChooser chooser = fileChooser;

        CustomFilter filter = new CustomFilter();
        filter.addExtension(CustomFilter.EXT_ANIM);
        chooser.resetChoosableFileFilters();
        chooser.setFileFilter(filter);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setApproveButtonText("Save");
        chooser.setDialogTitle("Save animation set");
        chooser.setSelectedFile(new File("newAnimation." + CustomFilter.EXT_ANIM));
        
        JFrame mainFrame = dfEditorApp.getApplication().getMainFrame();
        while (true)
        {
            int returnVal = chooser.showSaveDialog(mainFrame);
            if(returnVal == JFileChooser.APPROVE_OPTION)
            {
                java.io.File f = chooser.getSelectedFile();
                if(null == dfEditor.io.Utils.getExtension(f))
                {
                    f = new java.io.File(new String(f.getAbsolutePath() + "." + filter.getExtension()));
                }

                if (f.exists())
                {
                    //Custom button text
                    int response = JOptionPane.showConfirmDialog (null,
                       "Overwrite existing file?","Confirm Overwrite",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    if (response == JOptionPane.CANCEL_OPTION)
                        continue;
                }

                saveCoords(f);            
                bOK = true;
            }
            break;
        }        
        return bOK;
    }

    

    public boolean load(AnimationSetReader aReader)
    {
        String spriteSheetPath = aReader.getSpriteSheetPath();

        File ssFile = new File(spriteSheetPath);
        if (!ssFile.exists())
        {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not find sprite sheet: \n\n"+spriteSheetPath,
                    "Sprite sheet not found",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        setSpritesheetFile(ssFile);
        
        ArrayList<Animation> animations = null;
        try 
        {
            animations = aReader.getAnimations(spriteTree, bufferedImage);
        }
        catch (Exception e)
        {
            showParseError();             
        }

        if (animations != null)
        {
            for (int i=0; i<animations.size(); ++i)
            {
                Animation animation = animations.get(i);
                animation.addAnimationListener(this);

                animationStripPanel.setAnimation(animation);
                dfEditor.command.Command c = new AddAnimationCommand(animationList, animation);
                c.execute();

                animation.setCurrentCellIndex(0);
                AnimationCell cell = animation.getCurrentCell();
                while (cell != null)
                {
                    cell.rebuild();
                    cell = animation.getNextCell();
                }
                animation.setCurrentCellIndex(0);
            }

            return true;
        }
        
        return false;
    }

    public void stripIndexSelected(int aIndex)
    {
        removeCellButton.setEnabled(aIndex >= 0);
        addCellButton.setEnabled(true);
    }
    
    private void updateControlPanel(GraphicPanel aPanel)
    {           
        ArrayList<GraphicObject> sprites = aPanel.selectedGraphics();
        boolean bEnabled = (sprites.size() > 0);
        
        //try {            
            //controlPanel.setSelected(bEnabled);     // steals focus       
            modifySpriteToggle.setSelected(controlPanel.isVisible());            
        //} catch (java.beans.PropertyVetoException e) {}
        
        removeSpriteButton.setEnabled(bEnabled);
        rotateACWButton.setEnabled(bEnabled);
        rotateCWButton.setEnabled(bEnabled);
        zOrderSpinner.setEnabled(bEnabled);
        angleSpinner.setEnabled(bEnabled);
        flipVCheckBox.setEnabled(bEnabled);
        flipHCheckBox.setEnabled(bEnabled);
        
        boolean bSetZ = true;
        boolean bSetA = true;
        
        AnimationCell cell = this.getWorkingCell();
        for (int i=0; i<sprites.size(); ++i)            
        {
            if (i > 0)            
            {
                if (   cell.zOrderOfGraphic(sprites.get(i))
                    != cell.zOrderOfGraphic(sprites.get(i-1)))
                {                    
                    zOrderSpinner.setValue(null);                    
                    bSetZ = false;                    
                }
                
                if ((int)sprites.get(i).getAngle() != (int)sprites.get(i-1).getAngle())
                {
                    bSetA = false;                                        
                }
            }
        }
        
        //zOrderSpinner.setEnabled(bSetZ);
        
        for (int i=0; i<sprites.size(); ++i)            
        {
            SpriteGraphic graphic = (SpriteGraphic)sprites.get(i);
            
            if (bSetZ)
                zOrderSpinner.setValue(cell.zOrderOfGraphic(graphic));
            if (bSetA)
                angleSpinner.setValue(new Integer((int)graphic.getAngle()));
            
            boolean hFlipped = graphic.isFlippedH();
            if (flipHCheckBox.isSelected() != hFlipped)
                flipHCheckBox.setSelected(hFlipped);

            boolean vFlipped = graphic.isFlippedV();
            if (flipVCheckBox.isSelected() != vFlipped)
                flipVCheckBox.setSelected(vFlipped);
        }    
        repaint();
    }

    // Panel listener callbacks ///////////////////////////////////////////////
    public void graphicAdded(GraphicPanel aPanel, GraphicObject aGraphic)
    {    
        updateControlPanel(aPanel);
             
        boolean bFound = false;
        for (int i=0; i<spriteList.getModel().getSize(); ++i)
        {
            if (((DefaultListModel)spriteList.getModel()).elementAt(i) == aGraphic)
                bFound = true;
        }
        if (!bFound)
            ((DefaultListModel)spriteList.getModel()).addElement(aGraphic);
        
        workingCell.rebuild();
        animationStripPanel.repaint();
    }
    
    public void graphicMoved(GraphicPanel aPanel, GraphicObject aGraphic)
    {
        updateControlPanel(aPanel);
        
        if (workingCell != null)
        {
            workingCell.rebuild();
        }
        animationStripPanel.repaint();
    }
    
    // messy business, should tidy
    public void graphicsErased(GraphicPanel aPanel, ArrayList<GraphicObject> aGraphics)
    {
        Animation animation = getWorkingAnimation();
        AnimationCell cell =  animationStripPanel.selectedCell();
        
        ArrayList<UndoableCommand> commands = new ArrayList<UndoableCommand>();
        for (int i=0; i<aGraphics.size(); ++i)        
            commands.add(new RemoveAnimGraphicCommand(animation, cell, aGraphics.get(i), aPanel));
        
        GroupedUndoableCommand groupedCommand = new GroupedUndoableCommand(commands);
        if (cmdManager != null)
            cmdManager.execute(groupedCommand);
        else
            groupedCommand.execute();       
    }
    
    public void graphicErased(GraphicPanel aPanel, GraphicObject aGraphic)
    {
        updateControlPanel(aPanel);
                
        workingCell.removeGraphic(aGraphic);        
        
        workingCell.rebuild();
        ((DefaultListModel)spriteList.getModel()).removeElement(aGraphic);
        animationStripPanel.repaint();        
    }

    public void graphicSelectionChanged(GraphicPanel aPanel, GraphicObject aGraphic)
    {    
        updateControlPanel(aPanel);
        
        for (int i=0; i<spriteList.getModel().getSize(); ++i)
        {
            if (spriteList.getModel().getElementAt(i) == aGraphic)
            {
                if (aGraphic.isSelected())
                {
                    if (!spriteList.isSelectedIndex(i))
                        spriteList.addSelectionInterval(i, i);
                }
                else
                {
                    if (spriteList.isSelectedIndex(i))
                        spriteList.removeSelectionInterval(i, i);
                }
            }
        }
    }

    public void valueChanged(ListSelectionEvent e)
    {
        if (! e.getValueIsAdjusting()) //finished adjusting
        {
            JList list = (JList)e.getSource();
                                    
            int selectedIndices[] = list.getSelectedIndices();

            if (list == animationList)
            {
                DefaultMutableListModel model = (DefaultMutableListModel)list.getModel();
                
                if(selectedIndices.length > 0)
                {
                    Animation selectedAnimation = (Animation)model.get(selectedIndices[0]);
                    selectedAnimation.setCurrentCellIndex(0);
                    setWorkingCell(selectedAnimation.getCurrentCell());
                    animationStripPanel.setAnimation(selectedAnimation);
                    addCellButton.setEnabled(true);
                    playButton.setEnabled(selectedAnimation.numCells() > 1);
                    loopSpinner.setEnabled(true);
                    loopSpinner.setValue(selectedAnimation.getLoops());
                }
                else
                {
                    spriteTree.setSelectionPath(null);
                    setWorkingCell(null);
                    animationStripPanel.setAnimation(null);
                    addToFrameButton.setEnabled(false);
                    removeCellButton.setEnabled(false);
                    addCellButton.setEnabled(false);
                    playButton.setEnabled(false);
                    loopSpinner.setEnabled(false);
                }

                boolean bEnabled = (! ((DefaultMutableListModel)list.getModel()).isEmpty()) && (list.getSelectedIndex() >= 0);
                removeAnimationButton.setEnabled(bEnabled);
                duplicateAnimationButton.setEnabled(bEnabled);
                exportGifButton.setEnabled(bEnabled);
            } 
            else if (list == spriteList)
            {
                DefaultListModel model = (DefaultListModel)spriteList.getModel();
                for (int i=0; i<list.getModel().getSize(); ++i)
                {
                    boolean bFound = false;
                    for (int j=0; j<selectedIndices.length; ++j)
                    {
                        if (selectedIndices[j] == i)
                        {
                            bFound = true;
                            break;
                        }
                    }
                    if (!bFound)
                    {
                        GraphicObject graphic = (GraphicObject)model.getElementAt(i);
                        if (graphic.isSelected())
                        {                            
                            viewPanel.selectGraphic(graphic, false);                            
                        }
                    }
                }

                for (int i=0; i<selectedIndices.length; ++i)
                {
                    GraphicObject graphic = (GraphicObject)model.getElementAt(selectedIndices[i]);
                    if (!graphic.isSelected());
                        viewPanel.selectGraphic(graphic);//, true);
                }
                viewPanel.repaint();
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////

    // internal frame listener callbacks
    public void internalFrameOpened(InternalFrameEvent e) {}
    public void internalFrameClosing(InternalFrameEvent e) {}    
    public void internalFrameClosed(InternalFrameEvent e) {}    
    public void internalFrameIconified(InternalFrameEvent e) {}
    public void internalFrameDeiconified(InternalFrameEvent e) {}
    public void internalFrameActivated(InternalFrameEvent e) {}
    public void internalFrameDeactivated(InternalFrameEvent e)
    {
        if (e.getSource() == controlPanel)
            modifySpriteToggle.setSelected(false);
        if (e.getSource() == spriteListControlPanel)
            spriteListToggle.setSelected(false);
    }
    
    private class RollOverSpinModel extends javax.swing.SpinnerNumberModel
    {
        public RollOverSpinModel(int aValue, int aMin, int aMax, int aStepSize)
        {
            super(aValue, aMin, aMax, aStepSize);
        }
        
        public Object getNextValue()
        {
            Integer i = (Integer)super.getNextValue();
            Integer max = (Integer)this.getMaximum();
            
            if (i== null)
                return new Integer(this.getStepSize().intValue());
            if (i.intValue() >= max.intValue())
            {
                return new Integer(i.intValue() % max.intValue());
            }
            
            return i;
        }
        
        public Object getPreviousValue()
        {
            Object o = super.getPreviousValue();
            if (o != null)
                return o;
            
            return new Integer (((Integer)this.getMaximum()).intValue() - this.getStepSize().intValue());
                
        }
    }
    
    private class CustomSpinModel extends AbstractSpinnerModel 
    {
        protected String value = "";

        public void setValue(Object o) 
        {
            if (o != null)               
                value = o.toString();                                        
            else
                value = " ";    
            
            fireStateChanged();
        }

        public Object getValue() { return value; }    

        public Object getPreviousValue() 
        {    
            Integer i = parse();
            if (i == null) return "0"; // default to 0 from indeterminate
            else return "" + (i.intValue() - 1);
        }

        public Object getNextValue() 
        {
            Integer i = parse();
            if (i == null) return "0"; // default to 0 from indeterminate
            else return "" + (i.intValue() + 1);
        }

        private Integer parse() 
        {
            try { return new Integer(value); }
            catch (NumberFormatException exc) { return null; }
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAnimationButton;
    private javax.swing.JButton addCellButton;
    private javax.swing.JButton addToFrameButton;
    private javax.swing.JLabel angleLabel;
    private javax.swing.JSpinner angleSpinner;
    private javax.swing.JList animationList;
    private dfEditor.animation.AnimationPanel animationPanel1;
    private dfEditor.animation.AnimationStripPanel animationStripPanel;
    private javax.swing.JScrollPane animationStripScrollPane;
    private javax.swing.JInternalFrame controlPanel;
    private javax.swing.JSpinner delaySpinner;
    private javax.swing.JButton duplicateAnimationButton;
    private javax.swing.JButton exportGifButton;
    private javax.swing.JCheckBox flipHCheckBox;
    private javax.swing.JCheckBox flipVCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel loopLabel;
    private javax.swing.JSpinner loopSpinner;
    private javax.swing.JToggleButton modifySpriteToggle;
    private javax.swing.JCheckBox onionSkinsCheckBox;
    private javax.swing.JButton playButton;
    private javax.swing.JButton removeAnimationButton;
    private javax.swing.JButton removeCellButton;
    private javax.swing.JButton removeSpriteButton;
    private javax.swing.JButton rotateACWButton;
    private javax.swing.JButton rotateCWButton;
    private dfEditor.SpriteList spriteList;
    private javax.swing.JInternalFrame spriteListControlPanel;
    private javax.swing.JToggleButton spriteListToggle;
    private dfEditor.SimpleGraphicPanel spritePreviewPanel;
    private javax.swing.JLabel spritePreviewTitle;
    private dfEditor.SpriteTree spriteTree;
    private dfEditor.animation.AnimationPanel viewPanel;
    private javax.swing.JLabel zOrderLabel;
    private javax.swing.JSpinner zOrderSpinner;
    private javax.swing.JButton zoomInButton;
    private javax.swing.JButton zoomOutButton;
    // End of variables declaration//GEN-END:variables

}
