/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
