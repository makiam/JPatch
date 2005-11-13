package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;

import jpatch.entity.*;
import jpatch.control.edit.*;

public final class UseDofMorphAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	private RotationDof dof;
	private boolean bEnable;
	
	public UseDofMorphAction(RotationDof dof, boolean enable) {
		super(enable ? "Use morph" : "Discard morph");
		this.dof = dof;
		bEnable = enable;
		//putValue(Action.SHORT_DESCRIPTION,"Add Bone");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		if (bEnable && dof.getChildCount() == 0) {
			JPatchActionEdit edit = new JPatchActionEdit("use dof morph");
			if (dof.getMin() < 0)
				edit.addEdit(new AtomicMorphTarget.Add(dof, new MorphTarget(dof.getMin())));
			edit.addEdit(new AtomicMorphTarget.Add(dof, new MorphTarget(0)));
			if (dof.getMax() > 0)
				edit.addEdit(new AtomicMorphTarget.Add(dof, new MorphTarget(dof.getMax())));
			MainFrame.getInstance().getUndoManager().addEdit(edit);
		} else if (!bEnable && dof.getChildCount() > 0) {
			JPatchActionEdit edit = new JPatchActionEdit("discard dof morph");
			for (Iterator it = new ArrayList(dof.getTargets()).iterator(); it.hasNext(); ) {
				edit.addEdit(new AtomicMorphTarget.Remove(dof, (MorphTarget) it.next()));
			}
			MainFrame.getInstance().getUndoManager().addEdit(edit);
			MainFrame.getInstance().getModel().applyMorphs();
			MainFrame.getInstance().getModel().setPose();
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}

