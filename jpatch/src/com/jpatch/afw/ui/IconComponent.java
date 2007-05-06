package com.jpatch.afw.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JComponent;

@SuppressWarnings("serial")
public class IconComponent extends JComponent {
	private Icon icon;
	private Dimension dim = new Dimension();
	public IconComponent(Icon icon) {
		this.icon = icon;
	}
	
	@Override
	public Dimension getPreferredSize() {
		Insets insets = getInsets();
		dim.width = icon.getIconWidth() + insets.left + insets.right;
		dim.height = icon.getIconHeight() + insets.top + insets.bottom;
		return dim;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Insets insets = getInsets();
		int x = insets.left;
		int y = insets.top;
		icon.paintIcon(this, g, x, y);
	}
}