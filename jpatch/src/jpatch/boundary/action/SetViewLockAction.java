package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import javax.vecmath.*;
import jpatch.boundary.*;


public final class SetViewLockAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean lock;
	
	public SetViewLockAction(boolean lock) {
		super(lock ? "lock view to selection" : "unlock view");
		this.lock = lock;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition();
		Selection selection = MainFrame.getInstance().getSelection();
		if (lock && selection != null) {
			viewDef.setLock(new Point3f(selection.getPivot()));
		} else {
			viewDef.setLock(null);
		}
		Command.setViewDefinition(viewDef);
		viewDef.repaint();
		//((JPatchCanvas)viewDefinition.getViewport()).updateImage();
		//viewDefinition.reset();
	}
}

