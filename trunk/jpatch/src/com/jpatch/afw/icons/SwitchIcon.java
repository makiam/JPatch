package com.jpatch.afw.icons;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

import org.apache.batik.ext.awt.*;


public class SwitchIcon {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SwitchIcon();
	}
	
	SwitchIcon() {
		try {
			ImageIO.write(makeIcon(false), "png", new File("src/com/jpatch/afw/icons/SWITCH_LEFT.png"));
			ImageIO.write(makeIcon(true), "png", new File("src/com/jpatch/afw/icons/SWITCH_RIGHT.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		
//		
//		
//		
//		System.setProperty("swing.boldMetal", "false");
//		System.setProperty("swing.aatext", "true");
//		JFrame frame = new JFrame();
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setSize(640, 480);
//		Box box = Box.createVerticalBox();
//		
//		box.add(new Switcher("off", "on"));
//		box.add(new Switcher("false", "true"));
//		box.add(new Switcher("stop", "start"));
//		box.add(new Switcher("no", "yes"));
//		box.add(new Switcher("reverse", "forward"));
////		frame.setLayout(null);
//		frame.add(box);
//		frame.pack();
////		box.setBounds(0, 0, 100, 60);
//		frame.setVisible(true);
	}
	
	static BufferedImage makeIcon(boolean selected) {
		int h = 14;
		int w = 32;
		BufferedImage image = new BufferedImage(w + 1, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(new GradientPaint(0, 2, new Color(0x222222), 0, 10, new Color(0xffffff)));
		g2.fill(new RoundRectangle2D.Double(0, 0, w, h, h, h));
		
		g2.setPaint(new Color(0x000000));
		g2.fill(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, h - 2, h - 2));
		g2.setPaint(new Color(0x333333));
		g2.fill(new RoundRectangle2D.Double(2, 2, w - 3, h - 3, h - 3, h - 3));
		g2.setPaint(new Color(0x666666));
		g2.fill(new RoundRectangle2D.Double(3, 3, w - 4, h - 4, h - 4, h - 4));
		g2.setPaint(new Color(0x888888));
		g2.fill(new RoundRectangle2D.Double(4, 4, w - 6, h - 6, h - 6, h - 6));
		g2.setPaint(new Color(0x999999));
		g2.fill(new RoundRectangle2D.Double(5, 5, w - 8, h - 8, h - 8, h - 8));
		
		if (selected) {
			g2.setPaint(new GradientPaint(0, 0, new Color(0x99444444, true), w, 4, new Color(0x99cccccc, true)));
		} else {
			g2.setPaint(new GradientPaint(w, 0, new Color(0x99555555, true), 0, 4, new Color(0x99999999, true)));
		}
		g2.fill(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, h - 2, h - 2));
		int x = selected ? w - h + 1 : 1;
		int y = 1;
		g2.setColor(new Color(0x33000000, true));
		g2.fillOval(x + 1, y + 0, h - 1, h - 1);
		g2.setColor(new Color(0x88000000, true));
		g2.fillOval(x + 2, y + 1, h - 3, h - 3);
		g2.setPaint(new GradientPaint(0, 2, new Color(0xffffff), 0, h - 2, new Color(0x222222)));
		g2.fillOval(x, y, h - 2, h - 2);
		g2.setColor(new Color(0x888888));
		g2.fillOval(x + 1, y + 1, h - 4, h - 4);
		g2.setPaint(new LinearGradientPaint(x + 2, y + 2, x + h - 5, y + h - 5, new float[] { 0, 0.5f, 1 }, new Color[] { new Color(0x66ffffff, true), new Color(0xffffffff, true), new Color(0x66ffffff, true) }));
		g2.fillOval(x + 1, y + 1, h - 4, h - 4);
		g2.setPaint(new LinearGradientPaint(x + h - 7, y + 2, x + 2, y + h - 7, new float[] { 0, 0.5f, 1 }, new Color[] { new Color(0x00000000, true), new Color(0x33000000, true), new Color(0x00000000, true) }));
		g2.fillOval(x + 1, y + 1, h - 4, h - 4);
		return image;
	}

}
