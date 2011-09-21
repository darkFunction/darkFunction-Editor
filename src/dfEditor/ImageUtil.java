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
