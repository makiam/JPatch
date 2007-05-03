package com.jpatch.afw.ui;

import com.jpatch.afw.control.JPatchAction;

import javax.swing.JMenuItem;

public class JPatchMenuItem extends JMenuItem {
	JPatchAction action;
	
	public JPatchMenuItem(JPatchAction action) {
		this.action = action;
		addActionListener(action);
		setIcon(new MenuIcon(this));
	}
	
	JPatchAction getJPatchAction() {
		return action;
	}
}
