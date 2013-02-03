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
