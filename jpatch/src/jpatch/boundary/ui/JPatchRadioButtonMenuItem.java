package jpatch.boundary.ui;

import java.awt.*;
import java.awt.event.KeyEvent;

import javax.swing.*;;

public class JPatchRadioButtonMenuItem extends JRadioButtonMenuItem {
	private KeyEvent keyPressedEvent;
	
	public JPatchRadioButtonMenuItem() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public JPatchRadioButtonMenuItem(Action a) {
		super(a);
		// TODO Auto-generated constructor stub
	}
	
	public JPatchRadioButtonMenuItem(Icon icon, boolean selected) {
		super(icon, selected);
		// TODO Auto-generated constructor stub
	}
	
	public JPatchRadioButtonMenuItem(Icon icon) {
		super(icon);
		// TODO Auto-generated constructor stub
	}
	
	public JPatchRadioButtonMenuItem(String text, boolean selected) {
		super(text, selected);
		// TODO Auto-generated constructor stub
	}
	
	public JPatchRadioButtonMenuItem(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
		// TODO Auto-generated constructor stub
	}
	
	public JPatchRadioButtonMenuItem(String text, Icon icon) {
		super(text, icon);
		// TODO Auto-generated constructor stub
	}
	
	public JPatchRadioButtonMenuItem(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}
	
	
	
	

	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
//		if (true)
//			throw new RuntimeException();
		/* Check if event is a KEY_PRESSED or KEY_TYPED event */
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			/* KEY_PRESSED */
			if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
				/* if it has a character assigned, store it */
				System.out.println("*");
				keyPressedEvent = e;
				return false;
			} else {
				/* if not, process it */
				return super.processKeyBinding(ks, e, condition, pressed);
			}
		} else if (e.getID() == KeyEvent.KEY_TYPED) {
			/* KEY_TYPED */
			if (keyPressedEvent != null && e.getKeyChar() == keyPressedEvent.getKeyChar() && !e.isConsumed()) {
				/* if the KEY_TYPED event corresponds to the stored KEY_PRESSED event
				 * (i.e. it has the same character) and has not been consumed
				 * process the stored KEY_PRESSED event
				 */
				return super.processKeyBinding(ks, keyPressedEvent, condition, pressed);
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
