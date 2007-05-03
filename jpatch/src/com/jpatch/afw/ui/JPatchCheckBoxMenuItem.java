package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributeAdapter;
import com.jpatch.afw.control.ToggleAction;

import javax.swing.JCheckBoxMenuItem;

public class JPatchCheckBoxMenuItem extends JCheckBoxMenuItem {
	ToggleAction action;
	MenuIcon menuIcon;
	
	public JPatchCheckBoxMenuItem(ToggleAction action) {
		this.action = action;
		addActionListener(action);
		menuIcon = new MenuIcon(this);
		setIcon(menuIcon);
		setSelected(action.isSelected());
		action.getToggle().addAttributeListener(new AttributeAdapter() {
			public void attributeHasChanged(Attribute source) {
				setSelected(JPatchCheckBoxMenuItem.this.action.isSelected());
			}
		});
	}
	
	ToggleAction getJPatchAction() {
		return action;
	}
}
