/*
 * $Id: LatheEditorAction.java,v 1.3 2006/04/16 13:45:02 sascha_l Exp $
 */
package jpatch.boundary.action;

import java.awt.event.ActionEvent;

import javax.swing.*;

import jpatch.boundary.*;
import jpatch.boundary.dialog.*;
/**
 * 
 * @author lois
 * @version $Revision: 1.3 $
 *
 */
public final class LatheEditorAction extends AbstractAction {
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getUndoManager().addEdit((new LatheEditorDialog(MainFrame.getInstance())).getEdit());
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}

