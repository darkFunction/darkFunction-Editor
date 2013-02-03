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

import com.DeskMetrics.DeskMetrics;
//import com.apple.eawt.QuitStrategy;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import javax.swing.UIManager;
import java.awt.Toolkit;
import javax.swing.JDialog;
import java.awt.event.*;
import de.muntjak.tinylookandfeel.*;
import javax.swing.JOptionPane;
//import org.jvnet.substance.skin.SubstanceNebulaBrickWallLookAndFeel  ;

/**
 * The main class of the application.
 */
public class dfEditorApp extends SingleFrameApplication
{
    public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));
    public static boolean isFreeVersion = false;

    private static String regKey = null;
    private static long daysUsed = 0;
    private dfEditorView sv;
    private static boolean deskMetricsStarted = false;

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
                //JFrame.setDefaultLookAndFeelDecorated(true);
                //JDialog.setDefaultLookAndFeelDecorated(true);
                
                try {
                    TinyLookAndFeel tiny = new TinyLookAndFeel();
                  //  tiny.setCurrentTheme(de.muntjak.tinylookandfeel.Theme. )
                    UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                /*
                try {
                    javax.swing.UIManager.setLookAndFeel(javax.swing.plaf.UIResour
                } catch (javax.swing.UnsupportedLookAndFeelException e) {}
                */
                
                try
                {
                    String licenseKey = contentsOfFileWithPath(userDir + "license.lic");
                    self.setRegKey(licenseKey);
                }
                catch(java.io.IOException e)
                {

                }
                
                String filePath = userDir + ".conf";
                File file = new File(filePath);
                if (file.exists())
                {
                    Long lastModified = file.lastModified();
                    Date dateInstalled = new Date(lastModified);

                    Date dateNow = new Date(System.currentTimeMillis());

                    final long oneDay = 24 * 60 * 60 * 1000;

                    daysUsed = (dateNow.getTime() / oneDay) - (dateInstalled.getTime() / oneDay);
                }
                else
                {
                    daysUsed = 0;
                    
                    try {
                        FileOutputStream output = new FileOutputStream(filePath);
                        // just write any old bullshit to the file really it doesn't matter
                        Date dateNow = new Date(System.currentTimeMillis());
                        output.write(dateNow.toString().getBytes());
                        output.close();
                    } catch (IOException e) {
                        
                    }
                }
                
                
                try {
                    DeskMetrics.getInstance().start("4f70add4a14ad70c41000000", "1.3"); 
                    deskMetricsStarted = true;
                } catch (IOException e) {  }
                
                // so we get the window closed event on OSX and can stop DeskMetrics *BREAKS OTHER PLATFORMS NEED TO USE REFLECTION, SEE BELOW*
                //com.apple.eawt.Application.getApplication().setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
                registerForMacOSXEvents();
                
                if (!self.isRegistered()) 
                {
                    JDialog aboutBox = null;
                    if (self.isFreeVersion)
                        aboutBox = new dfEditorAboutBoxFree(self.getMainFrame());
                    else
                        aboutBox = new dfEditorAboutBox(self.getMainFrame());

                    aboutBox.setLocationRelativeTo(self.getMainFrame());
                    
                    show(aboutBox);
                    
                    aboutBox.addWindowListener(new WindowAdapter() {
                        public void windowClosed(WindowEvent e) {
                            dfEditorAboutBox aboutBox = (dfEditorAboutBox) e.getSource();
                            if (aboutBox.continueButtonClicked)
                                self.showMainView();
                            else
                                self.exit();
                        }
                    });
                } 
                else 
                {
                    self.showMainView();
                }
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
    
    public void registerForMacOSXEvents() {
        if (MAC_OS_X) {
            try {
                // Generate and register the OSXAdapter, passing it a hash of all the methods we wish to
                // use as delegates for various com.apple.eawt.ApplicationListener methods
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quitMacOSX", (Class[])null));    
            } catch (Exception e) {
                System.err.println("Error while loading the OSXAdapter:");
                e.printStackTrace();
            }
        }
    }
    
    public void quitMacOSX()
    {
        closeDeskMetrics();
    }
    
    public static void closeDeskMetrics()
    {
        if (!deskMetricsStarted)
            return;
        
        deskMetricsStarted = false;
        
        try 
        {
            DeskMetrics.getInstance().stop();
        } catch (java.io.IOException ex) {
            System.out.println("IOException closing DeskMetrics...");
        }
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
        dfEditorApp.closeDeskMetrics();
        super.shutdown();
    }
    
    private String contentsOfFileWithPath(String path) throws IOException
    {
        FileInputStream input = new FileInputStream(path);

        StringBuffer strContent = new StringBuffer();
        
        int ch;
        while( (ch = input.read()) != -1)
            strContent.append((char)ch);
        input.close();
        
        return strContent.toString();
    }

    public boolean setRegKey(String key)
    {
        if (dfEditor.license.LicenseReader.checkLicense(key))
        {
            regKey = key;

            if (sv != null)
                sv.checkRegistered();
            
            try
            {
                FileOutputStream output = new FileOutputStream(getUserDataDirectory() + "license.lic");
                output.write(key.getBytes());
                output.close();
            }
            catch (Exception e)
            {
            }
            
            return true;
        }
        return false;
    }

    public String getRegKey()
    {
        return regKey;
    }

    public long getDaysUsed()
    {
        return daysUsed;
    }

    public int getDaysRemaining()
    {
        int rem = 30 - (int)this.getDaysUsed();
        if (rem < 0)
            rem = 0;
        return rem;
    }

    public boolean isRegistered()
    {
        if (isFreeVersion)
            return true;

        return (regKey != null);
    }
}
