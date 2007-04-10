package jpatch.control;

import jpatch.control.edit2.AbstractUndoableEdit;

public abstract class SwapperEdit extends AbstractUndoableEdit {
	
	@Override
	public void undo() {
		super.undo();
		swap();
	}

	@Override
	public void redo() {
		super.redo();
		swap();
	}
	
	protected abstract void swap();
}
