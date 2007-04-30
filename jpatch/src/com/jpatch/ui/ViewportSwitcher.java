package com.jpatch.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.ObjectInputStream;

import javax.swing.JComponent;

import com.jpatch.afw.icons.IconSet;
import com.jpatch.afw.icons.PackedIcon;

public class ViewportSwitcher {
	private final BufferedImage[][] images = new BufferedImage[4][5];
	private final boolean[] selected = new boolean[] { true, false, false, false };
	private final int[] xOff = new int[4];
	private final int[] yOff = new int[4];
	private final int w, h;
	private int mousePosition = -1;
	private boolean mousePressed = false;
	private JComponent component = new JComponent() {
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
		}
	};
	
	public ViewportSwitcher() {
		PackedIcon[] icons;
		try {
			ObjectInputStream ois = new ObjectInputStream(ClassLoader.getSystemResourceAsStream("com/jpatch/icons/switcher"));
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
		});
		component.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				mousePosition = -1;
				component.repaint();
			}
			@Override
			public void mousePressed(MouseEvent e) {
				mousePressed = true;
				mousePosition = getPosition(e.getX(), e.getY());
				for (int i = 0; i < 4; i++) {
					selected[i] = mousePosition == i;
				}
				component.repaint();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				mousePressed = false;
				mousePosition = getPosition(e.getX(), e.getY());
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
