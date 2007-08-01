package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributePostChangeListener;
import com.jpatch.afw.attributes.BooleanAttr;

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
	private final BooleanAttr expandedAttr;
	private boolean componentsAdded;
	private Color borderColor = new Color(0x808080);
	private final String title; // FIXME: for debugging, remove
	private AttributePostChangeListener expansionListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			setExpanded(expandedAttr.getBoolean());
		}
	};
	public JPatchFormContainer(String title, BooleanAttr expansionControl) {
		this.title = title;
		if (expansionControl != null) {
			expandedAttr = expansionControl;
		} else {
			expandedAttr = new BooleanAttr();
		}
		JToggleButton button = new JToggleButton("", expandedAttr.getBoolean());
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
				return expandedAttr.getBoolean() ? EXPANDED_INSETS : COLLAPSED_INSETS;
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
				if (expandedAttr.getBoolean()) {
					g2.drawRoundRect(x, y + h - 2, width - 1, height - h - 1, 8, 8);
					g2.drawRoundRect(x + 1, y + h - 1, width - 3, height - h - 3, 6, 6);
					g2.fillRect(x, 11, 2, 8);
					g2.fillRect(x + width - 2, 11, 2, 8);
				}
			}
			
		});
//		setExpanded(expandedAttr.getBoolean());
//		component.addHierarchyListener(new HierarchyListener() {
//			public void hierarchyChanged(HierarchyEvent e) {
//				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
//					if (component.isShowing()) {
////						setExpanded(expandedAttr.getBoolean());
//						expandedAttr.addAttributePostChangeListener(expansionListener);
//					} else {
//						expandedAttr.removeAttributePostChangeListener(expansionListener);
//					}
//				}
//			}
//		});
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
	
	private void setExpanded(boolean expanded) {
		System.err.println(title + " setExpanded(" + expanded + ") called");
		System.err.println("    componentsAdded=" + componentsAdded + " expandedAttr=" + expandedAttr.getBoolean());
		if (!componentsAdded && expanded) {
			componentsAdded = true;
			component.add(formBox);
			component.add(containerBox);
		} else if (componentsAdded && !expanded) {
			componentsAdded = false;
			component.remove(formBox);
			component.remove(containerBox);
		}
		expandedAttr.setBoolean(expanded);
		if (component.isShowing()) {
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
