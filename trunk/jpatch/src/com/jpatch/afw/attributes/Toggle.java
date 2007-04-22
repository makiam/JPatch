package com.jpatch.afw.attributes;

public class Toggle extends AbstractAttribute {
	protected boolean currentState;

	public Toggle() {
		this(false);
	}
	
	public Toggle(boolean initialState) {
		this.currentState = initialState;
	}
	
	public boolean setState(boolean newState) {
		if (newState == currentState) {
			return false;
		}
		if (!toggle(newState)) {
			return false;
		}
		currentState = newState;
		fireAttributeChanged();
		return true;
	}
	
	public boolean getState() {
		return currentState;
	}
	
	protected boolean toggle(boolean newState) {
		return true;
	}
}
