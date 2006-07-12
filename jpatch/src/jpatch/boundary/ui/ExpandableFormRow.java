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
package jpatch.boundary.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.*;

/**
 * @author sascha
 *
 */
public class ExpandableFormRow extends JComponent {
	private Dimension layoutSize = new Dimension();
	
	public ExpandableFormRow() {
		
		setLayout(new LayoutManager2() {
			public void addLayoutComponent(String name, Component comp) { }

			public void removeLayoutComponent(Component comp) { }

			public Dimension preferredLayoutSize(Container parent) {
				return computeSize(parent);
			}

			public Dimension minimumLayoutSize(Container parent) {
				return computeSize(parent);
			}

			public Dimension maximumLayoutSize(Container target) {
				return computeSize(target);
			}
			
			public void layoutContainer(Container parent) {
				int width = parent.getSize().width;
				int n = parent.getComponentCount();
				int cellWidth = (width - 2) / n + 1;
				int cell = 0;
				for (Component c : parent.getComponents()) {
					Dimension size = c.getPreferredSize();
					c.setBounds(cellWidth * cell + (cellWidth - size.width) / 2, (layoutSize.height - size.height) / 2, size.width, size.height);
					cell++;
				}
			}

			public void addLayoutComponent(Component comp, Object constraints) { }

			public float getLayoutAlignmentX(Container target) {
				return 0;
			}

			public float getLayoutAlignmentY(Container target) {
				return 0;
			}

			public void invalidateLayout(Container target) { }
			
			private Dimension computeSize(Container parent) {
				int w = 0;
				int height = 0;
				for (Component c : parent.getComponents()) {
					Dimension size = c.getPreferredSize();
					w += size.width + 2;
					if (size.height > height)
						height = size.height;
				}
				layoutSize.height = height;
				layoutSize.width = w;
				return layoutSize;
			}
		});
	}
	
//	@Override
//	public Dimension getMinimumSize() {
//		return layoutSize;
//	}
//	
//	@Override
//	public Dimension getMaximumSize() {
//		return layoutSize;
//	}
//	
//	@Override
//	public Dimension getPreferredSize() {
//		return layoutSize;
//	}
}
