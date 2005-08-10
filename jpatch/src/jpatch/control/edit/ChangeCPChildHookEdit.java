package jpatch.control.edit;

import jpatch.entity.*;

public class ChangeCPChildHookEdit extends JPatchAbstractUndoableEdit {
	
	private ControlPoint cp;
	private ControlPoint cpChildHook;
	
	public ChangeCPChildHookEdit(ControlPoint cp) {
		this.cp = cp;
		cpChildHook = null;
	}
	
	public ChangeCPChildHookEdit(ControlPoint cp, ControlPoint childHook) {
		this.cp = cp;
		cpChildHook = childHook;
		swap();
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		ControlPoint cpDummy = cpChildHook;
		cpChildHook = cp.getChildHook();
		cp.setChildHook(cpDummy);
	}
}
