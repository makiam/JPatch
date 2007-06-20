package com.jpatch.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ObjectInputStream;

import javax.swing.JComponent;

import com.jpatch.afw.attributes.StateMachine;
import com.jpatch.afw.icons.IconSet;
import com.jpatch.afw.icons.PackedIcon;
import com.jpatch.boundary.actions.Actions;

import static com.jpatch.boundary.actions.Actions.ViewportMode;

public class ViewportSwitcher {
	
	private final BufferedImage[][] images = new BufferedImage[4][5];
	private final boolean[] selected = new boolean[] { true, false, false, false };
	private final int[] xOff = new int[4];
	private final int[] yOff = new int[4];
	private final int w, h;
	private final StateMachine<Actions.ViewportMode> stateMachine;
	private int mousePosition = -1;
	private int mx0, my0, mx1, my1;
	private boolean mousePressed = false;
	private final JComponent component = new JComponent() {
		@Override
		protected void paintComponent(Graphics g) {
			for (int i = 0; i < 4; i++) {
				int mode;
				if (selected[i]) {
					if (mousePressed) {
						mode = 4;
					} else if (mousePosition == i) {
						mode = 3;
					} else {
						mode = 2;
					}
				} else {
					if (mousePosition == i) {
						mode = 1;
					} else {
						mode = 0;
					}
				}
				g.drawImage(images[i][mode], xOff[i], yOff[i], null);
			}
			if (mousePressed) {
				int rx = Math.min(mx0, mx1);
				int ry = Math.min(my0, my1);
				int rw = Math.abs(mx1 - mx0);
				int rh = Math.abs(my1 - my0);
				g.setClip(new RoundRectangle2D.Float(2, 2, w * 2 - 4, h * 2 - 4, h * 0.5f, h * 0.5f));
				g.setColor(new Color(0x40ffffff, true));
				g.fillRect(rx, ry, rw, rh);
				g.setColor(new Color(0x80ffffff, true));
				g.drawRect(rx, ry, rw, rh);
				g.setClip(null);
			}
		}
	};
	
	public ViewportSwitcher(StateMachine<Actions.ViewportMode> modeStateMachine) {
		stateMachine = modeStateMachine;
		PackedIcon[] icons;
		try {
			ObjectInputStream ois = new ObjectInputStream(ClassLoader.getSystemResourceAsStream("com/jpatch/icons/switcher.iconset"));
			icons = (PackedIcon[]) ois.readObject();
			ois.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		IconSet iconSet = new IconSet(); // FIXME
		for (int i = 0; i < 4; i++) {
			images[i][0] = iconSet.createTintedImage(icons[i], IconSet.Mode.DEFAULT);
			images[i][1] = iconSet.createTintedImage(icons[i], IconSet.Mode.ROLLOVER);
			images[i][2] = iconSet.createTintedImage(icons[i], IconSet.Mode.SELECTED);
			images[i][3] = iconSet.createTintedImage(icons[i], IconSet.Mode.ROLLOVERSELECTED);
			images[i][4] = iconSet.createTintedImage(icons[i], IconSet.Mode.PRESSED);
		}
		w = images[0][0].getWidth();
		h = images[0][0].getHeight();
		xOff[1] = xOff[3] = w;
		yOff[2] = yOff[3] = h;
		
		component.setPreferredSize(new Dimension(2 * w, 2 * h));
		component.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int pos = getPosition(e.getX(), e.getY());
				if (pos != mousePosition) {
					mousePosition = pos;
					component.repaint();
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				mx1 = e.getX();
				my1 = e.getY();
				int pos = getPosition(mx1, my1);
				for (int i = 0; i < 4; i++) {
					selected[i] = i == mousePosition || i == pos;
				}
				if (selected[0] && selected[3]) {
					selected[1] = selected[2] = true;
				} else if (selected[1] && selected[2]) {
					selected[0] = selected[3] = true;
				}
				component.repaint();
			}
			
		});
		component.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				if (!mousePressed) {
					mousePosition = -1;
					component.repaint();
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				mousePressed = true;
				mx0 = mx1 = e.getX();
				my0 = my1 = e.getY();
				mousePosition = getPosition(mx0, my0);
				for (int i = 0; i < 4; i++) {
					selected[i] = mousePosition == i;
				}
				component.repaint();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				mousePressed = false;
				int x = e.getX();
				int y = e.getY();
				if (x < 0 || y < 0 || x > w * 2 || y > h * 2) {
					mousePosition = -1;
				} else {
					mousePosition = getPosition(x, y);
				}
				if (selected[0] && !selected[1] && !selected[2] && !selected[3]) stateMachine.setValue(ViewportMode.VIEWPORT_1);
				else if (!selected[0] && selected[1] && !selected[2] && !selected[3]) stateMachine.setValue(ViewportMode.VIEWPORT_2);
				else if (!selected[0] && !selected[1] && selected[2] && !selected[3]) stateMachine.setValue(ViewportMode.VIEWPORT_3);
				else if (!selected[0] && !selected[1] && !selected[2] && selected[3]) stateMachine.setValue(ViewportMode.VIEWPORT_4);
				else if (selected[0] && selected[1] && !selected[2] && !selected[3]) stateMachine.setValue(ViewportMode.SPLIT_1_2);
				else if (!selected[0] && !selected[1] && selected[2] && selected[3]) stateMachine.setValue(ViewportMode.SPLIT_3_4);
				else if (selected[0] && !selected[1] && selected[2] && !selected[3]) stateMachine.setValue(ViewportMode.SPLIT_1_3);
				else if (!selected[0] && selected[1] && !selected[2] && selected[3]) stateMachine.setValue(ViewportMode.SPLIT_2_4);
				else if (selected[0] && selected[1] && selected[2] && selected[3]) stateMachine.setValue(ViewportMode.QUAD);
				else throw new IllegalStateException();
				component.repaint();
			}
		});
	}
	
	public JComponent getComponent() {
		return component;
	}
	
	private int getPosition(int x, int y) {
		if (y < h) {
			if (x < w) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (x < w) {
				return 2;
			} else {
				return 3;
			}
		}
	}
}
