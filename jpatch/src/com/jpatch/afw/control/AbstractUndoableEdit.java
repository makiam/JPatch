package com.jpatch.afw.control;

/**
 * Baseclass for all UndoableEdits.
 * Automatically checks state (whether the edit has been applied or not).
 * Initially edits are in the "unapplied" state. Subclasses should, after construction
 * was finished, call the apply(boolean apply) method. Calling this method with an argument "false"
 * will simply set the state to "applied", calling it with an argument "true" will call the edit's
 * performRedo() method before setting the state to "applied".
 * 
 * Provides final performUndo() and performRedo() methods, which will do the state-checking and then
 * call undo() or redo() respectively, which have to be overridden in subclasses.
 * @author sascha
 *
 */
public abstract class AbstractUndoableEdit implements JPatchUndoableEdit {
	private boolean applied;
	
	/**
	 * @param apply <b>true</b> will call redo(), <b>false</b> will only set the state to "applied".
	 */
	protected final void apply(boolean apply) {
		if (apply) {
			performRedo();
		} else {
			applied = true;
		}
	}
	
	/**
	 * Checks whether state is "applied" and sets it to "unapplied", calls undo().
	 * @throws IllegalStateException if state was "unapplied" when this method has been called
	 */
	public final void performUndo() {
//		System.out.println("undo " + this);
		if (!applied) {
			throw new IllegalStateException("undo attempted on unapplied edit " + this);
		}
		undo();
		applied = false;
	}
	
	/**
	 * Checks whether state is "unapplied" and sets it to "applied", calls redo().
	 * @throws IllegalStateException if state was "applied" when this method has been called
	 */
	public final void performRedo() {
//		System.out.println("redo " + this);
		if (applied) {
			throw new IllegalStateException("redo attempted on already applied edit " + this);
		}
		redo();
		applied = true;
	}
	
	public abstract void undo();
	
	public abstract void redo();
}