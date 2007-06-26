package com.jpatch.afw.control;


import com.jpatch.afw.attributes.*;
import com.jpatch.afw.ui.KeyboardShortcutManager;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.KeyStroke;

public abstract class JPatchAction implements ActionListener {
	protected final String name;
	protected final JPatchUndoManager undoManager;
	protected GenericAttr<String> displayName = new GenericAttr<String>("");
	protected GenericAttr<String> buttonText = new GenericAttr<String>("");
	protected GenericAttr<String> menuText = new GenericAttr<String>("");
	protected BooleanAttr useMenuIcon = new BooleanAttr(false);
	protected GenericAttr<Icon> icon = new GenericAttr<Icon>();
	protected GenericAttr<Icon> selectedIcon = new GenericAttr<Icon>();
	protected GenericAttr<Icon> disabledIcon = new GenericAttr<Icon>();
	protected GenericAttr<Icon> disabledSelectedIcon = new GenericAttr<Icon>();
	protected GenericAttr<Icon> rolloverIcon = new GenericAttr<Icon>();
	protected GenericAttr<Icon> rolloverSelectedIcon = new GenericAttr<Icon>();
	protected GenericAttr<KeyStroke> keyboardShortcut = new GenericAttr<KeyStroke>();
	protected BooleanAttr enabled = new BooleanAttr(true);

	
	public JPatchAction(JPatchUndoManager undoManager, String name) {
		this.undoManager = undoManager;
		this.name = name;
		keyboardShortcut.addAttributePreChangeListener(new AttributePreChangeAdapter<String>() {
			@Override
			public String attributeWillChange(ScalarAttribute source, String value) {
				KeyboardShortcutManager.getInstance().unmanageAction(JPatchAction.this);
				return value;
			}
		});
		keyboardShortcut.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				KeyboardShortcutManager.getInstance().manageAction(JPatchAction.this);
			}
		});
		ResourceManager.getInstance().configureAction(this);
	}

	public JPatchAction(JPatchUndoManager undoManager, String name, String text) {
		this(undoManager, name);
		this.buttonText.setValue(text);
		this.menuText.setValue(text);
	}
	
	public JPatchAction(JPatchUndoManager undoManager, String name, String text, Icon icon, boolean useMenuIcon) {
		this(undoManager, name, text);
		this.icon.setValue(icon);
		this.useMenuIcon.setBoolean(useMenuIcon);
	}
	
	public boolean isEnabled() {
		return enabled.getBoolean();
	}

	public GenericAttr<String> getButtonText() {
		return buttonText;
	}

	public GenericAttr<Icon> getDisabledIcon() {
		return disabledIcon;
	}

	public GenericAttr<Icon> getDisabledSelectedIcon() {
		return disabledSelectedIcon;
	}

	public GenericAttr<String> getDisplayName() {
		return displayName;
	}

	public BooleanAttr getEnabled() {
		return enabled;
	}

	public GenericAttr<Icon> getIcon() {
		return icon;
	}

	public GenericAttr<KeyStroke> getKeyboardShortcut() {
		return keyboardShortcut;
	}

	public GenericAttr<String> getMenuText() {
		return menuText;
	}

	public String getName() {
		return name;
	}

	public GenericAttr<Icon> getRolloverIcon() {
		return rolloverIcon;
	}

	public GenericAttr<Icon> getRolloverSelectedIcon() {
		return rolloverSelectedIcon;
	}

	public GenericAttr<Icon> getSelectedIcon() {
		return selectedIcon;
	}

	public BooleanAttr getUseMenuIcon() {
		return useMenuIcon;
	}
}
