package com.jpatch.afw.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class JPatchFormContainer {
	static final int MAX_NEST_LEVEL = 3;
	private static final Font LABEL_FONT = new Font("sans-serif", Font.BOLD, 12);
	private static final Icon EXPANDED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/EXPANDED.png"));
	private static final Icon COLLAPSED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/COLLAPSED.png"));
	private static final Insets EXPANDED_INSETS = new Insets(0, 4, 4, 4);
	private static final Insets COLLAPSED_INSETS = new Insets(0, 4, 2, 4);
	private final JComponent component = Box.createVerticalBox();
	private final JPanel titleBar = new JPanel(new BorderLayout());
	private final Component strut = Box.createVerticalStrut(2);
	private final Box formBox = Box.createVerticalBox();
	private final Box containerBox = Box.createVerticalBox();
	private JPatchFormContainer parentContainer;
	private boolean expanded;
	private static final Color[] BORDER_COLORS = new Color[] {
		new Color(0x888888),
		new Color(0xaaaaaa),
		new Color(0xcccccc)
	};
	public JPatchFormContainer(String title) {
		JToggleButton button = new JToggleButton();
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setExpanded(((JToggleButton) e.getSource()).isSelected());
			}
		});
		button.setContentAreaFilled(false);
		button.setBorder(null);
		button.setIcon(COLLAPSED_ICON);
		button.setSelectedIcon(EXPANDED_ICON);
		titleBar.add(button, BorderLayout.WEST);
		JLabel label = new JLabel(title);
		label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
		label.setFont(LABEL_FONT);
		titleBar.add(label, BorderLayout.CENTER);
		titleBar.setOpaque(false);
//		titleBar.setBackground(BORDER_COLORS[0]);
		component.add(titleBar);
		component.setBorder(new Border() {
			private final Insets insets = new Insets(0, 4, 0, 4);
			public Insets getBorderInsets(Component c) {
				return expanded ? EXPANDED_INSETS : COLLAPSED_INSETS;
			}

			public boolean isBorderOpaque() {
				// TODO Auto-generated method stub
				return false;
			}

			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(BORDER_COLORS[getLevel() - 1]);
				g2.fillRoundRect(x, y + 1, width, 16, 8, 8);
				if (expanded) {
//					g.fillRect(x, y, 2, height - 4);
//					g.fillRect(x + width - 2, y, 2, height - 4);
//					g.fillRect(x, y + height - 4, width, 2);
//					g2.setStroke(new BasicStroke(2));
					g2.drawRoundRect(x, y + 14 + 1, width - 1, height - 3 - 14, 8, 8);
					g2.drawRoundRect(x + 1, y + 15 + 1, width - 3, height - 5 - 14, 6, 6);
					g2.fillRect(x, 12, 2, 8);
					g2.fillRect(x + width - 2, 12, 2, 8);
				}
			}
			
		});
	}
	
	public JComponent getComponent() {
		return component;
	}
	
	public void add(JPatchForm form) {
		formBox.add(form.getComponent());
		form.setFormContainer(this);
	}
	
	public void add(JPatchFormContainer formContainer) {
		containerBox.add(formContainer.getComponent());
		formContainer.parentContainer = this;
	}
	
	public void remove(JPatchForm form) {
		formBox.remove(form.getComponent());
	}
	
	public void remove(JPatchFormContainer formContainer) {
		containerBox.remove(formContainer.getComponent());
	}
	
	public void setExpanded(boolean expanded) {
		if (!this.expanded && expanded) {
			this.expanded = true;
			component.add(strut);
			component.add(formBox);
			component.add(containerBox);
			getRootContainer().component.getParent().validate();
		} else if (this.expanded && !expanded) {
			this.expanded = false;
			component.remove(strut);
			component.remove(formBox);
			component.remove(containerBox);
			getRootContainer().component.getParent().validate();
		}
	}
	
	private JPatchFormContainer getRootContainer() {
		return parentContainer == null ? this : parentContainer.getRootContainer();
	}
	
	int getLevel() {
		return getLevel(1);
	}
	
	private int getLevel(int level) {
		return parentContainer == null ? level : parentContainer.getLevel(++level);
	}
}
