package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributePostChangeListener;
import com.jpatch.afw.attributes.GenericAttr;
import com.jpatch.afw.attributes.StateMachine;
import com.jpatch.afw.control.AttributeEdit;
import com.jpatch.afw.control.SwitchStateAction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class JPatchStateButton extends JToggleButton implements JPatchButton {
	private static final GenericAttr<String> EDIT_NAME = new GenericAttr<String>("statechange");
	
	private SwitchStateAction jpatchAction;
	public JPatchStateButton(SwitchStateAction action) {
		this.jpatchAction = action;
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setSelected(jpatchAction.getStateMachine().getValue() == jpatchAction.getState());
			}
		});
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StateMachine stateMachine = jpatchAction.getStateMachine();
				Object currentValue = stateMachine.getValue();
				if (currentValue == jpatchAction.getState()) {
					Object newValue = stateMachine.revertToDefault();
					if (newValue != currentValue) {
//						jpatchAction.getUndoManager().addEdit(EDIT_NAME, AttributeEdit.changeAttribute(stateMachine, currentValue, false));
						stateMachine.revertToDefault();
					}
				} else {
//					jpatchAction.getUndoManager().addEdit(EDIT_NAME, AttributeEdit.changeAttribute(stateMachine, jpatchAction.getState(), true));
					jpatchAction.getStateMachine().setValue(jpatchAction.getState());
				}
			}
		});
		jpatchAction.getStateMachine().addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				setSelected(jpatchAction.getStateMachine().getValue() == jpatchAction.getState());
			}
		});
		setSelected(jpatchAction.getStateMachine().getValue() == jpatchAction.getState());
	}
	
	public SwitchStateAction getJPatchAction() {
		return jpatchAction;
	}
}
