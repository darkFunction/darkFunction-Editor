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

import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;

/**
 *
 * @author s4m20
 */
public class TabComponent extends JPanel
{
    private JTabbedPane tabbedPane;
    
    public TabComponent(JTabbedPane aPane)
    {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        tabbedPane = aPane;

        JLabel title = new JLabel()
        {
            public String getText()
            {
                int index = tabbedPane.indexOfTabComponent(TabComponent.this);
                if (index >= 0)
                    return tabbedPane.getTitleAt(index);
                return null;                 
            }
        };
        add(title);

        CloseButton closeButton = new CloseButton();
        add(closeButton);

        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        setOpaque(false);

    }

    private class CloseButton extends JButton implements ActionListener
    {
        public CloseButton()
        {
            super();

            setPreferredSize(new Dimension(15, 15));
            addActionListener(this);
            setRolloverEnabled(true);
        }

        @Override
        public void paintComponent(Graphics g)
        {
            //super.paintComponent(g);

            Dimension size = this.getSize();
           
            if (getModel().isRollover())
            {                
                g.setColor(Color.RED);
            }
            else
            {
                g.setColor(Color.BLACK);
            }

            if (getModel().isPressed())
            {
                g.translate(1,0);
            }

            int space = Math.min(size.width, size.height)/3;
            g.drawLine(space, space, size.width - space, size.height - space);
            g.drawLine(size.width - space, space, space, size.height - space);

            g.drawLine(space + 1, space, size.width - space, size.height - space);
            g.drawLine(size.width - space, space-1, space, size.height - space);
        }

        public void actionPerformed(ActionEvent e)
        {
            int index = tabbedPane.indexOfTabComponent(TabComponent.this);
            if (index < 0)
                return;
            
            dfEditorTask currentTask = ((dfEditorTask)tabbedPane.getComponentAt((index)));

            if (!currentTask.hasBeenModified())
                tabbedPane.remove(index);
            else
            {
                String[] choices = {" Save ", " Discard ", " Cancel "};

                int choice = JOptionPane.showOptionDialog(
                                   tabbedPane                   // Center in window.
                                 , "Save changes?"              // Message
                                 , "Closing tab"                  // Title in titlebar
                                 , JOptionPane.YES_NO_OPTION    // Option type
                                 , JOptionPane.WARNING_MESSAGE    // messageType
                                 , null                         // Icon (none)
                                 , choices                      // Button text as above.
                                 , " Save "      // Default button's label
                               );



                switch(choice)
                {
                    case 0:
                        // save
                        if (currentTask.save())
                            tabbedPane.remove(index);
                        break;
                    case 1:
                        tabbedPane.remove(index);
                        break;
                    case 2:
                        // cancel
                        break;
                }
            }       
                 
        }
    }

}
