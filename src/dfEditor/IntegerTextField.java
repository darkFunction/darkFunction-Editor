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

/**
 *
 * @author s4m20
 */
import java.awt.event.KeyEvent;
import javax.swing.JTextField;


public class IntegerTextField extends JTextField {

    final static String badchars
       = "`~!@#$%^&*()_+=\\|\"':;?/>.<, ";

    public void processKeyEvent(KeyEvent ev) {

        char c = ev.getKeyChar();

        if((Character.isLetter(c) && !ev.isAltDown())
           || badchars.indexOf(c) > -1) {
            ev.consume();
            return;
        }
        if(c == '-' && getDocument().getLength() > 0)
            ev.consume();
        else super.processKeyEvent(ev);

    }
    public int getNum()
    {
        try
        {
            return Integer.parseInt(this.getText());
        }
        catch (java.lang.NumberFormatException e)
        {
            return -1;
        }
    }
}
