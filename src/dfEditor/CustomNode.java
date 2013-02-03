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

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Color;

public class CustomNode extends DefaultMutableTreeNode
{
    private Color colour = Color.BLUE;
    private int childDirUniqueID;
    private int childLeafUniqueID;
    private boolean isLeaf;
    private Object _object;


    public CustomNode(Object userObject, boolean allowsChildren)
    {
       super(userObject, allowsChildren);

       isLeaf = !allowsChildren;
       childDirUniqueID = 0;
       childLeafUniqueID = 0;
    }

    @Override
    public boolean isLeaf()
    {
        return isLeaf;
    }

    public String suggestNameForChildLeaf()
    {
        return new String(""+childLeafUniqueID++);

    }

    public String suggestNameForChildDir()
    {
        return new String(""+childDirUniqueID++);

    }

    public Color getColour()
    {
        return colour;
    }

    public void setColour(Color c)
    {
        //System.out.println("setting colour of " + this + " to " + c);
        colour = c;
    }

    public void setCustomObject(final Object aObj)
    {
        _object = aObj;
    }

    public final Object getCustomObject()
    {
        return _object;
    }

    public String getFullPathName()
    {
        String path = new String("");

        Object[] objects = this.getUserObjectPath();

        for (int i=0; i<objects.length; ++i)
        {            
            path += objects[i];
            if (i < objects.length-1 && i > 0 || !this.isLeaf())
                path += "/";
        }

        return path;
    }

}
