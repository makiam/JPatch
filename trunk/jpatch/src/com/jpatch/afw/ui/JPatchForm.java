package com.jpatch.afw.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

public class JPatchForm {
	private static final Insets LABEL_INSETS = new Insets(0, 0, 0, 4);
	private static final Insets INSETS = new Insets(1, 1, 1, 1);
	private static final Dimension LABEL_SIZE = new Dimension(80, 20);
	private static final Dimension SLIDER_SIZE = new Dimension(20, 20);
	private final JComponent component = new JPanel(new GridBagLayout()) {
		@Override
		public void doLayout() {
			int level = container.getLevel();
			System.out.println(level);
			BORDER_INSETS.left = BORDER_INSETS.right = (JPatchFormContainer.MAX_NEST_LEVEL - level) * 4;
			super.doLayout();
		}
	};
	
	private int row = 0;
	private JPatchFormContainer container;
	private final Insets BORDER_INSETS = new Insets(8, 8, 8, 8);
	
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
		GridBagConstraints gbc = new GridBagConstraints(0, row, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, LABEL_INSETS, 0, 0);
		label.setMinimumSize(LABEL_SIZE);
		label.setPreferredSize(LABEL_SIZE);
		label.setMaximumSize(LABEL_SIZE);
		component.add(label, gbc);
//		for (JComponent c : components) {
//			/* set equal preferred sizes for all components (metal combobox would be too high otherwise) */
//			if (getFill(c) == GridBagConstraints.HORIZONTAL) {
//				c.setPreferredSize(SLIDER_SIZE);
//			}
//		}
		if (components.length == 3) {
			/* add three components to the grid */
			for (int i = 0; i < 3; i++) {
				gbc = new GridBagConstraints(1 + i, row, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, getFill(components[i]), INSETS, 0, 0);
				component.add(components[i], gbc);
			}
		} else if (components.length == 2) {
			/* add two components to the grid */
			/* add the first component */
			gbc = new GridBagConstraints(1, row, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, getFill(components[0]), INSETS, 0, 0);
			component.add(components[0], gbc);
			if (components[1] instanceof JSlider) {
				/* if the right component is a slider, fill the right side of the grid with the slider */
				gbc = new GridBagConstraints(2, row, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, getFill(components[1]), INSETS, 0, 0);
				components[1].setPreferredSize(SLIDER_SIZE);
			} else {
				gbc = new GridBagConstraints(2, row, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, getFill(components[0]), INSETS, 0, 0);
			}
			component.add(components[1], gbc);
		} else if (components.length == 1) {
			/* add a single component to the grid (filling the entire right side) */
//			int fill = getFill(components[0]);
//			int width = (fill == GridBagConstraints.HORIZONTAL) ? 3 : 1;
//			components[0].setPreferredSize(SLIDER_SIZE);
			gbc = new GridBagConstraints(1, row, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, INSETS, 0, 0);
			component.add(components[0], gbc);
		} else {
			throw new IllegalArgumentException("Can't layout " + components.length + " elements");
		}
		row++;
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
}
