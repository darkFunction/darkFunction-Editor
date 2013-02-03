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

package dfEditor.animation;


import java.awt.Rectangle;
import dfEditor.animation.AnimationCell;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.Point;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.AlphaComposite;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import dfEditor.command.*;

import dfEditor.MathUtil;


/**
 *
 * @author s4m20
 */
public class AnimationStripPanel extends javax.swing.JPanel implements AnimationDataListener, MouseMotionListener, MouseListener, ActionListener
{   
    private Animation animation = null;
    private ArrayList<Slot> slotList = null;
    private AnimationController controller = null;
    private Point mousePoint = null;
    private int insertBeforeSlotIndex = -1;
    private Timer timer = null;
    private int currentSlotInAnimation = -1;
    private int currentSlotInAnimationFramesLeft = -1;
    private ArrayList<AnimationStripListener> stripListeners = null;
    private CommandManager commandManager = null;
    int currentLoop = 0;

    public AnimationStripPanel()
    {
        super();

        slotList = new ArrayList<Slot>();
        addMouseListener(this);        
        addMouseMotionListener(this);

        Dimension d = new Dimension(0,0);
        this.setSize(d);
        this.setPreferredSize(d);
    }

    public void setController(AnimationController aController)
    {
        controller = aController;
    }

    public void setAnimation(Animation aAnimation)
    {
        slotList.clear();
        animation = aAnimation;

        if (animation != null)
        {
            animation.removeAnimationListener(this);
            animation.addAnimationListener(this);

            animation.setCurrentCellIndex(0);
            AnimationCell cell = animation.getCurrentCell();
            selectCell(cell);
            while (cell != null)
            {
                addCell(cell);
                cell = animation.getNextCell();
            }
        }

        repaint();
    }

    public void setCommandManager(CommandManager aManager)
    {
        this.commandManager = aManager;
    }
    
    public void cellAdded(Animation aAnimation, AnimationCell aCell)
    {
        if (aAnimation == animation)
        {
            setAnimation(aAnimation); // rebuilds slot positions
        }
    }

    public void cellRemoved(Animation aAnimation, AnimationCell aCell)
    {
        if (aAnimation == animation)
        {
            setAnimation(aAnimation); // rebuilds slot positions
        }
    }
    
    public void cellOrderChanged(Animation aAnimation)
    {
        if (aAnimation == animation)
        {
            // do nothing
        }
    }

    public void selectCell(AnimationCell aCell)
    {
        for (int i=0; i<slotList.size(); ++i)
        {
            if (aCell == slotList.get(i).getCell())
                slotList.get(i).setSelected(true);
            else
                slotList.get(i).setSelected(false);
        }
        repaint();
    }

    private void addCell(AnimationCell aCell)
    {
        Point p = new Point(0,0);
        if (slotList.size() > 0)
        {
            Rectangle lastSlotRect = slotList.get(slotList.size()-1).getRect();
            p.x = lastSlotRect.x + lastSlotRect.width;
        }

        Slot slot = new Slot(aCell, p, 86);
        slotList.add(slot);

        for (int i=0; i<slotList.size(); ++i)
            slotList.get(i).setSelected(false);

        slot.setSelected(true);

        // stretch and update scrollbar
        Dimension stripSize = getStripSize();
        this.setPreferredSize(stripSize);
        this.setSize(stripSize);

        animation.setCurrentCellIndex(slotList.size()-1);

        if (controller != null)
        {
            controller.setWorkingCell(aCell);
            controller.stripIndexSelected(slotList.size()-1);
        }

        repaint();
    }

    private void removeCell(AnimationCell aCell)
    {
        for (int i=0; i<slotList.size(); ++i)
        {
            Slot slot = slotList.get(i);

            if (slot.getCell() == aCell)
            {
                //animation.removeCell(aCell);
                slotList.remove(slot);
            }

            if (i == currentSlotInAnimation)
            {
                currentSlotInAnimation --;
                if (currentSlotInAnimation < 0)
                    currentSlotInAnimation = 0;

            }
        }

        repaint();
    }

    private Dimension getStripSize()
    {
        Dimension ret = new Dimension(0,0);

        for (int i=0; i<slotList.size(); ++i)
        {
            Rectangle slotRect = slotList.get(i).getRect();
            int bottom = slotRect.y + slotRect.height;
            int right = slotRect.x + slotRect.width;

            if (bottom > ret.height)
                ret.height = bottom;
            if (right > ret.width)
                ret.width = right;
        }

        return ret;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        // slots
        for (int i=0; i<slotList.size(); ++i)
        {
            Slot slot = slotList.get(i);
            slot.draw(g);          
        }

        // dragged slots
        for (int i=0; i<slotList.size(); ++i)
        {
            Slot draggedSlot = slotList.get(i);
            if (draggedSlot.isDragged() && draggedSlot.getDragImage() != null)
            {
                g.drawImage(draggedSlot.getDragImage(), mousePoint.x, draggedSlot.getRect().y, null);

                if (insertBeforeSlotIndex >= 0)
                {
                    g.setColor(Color.RED);
                    int insertXPos = 0;

                    if (insertBeforeSlotIndex < slotList.size())
                    {
                        insertXPos = slotList.get(insertBeforeSlotIndex).getRect().x;
                    }
                    else if (insertBeforeSlotIndex == slotList.size())
                    {
                        // last slot, draw inser marker at end
                        Rectangle slotRect = slotList.get(insertBeforeSlotIndex-1).getRect();
                        insertXPos = slotRect.x + slotRect.width;
                    }

                    g.fillRect(insertXPos-1, 0, 3, this.getHeight());
                }
            }
        }

        // animation marker
        g.setColor(Color.BLUE);
        for (int i=0; i<slotList.size(); ++i)
        {
            if (i == currentSlotInAnimation)
            {
                Slot slot = slotList.get(i);
                Rectangle r = slot.getRect();

                g.drawRect(r.x, r.y, r.width, r.height);
            }
        }

    }

    public AnimationCell selectedCell()
    {
        for (int i=0; i<slotList.size(); ++i)
        {
            Slot slot = slotList.get(i);//
            if (slot.isSelected())
                return slot.getCell();
        }
        return null;
    }
    
    // not used
//    public void removeSelected()
//    {
//       if (controller != null)
//            controller.stripIndexSelected(-1);
//
//        for (int i=0; i<slotList.size(); ++i)
//        {
//            Slot slot = slotList.get(i);
//
//            if (slot.isSelected())
//            {
//                animation.removeCell(slot.getCell());
//                slotList.remove(slot);
//            }
//
//            if (i == currentSlotInAnimation)
//            {
//                currentSlotInAnimation --;
//                if (currentSlotInAnimation < 0)
//                    currentSlotInAnimation = 0;
//
//            }
//        }
//
//        setAnimation(animation);
//
//        repaint();
//    }

    public boolean isEmpty()
    {
        return (slotList.size() == 0);
    }

    private int getSelectedSlotIndex()
    {
        for (int i=0; i<slotList.size(); ++i)
        {
            Slot slot = slotList.get(i);
            if (slot.isSelected())
            {
                return i;
            }
        }

        return -1;
    }

    public void play()
    {
        if (slotList.size() == 0)
            return;
        
        currentLoop = animation.getLoops();
        
        if (timer == null)
            timer = new Timer(30, this);

        timer.start();

        currentSlotInAnimation = getSelectedSlotIndex();
        if (getSelectedSlotIndex() < 0)
            currentSlotInAnimation = 0;

        currentSlotInAnimationFramesLeft = slotList.get(currentSlotInAnimation).getCell().getDelay();

        notifyTick();

        repaint();
    }

    public void stop()
    {
        if (timer != null)
            timer.stop();
        
        currentSlotInAnimation = -1;

        repaint();
    }

    public boolean isPlaying()
    {
        return (timer != null && timer.isRunning());
    }

    public void addAnimationStripListener(AnimationStripListener aListener)
    {
        if (stripListeners == null)
        {
            stripListeners = new ArrayList<AnimationStripListener>();
        }

        removeAnimationStripListener(aListener);
        stripListeners.add(aListener);
    }

    public void removeAnimationStripListener(AnimationStripListener aListener)
    {
        if (stripListeners != null && stripListeners.contains(aListener))
        {
            stripListeners.remove(aListener);
        }
    }


    private void notifyTick()
    {
        if (stripListeners == null || stripListeners.isEmpty())
            return;

        if (currentSlotInAnimation > slotList.size()-1)
        {
            currentSlotInAnimation = 0;
        }
        
        for (int i=0; i<stripListeners.size(); ++i)
        {
           stripListeners.get(i).animatedToCell(slotList.get(currentSlotInAnimation).getCell());
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        currentSlotInAnimationFramesLeft --;
        if (currentSlotInAnimationFramesLeft <= 0)
        {
            currentSlotInAnimation ++;
            currentSlotInAnimation %= slotList.size();
                        
            currentSlotInAnimationFramesLeft = slotList.get(currentSlotInAnimation).getCell().getDelay();
                        
            if (currentSlotInAnimation == 0)
            {          
                if (--currentLoop == 0)
                    this.stop();
            }
        }

        if (currentSlotInAnimation != -1)
            notifyTick();

        repaint();
    }

    /***************************************************
     * Input
     * *************************************************/

    public void mouseMoved(MouseEvent evt)
    {
        Point p = evt.getPoint();

        mousePoint = p;
    }

    public void mouseDragged(MouseEvent evt)
    {
        Point p = evt.getPoint();

        mousePoint = p;

        int xOffset = 0;

        for (int i=0; i<slotList.size(); ++i)
        {
            Slot slot = slotList.get(i);
            if (slot.isSelected())
                slot.setDragged(true);
        }

        for (int i=0; i<slotList.size(); ++i)
        {
            Slot draggedSlot = slotList.get(i);
            if (draggedSlot.isDragged() && draggedSlot.getDragImage() != null)
            {
                xOffset += draggedSlot.getRect().width;
                int imgCentreX = p.x + draggedSlot.getDragImage().getWidth() + xOffset;

                for (int j=0; j<slotList.size(); ++j)
                {
                    Slot slot = slotList.get(j);
                    int slotCentreX = slot.getRect().x + slot.getRect().width;
                    if (imgCentreX > slotCentreX)
                    {
                        if (imgCentreX < slotCentreX + slot.getRect().width)
                        {
                            insertBeforeSlotIndex = j;                           
                        }                        
                        else
                        {
                            insertBeforeSlotIndex = j+1;
                        }                         
                    }
                }
            }
        }

        repaint();

    }

    public void mousePressed(MouseEvent evt)
    {
        Point p = evt.getPoint();
        controller.stripIndexSelected(-1);

        boolean selectedSlot = false;

        for (int i=0; i<slotList.size(); ++i)
        {
            Slot slot = slotList.get(i);
            slot.setSelected(false);

            Rectangle rect = slot.getRect();

            if (MathUtil.pointRectCollide(p, rect))
            {
                slot.setSelected(true);
                controller.stripIndexSelected(i);
                //slot.setDragged(true);
                controller.setWorkingCell(slot.getCell());
                selectedSlot = true;
            }
        }

        if (!selectedSlot)
            controller.setWorkingCell(null);


        repaint();
    }

    public void mouseReleased(MouseEvent evt)
    {
        boolean orderChanged = false;
        
        for (int i=0; i<slotList.size(); ++i)
        {            
            Slot slot = slotList.get(i);          
            
            if (!slot.isDragged())
                continue;
            if (insertBeforeSlotIndex >= 0)
            {
                orderChanged = true;
                animation.moveCell(i, insertBeforeSlotIndex);
                // TODO: do a command for this 
            }
        }       
        
        // relayout slots
        if (orderChanged)
        {
            setAnimation(animation);
        }

        for (int i=0; i<slotList.size(); ++i)
        {
            Slot slot = slotList.get(i);
            slot.setDragged(false);
        }

        if (orderChanged && insertBeforeSlotIndex < slotList.size())
                slotList.get(insertBeforeSlotIndex).setSelected(true);

        insertBeforeSlotIndex = -1;        
    }

    public void mouseExited(MouseEvent evt)
    {

    }

    public void mouseEntered(MouseEvent evt)
    {

    }

    public void mouseClicked(MouseEvent evt)
    {

    }

    // just a dumb object really... could have come up with an elaborate
    // design for this whole strip class but just got it working... can always
    // revisit!
    private class Slot
    {
        static final int MARGIN = 3;

        private AnimationCell cell;        
        private Rectangle innerRect;
        private boolean isSelected;
        private boolean isDragged;
        private BufferedImage dragImage = null;

        public Slot(final AnimationCell aCell, final Point aPos, final int aSize)
        {
            setCell(aCell);

            int size = aSize + MARGIN * 2;
            Rectangle rect = new Rectangle(aPos.x, aPos.y, size, size);
            innerRect = new Rectangle(rect.x + MARGIN, rect.y + MARGIN, rect.width - MARGIN*2, rect.height - MARGIN*2);

            isSelected = false;
            isDragged = false;
        }

        public Rectangle getRect()
        {
            return innerRect;
        }

        public void setSelected(boolean aSelected)
        {
            isSelected = aSelected;
        }

        public boolean isSelected()
        {
            return isSelected;
        }

        public AnimationCell getCell()
        {
            return cell;
        }

        public void setCell(AnimationCell aCell)
        {
            cell = aCell;
        }

        public void setDragged(boolean aDragged)
        {
            isDragged = aDragged;

            if (aDragged)            
                dragImage = createTranslucentCopy();            
            else
                dragImage = null;
        }

        public boolean isDragged()
        {
            return isDragged;
        }

        public BufferedImage getDragImage()
        {
            return dragImage;
        }

        public void draw(Graphics g)
        {
            final int BORDER = MARGIN+1;
            final Rectangle selectionRect = new Rectangle(
                        innerRect.x - BORDER,
                        innerRect.y - BORDER,
                        innerRect.width + BORDER * 2,
                        innerRect.height + BORDER * 2
                    );

            if (isSelected)
            {
                g.setColor(new Color(0, 0.7f, 0, 0.3f));
                g.fillRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
            }

             // debug
            g.setColor(new Color(0, 0.7f, 0, 0.7f));
            g.drawRect(innerRect.x, innerRect.y, innerRect.width, innerRect.height);

            this.drawCellInRect(cell, g, innerRect);
        }
        
        // scales to fit inside rect amd centres it
        private void drawCellInRect(final AnimationCell aCell, final Graphics aGraphics, final Rectangle aRect)
        {
            Rectangle r = new Rectangle(aRect.x, aRect.y, aRect.width, aRect.height);
            
            Point size = aCell.getImageSize();
            int w = size.x;
            int h = size.y;
            float aspectRatio = (float)w / (float)h;
         
            if (w > h)
            {
                int oldHeight = r.height;
                r.height /= aspectRatio;
                r.y += (oldHeight - r.height)/2;                
            }
            else if (h > w)
            {                
                int oldWidth = r.width;
                r.width *= aspectRatio;
                r.x += (oldWidth - r.width)/2;
            }

            aCell.draw(aGraphics, r);
        }

        private BufferedImage createTranslucentCopy()
        {
            BufferedImage img = new BufferedImage(innerRect.width, innerRect.height, BufferedImage.TRANSLUCENT);

            Graphics2D g = img.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

            this.drawCellInRect(cell, g, new Rectangle(0, 0, img.getWidth(), img.getHeight()));
           
            g.dispose();
            
            return img;
        }

    }

}
