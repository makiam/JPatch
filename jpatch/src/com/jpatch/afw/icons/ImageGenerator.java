package com.jpatch.afw.icons;

import com.jpatch.afw.attributes.ScalarAttribute;
import com.jpatch.afw.attributes.AttributePreChangeAdapter;
import com.jpatch.afw.attributes.AttributePostChangeListener;
import com.jpatch.afw.attributes.StateMachine;
import com.jpatch.afw.ui.*;
import com.jpatch.ui.ViewportSwitcher;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

import javax.swing.*;

import org.apache.batik.ext.awt.LinearGradientPaint;
import ui.JPatchToolBar;
import static com.jpatch.afw.icons.IconSet.*;

public class ImageGenerator {
	private BufferedImage image, stencil;
	private ImagePanel zoom = new ImagePanel();
	private JFrame frame;
	private int[][] offsets = new int[][] {	// style, type, width, height
			{ Style.GLOSSY.ordinal(), Type.LEFT.ordinal(), 30, 24 },
			{ Style.GLOSSY.ordinal(), Type.CENTER.ordinal(), 28, 24 },
			{ Style.GLOSSY.ordinal(), Type.RIGHT.ordinal(), 30, 24 },
			{ Style.FROSTED.ordinal(), Type.LEFT.ordinal(), 30, 24 },
			{ Style.FROSTED.ordinal(), Type.CENTER.ordinal(), 28, 24 },
			{ Style.FROSTED.ordinal(), Type.RIGHT.ordinal(), 30, 24 },
			{ Style.BRUSHED.ordinal(), Type.LEFT.ordinal(), 30, 24 },
			{ Style.BRUSHED.ordinal(), Type.CENTER.ordinal(), 28, 24 },
			{ Style.BRUSHED.ordinal(), Type.RIGHT.ordinal(), 30, 24 },
			{ Style.DARK.ordinal(), Type.LEFT.ordinal(), 34, 34 },
			{ Style.DARK.ordinal(), Type.CENTER.ordinal(), 32, 34 },
			{ Style.DARK.ordinal(), Type.RIGHT.ordinal(), 34, 34 },
			{ Style.GLOSSY.ordinal(), Type.SINGLE.ordinal(), 30, 24 },
			{ Style.FROSTED.ordinal(), Type.SINGLE.ordinal(), 30, 24 },
			{ Style.BRUSHED.ordinal(), Type.SINGLE.ordinal(), 30, 24 },
			{ Style.GLOSSY.ordinal(), Type.ROUND.ordinal(), 30, 30 },
			{ Style.FROSTED.ordinal(), Type.ROUND.ordinal(), 30, 30 },
			{ Style.BRUSHED.ordinal(), Type.ROUND.ordinal(), 30, 30 },
			{ Style.GLOSSY.ordinal(), Type.LARGE.ordinal(), 36, 36 },
			{ Style.FROSTED.ordinal(), Type.LARGE.ordinal(), 36, 36 },
			{ Style.BRUSHED.ordinal(), Type.LARGE.ordinal(), 36, 36 },
			{ Style.TINY.ordinal(), Type.LEFT.ordinal(), 19, 16 },
			{ Style.TINY.ordinal(), Type.CENTER.ordinal(), 18, 16 },
			{ Style.TINY.ordinal(), Type.RIGHT.ordinal(), 19, 16 },
	};
	
	int n = 0;
	
	public static void main(String[] args) throws Exception {
		new ImageGenerator().test();
	}
	
	void test2() throws Exception {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		ObjectInputStream ois = new ObjectInputStream(ClassLoader.getSystemResourceAsStream("com/jpatch/afw/icons/icons"));
		IconSet iconSet = (IconSet) ois.readObject();
		ois.close();
		
		JToolBar toolBar = new JPatchToolBar();
		toolBar.setFloatable(false);
		toolBar.setBorder(null);
		JToggleButton b0 = new JToggleButton();
		JToggleButton b1 = new JToggleButton();
		JToggleButton b2 = new JToggleButton();
		JToggleButton b3 = new JToggleButton();
		b0.setToolTipText("Vertex mode");
		b1.setToolTipText("Edge mode");
		b2.setToolTipText("Face mode");
		b3.setToolTipText("Object mode");
		Image test = makeIcon(0);
		Image i0 = makeIcon(1);
		Image i1 = makeIcon(2);
		Image i2 = makeIcon(3);
		Image i3 = makeIcon(4);
		ButtonGroup bg0 = new ButtonGroup();
		bg0.add(b0);
		bg0.add(b1);
		bg0.add(b2);
		bg0.add(b3);
		JToggleButton b4 = new JToggleButton();
		JToggleButton b5 = new JToggleButton();
		JToggleButton b6 = new JToggleButton();
		b4.setToolTipText("Move view");
		b5.setToolTipText("Zoom view");
		b6.setToolTipText("Rotate view");
		Image i4 = makeIcon(5);
		Image i5 = makeIcon(6);
		Image i6 = makeIcon(7);
		ButtonGroup bg1 = new ButtonGroup();
		bg1.add(b4);
		bg1.add(b5);
		bg1.add(b6);
		JButton b7 = new JButton();
		JButton b8 = new JButton();
		b7.setToolTipText("Undo");
		b8.setToolTipText("Redo");
		Image i7 = makeIcon(8);
		Image i8 = makeIcon(9);
		JToggleButton b9 = new JToggleButton();
		JToggleButton b10 = new JToggleButton();
		JToggleButton b11 = new JToggleButton();
		JToggleButton b12 = new JToggleButton();
		JToggleButton b13 = new JToggleButton();
		JToggleButton b14 = new JToggleButton();
		JToggleButton b15 = new JToggleButton();
		b9.setToolTipText("Default tool");
		b10.setToolTipText("Move tool");
		b11.setToolTipText("Scale tool");
		b12.setToolTipText("Rotate tool");
		b13.setToolTipText("Extrude tool");
		b14.setToolTipText("Lathe tool");
		Image i9 = makeIcon(10);
		Image i10 = makeIcon(11);
		Image i11 = makeIcon(12);
		Image i12 = makeIcon(13);
		Image i13 = makeIcon(14);
		Image i14 = makeIcon(15);
		Image i15 = makeIcon(16);
		bg1.add(b9);
		bg1.add(b10);
		bg1.add(b11);
		bg1.add(b12);
		bg1.add(b13);
		bg1.add(b14);
		bg1.add(b15);
		
		b13.setEnabled(false);
		b14.setEnabled(false);
		b15.setEnabled(false);
		
		JToggleButton b16 = new JToggleButton();
		b16.setToolTipText("Snap to grid");
		Image i16 = makeIcon(17);
		
		iconSet.configureButton(b0, Style.FROSTED, Type.LEFT, i0);
		iconSet.configureButton(b1, Style.FROSTED, Type.CENTER, i1);
		iconSet.configureButton(b2, Style.FROSTED, Type.CENTER, i2);
		iconSet.configureButton(b3, Style.FROSTED, Type.RIGHT, i3);
		iconSet.configureButton(b4, Style.GLOSSY, Type.LEFT, i4);
		iconSet.configureButton(b5, Style.GLOSSY, Type.CENTER, i5);
		iconSet.configureButton(b6, Style.GLOSSY, Type.RIGHT, i6);
		iconSet.configureButton(b7, Style.DARK, Type.LEFT, i7);
		iconSet.configureButton(b8, Style.DARK, Type.RIGHT, i8);
		iconSet.configureButton(b9, Style.BRUSHED, Type.LEFT, i9);
		iconSet.configureButton(b10, Style.BRUSHED, Type.CENTER, i10);
		iconSet.configureButton(b11, Style.BRUSHED, Type.CENTER, i11);
		iconSet.configureButton(b12, Style.BRUSHED, Type.RIGHT, i12);
		iconSet.configureButton(b13, Style.BRUSHED, Type.LEFT, i13);
		iconSet.configureButton(b14, Style.BRUSHED, Type.CENTER, i14);
		iconSet.configureButton(b15, Style.BRUSHED, Type.RIGHT, i15);
		iconSet.configureButton(b16, Style.FROSTED, Type.SINGLE, i16);
		
		StateMachine<ViewportSwitcher.Mode> sm = new StateMachine<ViewportSwitcher.Mode>(ViewportSwitcher.Mode.class, ViewportSwitcher.Mode.VIEWPORT_1);
		sm.addAttributeListener(new AttributePreChangeAdapter() {
			public void attributeHasChanged(ScalarAttribute source) {
				System.out.println(((StateMachine) source).getState());
			}
		});
		toolBar.add(b7);
		toolBar.add(b8);
		toolBar.add(Box.createHorizontalStrut(16));
		toolBar.add(b4);
		toolBar.add(b5);
		toolBar.add(b6);
		toolBar.add(Box.createHorizontalStrut(32));
		toolBar.add(new ViewportSwitcher(sm).getComponent());
		toolBar.add(Box.createHorizontalStrut(32));
		
		toolBar.add(b0);
		toolBar.add(b1);
		toolBar.add(b2);
		toolBar.add(b3);
		toolBar.add(Box.createHorizontalStrut(8));
		toolBar.add(b16);
		toolBar.add(Box.createHorizontalStrut(8));
		toolBar.add(b9);
		toolBar.add(b10);
		toolBar.add(b11);
		toolBar.add(b12);
		toolBar.add(Box.createHorizontalStrut(8));
		toolBar.add(b13);
		toolBar.add(b14);
		toolBar.add(b15);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(toolBar, BorderLayout.CENTER);
		frame.add(panel, BorderLayout.NORTH);
		frame.setSize(1024, 768);
		frame.setVisible(true);
		iconSet = null;
	}
	
	void test() throws Exception {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		image = new BufferedImage(1000, 50, BufferedImage.TYPE_INT_ARGB);
		stencil = new BufferedImage(1000, 50, BufferedImage.TYPE_BYTE_GRAY);
		ImagePanel imagePanel = new ImagePanel(image);
		ImagePanel stencilPanel = new ImagePanel(stencil);
		Graphics2D ig = image.createGraphics();
		Graphics2D sg = stencil.createGraphics();
		configureGraphics(ig);
		configureGraphics(sg);
//		drawBackground(g);
		ig.translate(1, 1);
		sg.translate(1, 1);
		if (true) {
			drawGroupButtons(Style.GLOSSY, 29, 28, 22, ig, sg, false);
			drawGroupButtons(Style.FROSTED, 29, 28, 22, ig, sg, false);
			drawGroupButtons(Style.BRUSHED, 29, 28, 22, ig, sg, false);
			drawGroupButtons(Style.DARK, 28, 28, 28, ig, sg, true);
			drawButton(Style.GLOSSY, 28, 22, ig, sg, false);
			drawButton(Style.FROSTED, 28, 22, ig, sg, false);
			drawButton(Style.BRUSHED, 28, 22, ig, sg, false);
			drawButton(Style.GLOSSY, 28, 28, ig, sg, true);
			drawButton(Style.FROSTED, 28, 28, ig, sg, true);
			drawButton(Style.BRUSHED, 28, 28, ig, sg, true);
			drawButton(Style.GLOSSY, 34, 34, ig, sg, true);
			drawButton(Style.FROSTED, 34, 34, ig, sg, true);
			drawButton(Style.BRUSHED, 34, 34, ig, sg, true);
			drawGroupButtons(Style.TINY, 19, 18, 16, ig, sg, false);
		}
//		drawSwitcher(48, 42, g);
//		g.setTransform(at);
		
//		Image im = makeIcon(7);
//		g.drawImage(im, 16, 12, null);
//		im = makeIcon(8);
//		g.drawImage(im, 398, 14, null);
//		im = makeIcon(9);
//		g.drawImage(im, 398 + 28 + 6, 14, null);
		
		IconSet iconSet = new IconSet();
		for (int i = 0; i < offsets.length; i++) {
			BufferedImage img = drawImage(i);
			BufferedImage stc = drawStencil(i);
			Style style = Style.values()[offsets[i][0]];
			Type type = Type.values()[offsets[i][1]];
			int xoff = 0, yoff = 4;
			if (style == Style.DARK) {
				yoff = 9;
				if (type == Type.LEFT) xoff = 10;
				if (type == Type.CENTER) xoff = 8;
				if (type == Type.RIGHT) xoff = 8;
			} else {
				if (type == Type.LEFT) xoff = 8;
				if (type == Type.CENTER) xoff = 6;
				if (type == Type.RIGHT) xoff = 6;
			}
			if (type == Type.SINGLE) {
				xoff = 7;
			}
			if (style == Style.TINY) {
				xoff = 1;
				yoff = 0;
			}
			iconSet.setIcon(style, type, img, stc, xoff, yoff);
		}
		
		
		
		File file = new File("src/com/jpatch/afw/icons/buttonBorders.iconset");
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(iconSet);
		oos.close();
		
//		System.exit(0);
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("src/com/jpatch/afw/icons/buttonBorders.iconset"));
		iconSet = (IconSet) ois.readObject();
		ois.close();
		
		frame.setLayout(new BorderLayout());
		
		if (true) {
			frame.add(imagePanel.getComponent(), BorderLayout.NORTH);
			frame.add(stencilPanel.getComponent(), BorderLayout.CENTER);
			frame.addKeyListener(new KeyAdapter() {
	
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_LEFT) {
						n = n - 1;
						if (n < 0) {
							n = offsets.length - 1;
						}
						drawx(n);
					} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
						n = n + 1;
						if (n >= offsets.length) {
							n = 0;
						}
						drawx(n);
					}
				}
				
			});
			frame.add(zoom.getComponent(), BorderLayout.SOUTH);
			frame.setVisible(true);
		} 
	}
	
	void drawx(int num) {
		BufferedImage zimage = drawImage(num);
		
		Image zoomedImage = zimage.getScaledInstance(zimage.getWidth() * 8, zimage.getHeight() * 8, BufferedImage.SCALE_FAST);
		zoom.setImage(zoomedImage);
		zoom.getComponent().invalidate();
		frame.doLayout();
		frame.repaint();
	}
	
	BufferedImage drawImage(int n) {
		int xoff = n > 20 ? 1 : 0;
		int yoff = n > 20 ? -1 : 0;
		for (int i = 0; i < n; i++) {
			xoff += offsets[i][2];
		}
		BufferedImage i = new BufferedImage(offsets[n][2], offsets[n][3], BufferedImage.TYPE_INT_ARGB);
		i.createGraphics().drawImage(image, -xoff, yoff, null);
		return i;
	}
	
	BufferedImage drawStencil(int n) {
		int xoff = n > 20 ? 2 : 0;
		int yoff = n > 20 ? -2 : 0;
		for (int i = 0; i < n; i++) {
			xoff += offsets[i][2];
		}
		BufferedImage s = new BufferedImage(offsets[n][2], offsets[n][3], BufferedImage.TYPE_BYTE_GRAY);
		s.createGraphics().drawImage(stencil, -xoff, yoff, null);
		return s;
	}
	
	
	
	void drawButton(Style style, int width, int height, Graphics2D ig, Graphics2D sg, boolean round) {
		drawXButton(style, width, height, ig, sg, round);
		ig.translate(width + 2, 0);
		sg.translate(width + 2, 0);
	}
	
	void drawXButton(Style style, int width, int height, Graphics2D ig, Graphics2D sg, boolean round) {
		int outerWidth = width;
		int innerWidth = outerWidth - 2;
		int innerHeight = height - 2;
		RoundRectangle2D outerRect, innerRect, ooRect;
		if (round) {
			outerRect = new RoundRectangle2D.Float(0, 0, outerWidth, height, height, height);
			innerRect = new RoundRectangle2D.Float(1, 1, innerWidth, innerHeight, innerHeight, innerHeight);
			ooRect = new RoundRectangle2D.Float(-1, -1, outerWidth + 2, height + 2, height + 2, height + 2);
		} else {
			ooRect = new RoundRectangle2D.Float(-1, -1, outerWidth + 2, height + 2, (height + 2) / 2.1f, (height + 2) / 2.1f);
			outerRect = new RoundRectangle2D.Float(0, 0, outerWidth, height, height / 2.3f, height / 2.3f);
			innerRect = new RoundRectangle2D.Float(1, 1, innerWidth, innerHeight, innerHeight / 2.5f, innerHeight / 2.5f);
		}
		sg.setColor(Color.WHITE);
		sg.fill(innerRect);
		ig.setPaint(new GradientPaint(0, 0, new Color(0x20000000, true), 0, height, new Color(0x80ffffff, true)));
		ig.fill(ooRect);
		ig.setColor(new Color(0x80000000, true));
		ig.fill(outerRect);
		switch (style) {
		case GLOSSY:
			float halfHeight = innerHeight / 2.0f;
			Area area1 = new Area();
			area1.add(new Area(new Arc2D.Float(1, 1 + halfHeight, halfHeight * 2, halfHeight * 2, 90, 90, Arc2D.PIE)));
			area1.add(new Area(new Rectangle2D.Float(1 + halfHeight, 1 + halfHeight, innerWidth / 2.0f, halfHeight)));
			Area area2 = new Area();
			area2.add(new Area(new Rectangle2D.Float(1 + innerWidth / 2.0f, 1, innerWidth / 2.0f, innerHeight)));
			area2.subtract(new Area(new Ellipse2D.Float(1 + innerWidth - innerHeight, 1 - halfHeight, halfHeight * 2, halfHeight * 2)));
			area2.subtract(new Area(new Rectangle2D.Float(1 + innerWidth / 2.0f, 1, innerWidth / 2.0f - halfHeight, halfHeight)));
			area1.add(area2);
			area1.intersect(new Area(innerRect));
			ig.setPaint(new GradientPaint(0, 1, new Color(0xe4e4e4), 0, 1 + innerHeight, new Color(0xffffff)));
			ig.fill(innerRect);
			ig.setPaint(new GradientPaint(0, 1, new Color(0xb8b8b8), 0, 1 + innerHeight, new Color(0xffffff)));
			ig.fill(area1);
			break;
		case FROSTED:
//			g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.3f, 0.7f, 1.0f }, new Color[] { new Color(1.0f, 1.0f, 1.0f), new Color(0.85f, 0.85f, 0.85f), new Color(0.7f, 0.7f, 0.7f), new Color(0.50f, 0.50f, 0.50f) } ));
			ig.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.5f, 1.0f }, new Color[] { new Color(1.00f, 1.00f, 1.00f), new Color(0.85f, 0.85f, 0.85f), new Color(0.90f, 0.90f, 0.90f) } ));
//			g.setPaint(new GradientPaint(0, 1, new Color(0xffffff), 0, 1 + innerHeight, new Color(0xa0a0a0)));
			ig.fill(innerRect);
			break;
		case TINY:
//			g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.3f, 0.7f, 1.0f }, new Color[] { new Color(1.0f, 1.0f, 1.0f), new Color(0.85f, 0.85f, 0.85f), new Color(0.7f, 0.7f, 0.7f), new Color(0.50f, 0.50f, 0.50f) } ));
			ig.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.45f, 0.45f, 1.0f }, new Color[] { new Color(0.9f, 0.9f, 0.9f), new Color(1.0f, 1.0f ,1.0f), new Color(0.85f, 0.85f, 0.85f), new Color(0.9f, 0.9f, 0.9f) } ));
//			g.setPaint(new GradientPaint(0, 1, new Color(0xffffff), 0, 1 + innerHeight, new Color(0xa0a0a0)));
			ig.fill(innerRect);
			break;
		case BRUSHED:
//			g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.3f, 0.7f, 1.0f }, new Color[] { new Color(1.0f, 1.0f, 1.0f), new Color(0.85f, 0.85f, 0.85f), new Color(0.7f, 0.7f, 0.7f), new Color(0.50f, 0.50f, 0.50f) } ));
//			g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.5f, 1.0f }, new Color[] { new Color(0.95f, 0.95f, 0.95f), new Color(0.75f, 0.75f, 0.75f), new Color(0.80f, 0.80f, 0.80f) } ));
			ig.setPaint(new GradientPaint(0, 1, new Color(0xffffff), 0, 1 + innerHeight, new Color(0.75f, 0.75f, 0.75f)));
			ig.fill(innerRect);
			break;
		case DARK:
			ig.setPaint(new GradientPaint(0, 1, new Color(0x545454), 0, 1 + innerHeight, new Color(0x444444)));
			ig.fill(innerRect);
			ig.setPaint(new GradientPaint(0, 1, new Color(0xa0a0a0), 0, 1 + innerHeight * 0.5f, new Color(0x505050)));
			Area area = new Area(new RoundRectangle2D.Float(1, 1, innerWidth, innerHeight * 0.5f, innerHeight * 0.5f, innerHeight * 0.5f));
			area.intersect(new Area(innerRect));
			ig.fill(area);
			if (round) {
				ig.setPaint(new GradientPaint(0, 1, new Color(0xffffffff, true), 0, 1 + innerHeight * 0.25f, new Color(0x00ffffff, true)));
			} else {
				ig.setPaint(new GradientPaint(0, 1, new Color(0x80ffffff, true), 0, 1 + innerHeight * 0.25f, new Color(0x00ffffff, true)));
			}
			ig.draw(innerRect);
			break;
		}
	}
	
	void drawGroupButtons(Style style, int borderWidth, int centerWidth, int height, Graphics2D ig, Graphics2D sg, boolean round) {
		int outerWidth = 2 * borderWidth + centerWidth;
		int innerHeight = height - 2;
		
		if (round) {
			ig.translate(2, 2);
			sg.translate(2, 2);
			outerWidth = 3 * height + 10;
			RoundRectangle2D.Float outerRect = new RoundRectangle2D.Float(-3, -3, outerWidth + 6, height + 6, height + 6, height + 6);
			RoundRectangle2D.Float innerRect = new RoundRectangle2D.Float(-2, -2, outerWidth + 4, height + 4, height + 4, height + 6);
			ig.setPaint(new GradientPaint(0, -3, new Color(0x40000000, true), 0, height + 6, new Color(0x80ffffff, true)));
			ig.fill(outerRect);
//			g.setPaint(new GradientPaint(0, -2, new Color(0xb0b0b0), 0, height + 4, new Color(0xd0d0d0)));
//			g.fill(innerRect);
			ig.translate(1, 0);
			sg.translate(1, 0);
			drawXButton(style, height, height, ig, sg, true);
			ig.translate(height + 4, 0);
			sg.translate(height + 4, 0);
			drawXButton(style, height, height, ig, sg, true);
			ig.translate(height + 4, 0);
			sg.translate(height + 4, 0);
			drawXButton(style, height, height, ig, sg, true);
			ig.translate(height + 4, 0);
			sg.translate(height + 4, 0);
//			ig.translate(-2 * (height + 2) - 1, 0);
//			sg.translate(-2 * (height + 2) - 1, 0);
			ig.translate(1, -2);
			sg.translate(1, -2);	
		} else {
			drawXButton(style, outerWidth, height, ig, sg, false);
			ig.setColor(new Color(0x40000000, true));
			ig.drawLine(borderWidth - 1, 1, borderWidth - 1, innerHeight);
			ig.drawLine(outerWidth - borderWidth - 1, 1, outerWidth - borderWidth - 1, innerHeight);
			ig.setColor(new Color(0x40ffffff, true));
			ig.drawLine(borderWidth, 1, borderWidth, innerHeight);
			ig.drawLine(outerWidth - borderWidth, 1, outerWidth - borderWidth, innerHeight);
			ig.translate(outerWidth + 2, 0);
			sg.translate(outerWidth + 2, 0);
		}
	}
	
	void configureGraphics(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
	}
	
	void drawBackground(Graphics2D g) {
		g.setPaint(new GradientPaint(0, 0, new Color(0xcccccc), 0, 50, new Color(0x999999)));
		g.fillRect(0, 0, 600, 50);
	}
	
	
	
	void drawIcon(int num, Graphics2D g) {
		g.setComposite(AlphaComposite.DstAtop);
		switch(num) {
		case 0: // test
			g.setColor(new Color(0xff000000, true));
			g.drawRect(0, 0, 15, 15);
			break;
		case 1:	// vertex mode
			g.setColor(new Color(0x40000000, true));
			g.drawLine(1, 3, 7, 1);
			g.drawLine(7, 1, 13, 3);
			g.drawLine(13, 3, 7, 5);
			g.drawLine(7, 5, 1, 3);
			g.drawLine(13, 11, 7, 13);
			g.drawLine(7, 13, 1, 11);
			g.drawLine(1, 3, 1, 11);
			g.drawLine(7, 5, 7, 13);
			g.drawLine(13, 3, 13, 11);	
			g.setColor(new Color(0xc0000000, true));
			g.fill(new Ellipse2D.Float(0, 2, 3, 3));
			g.fill(new Ellipse2D.Float(6, 4, 3, 3));
			g.fill(new Ellipse2D.Float(0, 10, 3, 3));
			g.fill(new Ellipse2D.Float(6, 12, 3, 3));
			break;
		case 2:	// edge mode
			g.setColor(new Color(0x40000000, true));
			g.drawLine(1, 3, 7, 1);
			g.drawLine(7, 1, 13, 3);
			g.drawLine(13, 3, 7, 5);
			g.drawLine(13, 11, 7, 13);
			g.drawLine(13, 3, 13, 11);	
			g.setColor(new Color(0xc0000000, true));
			g.drawLine(7, 5, 1, 3);
			g.drawLine(7, 13, 1, 11);
			g.drawLine(1, 3, 1, 11);
			g.drawLine(7, 5, 7, 13);
			break;
		case 3:	// face mode
//			g.setComposite(AlphaComposite.Src);
			g.setColor(new Color(0x40000000, true));
			g.drawLine(1, 3, 7, 1);
			g.drawLine(7, 1, 13, 3);http://www.87er.com/herz.htm#
			g.drawLine(13, 3, 7, 5);
			g.drawLine(7, 5, 1, 3);
			g.drawLine(13, 11, 7, 13);
			g.drawLine(7, 13, 1, 11);
			g.drawLine(1, 3, 1, 11);
			g.drawLine(7, 5, 7, 13);
			g.drawLine(13, 3, 13, 11);	
//			g.setComposite(AlphaComposite.Xor);
			g.setColor(new Color(0x40000000, true));
			GeneralPath p = new GeneralPath();
			p.moveTo(1, 3);
			p.lineTo(8, 5);
			p.lineTo(8, 14);
			p.lineTo(1, 12);
			p.closePath();
			g.fill(p);
			g.setColor(new Color(0xc0000000, true));
			g.drawLine(7, 5, 1, 3);
			g.drawLine(7, 13, 1, 11);
			g.drawLine(1, 3, 1, 11);
			g.drawLine(7, 5, 7, 13);
			break;
		case 4:	// object mode
//			g.setComposite(AlphaComposite.Src);
			g.setColor(new Color(0xc0000000, true));
			g.drawLine(1, 3, 7, 1);
			g.drawLine(7, 1, 13, 3);
			g.drawLine(13, 3, 7, 5);
			g.drawLine(7, 5, 1, 3);
			g.drawLine(13, 11, 7, 13);
			g.drawLine(7, 13, 1, 11);
			g.drawLine(1, 3, 1, 11);
			g.drawLine(7, 5, 7, 13);
			g.drawLine(13, 3, 13, 11);
			g.setComposite(AlphaComposite.Xor);
			g.setColor(new Color(0x40000000, true));
			p = new GeneralPath();
			p.moveTo(1, 3);
			p.lineTo(7, 1);
			p.lineTo(14, 3);
			p.lineTo(14, 11);
			p.lineTo(7, 14);
			p.lineTo(1, 12);
			p.closePath();
			g.fill(p);
			break;
		case 5: // move view
			g.setColor(new Color(0xc0000000, true));
			g.drawLine(7, 4, 7, 1);
			g.drawLine(10, 7, 13, 7);
			g.drawLine(7, 10, 7, 13);
			g.drawLine(4, 7, 1, 7);
			g.draw(new Polygon(new int[] { 7, 9, 5 }, new int[] { 0, 2, 2 }, 3));
			g.draw(new Polygon(new int[] { 0, 2, 2 }, new int[] { 7, 9, 5 }, 3));
			g.draw(new Polygon(new int[] { 7, 9, 5 }, new int[] { 14, 12, 12 }, 3));
			g.draw(new Polygon(new int[] { 14, 12, 12 }, new int[] { 7, 9, 5 }, 3));
			break;
		case 6: // zoom view
			g.setColor(new Color(0xc0000000, true));
			g.draw(new Ellipse2D.Float(0, 0, 10, 10));
			g.setStroke(new BasicStroke(1.5f));
			g.drawLine(9, 9, 13, 13);
			g.setStroke(new BasicStroke(2.5f));
			g.drawLine(11, 11, 13, 13);
			break;
		case 7: // rotate view
			g.setColor(new Color(0x20000000, true));
			g.drawArc(2, 2, 12, 11, 45, 300);
			g.setColor(new Color(0x40000000, true));
			g.drawArc(2, 2, 11, 11, 45, 270);
			g.setColor(new Color(0x60000000, true));
			g.drawArc(2, 2, 11, 11, 45, 240);
			g.setColor(new Color(0x80000000, true));
			g.drawArc(2, 2, 11, 11, 45, 210);
			g.setColor(new Color(0xa0000000, true));
			g.drawArc(2, 2, 11, 11, 45, 180);
			g.setColor(new Color(0xc0000000, true));
			g.drawArc(2, 2, 11, 11, 45, 150);
			g.fill(new Polygon(new int[] { 14, 9, 14 }, new int[] { 6, 6, 1 }, 3));
			break;
		case 8: // undo
			g.setPaint(new GradientPaint(0, 0, new Color(0xffffffff, true), 0, 16, new Color(0x80ffc0c0, true)));
			Area a = new Area(new Ellipse2D.Float(0, 0, 11.5f, 16));
			a.subtract(new Area(new Ellipse2D.Float(-1, 3, 9.5f, 14)));
			a.subtract(new Area(new Rectangle2D.Float(0, 0, 6, 16)));
			g.fill(a);
			g.fill(new Polygon(new int[] { 4, 12, 4 }, new int[] { 0, 0, 7 }, 3));
			break;
		case 9: // redo
			g.setTransform(new AffineTransform(-1, 0, 0, 1, 16, 0));
			g.setPaint(new GradientPaint(0, 0, new Color(0xffffffff, true), 0, 16, new Color(0x80c0c0ff, true)));
			a = new Area(new Ellipse2D.Float(0, 0, 11.5f, 16));
			a.subtract(new Area(new Ellipse2D.Float(-1, 3, 9.5f, 14)));
			a.subtract(new Area(new Rectangle2D.Float(0, 0, 6, 16)));
			g.fill(a);
			g.fill(new Polygon(new int[] { 4, 12, 4 }, new int[] { 0, 0, 7 }, 3));
			break;
		case 10: // default tool
			g.setComposite(AlphaComposite.Src);
			Polygon pl = new Polygon(new int[] { 4, 13, 10, 11, 9, 7, 4 }, new int[] { 0, 8, 9, 15, 15, 10, 12 }, 7);
			g.setColor(Color.WHITE);
			g.fill(pl);
			g.setStroke(new BasicStroke(0.5f));
			g.setColor(Color.BLACK);
			g.draw(pl);
			break;
		case 11: // move tool
			g.setColor(new Color(0xbb3333));
			g.drawLine(4, 11, 13, 11);
			g.drawPolygon(new Polygon(new int[] { 15, 12, 12 }, new int[] { 11, 13, 9 }, 3));
			g.fillPolygon(new Polygon(new int[] { 15, 12, 12 }, new int[] { 11, 13, 9 }, 3));
			g.setColor(new Color(0x339933));
			g.drawLine(4, 2, 4, 11);
			g.drawPolygon(new Polygon(new int[] { 4, 6, 2 }, new int[] { 0, 3, 3 }, 3));
			g.fillPolygon(new Polygon(new int[] { 4, 6, 2 }, new int[] { 0, 3, 3 }, 3));
			g.setColor(new Color(0x5555bb));
			g.drawLine(4, 11, 1, 14);
			g.drawPolygon(new Polygon(new int[] { 0, 3, 0 }, new int[] { 12, 15, 15 }, 3));
			g.fillPolygon(new Polygon(new int[] { 0, 3, 0 }, new int[] { 12, 15, 15 }, 3));
			break;
		case 12: // scale tool
			g.setColor(new Color(0xbb3333));
			g.drawLine(4, 11, 13, 11);
			g.fillRect(13, 10, 3, 3);
			g.setColor(new Color(0x339933));
			g.drawLine(4, 2, 4, 11);
			g.fillRect(3, 0, 3, 3);
			g.setColor(new Color(0x5555bb));
			g.drawLine(4, 11, 1, 14);
			g.fillRect(0, 13, 3, 3);
			break;
		case 13: // rotate tool
			g.setColor(new Color(0xbb3333));
			g.drawOval(6, 1, 4, 14);
			g.setColor(new Color(0x339933));
			g.drawOval(1, 6, 14, 4);
			g.setColor(new Color(0x5555bb));
			g.drawOval(1, 1, 14, 14);
			break;
		case 14: // extrude tool
			g.setColor(new Color(0x60000000, true));
			g.drawLine(5, 1, 13, 1);
			g.drawLine(2, 4, 10, 4);
			g.drawLine(5, 11, 13, 11);
			g.drawLine(2, 14, 10, 14);
			g.drawLine(13, 1, 12, 0);
			g.drawLine(13, 1, 12, 2);
			g.drawLine(10, 4, 9, 3);
			g.drawLine(10, 4, 9, 5);
			g.drawLine(10, 14, 9, 13);
			g.drawLine(10, 14, 9, 15);
			g.drawPolygon(new Polygon(new int[] { 4, 4, 1, 1}, new int[] { 1, 11, 14, 4 }, 4));
			g.setColor(new Color(0xa0000000, true));
			g.drawPolygon(new Polygon(new int[] { 14, 14, 11, 11}, new int[] { 1, 11, 14, 4 }, 4));
			g.setColor(new Color(0x40000000, true));
			g.fillPolygon(new Polygon(new int[] { 14, 14, 12, 12}, new int[] { 2, 11, 14, 5 }, 4));
			break;
		case 15: // lathe tool
			p = new GeneralPath();
			p.moveTo(2, 0);
			p.curveTo(0, 4, 3, 7, 5, 7);
			p.curveTo(7, 9, 7, 13, 4, 15);
			p.lineTo(13, 15);
			p.curveTo(10, 14, 10, 10, 12, 8);
			p.curveTo(13, 8, 16, 5, 14.2f, 0);
			p.closePath();
			g.setPaint(new LinearGradientPaint(0, 0, 15, 0, new float[] { 0.0f, 0.5f, 1.0f }, new Color[] { new Color(0x30000000, true), new Color(0x00000000, true), new Color(0x60000000, true) }));
			g.fill(p);
			g.setColor(new Color(0xc0000000, true));
			p = new GeneralPath();
			p.moveTo(3, 0);
			p.curveTo(1, 4, 3, 7, 5, 7);
			p.curveTo(7, 9, 7, 13, 4, 14);
			g.draw(p);
			g.setColor(new Color(0x80000000, true));
			p = new GeneralPath();
			p.moveTo(16 - 3, 0);
			p.curveTo(16 - 1, 4, 16 - 3, 7, 16 - 5, 7);
			p.curveTo(16 - 7, 9, 16 - 7, 13, 16 - 4, 14);
			g.draw(p);
			g.setColor(new Color(0x60000000, true));
			g.drawLine(8, 0, 8, 2);
			g.drawLine(8, 4, 8, 4);
			g.drawLine(8, 6, 8, 8);
			g.drawLine(8, 10, 8, 10);
			g.drawLine(8, 12, 8, 14);
			break;
		case 16: // polygon tool
			break;
		case 17: // snap to grid
			g.setColor(new Color(0x30000000, true));
			g.drawRect(0, 0, 15, 15);
			
			g.setColor(new Color(0x50000000, true));
//			g.drawLine(0, 0, 15, 0);
			g.drawLine(0, 5, 15, 5);
			g.drawLine(0, 10, 15, 10);
//			g.drawLine(0, 15, 15, 15);
//			g.drawLine(0, 0, 0, 15);
			g.drawLine(5, 0, 5, 15);
			g.drawLine(10, 0, 10, 15);
//			g.drawLine(15, 0, 15, 15);
			g.setColor(new Color(0x70000000, true));
			g.drawRect(5, 5, 5, 5);
			break;
		case 18: // hide
			break;
		}
	}
}
