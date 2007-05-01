package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributeListener;
import com.jpatch.afw.control.SwitchStateAction;

import javax.swing.JRadioButtonMenuItem;

public final class JPatchRadioButtonMenuItem extends JRadioButtonMenuItem {
	final SwitchStateAction action;
	MenuIcon menuIcon;
	
	public JPatchRadioButtonMenuItem(SwitchStateAction action) {
		this.action = action;
		addActionListener(action);
		menuIcon = new MenuIcon(this);
		setIcon(menuIcon);
		setSelected(action.isSelected());
		action.getStateMachine().addAttributeListener(new AttributeListener() {
			public void attributeHasChanged(Attribute source) {
				setSelected(JPatchRadioButtonMenuItem.this.action.isSelected());
			}
		});
	}
	
	SwitchStateAction getJPatchAction() {
		return action;
	}
}
