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

