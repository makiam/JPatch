package com.jpatch.icons;

import com.jpatch.afw.icons.PackedIcon;
import com.jpatch.afw.ui.ImageUtils;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;

import org.apache.batik.ext.awt.LinearGradientPaint;

public class ImageGenerator {
	static String[] iconNames = {
		"VERTEX_MODE",
		"EDGE_MODE",
		"FACE_MODE",
		"OBJECT_MODE",
		"MOVE_VIEW",
		"ZOOM_VIEW",
		"ROTATE_VIEW",
		"UNDO",
		"REDO",
		"TWEAK_TOOL",
		"MOVE_TOOL",
		"SCALE_TOOL",
		"ROTATE_TOOL",
		"EXTRUDE_TOOL",
		"LATHE_TOOL",
		"SNAP_TO_GRID",
		"SELECT_TOOL",
		"INSET_TOOL",
		"ADD_EDGE_TOOL",
		"FLIP_TOOL",
	};
	
	public static void main(String[] args) throws Exception {
		ImageGenerator ig = new ImageGenerator();
		ig.generateSwitcher();
		for (int i = 0; i < iconNames.length; i++) {
			ig.generateIcon(i);
		}
	}
	void generateIcon(int num) throws IOException {
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		configureGraphics(g);
		drawIcon(num + 1, g);
		ImageIO.write(image, "png", new File("src/com/jpatch/icons/" + iconNames[num] + ".png"));
	}
	
	void generateSwitcher() throws FileNotFoundException, IOException {
		int switcherWidth = 56, switcherHeight = 44;
		BufferedImage switcherImage = new BufferedImage(switcherWidth, switcherHeight, BufferedImage.TYPE_INT_ARGB);
		BufferedImage switcherStencil = new BufferedImage(switcherWidth, switcherHeight, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D sig = switcherImage.createGraphics();
		Graphics2D ssg = switcherStencil.createGraphics();
		configureGraphics(sig);
		configureGraphics(ssg);
		sig.translate(1, 1);
		ssg.translate(1, 1);
		drawSwitcher(switcherWidth - 2, switcherHeight - 2, sig, ssg);
		PackedIcon[] switcherIcons = new PackedIcon[4];
		for (int i = 0; i < 4; i++) {
			BufferedImage img = new BufferedImage(switcherWidth / 2, switcherHeight / 2, BufferedImage.TYPE_INT_ARGB);
			BufferedImage stc = new BufferedImage(switcherWidth / 2, switcherHeight / 2, BufferedImage.TYPE_BYTE_GRAY);
			Graphics2D xig = img.createGraphics();
			Graphics2D xsg = stc.createGraphics();
			switch(i) {
			case 1:
				xig.translate(-switcherWidth / 2, 0);
				xsg.translate(-switcherWidth / 2, 0);
				break;
			case 2:
				xig.translate(0, -switcherHeight / 2);
				xsg.translate(0, -switcherHeight / 2);
				break;
			case 3:
				xig.translate(-switcherWidth / 2, -switcherHeight / 2);
				xsg.translate(-switcherWidth / 2, -switcherHeight / 2);
				break;
			}
			xig.drawImage(switcherImage, 0, 0, null);
			xsg.drawImage(switcherStencil, 0, 0, null);
			switcherIcons[i] = new PackedIcon(img, stc, 0, 0);
		}
		
		File file = new File("src/com/jpatch/icons/switcher.iconset");
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(switcherIcons);
		oos.close();
	}
	
	void drawSwitcher(int width, int height, Graphics2D ig, Graphics2D sg) {
		int outerWidth = width;
		int innerWidth = outerWidth - 2;
		int innerHeight = height - 2;
		RoundRectangle2D outerRect, innerRect, ooRect;
		outerRect = new RoundRectangle2D.Float(0, 0, outerWidth, height, height / 2.3f, height / 2.3f);
		innerRect = new RoundRectangle2D.Float(1, 1, innerWidth, innerHeight, innerHeight / 2.5f, innerHeight / 2.5f);
		ooRect = new RoundRectangle2D.Float(-1, -1, outerWidth + 2, height + 2, (height + 2) / 2.1f, (height + 2) / 2.1f);
		
		sg.setColor(Color.WHITE);
		sg.fill(innerRect);
		
		ig.setPaint(new GradientPaint(0, 0, new Color(0x20000000, true), 0, height, new Color(0x80ffffff, true)));
		ig.fill(ooRect);
		ig.setColor(new Color(0x80000000, true));
		ig.fill(outerRect);
		
		float halfHeight = innerHeight / 4.0f;
		Area area1 = new Area();
		area1.add(new Area(new Arc2D.Float(1, 1 + halfHeight, halfHeight * 2, halfHeight * 2, 90, 90, Arc2D.PIE)));
		area1.add(new Area(new Rectangle2D.Float(1 + halfHeight, 1 + halfHeight, innerWidth / 2.0f, halfHeight)));
		Area area2 = new Area();
		area2.add(new Area(new Rectangle2D.Float(1 + innerWidth / 2.0f, 1, innerWidth / 2.0f, innerHeight)));
		area2.subtract(new Area(new Ellipse2D.Float(1 + innerWidth - halfHeight * 2, 1 - halfHeight, halfHeight * 2, halfHeight * 2)));
		area2.subtract(new Area(new Rectangle2D.Float(1 + innerWidth / 2.0f, 1, innerWidth / 2.0f - halfHeight, halfHeight)));
		area1.add(area2);
		Area area3 = new Area(area1);
		area3.transform(new AffineTransform(1, 0, 0, 1, 0, halfHeight * 2));
		area1.intersect(new Area(innerRect));
		area1.intersect(new Area(new Rectangle2D.Float(0, 0, width, height / 2.0f)));
		area3.intersect(new Area(innerRect));
		
//		g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.25f, 0.5f, 0.5f, 0.75f, 1.0f }, new Color[] { new Color(1.00f, 1.00f, 1.00f), new Color(0.80f, 0.80f, 0.80f), new Color(0.85f, 0.85f, 0.85f), new Color(1.00f, 1.00f, 1.00f), new Color(0.80f, 0.80f, 0.80f), new Color(0.85f, 0.85f, 0.85f) } ));
//		g.fill(innerRect);
		
		ig.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.5f, 0.5f, 1.0f }, new Color[] { new Color(0xe4e4e4), new Color(0xffffff), new Color(0xe4e4e4), new Color(0xffffff) } ));
		ig.fill(innerRect);
		ig.setPaint(new GradientPaint(0, 1, new Color(0xb8b8b8), 0, 1 + halfHeight * 2, new Color(0xffffff)));
		ig.fill(area1);
		ig.setPaint(new GradientPaint(0, halfHeight * 2 + 1, new Color(0xb8b8b8), 0, halfHeight * 2 + 1 + halfHeight * 2, new Color(0xffffff)));
		ig.fill(area3);
		
		ig.setColor(new Color(0x40000000, true));
		ig.drawLine(width / 2 - 1, 1, width / 2 - 1, innerHeight);
		ig.drawLine(1, height / 2 - 1, width - 2, height / 2 - 1);
		ig.setColor(new Color(0x40ffffff, true));
		ig.drawLine(width / 2, 1, width / 2, innerHeight);
		ig.drawLine(1, height / 2, width - 2, height / 2);
		
		Font font = new Font("monospaced", Font.BOLD, 20);
		ig.drawImage(ImageUtils.createTextIcon(font, new Color(0x30000000, true), "1"), width / 4 - 6, height / 4 - 10, null);
		ig.drawImage(ImageUtils.createTextIcon(font, new Color(0x30000000, true), "2"), width * 3 / 4 - 8, height / 4 - 10, null);
		ig.drawImage(ImageUtils.createTextIcon(font, new Color(0x30000000, true), "3"), width / 4 - 5, height * 3 / 4 - 11, null);
		ig.drawImage(ImageUtils.createTextIcon(font, new Color(0x30000000, true), "4"), width * 3 / 4 - 9, height * 3 / 4 - 11, null);
	}
	
	void configureGraphics(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
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
		case 16: // snap to grid
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
		case 17: // select
			g.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1, new float[] { 13f / 4, 13f / 4 }, 13f / 8));
			g.setColor(new Color(0x80000000, true));
			g.drawRect(1, 1, 13, 13);
			break;
		case 18: // inset
			
			g.setColor(new Color(0x30000000, true));
			g.fillRect(4, 4, 8, 8);
			
			g.setColor(new Color(0x60000000, true));
			
			g.drawLine(1, 1, 3, 3);
			g.drawLine(14, 14, 12, 12);
			g.drawLine(1, 14, 3, 12);
			g.drawLine(14, 1, 12, 3);
			g.drawLine(2, 4, 4, 4);
			g.drawLine(4, 4, 4, 2);
			g.drawLine(11, 4, 13, 4);
			g.drawLine(11, 4, 11, 2);
			g.drawLine(2, 11, 4, 11);
			g.drawLine(4, 11, 4, 13);
			g.drawLine(11, 11, 13, 11);
			g.drawLine(11, 11, 11, 13);
			
			g.setColor(new Color(0x90000000, true));
			g.drawRect(0, 0, 15, 15);
			g.drawRect(4, 4, 7, 7);
			break;
		case 19: // add edge
			g.setColor(new Color(0x60000000, true));
			g.drawLine(1, 1, 3, 7);
			g.drawLine(3, 8, 1, 14);
			g.drawLine(14, 1, 12, 7);
			g.drawLine(12, 8, 14, 14);
//			g.setStroke(new BasicStroke(1.5f));
			g.setColor(new Color(0xc0000000, true));
			g.drawLine(3, 7, 12, 7);
			g.drawLine(3, 8, 12, 8);
			break;
		case 20: // flip
			g.setComposite(AlphaComposite.SrcOver);
			g.setColor(new Color(0x40000000, true));
			g.fillRect(3, 3, 11, 11);
			g.setColor(new Color(0x80000000, true));
			g.drawRect(2, 2, 12, 12);
			Polygon arrow = new Polygon(
				new int[] { 3, 6, 6, 11, 11, 14, 14, 11, 11, 6, 6, 3 },
				new int[] { 8, 5, 7, 7,  5,  8,  9, 12,  10, 10, 12, 9},
				12
			);
			g.fill(arrow);
			break;
		}
	}
}
