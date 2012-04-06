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
            if (i < objects.length-1 || !this.isLeaf())
                path += "/";
        }

        return path;
    }

}
