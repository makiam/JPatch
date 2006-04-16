/*
 * $Id$
 */
package jpatch.boundary.action;

import java.awt.event.ActionEvent;

import javax.swing.*;

import jpatch.boundary.*;
import jpatch.boundary.dialog.*;
/**
 * 
 * @author lois
 * @version $Revision$
 *
 */
public final class LatheEditorAction extends AbstractAction {
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getUndoManager().addEdit((new LatheEditorDialog(MainFrame.getInstance())).getEdit());
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}

