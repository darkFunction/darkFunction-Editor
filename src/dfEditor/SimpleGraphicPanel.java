/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
