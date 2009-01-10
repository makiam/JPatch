package com.jpatch.afw.control;


public abstract class AbstractUndoableEdit implements JPatchUndoableEdit {
	private boolean applied;
	
	protected final void apply(boolean apply) {
		if (apply) {
			redo();
		} else {
			applied = true;
		}
	}
	
	public void undo() {
//		System.out.println("undo " + this);
		if (!applied) {
			throw new IllegalStateException("undo attempted on unapplied edit " + this);
		}
		applied = false;
	}
	
	public void redo() {
//		System.out.println("redo " + this);
		if (applied) {
			throw new IllegalStateException("redo attempted on already applied edit " + this);
		}
		applied = true;
	}
}