package com.jpatch.boundary.tools;

/**
 * Keeps a record of that last tool that actually caused a modification.
 * This is helpful to prevent redundant tool changes to become and
 * undoable edit.
 * @author sascha
 *
 */
public final class LastModifierTool {
	private final static LastModifierTool INSTANCE = new LastModifierTool();
	private JPatchTool tool;
	
	public static LastModifierTool getInstance() {
		return INSTANCE;
	}
	
	public void set(JPatchTool tool) {
		this.tool = tool;
	}
	
	public JPatchTool get() {
		return tool;
	}
}
