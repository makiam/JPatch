package com.jpatch.afw.control;


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
	protected Icon buttonIcon;
	protected Icon buttonSelectedIcon;
	protected Icon buttonDisabledIcon;
	protected Icon buttonDisabledSelectedIcon;
	protected Icon buttonRolloverIcon;
	protected Icon buttonRolloverSelectedIcon;
	protected KeyStroke keyboardShortcut;
	
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
		this.buttonIcon = icon;
		this.useMenuIcon = useMenuIcon;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public Icon getButtonDisabledIcon() {
		return buttonDisabledIcon;
	}

	public void setButtonDisabledIcon(Icon buttonDisabledIcon) {
		this.buttonDisabledIcon = buttonDisabledIcon;
	}

	public Icon getButtonDisabledSelectedIcon() {
		return buttonDisabledSelectedIcon;
	}

	public void setButtonDisabledSelectedIcon(Icon buttonDisabledSelectedIcon) {
		this.buttonDisabledSelectedIcon = buttonDisabledSelectedIcon;
	}

	public Icon getButtonIcon() {
		return buttonIcon;
	}

	public void setButtonIcon(Icon buttonIcon) {
		this.buttonIcon = buttonIcon;
	}

	public Icon getButtonRolloverIcon() {
		return buttonRolloverIcon;
	}

	public void setButtonRolloverIcon(Icon buttonRolloverIcon) {
		this.buttonRolloverIcon = buttonRolloverIcon;
	}

	public Icon getButtonRolloverSelectedIcon() {
		return buttonRolloverSelectedIcon;
	}

	public void setButtonRolloverSelectedIcon(Icon buttonRolloverSelectedIcon) {
		this.buttonRolloverSelectedIcon = buttonRolloverSelectedIcon;
	}

	public Icon getButtonSelectedIcon() {
		return buttonSelectedIcon;
	}

	public void setButtonSelectedIcon(Icon buttonSelectedIcon) {
		this.buttonSelectedIcon = buttonSelectedIcon;
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
		this.keyboardShortcut = keyboardShortcut;
	}
}
