package jpatch.boundary.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class JPatchToggleButton extends JToggleButton {

	private static final Insets INSETS = new Insets(2, 2, 2, 2);
	
	JPatchToggleButton() {
		super();
	}
	
	public JPatchToggleButton(JToggleButton.ToggleButtonModel buttonModel) {
		super();
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
            acceleratorText += "&nbsp;</font>";
            System.out.println(acceleratorText);
        }
		if (toolTipText != null) {
			if (acceleratorText != null)
				setToolTipText("<html>" + toolTipText + acceleratorText + "</html>");
			else
				setToolTipText(toolTipText);
		} else if (shortDescription != null) {
			System.out.println("*");
			if (acceleratorText != null) {
				setToolTipText("<html>" + shortDescription + acceleratorText + "</html>");
				System.out.println("**" + getToolTipText());
			} else
				setToolTipText(shortDescription);
		}
	}
	
	@Override
	/**
	 * Overrides proccessKeyBinding.
	 * If the KeyEvent is of type KEY_PRESSED and has a character associated with it, this method delays
	 * processing of the KeyBindings, stores the KeyStroke, KeyEvent, condition and pressed values in the
	 * static KeyBindingHelper and assumes to see a KEY_TYPED right after that KEY_PRESSED.
	 * Only if the corresponding KEY_TYPED event has not been consumed, it continues to process the KeyBinding
	 * with the values stored in KeyBindingHelper.
	 * 
	 * This way, KEY_PRESSED events can be used as key-bindings or menu-accelerators, but it prevents the
	 * action from being fired if a corresponding KEY_TYPED event has already been consumed (e.g. by a text
	 * component).
	 */
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		System.out.println(ks);
		/* Check if event is a KEY_PRESSED or KEY_TYPED event */
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			/* KEY_PRESSED */
			if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
				/* if it has a character assigned, store it */
				KeyBindingHelper.ks = ks;
				KeyBindingHelper.e = e;
				KeyBindingHelper.condition = condition;
				KeyBindingHelper.pressed = pressed;
				System.out.println("keypress stored");
				/* and return "false" */
				return false;
			} else {
				/* if not, process it */
				return super.processKeyBinding(ks, e, condition, pressed);
			}
		} else if (e.getID() == KeyEvent.KEY_TYPED) {
			/* KEY_TYPED */
			System.out.println("key typed");
			if (KeyBindingHelper.e != null && e.getKeyChar() == KeyBindingHelper.e.getKeyChar() && !e.isConsumed()) {
				/* if the KEY_TYPED event corresponds to the stored KEY_PRESSED event
				 * (i.e. it has the same character) and has not been consumed
				 * process the *stored* event
				 */
				return super.processKeyBinding(KeyBindingHelper.ks, KeyBindingHelper.e, KeyBindingHelper.condition, KeyBindingHelper.pressed);
			} else {
				/* else, process the KEY_TYPED event */
				return super.processKeyBinding(ks, e, condition, pressed);
			}
		} else {
			/* let superclass process the event */
			return super.processKeyBinding(ks, e, condition, pressed);
		}
	}
}
