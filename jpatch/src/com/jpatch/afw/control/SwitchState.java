package com.jpatch.afw.control;

import com.jpatch.afw.attributes.StateMachine;

public class SwitchState extends AttributeEdit {
	private final StateMachine stateMachine;
	private Object state;
	
	public SwitchState(StateMachine stateMachine, Object newState, boolean applyNow) {
		this.stateMachine = stateMachine;
		this.state = newState;
		apply(applyNow);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void toggle() {
		Object tmp = stateMachine.getValue();
		stateMachine.setValue(state);
		state = tmp;
	}
}
