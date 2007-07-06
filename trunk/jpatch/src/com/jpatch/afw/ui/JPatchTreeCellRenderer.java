/*
 * $Id:$
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
package com.jpatch.afw.ui;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

import jpatch.entity.*;

/**
 * @author sascha
 *
 */
@SuppressWarnings("serial")
public class JPatchTreeCellRenderer extends DefaultTreeCellRenderer {
	private static final Map<Object, Icon> iconMap = new HashMap<Object, Icon>();
	private static final Icon projectOpenIcon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/icons_16x16/folder_open.png"));
	/*
	 * initialize iconMap
	 */
	static {
		iconMap.put(Project.class, new ImageIcon(ClassLoader.getSystemResource("jpatch/images/icons_16x16/folder_closed.png")));
		iconMap.put(Object.class, new ImageIcon(ClassLoader.getSystemResource("jpatch/images/icons_16x16/unknown.png")));
		iconMap.put(TransformNode.class, new ImageIcon(ClassLoader.getSystemResource("jpatch/images/icons_16x16/transformNode.png")));
		iconMap.put(Model.class, new ImageIcon(ClassLoader.getSystemResource("jpatch/images/icons_16x16/model.png")));
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		/* initialze label by calling superclass method */
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		JPatchTreeNode node = (JPatchTreeNode) value;		// node needs to be a JPatchTreeNode
		Object userObject = node.getUserObject();
		setText(node.getName());							// set the label text
		if (userObject != null) {
			if (userObject instanceof Project && ((Project) userObject).isOpen()) {
				setIcon(projectOpenIcon);
			} else {
				Class objectClass = userObject.getClass();
				Icon icon = null;
				while (icon == null) {
					icon = iconMap.get(objectClass);
					objectClass = objectClass.getSuperclass();
				}
				setIcon(icon);
			}
		}
		return this;
	}
}
