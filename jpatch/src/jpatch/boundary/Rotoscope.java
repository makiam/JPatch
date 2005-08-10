package jpatch.boundary;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.imageio.*;

import jpatch.auxilary.*;

public final class Rotoscope {

	private BufferedImage originalRGBImage;
	private BufferedImage filteredRGBImage;
	private int iOpacity;
	private float fXPosition;
	private float fYPosition;
	private float fScale;
	private AffineTransform affineTransform = new AffineTransform();
	private String strFilename;
	private boolean bValid;
	
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
		return originalRGBImage.getWidth();
	}
	
	public int getPixelHeight() {
		return originalRGBImage.getHeight();
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
		originalRGBImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		int[] aiRGB = ((DataBufferInt) originalRGBImage.getRaster().getDataBuffer()).getData();
		int i = 0;
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				aiRGB[i++] = image.getRGB(x,y);
			}
		}
		
		//System.out.println("done. Size=" + image.getWidth(null) + "x" + image.getHeight(null));
		fScale = 0.05f;
		fXPosition = 0; //-fScale * 0.5f * image.getWidth(null);
		fYPosition = 0; //-fScale * 0.5f * image.getHeight(null);
		//System.out.print("Creating BufferedImage...");
		filteredRGBImage = new BufferedImage(originalRGBImage.getWidth(null),originalRGBImage.getHeight(null),BufferedImage.TYPE_INT_RGB);
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
			//Color backgroundColor = JPatchSettings.getInstance().cBackground;
			//g2.setColor(new Color(backgroundColor.getRed(),backgroundColor.getGreen(),backgroundColor.getBlue(),255 - iOpacity));
			//g2.fillRect(0,0,filteredImage.getWidth(),filteredImage.getHeight());
			//
			// ------------------------------------------------------------------------------
			int backgroundRed = ((JPatchSettings.getInstance().cBackground.getRed() * (255 - iOpacity)) & 0xFF00) << 8;
			int backgroundGreen = ((JPatchSettings.getInstance().cBackground.getGreen() * (255 - iOpacity)) & 0xFF00);
			int backgroundBlue = ((JPatchSettings.getInstance().cBackground.getBlue() * (255 - iOpacity)) & 0xFF00) >> 8;
			int backgroundColor = backgroundRed | backgroundGreen | backgroundBlue;
			
			int[] aiRGBoriginal = ((DataBufferInt) originalRGBImage.getRaster().getDataBuffer()).getData();
			int[] aiRGBfiltered = ((DataBufferInt) filteredRGBImage.getRaster().getDataBuffer()).getData();
			int size = aiRGBoriginal.length;
			for (int i = 0; i < size; i++) {
				int r = (((aiRGBoriginal[i] & 0xFF0000) * iOpacity) & 0xFF000000) >> 8;
				int g = (((aiRGBoriginal[i] & 0xFF00) * iOpacity) & 0xFF0000) >> 8;
				int b = (((aiRGBoriginal[i] & 0xFF) * iOpacity) & 0xFF00) >> 8;
				aiRGBfiltered[i] = (r | g | b) + backgroundColor;
			}
		}
	}

	public int getOpacity() {
		return iOpacity;
	}
	
	/**
	 * paint the background image on canvas
	 */
	public void paint(JPatchCanvas canvas) {
		
		/* get Graphics2D, scale, translateX and translateY from Canvas */
		Graphics2D g2 = (Graphics2D)canvas.getDrawable().getGraphics();
		ViewDefinition viewDefinition = canvas.getViewDefinition();
		float viewTranslateX = viewDefinition.getTranslateX();
		float viewTranslateY = viewDefinition.getTranslateY();
		float viewWidth = viewDefinition.getWidth();
		float viewHeight = viewDefinition.getHeight();
		float viewScale = viewDefinition.getScale() * viewWidth * 0.5f;
		float scale = fScale * viewScale;
		
		float xPos = fXPosition - fScale * 0.5f * filteredRGBImage.getWidth();
		float yPos = fYPosition - fScale * 0.5f * filteredRGBImage.getHeight();
		/* set up affine transform */
		affineTransform = new AffineTransform(scale,0,0,scale,viewWidth * 0.5f + (viewTranslateX + xPos) * viewScale,viewHeight * 0.5f + (viewTranslateY + yPos) * viewScale);
		
		/* paint image */
		g2.drawImage(filteredRGBImage,affineTransform,null);
		
		int iLeftX = (int) (viewWidth * 0.5f + (viewTranslateX + xPos) * viewScale);
		int iRightX = (int) (iLeftX + scale * getPixelWidth());
		int iTopY = (int) (viewHeight * 0.5f + (viewTranslateY + yPos) * viewScale);
		int iBottomY = (int) (iTopY + scale * getPixelHeight());
		
		g2.setColor(JPatchSettings.getInstance().cGrey);
		g2.drawRect(iLeftX,iTopY,iRightX - iLeftX,iBottomY - iTopY);
	}
	
	/**
	 * Get XML representation of rotoscope
	 *
	 * @param tabs The indent level
	 */
	public StringBuffer xml(int tabs,String view) {
		StringBuffer sbIndent = XMLutils.indent(tabs);
		StringBuffer sbLineBreak = XMLutils.lineBreak();
		StringBuffer sb = new StringBuffer();
		sb.append(sbIndent).append("<rotoscope view=\"" + view + "\">").append(sbLineBreak);
		sb.append(sbIndent).append("\t<image>").append(strFilename).append("</image>").append(sbLineBreak);
		sb.append(sbIndent).append("\t<display x=\"" + fXPosition + "\" y=\"" +fYPosition + "\" scale=\"" + fScale + "\" opacity=\"" + iOpacity +"\"/>").append(sbLineBreak);
		sb.append(sbIndent).append("</rotoscope>").append(sbLineBreak);
		return sb;
	}
}

