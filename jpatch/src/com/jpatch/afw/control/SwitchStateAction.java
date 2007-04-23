package com.jpatch.afw.control;

import java.awt.event.ActionEvent;
import com.jpatch.afw.attributes.StateMachine;
import javax.swing.Icon;

public class SwitchStateAction extends JPatchAction {
	protected final StateMachine stateMachine;
	protected final Object state;
	
	public SwitchStateAction(StateMachine stateMachine, Object state, JPatchUndoManager undoManager, String name) {
		super(undoManager, name);
		this.stateMachine = stateMachine;
		this.state = state;
	}
	
	public SwitchStateAction(StateMachine stateMachine, Object state, JPatchUndoManager undoManager, String name, String text) {
		super(undoManager, name);
		this.stateMachine = stateMachine;
		this.state = state;
	}
	
	public SwitchStateAction(StateMachine stateMachine, Object state, JPatchUndoManager undoManager, String name, String text, Icon icon) {
		super(undoManager, name);
		this.stateMachine = stateMachine;
		this.state = state;
	}

	public void actionPerformed(ActionEvent event) {
		JPatchUndoableEdit edit = new SwitchState(stateMachine, state, true);
		if (undoManager != null) {
			undoManager.addEdit(displayName, edit);
		}
	}
	
	public StateMachine getStateMachine() {
		return stateMachine;
	}
	
	public Object getState() {
		return state;
	}
	
	public boolean isSelected() {
		return stateMachine.getState() == state;
	}
}
