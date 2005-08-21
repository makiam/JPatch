/*
 * $Id: LatheEditorAction.java,v 1.2 2005/08/21 15:32:08 lois Exp $
 */
package jpatch.boundary.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import jpatch.boundary.KeyMapping;
import jpatch.boundary.MainFrame;
import jpatch.boundary.dialog.LatheEditorDialog;

/**
 * 
 * @author lois
 * @version $Revision: 1.2 $
 *
 */
public final class LatheEditorAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5180146108807944799L;
	public LatheEditorAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/sphere.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("add sphere"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getUndoManager().addEdit((new LatheEditorDialog(MainFrame.getInstance())).getEdit());
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}

