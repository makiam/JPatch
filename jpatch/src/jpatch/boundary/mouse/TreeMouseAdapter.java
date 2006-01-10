/*
 * $Id: TreeMouseAdapter.java,v 1.1 2006/01/10 15:46:50 sascha_l Exp $
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
package jpatch.boundary.mouse;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import jpatch.boundary.*;
import jpatch.entity.*;

/**
 * @author sascha
 *
 */
public class TreeMouseAdapter extends JPatchMouseAdapter {
	
//	private int x;
	private int dx;
	private Morph morph;
	private Rectangle bounds;
	
	public TreeMouseAdapter() {
	}
	
	public void mousePressed(MouseEvent event) {
		JTree tree = (JTree) event.getSource();
		TreePath path = tree.getPathForLocation(event.getX(), event.getY());
		if (path == null)
			return;
		Object element = path.getLastPathComponent();
		int row = tree.getRowForLocation(event.getX(), event.getY());
		if (element instanceof Morph) {
			morph = (Morph) element;
			bounds = tree.getRowBounds(row);
			dx = bounds.x + 20;
			int x = event.getX() - dx;
			int s = morph.getSliderValue();
			if (x > s - 8 && x < s + 8)
				tree.addMouseMotionListener(this);
		}
	}
	
	public void mouseReleased(MouseEvent event) {
		JTree tree = (JTree) event.getSource();
		tree.removeMouseMotionListener(this);
	}
	
	public void mouseDragged(MouseEvent event) {
		JTree tree = (JTree) event.getSource();
		morph.setSliderValue(event.getX() - dx);
		tree.repaint(bounds);
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}
