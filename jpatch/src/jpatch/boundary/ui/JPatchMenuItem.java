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

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

/**
 * @author sascha
 *
 */
public class JPatchMenuItem extends JMenuItem {

	/**
	 * 
	 */
	public JPatchMenuItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param icon
	 */
	public JPatchMenuItem(Icon icon) {
		super(icon);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param text
	 */
	public JPatchMenuItem(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param a
	 */
	public JPatchMenuItem(Action a) {
		super();
		setAction(a);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param text
	 * @param icon
	 */
	public JPatchMenuItem(String text, Icon icon) {
		super(text, icon);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param text
	 * @param mnemonic
	 */
	public JPatchMenuItem(String text, int mnemonic) {
		super(text, mnemonic);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void configurePropertiesFromAction(Action a) {
		super.configurePropertiesFromAction(a);
		if (a == null)
			return;
		String menuText = (String) a.getValue("MenuText");
		String mnemonic = (String) a.getValue("Mnemonic");
		setText(menuText);
		if (mnemonic != null)
			setMnemonic(mnemonic.charAt(0));
	}
}
