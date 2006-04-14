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
import java.beans.*;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * @author sascha
 *
 */
public class JPatchMenu extends JMenu implements PropertyChangeListener {

	/**
	 * 
	 */
	public JPatchMenu() {
		super();
		// TODO Auto-generated constructor stub
		setEnabled(false);
	}

	/**
	 * @param s
	 */
	public JPatchMenu(String s) {
		super(s);
		// TODO Auto-generated constructor stub
		setEnabled(false);
	}

	/**
	 * @param a
	 */
	public JPatchMenu(Action a) {
		super(a);
		// TODO Auto-generated constructor stub
		setEnabled(false);
	}

	/**
	 * @param s
	 * @param b
	 */
	public JPatchMenu(String s, boolean b) {
		super(s, b);
		// TODO Auto-generated constructor stub
		setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		rethinkMenuEnabled();
	}
	
	private void rethinkMenuEnabled() {
		for (Component c : getPopupMenu().getComponents()) {
			if (c.isEnabled()) {
				if (!isEnabled())
					setEnabled(true);
				return;
			}
		}
		if (isEnabled())
			setEnabled(false);
	}

	@Override
	public JMenuItem add(JMenuItem menuItem) {
		super.add(menuItem).addPropertyChangeListener("enabled", this);
		if (!isEnabled() && menuItem.isEnabled())
			setEnabled(true);
		return menuItem;
	}
	
	
	@Override
	public Component add(Component c, int index) {
		super.add(c, index).addPropertyChangeListener("enabled", this);
		if (!isEnabled() && c.isEnabled())
			setEnabled(true);
		return c;
	}

	@Override
	public Component add(Component c) {
		super.add(c).addPropertyChangeListener("enabled", this);
		if (!isEnabled() && c.isEnabled())
			setEnabled(true);
		return c;
	}

	@Override
	public void remove(Component c) {
		c.removePropertyChangeListener("enabled", this);
		super.remove(c);
	}

	@Override
	public void remove(int pos) {
		Component c = getPopupMenu().getComponent(pos);
		if (c != null)
			c.removePropertyChangeListener("enabled", this);
		super.remove(pos);
	}

	@Override
	public void remove(JMenuItem item) {
		item.removePropertyChangeListener("enabled", this);
		super.remove(item);
	}

	@Override
	public void removeAll() {
		for (Component c : getPopupMenu().getComponents())
			c.removePropertyChangeListener("enabled", this);
		super.removeAll();
	}
	
	
}
