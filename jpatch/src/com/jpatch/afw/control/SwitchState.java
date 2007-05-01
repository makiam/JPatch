package com.jpatch.afw.control;

import com.jpatch.afw.attributes.StateMachine;

public class SwitchState extends EditAttribute {
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
		Object tmp = stateMachine.getState();
		stateMachine.setState(state);
		state = tmp;
	}
}
