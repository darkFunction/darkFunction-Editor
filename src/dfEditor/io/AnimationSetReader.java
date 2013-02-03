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
import dfEditor.SpriteTree;
import java.awt.Rectangle;
import java.util.ArrayList;
import dfEditor.animation.*;
import dfEditor.*;
import java.awt.image.BufferedImage;
import java.awt.Point;

/**
 *
 * @author s4m20
 *
 * TODO: This file has fuck-all error handling
 */
public class AnimationSetReader
{
    private Document doc = null;
    private File file = null;
    private float version = 1;

    public AnimationSetReader(File aFile) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException
    {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        doc = builder.parse(aFile);

        file = aFile;
    }

    public String getSpriteSheetPath()
    {
        NodeList setList = doc.getElementsByTagName("animations");
        String spriteSheetName = ((Element)setList.item(0)).getAttribute("spriteSheet");
        String versionString = ((Element)setList.item(0)).getAttribute("ver");
        if (versionString != null)
        {
            try { 
                version = Float.parseFloat(versionString);
            } catch (java.lang.NumberFormatException nfe) {
                version = 1;
            }
        }

        String fullFileName = file.getParentFile().getPath() + "/" + spriteSheetName;

        return fullFileName;
    }

    public ArrayList<Animation> getAnimations(SpriteTree aSpriteTree, BufferedImage aImage) throws Exception
    {
        ArrayList animations = new ArrayList<Animation>();
        NodeList animNodeList = doc.getElementsByTagName("anim");

        for (int i=0; i<animNodeList.getLength(); ++i)
        {
            Node animNode = animNodeList.item(i);
            Animation animation = new Animation( ((Element)animNode).getAttribute("name") );

            String loopsStr = ((Element)animNode).getAttribute("loops");
            if (loopsStr != null && loopsStr.length() != 0)
            {
                int loops = Integer.parseInt(loopsStr);
                animation.setLoops(loops);
            }

            NodeList cellNodeList = animNode.getChildNodes();

            for (int j=0; j<cellNodeList.getLength(); ++j)
            {
                Node cellNode = cellNodeList.item(j);
                if (cellNode.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                AnimationCell cell = new AnimationCell();
                cell.setDelay( Integer.parseInt(((Element)cellNode).getAttribute("delay")) );

                NodeList spriteNodeList = cellNode.getChildNodes();
                for (int k=0; k<spriteNodeList.getLength(); ++k)
                {
                    Node spriteNode = spriteNodeList.item(k);
                    if (spriteNode.getNodeType() != Node.ELEMENT_NODE)
                        continue;
                        
                    CustomNode treeNode = aSpriteTree.getNodeForPath(
                            ((Element)spriteNode).getAttribute("name")
                            );

                    if (treeNode != null && treeNode.isLeaf())
                    {
                        GraphicObject spriteArea = (GraphicObject)treeNode.getCustomObject();

//                        boolean centreAnchor = false;
//                        String anchorString = ((Element)spriteNode).getAttribute("anchor");
//                        if (anchorString != null && anchorString.length() != 0)
//                        {
//                            if (anchorString.equals("centre"))
//                                centreAnchor = true;
//                        }
                        
                        int x=0;
                        int y=0;
                        int z=0;
                        try {
                            x = Integer.parseInt(((Element)spriteNode).getAttribute("x"));
                            y = Integer.parseInt(((Element)spriteNode).getAttribute("y"));   
                            z = Integer.parseInt(((Element)spriteNode).getAttribute("z"));   
                        } catch (NumberFormatException e) {}
                        
                        
                        Rectangle r = spriteArea.getRect();
                                                
                        if (version >= 1.2)
                        {
                            x -= r.width/2;
                            y -= r.height/2;                            
                        }
                                               
                        SpriteGraphic graphic = new SpriteGraphic(aImage, new Point(x, y), r);
                        
                        float angle = 0;
                        String angleString = ((Element)spriteNode).getAttribute("angle");
                        if (angleString != null && angleString.length() != 0)
                            angle = Float.parseFloat(angleString);                        
                        graphic.setAngle(angle);
                        graphic.saveAngle();
                        
                        boolean flipH = false;
                        boolean flipV = false;
                        
                        String flipHString = ((Element)spriteNode).getAttribute("flipH");
                        String flipVString = ((Element)spriteNode).getAttribute("flipV");
                        if (flipHString != null && flipHString.length() > 0)
                            flipH = 0 != Integer.parseInt(flipHString);                                         
                        if (flipVString != null && flipVString.length() > 0)
                            flipV = 0 != Integer.parseInt(flipVString);                                         
                        
                        if (flipV)
                            graphic.flip(false);
                        if (flipH)
                            graphic.flip(true);                            
                        
                        cell.addSprite(treeNode, graphic);
                        cell.setZOrder(graphic, z);
                    }
                }

                animation.addCell(cell);
            }
            animations.add(animation);
        }
        
        return animations;
    }

//    public ArrayList<Animation> getAnimations()
//    {
//        ArrayList<Animation> animationList = new ArrayList<Animation>();
//
//        NodeList animNodeList = doc.getElementsByTagName("anim");
//        for (int i=0; i<animNodeList.getLength(); ++i)
//        {
//            Node animNode = animNodeList.item(i);
//            Animation animation = new Animation( ((Element)animNode).getAttribute("name") );
//
//            NodeList cellNodeList = animNode.getChildNodes();
//
//            for (int j=0; j<cellNodeList.getLength(); ++j)
//            {
//                Node cellNode = cellNodeList.item(j);
//                AnimationCell cell = new AnimationCell();
//                cell.setDelay( Integer.parseInt(((Element)cellNode).getAttribute("delay")) );
//                //cell.addSprite(null, null);
//            }
//
//        }
//
//        return animationList;
//    }


//    public DefaultTreeModel getTreeModel()
//    {
//        DefaultTreeModel retTree = null;
//
//        NodeList defList = doc.getElementsByTagName("definitions");
//
//        // only support one bunch of definitions, use the first
//        if (defList.getLength() > 0)
//        {
//            Element first = (Element)defList.item(0);
//
//            if (first != null && first.getFirstChild() != null)
//            {
//                retTree = createTreeModelFromDOM((Element)first.getFirstChild());
//            }
//        }
//
//        return retTree;
//    }
//
//    private DefaultTreeModel createTreeModelFromDOM(Element aRootNode)
//    {
//        CustomNode rootNode = createCustomNodeFromDOMNode(aRootNode);
//
//        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode, true);
//
//        return treeModel;
//    }
//
//    public CustomNode createCustomNodeFromDOMNode(Element aDOMNode)
//    {
//        CustomNode node = new CustomNode(aDOMNode.getAttribute("name"), aDOMNode.hasChildNodes());
//
//        if (node.isLeaf() && aDOMNode.getTagName().equals("spr"))
//        {
//            node.setCustomObject(new SelectionBox(
//                    new Rectangle(
//                        Integer.parseInt(aDOMNode.getAttribute("x")),
//                        Integer.parseInt(aDOMNode.getAttribute("y")),
//                        Integer.parseInt(aDOMNode.getAttribute("w")),
//                        Integer.parseInt(aDOMNode.getAttribute("h"))),
//                    node.getColour()));
//        }
//        else
//        {
//            for (int i=0; i<aDOMNode.getChildNodes().getLength(); ++i)
//            {
//                CustomNode childNode = createCustomNodeFromDOMNode((Element)aDOMNode.getChildNodes().item(i));
//                node.add(childNode);
//            }
//        }
//
//        return node;
//    }
}