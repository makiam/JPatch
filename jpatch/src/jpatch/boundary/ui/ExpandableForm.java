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

import bsh.This;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.*;

/**
 * @author sascha
 *
 */
public class ExpandableForm extends JComponent {
	private static Icon expandIcon = new ImageIcon(createIconImage(new Color(0x87878f), new Color(0xe7e7ef), 0));
	private static Icon collapseIcon = new ImageIcon(createIconImage(new Color(0x87878f), new Color(0xe7e7ef), 1));
	private static Icon expandRolloverIcon = new ImageIcon(createIconImage(new Color(0xa7a7af), new Color(0xffffff), 0));
	private static Icon collapseRolloverIcon = new ImageIcon(createIconImage(new Color(0xa7a7af), new Color(0xffffff), 1));
	private static final Color borderColor = new Color(0xccccee);
	
	private JToggleButton button = new JToggleButton();
	private int labelWidth, fieldWidth;
	private Dimension layoutSize = new Dimension();
	private boolean expanded;
	private boolean alwaysExpanded;
	private static Insets borderInsets = new Insets(2, 4, 3, 0);
	private static Insets noBorderInsets = new Insets(2, 4, 0, 0);
	
	public ExpandableForm() {
		this(false);
	}
	public ExpandableForm(boolean keepExpanded) {
		alwaysExpanded = keepExpanded;
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				expanded = button.isSelected();
				doLayout();
				repaint();
				button.setPressedIcon(button.isSelected() ? collapseIcon : expandIcon);
				button.setToolTipText(button.isSelected() ? "collapse" : "expand");
			}
		});
		button.setIcon(expandIcon);
		button.setSelectedIcon(collapseIcon);
		button.setRolloverIcon(expandRolloverIcon);
		button.setRolloverSelectedIcon(collapseRolloverIcon);
		button.setPressedIcon(expandIcon);
		button.setToolTipText("expand");
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		add(button);
		
		setLayout(new LayoutManager2() {
			public void addLayoutComponent(String name, Component comp) { }

			public void removeLayoutComponent(Component comp) { }

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
				
				int width = layoutSize.width;
				Insets insets = parent.getInsets();
				int y = insets.top;
				for (Component component : parent.getComponents()) {
					component.setVisible(expanded || alwaysExpanded || y == insets.top || component == button);
					Dimension size = component.getPreferredSize();
					if (component instanceof JLabel) {
						component.setBounds(insets.left + labelWidth - size.width, y + 2, size.width, size.height);
					} else if (component != button) {
						component.setBounds(insets.left + labelWidth + 5, y, fieldWidth, size.height);
						if (expanded || alwaysExpanded || y == insets.top)
							y += size.height;
					}
				}
				button.setBounds(width - 18 - insets.right, insets.top + 1, 16, 16);
				button.setVisible(parent.getComponentCount() > 3 && !alwaysExpanded);
			}

			public void addLayoutComponent(Component comp, Object constraints) {
				System.out.println("addLayoutComponent(" + comp + ", " + constraints + ")");
				Dimension size = comp.getPreferredSize();
				if (comp instanceof JLabel) {
					if (size.width > labelWidth)
						labelWidth = size.width;
				} else if (comp != button) {
					if (size.width > fieldWidth)
						fieldWidth = size.width;
				}
			}

			public float getLayoutAlignmentX(Container target) {
				return 0;
			}

			public float getLayoutAlignmentY(Container target) {
				return 0;
			}

			public void invalidateLayout(Container target) { }
			
			private Dimension computeSize(Container parent) {
				int y = 0;
				for (Component component : parent.getComponents()) {
					if (!(component instanceof JLabel) && component != button) {
						if (expanded || alwaysExpanded || y == 0) {
							y += component.getPreferredSize().height;
						}
					}
				}
				Insets insets = parent.getInsets();
				layoutSize.height = y + insets.top + insets.bottom;
				layoutSize.width = insets.left + labelWidth + fieldWidth + 22;
				return layoutSize;
			}
		});
		
		setBorder(new Border() {
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
//				Graphics2D g2 = (Graphics2D) g.create();
				Color topColor = new Color(0xa7a7af);
				Color bottomColor = new Color(0x87878f);
				
				
//				g2.fillRoundRect(x + 2, y + 2, width - 5, height - 5, 12, 12);
				if (expanded) {
					Graphics2D g2 = (Graphics2D) g.create();
//					
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//					
////					g2.fillRect(x + width - 18, y, 18, height);
//					g2.setPaint(new GradientPaint(x, 0, c.getBackground(), x + width - 13, 0, borderColor));
////					g2.setPaint(new GradientPaint(x, y + 16, c.getBackground(), x, y + height, new Color(0xa7a7af)));
//					g2.drawLine(x, y + height - 1, x + width - 13, y + height - 1);
//					g2.setColor(borderColor);
////					g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 8, 8);
//					g2.setClip(x + width - 13, y + 16, 13, height - 16);
//					g2.fillRoundRect(width - 26, y, 20, height, 12, 12);
					g2.setColor(modifiedColor(c.getBackground(), -48, -48, -32));
					g2.fillRoundRect(x + 1, y + 1, width - 2, height - 2, 8, 8);
					g2.setColor(modifiedColor(c.getBackground(), 4, 4, 6));
					int h = ((Container) c).getComponent(0).getPreferredSize().height;
					g2.fillRoundRect(x + 3, y + h - 4, width - 6, height - h + 1, 4, 4); 
				}
			}

			public Insets getBorderInsets(Component c) {
				return expanded ? borderInsets : noBorderInsets;
			}

			public boolean isBorderOpaque() {
				return false;
			}
		});
	}
	
	private Color modifiedColor(Color c, int r, int g, int b) {
		r += c.getRed();
		g += c.getGreen();
		b += c.getBlue();
		r = r < 0 ? 0 : r > 255 ? 255 : r;
		g = g < 0 ? 0 : g > 255 ? 255 : g;
		b = b < 0 ? 0 : b > 255 ? 255 : b;
		return new Color(r, g, b);	
	}

	public int getFieldWidth() {
		return fieldWidth;
	}

	public void setFieldWidth(int fieldWidth) {
		this.fieldWidth = fieldWidth;
	}

	public int getLabelWidth() {
		return labelWidth;
	}

	public void setLabelWidth(int labelWidth) {
		this.labelWidth = labelWidth;
	}
	
	/**
	 * 
	 * @param fill the fill color
	 * @param stroke the stroke color
	 * @param direction 0 = downward arrow, 1 = upward arrow
	 * @return
	 */
	private static Image createIconImage(Color fill, Color stroke, int direction) {
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(fill);
		g2.fillOval(1, 1, 15, 15);
		g2.setColor(stroke);
		switch (direction) {
		case 0:
			g2.drawPolygon(new int[] { 5, 8, 11, 8}, new int[]{5, 8, 5, 7}, 4);
			g2.drawPolygon(new int[] { 5, 8, 11, 8}, new int[]{9, 12, 9, 11}, 4);
			break;
		case 1:
			g2.drawPolygon(new int[] { 5, 8, 11, 8}, new int[]{7, 4, 7, 5}, 4);
			g2.drawPolygon(new int[] { 5, 8, 11, 8}, new int[]{11, 8, 11, 9}, 4);
			break;
		}
		return image;
	}
}
