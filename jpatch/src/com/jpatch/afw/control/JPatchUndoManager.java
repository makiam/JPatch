package com.jpatch.afw.control;

import com.jpatch.afw.attributes.AbstractScalarAttribute;
import com.jpatch.afw.attributes.GenericAttr;

import java.util.*;

import jpatch.boundary.settings.Settings;

public class JPatchUndoManager {
	private static final int MAX_DEPTH = Settings.getInstance().undoDepth;
	private static final boolean LOG = true;
	
	private final List<NamedEditList> undoStack = new ArrayList<NamedEditList>();
	private final ArrayList<JPatchUndoListener> undoListeners = new ArrayList();
	
	private int position;
	
	public void addUndoListener(JPatchUndoListener undoListener) {
		if (undoListener == null) {
			throw new NullPointerException();
		}
		if (undoListeners.contains(undoListener)) {
			throw new IllegalArgumentException("UndoListener " + undoListener + " has already been added to " + this);
		}
		undoListeners.add(undoListener);
	}
	
	public void removeUndoListener(JPatchUndoListener undoListener) {
		if (undoListener == null) {
			throw new NullPointerException();
		}
		if (!undoListeners.contains(undoListener)) {
			throw new IllegalArgumentException("UndoListener " + undoListener + " has not been added to " + this);
		}
		undoListeners.remove(undoListener);
	}
	
	private void fireUndoPerformed() {
		for (JPatchUndoListener undoListener : undoListeners) {
			undoListener.undoPerformed(this);
		}
	}
	
	private void fireRedoPerformed() {
		for (JPatchUndoListener undoListener : undoListeners) {
			undoListener.redoPerformed(this);
		}
	}
	
	private void fireEditAdded() {
		for (JPatchUndoListener undoListener : undoListeners) {
			undoListener.editAdded(this);
		}
	}
	
	public void addEdit(GenericAttr<String> name, List<JPatchUndoableEdit> editList) {
		addEdit(new NamedEditList(name, editList.toArray(new JPatchUndoableEdit[editList.size()])));
	}
	
	public void addEdit(GenericAttr<String> name, JPatchUndoableEdit[] edits) {
		addEdit(new NamedEditList(name, edits));
	}
	
	public void addEdit(GenericAttr<String> name, JPatchUndoableEdit edit) {
		addEdit(new NamedEditList(name, edit));
	}
	
	public boolean canUndo() {
		return position > 0;
	}
	
	public boolean canRedo() {
		return position < undoStack.size();
	}
	
	public String getUndoName() {
		if (canUndo()) {
			return undoStack.get(position - 1).name.getValue();
		} else {
			return ResourceManager.getInstance().getString("CANT_UNDO");
		}
	}
	
	public String getRedoName() {
		if (canRedo()) {
			return undoStack.get(position).name.getValue();
		} else {
			return ResourceManager.getInstance().getString("CANT_REDO");
		}
	}
	
	public void undo() {
		if (!canUndo()) {
			throw new IllegalStateException("can't undo");
		}
		undoStack.get(--position).undo();
		fireUndoPerformed();
	}

	public void redo() {
		if (!canRedo()) {
			throw new IllegalStateException("can't redo");
		}
		undoStack.get(position++).redo();
		fireRedoPerformed();
	}
	
	private void addEdit(NamedEditList namedEditList) {
		if (position == MAX_DEPTH) {
			undoStack.remove(0);
			position--;
		} else {
			for (int i = position, n = undoStack.size(); i < n; i++) {
				undoStack.remove(position);
			}
		}
		undoStack.add(position++, namedEditList);
		fireEditAdded();
	}
	
	private static class NamedEditList {
		private final GenericAttr<String> name;
		private final JPatchUndoableEdit[] edits;
		
		private NamedEditList(GenericAttr<String> name, JPatchUndoableEdit[] edits) {
			this.name = name;
			this.edits = edits;
		}
		
		private NamedEditList(GenericAttr<String> name, JPatchUndoableEdit edit) {
			this.name = name;
			edits = new JPatchUndoableEdit[] { edit };
		}
		
		private void undo() {
			if (LOG) {
				System.out.println("undo " + name.getValue());
			}
			for (int i = edits.length - 1; i >= 0; i--) {
				edits[i].undo();
			}
		}
		
		private void redo() {
			if (LOG) {
				System.out.println("redo " + name.getValue());
			}
			for (int i = 0; i < edits.length; i++) {
				edits[i].redo();
			}
		}
	}
}
