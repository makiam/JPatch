package jpatch.boundary;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import java.io.*;
import javax.imageio.ImageIO;

/**
 * 
 */

/**
 * @author sascha
 *
 */
public class ImageIconFactory {
	public static enum Position { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
	
	private static BufferedImage lockedImage;
	
	static {
		try {
			lockedImage = ImageIO.read(ClassLoader.getSystemResource("jpatch/images/lock.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ImageIcon createLockedIcon(Icon icon, Position position) {
		if (icon instanceof ImageIcon)
			return createLockedIcon(((ImageIcon) icon).getImage(), position);
		BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		icon.paintIcon(null, image.createGraphics(), 0, 0);
		return createLockedIcon(image, position);
	}
	
	public static ImageIcon createLockedIcon(Image image, Position position) {
		return new ImageIcon(createLockedImage(image, position));
	}
	
	public static BufferedImage createLockedImage(Image image, Position position) {
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = newImage.createGraphics();
		g2.drawImage(image, 0, 0, null);
		switch (position) {
		case TOP_LEFT:
			g2.drawImage(lockedImage, 0, 0, null);
			break;
		case TOP_RIGHT:
			g2.drawImage(lockedImage, width - lockedImage.getWidth(), 0, null);
			break;
		case BOTTOM_LEFT:
			g2.drawImage(lockedImage, 0, height - lockedImage.getHeight(), null);
			break;
		case BOTTOM_RIGHT:
			g2.drawImage(lockedImage, width - lockedImage.getWidth(), height - lockedImage.getHeight(), null);
			break;
		}
		return newImage;
	}
}
