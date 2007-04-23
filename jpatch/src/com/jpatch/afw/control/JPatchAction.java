package com.jpatch.afw.control;


import com.jpatch.afw.ui.KeyboardShortcutManager;

import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.KeyStroke;

public abstract class JPatchAction implements ActionListener {
	protected final String name;
	protected final JPatchUndoManager undoManager;
	protected String displayName;
	protected String buttonText;
	protected String menuText;
	protected boolean useMenuIcon;
	protected Icon icon;
	protected Icon selectedIcon;
	protected Icon disabledIcon;
	protected Icon disabledSelectedIcon;
	protected Icon rolloverIcon;
	protected Icon rolloverSelectedIcon;
	protected KeyStroke keyboardShortcut;
	protected boolean enabled = true;

	public JPatchAction(JPatchUndoManager undoManager, String name) {
		this.undoManager = undoManager;
		this.name = name;
		ResourceManager.getInstance().configureAction(this);
	}

	public JPatchAction(JPatchUndoManager undoManager, String name, String text) {
		this(undoManager, name);
		this.buttonText = text;
		this.menuText = text;
	}
	
	public JPatchAction(JPatchUndoManager undoManager, String name, String text, Icon icon, boolean useMenuIcon) {
		this(undoManager, name, text);
		this.icon = icon;
		this.useMenuIcon = useMenuIcon;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public Icon getDisabledIcon() {
		return disabledIcon;
	}

	public void setDisabledIcon(Icon disabledIcon) {
		this.disabledIcon = disabledIcon;
	}

	public Icon getDisabledSelectedIcon() {
		return disabledSelectedIcon;
	}

	public void setDisabledSelectedIcon(Icon disabledSelectedIcon) {
		this.disabledSelectedIcon = disabledSelectedIcon;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public Icon getRolloverIcon() {
		return rolloverIcon;
	}

	public void setRolloverIcon(Icon rolloverIcon) {
		this.rolloverIcon = rolloverIcon;
	}

	public Icon getRolloverSelectedIcon() {
		return rolloverSelectedIcon;
	}

	public void setRolloverSelectedIcon(Icon rolloverSelectedIcon) {
		this.rolloverSelectedIcon = rolloverSelectedIcon;
	}

	public Icon getSelectedIcon() {
		return selectedIcon;
	}

	public void setSelectedIcon(Icon selectedIcon) {
		this.selectedIcon = selectedIcon;
	}

	public String getButtonText() {
		return buttonText;
	}

	public void setButtonText(String buttonText) {
		this.buttonText = buttonText;
	}

	public boolean isUseMenuIcon() {
		return useMenuIcon;
	}

	public void setUseMenuIcon(boolean useMenuIcon) {
		this.useMenuIcon = useMenuIcon;
	}

	public String getMenuText() {
		return menuText;
	}

	public void setMenuText(String menuText) {
		this.menuText = menuText;
	}

	public String getName() {
		return name;
	}

	public KeyStroke getKeyboardShortcut() {
		return keyboardShortcut;
	}

	public void setKeyboardShortcut(KeyStroke keyboardShortcut) {
		KeyboardShortcutManager.getInstance().unmanageAction(this);
		this.keyboardShortcut = keyboardShortcut;
		KeyboardShortcutManager.getInstance().manageAction(this);
	}
}
