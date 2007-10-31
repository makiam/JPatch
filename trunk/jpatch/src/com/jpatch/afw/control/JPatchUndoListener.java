package com.jpatch.afw.control;

public interface JPatchUndoListener {
	public void editAdded(JPatchUndoManager undoManager);
	public void undoPerformed(JPatchUndoManager undoManager);
	public void redoPerformed(JPatchUndoManager undoManager);
}
