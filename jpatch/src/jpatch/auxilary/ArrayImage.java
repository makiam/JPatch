package jpatch.auxilary;

import java.awt.image.*;

public class ArrayImage {
	private final BufferedImage image;
	private final int[] buffer;
	
	public ArrayImage(int width, int height) {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		buffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public int[] getBuffer() {
		return buffer;
	}
	
	public int getWidth() {
		return image.getWidth();
	}
	
	public int getHeight() {
		return image.getHeight();
	}
}
