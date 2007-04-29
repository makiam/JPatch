package com.jpatch.afw.icons;

import com.jpatch.afw.ui.ImageUtils;

import java.awt.Image;
import java.awt.image.*;
import java.io.Serializable;
import java.util.Random;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;

public class IconSet implements Serializable {
	private static final long serialVersionUID = -5773242156358795217L;
	public static enum Style { GLOSSY, FROSTED, BRUSHED, DARK }
	public static enum Type { SINGLE, ROUND, LARGE, LEFT, CENTER, RIGHT }
	private PackedIcon[][] icons = new PackedIcon[Style.class.getEnumConstants().length][Type.class.getEnumConstants().length];
	private Tint defaultTint = new Tint(+0x00, +0x00, +0x00);
	private Tint rolloverTint = new Tint(+0x08, +0x08, -0x04);
	private Tint selectedTint = new Tint(-0x20, -0x20, -0x18);
	private Tint pressedTint = new Tint(-0x24, -0x24, -0x1c
			);
	private Tint rolloverSelectedTint = new Tint(-0x18, -0x18, -0x20);
	
	public void setIcon(Style style, Type type, BufferedImage image, BufferedImage stencil, int xOffset, int yOffset) {
		icons[style.ordinal()][type.ordinal()] = new PackedIcon(image, stencil, xOffset, yOffset);
	}
	
	public void configureButton(AbstractButton button, Style style, Type type, Image icon) {
		BufferedImage defaultImage = defaultTint.createTintedImage(icons[style.ordinal()][type.ordinal()]);
		BufferedImage disabledImage = defaultTint.createTintedImage(icons[style.ordinal()][type.ordinal()]);
		BufferedImage rolloverImage = rolloverTint.createTintedImage(icons[style.ordinal()][type.ordinal()]);
		BufferedImage selectedImage = selectedTint.createTintedImage(icons[style.ordinal()][type.ordinal()]);
		BufferedImage pressedImage = pressedTint.createTintedImage(icons[style.ordinal()][type.ordinal()]);
		BufferedImage rolloverSelectedImage = rolloverSelectedTint.createTintedImage(icons[style.ordinal()][type.ordinal()]);
		BufferedImage disabledSelectedImage = selectedTint.createTintedImage(icons[style.ordinal()][type.ordinal()]);
		Image disabledIcon = ImageUtils.createDisabledIcon(icon);
		int xOffset = icons[style.ordinal()][type.ordinal()].xOffset;
		int yOffset = icons[style.ordinal()][type.ordinal()].yOffset;
		defaultImage.createGraphics().drawImage(icon, xOffset, yOffset, null);
		disabledImage.createGraphics().drawImage(disabledIcon, xOffset, yOffset, null);
		rolloverImage.createGraphics().drawImage(icon, xOffset, yOffset, null);
		selectedImage.createGraphics().drawImage(icon, xOffset, yOffset, null);
		pressedImage.createGraphics().drawImage(icon, xOffset, yOffset, null);
		rolloverSelectedImage.createGraphics().drawImage(icon, xOffset, yOffset, null);
		disabledSelectedImage.createGraphics().drawImage(disabledIcon, xOffset, yOffset, null);
		button.setIcon(new ImageIcon(defaultImage));
		button.setDisabledIcon(new ImageIcon(disabledImage));
		button.setRolloverIcon(new ImageIcon(rolloverImage));
		button.setSelectedIcon(new ImageIcon(selectedImage));
		button.setPressedIcon(new ImageIcon(pressedImage));
		button.setRolloverSelectedIcon(new ImageIcon(rolloverSelectedImage));
		button.setDisabledSelectedIcon(new ImageIcon(disabledSelectedImage));
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setBorder(null);
	}
	
	/**
	 * Stores bitmaps for button-icons: One 32-bit (INT_ARGB) image and one 8-bit (BYTE_GRAY) stencil.
	 * The stencil is used to create color tinted versions of the original image (for rollover,
	 * pressed and selected effects). The offset is used to position 16x16 icons inside the button.
	 * @author sascha
	 */
	private static class PackedIcon implements Serializable {
		private static final long serialVersionUID = -4332514525746402429L;
		final int width, height, xOffset, yOffset;
		final int[] argb;
		final byte[] stencil;
		
		PackedIcon(BufferedImage image, BufferedImage stencil, int xOffset, int yOffset) {
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
	
	static class Tint implements Serializable {
		private static final long serialVersionUID = -1187138026170174534L;
		private int red, green, blue;
		
		Tint(int red, int green, int blue) {
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
		
		BufferedImage createTintedImage(PackedIcon icon) {
			BufferedImage image = new BufferedImage(icon.width, icon.height, BufferedImage.TYPE_INT_ARGB);
			int[] buffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			for (int i = 0; i < buffer.length; i++) {
				int a = (icon.argb[i] & 0xff000000);
				int r = (icon.argb[i] & 0x00ff0000) >> 16;
				int g = (icon.argb[i] & 0x0000ff00) >> 8;
				int b = (icon.argb[i] & 0x000000ff);
				int s = icon.stencil[i] & 0xff;
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
}
