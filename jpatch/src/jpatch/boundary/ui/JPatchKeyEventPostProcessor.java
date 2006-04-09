package jpatch.boundary.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * This class is used to manage all JPatch key-bindings.
 * It's implemented as a KeyEventPostProcessor to prevent
 * KEY_PRESSES evnets whose corresponding KEY_TYPED events
 * have been consumed (e.g. by a text component) to trigger
 * actions.
 * 
 * @author sascha
 */
public class JPatchKeyEventPostProcessor implements KeyEventPostProcessor {
	/** A map to store KeyBindings */
	private Map<KeyStroke, ActionListener> keyBindings = new HashMap<KeyStroke, ActionListener>();
	/** The delayed KEY_PRESSED KeyEvent */
	private KeyEvent keyPressedEvent;
	
	/**
	 * Adds a key-binding
	 * @param keyStroke
	 * @param actionListener
	 */
	public void addKeyBinding(KeyStroke keyStroke, ActionListener actionListener) {
		keyBindings.put(keyStroke, actionListener);
	}
	
	/**
	 * Removes a key-binding
	 * @param keyStroke
	 */
	public void removeKeyBinding(KeyStroke keyStroke) {
		keyBindings.remove(keyStroke);
	}
	
	/**
	 * Removes all key-bindings
	 */
	public void clearAllKeyBindings() {
		keyBindings.clear();
	}
	
	/**
	 * Returns a Set of KeyStrokes containing all key-bindings
	 * @return the KeyStrokes
	 */
	public Set<KeyStroke> getAllKeyBindings() {
		return keyBindings.keySet();
	}
	
	/**
	 * This method implements the KeyEventPostProcessor interface.
	 *
	 * It is called by the current KeyboardFocusManager, requesting 
	 * that this KeyEventPostProcessor perform any necessary post-processing 
	 * which should be part of the KeyEvent's final resolution.
	 *
	 * @see java.awt.KeyEventPostProcessor#postProcessKeyEvent(java.awt.event.KeyEvent)
	 * 
	 * If the KeyEvent is a KEY_PRESSED event and has a character associated
	 * with it, this method stores the event and waits for the corresponding
	 * KEY_TYPED event. Only if the KEY_TYPED event has not been already consumed,
	 * the KEY_PRESSED event is processed further. If the KEY_PRESSED event does not
	 * have a character associated with it, it is processed immediately.
	 * 
	 * The whole purpose of this is to not process KEY_PRESSED events whose corresponging
	 * KEY_TYPED events have been handled e.g. by text components.
	 */
	public boolean postProcessKeyEvent(KeyEvent e) {
		/* Check if event is a KEY_PRESSED or KEY_TYPED event */
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			/* KEY_PRESSED */
			if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
				/* if it has a character assigned, store it */
				keyPressedEvent = e;
			} else {
				/* if not, process it */
				processEvent(e);
			}
		} else if (e.getID() == KeyEvent.KEY_TYPED) {
			/* KEY_TYPED */
			if (e.getKeyChar() == keyPressedEvent.getKeyChar() && !e.isConsumed()) {
				/* if the KEY_TYPED event corresponds to the stored KEY_PRESSED event
				 * (i.e. it has the same character) and has not been consumed
				 * process the stored KEY_PRESSED event
				 */
				processEvent(keyPressedEvent);
			} else {
				/* else, process the KEY_TYPED event */
				processEvent(e);
			}
		}
		/* return true in any case */
		return true;
	}
	
	/**
	 * Processes the KeyEvent (only called by postProcessKeyEvent)
	 */
	private void processEvent(KeyEvent e) {
		/* if the event has already been consumed, do nothing and return */
		if (e.isConsumed())
			return;
		/* If an ActionListener is bound to this KeyStroke, fire its actionPerformed method */
		KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
		ActionListener actionListener = keyBindings.get(keyStroke);
		if (actionListener != null) {
			ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
			actionListener.actionPerformed(actionEvent);
		}
	}
}
