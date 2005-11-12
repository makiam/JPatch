package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.vecmath.*;

import jpatch.boundary.*;
import jpatch.entity.*;
import jpatch.control.edit.*;

public final class DeleteMorphTargetAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MorphTarget morphTarget;
	
	public DeleteMorphTargetAction(MorphTarget morphTarget) {
		super("Delete target");
		this.morphTarget = morphTarget;
	}
	public void actionPerformed(ActionEvent actionEvent) {
		Morph morph = morphTarget.getMorph();
		if (morph.getChildCount() > 1) {
			MainFrame.getInstance().getUndoManager().addEdit(new AtomicMorphTarget.Remove(morph, morphTarget));
//			if (morph.getValue() < morph.getMin())
//				morph.setValue(morph.getMin());
//			else if (morph.getValue() > morph.getMax())
//				morph.setValue(morph.getMax());
			MainFrame.getInstance().getModel().applyMorphs();
			MainFrame.getInstance().getModel().setMorphPose();
		}
	}
}

