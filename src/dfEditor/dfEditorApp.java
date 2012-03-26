/*
 * dfEditorApp.java
 */

package dfEditor;

import com.DeskMetrics.DeskMetrics;
import com.apple.eawt.QuitStrategy;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.UIManager;
import java.awt.Toolkit;

import de.muntjak.tinylookandfeel.*;
//import org.jvnet.substance.skin.SubstanceNebulaBrickWallLookAndFeel  ;

/**
 * The main class of the application.
 */
public class dfEditorApp extends SingleFrameApplication
{
    public static boolean isFreeVersion = false;

    private static String regKey = null;
    private static long daysUsed = 0;
    private dfEditorView sv;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup()
    {          
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
                StringBuffer strContent = new StringBuffer();

                try
                {
                    FileInputStream input = new FileInputStream("./license.lic");

                    int ch;
                    while( (ch = input.read()) != -1)
                        strContent.append((char)ch);
                    input.close();

                    self.setRegKey(strContent.toString());
                }
                catch(java.io.IOException e)
                {

                }
                
                // Create an instance of file object.
                File file = new File("./dfEditor.jar");
                if (file.exists())
                {
                    Long lastModified = file.lastModified();
                    Date dateInstalled = new Date(lastModified);

                    Date dateNow = new Date(System.currentTimeMillis());

                    final long oneDay = 24 * 60 * 60 * 1000;

                    daysUsed = (dateNow.getTime() / oneDay) - (dateInstalled.getTime() / oneDay);
                }
                else
                    daysUsed = -1;
                
                final DeskMetrics deskmetrics = DeskMetrics.getInstance();
                String appID = "4f70add4a14ad70c41000000";
                
                try {
                    deskmetrics.start(appID, "1.3");                    
                    deskmetrics.trackCustomData("Days since install", ""+daysUsed);
                } catch (IOException e) {  }
                
                // so we get the window closed event on OSX and can stop DeskMetrics
                com.apple.eawt.Application.getApplication().setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
                
                sv = new dfEditorView(self);
                self.addExitListener(sv);
                show(sv);
            }
        });
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

    public boolean setRegKey(String key)
    {
        if (dfEditor.license.LicenseReader.checkLicense(key))
        {
            regKey = key;

            if (sv != null)
                sv.checkRegistered();
            
            try
            {
                FileOutputStream output = new FileOutputStream( "./license.lic");
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
