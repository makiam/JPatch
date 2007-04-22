package com.jpatch.afw.control;

public abstract class EditAttribute extends AbstractUndoableEdit {
	@Override
	public final void undo() {
		super.undo();
		toggle();
	}
	
	@Override
	public final void redo() {
		super.redo();
		toggle();
	}
	
	protected abstract void toggle();
}
