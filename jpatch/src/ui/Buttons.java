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
		new Color(0xf7f7f7),
		new Color(0xcacaca),
		new Color(0xc0c0c0),
		new Color(0x999999),
	};
	
	public static void main(String[] args) {
		new Buttons().test();
	}
	
	private Image createGroupButtonImage(Type type, Position position, float[] tint, int width, int height, Image icon) {
		int w = position == Position.CENTER ? width : width + 2;
		int off = 0;
		int ww = 0;
		if (type == Type.ROUND_PLASTIC || type == Type.ROUND_GLASS) {
			if (position == Position.LEFT) {
				off = 2;
				ww = 2;
			} else if (position == Position.RIGHT) {
				ww = 1;
				off = 0;
			}
		}
		BufferedImage image = new BufferedImage(w + ww, height + 8, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.translate(off, 4);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawGroupButton(type, position, tint, g, w - 1, height - 1);
		switch (position) {
		case LEFT:
			g.drawImage(icon, (w - icon.getWidth(null)) / 2 + 1, (height - icon.getHeight(null)) / 2, null);
			break;
		case CENTER:
			g.drawImage(icon, (w - icon.getWidth(null)) / 2, (height - icon.getHeight(null)) / 2, null);
			break;
		case RIGHT:
			g.drawImage(icon, (w - icon.getWidth(null)) / 2 - 1, (height - icon.getHeight(null)) / 2, null);
			break;
		}
		return image;
	}
	
	private void drawGroupButton(Type type, Position position, float[] tint, Graphics2D g, int width, int height) {
		int w = width - 2;
		int h = height - 1;
		int arc = h / 2;
		int h2 = h / 2 + 1;
		int left = position == Position.LEFT ? 1 : 1 - h2;
		int right = position == Position.RIGHT ? w + 2 : w + 2 + h2;
		
		Color[] colors = tintColors(GLASS_COLORS, tint);
		Color[] pColors = tintColors(PLASTIC_COLORS, tint);
		RoundRectangle2D roundRect = new RoundRectangle2D.Float(left, 1, right - left, h - 1, arc, arc);
		
		if (type == Type.ROUND_PLASTIC || type == Type.ROUND_GLASS) {
			g.setPaint(new GradientPaint(0, 0, new Color(0x40000000, true), 0, height, new Color(0x40ffffff, true)));
			g.fill(new RoundRectangle2D.Float(left - 3, -3, right - left + 5, height + 7, height + 5, height + 5));
//			g.setPaint(new GradientPaint(0, 1, new Color(0xbbbbbb), 0, h, new Color(0x888888)));
//			g.fill(new RoundRectangle2D.Float(left - 2, -2, right - left + 3, height + 5, height + 5, height + 5));
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
			int l = position == Position.LEFT ? 2 : position == Position.CENTER ? 0 : 0;
			g.setPaint(new GradientPaint(l, 1, colors[2], 0, h, colors[3]));
			g.fill(new Ellipse2D.Float(l, 0, height, height));
			g.setPaint(new GradientPaint(l, 1, colors[0], 0, h2, colors[1]));
			g.setClip(new Ellipse2D.Float(l, 0, height, height));
			g.fill(new Ellipse2D.Float(l, 0, height, height / 2));
			g.setClip(null);
			g.setPaint(new GradientPaint(l, 0, new Color(0xffffffff, true), 0, height, new Color(0x00ffffff, true)));
			g.setStroke(new BasicStroke(1.5f));
			g.draw(new Ellipse2D.Float(l, 1, height - 1, height - 3));
			g.setPaint(new GradientPaint(l, 0, new Color(0x00000000, true), 0, height, new Color(0x28000000, true)));
			g.draw(new Ellipse2D.Float(l, 1, height - 1, height - 3));
			g.setPaint(new GradientPaint(l, 0, new Color(0x80000000, true), 0, height, new Color(0xc0ffffff, true)));
			g.setStroke(new BasicStroke(1));
			g.draw(new Ellipse2D.Float(l, 0, height - 1, height - 1));
			break;
		case ROUND_PLASTIC:
			l = position == Position.LEFT ? 2 : position == Position.CENTER ? 0 : 0;
//			g.setClip(new Ellipse2D.Float(l, 0, height, height));
			g.setPaint(new LinearGradientPaint(0, 1, 0, h, new float[] { 0.0f, 0.2f, 0.8f, 1.0f }, tintColors(PLASTIC_COLORS, tint)));
			g.fill(new Ellipse2D.Float(l, 0, height, height));
//			g.setPaint(new GradientPaint(l, 1, pColors[1], 0, h2, pColors[2]));
//			
//			g.fill(new Rectangle.Float(l, 0, height, height / 2));
			g.setClip(null);
			g.setPaint(new GradientPaint(l, 0, new Color(0xffffffff, true), 0, height, new Color(0x00ffffff, true)));
			g.setStroke(new BasicStroke(1.0f));
			g.draw(new Ellipse2D.Float(l, 1, height - 1, height - 3));
			g.setPaint(new GradientPaint(l, 0, new Color(0x00000000, true), 0, height, new Color(0x40000000, true)));
//			g.draw(new Ellipse2D.Float(l, 1, height - 1, height - 3));
			g.setPaint(new GradientPaint(l, 0, new Color(0x80000000, true), 0, height, new Color(0x00000000, true)));
//			g.setStroke(new BasicStroke(1.0f));
			g.draw(new Ellipse2D.Float(l, 0, height - 1, height - 1));
			g.setStroke(new BasicStroke(1.0f));
			break;
		}
		if (type == Type.GLASS || type == Type.PLASTIC) {
			if (position != Position.LEFT) {
				g.setColor(new Color(0x60ffffff, true));
				g.drawLine(0, 1, 0, h - 1);
			}
			if (position != Position.RIGHT) {
				g.setColor(new Color(0x40000000, true));
				g.drawLine(width, 1, width, h - 1);
			}
			g.setClip(null);
			g.setPaint(new GradientPaint(0, 0, new Color(0x40000000, true), 0, height, new Color(0x80ffffff, true)));
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
		g.setClip(roundRect);
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
		g.drawLine(w2, 1, w2, h - 1);
		g.drawLine(1, h2, w - 1, h2);
		g.setColor(new Color(0x40000000, true));
		g.drawLine(w2 - 1, 1, w2 - 1, h - 1);
		g.drawLine(1, h2 - 1, w - 1, h2 - 1);
		
		g.setClip(null);
		g.setPaint(new GradientPaint(0, 0, new Color(0x80000000, true), 0, height, new Color(0x80ffffff, true)));
		g.draw(new RoundRectangle2D.Float(0, 0, w, h, arc + 0, arc + 0));
		
		Font font = new Font("sans serif", Font.BOLD, 12);
		g.drawImage(createEtchedIcon(createTextImage(font, "1")), 0, 0, null);
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
	
	private void test() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(640, 480);
		
		frame.setBackground(new Color(0xaaaaaa));
		frame.setLayout(new BorderLayout());
		//frame.add(component);
		
		
		JToolBar toolBar = new JToolBar() {
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
//				g2.setPaint(new GradientPaint(0, 0, new Color(0xb0b0b0), 0, getHeight(), new Color(0xa0a0a0)));
//				g2.fillRect(0, 0, getWidth(), getHeight());
				
				g2.setPaint(new GradientPaint(getWidth() * 4 / 10, 0, new Color(0xd0d0d0), getWidth() * 1 / 10, getHeight(), new Color(0xb0b0b0)));
				g2.fillRect(0, 0, getWidth() / 2, getHeight());
				g2.setPaint(new GradientPaint(getWidth() * 6 / 10, 0, new Color(0xd0d0d0), getWidth() * 9 / 10, getHeight(), new Color(0xb0b0b0)));
				g2.fillRect(getWidth() / 2, 0, getWidth() / 2, getHeight());
				g2.setPaint(new GradientPaint(0, 0, new Color(0x00000000, true), 0, getHeight(), new Color(0x20000000, true)));
				g2.fillRect(0, 0, getWidth(), getHeight());
			}
		};
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
		
		int buttonWidth = 27;
		int buttonHeight = 24;
		
		Image icon = createTransparentImage(createTestIcon(), 0.75f);
		
		Font font = new Font("sans serif", Font.BOLD, 12);
//
//		icon = createEtchedIcon(createTransparentImage(createTextImage(font, "Open"), 0.75f));
		Image etchedIcon = createEtchedIcon(icon);
		
		configureButton(tb1, Type.GLASS, Position.LEFT, buttonWidth, buttonHeight, createTransparentImage(createEtchedIcon(createTextImage(font, "New")), 0.75f));
		configureButton(tb2, Type.GLASS, Position.CENTER, buttonWidth, buttonHeight, createTransparentImage(createEtchedIcon(createTextImage(font, "Open")), 0.75f));
		configureButton(tb3, Type.GLASS, Position.RIGHT, buttonWidth, buttonHeight, createTransparentImage(createEtchedIcon(createTextImage(font, "Save")), 0.75f));
		configureButton(tb4, Type.PLASTIC, Position.LEFT, buttonWidth, buttonHeight, etchedIcon);
		configureButton(tb5, Type.PLASTIC, Position.CENTER, buttonWidth, buttonHeight, etchedIcon);
		configureButton(tb6, Type.PLASTIC, Position.RIGHT, buttonWidth, buttonHeight, etchedIcon);
		buttonWidth = 24;
		configureButton(b1, Type.ROUND_PLASTIC, Position.LEFT, buttonWidth, buttonHeight, etchedIcon);
		configureButton(b2, Type.ROUND_PLASTIC, Position.RIGHT, buttonWidth, buttonHeight, etchedIcon);
		configureButton(b3, Type.ROUND_GLASS, Position.LEFT, buttonWidth, buttonHeight, etchedIcon);
		configureButton(b4, Type.ROUND_GLASS, Position.CENTER, buttonWidth, buttonHeight, etchedIcon);
		configureButton(b5, Type.ROUND_GLASS, Position.CENTER, buttonWidth, buttonHeight, etchedIcon);
		configureButton(b6, Type.ROUND_GLASS, Position.RIGHT, buttonWidth, buttonHeight, etchedIcon);
		ButtonGroup group = new ButtonGroup();
		group.add(tb1);
		group.add(tb2);
		group.add(tb3);
		group = new ButtonGroup();
		group.add(tb4);
		group.add(tb5);
		group.add(tb6);
		
		toolBar.add(Box.createHorizontalStrut(8));
		toolBar.add(tb1);
		toolBar.add(tb2);
		toolBar.add(tb3);
		toolBar.add(Box.createHorizontalStrut(8));
		toolBar.add(tb4);
		toolBar.add(tb5);
		toolBar.add(tb6);
		toolBar.add(Box.createHorizontalStrut(8));
		toolBar.add(b1);
		toolBar.add(b2);
		toolBar.add(Box.createHorizontalStrut(8));
		toolBar.add(b3);
		toolBar.add(b4);
		toolBar.add(b5);
		toolBar.add(b6);
		toolBar.add(Box.createHorizontalStrut(8));
		tb1.setEnabled(false);
		tb6.setEnabled(false);
		toolBar.setBackground(new Color(0xbbbbbb));
		toolBar.setFloatable(false);
		frame.add(toolBar, BorderLayout.NORTH);
		
		JComponent testComponent = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				drawViewportSwitcher(null, (Graphics2D) g, 49, 43);
			}
		};
		testComponent.setPreferredSize(new Dimension(50, 44));
		toolBar.add(testComponent);
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
		BufferedImage image = new BufferedImage(13, 13, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setPaint(new GradientPaint(0, 0, new Color(0xff0000), 0, 13, new Color(0x0000ff)));
		g.setStroke(new BasicStroke(2));
		g.fillOval(1, 1, 11, 11);
		return image;
	}
	
	private void configureButton(AbstractButton button, Type type, Position position, int width, int height, Image icon) {
		if (icon.getWidth(null) > width) {
			width = icon.getWidth(null);
		}
		Image disabledIcon = createDisabledImage(icon);
		button.setUI(buttonUI);
		button.setBorder(null);
		button.setContentAreaFilled(false);
		button.setIcon(new ImageIcon(createGroupButtonImage(type, position, null, width, height, icon)));
		button.setRolloverEnabled(true);
		button.setPressedIcon(new ImageIcon(createGroupButtonImage(type, position, new float[] {0.90f, 0.90f, 0.90f }, width, height, icon)));
		button.setRolloverIcon(new ImageIcon(createGroupButtonImage(type, position, new float[] {1.05f, 1.05f, 1.05f }, width, height, icon)));
		button.setRolloverSelectedIcon(new ImageIcon(createGroupButtonImage(type, position, new float[] {0.90f, 0.90f, 0.90f }, width, height, icon)));
		button.setSelectedIcon(new ImageIcon(createGroupButtonImage(type, position, new float[] {0.85f, 0.85f, 0.85f }, width, height, icon)));
		button.setDisabledIcon(new ImageIcon(createGroupButtonImage(type, position, null, width, height, disabledIcon)));
	}
}


