package com.jpatch.afw.ui;

import com.jpatch.afw.Utils;
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
	private static final Insets EXPANDED_INSETS = new Insets(0, 3, 3, 3);
	private static final Insets COLLAPSED_INSETS = new Insets(0, 3, 3, 3);
	private final JComponent component = new JPanel();
	private final JComponent titleBar = new JPanel(new BorderLayout());
	private final JPanel actionButtonPanel;
	private final JButton actionButton;
	private final Box formBox = Box.createVerticalBox();
	private final Box containerBox = Box.createVerticalBox();
	private JPatchFormContainer parentContainer;
	private final BooleanAttr expandedAttr;
	private boolean componentsAdded;
	private Color borderColor = new Color(0x808080);
	private final String title; // FIXME: for debugging, remove
	private final JToggleButton expandedButton = new JToggleButton();
	
	private AttributePostChangeListener expansionListener = new AttributePostChangeListener() {
		private boolean ignore = false;
		public void attributeHasChanged(Attribute source) {
			if (!ignore) {
				ignore = true;
				if (expandedAttr.getBoolean()) {
					if (!componentsAdded) {
						component.add(formBox);
						component.add(containerBox);
						componentsAdded = true;
					}
				} else {
					if (componentsAdded) {
						component.remove(formBox);
						component.remove(containerBox);
						componentsAdded = false;
					}
				}
				expandedButton.setSelected(expandedAttr.getBoolean());
				if (actionButton != null) {
					actionButtonPanel.setVisible(expandedAttr.getBoolean());
				}
				System.out.println(title + " component=" + System.identityHashCode(component) + " parent=" + System.identityHashCode(component.getParent()) + " validateRoot=" + System.identityHashCode(Utils.getValidateRoot(component)));
				component.revalidate();
				ignore = false;
			}
		}
	};
	
	public JPatchFormContainer(String title, BooleanAttr expansionControl) {
		this(title, expansionControl, null);
	}
			
	public JPatchFormContainer(String title, BooleanAttr expansionControl, JButton actionButton) {
		this.title = title;
		if (expansionControl != null) {
			expandedAttr = expansionControl;
		} else {
			expandedAttr = new BooleanAttr();
		}
		this.actionButton = actionButton;
		component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
		titleBar.add(expandedButton, BorderLayout.WEST);
		if (actionButton != null) {
			actionButtonPanel = new JPanel(new BorderLayout());
			actionButtonPanel.setBorder(BorderFactory.createEmptyBorder(2, 1, 1, 2));
			System.out.println("panel border = " + actionButtonPanel.getBorder());
			System.out.println("button border = " + actionButton.getBorder());
			Dimension dim = actionButton.getPreferredSize();
			dim.height = 20;
			actionButton.setPreferredSize(dim);
			actionButtonPanel.add(actionButton, BorderLayout.CENTER);
			actionButtonPanel.setOpaque(false);
			titleBar.add(actionButtonPanel, BorderLayout.EAST);
		} else {
			actionButtonPanel = null;
		}
		expandedButton.setFont(LABEL_FONT);
		titleBar.setOpaque(false);
		titleBar.setPreferredSize(new Dimension(20, 18));
		component.add(titleBar);
		component.setOpaque(false);
//		component.setBackground(new Color(0xcccccc));
		component.setBorder(new Border() {
			public Insets getBorderInsets(Component c) {
				return expandedAttr.getBoolean() ? EXPANDED_INSETS : COLLAPSED_INSETS;
			}

			public boolean isBorderOpaque() {
				return false;
			}

			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				Color background = Color.white;
				int t = 127;
//				t = 100;
				Color brightColor = new Color(
						(borderColor.getRed() * t + background.getRed() * (255 - t)) / 255,
						(borderColor.getGreen() * t + background.getGreen() * (255 - t)) / 255,
						(borderColor.getBlue() * t + background.getBlue() * (255 - t)) / 255
				);
				int gray = (borderColor.getRed() * 0 + borderColor.getGreen() * 255 + borderColor.getBlue() * 0) * 5 / 6 / 255;
				Color grayColor = new Color(gray + borderColor.getRed() / 6, gray + borderColor.getGreen() / 6, gray + borderColor.getBlue() / 6);
//				grayColor = new Color(0xcccccc);
//				brightColor = Color.YELLOW;
				
				
				int h = titleBar.getHeight();
				Graphics2D g2 = (Graphics2D) g;
//				g2.setColor(new Color(0xeeeeee));
//				g2.fillRoundRect(x + 1, y + h - 1, width - 3, height - h - 3, 6, 6);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//				g2.setPaint(new GradientPaint(0, h, borderColor, width, 0, brightColor));
				
//				int level = getLevel();
//				int round = 14 - level * 2;
				
				int round = 10;
				
				g2.setPaint(new GradientPaint(0, h, brightColor, width, 0, grayColor));
				g2.fillRoundRect(x + 1, y + 1, width - 2, height - 3, round - 2, round - 2);
				int hh = Math.min(h * 2, height);
				g2.setPaint(new GradientPaint(0, 0, new Color(0x66ffffff, true), 0, hh, new Color(0x00ffffff, true)));
				g2.fillRoundRect(x, y, width, hh, round, round);
				g2.setColor(borderColor.darker());
				g2.drawRoundRect(x, y, width - 1, height - 2, round, round);
//				g2.setColor(borderColor);
//				g2.fillRoundRect(x, y + 0, width, h, 8, 8);
//				g2.drawRoundRect(x, y + 0, width - 1, h - 1, 8, 8);
//				if (expandedAttr.getBoolean()) {
//					g2.drawRoundRect(x, y + h - 2, width - 1, height - h - 1, 8, 8);
//					g2.drawRoundRect(x + 1, y + h - 1, width - 3, height - h - 3, 6, 6);
//					g2.fillRect(x, 11, 2, 8);
//					g2.fillRect(x + width - 2, 11, 2, 8);
//				}
			}
			
		});
		
		expandedAttr.addAttributePostChangeListener(expansionListener);
		expandedButton.setSelected(expandedAttr.getBoolean());
		expandedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				expandedAttr.setBoolean(expandedButton.isSelected());
			}
		});
		expandedButton.setContentAreaFilled(false);
		expandedButton.setBorderPainted(false);
		expandedButton.setBorder(null);
		expandedButton.setIcon(COLLAPSED_ICON);
		expandedButton.setSelectedIcon(EXPANDED_ICON);
		expandedButton.setFocusable(false);
		expandedButton.setText(title);
	}
	
	public void setTitle(String title) {
		expandedButton.setText(title);
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
	
	public void add(JComponent customComponent) {
		System.out.println("add " + customComponent);
		formBox.add(customComponent);	
	}
	
	public void remove(JComponent customComponent) {
		System.out.println("remove " + customComponent);
		formBox.remove(customComponent);	
	}
	
	public void add(JPatchFormContainer formContainer) {
		containerBox.add(formContainer.getComponent());
		formContainer.parentContainer = this;
//		Color background = UIManager.getColor("Panel.background");
		Color background = Color.white;
		int t = 255 - 127;
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
	
	int getLevel() {
		return getLevel(1);
	}
	
	private int getLevel(int level) {
		return parentContainer == null ? level : parentContainer.getLevel(++level);
	}
}
