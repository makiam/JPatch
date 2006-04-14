package jpatch.boundary.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import jpatch.boundary.action.*;

public class JPatchMenuItem extends JMenuItem {
	
	public JPatchMenuItem(DefaultButtonModel buttonModel) {
		super();
		setModel(new JPatchButtonModel(buttonModel));
	}

	@Override
	/**
	 * Overrides proccessKeyBinding.
	 * If the KeyEvent is of type KEY_PRESSED and has a character associated with it, this method delays
	 * processing of the KeyBindings, stores the KeyStroke, KeyEvent, condition and pressed values in the
	 * static KeyBindingHelper and <b>assumes to see a KEY_TYPED right after that KEY_PRESSED</b>.
	 * Only if the corresponding KEY_TYPED event has not been consumed, it continues to process the KeyBinding
	 * with the values stored in KeyBindingHelper.
	 * 
	 * This way, KEY_PRESSED events can be used as menu-accelerators, but it prevents the action from 
	 * being fired if a corresponding KEY_TYPED event has already been consumed (e.g. by a text component).
	 */
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		/* Check if event is a KEY_PRESSED or KEY_TYPED event */
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			/* KEY_PRESSED */
			if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
				/* 
				 * if it has a character assigned, return "false"
				 * (KeyBindingHelper will store this event!)
				 */
				return false;
			} else {
				/* if not, process it */
				return super.processKeyBinding(ks, e, condition, pressed);
			}
		} else if (e.getID() == KeyEvent.KEY_TYPED) {
			/* KEY_TYPED */
			if (KeyBindingHelper.e != null && e.getKeyChar() == KeyBindingHelper.e.getKeyChar() && !e.isConsumed()) {
				/* if the KEY_TYPED event corresponds to the stored KEY_PRESSED event
				 * (i.e. it has the same character) and has not been consumed
				 * process the stored (KEY_PRESSED) event.
				 */
				if (super.processKeyBinding(KeyBindingHelper.ks, KeyBindingHelper.e, condition, KeyBindingHelper.pressed))
					return true;
				/* if processing the stored event didn't return true, continue processing this (KEY_TYPED) event */
				return super.processKeyBinding(ks, e, condition, pressed);
			} else {
				/* else, process the KEY_TYPED event */
				return super.processKeyBinding(ks, e, condition, pressed);
			}
		} else {
			/* let superclass process the event */
			return super.processKeyBinding(ks, e, condition, pressed);
		}
	}
	
	@Override
	protected void configurePropertiesFromAction(Action a) {
		super.configurePropertiesFromAction(a);
		if (a == null)
			return;
		String text = (String) a.getValue(JPatchAction.MENU_TEXT);
		String shortDescription = (String) a.getValue(JPatchAction.SHORT_DESCRIPTION);
		String accelerator = (String) a.getValue(JPatchAction.ACCELERATOR);
		String mnemonic = (String) a.getValue(JPatchAction.MNEMONIC);
		String icon = (String) a.getValue(JPatchAction.ICON);
		String toolTipText = (String) a.getValue(JPatchAction.MENU_TOOLTIP);
		if (icon != null)
			setIcon(new ImageIcon(ClassLoader.getSystemResource(icon)));
		if (text != null)
			setText(text);
		else if (shortDescription != null)
			setText(shortDescription);
		setToolTipText(toolTipText);
		if (accelerator != null)
			setAccelerator(KeyStroke.getKeyStroke(accelerator));
		if (mnemonic != null)
			setMnemonic(mnemonic.charAt(0));
	}
	
	@Override
	protected PropertyChangeListener createActionPropertyChangeListener(final Action a) {
        return new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName().equals("enabled"))
					setEnabled((Boolean) e.getNewValue());
				else if (e.getPropertyName().equals(JPatchAction.SHORT_DESCRIPTION) && a.getValue(JPatchAction.MENU_TEXT) == null)
					setText((String) e.getNewValue());
				else if (e.getPropertyName().equals(JPatchAction.MENU_TEXT))
					setText((String) e.getNewValue());
			}
        };
    }
}
