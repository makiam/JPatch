package jpatch.boundary.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;;

public class JPatchMenuItem extends JMenuItem {
	private DefaultButtonModel dbm;
	public JPatchMenuItem(DefaultButtonModel buttonModel) {
		super();
		setModel(new JPatchButtonModel(buttonModel));
		dbm = buttonModel;
	}
	
	@Override
	protected void fireActionPerformed(ActionEvent e) {
		System.out.println(getClass().getSimpleName() + " " + getText() + " fireActionPerformed(" + e.getSource() + ")");
		System.out.println("underlying model = " + dbm);
		System.out.println("model = " + getModel());
//		for (StackTraceElement ste : Thread.currentThread().getStackTrace())
//			System.out.println(ste);
		super.fireActionPerformed(e);
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
		if (!getText().equals("Lathe"))
			return false;
		for (ActionListener al : getActionListeners())
			System.out.println(al);
		System.out.println("\tprocessKeyBinding " + ks + " " + e.isConsumed() + " " + getText());
		boolean consumed;
		/* Check if event is a KEY_PRESSED or KEY_TYPED event */
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			/* KEY_PRESSED */
			if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
				/* if it has a character assigned, store it */
//				KeyBindingHelper.ks = ks;
//				KeyBindingHelper.e = e;
//				KeyBindingHelper.condition = condition;
//				KeyBindingHelper.pressed = pressed;
				/* and return "false" */
				return false;
			} else {
				/* if not, process it */
				consumed = super.processKeyBinding(ks, e, condition, pressed);
				if (consumed)
					e.consume();
				System.out.println("char undefined, consumed=" + e.isConsumed());
				return consumed;
			}
		} else if (e.getID() == KeyEvent.KEY_TYPED) {
			/* KEY_TYPED */
			if (KeyBindingHelper.e != null && e.getKeyChar() == KeyBindingHelper.e.getKeyChar()) {
				if (e.isConsumed() || KeyBindingHelper.e.isConsumed())
					return true;
				/* if the KEY_TYPED event corresponds to the stored KEY_PRESSED event
				 * (i.e. it has the same character) and has not been consumed
				 * process the stored (KEY_PRESSED) event.
				 */
				System.out.println("calling super...");
				if (super.processKeyBinding(KeyBindingHelper.ks, KeyBindingHelper.e, condition, KeyBindingHelper.pressed)) {
					KeyBindingHelper.e.consume();
					e.consume();
					System.out.println("key typed 1 " + e.isConsumed());
					return true;
				}
				/* if processing the stored event didn't return true, continue processing this (KEY_TYPED) event */
				consumed = super.processKeyBinding(ks, e, condition, pressed);
				if (consumed)
					e.consume();
				return consumed;
			} else {
				/* else, process the KEY_TYPED event */
				consumed = super.processKeyBinding(ks, e, condition, pressed);
				if (consumed)
					e.consume();
				return consumed;
			}
		} else {
			/* let superclass process the event */
			consumed = super.processKeyBinding(ks, e, condition, pressed);
			if (consumed)
				e.consume();
			return consumed;
		}
	}
	
	protected void configurePropertiesFromAction(Action a) {
		super.configurePropertiesFromAction(a);
		if (a == null)
			return;
		String text = (String) a.getValue("MenuText");
		String shortDescription = (String) a.getValue("ShortDescription");
		String accelerator = (String) a.getValue("Accelerator");
		String mnemonic = (String) a.getValue("Mnemonic");
		String icon = (String) a.getValue("Icon");
		String toolTipText = (String) a.getValue("MenuToolTip");
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
}
