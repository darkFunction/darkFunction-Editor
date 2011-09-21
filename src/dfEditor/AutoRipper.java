import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
 
public class AutoRipper
{
    private static BufferedImage spriteSheet;
    private static Color color;
    private static ArrayList<BufferedImage> sprites;
    private static int tolerance = 75;
     
    public static void main(String[] args)
    {
        sprites = new ArrayList<BufferedImage>();
        spriteSheet = open();
        scanAutomaticallyIteratively();
        saveSprites();
    }
     
    private static BufferedImage open()
    {
        JFileChooser fc = new JFileChooser();
         
        fc.setCurrentDirectory(new File("/Users/Will/Desktop"));
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new ImageFileFilter());
        fc.setDialogTitle("Choose a Sprite Sheet");
         
        int returnVal = fc.showDialog(null, "Choose a Sprite Sheet");
         
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
                File file = fc.getSelectedFile();
            try
            {
                //Read the sprite sheet, then translate it to ARGB so I know how it is formatted
                BufferedImage im = ImageIO.read(file);
                BufferedImage ret = new BufferedImage(im.getWidth(),im.getHeight(),BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = ret.createGraphics();
                g.drawImage(im,0,0,null);
                g.dispose();
                int[] c = ret.getRaster().getPixel(0,0,new int[4]);
                color = new Color(c[0],c[1],c[2],c[3]);
                return ret;
            }
            catch(Exception e) {e.printStackTrace();}
        }
        return null;
    }
     
    private static String chooseSaveDirectory()
    {
        JFileChooser fc = new JFileChooser();
         
        fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new DirectoryFileFilter());
        fc.setDialogTitle("Choose a Folder");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         
        int returnVal = fc.showDialog(null, "Save Results in Folder...");
         
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            return fc.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
     
    private static class ImageFileFilter extends javax.swing.filechooser.FileFilter
    {
        public boolean accept(File f)
        {
            if (f.isDirectory())
                return true;
 
            String e = "";
                String s = f.getName();
                int i = s.lastIndexOf('.');
                if (i > 0 &&  i < s.length() - 1)
                e = s.substring(i+1).toLowerCase();
                 
                String[] types = {"jpg","gif","tif","bmp","png","tiff","jpeg"};
                 
                for (int j = 0; j < types.length; j++)
                    if (e.equals(types[j]))
                        return true;
            return false;
        }
         
        public String getDescription()
        {
            return ".jpg .gif .tif .bmp .png";
        }
    }
     
    private static class DirectoryFileFilter extends javax.swing.filechooser.FileFilter
    {
        public boolean accept(File f)
        {
            if (f.isDirectory())
                return true;
            return false;
        }
         
        public String getDescription()
        {
            return "Directories";
        }
    }
     
    public static void scanAutomaticallyIteratively()
    {
        if (spriteSheet == null)
            return;
         
        int minSize = 50;
         
        //A 2D array of booleans that tells whether a pixel has already been factored or not
        boolean[][] scanned = new boolean[spriteSheet.getWidth()][spriteSheet.getHeight()];
        //An ArrayList of all pixels that are connected. Is refreshed automatically per scan
        ArrayList<Point> connectedPixels = new ArrayList<Point>();
         
        for (int y = 0; y < spriteSheet.getHeight(); y++)
        {
            for (int x = 0; x < spriteSheet.getWidth(); x++)
            {
                //If a pixel is found that is within bounds, hasn't been scanned and
                //is not transparent, we need to search its connected pixels
                if (inBounds(x,y) && !scanned[x][y] && !pixelIsTransparent(x,y))
                {
                    //Add the point to the list of connected pixels
                    connectedPixels.add(new Point(x,y));
                    scanned[x][y] = true;
                     
                    //Keep searching every pixel within the list until all the
                    //pixels within the list's adjacent pixels have been scanned,
                    //are transparent, or are not in bounds.
                    int search = 0;
                    while (search < connectedPixels.size())
                    {
                        Point p = (Point) connectedPixels.get(search);
                        //Search the surrounding pixels. If any of them are not scanned, not transparent,
                        //and within bounds (inside the image), call this method recursively with new x and y.
                        for (int i = -1; i <= 1; i++)
                        {
                            for (int j = -1; j <= 1; j++)
                            {
                                if (inBounds(i+p.x,j+p.y) && !scanned[p.x+i][p.y+j] && !pixelIsTransparent(i+p.x,j+p.y))
                                {
                                    connectedPixels.add(new Point(i+p.x,j+p.y));
                                    scanned[i+p.x][j+p.y] = true;
                                }
                            }
                        }
                        search++;
                    }
                     
                    //There should now be a completed sprite in the list
                    addSpriteFromPixelList(connectedPixels,minSize);
                    connectedPixels.clear();
                }
            }
        }
    }
     
    public static boolean inBounds(int x, int y)
    {
        return (x >= 0 && x < spriteSheet.getWidth() && y >= 0 && y < spriteSheet.getHeight());
    }
     
    public static boolean pixelIsTransparent(int x, int y)
    {
        int[] sprite = spriteSheet.getRaster().getPixel(x,y,new int[4]);
        int[] transColor = {color.getRed(),color.getBlue(),color.getGreen(),color.getAlpha()};
        int total = 0;
         
        for (int i = 0; i < sprite.length; i++)
        {
            total += Math.abs(sprite[i]-transColor[i]);
            if (total > tolerance)
                return false;
        }
        return true;
    }
     
    public static void addSpriteFromPixelList(ArrayList<Point> connectedPixels, int minSize)
    {
        //Don't add if there are not minmum size pixels in the sprite
        if (connectedPixels.size() < minSize)
            return;
        //Theoretically, a new sprite has now been created and all of its points
        //are within the connectedPixels list. Now we need translate a list of
        //pixels into a BufferedImage, first finding its min/max coordinates.
        int minX, minY, maxX, maxY;
        minX = maxX = ((Point)connectedPixels.get(0)).x;
        minY = maxY = ((Point)connectedPixels.get(0)).y;
        for (int i = 1; i < connectedPixels.size(); i++)
        {
            Point p = (Point) connectedPixels.get(i);
            if (p.x < minX)
                minX = p.x;
            if (p.x > maxX)
                maxX = p.x;
            if (p.y < minY)
                minY = p.y;
            if (p.y > maxY)
                maxY = p.y;
        }
        //Now that we have the bounds of the image, we can add the sprite and clear the pixels
        addSprite(minX,minY,maxX-minX+1,maxY-minY+1);
    }
     
    public static void addSprite(int x, int y, int width, int height)
    {
        if (width > 0 && height > 0)
        {
            BufferedImage sprite = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = sprite.createGraphics();
            g.drawImage(spriteSheet,-x,-y,null);
            g.dispose();
             
            //Turn the specified color transparent
            for (int i = 0; i < sprite.getWidth(); i++)
                for (int j = 0; j < sprite.getHeight(); j++)
                    if(pixelIsTransparent(i+x,j+y))
                        sprite.getRaster().setPixel(i,j,new int[]{255,255,255,255});
             
            if (sprite.getWidth() > 0 && sprite.getHeight() > 0)
                sprites.add(sprite);
        }
    }
    public static void saveSprites()
    {
        String saveFolder = chooseSaveDirectory();
         
        if (saveFolder != null)
        {
            for (int i = 0; i < sprites.size(); i++)
            {
                try
                {
                    ImageIO.write((BufferedImage)sprites.get(i), "png", new java.io.File(saveFolder + "/" + i + ".png"));
                }
                catch (Exception ex) {ex.printStackTrace();}
            }
        }
    }
}