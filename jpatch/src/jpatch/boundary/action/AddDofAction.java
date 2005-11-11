package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;

import jpatch.entity.*;
import jpatch.control.edit.*;

public final class AddDofAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	private Bone bone;
	
	public AddDofAction(Bone bone) {
		super("add DOF");
		this.bone = bone;
		//putValue(Action.SHORT_DESCRIPTION,"Add Bone");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		RotationDof dof = new RotationDof(bone);
		dof.addTarget(new MorphTarget(dof.getMin()));
		dof.addTarget(new MorphTarget(0));
		dof.addTarget(new MorphTarget(dof.getMax()));
		MainFrame.getInstance().getUndoManager().addEdit(new AtomicAddDof(dof, bone));
		MainFrame.getInstance().selectTreeNode(dof);
//		MainFrame.getInstance().getTree().setSelectionPath(dof.getTreePath());
	}
}

