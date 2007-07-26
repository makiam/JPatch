package com.jpatch.afw.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class JPatchFormContainer {
	static final int MAX_NEST_LEVEL = 4;
	private static final Font LABEL_FONT = new Font("sans-serif", Font.BOLD, 12);
	private static final Icon EXPANDED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/EXPANDED.png"));
	private static final Icon COLLAPSED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/COLLAPSED.png"));
	private static final Insets EXPANDED_INSETS = new Insets(0, 4, 4, 4);
	private static final Insets COLLAPSED_INSETS = new Insets(0, 4, 2, 4);
	private final JComponent component = Box.createVerticalBox();
	private final JComponent titleBar = new JPanel(new BorderLayout());
	private final Box formBox = Box.createVerticalBox();
	private final Box containerBox = Box.createVerticalBox();
	private JPatchFormContainer parentContainer;
	private boolean expanded;
	private Color borderColor = new Color(0x808080);
	
	public JPatchFormContainer(String title) {
		JToggleButton button = new JToggleButton("abc");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setExpanded(((JToggleButton) e.getSource()).isSelected());
			}
		});
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setBorder(null);
		button.setIcon(COLLAPSED_ICON);
		button.setSelectedIcon(EXPANDED_ICON);
		button.setFocusable(false);
		button.setText(title);
		titleBar.add(button, BorderLayout.WEST);
		button.setFont(LABEL_FONT);
		titleBar.setOpaque(false);
		component.add(titleBar);
		component.setBorder(new Border() {
			private final Insets insets = new Insets(0, 4, 0, 4);
			public Insets getBorderInsets(Component c) {
				return expanded ? EXPANDED_INSETS : COLLAPSED_INSETS;
			}

			public boolean isBorderOpaque() {
				return false;
			}

			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				int h = titleBar.getHeight();
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(borderColor);
				g2.fillRoundRect(x, y + 0, width, h, 8, 8);
				g2.drawRoundRect(x, y + 0, width - 1, h - 1, 8, 8);
				if (expanded) {
					g2.drawRoundRect(x, y + h - 2, width - 1, height - h - 1, 8, 8);
					g2.drawRoundRect(x + 1, y + h - 1, width - 3, height - h - 3, 6, 6);
					g2.fillRect(x, 11, 2, 8);
					g2.fillRect(x + width - 2, 11, 2, 8);
				}
			}
			
		});
	}
	
	public JComponent getComponent() {
		return component;
	}
	
	public void setRootBorderColor(Color color) {
		borderColor = color;
	}
	
	public void add(JPatchForm form) {
		System.out.println("add " + form);
		formBox.add(form.getComponent());
		form.setFormContainer(this);	
	}
	
	public void add(JPatchFormContainer formContainer) {
		containerBox.add(formContainer.getComponent());
		formContainer.parentContainer = this;
		Color background = UIManager.getColor("Panel.background");
		int t = 255 - 100;
		formContainer.borderColor = new Color(
				(borderColor.getRed() * t + background.getRed() * (255 - t)) / 255,
				(borderColor.getGreen() * t + background.getGreen() * (255 - t)) / 255,
				(borderColor.getBlue() * t + background.getBlue() * (255 - t)) / 255
		);
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
			component.add(formBox);
			component.add(containerBox);
			getRootContainer().component.getRootPane().validate();
		} else if (this.expanded && !expanded) {
			this.expanded = false;
			component.remove(formBox);
			component.remove(containerBox);
			getRootContainer().component.getRootPane().validate();
		}
	}
	
	JPatchFormContainer getRootContainer() {
		return parentContainer == null ? this : parentContainer.getRootContainer();
	}
	
	int getLevel() {
		return getLevel(1);
	}
	
	private int getLevel(int level) {
		return parentContainer == null ? level : parentContainer.getLevel(++level);
	}
}
