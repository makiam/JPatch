package com.jpatch.afw.control;

import com.jpatch.afw.attributes.Toggle;

public final class EditToggle extends AttributeEdit {
	Toggle toggle;
	
	public EditToggle(Toggle toggle, boolean applyNow) {
		this.toggle = toggle;
		apply(applyNow);
	}
	
	protected void toggle() {
		toggle.setBoolean(!toggle.getBoolean());
	}
}
