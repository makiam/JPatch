package com.jpatch.afw.ui;

import com.jpatch.afw.control.ResourceManager;

import javax.swing.JMenu;

public class JPatchMenu extends JMenu {
	String name;
	MenuIcon menuIcon;
	
	public JPatchMenu(String name) {
		this.name = name;
		menuIcon = new MenuIcon(this);
		setIcon(menuIcon);
	}
	
	@Override
	public String getName() {
		return ResourceManager.getInstance().getString(name);
	}
}
