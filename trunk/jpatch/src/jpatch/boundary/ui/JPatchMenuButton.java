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
	
	/**
	 * @param buttonModel
	 */
	public JPatchMenuButton(MenuButtonModel buttonModel) {
		super(buttonModel);
	}

	public void setPopupMenu(JPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
		popupMenu.addPopupMenuListener(this);
	}
	
	@Override
	protected void configurePropertiesFromAction(Action a) {
		super.configurePropertiesFromAction(a);
//		for (ActionListener actionListener : getActionListeners())
//			removeActionListener(actionListener);
		addActionListener(this);
		setIcons();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (!isSelected())
			popupMenu.setVisible(false);
		else if (System.currentTimeMillis() > hideTime + 250)	// FIXME: This is a quick'n'dirty hack and should be corrected
			popupMenu.show(this, 0, getHeight());
		else
			setSelected(false);
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent)
	 */
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		setSelected(true);
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent)
	 */
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		setSelected(false);
		hideTime = System.currentTimeMillis();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event.PopupMenuEvent)
	 */
	public void popupMenuCanceled(PopupMenuEvent e) { }
	
	private void setIcons() {
		if (getIcon() != null)
			setIcon(pulldownIcon(getIcon()));
		if (getSelectedIcon() != null && getSelectedIcon() != getIcon())
			setSelectedIcon(pulldownIcon(getSelectedIcon()));
//		if (getRolloverIcon() != null)
//			setRolloverIcon(pulldownIcon(getRolloverIcon()));
//		if (getRolloverSelectedIcon() != null)
//			setRolloverSelectedIcon(pulldownIcon(getRolloverSelectedIcon()));
//		if (getDisabledIcon() != null)
//			setDisabledIcon(pulldownIcon(getDisabledIcon()));
//		if (getDisabledSelectedIcon() != null)
//			setDisabledSelectedIcon(pulldownIcon(getDisabledSelectedIcon()));	
	}
	
	private ImageIcon pulldownIcon(Icon icon) {
		Image i;
		if (icon instanceof ImageIcon) {
			i = ((ImageIcon) icon).getImage();
		} else {
			i = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			icon.paintIcon(null, i.getGraphics(), 0, 0);
		}
		int w = i.getWidth(null);
		int h = i.getHeight(null);
		BufferedImage image = new BufferedImage(w + 9, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.drawImage(i, 0, 0, null);
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.BLACK);
		g2.fillPolygon(new int[] { w + 1, w + 8, w + 4 }, new int[] { h/2 - 2, h/2 - 2, h/2 + 2}, 3);
		return new ImageIcon(image);
	}
	
	public static class MenuButtonModel extends JToggleButton.ToggleButtonModel { }
}
