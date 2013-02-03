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

import java.awt.Rectangle;
import java.awt.Point;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 *
 * @author Sam
 */
public class PixelBuffer 
{
    private int[] pixelArray = null;
    private boolean markArray[] = null;
    private int _imgW = 0;
    private int _imgH = 0;
    private boolean _bHasTransparentPixels = false;
    private BufferedImage _img;
        
    public PixelBuffer(BufferedImage aImage)
    {
        _img = aImage;
       
        _imgW = _img.getWidth();
        _imgH = _img.getHeight();

        int size = _imgW * _imgH;
        pixelArray = new int[size];
        
        // store pixels for performing checks later
        _img.getRGB(0, 0, _imgW, _imgH, pixelArray, 0, _imgW);       

        // hardware optimisation
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();
        _img = config.createCompatibleImage(_imgW, _imgH, Transparency.TRANSLUCENT);
        _img.setRGB(0, 0, _imgW, _imgH, pixelArray, 0, _imgW);
        
        markArray = new boolean[size];
        for (int y=0; y<_imgH; ++y)
            for (int x=0; x<_imgW; ++x)
                markPixel(x, y, false);

        _bHasTransparentPixels = false;
        for (int y=0; y<_imgH; ++y)
        {
            for (int x=0; x<_imgW; ++x)
            {
                if (isTransparentAt(x, y))
                {
                    _bHasTransparentPixels = true;
                    break;
                }
            }
            if (_bHasTransparentPixels)
                break;
        }
    }
        
    public BufferedImage getImage()
    {
        return _img;
    }
    
    protected boolean isTransparentAt(final int aX, final int aY)
    {
        if (aX < 0 || aY < 0 || aX > _imgW-1 || aY > _imgH-1)
            return false;

        int c = pixelArray[aY * _imgW + aX];//bufferedImage.getRGB(aX, aY);

        int a = (c & 0xff000000) >> 24;

        return (a == 0);
    }

    private boolean pixelIsMarked(int aX, int aY)
    {
        if (aX < 0 || aY < 0 || aX >= _imgW || aY >= _imgH)
            return false;
        
        return markArray[aY * _imgW + aX];
    }

    private boolean markPixel(int aX, int aY, boolean bMark)
    {
        if (aX < 0 || aY < 0 || aX >= _imgW || aY >= _imgH)
            return false;

        markArray[aY * _imgW + aX] = bMark;
        return true;
    }

    private boolean isNeighbourOfMarkedPixel(int aX, int aY)
    {
        return (      pixelIsMarked(aX - 1, aY)
                  ||  pixelIsMarked(aX + 1, aY)
                  ||  pixelIsMarked(aX, aY - 1)
                  ||  pixelIsMarked(aX, aY + 1)
                  ||  pixelIsMarked(aX - 1, aY - 1)
                  ||  pixelIsMarked(aX + 1, aY - 1)
                  ||  pixelIsMarked(aX + 1, aY + 1)
                  ||  pixelIsMarked(aX - 1, aY + 1) );
    }


    private void markInfectNonTransparents(int aX, int aY)
    {
        // method 2 (non recursive)
        markPixel(aX, aY, true);
        int minX = aX - 1;
        int maxX = aX + 1;
        int minY = aY - 1;
        int maxY = aY + 1;
        
        boolean bChanged = false;
        do
        {
            bChanged = false;
            for (int y=minY; y<maxY; ++y)
            {
                for (int x=minX; x<maxX; ++x)
                {
                    if (!pixelIsMarked(x, y) && !isTransparentAt(x,y) && isNeighbourOfMarkedPixel(x, y))
                    {
                        if (markPixel(x, y, true))
                        {
                            bChanged = true;
                            if (x - 1 < minX)
                                minX = x - 1;
                            if (x + 2 > maxX)
                                maxX = x + 2;
                            if (y - 1 < minY)
                                minY = y - 1;
                            if (y + 2 > maxY)
                                maxY = y + 2;
                        }
                    }
                }
            }
        }
        while (bChanged);

        // method 1 (recursive)
        // can cause stack overflow because of heavy recursion
//        if (aX < 0 || aY < 0 || aX >= _imgW || aY >= _imgH)
//            return;
//
//        if (!isTransparentAt(aX, aY) && !pixelIsMarked(aX, aY))
//        {
//            markPixel(aX, aY, true);
//
//            markInfectNonTransparents(aX - 1, aY);
//            markInfectNonTransparents(aX + 1, aY);
//            markInfectNonTransparents(aX, aY - 1);
//            markInfectNonTransparents(aX, aY + 1);
//
//            markInfectNonTransparents(aX - 1, aY - 1);
//            markInfectNonTransparents(aX + 1, aY - 1);
//            markInfectNonTransparents(aX + 1, aY + 1);
//            markInfectNonTransparents(aX - 1, aY + 1);
//        }
    }

    public void trimRect(final Rectangle aRect)
    {
        int lastX = aRect.x;
        int lastY = aRect.y;
        int firstX = aRect.x + aRect.width;
        int firstY = aRect.y + aRect.height;
        boolean bFoundAPixel = false;
        
        for (int y=aRect.y; y<aRect.y + aRect.height; ++y)
        {
            for (int x=aRect.x; x<aRect.x + aRect.width; ++x)
            {
                if (!isTransparentAt(x, y))
                {
                    bFoundAPixel = true;
                    
                    if (x < firstX)
                        firstX = x;
                    if (y < firstY)
                        firstY = y;
                    if (x+1 > lastX)
                        lastX = x+1;
                    if (y+1 > lastY)
                        lastY = y+1;
                }
            }
        }

        if (bFoundAPixel)
        {
            aRect.x = firstX;
            aRect.y = firstY;
            aRect.width = lastX - firstX;
            aRect.height = lastY - firstY;
        }
        else
        {
            aRect.width = 0;
            aRect.height = 0;
        }           
    }
    
    public Rectangle getRectAroundSpriteAtPoint(final Point aPoint)
    {       
        markInfectNonTransparents(aPoint.x, aPoint.y);
        
        int minX = aPoint.x;
        int minY = aPoint.y;
        int maxX = aPoint.x;
        int maxY = aPoint.y;

        for (int y=0; y<_imgH; ++y)
        {
            for (int x=0; x<_imgW; ++x)
            {
                if (pixelIsMarked(x, y))
                {
                    if (x < minX)
                        minX = x;
                    if (y < minY)
                        minY = y;
                    if (x > maxX)
                        maxX = x;
                    if (y > maxY)
                        maxY = y;

                    markPixel(x, y, false);
                }
            }
        }       

        Rectangle r = new Rectangle(minX, minY, maxX - minX, maxY - minY);

        if (r.width > 0)
            r.width += 1;
        if (r.height > 0)
            r.height += 1;

        return r;
    }
    
    public void makeColourTransparent(int aCol32)
    {
        int c = aCol32;
        int a = (c & 0xff000000) >> 24;
        int r = (c & 0x00ff0000) >> 16;
        int g = (c & 0x0000ff00) >> 8;
        int b = (c & 0x000000ff);
        c = aCol32 & 0x00ffffff;

        for (int y=0; y<_imgH; ++y)
        {
            for (int x=0; x<_imgW; ++x)
            {
                if (pixelArray[y * _imgW + x] == aCol32)
                    pixelArray[y * _imgW + x] = c;
            }
        }

        _img.setRGB(0, 0, _imgW, _imgH, pixelArray, 0, _imgW);
        _bHasTransparentPixels = true;
    }
    
    public int pixelAt(int aX, int aY)
    {
        if (aX >= _imgW || aY >= _imgH)
            return -1;
        
        return pixelArray[aY * _imgW + aX];
    }
    
    public boolean hasTransparentPixels()
    {
        return _bHasTransparentPixels;
    }
}
