package jpatch.boundary;

import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.imageio.*;
import javax.vecmath.*;

public final class Rotoscope {

	private BufferedImage originalImage;
	private BufferedImage filteredImage;
	private int iOpacity;
	private float fXPosition;
	private float fYPosition;
	private float fScale;
	private String strFilename;
	private boolean bValid;
	private boolean glImage;
	private boolean bImageChanged = false;
	public Rotoscope() {
		bValid = false;
	}
	
	public Rotoscope(String filename) {
		loadImageFromFile(filename);
	}
	
	public boolean isValid() {
		return bValid;
	}
	
	public int getPixelWidth() {
		return originalImage.getWidth();
	}
	
	public int getPixelHeight() {
		return originalImage.getHeight();
	}
	
	public float getScale() {
		return fScale;
	}
	
	public float getXPosition() {
		return fXPosition;
	}
	
	public float getYPosition() {
		return fYPosition;
	}
	
	public void setPosition(float x, float y) {
		fXPosition = x;
		fYPosition = y;
	}
	
	public void setScale(float scale) {
		fScale = scale;
	}
	
	public void loadImageFromFile(String filename) {
		//System.out.println("loadImageFromFile(" + filename + ") called...");
		strFilename = filename;
		BufferedImage image;
		//ImageIcon imageIcon = new ImageIcon(filename);
		//image = imageIcon.getImage();
		//if (image.getWidth(null) == -1) {
		//	bValid = false;
		//	System.out.println("ERROR");
		//	return;
		//}
		try {
			image = ImageIO.read(new File(filename));
		} catch (IOException ioException) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(),"Unable to load rotoscope image \"" + filename + "\"\n" + ioException, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (image == null) {
			//System.err.println("ERROR - Could not load image!");
			JOptionPane.showMessageDialog(MainFrame.getInstance(),"Unable to load rotoscope image \"" + filename + "\"\nInvalid file format", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		originalImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		originalImage.getGraphics().drawImage(image, 0, 0, null);
		image = null;
//		originalRGBImage = image;
		
		//System.out.println("done. Size=" + image.getWidth(null) + "x" + image.getHeight(null));
		fScale = 0.05f;
		fXPosition = 0; //-fScale * 0.5f * image.getWidth(null);
		fYPosition = 0; //-fScale * 0.5f * image.getHeight(null);
		//System.out.print("Creating BufferedImage...");
		filteredImage = new BufferedImage(originalImage.getWidth(null),originalImage.getHeight(null),BufferedImage.TYPE_3BYTE_BGR);
		glImage = false;
		bValid = true;
		//System.out.println("done. Size = " + filteredRGBImage.getWidth(null) + "x" + filteredRGBImage.getHeight(null));
		//System.out.print("Filtering image...");
		setOpacity(128);
		//int rgb = filteredRGBImage.getRGB(0,0);
		//int r = (rgb & 0xFF0000) >> 16;
		//int g = (rgb & 0xFF00) >> 8;
		//int b = rgb & 0xFF;
		//System.out.println("done. RGB at 0/0 = " + r + "," + g + "," + b);
		
	}
	
	public void setOpacity(int opacity) {
		iOpacity = opacity;
		if (bValid) {
			// the following lines did not work on Mac OS X, so they have been changed...
			//
			//Graphics2D g2 = (Graphics2D)filteredImage.getGraphics();
			//g2.drawImage(image,0,0,null);
			//Color backgroundColor = JPatchUserSettings.getInstance().cBackground;
			//g2.setColor(new Color(backgroundColor.getRed(),backgroundColor.getGreen(),backgroundColor.getBlue(),255 - iOpacity));
			//g2.fillRect(0,0,filteredImage.getWidth(),filteredImage.getHeight());
			//
			// ------------------------------------------------------------------------------
			int backgroundRed = ((JPatchUserSettings.getInstance().colors.background.get().getRed() * (255 - iOpacity)) & 0xFF00);
			int backgroundGreen = ((JPatchUserSettings.getInstance().colors.background.get().getGreen() * (255 - iOpacity)) & 0xFF00);
			int backgroundBlue = ((JPatchUserSettings.getInstance().colors.background.get().getBlue() * (255 - iOpacity)) & 0xFF00);
			//int backgroundColor = backgroundRed | backgroundGreen | backgroundBlue;
			
//			int[] aiRGBoriginal = ((DataBufferInt) originalRGBImage.getRaster().getDataBuffer()).getData();
//			int[] aiRGBfiltered = ((DataBufferInt) filteredRGBImage.getRaster().getDataBuffer()).getData();
//			int size = aiRGBoriginal.length;
//			for (int i = 0; i < size; i++) {
//				int r = (((aiRGBoriginal[i] & 0xFF0000) * iOpacity) & 0xFF000000) >> 8;
//				int g = (((aiRGBoriginal[i] & 0xFF00) * iOpacity) & 0xFF0000) >> 8;
//				int b = (((aiRGBoriginal[i] & 0xFF) * iOpacity) & 0xFF00) >> 8;
//				aiRGBfiltered[i] = (r | g | b) + backgroundColor;
//			}
			
			byte[] abBGR = ((DataBufferByte) filteredImage.getRaster().getDataBuffer()).getData();
			byte[] abBGRo = ((DataBufferByte) originalImage.getRaster().getDataBuffer()).getData();
			int source = 0;
			int destination = 0;
			int width = originalImage.getWidth();
			if (filteredImage.getWidth() < width)
				width = filteredImage.getWidth();
			int height = originalImage.getHeight();
			if (filteredImage.getHeight() < height)
				height = filteredImage.getHeight();
			int soffset = (originalImage.getWidth() - width) * 3;
			int doffset = (filteredImage.getWidth() - width) * 3;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int b = abBGRo[source++] & 0xff;
					abBGR[destination++] = (byte) ((iOpacity * b + backgroundBlue) >> 8);
					int g = abBGRo[source++] & 0xff;
					abBGR[destination++] = (byte) ((iOpacity * g + backgroundGreen) >> 8);
					int r = abBGRo[source++] & 0xff;
					abBGR[destination++] = (byte) ((iOpacity * r + backgroundRed) >> 8);
//					abBGR[i++] = (byte) (rgb & 0xff);
//					abBGR[i++] = (byte) ((rgb >> 8) & 0xff);
//					abBGR[i++] = (byte) ((rgb >> 16) & 0xff);
				}
				source += soffset;
				destination += doffset;
			}
		}
		bImageChanged = true;
	}

	public int getOpacity() {
		return iOpacity;
	}
	
	/**
	 * paint the background image on canvas
	 */
	public void paint(ViewDefinition viewDef) {
		JPatchDrawable2 drawable = viewDef.getDrawable();
		
		/*
		 * GL drawables have to be treated separately
		 */
		boolean gl = (drawable instanceof JPatchDrawableGL);
		if (gl && !glImage) {
			int x = ((JPatchDrawableGL) drawable).getTextureSize(originalImage.getWidth());
			int y = ((JPatchDrawableGL) drawable).getTextureSize(originalImage.getHeight());
			filteredImage = new BufferedImage(x, y, BufferedImage.TYPE_3BYTE_BGR);
			glImage = true;
			setOpacity(iOpacity);
		} else if (!gl && glImage) {
			filteredImage = new BufferedImage(originalImage.getWidth(null),originalImage.getHeight(null),BufferedImage.TYPE_3BYTE_BGR);
			glImage = false;
			setOpacity(iOpacity);
		}
		if (glImage && bImageChanged) {
			((JPatchDrawableGL) drawable).setTexture(filteredImage.getWidth(), filteredImage.getHeight(), ((DataBufferByte) filteredImage.getRaster().getDataBuffer()).getData());
			bImageChanged = false;
		}
		
		float viewTranslateX = viewDef.getTranslateX();
		float viewTranslateY = viewDef.getTranslateY();
		float viewWidth = viewDef.getWidth();
		float viewHeight = viewDef.getHeight();
		float viewScale = viewDef.getScale() * viewWidth * 0.5f;
		float scale = fScale * viewScale;
		
		float xPos = fXPosition - fScale * 0.5f * originalImage.getWidth();
		float yPos = fYPosition + fScale * 0.5f * originalImage.getHeight();
		/* set up affine transform */
		//affineTransform = new AffineTransform(scale,0,0,scale,viewWidth * 0.5f + (viewTranslateX + xPos) * viewScale,viewHeight * 0.5f + (viewTranslateY + yPos) * viewScale);
		
		/* paint image */
		//g2.drawImage(filteredRGBImage,affineTransform,null);
		
		int iLeftX = (int) (viewWidth * 0.5f + (viewTranslateX + xPos) * viewScale);
		int iWidth = (int) (scale * getPixelWidth());
		int iTopY = (int) (viewHeight * 0.5f - (viewTranslateY + yPos) * viewScale);
		int iHeight = (int) (scale * getPixelHeight());
		
		if (glImage)
			((JPatchDrawableGL) drawable).drawImage(originalImage.getWidth(), originalImage.getHeight(), iLeftX, iTopY, scale, scale);
		else
			drawable.drawImage(filteredImage, iLeftX, iTopY, scale, scale);
		drawable.setColor(JPatchUserSettings.getInstance().colors.grey);
		drawable.drawRect(iLeftX, iTopY, iWidth, iHeight);
		drawable.drawRect(iLeftX - 1, iTopY - 1, iWidth + 2, iHeight + 2);
//		drawable.drawLine(iLeftX, iTopY, iRightX, iTopY);
//		drawable.drawLine(iLeftX, iBottomY, iRightX, iBottomY);
//		drawable.drawLine(iLeftX, iTopY, iLeftX, iBottomY);
//		drawable.drawLine(iRightX, iTopY, iRightX, iBottomY);
		
		//g2.drawRect(iLeftX,iTopY,iRightX - iLeftX,iBottomY - iTopY);
	}
	
	/**
	 * Get XML representation of rotoscope
	 *
	 * @param tabs The indent level
	 */
	public StringBuffer xml(String prefix, String view) {
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append("<rotoscope view=\"" + view + "\">").append("\n");
		sb.append(prefix).append("\t<image>").append(strFilename).append("</image>").append("\n");
		sb.append(prefix).append("\t<display x=\"" + fXPosition + "\" y=\"" +fYPosition + "\" scale=\"" + fScale + "\" opacity=\"" + iOpacity +"\"/>").append("\n");
		sb.append(prefix).append("</rotoscope>").append("\n");
		return sb;
	}
}

