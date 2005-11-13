package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.control.edit.*;
import jpatch.entity.*;

public final class DeleteDofAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RotationDof dof;
	
	public DeleteDofAction(RotationDof dof) {
		super("Delete DOF");
		this.dof = dof;
		//putValue(Action.SHORT_DESCRIPTION,"Add Controlpoint [A]");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		//MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		//MainFrame.getInstance().getJPatchScreen().addMouseListeners(new AddControlPointMouseAdapter());
		//MainFrame.getInstance().clearDialog();
		
		MainFrame.getInstance().getUndoManager().addEdit(new AtomicDeleteDof(dof));
		MainFrame.getInstance().getJPatchScreen().update_all();
		
	}
}

