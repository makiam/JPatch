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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * @author sascha
 *
 */
public class ExpandableFormContainer extends JComponent {
	private Dimension layoutSize = new Dimension();
	private int labelWidth;
	private int fieldWidth;
	
	public ExpandableFormContainer() {
		setLayout(new LayoutManager2() {

			public void addLayoutComponent(Component comp, Object constraints) {
				
			}

			public float getLayoutAlignmentX(Container target) {
				return 0;
			}

			public float getLayoutAlignmentY(Container target) {
				return 0;
			}

			public void invalidateLayout(Container target) {
			}

			public void addLayoutComponent(String name, Component comp) {
				
			}

			public void removeLayoutComponent(Component comp) {
				
			}

			public Dimension preferredLayoutSize(Container parent) {
				return computeSize(parent);
			}

			public Dimension minimumLayoutSize(Container parent) {
				return computeSize(parent);
			}

			public Dimension maximumLayoutSize(Container parent) {
				return computeSize(parent);
			}
			
			public void layoutContainer(Container parent) {
				computeSize(parent);
				int y = 0;
				for (Component component : parent.getComponents()) {
					if (component instanceof ExpandableForm) {
						Dimension size = component.getPreferredSize();
						component.setBounds(0, y, size.width, size.height);
						y += size.height;
					}
				}
			}
			
			private Dimension computeSize(Container parent) {
				labelWidth = 0;
				fieldWidth = 0;
				for (Component component : parent.getComponents()) {
					if (component instanceof ExpandableForm) {
						int lw = ((ExpandableForm) component).getLabelWidth();
						int fw = ((ExpandableForm) component).getFieldWidth();
						if (lw > labelWidth)
							labelWidth = lw;
						if (fw > fieldWidth)
							fieldWidth = fw;
					}
				}
				int y = 0;
				for (Component component : parent.getComponents()) {
					if (component instanceof ExpandableForm) {
						((ExpandableForm) component).setLabelWidth(labelWidth);
						((ExpandableForm) component).setFieldWidth(fieldWidth);
						y += component.getPreferredSize().height;
					}
				}
				System.out.println(labelWidth + " " + fieldWidth + " " + layoutSize);
				layoutSize.width = 4 + labelWidth + fieldWidth + 22;
				layoutSize.height = y;
				return layoutSize;
			}
		});
	}
}
