package com.jpatch.afw.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

public class JPatchForm {
	private static final Insets LABEL_INSETS = new Insets(0, 0, 0, 4);
	private static final Insets INSETS = new Insets(1, 1, 1, 1);
	private static final Dimension LABEL_SIZE = new Dimension(100, 20);
	private static final Dimension SLIDER_SIZE = new Dimension(20, 20);
	private final JComponent component = new JPanel(new ColumnLayout());
//		@Override
//		public void doLayout() {
//			int level = container.getLevel();
//			BORDER_INSETS.left = BORDER_INSETS.right = (JPatchFormContainer.MAX_NEST_LEVEL - level) * 4;
//			super.doLayout();
//		}
//	};
	
	private int row = 0;
	private JPatchFormContainer container;
	private final Insets BORDER_INSETS = new Insets(0, 8, 0, 8);
	
	public JPatchForm() {
		component.setBorder(new Border() {
			public Insets getBorderInsets(Component c) {
				return BORDER_INSETS;
			}
			public boolean isBorderOpaque() {
				return false;
			}
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) { }
		});
	}
	
	
	public JComponent getComponent() {
		return component;
	}
	
	public void addRow(JLabel label, JComponent... components) {
		JComponent row = new JPanel(new RowLayout());
		row.add(label);
		for (JComponent c : components) {
			row.add(c);
		}
		component.add(row);
//		GridBagConstraints gbc = new GridBagConstraints(0, row, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, LABEL_INSETS, 0, 0);
//		label.setMinimumSize(LABEL_SIZE);
//		label.setPreferredSize(LABEL_SIZE);
//		label.setMaximumSize(LABEL_SIZE);
//		component.add(label, gbc);
//		for (JComponent c : components) {
//			/* set equal preferred sizes for all components (metal combobox would be too high otherwise) */
//			if (getFill(c) == GridBagConstraints.HORIZONTAL) {
//				c.setPreferredSize(SLIDER_SIZE);
//			}
//		}
//		if (components.length == 3) {
//			/* add three components to the grid */
//			for (int i = 0; i < 3; i++) {
//				gbc = new GridBagConstraints(1 + i, row, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, getFill(components[i]), INSETS, 0, 0);
//				component.add(components[i], gbc);
//			}
//		} else if (components.length == 2) {
//			/* add two components to the grid */
//			/* add the first component */
//			gbc = new GridBagConstraints(1, row, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, getFill(components[0]), INSETS, 0, 0);
//			component.add(components[0], gbc);
//			if (components[1] instanceof JSlider) {
//				/* if the right component is a slider, fill the right side of the grid with the slider */
//				gbc = new GridBagConstraints(2, row, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, getFill(components[1]), INSETS, 0, 0);
//				components[1].setPreferredSize(SLIDER_SIZE);
//			} else {
//				gbc = new GridBagConstraints(2, row, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, getFill(components[0]), INSETS, 0, 0);
//			}
//			component.add(components[1], gbc);
//		} else if (components.length == 1) {
//			/* add a single component to the grid (filling the entire right side) */
////			int fill = getFill(components[0]);
////			int width = (fill == GridBagConstraints.HORIZONTAL) ? 3 : 1;
////			components[0].setPreferredSize(SLIDER_SIZE);
//			gbc = new GridBagConstraints(1, row, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, INSETS, 0, 0);
//			component.add(components[0], gbc);
//		} else {
//			throw new IllegalArgumentException("Can't layout " + components.length + " elements");
//		}
//		row++;
	}
	
	private static int getFill(JComponent component) {
//		if (true) return GridBagConstraints.HORIZONTAL;
		if (component instanceof JCheckBox || component instanceof JButton) {
			return GridBagConstraints.NONE;
		} else {
			return GridBagConstraints.HORIZONTAL;
		}
	}
	
	void setFormContainer(JPatchFormContainer container) {
		this.container = container;
	}
	
	private class ColumnLayout implements LayoutManager2 {
		private Dimension layoutSize = new Dimension();
		
		public void addLayoutComponent(Component comp, Object constraints) {
			// TODO Auto-generated method stub
			
		}

		public float getLayoutAlignmentX(Container target) {
			// TODO Auto-generated method stub
			return 0;
		}

		public float getLayoutAlignmentY(Container target) {
			// TODO Auto-generated method stub
			return 0;
		}

		public void invalidateLayout(Container target) {
			// TODO Auto-generated method stub
			
		}

		public Dimension maximumLayoutSize(Container target) {
			return computeSize(target);
		}

		public void addLayoutComponent(String name, Component comp) {
			// TODO Auto-generated method stub
			
		}

		public void layoutContainer(Container parent) {
			int insets = JPatchFormContainer.MAX_NEST_LEVEL - container.getLevel();
			int y = 2, w = parent.getSize().width - 8 * insets;
			for (Component c : parent.getComponents()) {
				int height = c.getPreferredSize().height;
				c.setBounds(4 * insets, y, w, height);
				y += height + 1;
			}
		}

		public Dimension minimumLayoutSize(Container parent) {
			return computeSize(parent);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return computeSize(parent);
		}

		public void removeLayoutComponent(Component comp) {
			// TODO Auto-generated method stub
			
		}
		
		private Dimension computeSize(Container parent) {
			int width = 0, height = 2;
			for (Component c : parent.getComponents()) {
				Dimension size = c.getPreferredSize();
				height += size.height + 1;
				width = Math.max(width, size.width);
			}
			layoutSize.height = parent.getComponentCount() > 0 ? height + 1 : height;
			layoutSize.width = width;
//			System.out.println("column-size:" + layoutSize);
			return layoutSize;
		}
	}
	
	private class RowLayout implements LayoutManager2 {
		private Dimension layoutSize = new Dimension();
		
		public void addLayoutComponent(Component comp, Object constraints) {
			// TODO Auto-generated method stub
			
		}

		public float getLayoutAlignmentX(Container target) {
			// TODO Auto-generated method stub
			return 0;
		}

		public float getLayoutAlignmentY(Container target) {
			// TODO Auto-generated method stub
			return 0;
		}

		public void invalidateLayout(Container target) {
			// TODO Auto-generated method stub
			
		}

		public Dimension maximumLayoutSize(Container target) {
			return computeSize(target);
		}

		public void addLayoutComponent(String name, Component comp) {
			// TODO Auto-generated method stub
			
		}

		public void layoutContainer(Container parent) {
			int maxHeight = 0;
			for (Component c : parent.getComponents()) {
				int height = c.getPreferredSize().height;
				if (height > maxHeight) {
					maxHeight = height;
				}
			}
//			System.out.println("maxHeight = " + maxHeight);
			int start = LABEL_SIZE.width, width = parent.getSize().width - start;
			setComponentBounds(parent.getComponent(0), 0, 0, start, maxHeight, false, false);
			for (int i = 0, n = parent.getComponentCount() - 1; i < n; i++) {
				Component c = parent.getComponent(i + 1);
				int d = n;
				if (n == 2) {
					d = 2;
				} else if (n == 1) {
					d = c instanceof JCheckBox ? 3 : 1;
				}
				boolean fill = !(c instanceof JCheckBox || c instanceof JButton || c instanceof Box);
				int w = c instanceof JSlider ? width - (i * width / d) : (i + 1) * width / d - (i * width / d);
				int y = c instanceof Box ? -1 : 0;
				setComponentBounds(c, start + (i * width / d), y, w + 1, maxHeight, true, fill);
			}
		}

		public Dimension minimumLayoutSize(Container parent) {
			return computeSize(parent);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return computeSize(parent);
		}

		public void removeLayoutComponent(Component comp) {
			// TODO Auto-generated method stub
			
		}
		
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
			layoutSize.width = Short.MAX_VALUE;
//			layoutSize.width = 300;//JPatchForm.this.container.getRootContainer().getComponent().getRootPane().getWidth();
//			System.out.println("row-size:" + layoutSize);
			return layoutSize;
		}
		
		private void setComponentBounds(Component c, int x, int y, int maxWidth, int maxHeight, boolean center, boolean fill) {
			Dimension size = c.getPreferredSize();
			int w = Math.min(size.width, maxWidth - 2);
			int h = Math.min(size.height, maxHeight);
			if (fill) {
				c.setBounds(x + 1, y + (maxHeight - h) / 2, maxWidth - 2, h);
			} else if (center) {
				c.setBounds(x + (maxWidth - w) / 2, y + (maxHeight - h) / 2 + 1, w, h);
			} else {
				c.setBounds(x + 1, y + (maxHeight - h) / 2, w, h);
			}
		}
	}
}
