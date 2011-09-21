/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dfEditor;

import dfEditor.command.CommandManager;

/**
 *
 * @author s4m20
 */
public interface CommandManagerListener
{
    public void commandStackChanged(CommandManager aManager);

}
