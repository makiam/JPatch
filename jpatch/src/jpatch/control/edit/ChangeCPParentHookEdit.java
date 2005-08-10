package jpatch.control.edit;

import jpatch.entity.*;

public class ChangeCPParentHookEdit extends JPatchAbstractUndoableEdit {
	
	private ControlPoint cp;
	private ControlPoint cpParentHook;
	
	public ChangeCPParentHookEdit(ControlPoint cp, ControlPoint parentHook) {
		this.cp = cp;
		cpParentHook = parentHook;
		swap();
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		ControlPoint cpDummy = cpParentHook;
		cpParentHook = cp.getParentHook();
		cp.setParentHook(cpDummy);
	}
}
