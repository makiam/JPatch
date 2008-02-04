package com.jpatch.afw.control;

public abstract class AbstractSwapEdit extends AbstractUndoableEdit {

	protected AbstractSwapEdit() { }
	
	@Override
	public void redo() {
		super.redo();
		swap();
	}

	@Override
	public void undo() {
		super.undo();
		swap();
	}
	
	protected abstract void swap();

}
