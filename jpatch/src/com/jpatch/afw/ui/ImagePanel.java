package com.jpatch.afw.ui;

import java.awt.*;
import javax.swing.*;

public class ImagePanel  {
	private Image image;
	
	@SuppressWarnings("serial")
	private final JComponent component = new JComponent() {
		@Override
		protected void paintComponent(Graphics g) {
			if (image != null) {
				g.drawImage(image, 0, 0, null);
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
			component.setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
		}
	}
	
	public Image getImage() {
		return image;
	}
}

