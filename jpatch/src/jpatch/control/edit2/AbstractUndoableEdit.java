package jpatch.control.edit2;


public abstract class AbstractUndoableEdit implements JPatchUndoableEdit {
	protected boolean applied;
	
	public void undo() {
		assert applied : "undo attempted on unapplied edit " + this;
		applied = false;
	}
	
	public void redo() {
		assert !applied : "redo attempted on already applied edit " + this;
		applied = true;
	}
}