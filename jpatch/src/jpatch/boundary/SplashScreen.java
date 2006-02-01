/*
 * $Id: SplashScreen.java,v 1.6 2006/02/01 21:11:28 sascha_l Exp $
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

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.font.*;
import javax.swing.*;

import jpatch.VersionInfo;

/**
 * @author sascha
 *
 */
public class SplashScreen {
	public static SplashScreen instance;
	JWindow window = new JWindow();
	JPanel logo = new JPanel() {
		private static final long serialVersionUID = 1L;
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			FontRenderContext frc = g2.getFontRenderContext();
			Font font;
			GlyphVector gv;
			Shape shape;
//			font = new Font("sans-serif", Font.BOLD, 60);
			font = new Font("sans-serif", Font.BOLD, 45);
			gv = font.createGlyphVector(frc, VersionInfo.name);
			shape = gv.getOutline();
			Rectangle bounds = shape.getBounds();
			AffineTransform at = new AffineTransform(1, 0, 0, 1, (getWidth() - bounds.width) / 2, bounds.height + 20);
			shape = at.createTransformedShape(gv.getOutline());
//			shape = gv.getOutline();
//			g2.setPaint(new GradientPaint(0, 0, new Color(0.0f, 0.0f, 0.0f, 1.0f), 0, -40, new Color(0.0f, 0.0f, 0.0f, 0.0f)));
//			g2.fill(shape)
			
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(2));
			g2.draw(shape);
			
			g2.setClip(0, 0, getWidth(), 50);
			g2.setPaint(new GradientPaint(0, 10, Color.BLUE, 0, 50, Color.WHITE));
			g2.fill(shape);
			g2.setClip(0, 50, getWidth(), 20);
			g2.setPaint(new GradientPaint(0, 50, Color.BLACK, 0, 70, Color.WHITE));
			g2.fill(shape);
			
			g2.setClip(0, 0, getWidth(), getHeight());
			g2.setColor(Color.WHITE);
			g2.setStroke(new BasicStroke(1));
			g2.draw(shape);
		}
		public Dimension getPreferredSize() {
			return new Dimension(400, 80);
		}
	};
	
	public void showSplash(boolean progressBar) {
		if (instance != null)
			return;
		if (!progressBar)
			MainFrame.getInstance().setEnabled(false);
		JPanel panel = new JPanel();
		JPanel foreground = new JPanel();
		foreground.setOpaque(false);
		JPanel background = new JPanel() {
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setPaint(new GradientPaint(0, 0, Color.WHITE, getWidth() / 2, getHeight() * 3, Color.GRAY));
				g2.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		panel.setLayout(new OverlayLayout(panel));
		
		panel.add(foreground);
		panel.add(background);
		try {
			window.setAlwaysOnTop(true);
		} catch (NoSuchMethodError e) {
			window.toFront();
		}
		foreground.setLayout(new BorderLayout());
		JProgressBar progress = new JProgressBar();
		progress.setIndeterminate(true);
		progress.setName("Starting JPatch...");
		progress.setBorderPainted(false);
		progress.setBackground(Color.GRAY);
		foreground.add(logo, BorderLayout.NORTH);
		JLabel label = new JLabel("<html><center><font size='+1' color='red'><b>DEVELOPMENT VERSION</b></font><br><br>written by Sascha Ledinsky<br>Copyright &copy; 2002-2005<br><br>" +
				"<font size = '-2'>This program is free software.<br>You can redistribute it and/or modify" +
				"it under the terms of the<br>GNU General Public License as published by" +
				"the Free Software Foundation.<br><br></font>" +
				"<font color='gray'><b>http://www.jpatch.com</b></font></center></html>"
		);
		label.setFont(new Font("Sans Serif", Font.PLAIN, 14));
		JPanel testPanel = new JPanel();
		testPanel.add(label);
		testPanel.setOpaque(false);
		foreground.add(testPanel, BorderLayout.CENTER);
		if (progressBar)
			foreground.add(progress, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createRaisedBevelBorder());
		window.getContentPane().add(panel);
		window.setSize(400,300);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		instance = this;
		window.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				clearSplash();
			}
		});
	}
	
	public void clearSplash() {
		window.setVisible(false);
		window.dispose();
		MainFrame.getInstance().setEnabled(true);
		instance = null;
	}
}
