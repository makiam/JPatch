package com.jpatch.afw.ui;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class JPatchSplitPane {
	private static final Icon COLLAPSED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/COLLAPSED.png"));
	private static Icon EXPANDED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/EXPANDED.png"));
	private static Insets BORDER_INSETS = new Insets(2, 4, 2, 4);
	private static Border BORDER = new Border() {
		public Insets getBorderInsets(Component c) {
			return BORDER_INSETS;
		}
		public boolean isBorderOpaque() {
			return false;
		}
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			g.setColor(c.getBackground().brighter());
			g.drawLine(x, y, x + width - 2, y);
			g.drawLine(x, y, x, y + height - 2);
			g.setColor(c.getBackground().darker());
			g.drawLine(x + width - 1, 1, x + width - 1, y + height - 1);
			g.drawLine(1, y + height - 1, x + width - 1, y + height - 1);
		}
	};
	
	private List<Item> items = new ArrayList<Item>();
	private Map<JComponent, Item> map = new HashMap<JComponent, Item>();
	
	private JComponent comp = new JPanel() {
		@Override
		public void doLayout() {
			int y = 0;
			int width = getWidth();
			System.out.println("width=" + width);
			for (Item item : items) {
				item.button.setBounds(2, y, item.button.getPreferredSize().width, item.label.getPreferredSize().height);
				item.label.setBounds(2 + item.button.getPreferredSize().width, y, width - 4, item.label.getPreferredSize().height);
				y += item.label.getPreferredSize().height;
				if (item.component.isVisible()) {
					item.component.setBounds(2, y, width - 4, item.component.getPreferredSize().height);
					y += item.component.getPreferredSize().height;
				}
				y += 4;
			}
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);	
			int y = 0;
			int width = getWidth();
			g.setColor(getBackground());
			for (Item item : items) {
				int h = item.label.getPreferredSize().height;
				if (item.component.isVisible()) {
					h += item.component.getPreferredSize().height;
				}
				h += 4;
				g.draw3DRect(0, y, width - 1, h - 1, true);
				y += h;
			}
		}
	};
	
	public JComponent getComponent() {
		return comp;
	}
	
	public void add(JComponent component, String name) {
		Item item = new Item(component, name);
		items.add(item);
		map.put(component, item);
		comp.add(item.button);
		comp.add(item.label);
		comp.add(item.component);
	}
	
	public void remove(JComponent component) {
		Item item = map.get(component);
		if (item != null) {
			items.remove(item);
			comp.remove(item.button);
			comp.remove(item.label);
			comp.remove(item.component);
		}
	}
	
	private class Item {
		JToggleButton button;
		JLabel label;
		JComponent component;
		
		Item(JComponent component, String name) {
			this.component = component;
			this.label = new JLabel(name);
			button = new JToggleButton();
			button.setPreferredSize(new Dimension(COLLAPSED_ICON.getIconWidth(), COLLAPSED_ICON.getIconHeight()));
			button.setContentAreaFilled(false);
			button.setBorderPainted(false);
			button.setBorder(null);
			button.setIcon(COLLAPSED_ICON);
			button.setSelectedIcon(EXPANDED_ICON);
			button.setSelected(true);
			button.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					Item.this.component.setVisible(Item.this.button.isSelected());
					comp.doLayout();
				}
			});
		}
	}
}
