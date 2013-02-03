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
//import java.util.ArrayList;
import java.util.Enumeration;
import java.awt.image.BufferedImage;

/**
 *
 * @author s4m20
 */
public class PixelPacker
{
    private class PixelRectPair
    {
        public Rectangle rect;
        public int[] pixels;

        public PixelRectPair(final int[] aPixels, final Rectangle aRect)
        {
            rect = aRect;
            pixels = aPixels;
        }
    }

    private Rectangle applySpacing(final Rectangle aRect, final int aSpacing)
    {
        return new Rectangle(aRect.x - aSpacing, aRect.y - aSpacing, aRect.width + aSpacing, aRect.height + aSpacing);
    }

    public boolean packRects(final Rectangle aMasterRect, final Rectangle[] aSubRects, final int aSpacing)
    {
        boolean bSuccess = true;

        Node rootNode = new Node();
        rootNode.rect = aMasterRect;
        for (int i=0; i<aSubRects.length; ++i)
        {
            Node node = rootNode.Insert(applySpacing(aSubRects[i], aSpacing));
            if (node != null)
                copyRect(applySpacing(node.rect, -aSpacing), aSubRects[i]); // don't want to swap pointers
            else
                bSuccess = false;
        }
        return bSuccess;
    }

    public BufferedImage packPixels(final BufferedImage aOrigImage, final Rectangle[] aRects, boolean bPowerOfTwo)
    {
        int[] origPixels = new int[aOrigImage.getWidth(null) * aOrigImage.getHeight(null)];
        aOrigImage.getRGB( 0,
                    0,
                    aOrigImage.getWidth(),
                    aOrigImage.getHeight(),
                    origPixels,
                    0,
                    aOrigImage.getWidth());


        //Dictionary<Rectangle, int[]> dict = new Hashtable<Rectangle, int[]>();
        PixelRectPair[] pairs = new PixelRectPair[aRects.length];

        // first create a seperate array of pixels for every sprite
        for (int i=0; i<aRects.length; ++i)
        {
            Rectangle r = aRects[i];
            int[] pixels = new int[r.width * r.height];          

            for (int y=0; y<r.height; ++y)
            {
                for (int x=0; x<r.width; ++x)
                {
                    pixels[y * r.width + x] = origPixels[(y + r.y) * aOrigImage.getWidth() + (r.x + x)];
                }
            }

            pairs[i] = new PixelRectPair(pixels, r);
        }

        // TODO: sprites totally enclosed by other sprites should remain enclosed
        // TODO: multiple images?

      	// sort by size
	for (int i=0; i<pairs.length-1; ++i)
	{
            double area1 = pairs[i].rect.width * pairs[i].rect.height;
            double area2 = pairs[i+1].rect.width * pairs[i+1].rect.height;

            if (area1 < area2)
            {
		PixelRectPair temp = pairs[i];
                pairs[i] = pairs[i+1];
                pairs[i+1] = temp;
            }
	}

        // do the packing here
        // madly inefficient but doesn't seem to matter a flying fuck
        // some seriously shoddy coding here actually.. if neccessary will come
        // back to it. Bit drunk
        boolean bFailed = true;
        int sizeX = 1;
        int sizeY = sizeX;
        while (true)
        {
            bFailed = false;
            Node rootNode = new Node();
            rootNode.rect = new Rectangle(0, 0, sizeX, sizeY);
            for (int i=0; i<pairs.length; ++i)
            {
                Node node = rootNode.Insert(pairs[i].rect);
                if (node == null)
                {
                    // failed to insert
                    bFailed = true;
                    if (sizeX > sizeY)
                    {
                        if (bPowerOfTwo)
                            sizeY *= 2;
                        else
                            sizeY += 1;
                    }
                    else
                    {
                        if (bPowerOfTwo)
                            sizeX *= 2;
                        else
                            sizeX += 1;
                    }
                    break;
                }                
            }
            if (!bFailed)
                break;            
        }
        Node rootNode = new Node();
        rootNode.rect = new Rectangle(0, 0, sizeX, sizeY);
        for (int i=0; i<pairs.length; ++i)
        {
            Node node = rootNode.Insert(pairs[i].rect);
            copyRect(node.rect, pairs[i].rect); // don't want to swap pointers
        }

        // create the new image!

        // first work out the min size
        // constrain as tight as poss
        {
            sizeX = sizeY = 0;
            for (int i=0; i<pairs.length; ++i)
            {
                Rectangle r = pairs[i].rect;
                if (sizeX < r.x + r.width)
                    sizeX = r.x + r.width;
                if (sizeY < r.y + r.height)
                    sizeY = r.y + r.height;
            }
        }
        
        // shouldn't need to do this with power of two because the algo is 
        // supposed to give us correct dimensions, sometimes it is wrong though,
        // so re-constrain it according to min size above
        if (bPowerOfTwo)
        {
            int x = 1;
            while(x < sizeX)
                x *= 2;
            sizeX = x;
            int y = 1;
            while(y < sizeY)
                y *= 2;
            sizeY = y;
        }
        
        

        if (sizeX * sizeY == 0)
            return null;
        
        // create image
        int[] newImagePixels = new int[sizeX * sizeY];

        // copy rects to new image
        for (int i=0; i<pairs.length; ++i)
        {
            Rectangle r = pairs[i].rect;
            int[] pixels = pairs[i].pixels;

            for (int y=0; y<r.height; ++y)
            {
                for (int x=0; x<r.width; ++x)
                {
                    newImagePixels[(r.y+y) * sizeX + (r.x+x)] = pixels[y * r.width + x];
                }
            }
        }

        BufferedImage newImage = new BufferedImage(sizeX, sizeY, aOrigImage.getType());
        newImage.setRGB(0, 0, sizeX, sizeY, newImagePixels, 0, sizeX);
        
        return newImage;
    }

    // don't want to change the pointer
    private void copyRect(final Rectangle aFrom, final Rectangle aTo)
    {
        aTo.x = aFrom.x;
        aTo.y = aFrom.y;
        aTo.width = aFrom.width;
        aTo.height = aFrom.height;
    }
 
    private class Node
    {
        public Node[] children;
        public Rectangle rect;
        boolean bTaken;

        Node()
        {
            children = null;
            bTaken = false;
        }


        private Node Insert(final Rectangle aRect)
        {
            if (children != null) // not  a leaf
            {
                // try inserting into first child
                Node node = children[0].Insert(aRect);
                if (node != null)
                    return node;

                // no room, insert into second
                return children[1].Insert(aRect);
            }
            else
            {
                if (bTaken)
                    return null;

                if (    aRect.width > this.rect.width
                    ||  aRect.height > this.rect.height)
                {
                    // node is too small, rect can't fit inside
                    return null;
                }

                if (    aRect.width == this.rect.width
                    &&  aRect.height == this.rect.height)
                {
                    // fits perfectly
                    this.bTaken = true;
                    return this;
                }

                // otherwise split node and create kids
                children = new Node[2];
                children[0] = new Node();
                children[1] = new Node();

                // decide which way to split
                int dw = this.rect.width - aRect.width;
                int dh = this.rect.height - aRect.height;

                if (dw > dh)
                {
                    children[0].rect = new Rectangle(   this.rect.x,
                                                        this.rect.y,
                                                        aRect.width,
                                                        this.rect.height);

                    children[1].rect = new Rectangle(   this.rect.x + aRect.width,
                                                        this.rect.y,
                                                        this.rect.width - aRect.width,
                                                        this.rect.height);
                }
                else
                {
                    children[0].rect = new Rectangle(   this.rect.x,
                                                        this.rect.y,
                                                        this.rect.width,
                                                        aRect.height);

                    children[1].rect = new Rectangle(   this.rect.x,
                                                        this.rect.y + aRect.height,
                                                        this.rect.width,
                                                        this.rect.height - aRect.height);
                }

                // insert into first child we created
                return children[0].Insert(aRect);
            }
        }
    }
}


