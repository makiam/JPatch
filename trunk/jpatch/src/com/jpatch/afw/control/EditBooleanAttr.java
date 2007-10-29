package com.jpatch.afw.control;

import com.jpatch.afw.attributes.BooleanAttr;

public final class EditBooleanAttr extends AttributeEdit {
	BooleanAttr booleanAttr;
	
	public EditBooleanAttr(BooleanAttr booleanAttr, boolean applyNow) {
		this.booleanAttr = booleanAttr;
		apply(applyNow);
	}
	
	protected void toggle() {
		booleanAttr.setBoolean(!booleanAttr.getBoolean());
	}
}
