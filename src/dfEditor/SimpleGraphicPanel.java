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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 *
 * @author Sam
 */
public class SimpleGraphicPanel extends javax.swing.JPanel
{
    private GraphicObject graphic = null;

    public SimpleGraphicPanel()         
    {
        //this.setBackground(Color.red);
    }

    public void setGraphic(GraphicObject aGraphic)
    {
        graphic = aGraphic;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (graphic != null)
        {
            Point p = new Point((this.getSize().width / 2)-(graphic.getRect().width/2),
                                (this.getSize().height / 2)-(graphic.getRect().height/2));
            graphic.draw(g, p, 1.0f, 1.0f, false);
        }
    }

}
