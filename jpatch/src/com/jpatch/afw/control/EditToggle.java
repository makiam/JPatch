package com.jpatch.afw.control;

import com.jpatch.afw.attributes.Toggle;

public final class EditToggle extends EditAttribute {
	Toggle toggle;
	
	public EditToggle(Toggle toggle, boolean applyNow) {
		this.toggle = toggle;
		apply(applyNow);
	}
	
	protected void toggle() {
		toggle.setState(!toggle.getState());
	}
}
