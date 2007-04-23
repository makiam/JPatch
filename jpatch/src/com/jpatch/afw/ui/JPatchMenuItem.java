package com.jpatch.afw.ui;

import com.jpatch.afw.control.JPatchAction;

import javax.swing.JMenuItem;

public class JPatchMenuItem extends JMenuItem {
	JPatchAction action;
	MenuIcon menuIcon;
	
	public JPatchMenuItem(JPatchAction action) {
		this.action = action;
		addActionListener(action);
		menuIcon = new MenuIcon(this);
		setIcon(menuIcon);
	}
	
	JPatchAction getJPatchAction() {
		return action;
	}
}
