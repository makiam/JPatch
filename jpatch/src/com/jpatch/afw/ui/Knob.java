package com.jpatch.afw.ui;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

import javax.swing.*;

public final class Knob {
	private enum State { INACTIVE, IDLE, HOVER, ACTIVE };
	private static Robot robot;
	
	final BufferedImage idleImage;
	final BufferedImage activeImage;
	final BufferedImage hoverImage;
	final JComponent component;
	boolean hasMinimum;
	boolean hasMaximum;
	boolean hover;
	boolean active;
	double alpha;
	
	int rotations;
	
	State state = State.IDLE;
	
	public Knob(final int size) {
		idleImage = drawKnob(size, State.IDLE);
		activeImage = drawKnob(size, State.ACTIVE);
		hoverImage = drawKnob(size, State.HOVER);
		
		component = new JComponent() {

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				switch (state) {
				case IDLE:
					g.drawImage(idleImage, 0, 0, null);
					break;
				case ACTIVE:
					g.drawImage(activeImage, 0, 0, null);
					break;
				case HOVER:
					g.drawImage(hoverImage, 0, 0, null);
					break;
				}
				
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				
				float knobSize = size / 4f;
				double a = (alpha % 360) / 180 * Math.PI;
				double x = Math.sin(a);
				double y = -Math.cos(a);
				double s = size * 0.5;
				double r = s - knobSize * 0.5 - 2;
				g2.translate(s + r * x, s + r * y);
				
				if (!active) {
					g2.setPaint(new GradientPaint(0, -knobSize * 0.5f, new Color(0x222222), 0, knobSize * 0.5f, new Color(0x888888)));
				} else {
					g2.setPaint(new GradientPaint(0, -knobSize * 0.5f, new Color(0x222222), 0, knobSize * 0.5f, new Color(0x888888)));
				}
				g2.fill(new Ellipse2D.Float(-knobSize * 0.5f, -knobSize * 0.5f, knobSize, knobSize));
				if (!active) {
					g2.setPaint(new GradientPaint(0, -knobSize * 0.5f, new Color(0x666666), 0, knobSize * 0.5f, new Color(0xeeeeee)));
				} else {
					g2.setPaint(new GradientPaint(0, -knobSize * 0.5f, new Color(0x444444), 0, knobSize * 0.5f, new Color(0xaaaaaa)));
				}
				g2.fill(new Ellipse2D.Float(-knobSize * 0.25f, -knobSize * 0.25f, knobSize * 0.5f, knobSize * 0.5f));
				
			}
		};
		
		component.setPreferredSize(new Dimension(size, size));
		component.addMouseListener(new MouseAdapter() {
//			public void mouseEntered(MouseEvent e) {
//				System.out.println(e.getButton());
//				hover = true;
//				computeState();
//			}
			public void mouseExited(MouseEvent e) {
				hover = false;
				computeState();
			}
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					active = true;
					computeState();
					
					float knobSize = size / 4f;
					double a = (alpha % 360) / 180 * Math.PI;
					double x = Math.sin(a);
					double y = -Math.cos(a);
					double s = size * 0.5;
					double r = s - knobSize * 0.5 - 2;
					Point p = new Point((int) (s + r * x), (int) (s + r * y));
					SwingUtilities.convertPointToScreen(p, component);
					if (robot != null) {
						robot.mouseMove(p.x, p.y);
					}
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					active = false;
					computeState();
				}
			}
		});
		component.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				if (!hover) {
					hover = true;
					computeState();
				}
			}
			
			public void mouseDragged(MouseEvent e) {
				double x = e.getX() - size * 0.5;
				double y = e.getY() - size * 0.5;
				if (x * x + y * y < 4) {
					return;
				}
				double a = Math.atan2(x, -y);
				double tmp = alpha;
				alpha = a / Math.PI * 180;
				if (Math.abs(alpha) < 90 && Math.abs(tmp) < 90) {
					if (tmp < 0 && alpha >= 0) {
						rotations++;
					} else if (tmp >= 0 && alpha < 0) {
						rotations--;
					}
				}
				double fraction = alpha / 360;
				if (fraction < 0) {
					fraction += 1;
				}
				System.out.println(rotations + fraction);
				component.repaint();
			}
		});
	}
	
	public static void setRobot(Robot robot) {
		Knob.robot = robot;
	}
	
	private void computeState() {
		if (active) {
			state = State.ACTIVE;
		} else {
			if (hover) {
				state = State.HOVER;
			} else {
				state = State.IDLE;
			}
		}
		component.repaint();
	}
	
	public JComponent getComponent() {
		return component;
	}
	
	private static BufferedImage drawKnob(int size, State state) {
		BufferedImage img1 = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img1.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		switch (state) {
		case IDLE:
			g2.setPaint(new GradientPaint(0, 0, new Color(0xaaaaaa), 0, size, new Color(0xeeeeee)));
			break;
		case HOVER:
			g2.setPaint(new GradientPaint(0, 0, new Color(0xcccccc), 0, size, new Color(0xffffff)));
			break;
		case ACTIVE:
			g2.setPaint(new GradientPaint(0, 0, new Color(0x888888), 0, size, new Color(0xcccccc)));
			break;
		}
		
		g2.fill(new Ellipse2D.Float(1, 1, size - 2, size - 2));
		g2.setClip(new Ellipse2D.Float(1, 1, size - 2, size - 2));
		g2.setPaint(new GradientPaint(0, 0, new Color(0xffffffff, true), 0, size, new Color(0x00ffffff, true)));
		
		g2.fill(new Ellipse2D.Float(2, 2, size - 4, size / 2 - 2));
		
		g2.setClip(null);
//		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
//		g2 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//		g2.setStroke(new BasicStroke(2));
		
		g2.setPaint(new GradientPaint(0, size / 4, new Color(0x00ffffff, true), 0, size * 3 / 4, new Color(0xffffffff,true)));
		Area a = new Area(new Ellipse2D.Float(0, 0, size, size));
		a.subtract(new Area(new Ellipse2D.Float(1, 1, size - 2, size - 2)));
		g2.setClip(0, size / 2, size, size/ 2);
		g2.fill(a);
		g2.setClip(null);
		g2.setPaint(new GradientPaint(0, 0, new Color(0x888888), 0, size, new Color(0x888888)));
		a = new Area(new Ellipse2D.Float(1, 1, size - 2, size - 2));
		a.subtract(new Area(new Ellipse2D.Float(2, 2, size - 4, size - 4)));
		g2.fill(a);
		
//		g2.drawImage(img1, 0, 0, null);
		
		
		return img1;
	}
	
	public static void main(String[] args) throws AWTException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Knob.setRobot(new Robot());
		int size = 20;
		panel.add(new Knob(size).getComponent());
		panel.add(new Knob(size).getComponent());
		panel.add(new Knob(size).getComponent());
		panel.add(new Knob(size).getComponent());
		panel.add(new Knob(size).getComponent());
		panel.add(new Knob(size).getComponent());
		panel.add(new Knob(size).getComponent());
		panel.add(new Knob(size).getComponent());
		panel.add(new Knob(size).getComponent());
		frame.add(panel);
		frame.setSize(100, 100);
		frame.setVisible(true);
	}
}
