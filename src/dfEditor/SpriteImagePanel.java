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
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
//import java.awt.Image;

public class SpriteImagePanel extends GraphicPanel
{
    public SpriteImagePanel()
    {
        _graphicBounds = new Rectangle(0, 0, 0, 0);
    }

    public void setTextureArea(int aWidth, int aHeight)
    {
        Rectangle r = this.getGraphicsBounds();
        this.setGraphicsBounds(new Rectangle(r.x, r.y, aWidth, aHeight));
    }

    @Override
    protected void draw(Graphics g)
    {
        super.draw(g);

        this.drawCheckerBoardBuffer(g, convertRectToViewRect(_graphicBounds));        
    }
    
    @Override
    public void drawStack(Graphics g)
    {        
        Rectangle r = convertRectToViewRect(_graphicBounds);
        g.clipRect(r.x, r.y, r.width, r.height);
        super.drawStack(g);
        g.setClip(null);
    }
    
    public BufferedImage getImage()            
    {
        BufferedImage image = new BufferedImage(_graphicBounds.width, _graphicBounds.height, BufferedImage.TYPE_INT_ARGB);
               
        if (image != null)
        {
            Graphics gImg = image.getGraphics();
            this.unselectAllGraphics();
            super.drawStack(gImg, new java.awt.Point(0,0), 1.0f, 1.0f);
        }
        
        return image;
    }


}
