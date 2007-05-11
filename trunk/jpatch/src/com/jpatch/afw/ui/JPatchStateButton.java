package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributePostChangeListener;
import com.jpatch.afw.control.SwitchStateAction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class JPatchStateButton extends JToggleButton implements JPatchButton {
	private SwitchStateAction jpatchAction;
	public JPatchStateButton(SwitchStateAction action) {
		this.jpatchAction = action;
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setSelected(jpatchAction.getStateMachine().getState() == jpatchAction.getState());
			}
		});
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jpatchAction.getStateMachine().getState() == jpatchAction.getState()) {
					jpatchAction.getStateMachine().revertToDefault();
				} else {
					jpatchAction.getStateMachine().setState(jpatchAction.getState());
				}
			}
		});
		jpatchAction.getStateMachine().addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				setSelected(jpatchAction.getStateMachine().getState() == jpatchAction.getState());
			}
		});
		setSelected(jpatchAction.getStateMachine().getState() == jpatchAction.getState());
	}
	
	public SwitchStateAction getJPatchAction() {
		return jpatchAction;
	}
}
