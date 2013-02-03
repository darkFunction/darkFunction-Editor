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

import dfEditor.*;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;

/**
 *
 * @author s4m20
 */
public class SelectionBox extends GraphicObject
{
    private Color _colInner;
    private Color _colOuter;
    private Color _colSelected;

//    private float _alpha;
//    private SpritesheetPanel _panel;

    public SelectionBox(/*SpritesheetPanel aPanel,*/ Rectangle aRect, Color aColour)
    {
        super(aRect);

        _bResizable = true;
        setBounds(_bounds);

        setColour(aColour);

        _colSelected = new Color(255,255,255,128);
        
//        _panel = aPanel;
//        _alpha = 0.0f;
//        this.fadeIn();
    }

    public void draw(Graphics g, Point aOffset, float aScale, boolean bDrawSelectedMode)
    {       
        Rectangle r = getRect();

        // bit of a hack to minimise jittering when resizing a box ////////////////
        // requires further investigation but this is seriously pissing me off
        int xmod = 0;
        int ymod = 0;
        if ((int)Math.round(r.width * aScale) != (int)(r.width * aScale)) // rounded up width
        {
            if ((int)Math.round(r.x * aScale) == (int)(r.x * aScale)) // didn't round up x
            {
                xmod = -1;
            }
        }
        if ((int)Math.round(r.height * aScale) != (int)(r.height * aScale)) // rounded up height
        {
            if ((int)Math.round(r.y * aScale) == (int)(r.y * aScale)) // didn't round up y
            {
                ymod = -1;
            }
        }
        ///////////////////////////////////////////////////////////////////////////

        Rectangle drawRect = new Rectangle(
                aOffset.x + xmod + ((int)(r.x *  aScale)),
                aOffset.y + ymod + ((int)(r.y *  aScale)),
                (int)Math.round(r.width * aScale),
                (int)Math.round(r.height * aScale));
       
        if (isSelected() && bDrawSelectedMode)
        {
            g.setColor(_colSelected);
        }
        else
            g.setColor(_colInner);        
        
        g.fillRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
        g.setColor(_colOuter);
        g.drawRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
        
    }

    public void setColour(Color aColour)
    {
        _colOuter = aColour;
        _colInner = new Color(
                aColour.getRed(),
                aColour.getGreen(),
                aColour.getBlue(),
                aColour.getAlpha() / 2);
    }

    public GraphicObject copy()
    {
        // TODO:
        return null;
    }


//    public void fadeIn()
//    {
//        Fader fader = new Fader();
//        fader.fade(this, 0.05f);
//        fader.start();
//    }
//
//    public void fadeOut()
//    {
//        Fader fader = new Fader();
//        fader.fade(this, -0.05f);
//        fader.start();
//    }
//
//    private class Fader extends Thread
//    {
//        private float mod;
//        private SelectionBox graphic;
//
//        public void fade(final SelectionBox aGraphic, float aMod)
//        {
//            this.graphic = aGraphic;
//            this.mod = aMod;
//        }
//
//        public void run()
//        {
//            while (true)
//            {
//                try
//                {
//                    sleep(1000 / 30);
//                    boolean bStop = false;
//                    graphic._alpha += mod;
//                    if (graphic._alpha > 1.0f)
//                    {
//                        graphic._alpha = 1.0f;
//                        bStop = true;
//                    }
//                    else if (graphic._alpha < 0)
//                    {
//                        graphic._alpha = 0;
//                        bStop = true;
//                    }
//
//                    graphic._panel.repaint();
//                    if (bStop)
//                        return;
//                }
//                catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

}
