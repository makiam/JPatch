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
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * @author sascha
 *
 */
public class JPatchMenuButton extends JPatchToggleButton implements ActionListener, PopupMenuListener {
	private JPopupMenu popupMenu;
	private long hideTime;
	private Icon icon;
	private Icon pulldownRolloverIcon;
	private boolean pulldown = false;
	private Action action;
	
	/**
	 * @param buttonModel
	 */
	public JPatchMenuButton(MenuButtonModel buttonModel) {
		super(buttonModel);
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (pulldown && e.getX() < getWidth() - 13) {
					pulldown = false;
					setIcon(icon);
					repaint();
				} else if (!pulldown && e.getX() >= getWidth() -13) {
					pulldown = true;
					setIcon(pulldownRolloverIcon);
					repaint();
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (!pulldown && e.getX() >= getWidth() -13) {
					pulldown = true;
					setIcon(pulldownRolloverIcon);
					repaint();
				}
			}
			@Override
			public void mouseExited(MouseEvent e) {
				if (pulldown) {
					pulldown = false;
					setIcon(icon);
					repaint();
				}
			}
			
		});
		addActionListener(this);
	}

	public void setPopupMenu(JPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
		popupMenu.addPopupMenuListener(this);
	}

	
	@Override
	public void setAction(Action a) {
		super.setAction(a);
		listenerList.remove(ActionListener.class, a);
	}

	@Override
	protected void configurePropertiesFromAction(Action a) {
		super.configurePropertiesFromAction(a);
		setIcons();
		action = a;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (System.currentTimeMillis() > hideTime + 150) {	// FIXME: this is a little dirty
			if (pulldown) {
				popupMenu.show(this, 0, getHeight());
			} else {
				action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, e.getActionCommand(), e.getWhen(), e.getModifiers()));
				setSelected(false);
			}
		} else {
			setSelected(false);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent)
	 */
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		setSelected(true);
		removeActionListener(this);
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent)
	 */
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		setSelected(false);
		hideTime = System.currentTimeMillis();
		addActionListener(this);
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event.PopupMenuEvent)
	 */
	public void popupMenuCanceled(PopupMenuEvent e) { }
	
	private void setIcons() {
		icon = pulldownIcon(getIcon(), false);
		pulldownRolloverIcon = pulldownIcon(getIcon(), true);
		setIcon(icon);
	}
	
	private ImageIcon pulldownIcon(Icon icon, boolean highlight) {
		Image i;
		if (icon instanceof ImageIcon) {
			i = ((ImageIcon) icon).getImage();
		} else {
			i = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			icon.paintIcon(null, i.getGraphics(), 0, 0);
		}
		int w = i.getWidth(null);
		int h = i.getHeight(null);
		BufferedImage image = new BufferedImage(w + 11, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.drawImage(i, 0, 0, null);
		if (highlight) {
			g2.setColor(Color.WHITE);
			g2.fillPolygon(new int[] { w + 3, w + 10, w + 6 }, new int[] { h/2 - 2, h/2 - 2, h/2 + 2}, 3);
			g2.setColor(Color.BLACK);
			g2.drawPolygon(new int[] { w + 3, w + 9, w + 6 }, new int[] { h/2 - 2, h/2 - 2, h/2 + 1}, 3);
		} else {
			g2.setColor(Color.BLACK);
			g2.fillPolygon(new int[] { w + 3, w + 10, w + 6 }, new int[] { h/2 - 2, h/2 - 2, h/2 + 2}, 3);
		}
		return new ImageIcon(image);
	}
	
	public static class MenuButtonModel extends JToggleButton.ToggleButtonModel { }
}
