package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.sidebar.*;
import jpatch.control.edit.*;
import jpatch.entity.*;

public final class EditMorphAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MorphTarget morph;
	private MorphPanel morphPanel;
	
	public EditMorphAction(MorphTarget morph, MorphPanel morphPanel) {
		super("Edit");
		this.morph = morph;
		this.morphPanel = morphPanel;
	}
	
	public EditMorphAction(MorphTarget morph, String name) {
		super(name);
		this.morph = morph;
		this.morphPanel = null;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
//		int mode = MainFrame.getInstance().getMode();
		if (MainFrame.getInstance().getEditedMorph() == null) {
			/* turn on edit morph mode */
			//MainFrame.getInstance().switchMode(MainFrame.MORPH);
			MainFrame.getInstance().setEditedMorph(morph);
			morph.unapply();
			morph.prepare();
			morph.setValue(1);
			morph.apply();
			//MainFrame.getInstance().getModel().applyMorphs();
			//morph.setValue(1);
			MainFrame.getInstance().getUndoManager().setStop();	//set stop marker for undoes
			
			//MainFrame.getInstance().getUndoManager().setEnabled(false);	// disables undo manager for now
			if (morphPanel != null)
				morphPanel.editMorph();
		} else {
			/* turn off edit morph mode */
			MainFrame.getInstance().setEditedMorph(null);
			//MainFrame.getInstance().getUndoManager().clearStop();	//clear stop marker for undoes
			MainFrame.getInstance().getUndoManager().rewind();
			MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeMorph(morph));
			//morph.set();
			if (morphPanel != null)
				morphPanel.editMorphDone();
			
			//MainFrame.getInstance().getUndoManager().setEnabled(true);	// re-enables undo manager
		}
	}
}

