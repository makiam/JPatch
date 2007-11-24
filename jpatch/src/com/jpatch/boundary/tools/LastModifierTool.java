package com.jpatch.boundary.tools;

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
