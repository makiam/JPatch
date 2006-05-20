package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;

import jpatch.boundary.*;
import jpatch.boundary.sidebar.*;
import jpatch.entity.*;
import jpatch.control.edit.*;

@SuppressWarnings("serial")
public final class NewMorphAction extends AbstractAction {
	
	private static int iNum = 1;
	
	public NewMorphAction() {
		super("New Morph");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		Morph morph = new Morph("new morph #" + iNum++, MainFrame.getInstance().getModel());
		morph.addTarget(new MorphTarget(0));
		MorphTarget target = new MorphTarget(morph.getMax());
		morph.addTarget(target);
		
		MainFrame.getInstance().getUndoManager().addEdit(new AtomicAddMorph(morph));
		MainFrame.getInstance().selectTreeNode(target);
		MainFrame.getInstance().setEditedMorph(target);
		((MorphTargetPanel) MainFrame.getInstance().getSideBar().getSidePanel()).getEditButton().setSelected(true);
	}
}

