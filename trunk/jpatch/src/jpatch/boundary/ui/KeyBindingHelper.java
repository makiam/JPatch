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

import java.awt.event.*;
import javax.swing.*;

/**
 * This class is used to store the KeyStroke, KeyEvent, condition and pressed values
 * when processing KeyBindings.
 * @see JPatchRadioButtonMenuItem
 * @author sascha
 */
class KeyBindingHelper {
	/* KeyStroke */
	static KeyStroke ks;
	/* KeyEvent */
	static KeyEvent e;
	/* condition (can be either JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, JComponent.WHEN_FOCUSED or JComponent.WHEN_IN_FOCUSED_WINDOW) */
	static int condition;
	/* pressed */
	static boolean pressed;
}
