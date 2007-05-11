package com.jpatch.afw.control;

import com.jpatch.afw.attributes.AbstractAttribute;
import com.jpatch.afw.attributes.GenericAttr;

import java.util.*;

import jpatch.boundary.settings.Settings;

public class JPatchUndoManager extends AbstractAttribute {
	private static final int MAX_DEPTH = Settings.getInstance().undoDepth;
	
	private List<NamedEditList> undoStack = new ArrayList<NamedEditList>();
	private int position;
	
	public void addEdit(GenericAttr<String> name, List<JPatchUndoableEdit> editList) {
		addEdit(new NamedEditList(name, editList));
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
		fireAttributeHasChanged();
	}
	
	public void redo() {
		if (!canRedo()) {
			throw new IllegalStateException("can't redo");
		}
		undoStack.get(position++).redo();
		fireAttributeHasChanged();
	}
	
	private void addEdit(NamedEditList namedEditList) {
		if (position == MAX_DEPTH) {
			undoStack.remove(0);
		} else {
			for (int i = position, n = undoStack.size(); i < n; i++) {
				undoStack.remove(position);
			}
		}
		undoStack.add(position++, namedEditList);
		fireAttributeHasChanged();
	}
	
	private static class NamedEditList {
		private final GenericAttr<String> name;
		private final JPatchUndoableEdit[] edits;
		
		private NamedEditList(GenericAttr<String> name, List<JPatchUndoableEdit> editList) {
			this.name = name;
			edits = editList.toArray(new JPatchUndoableEdit[editList.size()]);
		}
		
		private NamedEditList(GenericAttr<String> name, JPatchUndoableEdit edit) {
			this.name = name;
			edits = new JPatchUndoableEdit[] { edit };
		}
		
		private void undo() {
			for (int i = edits.length - 1; i > 0; i--) {
				edits[i].undo();
			}
		}
		
		private void redo() {
			for (int i = 0; i < edits.length; i++) {
				edits[i].undo();
			}
		}
	}
}
