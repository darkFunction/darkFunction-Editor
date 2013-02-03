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

import dfEditor.*;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.VolatileImage;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsConfiguration;
import java.awt.AlphaComposite;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import java.awt.geom.AffineTransform;

;
/**
 *
 * @author s4m20
 */
public class AnimationCell
{   
    private Dictionary<GraphicObject, Integer> graphicZOrderDict;
    private Dictionary<GraphicObject, CustomNode> graphicNodeDict;
    private ArrayList<GraphicObject> graphicOrderList;

    private VolatileImage vImage = null;
    private int delay;

    public AnimationCell()
    {
        setDelay(1);
        
        graphicZOrderDict = new Hashtable<GraphicObject, Integer>();
        graphicNodeDict = new Hashtable<GraphicObject, CustomNode>();
        graphicOrderList = new ArrayList<GraphicObject>();
        rebuild();
    }
    
    public Point getImageSize()            
    {
        if (vImage == null)
        {
            return new Point(0,0);
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        int valid = vImage.validate(gc);
	if (valid == VolatileImage.IMAGE_INCOMPATIBLE)
        {
            rebuild();
        }
        
        return new Point (vImage.getWidth(), vImage.getHeight());
    }

    public void rebuild()
    {
        //System.out.println("Attempting to rebuild animation cell volatile image");
        
        Rectangle r = getSpreadRect();

        if (r.width <= 0 || r.height <= 0)
        {
            vImage = null;
            return;
        }

        do
        {
            vImage = ImageUtil.createVolatileImage(r.width, r.height, java.awt.Transparency.TRANSLUCENT);

            Graphics2D g = null;
            try
            {
                g = vImage.createGraphics();

                // clear to transparent
                g.setComposite(AlphaComposite.Clear);
                g.fillRect(0, 0, r.width, r.height);
                g.setComposite(AlphaComposite.SrcOver);

                //for (Enumeration<GraphicObject> e = graphicNodeDict.keys(); e.hasMoreElements();)
                for (int i=graphicOrderList.size()-1; i>=0; i--)
                {
                    //GraphicObject graphic = e.nextElement();
                    GraphicObject graphic = graphicOrderList.get(i);
                    
                    // backup selected state and remove... don't want to draw it selected in the cell
                    boolean isSelected = graphic.isSelected();
                    graphic.setSelected(false);

                    Graphics2D g2d = (Graphics2D)g;          
            
                    AffineTransform transform = new AffineTransform(g2d.getTransform());
                    AffineTransform oldTransform = g2d.getTransform();

                    Rectangle gr = graphic.getRect();
                    transform.rotate(Math.toRadians(graphic.getAngle()), -r.x+gr.x+gr.width/2, -r.y+gr.y+gr.height/2);            
                    g2d.setTransform(transform);
                    
                    graphic.draw(g2d, new Point(-r.x,-r.y), 1.0f, 1.0f, false);
                    
                    g2d.setTransform(oldTransform);
                    
                    // restore selected state
                    graphic.setSelected(isSelected);
                }
            }
            finally
            {
                g.dispose();
            }
        }
        while (vImage.contentsLost());	
    }

    public void draw(final Graphics g, final Rectangle destRect)
    {
        if (vImage == null)
            return;
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

	int valid = vImage.validate(gc);
	if (valid == VolatileImage.IMAGE_INCOMPATIBLE)
        {
            rebuild();
        }

        if (vImage != null)
        {
            Rectangle r = destRect;
            g.drawImage(vImage, r.x, r.y, r.width, r.height, null);
        }
    }

    public Rectangle getSpreadRect()
    {
        double minX = 0;
        double minY = 0;
        double maxX = 0;
        double maxY = 0;

        // find extremities
        int i = 0;
        for (Enumeration<GraphicObject> e = graphicNodeDict.keys(); e.hasMoreElements();)
        {
            GraphicObject graphic = e.nextElement();
            Rectangle gr = graphic.getRect();
           
            AffineTransform at = AffineTransform.getRotateInstance(
                    Math.toRadians(graphic.getAngle()),
                    gr.width/2, 
                    gr.height/2);
                        
            java.awt.geom.Point2D topLeft = at.transform(new Point(0,0), null);
            java.awt.geom.Point2D topRight = at.transform(new Point(gr.width, 0), null);
            java.awt.geom.Point2D bottomLeft = at.transform(new Point(0, gr.height), null);
            java.awt.geom.Point2D bottomRight = at.transform(new Point(gr.width, gr.height), null);
            
            double loopMinX = gr.x + Math.min( 
                                        Math.min(topLeft.getX(), topRight.getX()),
                                        Math.min(bottomLeft.getX(), bottomRight.getX()) );
            double loopMinY = gr.y + Math.min( 
                                        Math.min(topLeft.getY(), topRight.getY()),
                                        Math.min(bottomLeft.getY(), bottomRight.getY()) );
            double loopMaxX = gr.x + Math.max( 
                                        Math.max(topLeft.getX(), topRight.getX()),
                                        Math.max(bottomLeft.getX(), bottomRight.getX()) );
            double loopMaxY = gr.y + Math.max( 
                                        Math.max(topLeft.getY(), topRight.getY()),
                                        Math.max(bottomLeft.getY(), bottomRight.getY()) );
            
            if (i++==0)
            {
                minX = loopMinX;
                minY = loopMinY;
                maxX = loopMaxX;
                maxY = loopMaxY;
            }
            else
            {
                if (loopMinX < minX)
                    minX = loopMinX;
                if (loopMinY < minY)
                    minY = loopMinY;

                if (loopMaxX > maxX)
                    maxX = loopMaxX;
                if (loopMaxY > maxY)
                    maxY = loopMaxY;
            }
        }
        
        //System.out.println("graphic spread = "+ minX +", "+ minY +", "+ (maxX-minX) +", " + (maxY-minY));
        return new Rectangle((int)minX, (int)minY, (int)maxX - (int)minX, (int)maxY - (int)minY);
    }

    public int getDelay()
    {
        return delay;
    }

    public void setDelay(int aDelay)
    {
        delay = aDelay;
        if (delay < 1)
        {
            delay = 1;
        }
    }

    public void addSprite(CustomNode aNode, GraphicObject aGraphic)
    {
        if (aNode != null && aGraphic != null)
        {
            graphicZOrderDict.put(aGraphic, 0);
            graphicNodeDict.put(aGraphic, aNode);
            graphicOrderList.add(aGraphic);
        }
    }
    
    public void setZOrder(final GraphicObject aGraphic, final int zOrder)
    {
        graphicZOrderDict.remove(aGraphic);
        graphicZOrderDict.put(aGraphic, new Integer(zOrder));
        
        boolean bRebuild = false;
       
        // bubble
        int n = graphicOrderList.size();
        for (int i = 0; i < n; i++)
        {            
            for (int j = n-1; j > i; j--)
            {
                GraphicObject A = graphicOrderList.get(j-1);
                GraphicObject B = graphicOrderList.get(j);
                
                int a = graphicZOrderDict.get(A).intValue();
                int b = graphicZOrderDict.get(B).intValue();
                
                if (a > b)
                {
                    this.swapGraphics(A, B);
                    bRebuild = true;
                }
            }
        }
        
        if (bRebuild)
        {
            this.rebuild();            
        }
    }

    public int zOrderOfGraphic(final GraphicObject aGraphic)
    {
        return graphicZOrderDict.get(aGraphic).intValue();
    }
    
    public ArrayList<GraphicObject> getGraphicList()
    {
//        ArrayList<GraphicObject> list = new ArrayList<GraphicObject>();
//        for (Enumeration<GraphicObject> e = graphicNodeDict.keys(); e.hasMoreElements();)
//        {
//            GraphicObject graphic = e.nextElement();
//            list.add(graphic);
//        }
//        return list;

        return graphicOrderList;
    }

    public CustomNode nodeForGraphic(GraphicObject aGraphic)
    {
        return graphicNodeDict.get(aGraphic);
    }

    public void removeGraphic(GraphicObject aGraphic)
    {
        graphicNodeDict.remove(aGraphic);
        graphicOrderList.remove(aGraphic);
    }

    public void swapGraphics(GraphicObject aA, GraphicObject aB)
    {
        int indexA = graphicOrderList.indexOf(aA);
        int indexB = graphicOrderList.indexOf(aB);

        graphicOrderList.set(indexA, aB);
        graphicOrderList.set(indexB, aA);

        //this.rebuild();
    }
    
    public AnimationCell copy()
    {
        AnimationCell newCell = new AnimationCell();
        newCell.setDelay(this.getDelay());
        
        for (int i=0; i<graphicOrderList.size(); ++i)
        {
            GraphicObject graphic = graphicOrderList.get(i);
            GraphicObject newGraphic = graphic.copy();
            newCell.addSprite(this.nodeForGraphic(graphic), newGraphic);
            newCell.setZOrder(newGraphic, this.zOrderOfGraphic(graphic));
        }
        
        return newCell;
        
    }

}