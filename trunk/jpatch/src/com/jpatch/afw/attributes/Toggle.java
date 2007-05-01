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
		if (newState != currentState) {
			newState = fireAttributeWillChange(newState);
			currentState = toggle(newState);
			fireAttributeHasChanged();
		}
		return currentState;
	}
	
	public boolean getState() {
		return currentState;
	}
	
	protected boolean toggle(boolean newState) {
		return newState;
	}
}
