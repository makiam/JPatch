/*
 * $Id$
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.boundary.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * This class is used to store the KeyStroke, KeyEvent, condition and pressed values
 * when processing KeyBindings.
 * @see JPatchRadioButtonMenuItem
 * @author sascha
 */
public class KeyBindingHelper {
	/* KeyStroke */
	static KeyStroke ks;
	/* KeyEvent */
	static KeyEvent e;
	/* pressed */
	static boolean pressed;
	
	static List<Listener> listeners = new ArrayList<Listener>();
	
	static {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(new KeyEventPostProcessor() {
			public boolean postProcessKeyEvent(KeyEvent e) {
				if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
					/* if it has a character assigned, store it */
					KeyBindingHelper.ks = KeyStroke.getKeyStrokeForEvent(e);
					KeyBindingHelper.e = e;
//					KeyBindingHelper.condition = condition;
					KeyBindingHelper.pressed = e.getID() == KeyEvent.KEY_PRESSED;
				} else if (e.getID() == KeyEvent.KEY_TYPED && KeyBindingHelper.e != null && e.getKeyChar() == KeyBindingHelper.e.getKeyChar() && !e.isConsumed()) {
					for (Listener listener : listeners) {
						if (listener.callBack.reprocessKeyBinding(KeyBindingHelper.ks, KeyBindingHelper.e, listener.condition, KeyBindingHelper.pressed))
							break;								
					}
					listeners.clear();
				}
				return false;
			}
		});
	}
	
	static void registerCallback(CallBack callBack, int condition) {
		listeners.add(new Listener(callBack, condition));
	}
	
	static interface CallBack {
		boolean reprocessKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed);
	}
	
	static class Listener {
		CallBack callBack;
		int condition;
		
		Listener(CallBack callBack, int condition) {
			this.callBack = callBack;
			this.condition = condition;
		}
	}
}
