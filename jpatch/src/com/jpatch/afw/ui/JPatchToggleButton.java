package com.jpatch.afw.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributeAdapter;
import com.jpatch.afw.control.*;

import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JPatchToggleButton extends JToggleButton implements JPatchButton {
	private ToggleAction jpatchAction;
	
	public JPatchToggleButton(ToggleAction action) {
		this.jpatchAction = action;
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setSelected(jpatchAction.getToggle().getState());
			}
		});
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSelected(jpatchAction.getToggle().toggleState());
			}
		});
		jpatchAction.getToggle().addAttributeListener(new AttributeAdapter() {
			@Override
			public void attributeHasChanged(Attribute source) {
				setSelected(jpatchAction.getToggle().getState());
			}
		});
		setSelected(jpatchAction.getToggle().getState());
	}
	
	public ToggleAction getJPatchAction() {
		return jpatchAction;
	}
}
