package dfEditor.command;

/**
 *
 * @author s4m20
 */
public abstract class UndoableCommand extends Command
{
    public abstract void undo();
}
