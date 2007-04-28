package com.jpatch.afw.ui;

import java.awt.*;
import javax.swing.*;

public class ImagePanel  {
	private Image image;
	
	@SuppressWarnings("serial")
	private final JComponent component = new JComponent() {
		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(new Color(0x777777));
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(new Color(0x999999));
			for (int i = 0; i < getWidth() + getHeight(); i+= 2) {
				g.drawLine(0, i, i, 0);
			}
			if (image != null) {
				g.drawImage(image, 1, 1, null);
				g.setColor(Color.WHITE);
				g.drawRect(0, 0, image.getWidth(null) + 1, image.getHeight(null) + 1);
			}
		}
	};
	
	public ImagePanel() { }
	
	public ImagePanel(Image image) {
		this();
		setImage(image);
	}
	
	public JComponent getComponent() {
		return component;
	}
	
	public void setImage(Image image) {
		this.image = image;
		if (image != null) {
			component.setPreferredSize(new Dimension(image.getWidth(null) + 2, image.getHeight(null) + 2));
		}
	}
	
	public Image getImage() {
		return image;
	}
}

