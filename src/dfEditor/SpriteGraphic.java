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

import java.awt.*;
import java.awt.image.VolatileImage;
import java.awt.image.BufferedImage;
import dfEditor.PixelBuffer;

/**
 *
 * @author s4m20
 */
public class SpriteGraphic extends GraphicObject
{
    private static final Color SELECTION_COLOUR = new Color(100, 255, 100, 150);
    private static final int SELECTION_BORDER = 0;

    private VolatileImage vImage;
    private VolatileImage selectedVImage;
    private BufferedImage bImage;

    private Rectangle subRect;
    private Rectangle untrimmedSubRect;

    private boolean bTrimmed = false;
    
    protected boolean _hFlip, _vFlip;

    public SpriteGraphic(BufferedImage aImage, Point aPoint, Rectangle aSubRect)
    {
        super(new Rectangle(aPoint.x, aPoint.y, aSubRect.width, aSubRect.height));

        subRect = aSubRect;
        untrimmedSubRect = null;
                
        _hFlip = _vFlip = false;        
        
        bImage = aImage;        
        vImage = createVImage();      
        selectedVImage = null;
    }

    private VolatileImage createVImage()
    {
        VolatileImage vi = ImageUtil.createVolatileImage(subRect.width, subRect.height, Transparency.TRANSLUCENT);
        
        if (vi == null)
            return null;
        
        Graphics2D g2d = (Graphics2D)vi.getGraphics();
                
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1));
        g2d.fillRect(0, 0, subRect.width, subRect.height);
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);            
                
        g2d.drawImage(
                bImage,
                0,
                0,
                subRect.width,
                subRect.height,
                subRect.x,
                subRect.y,
                subRect.x + subRect.width,
                subRect.y + subRect.height,                
                null);
        
        
        return vi;
    }
    
    private VolatileImage createSelectedVImage()
    {
        VolatileImage vi = ImageUtil.createVolatileImage(subRect.width, subRect.height, Transparency.TRANSLUCENT);
        
        Graphics2D g2d = (Graphics2D)vi.getGraphics();
                
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1));
        g2d.fillRect(0, 0, subRect.width, subRect.height);
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);            
                
        g2d.drawImage(
                bImage,
                0,
                0,
                subRect.width,
                subRect.height,
                subRect.x,
                subRect.y,
                subRect.x + subRect.width,
                subRect.y + subRect.height,                
                null);
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1));
         
        
        g2d.setColor(SELECTION_COLOUR);
        g2d.fillRect(0, 0, subRect.width, subRect.height);
        
        return vi;           
    }
    
    public SpriteGraphic copy()
    {
        SpriteGraphic graphic =new SpriteGraphic(
                bImage,
                new Point(this.getRect().x, this.getRect().y),
                subRect  );
        
        graphic.setAngle(_angle);
        graphic.saveAngle();
        
        if (_vFlip)
            graphic.flip(false);
        
        if (_hFlip)
            graphic.flip(true);
        
        return graphic;
    }
    
    public void flip(boolean bHoriz)
    {
        if (bHoriz)
            _hFlip = !_hFlip;
        else
            _vFlip = !_vFlip;  
    }
    
    public boolean isFlippedH()
    {
        return _hFlip;
    }
    
    public boolean isFlippedV()
    {
        return _vFlip;
    }
    
    
    public boolean pixelIsTransparent(int aX, int aY)
    {
        if (aX < 0 || aY < 0 || aX > getRect().width || aY > getRect().height)
            return true; // not in rect
        
        PixelBuffer pb = new PixelBuffer(bImage);
        
        return (pb.hasTransparentPixels() && pb.isTransparentAt(aX + subRect.x, aY + subRect.y));
    }
            
    public void draw(Graphics g, Point aPoint, float aScale, boolean bDrawSelectedMode)
    {       
        Rectangle r = getRect();

        Rectangle dest = new Rectangle(
                aPoint.x + ((int)(r.x *  aScale)),
                aPoint.y + ((int)(r.y *  aScale)),
                (int)(r.width * aScale),
                (int)(r.height * aScale));

        if (vImage != null)
        {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

            int valid = vImage.validate(gc);
            if (valid == VolatileImage.IMAGE_INCOMPATIBLE)
            {
                vImage = createVImage();
            }

            Point topLeft = new Point(dest.x, dest.y);
            Point bottomRight = new Point(dest.x + dest.width, dest.y + dest.height);

            if (_hFlip)
            {
                topLeft.x = dest.x + dest.width;
                bottomRight.x = dest.x;
            }

            if (_vFlip)
            {
                topLeft.y = dest.y + dest.height;
                bottomRight.y = dest.y;
            }

            VolatileImage img = null;

            if (bDrawSelectedMode && isSelected())
            {
                if (selectedVImage == null || selectedVImage.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE)
                {
                    selectedVImage = this.createSelectedVImage();
                }

                img = selectedVImage;
            }        
            else
            {
                img = vImage;
            }

            g.drawImage(
                    img,
                    topLeft.x,
                    topLeft.y,
                    bottomRight.x,
                    bottomRight.y,
                    0,
                    0,
                    subRect.width,
                    subRect.height,                
                    null);
        }
        
        if (bDrawSelectedMode && isSelected())
        {
            g.setColor(new Color(0,0,0,100));
            g.drawRect(dest.x, dest.y, dest.width, dest.height);
        }
    }
    
    @Override
    public void constrainToBounds(Rectangle aBounds)
    {
        // force it to keep width - don't shrink when it won't fit in the canvas
        int backupWidth = this.getRect().width;
        int backupHeight = this.getRect().height;
        
        super.constrainToBounds(aBounds);
        
        this.getRect().width = backupWidth;
        this.getRect().height = backupHeight;        
    }
    
    public void trim()
    {
        if (bTrimmed)
            return;

        bTrimmed = true;
        
        untrimmedSubRect = new Rectangle(subRect.x, subRect.y, subRect.width, subRect.height);
        
        PixelBuffer pix = new PixelBuffer(bImage);

        pix.trimRect(subRect);
        Rectangle r = getRect();
        Rectangle newRect = new Rectangle(r.x, r.y, subRect.width, subRect.height);
        this.setRect(newRect);
        vImage = createVImage();
        selectedVImage = null;
    }

    public void untrim()
    {
        if (!bTrimmed)
            return;

        bTrimmed = false;

        subRect = new Rectangle (untrimmedSubRect.x, untrimmedSubRect.y, untrimmedSubRect.width, untrimmedSubRect.height);
        Rectangle r = getRect();
        Rectangle newRect = new Rectangle(r.x, r.y, subRect.width, subRect.height);
        this.setRect(newRect);
        vImage = createVImage();
        selectedVImage = null;
    }

}
