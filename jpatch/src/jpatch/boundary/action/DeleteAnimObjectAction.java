package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.control.edit.AtomicAddRemoveAnimObject;
import jpatch.control.edit.JPatchRootEdit;

public final class DeleteAnimObjectAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private AnimObject animObject;
	
	public DeleteAnimObjectAction(AnimObject animObject) {
		super("delete");
		this.animObject = animObject;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		JPatchRootEdit edit = new AtomicAddRemoveAnimObject(animObject, true);
		MainFrame.getInstance().getUndoManager().addEdit(edit);
	}
}


