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

import dfEditor.*;
import java.io.File;
import dfEditor.animation.*;
import com.generationjava.io.xml.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
/**
 * Interface between the editors and the file system.
 * Also encapsulates the format.
 *
 * @author s4m20
 */
public class AnimationSetWriter
{
    public AnimationSetWriter()
    {

    }

    public void createAnimationSet(File aSaveFile, String aSpritesheetName, ArrayList<Animation> aAnimList)
    {
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(aSaveFile));

            PrettyPrinterXmlWriter xmlwriter = new PrettyPrinterXmlWriter(new SimpleXmlWriter(out));
            
            xmlwriter.writeXmlVersion();
            String comment = "Generated by darkFunction Editor (www.darkfunction.com)";
            xmlwriter.writeComment(comment);

            xmlwriter.writeEntity("animations");
            xmlwriter.writeAttribute("spriteSheet", aSpritesheetName);
            xmlwriter.writeAttribute("ver", "1.2");

            for (int i=0; i<aAnimList.size(); ++i)
            {
                writeAnimToXml(xmlwriter, aAnimList.get(i));
            }

            xmlwriter.endEntity();

            xmlwriter.close();
            System.err.println(out.toString());
            out.close();
        }
        catch (IOException e)
        {


        }
    }

    private void writeAnimToXml(XmlWriter aXmlWriter, Animation aAnimation) throws IOException
    {
         aXmlWriter.writeEntity("anim");
         aXmlWriter.writeAttribute("name", aAnimation.getName());
         aXmlWriter.writeAttribute("loops", aAnimation.getLoops());

         int backupIndex = aAnimation.getCurrentCellIndex();
         aAnimation.setCurrentCellIndex(0);
         AnimationCell cell = aAnimation.getCurrentCell();
         while(cell !=  null)
         {
            aXmlWriter.writeEntity("cell");
            aXmlWriter.writeAttribute("index", aAnimation.getCurrentCellIndex());
            aXmlWriter.writeAttribute("delay", cell.getDelay());

            ArrayList<GraphicObject> graphicList = cell.getGraphicList();
            for (int i=0; i<graphicList.size(); ++i)
            {
                SpriteGraphic graphic = (SpriteGraphic)graphicList.get(i);
                CustomNode node = cell.nodeForGraphic(graphic);
                aXmlWriter.writeEntity("spr");
                aXmlWriter.writeAttribute("name", node.getFullPathName());
                aXmlWriter.writeAttribute("x", graphic.getRect().x + graphic.getRect().width/2);
                aXmlWriter.writeAttribute("y", graphic.getRect().y + graphic.getRect().height/2);                                
                aXmlWriter.writeAttribute("z", cell.zOrderOfGraphic(graphic));                                
                
                if (graphic.getAngle() != 0)
                    aXmlWriter.writeAttribute("angle", graphic.getAngle());
                if (graphic.getScale() != 100)
                    aXmlWriter.writeAttribute("scale", graphic.getScale());
                if (graphic.getOpacity() != 255)
                    aXmlWriter.writeAttribute("opacity", graphic.getOpacity());
                if (graphic.getId().length() > 0)
                    aXmlWriter.writeAttribute("id", graphic.getId());
                if (graphic.isFlippedV())
                    aXmlWriter.writeAttribute("flipV", graphic.isFlippedV() ? 1 : 0);
                if (graphic.isFlippedH())
                    aXmlWriter.writeAttribute("flipH", graphic.isFlippedH() ? 1 : 0);
                
                aXmlWriter.endEntity();
            }

            aXmlWriter.endEntity();

            cell = aAnimation.getNextCell();
         }

         aXmlWriter.endEntity();
    }

    

}
