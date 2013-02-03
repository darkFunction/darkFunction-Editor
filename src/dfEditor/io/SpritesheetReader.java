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

import java.io.File;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.swing.tree.DefaultTreeModel;
import dfEditor.CustomNode;
import dfEditor.SelectionBox;
import java.awt.Rectangle;

/**
 *
 * @author s4m20
 *
 * TODO: This file has fuck-all error handling
 */
public class SpritesheetReader
{
    private Document doc = null;
    private File file = null;

    public SpritesheetReader(File aFile)
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            doc = builder.parse(aFile);

            file = aFile;
        }
        catch (Exception e)
        {
            // TODO
        }
    }

    public String getImagePath()
    {        
        NodeList imgList = doc.getElementsByTagName("img");

        if (imgList.getLength() == 0)
            return null;
        
        String imgName = ((Element)imgList.item(0)).getAttribute("name");

        String fullFileName = file.getParentFile().getPath() + "/" + imgName;

        return fullFileName;
    }

    public DefaultTreeModel getTreeModel()
    {
        DefaultTreeModel retTree = null;

        NodeList defList = doc.getElementsByTagName("definitions");

        // only support one bunch of definitions, use the first
        if (defList.getLength() > 0)
        {
            Element first = (Element)defList.item(0);

            if (first != null)
            {
                NodeList children = first.getChildNodes();
                Element firstChild = null;

                for (int i=0; i<children.getLength(); i++)
                {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE)
                    {
                        firstChild = (Element)child;
                        break;
                    }
                }

                if (firstChild != null)
                {
                    retTree = createTreeModelFromDOM(firstChild);
                }
            }
        }

        return retTree;
    }

    private DefaultTreeModel createTreeModelFromDOM(Element aRootNode)
    {
        CustomNode rootNode = createCustomNodeFromDOMNode(aRootNode);

        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode, true);

        return treeModel;
    }

    public CustomNode createCustomNodeFromDOMNode(Element aDOMNode)
    {
        boolean bDir = false;
        if (aDOMNode.getTagName().equals("dir"))
            bDir = true;
        CustomNode node = new CustomNode(aDOMNode.getAttribute("name"), bDir);

        if (node.isLeaf() && aDOMNode.getTagName().equals("spr"))
        {
            node.setCustomObject(new SelectionBox(                    
                    new Rectangle(
                        Integer.parseInt(aDOMNode.getAttribute("x")),
                        Integer.parseInt(aDOMNode.getAttribute("y")),
                        Integer.parseInt(aDOMNode.getAttribute("w")),
                        Integer.parseInt(aDOMNode.getAttribute("h"))),
                    node.getColour()));
        }
        else
        {
            for (int i=0; i<aDOMNode.getChildNodes().getLength(); ++i)
            {
                Node childNode = aDOMNode.getChildNodes().item(i);
                Element element = null;
                if (childNode.getNodeType() == Node.ELEMENT_NODE)
                    element = (Element)childNode;

                if (element != null)
                {
                    CustomNode customNode = createCustomNodeFromDOMNode(element);
                    node.add(customNode);
                }
            }
        }

        return node;
    }
}