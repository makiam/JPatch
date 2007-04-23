package com.jpatch.afw.ui;

import com.jpatch.afw.control.JPatchAction;
import com.jpatch.afw.control.SwitchStateAction;
import com.jpatch.afw.control.ToggleAction;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

class MenuIcon implements Icon {
	private static final ImageIcon BULLET_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/bullet.png"));
	private static final ImageIcon CHECKMARK_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/checkmark.png"));
//	private static final ImageIcon SUBMENU_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/submenu.png"));
	private static final ImageIcon DISABLED_BULLET_ICON = new ImageIcon(ImageUtils.createDisabledIcon(BULLET_ICON.getImage()));
	private static final ImageIcon DISABLED_CHECKMARK_ICON = new ImageIcon(ImageUtils.createDisabledIcon(CHECKMARK_ICON.getImage()));
	private static final int HEIGHT = 16;
	private static final int GAP = 4;
	private static final int ICON_WIDTH = 16;
	private JMenuItem menuItem;
	private JLabel menuText = new JLabel();
	private JLabel menuAccelerator = new JLabel();
	
	MenuIcon(JMenuItem menuItem) {
		this.menuItem = menuItem;
		configureLabels();
	}
	
	void configureLabels() {
		if (menuItem instanceof JPatchMenuItem) {
			menuText.setText(((JPatchMenuItem) menuItem).getJPatchAction().getMenuText());
			menuAccelerator.setText(PlatformUtils.getAcceleratorString(((JPatchMenuItem) menuItem).getJPatchAction().getKeyboardShortcut()));
		} else if (menuItem instanceof JPatchMenu) {
			menuText.setText(((JPatchMenu) menuItem).getName());
		}
	}
	
	public int getIconHeight() {
		return HEIGHT;
	}

	int getIconFlags() {
		int flags = 0;
		for (Component comp : menuItem.getParent().getComponents()) {
			if (comp instanceof JPatchMenuItem) {
				JPatchMenuItem mi = (JPatchMenuItem) comp;
				if (mi.menuIcon.hasIcon()) {
					flags |= 1;
				}
			} else if (comp instanceof JPatchMenu) {
				flags |= 2;
			}
		}
		return flags;
	}

	public int getIconWidth() {
		if (menuItem.getParent() instanceof JMenuBar) {
			return menuText.getPreferredSize().width;
		}
		int width = 0;
		for (Component comp : menuItem.getParent().getComponents()) {
			MenuIcon mi;
			if (comp instanceof JPatchMenuItem) {
				mi = ((JPatchMenuItem) comp).menuIcon;
			} else if (comp instanceof JPatchMenu) {
				mi = ((JPatchMenu) comp).menuIcon;
			} else {
				continue;
			}
			int w = mi.menuText.getPreferredSize().width + mi.menuAccelerator.getPreferredSize().width;
			if (mi.menuAccelerator.getText() != null) {
				w += GAP;
			}
			width = Math.max(width, w);
		}
		int flags = getIconFlags();
		if ((flags & 1) != 0) {
			width += ICON_WIDTH;
		} else {
			width += GAP;
		}
//		if ((flags & 2) != 0) {
//			width += ICON_WIDTH;
//		} else {
//			width += GAP;
//		}
		return width;
	}
	
	public void paintIcon(Component comp, Graphics g, int x, int y) {
		menuText.setBounds(0, 0, menuText.getPreferredSize().width, HEIGHT);
		menuAccelerator.setBounds(0, 0, menuAccelerator.getPreferredSize().width, HEIGHT);
		if (menuItem.getParent() instanceof JMenuBar) {
			Graphics gLabel = g.create();
			gLabel.translate(GAP, 2);
			menuText.paint(gLabel);
			return;
		}
		int flags = getIconFlags();
		if (menuItem instanceof JPatchMenuItem) {
			JPatchAction action = ((JPatchMenuItem) menuItem).getJPatchAction();
			if (action instanceof ToggleAction) {
				if (((ToggleAction) action).isSelected()) {
					if (action.isEnabled()) {
						CHECKMARK_ICON.paintIcon(comp, g, 0, 0);
					} else {
						DISABLED_CHECKMARK_ICON.paintIcon(comp, g, 0, 0);
					}
				}
			} else if (action instanceof SwitchStateAction) {
				if (((SwitchStateAction) action).isSelected()) {
					if (action.isEnabled()) {
						BULLET_ICON.paintIcon(comp, g, 0, 0);
					} else {
						DISABLED_BULLET_ICON.paintIcon(comp, g, 0, 0);
					}
				}
			} else {
				if (action.getIcon() != null) {
					if (action.isEnabled()) {
						action.getIcon().paintIcon(comp, g, 0, 0);
					} else {
						action.getDisabledIcon().paintIcon(comp, g, 0, 0);
					}
				}
			}
			menuText.setForeground(action.isEnabled() ? Color.BLACK : new Color(1.0f, 1.0f, 1.0f, 1.0f / 3.0f));
			menuAccelerator.setForeground(menuText.getForeground());
		}
		
		int xOff = ((flags & 1) != 0) ? ICON_WIDTH + GAP : GAP;
		Graphics gLabel = g.create();
		gLabel.translate(xOff, 2);
		menuText.paint(gLabel);
//		xOff = ((flags & 2) != 0) ? ICON_WIDTH + GAP : GAP;
		xOff = GAP + menuAccelerator.getPreferredSize().width;
		gLabel = g.create();
		gLabel.translate(menuItem.getWidth() - xOff, 2);
		menuAccelerator.paint(gLabel);
//		if (menuItem instanceof JPatchMenu) {
//			SUBMENU_ICON.paintIcon(comp, g, menuItem.getWidth() - ICON_WIDTH, 0);
//		}
	}

	boolean hasIcon() {
		if (menuItem instanceof JPatchMenuItem) {
			JPatchAction action = ((JPatchMenuItem) menuItem).getJPatchAction();
			if (action instanceof ToggleAction || action instanceof SwitchStateAction) {
				return true;
			}
			if (action.isUseMenuIcon() && action.getIcon() != null) {
				return true;
			}
		}
		return false;
	}
}
