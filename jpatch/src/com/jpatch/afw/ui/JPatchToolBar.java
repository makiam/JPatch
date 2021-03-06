package com.jpatch.afw.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JPatchToolBar extends JToolBar {
	private Color textColor = new Color(0xffffffff, true);
	private Font textFont = new Font("sans-serif", Font.BOLD, 13);
	
	private List<ToolTipComponent> componentList = new ArrayList<ToolTipComponent>();
	private Dimension dimension = new Dimension();
	
	
	public JPatchToolBar() {
		setFloatable(false);
		setBorder(null);
	}
	
	@Override
	public Component add(Component comp) {
		ToolTipComponent tt = new ToolTipComponent(comp);
		componentList.add(tt);
		super.add(tt);
		super.add(tt.component);
		setComponentZOrder(tt, 0);
		setComponentZOrder(tt.component, getComponentCount() - 1);
		computeSize();
		return comp;
	}

	@Override
	public Dimension getPreferredSize() {
		return dimension;
	};
	
	@Override
	public Dimension getMinimumSize() {
		return dimension;
	};
	
	@Override
	public Dimension getMaximumSize() {
		return dimension;
	};
	
	@Override
	public void doLayout() {
		int xPos = (getWidth() - dimension.width) / 2;
		for (int i = 0, n = componentList.size(); i < n; i++) {
			ToolTipComponent comp = componentList.get(i);
			if (i == 0) {
				xPos += comp.getOffsetWidth() - comp.component.getWidth() / 2;
			}
			comp.component.setBounds(xPos, Math.max(0, 18 - comp.component.getPreferredSize().height / 2), comp.component.getPreferredSize().width, comp.component.getPreferredSize().height);
			comp.setBounds(xPos + (comp.component.getPreferredSize().width - comp.getImageWidth()) / 2, 33, comp.getImageWidth(), comp.getImageHeight());
			xPos += (i == n - 1 ? comp.getOffsetWidth() : comp.component.getPreferredSize().width);
//			System.out.println(xPos + " " + comp.getClass() + " " + comp.getBounds());
		}
	}

	public void paintComponent(Graphics g) {
		Background.fillComponent(this, g);
		if (false) {
			super.paintComponent(g);
			return;
		}
		if (false) {
			Graphics2D g2 = (Graphics2D) g;
			int s = (getHeight() * 2) / 3;
			g2.setPaint(new GradientPaint(0, 0, new Color(0xf0f0f0), 0, s, new Color(0xa0a0a0)));
			g2.fillRect(0, 0, getWidth(), s);
			g2.setPaint(new GradientPaint(0, s, new Color(0xa0a0a0), 0, getHeight(), new Color(0xb0b0b0)));
			g2.fillRect(0, s, getWidth(), getHeight() - s);
			return;
		}
//		Rectangle bounds = getBounds();
//		Graphics2D g2 = (Graphics2D) g;
//		AffineTransform saveAt = g2.getTransform();
//		g.translate(-bounds.x, -bounds.y);
//		final float width = getParent().getWidth();
//		final float height = getParent().getHeight();
//		final float yoff = width * 2;
//		final int n = 9;
//		final float l0 = yoff;
//		final float l1 = (float) Math.sqrt((width / 2) * (width / 2) + (yoff + height) * (yoff + height));
//		final Color c0 = new Color(0xe0e0e0);
//		final Color c1 = new Color(0xa0a0a0);
//		for (int i = 0; i < n; i++) {
//			float xoff = width * (i + 0.5f) / n - width / 2.0f;
//			float len = (float) Math.sqrt(xoff * xoff + yoff * yoff);
//			float x0 = xoff / len * l0 + width / 2.0f;
//			float y0 = yoff / len * l0 - yoff;
//			float x1 = xoff / len * l1 + width / 2.0f;
//			float y1 = yoff / len * l1 - yoff;
//			g2.setPaint(new GradientPaint(x0, y0, c0, x1, y1, c1));
//			g2.fillRect(getParent().getWidth() * i / n, 0, getParent().getWidth() * (i + 1) / n - getParent().getWidth() * i / n, getParent().getHeight());
//		}
//		g2.setTransform(saveAt);
//		g.setColor(new Color(0x808080));
//		g.drawLine(0, bounds.height - 1, bounds.width - 1, bounds.height - 1);
	}
	
	@SuppressWarnings("unchecked")
	private void computeSize() {
		int width = 0, height = 0;
		for (int i = 0, n = componentList.size(); i < n; i++) {
			ToolTipComponent comp = componentList.get(i);
			width += (i == 0 || i == n - 1) ? comp.getOffsetWidth() : comp.component.getPreferredSize().width;
			int h = comp.component.getPreferredSize().height + comp.getHeight();
			if (h > height) {
				height = h;
			}
		}
		dimension.width = width;
		dimension.height = 48;
	}
	
	private class ToolTipComponent extends JComponent {
		private Component component;
		private Image enabledToolTip;
		private Image disabledToolTip;
		private Image shortcut;
		private Image disabledShortcut;
		
		ToolTipComponent(Component component) {
			this.component = component;
//			System.out.print(component.getClass().getName() + "@" + System.identityHashCode(component) + " ");
			if (component instanceof JPatchButton) {
				JPatchButton button = (JPatchButton) component;
				String text = button.getJPatchAction().getButtonText().getValue();
				if (text != null) {
//					System.out.print(text + " ");
					enabledToolTip = ImageUtils.createShadowIcon(ImageUtils.createTextIcon(textFont, textColor, text));
					disabledToolTip = ImageUtils.createDisabledIcon(enabledToolTip);
				}
//				((AbstractButton) button).addChangeListener(new ChangeListener() {
//					public void stateChanged(ChangeEvent e) {
//						setVisible(((AbstractButton) e.getSource()).getModel().isRollover());
//					}
//				});
				((AbstractButton) button).addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						setVisible(true);
					}
					@Override
					public void mouseExited(MouseEvent e) {
						setVisible(false);
					}			
				});
			}
			setVisible(false);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
//			System.out.println("paint image");
//			System.out.println(g.getClipBounds() + " " + getBounds() + " " + getWidth() + "x" + getHeight());
			Image image = component.isEnabled() ? enabledToolTip : disabledToolTip;
			if (image != null) {
				g.drawImage(image, 0, 0, null);
			}
		}
		
		
		
		public int getOffsetWidth() {
			int bw = component.getPreferredSize().width;
			int lw = getImageWidth();
			if (lw > bw) {
				return (lw + bw) / 2;
			} else {
				return bw;
			}
		}
		
		public int getImageWidth() {
			return enabledToolTip == null ? 0 : enabledToolTip.getWidth(null);
		}
		
		public int getImageHeight() {
			return enabledToolTip == null ? 0 : enabledToolTip.getHeight(null);
		}
	}
}

