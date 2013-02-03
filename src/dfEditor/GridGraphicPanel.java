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

import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Rectangle;


/**
 *
 * @author s4m20
 */
public class GridGraphicPanel extends GraphicPanel
{
    protected Dimension tileSize = null;
    protected Dimension padding = null;

    public GridGraphicPanel(final Dimension aTileSize, final Dimension aPadding)
    {
        super();
        
        setTileSize(aTileSize);
        setPadding(aPadding);
    }

    public GridGraphicPanel()
    {
        super();

        setTileSize(null);
        setPadding(null);
    }

    public void setTileSize(final Dimension aTileSize)
    {
        tileSize = aTileSize;
        relayout();
    }

    public void setPadding(final Dimension aPadding)
    {
        padding = aPadding;
        relayout();
    }

    private void relayout()
    {
        for (int i=0; i<_drawStack.size(); ++i)
        {
            dropGraphic(_drawStack.get(i), false);
        }

        repaint();
    }

    @Override
    protected void draw(Graphics g)
    {
        super.draw(g);
        drawGridLines(g, Color.GRAY);
    }

    private void drawGridLines(Graphics g, Color aColour)
    {
        if (tileSize == null || tileSize.width == 0 || tileSize.height == 0)
            return;

        g.setColor(aColour);

        Rectangle bounds = this.getGraphicsBounds();
        if (bounds == null)
        {
            bounds = new Rectangle(0, 0, this.getSize().width, this.getSize().height);
        }

        Dimension padding = this.padding;
        if (padding == null)
            padding = new Dimension(0,0);

        float totalWidth = (tileSize.width + padding.width) * this.getZoom();
        float totalHeight = (tileSize.height + padding.height) * this.getZoom();

        float startX = (_origin.x % totalWidth) - totalWidth - (padding.width * this.getZoom() / 2);
        float startY = (_origin.y % totalHeight) - totalHeight - (padding.height * this.getZoom() / 2);
                            

        float x = startX;
        float y = startY;

        while(x + totalWidth <= bounds.width)
        {
            x += padding.width * this.getZoom();
            y = startY;
            
            while(y + totalWidth <= bounds.height)
            {
                y += padding.height * this.getZoom();
                g.drawRect((int)x, (int)y, (int)(tileSize.width * this.getZoom()), (int)(tileSize.height * this.getZoom()));
                y += tileSize.height * this.getZoom();
            }

            x += tileSize.width * this.getZoom();
        }
    }

    @Override
    protected void dropGraphic(GraphicObject aGraphic, boolean aUndoable)
    {
        Rectangle bounds = this.getGraphicsBounds();
        if (bounds == null)
        {
            bounds = new Rectangle(0, 0, this.getSize().width, this.getSize().height);
        }

        Dimension padding = this.padding;
        if (padding == null)
            padding = new Dimension(0,0);
        
        Rectangle r = aGraphic.getRect();
       
        if (this.tileSize != null)
        {
            Dimension total = new Dimension(
                    tileSize.width + padding.width,
                    tileSize.height + padding.height);


            Point currentCentre = new Point(r.x + r.width/2, r.y + r.height/2);
            Point closestPoint = new Point(
                (currentCentre.x - (Math.abs(currentCentre.x) % total.width)) + (total.width/2),
                (currentCentre.y - (Math.abs(currentCentre.y) % total.height)) + (total.height/2) );

            aGraphic.setRect(new Rectangle(
                    closestPoint.x - r.width/2,
                    closestPoint.y - r.height/2,
                    r.width,
                    r.height));
        }

        super.dropGraphic(aGraphic, aUndoable);
    }
}
