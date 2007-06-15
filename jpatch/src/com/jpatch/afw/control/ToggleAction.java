package com.jpatch.afw.control;

import java.awt.event.ActionEvent;
import com.jpatch.afw.attributes.Toggle;
import javax.swing.Icon;

public class ToggleAction extends JPatchAction {
	protected final Toggle toggle;
	
	public ToggleAction(Toggle toggle, JPatchUndoManager undoManager, String name) {
		super(undoManager, name);
		this.toggle = toggle;
	}
	
	public ToggleAction(Toggle toggle, JPatchUndoManager undoManager, String name, String text) {
		super(undoManager, name, text);
		this.toggle = toggle;
	}
	
	public ToggleAction(Toggle toggle, JPatchUndoManager undoManager, String name, String text, Icon icon) {
		super(undoManager, name, text, icon, false);
		this.toggle = toggle;
	}

	public void actionPerformed(ActionEvent event) {
		JPatchUndoableEdit edit = new EditToggle(toggle, true);
		if (undoManager != null) {
			undoManager.addEdit(displayName, edit);
		}
	}
	
	public Toggle getToggle() {
		return toggle;
	}
	
	public boolean isSelected() {
		return toggle.getBoolean();
	}
}
