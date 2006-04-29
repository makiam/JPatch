package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;

import jpatch.entity.*;
import jpatch.boundary.*;

public final class EditAnimObjectAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private AnimObject animObject;
	
	public EditAnimObjectAction(AnimObject animObject) {
		super("edit");
		this.animObject = animObject;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
//		new AnimObjectEditor(animObject, new BFrame());
	}
}