package com.jpatch.afw.ui;

import com.jpatch.afw.control.JPatchAction;

import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.KeyStroke;

/**
 * This is a workaround for the following problem:
 * Some Swing components like JTextFields only consume KEY_TYPED events, but not teir preceding KEY_PRESSED events.
 * The result of this is that if one is listening for KEY_PRESSED events, events will be reported even if something like
 * a JTextField has focus and the KeyEvent belongs to some text entered by the user. Listening only for KEY_TYPED events
 * doesn't work because some keys don't produce KEY_TYPED events, and (worse), KEY_TYPED events have only keyChar information,
 * and unfortunately no keyCode information.
 * 
 * Here's what this workaround does:
 * A KeyEventPostProcessor reveives all KeyEvents from the KeyboardFocusManager.
 * If the KeyEvent has already been consumed, it does nothing.
 * Otherwise, if the KeyEvent has no character assigned (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED), the event
 * is being processed. If on the other hand a character is assigned, it stores the event (in the assumption that
 * a subsequent KEY_TYPED event will follow.
 * This KEY_TYPED event can only get so far if it hasn't been consumed already (e.g. by a JTextField), so it checks
 * wheter it corresponds to the stored KEY_PRESSED event, and if that's the case, it processes the stored KEY_PRESSED
 * event (remember that the keyCode is only stored in this KEY_PRESSED event, not in the KEY_TYPED event). Otherwise
 * it will process the KEY_TYPED event.
 */
public class KeyboardShortcutManager implements KeyEventPostProcessor {
	private static KeyboardShortcutManager INSTANCE;
	
	private KeyEvent storedEvent;
	private final Map<KeyStroke, JPatchAction> keyMap = new HashMap<KeyStroke, JPatchAction>();
	private final ActionEvent actionEvent = new ActionEvent(this, 0, "");
	
	private KeyboardShortcutManager() { }
	
	public static KeyboardShortcutManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new KeyboardShortcutManager();
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(INSTANCE);
		}
		return INSTANCE;
	}
	
	public boolean postProcessKeyEvent(KeyEvent e) {
		if (e.isConsumed()) {
			return false;
		}
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
				processEvent(e);
			} else {
				storedEvent = e;
			}
		} else if (e.getID() == KeyEvent.KEY_TYPED) {
			if (storedEvent != null && e.getKeyChar() == storedEvent.getKeyChar()) {
				processEvent(storedEvent);
			} else {
				processEvent(e);
			}
		}
		return false;
	}
	
	private void processEvent(KeyEvent e) {
		System.out.println(e);
		JPatchAction action = keyMap.get(KeyStroke.getKeyStrokeForEvent(e));
		if (action != null) {
			action.actionPerformed(actionEvent);
		}
	}
	
	public void manageAction(JPatchAction action) {
		KeyStroke keyStroke = action.getKeyboardShortcut().getValue();
		if (keyStroke != null) {
			keyMap.put(keyStroke, action);
		}
	}
	
	public void unmanageAction(JPatchAction action) {
		KeyStroke keyStroke = action.getKeyboardShortcut().getValue();
		if (keyStroke != null) {
			keyMap.remove(keyStroke);
		}
	}
}
