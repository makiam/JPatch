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
package jpatch.boundary.action;

import java.awt.event.ActionEvent;

import javax.swing.*;

import jpatch.boundary.JPatchScreen;
import jpatch.boundary.MainFrame;

/**
 * @author sascha
 *
 */
public class ViewportModeAction extends AbstractAction {
	public static enum Mode { SINGLE, VERTICAL_SPLIT, HORIZONTAL_SPLIT, QUAD }
	private Mode mode;
	/**
	 * 
	 */
	public ViewportModeAction(Mode mode) {
		this.mode = mode;
	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		switch (mode) {
		case SINGLE:
			MainFrame.getInstance().getJPatchScreen().resetMode(JPatchScreen.SINGLE);
			break;
		case VERTICAL_SPLIT:
			MainFrame.getInstance().getJPatchScreen().resetMode(JPatchScreen.VERTICAL_SPLIT);
			break;
		case HORIZONTAL_SPLIT:
			MainFrame.getInstance().getJPatchScreen().resetMode(JPatchScreen.HORIZONTAL_SPLIT);
			break;
		case QUAD:
			MainFrame.getInstance().getJPatchScreen().resetMode(JPatchScreen.QUAD);
			break;
		}
	}
}
