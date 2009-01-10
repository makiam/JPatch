package com.jpatch.afw.control;

/**
 * Each UndoableEdit must provide an undo() and a redo() method.
 * @author sascha
 *
 */
public interface JPatchUndoableEdit {
	public void performUndo();
	public void performRedo();
}
