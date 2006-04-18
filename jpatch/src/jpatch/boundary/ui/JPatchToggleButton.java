package jpatch.boundary.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

import jpatch.boundary.action.JPatchAction;

public class JPatchToggleButton extends JToggleButton implements KeyBindingHelper.CallBack {

	private static final Insets INSETS = new Insets(2, 2, 2, 2);
	
	JPatchToggleButton() {
		super();
		getActionMap().put("doClick", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				model.setArmed(true);
				model.setPressed(true);
				model.setPressed(false);
				model.setArmed(false);
			}
		});
		getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doClick");
		getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "doClick");
	}
	
	public JPatchToggleButton(JToggleButton.ToggleButtonModel buttonModel) {
		this();
		setModel(new JPatchToggleButtonModel(buttonModel));
	}

//	public JPatchToggleButton(Icon icon) {
//		super(icon);
//	}
//
//	public JPatchToggleButton(Icon icon, boolean selected) {
//		super(icon, selected);
//	}
//
//	public JPatchToggleButton(String text) {
//		super(text);
//	}
//
//	public JPatchToggleButton(String text, boolean selected) {
//		super(text, selected);
//	}
//
//	public JPatchToggleButton(Action a) {
//		super(a);
//	}
//
//	public JPatchToggleButton(String text, Icon icon) {
//		super(text, icon);
//	}
//
//	public JPatchToggleButton(String text, Icon icon, boolean selected) {
//		super(text, icon, selected);
//	}

	public Insets getMargin() {
		return INSETS;
	}
	
	@Override
	protected void configurePropertiesFromAction(Action a) {
		super.configurePropertiesFromAction(a);
		if (a == null)
			return;
		String icon = (String) a.getValue("Icon");
		String selectedIcon = (String) a.getValue("SelectedIcon");
		String rolloverIcon = (String) a.getValue("RolloverIcon");
		String rolloverSelectedIcon = (String) a.getValue("RolloverSelectedIcon");
		String disabledIcon = (String) a.getValue("DisabledIcon");
		String disabledSelectedIcon = (String) a.getValue("DisabledSelectedIconResoure");
		String shortDescription = (String) a.getValue("ShortDescription");
		String toolTipText = (String) a.getValue("ButtonToolTip");
		String accelerator = (String) a.getValue("Accelerator");
		if (icon != null)
			setIcon(new ImageIcon(ClassLoader.getSystemResource(icon)));
		if (selectedIcon != null)
			setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource(selectedIcon)));
		if (rolloverIcon != null)
			setRolloverIcon(new ImageIcon(ClassLoader.getSystemResource(rolloverIcon)));
		if (rolloverSelectedIcon != null)
			setRolloverSelectedIcon(new ImageIcon(ClassLoader.getSystemResource(rolloverSelectedIcon)));
		if (disabledIcon != null)
			setDisabledIcon(new ImageIcon(ClassLoader.getSystemResource(disabledIcon)));
		if (disabledSelectedIcon != null)
			setDisabledSelectedIcon(new ImageIcon(ClassLoader.getSystemResource(disabledSelectedIcon)));
		String acceleratorText = null;
		if (accelerator != null) {
			String acceleratorDelimiter = UIManager.getString("MenuItem.acceleratorDelimiter");
			if (acceleratorDelimiter == null)
				acceleratorDelimiter = "+";
			KeyStroke ks =  KeyStroke.getKeyStroke(accelerator);
			int color = UIManager.getColor("ToolTip.foregroundInactive").getRGB() & 0xffffff;
			acceleratorText = "&nbsp;&nbsp;&nbsp;<font style='font-size: 90%; color: #" + Integer.toHexString(color) + "'>";
			int modifiers = ks.getModifiers();
            if (modifiers > 0) {
                acceleratorText += KeyEvent.getKeyModifiersText(modifiers);
                acceleratorText += acceleratorDelimiter;
            }
            int keyCode = ks.getKeyCode();
            if (keyCode != 0) {
                acceleratorText += KeyEvent.getKeyText(keyCode);
            } else {
                acceleratorText += ks.getKeyChar();
            }
            acceleratorText += "</font>";
        }
		if (toolTipText != null) {
			if (acceleratorText != null)
				setToolTipText("<html>&nbsp;" + toolTipText + acceleratorText + "&nbsp;</html>");
			else
				setToolTipText(toolTipText);
		} else if (shortDescription != null) {
			if (acceleratorText != null)
				setToolTipText("<html>&nbsp;" + shortDescription + acceleratorText + "&nbsp;</html>");
			else
				setToolTipText(shortDescription);
		}
		
		if (accelerator != null) {
			KeyStroke ks = KeyStroke.getKeyStroke(accelerator);
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ks, "doClick");
			getActionMap().put("doClick", new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					model.setArmed(true);
					model.setPressed(true);
					model.setPressed(false);
					model.setArmed(false);
				}
			});
		}
	}
	
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
			KeyBindingHelper.registerCallback(this, condition);
		}
		return false;
	}

	public boolean reprocessKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		return super.processKeyBinding(ks, e, condition, pressed);
	}
	
	@Override
	protected PropertyChangeListener createActionPropertyChangeListener(final Action a) {
        return new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName().equals("enabled"))
					setEnabled((Boolean) e.getNewValue());
				else if (e.getPropertyName().equals(JPatchAction.SHORT_DESCRIPTION) && a.getValue(JPatchAction.MENU_TEXT) == null)
					setToolTipText((String) e.getNewValue());
				else if (e.getPropertyName().equals(JPatchAction.BUTTON_TEXT))
					setText((String) e.getNewValue());
				else if (e.getPropertyName().equals(JPatchAction.ACCELERATOR)) {
					KeyStroke newKs = KeyStroke.getKeyStroke((String) e.getNewValue());
					KeyStroke oldKs = KeyStroke.getKeyStroke((String) e.getOldValue());
					if (oldKs != null)
						getInputMap(WHEN_IN_FOCUSED_WINDOW).put(oldKs, null);
					if (newKs != null)
						getInputMap(WHEN_IN_FOCUSED_WINDOW).put(newKs, "doClick");
					
				}
			}
        };
    }
}
