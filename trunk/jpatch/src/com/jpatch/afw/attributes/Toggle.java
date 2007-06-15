package com.jpatch.afw.attributes;

public class Toggle extends BooleanAttr {
	public Toggle() {
		super();
	}
	
	public Toggle(boolean initialState) {
		super(initialState);
	}
	
	@Override
	public boolean setBoolean(boolean newState) {
		if (newState != value) {
			newState = fireAttributeWillChange(newState);
			value = setState(newState);
			fireAttributeHasChanged();
		}
		return value;
	}
	
	public boolean toggle() {
		return setBoolean(!value);
	}
	
	protected boolean setState(boolean newState) {
		return newState;
	}
}
