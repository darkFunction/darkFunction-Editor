package dfEditor;
import java.io.File;

/**
 *
 * @author s4m20
 */
public interface dfEditorTask
{
    public boolean save();
    public boolean saveAs();
    public boolean hasBeenModified();
    public void refreshCommandManagerButtons();
    public void undo();
    public void redo();
    public File getSavedFile();
    public void setSavedFile(File aFile);
    public String getName();
}
