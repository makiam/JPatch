package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;

import jpatch.boundary.*;
import jpatch.boundary.sidebar.*;
import jpatch.control.edit.*;
import jpatch.entity.*;

public final class StopEditMorphAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public StopEditMorphAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/stop.png")));
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		MorphTarget morph = MainFrame.getInstance().getEditedMorph();
		if (morph == null)
			throw new IllegalStateException("StopEditMorphAction called when no morph was edited");
		MainFrame.getInstance().setEditedMorph(null);
		MainFrame.getInstance().getUndoManager().rewind();
		MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeMorph(morph));
	}
}

