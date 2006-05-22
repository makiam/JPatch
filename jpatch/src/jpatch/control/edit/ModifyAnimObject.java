package jpatch.control.edit;

import jpatch.boundary.*;
import jpatch.entity.*;

public class ModifyAnimObject extends JPatchCompoundEdit implements JPatchRootEdit {
	AnimObject animObject;
	
	private ModifyAnimObject() { }
	
	public ModifyAnimObject(AnimObject animObject) {
		this.animObject = animObject;
	}
	
	public void addEdit(JPatchUndoableEdit edit) {
		super.addEdit(edit);
	}
	
	public void undo() {
		super.undo();
		MainFrame.getInstance().getAnimation().getCurvesetFor(animObject).setPosition(MainFrame.getInstance().getAnimation().getPosition());
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
	
	public void redo() {
		super.redo();
		MainFrame.getInstance().getAnimation().getCurvesetFor(animObject).setPosition(MainFrame.getInstance().getAnimation().getPosition());
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
	
	public int sizeOf() {
		return super.sizeOf() + 4;
	}

	public String getName() {
		return "modify " + animObject.getName();
	}
}
