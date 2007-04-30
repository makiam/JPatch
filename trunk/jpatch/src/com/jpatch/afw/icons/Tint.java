/**
 * 
 */
package com.jpatch.afw.icons;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.Serializable;

class Tint implements Serializable {
	private static final long serialVersionUID = -1187138026170174534L;
	private int red, green, blue;
	
	Tint(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	BufferedImage createTintedImage(PackedIcon icon) {
		int[] argb = icon.getArgb();
		byte[] stencil = icon.getStencil();
		BufferedImage image = new BufferedImage(icon.getWidth(), icon.getHeight(), BufferedImage.TYPE_INT_ARGB);
		int[] buffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		for (int i = 0; i < buffer.length; i++) {
			int a = (argb[i] & 0xff000000);
			int r = (argb[i] & 0x00ff0000) >> 16;
			int g = (argb[i] & 0x0000ff00) >> 8;
			int b = (argb[i] & 0x000000ff);
			int s = stencil[i] & 0xff;
			r += (red * s) >> 8;
			g += (green * s) >> 8;
			b += (blue * s) >> 8;
			r = Math.min(0xff, Math.max(0, r));
			g = Math.min(0xff, Math.max(0, g));
			b = Math.min(0xff, Math.max(0, b));
			buffer[i] = a | (r << 16) | (g << 8) | b;
		}
		return image;
	}
}