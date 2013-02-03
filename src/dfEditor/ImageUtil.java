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

import java.awt.image.VolatileImage;
import java.awt.image.BufferedImage;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsConfiguration;
import java.awt.AlphaComposite;
import java.awt.Color;


/**
 *
 * @author s4m20
 */
public class ImageUtil
{
    public static VolatileImage createVolatileImage(int width, int height, int transparency)
    {
        if (width == 0 || height == 0)
            return null;
        
	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
	VolatileImage image = null;

	image = gc.createCompatibleVolatileImage(width, height, transparency);

	int valid = image.validate(gc);

	if (valid == VolatileImage.IMAGE_INCOMPATIBLE)
        {
            image = createVolatileImage(width, height, transparency);
	}

	return image;
    }

    public static VolatileImage createVolatileImage(BufferedImage bi)
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

        VolatileImage vimage = createVolatileImage(bi.getWidth(), bi.getHeight(), java.awt.Transparency.TRANSLUCENT);

        java.awt.Graphics2D g = null;      

        try
        {
            g = vimage.createGraphics();

            // clear to transparent
            g.setComposite(AlphaComposite.Src);
            g.setColor(new Color(0,0,0,0));
            g.fillRect(0, 0, vimage.getWidth(), vimage.getHeight());

            g.drawImage(bi, null, 0, 0);
        }
        finally
        {
            g.dispose();
        }

        return vimage;
    }

}
