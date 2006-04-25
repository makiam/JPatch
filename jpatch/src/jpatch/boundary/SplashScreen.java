/*
 * $Id: SplashScreen.java,v 1.9 2006/04/25 20:24:26 sascha_l Exp $
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.boundary;

import com.sun.org.apache.bcel.internal.util.ClassLoader;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.font.*;
import javax.imageio.*;
import javax.swing.*;

import jpatch.VersionInfo;

/**
 * @author sascha
 *
 */
public class SplashScreen {
	public static SplashScreen instance;
	private JWindow window;
	private String text;
//	JPanel logo = new JPanel() {
//		private static final long serialVersionUID = 1L;
//		public void paintComponent(Graphics g) {
//			Graphics2D g2 = (Graphics2D) g;
//			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//			FontRenderContext frc = g2.getFontRenderContext();
//			Font font;
//			GlyphVector gv;
//			Shape shape;
////			font = new Font("sans-serif", Font.BOLD, 60);
//			font = new Font("sans-serif", Font.BOLD, 45);
//			gv = font.createGlyphVector(frc, VersionInfo.name);
//			shape = gv.getOutline();
//			Rectangle bounds = shape.getBounds();
//			AffineTransform at = new AffineTransform(1, 0, 0, 1, (getWidth() - bounds.width) / 2, bounds.height + 20);
//			shape = at.createTransformedShape(gv.getOutline());
////			shape = gv.getOutline();
////			g2.setPaint(new GradientPaint(0, 0, new Color(0.0f, 0.0f, 0.0f, 1.0f), 0, -40, new Color(0.0f, 0.0f, 0.0f, 0.0f)));
////			g2.fill(shape)
//			
//			g2.setColor(Color.BLACK);
//			g2.setStroke(new BasicStroke(2));
//			g2.draw(shape);
//			
//			g2.setClip(0, 0, getWidth(), 50);
//			g2.setPaint(new GradientPaint(0, 10, Color.BLUE, 0, 50, Color.WHITE));
//			g2.fill(shape);
//			g2.setClip(0, 50, getWidth(), 20);
//			g2.setPaint(new GradientPaint(0, 50, Color.BLACK, 0, 70, Color.WHITE));
//			g2.fill(shape);
//			
//			g2.setClip(0, 0, getWidth(), getHeight());
//			g2.setColor(Color.WHITE);
//			g2.setStroke(new BasicStroke(1));
//			g2.draw(shape);
//		}
//		public Dimension getPreferredSize() {
//			return new Dimension(400, 80);
//		}
//	};
	
	public void showSplash(boolean progressBar) {
//		if (instance != null)
//			return;
//		if (!progressBar)
//			MainFrame.getInstance().setEnabled(false);
//		JPanel panel = new JPanel();
//		JPanel foreground = new JPanel();
//		foreground.setOpaque(false);
//		JPanel background = new JPanel() {
//			private static final long serialVersionUID = 1L;
//			public void paintComponent(Graphics g) {
//				Graphics2D g2 = (Graphics2D) g;
//				g2.setPaint(new GradientPaint(0, 0, Color.WHITE, getWidth() / 2, getHeight() * 3, Color.GRAY));
//				g2.fillRect(0, 0, getWidth(), getHeight());
//			}
//		};
//		panel.setLayout(new OverlayLayout(panel));
//		
//		panel.add(foreground);
//		panel.add(background);
//		try {
//			window.setAlwaysOnTop(true);
//		} catch (NoSuchMethodError e) {
//			window.toFront();
//		}
//		foreground.setLayout(new BorderLayout());
//		JProgressBar progress = new JProgressBar();
//		progress.setIndeterminate(true);
//		progress.setName("Starting JPatch...");
//		progress.setBorderPainted(false);
//		progress.setBackground(Color.GRAY);
//		foreground.add(logo, BorderLayout.NORTH);
//		JLabel label = new JLabel("<html><center><font size='+1' color='red'><b>DEVELOPMENT VERSION</b></font><br><br>written by Sascha Ledinsky<br>Copyright &copy; 2002-2006<br><br>" +
//				"<font size = '-2'>This program is free software.<br>You can redistribute it and/or modify" +
//				"it under the terms of the<br>GNU General Public License as published by" +
//				"the Free Software Foundation.<br><br></font>" +
//				"<font color='gray'><b>http://www.jpatch.com</b></font></center></html>"
//		);
//		label.setFont(new Font("Sans Serif", Font.PLAIN, 14));
//		JPanel testPanel = new JPanel();
//		testPanel.add(label);
//		testPanel.setOpaque(false);
//		foreground.add(testPanel, BorderLayout.CENTER);
//		if (progressBar)
//			foreground.add(progress, BorderLayout.SOUTH);
//		panel.setBorder(BorderFactory.createRaisedBevelBorder());
//		window.getContentPane().add(panel);
//		window.setSize(400,300);
//		window.setLocationRelativeTo(null);
//		window.setVisible(true);
//		instance = this;
//		window.addMouseListener(new MouseAdapter() {
//			public void mousePressed(MouseEvent e) {
//				clearSplash();
//			}
//		});
		System.out.println("Showing splash screen...");
		if (instance != null)
			return;
		if (!progressBar) {
			MainFrame.getInstance().setVisible(false);
		}
		try {
			final Image image = ImageIO.read(ClassLoader.getSystemResource("jpatch/images/title.png"));
			window = new JWindow() {
				@Override
				public void paint(Graphics g) {
					g.drawImage(image, 0, 0, null);
					if (text != null) {
						((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						int i = g.getFontMetrics().charsWidth(text.toCharArray(), 0, text.length()) >> 1;
						int h = g.getFontMetrics().getHeight();
						int y = 30;
						g.setColor(new Color(0x66444400, true));
						g.fillRect(200 - i - 8, y - h, 2 * i + 16, h + 6);
						g.setColor(Color.YELLOW);
						g.drawRect(200 - i - 8, y - h, 2 * i + 16, h + 6);
						g.drawString(text, 200 - i, y);
					}
				}
			};
			window.setSize(400, 300);
			window.setLocationRelativeTo(null);
			window.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					clearSplash();
				}
			});
			window.requestFocus();
//			System.out.println(MainFrame.getInstance().isEnabled());
			window.setVisible(true);
			instance = this;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setText(String text) {
		this.text = text;
		if (window != null)
			window.repaint();
//		try {
//			Thread.sleep(100);
//		} catch (Exception e) { }
	}
	
	public void clearSplash() {
		if (window == null)
			return;
		window.setVisible(false);
		window.dispose();
		window = null;
		if (MainFrame.getInstance() != null) {
			MainFrame.getInstance().setVisible(true);
		}
		instance = null;
	}
}
