package com.jpatch.afw.control;

/**
 * This is the base class for all Edits that add or remove something.
 * Implementing classes must override the add() and remove() methods,
 * the behavior for undo() and redo() is determined by the Mode parameter
 * (ADD or REMOVE) at construction time:
 * <dl>
 * <dt>ADD:</dt><dd>undo() calls remove(), redo() calls add() respectively</dd>
 * <dt>REMOVE:</dt><dd>undo() calls add(), redo() calls remove() respectively</dd>
 * </dl>
 * This way, only one Class has to be defined per add-/removable entity, which
 * saves a lot of duplicate boilerplate code.
 * 
 * @author sascha
 */
public abstract class AbstractAddRemoveEdit extends AbstractUndoableEdit {
	public static enum Mode { ADD, REMOVE }
	
	private final boolean add;
	
	public AbstractAddRemoveEdit(Mode mode) {
		add = (mode == Mode.ADD);
		assert add || mode == Mode.REMOVE;
	}
	
	@Override
	public final void undo() {
		if (add) {
			remove();
		} else {
			add();
		}
	}
	
	@Override
	public final void redo() {
		if (add) {
			add();
		} else {
			remove();
		}
	}
	
	public abstract void add();
	
	public abstract void remove();
	
}
