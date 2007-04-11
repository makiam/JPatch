package ui;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.metal.MetalButtonUI;

import org.apache.batik.ext.awt.LinearGradientPaint;
import static java.awt.RenderingHints.*;

public class Buttons {
	private static ButtonUI buttonUI = new BasicButtonUI();
	
	public static final int BUTTON_HEIGHT = 21;
	public static final int BUTTON_WIDTH = 23;
	public static final int BUTTON_SIDE = 4;

	public static enum Type { GLASS, PLASTIC, ROUND_GLASS, ROUND_PLASTIC }
	public static enum Position { LEFT, CENTER, RIGHT }
	public static enum Mode { DEFAULT, DISABLED, SELECTED, ROLLOVER }
	
	private static final Color[] GLASS_COLORS = new Color[] {
			new Color(0xe0e0e0),
			new Color(0xf7f7f7),
			new Color(0xc0c0c0),
			new Color(0xe0e0e0)
	};
	
	private static final Color[] PLASTIC_COLORS = new Color[] {
		new Color(0xe7e7e7),
		new Color(0xbababa),
		new Color(0xb0b0b0),
		new Color(0x888888),
	};
	
	private static final Color[] PLASTIC_COLORS2 = new Color[] {
		new Color(0xc7c7c7),
		new Color(0x9a9a9a),
		new Color(0x909090),
		new Color(0x696969),
	};
	
	public static void main(String[] args) {
		new Buttons().test();
	}
	
	private Image createGroupButtonImage(Type type, boolean left, boolean right, float[] tint, int width, int height, Image icon, int size) {
		int w = (!left && !right) ? width : width + 2;
		int off = 0;
		int ww = 0;
		int imgOff = 0;
		int yOff = 0;
		if (type == Type.ROUND_PLASTIC || type == Type.ROUND_GLASS) {
			if (left) {
				off = 2;
				ww = 2;
			}
			if (right) {
				ww = 1;
				off = 0;
			}
		}
		if (type == Type.ROUND_PLASTIC) {
			if (left && right) {
				imgOff = 2;
			} else {
				imgOff = -1;
			}
		}
		if (type == Type.ROUND_GLASS && left && right) {
			yOff = -1;
			imgOff = 1;
		}
		BufferedImage image = new BufferedImage(w + ww, height + 8, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.translate(off, 4 + yOff);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawGroupButton(type, left, right, tint, g, w - 1, height - 1, size);
		if (left && !right) {
			g.drawImage(icon, (w - icon.getWidth(null)) / 2 + 1 + imgOff, (height - icon.getHeight(null)) / 2, null);
		} else if (!left && right) {
			g.drawImage(icon, (w - icon.getWidth(null)) / 2 - 1 + imgOff, (height - icon.getHeight(null)) / 2, null);
		} else {
			g.drawImage(icon, (w - icon.getWidth(null)) / 2 + imgOff, (height - icon.getHeight(null)) / 2, null);
		}
		return image;
	}
	
	private void drawGroupButton(Type type, boolean posLeft, boolean posRight, float[] tint, Graphics2D g, int width, int height, int size) {
		int w = width - 2;
		int h = height - 1;
		int arc = h / 2;
		int h2 = h / 2 + 1;
		int left = posLeft ? 1 : 1 - h2;
		int right = posRight ? w + 2 : w + 2 + h2;
		
		if (posRight && type == Type.ROUND_PLASTIC) {
			right -= 2;
		}
		
		if (posLeft && posRight) {
			left += 1;
			right -= 1;
		}
		Color[] colors = tintColors(GLASS_COLORS, tint);
		Color[] pColors = tintColors(PLASTIC_COLORS, tint);
		RoundRectangle2D roundRect = new RoundRectangle2D.Float(left, 1, right - left, h - 1, arc, arc);
		
		if (!posRight || !posLeft) {
			if (type == Type.ROUND_PLASTIC) {
				g.setPaint(new GradientPaint(0, 0, new Color(0x40000000, true), 0, height, new Color(0x80ffffff, true)));
				g.fill(new RoundRectangle2D.Float(left - 3, -3, right - left + 5, height + 7, height + 5, height + 5));
	//			g.setPaint(new GradientPaint(0, 1, new Color(0xbbbbbb), 0, h, new Color(0x888888)));
	//			g.fill(new RoundRectangle2D.Float(left - 2, -2, right - left + 3, height + 5, height + 5, height + 5));
			} else if (type == Type.ROUND_GLASS) {
				g.setPaint(new GradientPaint(0, 0, new Color(0x80ffffff, true), 0, height, new Color(0x30000000, true)));
				g.draw(new RoundRectangle2D.Float(left - 2, -2, right - left + 2, height + 3, height + 4, height + 4));
				g.setPaint(new GradientPaint(0, 0, new Color(0x30ffffff, true), 0, height, new Color(0x18000000, true)));
				g.fill(new RoundRectangle2D.Float(left - 1, -1, right - left + 1, height + 3, height + 4, height + 4));
			}
		}
		
		switch (type) {
		case GLASS:
			g.setPaint(new GradientPaint(0, 1, colors[0], 0, h2, colors[1]));
			g.fill(roundRect);
			g.setPaint(new GradientPaint(0, 1, colors[2], 0, h, colors[3]));
			GeneralPath path = new GeneralPath();
			path.moveTo(left, height);
			path.curveTo(left, h2, left, h2, left + h, h2);
			path.lineTo(right - h, h2);
			path.curveTo(right, h2, right, h2, right, 0);
			path.lineTo(right, h);
			path.closePath();
			g.setClip(roundRect);
			g.fill(path);
			g.setColor(new Color(0x18000000, true));
			g.drawLine(left, h - 1, right, h - 1);
			g.setColor(new Color(0x0a000000, true));
			g.drawLine(left, h - 2, right, h - 2);
			break;
		case PLASTIC:
			g.setPaint(new LinearGradientPaint(0, 1, 0, h, new float[] { 0.0f, 0.2f, 0.8f, 1.0f }, tintColors(PLASTIC_COLORS, tint)));
			g.fill(roundRect);
			break;
		case ROUND_GLASS:
			int l = posLeft ? 2 : !posRight ? 0 : 0;
			g.setPaint(new GradientPaint(l, 1, colors[2], 0, h, colors[3]));
			g.fill(new Ellipse2D.Float(l, 0 + (height - size) / 2, size, size));
			g.setPaint(new GradientPaint(l, 1, colors[0], 0, h2, colors[1]));
			g.setClip(new Ellipse2D.Float(l, + (height - size) / 2, size, size));
			g.fill(new Ellipse2D.Float(l, 0 + (height - size) / 2, size, size / 2));
			g.setClip(null);
			g.setPaint(new GradientPaint(l, 0, new Color(0xffffffff, true), 0, height, new Color(0x00ffffff, true)));
			g.setStroke(new BasicStroke(1.5f));
			g.draw(new Ellipse2D.Float(l, 1 + (height - size) / 2, size - 1, size - 3));
			g.setPaint(new GradientPaint(l, 0, new Color(0x00000000, true), 0, height, new Color(0x28000000, true)));
			g.draw(new Ellipse2D.Float(l, 1 + (height - size) / 2, size - 1, size - 3));
			g.setPaint(new GradientPaint(l, 0, new Color(0x80000000, true), 0, height, new Color(0xc0ffffff, true)));
			g.setStroke(new BasicStroke(1));
			g.draw(new Ellipse2D.Float(l, 0 + (height - size) / 2, size - 1, size - 1));
			break;
		case ROUND_PLASTIC:
			l = posLeft ? 3 : !posRight ? 1 : 1;
//			g.setClip(new Ellipse2D.Float(l, 0, height, height));
			g.setPaint(new LinearGradientPaint(0, 1, 0, h, new float[] { 0.0f, 0.4f, 0.6f, 1.0f }, tintColors(PLASTIC_COLORS2, tint)));
			g.fill(new Ellipse2D.Float(l - 1, - 1, height + 2, height + 2));
//			g.setPaint(new GradientPaint(l, 1, pColors[1], 0, h2, pColors[2]));
//			
//			g.fill(new Rectangle.Float(l, 0, height, height / 2));
			g.setClip(null);
			g.setPaint(new GradientPaint(l, 0, new Color(0xffffffff, true), 0, height, new Color(0x00ffffff, true)));
			g.setStroke(new BasicStroke(1.0f));
			g.draw(new Ellipse2D.Float(l - 1, 0, height + 1, height - 1));
			g.setPaint(new GradientPaint(l, 0, new Color(0x00000000, true), 0, height, new Color(0x40000000, true)));
//			g.draw(new Ellipse2D.Float(l, 1, height - 1, height - 3));
			g.setPaint(new GradientPaint(l, 0, new Color(0x80000000, true), 0, height, new Color(0x00000000, true)));
//			g.setStroke(new BasicStroke(1.0f));
			g.draw(new Ellipse2D.Float(l - 1, -1, height + 1, height + 1));
			g.setStroke(new BasicStroke(1.0f));
			break;
		}
		if (type == Type.GLASS || type == Type.PLASTIC) {
			if (!posLeft) {
				g.setColor(new Color(0x60ffffff, true));
				g.drawLine(0, 1, 0, h - 1);
			}
			if (!posRight) {
				g.setColor(new Color(0x40000000, true));
				g.drawLine(width, 1, width, h - 1);
			}
			g.setClip(null);
			g.setPaint(new GradientPaint(0, 0, new Color(0x40000000, true), 0, height, new Color(0xffffffff, true)));
			g.draw(new RoundRectangle2D.Float(left - 1, 0, right - left + 1, height - 1, arc + 1, arc + 1));
		} else if (type == Type.ROUND_PLASTIC || type == Type.ROUND_GLASS) {
//			g.setPaint(new GradientPaint(0, 0, new Color(0x40000000, true), 0, height, new Color(0x80ffffff, true)));
//			g.draw(new RoundRectangle2D.Float(left - 3, -3, right - left + 4, height + 6, height + 0, height + 0));
		}
	}
	
	private void drawViewportSwitcher(float[] tint, Graphics2D g, int width, int height) {
		int w = width - 1;
		int h = height - 1;
		int arc = h / 3;
		int h2 = h / 2 + 1;
		int h4 = h2 / 2 + 1;
		int w2 = w / 2 + 1;
		
		Color[] colors = tintColors(GLASS_COLORS, tint);
		
		RoundRectangle2D roundRect = new RoundRectangle2D.Float(1, 1, w, h, arc, arc);
		g.setColor(Color.WHITE);
		g.fill(roundRect);
		g.setClip(new RoundRectangle2D.Float(0, 0, w + 1, h + 1, arc, arc));
		g.setPaint(new GradientPaint(0, 1, colors[0], 0, h2, colors[1]));
		g.fill(new Rectangle2D.Float(0, 0, w, h2));
		g.setPaint(new GradientPaint(0, h2, colors[0], 0, h, colors[1]));
		g.fill(new Rectangle2D.Float(0, h2, w, h2));
		g.setPaint(new GradientPaint(0, 1, colors[2], 0, h2, colors[3]));
		GeneralPath path = new GeneralPath();
		path.moveTo(1, h2);
		path.curveTo(1, h4, 1, h4, 1 + h2, h4);
		path.lineTo(w - h2, h4);
		path.curveTo(w, h4, w, h4, w, 0);
		path.lineTo(w, h2);
		path.closePath();
		g.setClip(roundRect);
		g.fill(path);
		g.setPaint(new GradientPaint(0, h2, colors[2], 0, h, colors[3]));
		path = new GeneralPath();
		path.moveTo(1, h2 + h2 - 1);
		path.curveTo(1, h4 + h2 - 1, 1, h4 + h2 - 1, 1 + h2 - 1, h4 + h2 - 1);
		path.lineTo(w - h2 - 1, h4 + h2 - 1);
		path.curveTo(w, h4 + h2 - 1, w, h4 + h2 - 1, w, 0 + h2 - 1);
		path.lineTo(w, h2 + h2 - 1);
		path.closePath();
		g.setClip(roundRect);
		g.fill(path);
		
		g.setColor(new Color(0x18000000, true));
		g.drawLine(1, h - 1, w, h - 1);
		g.setColor(new Color(0x0a000000, true));
		g.drawLine(1, h - 2, w, h - 2);
		
		g.setColor(new Color(0x60ffffff, true));
		g.drawLine(w2, 1, w2, h + 1);
		g.drawLine(1, h2, w - 1, h2);
		g.setColor(new Color(0x40000000, true));
		g.drawLine(w2 - 1, 1, w2 - 1, h + 1);
		g.drawLine(1, h2 - 1, w - 1, h2 - 1);
		
		g.setClip(null);
		g.setStroke(new BasicStroke(2));
		g.setPaint(new GradientPaint(0, 0, new Color(0xffcccccc, true), 0, height, new Color(0xffbbbbbb, true)));
		g.draw(new RoundRectangle2D.Float(0, 0, w, h, arc + 0, arc + 0));
		g.setStroke(new BasicStroke(1));
		g.setPaint(new GradientPaint(0, 0, new Color(0x50000000, true), 0, height, new Color(0x28000000, true)));
		g.draw(new RoundRectangle2D.Float(0, 0, w, h + 1, arc + 0, arc + 0));
		
		Font font = new Font("monospace", Font.BOLD, 18);
		g.drawImage(createDisabledImage(createDisabledImage(createTextImage(font, "1"))), w / 4 - 6, h / 4 - 9, null);
		g.drawImage(createDisabledImage(createDisabledImage(createTextImage(font, "2"))), w * 3 / 4 - 6, h / 4 - 9, null);
		g.drawImage(createDisabledImage(createDisabledImage(createTextImage(font, "3"))), w / 4 - 5, h * 3 / 4 - 9, null);
		g.drawImage(createDisabledImage(createDisabledImage(createTextImage(font, "4"))), w * 3 / 4 - 7, h * 3 / 4 - 9, null);
	}
	
	private Color[] tintColors(Color[] colors, float[] tint) {
		Color[] c = new Color[colors.length];
		float[] cc = new float[3];
		for (int i = 0; i < c.length; i++) {
			cc = colors[i].getRGBColorComponents(cc);
			if (tint != null) {
				for (int j = 0; j < 3; j++) {
					cc[j] = Math.min(1, cc[j] * tint[j]);
				}
			}
			c[i] = new Color(cc[0], cc[1], cc[2]);
		}
		return c;
	}
	
	private static class TransparentToolbar extends JToolBar {
		public void paintComponent(Graphics g) {
			Rectangle bounds = getBounds();
//			g.translate(-bounds.x, -bounds.y);
			Graphics2D g2 = (Graphics2D) g;
			AffineTransform saveAt = g2.getTransform();
			g.translate(-bounds.x, -bounds.y);
			final float width = getParent().getWidth();
			final float height = getParent().getHeight();
			final float yoff = width * 1.414f;
			final int n = 9;
			final float l0 = yoff;
			final float l1 = (float) Math.sqrt((width / 2) * (width / 2) + (yoff + height) * (yoff + height));
			final Color c0 = new Color(0.7f, 0.7f, 0.7f);
			final Color c1 = new Color(0.4f, 0.4f, 0.4f);
			System.out.println(l0 + " " + l1);
			for (int i = 0; i < n; i++) {
				float xoff = width * (i + 0.5f) / n - width / 2.0f;
				float len = (float) Math.sqrt(xoff * xoff + yoff * yoff);
				float x0 = xoff / len * l0 + width / 2.0f;
				float y0 = yoff / len * l0 - yoff;
				float x1 = xoff / len * l1 + width / 2.0f;
				float y1 = yoff / len * l1 - yoff;
				g2.setPaint(new GradientPaint(x0, y0, c0, x1, y1, c1));
				g2.fillRect(getParent().getWidth() * i / n, 0, getParent().getWidth() * (i + 1) / n - getParent().getWidth() * i / n, getParent().getHeight());
			}
			g2.setTransform(saveAt);
		}
	}
	
	private void test() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(640, 480);
		
		frame.setBackground(new Color(0xaaaaaa));
		frame.setLayout(new BorderLayout());
		//frame.add(component);
		
		JPanel panel = new JPanel() {
//
//			@Override
//			public void doLayout() {
////				super.doLayout();
//				Dimension leftDim = getComponent(0).getPreferredSize();
//				Dimension centerDim = getComponent(1).getPreferredSize();
//				Dimension rightDim = getComponent(2).getPreferredSize();
//				getComponent(0).setBounds(0, 0, leftDim.width, leftDim.height);
//				getComponent(1).setBounds((getWidth() - centerDim.width) / 2, 0, centerDim.width, centerDim.height);
//				getComponent(2).setBounds(getWidth() - rightDim.width, 0, rightDim.width, rightDim.height);
//			}
//			
//			public void paintComponent(Graphics g) {
//				Graphics2D g2 = (Graphics2D) g;
//				final float width = getParent().getWidth();
//				final float height = getParent().getHeight();
//				final float yoff = width * 1.414f;
//				final int n = 9;
//				final float l0 = yoff;
//				final float l1 = (float) Math.sqrt((width / 2) * (width / 2) + (yoff + height) * (yoff + height));
//				final Color c0 = new Color(0.7f, 0.7f, 0.7f);
//				final Color c1 = new Color(0.4f, 0.4f, 0.4f);
//				System.out.println(l0 + " " + l1);
//				for (int i = 0; i < n; i++) {
//					float xoff = width * (i + 0.5f) / n - width / 2.0f;
//					float len = (float) Math.sqrt(xoff * xoff + yoff * yoff);
//					float x0 = xoff / len * l0 + width / 2.0f;
//					float y0 = yoff / len * l0 - yoff;
//					float x1 = xoff / len * l1 + width / 2.0f;
//					float y1 = yoff / len * l1 - yoff;
//					g2.setPaint(new GradientPaint(x0, y0, c0, x1, y1, c1));
//					g2.fillRect(getParent().getWidth() * i / n, 0, getParent().getWidth() * (i + 1) / n - getParent().getWidth() * i / n, getParent().getHeight());
//				}
//			}
		};
		
		JToolBar leftToolBar = new TransparentToolbar();
		JToolBar centerToolBar = new TransparentToolbar();
		JToolBar rightToolBar = new TransparentToolbar();
		leftToolBar.setFloatable(false);
		centerToolBar.setFloatable(false);
		rightToolBar.setFloatable(false);
		
		centerToolBar.setLayout(new BorderLayout());
		
		JToggleButton tb1 = new JToggleButton();
		JToggleButton tb2 = new JToggleButton();
		JToggleButton tb3 = new JToggleButton();
		JToggleButton tb4 = new JToggleButton();
		JToggleButton tb5 = new JToggleButton();
		JToggleButton tb6 = new JToggleButton();
		JButton b1 = new JButton();
		JButton b2 = new JButton();
		JButton b3 = new JButton();
		JButton b4 = new JButton();
		JButton b5 = new JButton();
		JButton b6 = new JButton();
		
		JToggleButton tb7 = new JToggleButton();
		JToggleButton tb8 = new JToggleButton();
		JToggleButton tb9 = new JToggleButton();
		JToggleButton tb10 = new JToggleButton();
		
		JToggleButton tb11 = new JToggleButton();
		JButton b7 = new JButton();
		JButton b8 = new JButton();
		
		int buttonWidth = 30;
		int buttonHeight = 26;
		
		Image icon = createTestIcon();
		
		Font font = new Font("sans serif", Font.BOLD, 12);
//
//		icon = createEtchedIcon(createTransparentImage(createTextImage(font, "Open"), 0.75f));
		Image etchedIcon = createTransparentImage(createEtchedIcon(icon), 0.67f);
		
		configureButton(tb1, Type.GLASS, true, false, buttonWidth, buttonHeight, etchedIcon);
		configureButton(tb2, Type.GLASS, false, false, buttonWidth, buttonHeight, etchedIcon);
		configureButton(tb3, Type.GLASS, false, true, buttonWidth, buttonHeight, etchedIcon);
		configureButton(tb4, Type.PLASTIC, true, false, buttonWidth, buttonHeight, etchedIcon);
		configureButton(tb5, Type.PLASTIC, false, false, buttonWidth, buttonHeight, etchedIcon);
		configureButton(tb6, Type.PLASTIC, false, true, buttonWidth, buttonHeight, etchedIcon);
		
		buttonWidth = 31;
		configureButton(tb7, Type.PLASTIC, true, true, buttonWidth, buttonHeight, etchedIcon);
		configureButton(tb8, Type.GLASS, true, true, buttonWidth, buttonHeight, etchedIcon);
		buttonWidth = 26;
		buttonHeight = 26;
		configureButton(tb9, Type.ROUND_PLASTIC, true, true, buttonWidth, buttonHeight, etchedIcon);
		buttonWidth = 27;
		buttonHeight = 27;
		configureButton(tb10, Type.ROUND_GLASS, true, true, buttonWidth, buttonHeight, etchedIcon);
		
		buttonHeight = 30;
		buttonWidth = 30;
		configureButton(b7, Type.ROUND_GLASS, true, false, buttonWidth - 2, buttonHeight, etchedIcon, buttonHeight - 3);
		configureButton(b8, Type.ROUND_GLASS, false, false, buttonWidth - 2, buttonHeight, etchedIcon, buttonHeight - 3);
		configureButton(tb11, Type.ROUND_GLASS, false, true, buttonWidth - 2, buttonHeight, etchedIcon, buttonHeight - 3);
		
		buttonWidth = 34;
		buttonHeight = 30;
		configureButton(b1, Type.ROUND_PLASTIC, true, false, buttonWidth, buttonHeight, etchedIcon);
		configureButton(b2, Type.ROUND_PLASTIC, false, false, buttonWidth, buttonHeight, etchedIcon);
		configureButton(b3, Type.ROUND_PLASTIC, false, true, buttonWidth, buttonHeight, etchedIcon);
		buttonWidth = 30;
		configureButton(b4, Type.ROUND_GLASS, true, false, buttonWidth - 2, buttonHeight, etchedIcon, buttonHeight - 3);
		configureButton(b5, Type.ROUND_GLASS, false, false, buttonWidth + 6, buttonHeight, etchedIcon, buttonHeight + 5);
		configureButton(b6, Type.ROUND_GLASS, false, true, buttonWidth - 2, buttonHeight, etchedIcon, buttonHeight - 3);
//		configureButton(b6, Type.ROUND_GLASS, Position.RIGHT, buttonWidth, buttonHeight, etchedIcon);
		ButtonGroup group = new ButtonGroup();
		group.add(tb1);
		group.add(tb2);
		group.add(tb3);
		group = new ButtonGroup();
		group.add(tb4);
		group.add(tb5);
		group.add(tb6);
		group = new ButtonGroup();
		
		leftToolBar.add(Box.createHorizontalStrut(8));
		leftToolBar.add(tb1);
		leftToolBar.add(tb2);
		leftToolBar.add(tb3);
		leftToolBar.add(Box.createHorizontalStrut(8));
		leftToolBar.add(tb4);
		leftToolBar.add(tb5);
		leftToolBar.add(tb6);
		leftToolBar.add(Box.createHorizontalStrut(8));
		leftToolBar.add(b1);
		leftToolBar.add(b2);
		leftToolBar.add(b3);
		
		
		rightToolBar.add(Box.createHorizontalStrut(8));
		rightToolBar.add(b7);
		rightToolBar.add(b8);
		rightToolBar.add(tb11);
		rightToolBar.add(b4);
		rightToolBar.add(b5);
		rightToolBar.add(b6);
//		toolBar.add(b6);
		rightToolBar.add(Box.createHorizontalStrut(8));
		rightToolBar.add(tb7);
		rightToolBar.add(tb8);
		rightToolBar.add(tb9);
		rightToolBar.add(tb10);
		rightToolBar.add(Box.createHorizontalStrut(8));
		tb1.setEnabled(false);
		tb6.setEnabled(false);
		rightToolBar.setBackground(new Color(0xbbbbbb));
		rightToolBar.setFloatable(false);
		
		panel.setLayout(new BorderLayout());
//		panel.add(leftToolBar, new Spri)
		panel.add(leftToolBar, BorderLayout.WEST);
		panel.add(centerToolBar, BorderLayout.CENTER);
		panel.add(rightToolBar, BorderLayout.EAST);
		
		frame.add(panel, BorderLayout.NORTH);
		
		JComponent testComponent = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				drawViewportSwitcher(null, (Graphics2D) g, 49, 43);
			}
		};
		testComponent.setPreferredSize(new Dimension(50, 44));
		centerToolBar.add(testComponent, BorderLayout.CENTER);
//		frame.add(testComponent, BorderLayout.CENTER);
		
		frame.setVisible(true);
	}
	
	private Image createDisabledImage(Image source) {
		BufferedImage image = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		image.createGraphics().drawImage(source, 0, 0, null);
		int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		for (int i = 0; i < pixels.length; i++) {
			int argb = pixels[i];
			int a = (argb >> 24) & 0xff;
			int r = (argb >> 16) & 0xff;
			int g = (argb >> 8) & 0xff;
			int b = argb & 0xff;
			
			int gray = (r + g + b) / 3;
			int alpha = a / 3;
			
			pixels[i] = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
		}
		return image;
	}
	
	
	private Image createTextImage(Font font, String text) {
		font = font.deriveFont(new AffineTransform(1.0, 0, 0, 0.8, 0, 0));
		BufferedImage image = new BufferedImage(100, 30, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		FontRenderContext frc = g2.getFontRenderContext();
		TextLayout textLayout = new TextLayout(text, font, frc);
		
//		textLayout.ge
//		GlyphVector gv = font.createGlyphVector(g.getFontRenderContext(), text);
		Rectangle2D bounds = textLayout.getBounds();
		float height = textLayout.getAscent() + textLayout.getDescent();
		
		
		System.out.println(bounds);
		image = new BufferedImage((int) (bounds.getWidth()) + 5, (int) height + 2, BufferedImage.TYPE_INT_ARGB);
		g2 = image.createGraphics();
		g2.setColor(Color.BLACK);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		textLayout.draw(g2, 1, textLayout.getAscent() + 1);
		return image;
	}
	
	private Image createTransparentImage(Image source, float opacity) {
		BufferedImage image = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		image.createGraphics().drawImage(source, 0, 0, null);
		int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		for (int i = 0; i < pixels.length; i++) {
			int argb = pixels[i];
			int a = (argb >> 24) & 0xff;
			int r = (argb >> 16) & 0xff;
			int g = (argb >> 8) & 0xff;
			int b = argb & 0xff;
			
			int alpha = (int) (a * opacity);
			
			pixels[i] = (alpha << 24) | (r << 16) | (g << 8) | b;
		}
		return image;
	}
	
	private Image createEtchedIcon(Image source) {
		BufferedImage tmp = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		tmp.createGraphics().drawImage(source, 0, 0, null);
		int[] pixels = ((DataBufferInt) tmp.getRaster().getDataBuffer()).getData();
		for (int i = 0; i < pixels.length; i++) {
			int argb = pixels[i];
			int a = (argb >> 24) & 0xff;
			int alpha = a;
			
			pixels[i] = (alpha << 24) | 0xffffff;
		}
		BufferedImage image = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.drawImage(tmp, 0, 1, null);
//		g.drawImage(tmp, 0, 1, null);
		g.drawImage(source, 0, 0, null);
		return image;
	}
	
	private Image createTestIcon() {
		BufferedImage image = new BufferedImage(19, 19, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setPaint(new GradientPaint(0, 0, new Color(0xff0000), 0, 13, new Color(0x0000ff)));
		g.setStroke(new BasicStroke(2));
		g.fillOval(1, 1, 17, 17);
		return image;
	}
	
	private void configureButton(AbstractButton button, Type type, boolean left, boolean right, int width, int height, Image icon) {
		configureButton(button, type, left, right, width, height, icon, height);
	}
	
	private void configureButton(AbstractButton button, Type type, boolean left, boolean right, int width, int height, Image icon, int size) {
		if (icon.getWidth(null) > width) {
			width = icon.getWidth(null);
		}
		Image disabledIcon = createDisabledImage(icon);
		button.setUI(buttonUI);
		button.setBorder(null);
		button.setContentAreaFilled(false);
		button.setIcon(new ImageIcon(createGroupButtonImage(type, left, right, null, width, height, icon, size)));
		button.setRolloverEnabled(true);
		button.setPressedIcon(new ImageIcon(createGroupButtonImage(type, left, right, new float[] {0.90f, 0.90f, 0.90f }, width, height, icon, size)));
		button.setRolloverIcon(new ImageIcon(createGroupButtonImage(type, left, right, new float[] {1.05f, 1.05f, 0.95f }, width, height, icon, size)));
		button.setRolloverSelectedIcon(new ImageIcon(createGroupButtonImage(type, left, right, new float[] {0.90f, 0.90f, 0.80f }, width, height, icon, size)));
		button.setSelectedIcon(new ImageIcon(createGroupButtonImage(type, left, right, new float[] {0.85f, 0.85f, 0.95f }, width, height, icon, size)));
		button.setDisabledIcon(new ImageIcon(createGroupButtonImage(type, left, right, null, width, height, disabledIcon, size)));
	}
}


