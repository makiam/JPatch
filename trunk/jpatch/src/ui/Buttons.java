package ui;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

import org.apache.batik.ext.awt.LinearGradientPaint;
import static java.awt.RenderingHints.*;

public class Buttons {
	public static final int BUTTON_HEIGHT = 21;
	public static final int BUTTON_WIDTH = 23;
	public static final int BUTTON_SIDE = 4;

	public static enum Type { GLASS, PLASTIC }
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
		BufferedImage image = new BufferedImage(w, height + 8, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.translate(0, 4);
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
		
		RoundRectangle2D roundRect = new RoundRectangle2D.Float(left, 1, right - left, h - 1, arc, arc);
		
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
		}
		if (position != Position.LEFT) {
			g.setColor(new Color(0x60ffffff, true));
			g.drawLine(0, 1, 0, h - 1);
		}
		if (position != Position.RIGHT) {
			g.setColor(new Color(0x40000000, true));
			g.drawLine(width, 1, width, h - 1);
		}
		g.setClip(null);
		g.setPaint(new GradientPaint(0, 0, new Color(0x80000000, true), 0, height, new Color(0xc0ffffff, true)));
		g.draw(new RoundRectangle2D.Float(left - 1, 0, right - left + 1, height - 1, arc + 1, arc + 1));
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
				g2.setPaint(new GradientPaint(0, 0, new Color(0xb0b0b0), 0, getHeight(), new Color(0xa0a0a0)));
				g2.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		JToggleButton tb1 = new JToggleButton();
		JToggleButton tb2 = new JToggleButton();
		JToggleButton tb3 = new JToggleButton();
		JToggleButton tb4 = new JToggleButton();
		JToggleButton tb5 = new JToggleButton();
		JToggleButton tb6 = new JToggleButton();
		int buttonWidth = 57;
		int buttonHeight = 24;
		
		Image icon = createTransparentImage(createTestIcon(), 0.75f);
		
		Font font = new Font("sans serif", Font.BOLD, 12);
//
//		icon = createEtchedIcon(createTransparentImage(createTextImage(font, "Open"), 0.75f));
		Image etchedIcon = createEtchedIcon(icon);
		
		configureButton(tb1, Type.GLASS, Position.LEFT, buttonWidth, buttonHeight, createTransparentImage(createEtchedIcon(createTextImage(font, "New")), 0.75f));
		configureButton(tb2, Type.GLASS, Position.CENTER, buttonWidth, buttonHeight, createTransparentImage(createEtchedIcon(createTextImage(font, "Open")), 0.75f));
		configureButton(tb3, Type.GLASS, Position.RIGHT, buttonWidth, buttonHeight, createTransparentImage(createEtchedIcon(createTextImage(font, "Save")), 0.75f));
		buttonWidth = 27;
		configureButton(tb4, Type.PLASTIC, Position.LEFT, buttonWidth, buttonHeight, etchedIcon);
		configureButton(tb5, Type.PLASTIC, Position.CENTER, buttonWidth, buttonHeight, etchedIcon);
		configureButton(tb6, Type.PLASTIC, Position.RIGHT, buttonWidth, buttonHeight, etchedIcon);
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
		tb1.setEnabled(false);
		tb6.setEnabled(false);
		toolBar.setBackground(new Color(0xbbbbbb));
		toolBar.setFloatable(false);
		frame.add(toolBar, BorderLayout.NORTH);
		
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
		
		System.out.println(bounds);
		image = new BufferedImage((int) (bounds.getWidth()) + 3, (int) bounds.getHeight() + 2, BufferedImage.TYPE_INT_ARGB);
		g2 = image.createGraphics();
		g2.setColor(Color.BLACK);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		textLayout.draw(g2, 0, font.getSize() * 0.75f);
//		int w = image.getWidth();
//		int[] sourcePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
//		image = new BufferedImage(image.getWidth() / 2, (int) image.getHeight() / 2, BufferedImage.TYPE_INT_ARGB);
//		int[] dstPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
//		for (int y = 0; y < image.getHeight() - 1; y++) {
//			for (int x = 0; x < image.getWidth(); x++) {
//				int a = 0, r = 0, g = 0, b = 0;
//				for (int yo = 1; yo < 3; yo++) {
//					for (int xo = 0; xo < 2; xo++) {
//						a += ((sourcePixels[x * 2 + xo + (y * 2 + yo) * w]) >> 24) & 0xff;
//						r += ((sourcePixels[x * 2 + xo + (y * 2 + yo) * w]) >> 16) & 0xff;
//						g += ((sourcePixels[x * 2 + xo + (y * 2 + yo) * w]) >> 8) & 0xff;
//						b += (sourcePixels[x * 2 + xo + (y * 2 + yo) * w]) & 0xff;
//					}
//				}
//				a /= 4;
//				r /= 4;
//				g /= 4;
//				b /= 4;
//				dstPixels[x + y * image.getWidth()] = (a << 24) | (r << 16) | (g << 8) | b;
//			}
//		}
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
		Image disabledIcon = createDisabledImage(icon);
		button.setBorder(null);
		button.setContentAreaFilled(false);
		button.setIcon(new ImageIcon(createGroupButtonImage(type, position, null, width, height, icon)));
		button.setRolloverEnabled(true);
		button.setRolloverIcon(new ImageIcon(createGroupButtonImage(type, position, new float[] {1.00f, 1.05f, 1.10f }, width, height, icon)));
		button.setSelectedIcon(new ImageIcon(createGroupButtonImage(type, position, new float[] {0.82f, 0.88f, 0.94f }, width, height, icon)));
		button.setDisabledIcon(new ImageIcon(createGroupButtonImage(type, position, null, width, height, disabledIcon)));
	}
}


