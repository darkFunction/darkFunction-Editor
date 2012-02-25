package dfEditor.io;

import java.util.ArrayList;
import java.io.File;
import javax.swing.filechooser.*;


public class CustomFilter extends FileFilter
{
    public final static String EXT_ANIM = "anim";
    public final static String EXT_SPRITE = "sprites";
    public final static String EXT_GIF = "gif";

    private ArrayList<String> ext = null;

    public CustomFilter()
    {
        ext = new ArrayList<String>();
    }

    public String getExtension()
    {
        return ext.get(0);
    }

    public void addExtension(String aExt)
    {
        if (!aExt.isEmpty())
        {
            ext.add(aExt);
        }
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = Utils.getExtension(f);
        if (extension != null) 
        {
            for (int i=0; i<ext.size(); ++i)
            {
                if (extension.equals(ext.get(i)))
                    return true;
            }            
        }

        return false;
    }

    public String getDescription() 
    {
        String desc = new String();
        for (int i=0; i<ext.size(); ++i)
        {
            String verbose = null;
            String extension = ext.get(i);
            if (extension.equals(EXT_ANIM))
                verbose = "Animations";
            else if (extension.equals(EXT_SPRITE))
                verbose = "Spritesheets";
            else if (extension.equals(EXT_GIF))
                verbose = "Gif animated image";

            desc += verbose + " ";
            if (verbose != null)
                desc += "(*."+extension+")";
            if (i < ext.size()-1)
                desc += ", ";
        }
        return desc;
    }
}

