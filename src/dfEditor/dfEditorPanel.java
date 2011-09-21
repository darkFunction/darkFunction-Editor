/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dfEditor;


import dfEditor.command.CommandManager;
import javax.swing.*;
import java.io.File;


/**
 *
 * @author s4m20
 */
public abstract class dfEditorPanel extends javax.swing.JPanel implements dfEditorTask, CommandManagerListener
{
    protected CommandManager cmdManager = null;
    protected JLabel helpLabel = null;
    protected TaskChangeListener taskChangeListener = null;
    protected boolean bModified = false;
    protected File savedFile = null;
    protected JFileChooser fileChooser = null;

    public dfEditorPanel(CommandManager aCmdManager, JLabel aHelpLabel, TaskChangeListener aListener, JFileChooser aChooser)
    {
        this.cmdManager = aCmdManager;
        this.helpLabel = aHelpLabel;
        this.taskChangeListener = aListener;
        this.fileChooser = aChooser;        

        aCmdManager.setListener(this);         
    }

    public void postInit()
    {
          
    }
    
    public abstract boolean saveAs();
    public abstract boolean save();
    
    @Override
    public String getName()            
    {
        return super.getName();
    }
    
    public File getSavedFile()
    {       
        return savedFile;
    }

    public void setSavedFile(File aFile)
    {
        savedFile = aFile;
        this.setName(aFile.getName());
    }
    
    public void refreshCommandManagerButtons()
    {
        cmdManager.refreshComponents();
    }

    public void undo()
    {
        cmdManager.undo();
    }

    public void redo()
    {
        cmdManager.redo();
    }

    protected void setModified(boolean aModified)
    {        
        bModified = aModified;
    }

    public boolean hasBeenModified()
    {
        return bModified;
    }

    public void commandStackChanged(CommandManager aCmdManager)
    {
        this.setModified(aCmdManager.sizeOfUndoStack() > 0);
        taskChangeListener.taskChanged(this);
    }

}
