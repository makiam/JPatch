package com.jpatch.afw.control;

/**
 * This is the baseclass for all edits that merely swap the contents of two (or more) variables.<br>
 * <b>Example:</b> An edit that changes the material of a face would store the
 * new material, and - when redo() is called - swap the contents of that
 * variable with the contents of the variable
 * that hold the face's current material. Since undo() does exactly the same,
 * this class provided final undo() and redo() method that both simply call
 * the swap() method, which must be implemented by subclasses.
 * 
 * @author sascha
 *
 */
public abstract class AbstractSwapEdit extends AbstractUndoableEdit {

	protected AbstractSwapEdit() { }
	
	@Override
	public final void redo() {
		swap();
	}

	@Override
	public final void undo() {
		swap();
	}
	
	protected abstract void swap();

}
