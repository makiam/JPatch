package com.jpatch.afw.icons;

import com.jpatch.afw.ui.ImageUtils;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.metal.MetalButtonUI;

public class IconSet implements Serializable {
	private static final long serialVersionUID = -5773242156358795217L;
	public static enum Style { GLOSSY, FROSTED, BRUSHED, DARK, TINY, UNDECORATED }
	public static enum Type { SINGLE, ROUND, LARGE, LEFT, CENTER, RIGHT }
	public static enum Mode { DEFAULT, ROLLOVER, SELECTED, PRESSED, ROLLOVERSELECTED }
	
	private PackedIcon[][] icons = new PackedIcon[Style.class.getEnumConstants().length][Type.class.getEnumConstants().length];
	private Map<Mode, Tint> tints = new HashMap<Mode, Tint>();
	
	public IconSet() {
		tints.put(Mode.DEFAULT, new Tint(+0x00, +0x00, +0x00));
		tints.put(Mode.ROLLOVER, new Tint(+0x08, +0x08, -0x04));
		tints.put(Mode.SELECTED, new Tint(-0x20, -0x20, -0x18));
		tints.put(Mode.PRESSED, new Tint(-0x24, -0x24, -0x1c));
		tints.put(Mode.ROLLOVERSELECTED, new Tint(-0x18, -0x18, -0x20));
	}
	
	public void setIcon(Style style, Type type, BufferedImage image, BufferedImage stencil, int xOffset, int yOffset) {
		icons[style.ordinal()][type.ordinal()] = new PackedIcon(image, stencil, xOffset, yOffset);
	}
	
	public BufferedImage createTintedImage(PackedIcon packedIcon, Mode mode) {
		return tints.get(mode).createTintedImage(packedIcon);
	}
	
	public void configureButton(AbstractButton button, Style style, Type type, Icon icon) {
		BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		icon.paintIcon(null, image.createGraphics(), 0, 0);
		if (style == Style.UNDECORATED) {
			button.setIcon(new ImageIcon(image));
			button.setContentAreaFilled(false);
			button.setBorderPainted(false);
			button.setBorder(null);
			button.setOpaque(false);
			button.setPreferredSize(new Dimension(icon.getIconWidth() + 4, icon.getIconHeight() + 6));
			button.setRolloverEnabled(true);
			button.setFocusable(false);
			return;
		}
		BufferedImage defaultImage = createTintedImage(icons[style.ordinal()][type.ordinal()], Mode.DEFAULT);
		BufferedImage disabledImage = createTintedImage(icons[style.ordinal()][type.ordinal()], Mode.DEFAULT);
		BufferedImage rolloverImage = createTintedImage(icons[style.ordinal()][type.ordinal()], Mode.ROLLOVER);
		BufferedImage selectedImage = createTintedImage(icons[style.ordinal()][type.ordinal()], Mode.SELECTED);
		BufferedImage pressedImage = createTintedImage(icons[style.ordinal()][type.ordinal()], Mode.PRESSED);
		BufferedImage rolloverSelectedImage = createTintedImage(icons[style.ordinal()][type.ordinal()], Mode.ROLLOVERSELECTED);
		BufferedImage disabledSelectedImage = createTintedImage(icons[style.ordinal()][type.ordinal()], Mode.SELECTED);
		Image disabledIcon = ImageUtils.createDisabledIcon(image);
		int xOffset = icons[style.ordinal()][type.ordinal()].getXOffset();
		int yOffset = icons[style.ordinal()][type.ordinal()].getYOffset();
		defaultImage.createGraphics().drawImage(image, xOffset, yOffset, null);
		disabledImage.createGraphics().drawImage(disabledIcon, xOffset, yOffset, null);
		rolloverImage.createGraphics().drawImage(image, xOffset, yOffset, null);
		selectedImage.createGraphics().drawImage(image, xOffset, yOffset, null);
		pressedImage.createGraphics().drawImage(image, xOffset, yOffset, null);
		rolloverSelectedImage.createGraphics().drawImage(image, xOffset, yOffset, null);
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
		button.setPreferredSize(new Dimension(defaultImage.getWidth(), defaultImage.getHeight()));
		button.setRolloverEnabled(true);
	}
}
