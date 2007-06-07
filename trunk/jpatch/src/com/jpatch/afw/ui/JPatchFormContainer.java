package com.jpatch.afw.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class JPatchFormContainer {
	static final int MAX_NEST_LEVEL = 3;
	private static final Font LABEL_FONT = new Font("sans-serif", Font.BOLD, 14);
	private static final Icon EXPANDED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/EXPANDED.png"));
	private static final Icon COLLAPSED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/COLLAPSED.png"));
	private final JComponent component = Box.createVerticalBox();
	private final JPanel titleBar = new JPanel(new BorderLayout());
	private final Box formBox = Box.createVerticalBox();
	private final Box containerBox = Box.createVerticalBox();
	private JPatchFormContainer parentContainer;
	private boolean expanded;
	private static final Color[] BORDER_COLORS = new Color[] {
		new Color(0xaaaaaa),
		new Color(0x40000000, true),
		new Color(0x20000000, true)
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
		titleBar.setBackground(BORDER_COLORS[0]);
		component.add(titleBar);
		component.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0), BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLORS[0], 2), BorderFactory.createEmptyBorder(0, 2, 0, 2))));
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
			component.add(formBox);
			component.add(containerBox);
			getRootContainer().component.getParent().validate();
		} else if (this.expanded && !expanded) {
			this.expanded = false;
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
