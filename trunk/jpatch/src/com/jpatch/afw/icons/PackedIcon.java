package com.jpatch.afw.icons;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.Serializable;

/**
 * Stores bitmaps for button-icons: One 32-bit (INT_ARGB) image and one 8-bit (BYTE_GRAY) stencil.
 * The stencil is used to create color tinted versions of the original image (for rollover,
 * pressed and selected effects). The offset is used to position 16x16 icons inside the button.
 * @author sascha
 */
public class PackedIcon implements Serializable {
	private static final long serialVersionUID = -4332514525746402429L;
	private final int width, height, xOffset, yOffset;
	private final int[] argb;
	private final byte[] stencil;
	
	public int[] getArgb() {
		return argb;
	}

	public int getHeight() {
		return height;
	}

	public byte[] getStencil() {
		return stencil;
	}

	public int getWidth() {
		return width;
	}

	public int getXOffset() {
		return xOffset;
	}

	public int getYOffset() {
		return yOffset;
	}

	public PackedIcon(BufferedImage image, BufferedImage stencil, int xOffset, int yOffset) {
		this.width = image.getWidth();
		this.height = image.getHeight();
		if (stencil.getWidth() != width || stencil.getHeight() != height) {
			throw new IllegalArgumentException("stencil size does not equal image size");
		}
		if (!(image.getData().getDataBuffer() instanceof DataBufferInt)) {
			throw new IllegalArgumentException("image type != DataBufferInt");
		}
		if (!(stencil.getData().getDataBuffer() instanceof DataBufferByte)) {
			throw new IllegalArgumentException("stencil type != DataBufferByte");
		}
		this.argb = ((DataBufferInt) image.getData().getDataBuffer()).getData();
		this.stencil = ((DataBufferByte) stencil.getData().getDataBuffer()).getData();
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
}