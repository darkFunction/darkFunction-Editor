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

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import java.io.File;
import javax.swing.UIManager;
import java.awt.Toolkit;
import de.muntjak.tinylookandfeel.*;


public class dfEditorApp extends SingleFrameApplication
{
    private dfEditorView sv;
    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup()
    {          
        final String userDir = dfEditorApp.getUserDataDirectory();
        final dfEditorApp self = this;
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {                                
                Toolkit.getDefaultToolkit().setDynamicLayout(true);
                System.setProperty("sun.awt.noerasebackground", "true");
                
                try {
                    TinyLookAndFeel tiny = new TinyLookAndFeel();
                    UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
                } catch(Exception ex) {
                    ex.printStackTrace();
                }           
                
                self.showMainView();
            }
        });
    }
    
    private void showMainView()
    {
        sv = new dfEditorView(this);
        this.addExitListener(sv);
        show(sv);
    }
    
    private static String getUserDataDirectory()
    {
        String dir = System.getProperty("user.home") + File.separator + ".dfEditor" + File.separator;
        File test = new File(dir);
        if (!test.exists())
            test.mkdir();
        return dir;
    }

    
    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of dfEditorApp
     */
    public static dfEditorApp getApplication() {
        return Application.getInstance(dfEditorApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(dfEditorApp.class, args);
    }

    @Override
    public void shutdown()
    {        
        super.shutdown();
    }
}
