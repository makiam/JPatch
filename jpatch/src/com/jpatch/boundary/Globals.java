package com.jpatch.boundary;

import com.jpatch.afw.attributes.*;

public class Globals {
	private static final Globals INSTANCE = new Globals();
	private final IntAttr editLevelAttr = new IntAttr(0);
	
	public static Globals getInstance() {
		return INSTANCE;
	}
	
	private Globals() {
		;
	}
	
	public IntAttr getEditLevelAttribute() {
		return editLevelAttr;
	}
}
