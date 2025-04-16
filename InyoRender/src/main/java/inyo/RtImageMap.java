/*
 * Created on Feb 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package inyo;

/**
 * @author dcuny
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import java.awt.*;
import java.awt.image.*;
import javax.vecmath.*;

import javax.swing.ImageIcon;

public class RtImageMap {

	
	Image image;
	int width;
	int height;
	int pixels[];
	 
	RtImageMap( String fileName ) {
		
		// load the image from the file
        Image image = Toolkit.getDefaultToolkit().getImage(fileName);
        ImageIcon icon = new ImageIcon(image);
        
        // get the height and width
        this.height = icon.getIconHeight();
        this.width = icon.getIconWidth();
        
		// allocate space for storing the pixels
		this.pixels = new int[this.width * this.height];
		
		// must be in try because it throws an InterruptedException
		try {
			// create a PixelGrabber to copy the pixels
			PixelGrabber grabber = new PixelGrabber(
				image, 0, 0,(int)width, (int)height, this.pixels, 0, (int)width);
			// copy the pixels
			grabber.grabPixels();
			
		} catch (InterruptedException e) {
			// handle error
		}
    
	 }
	
	final private Color3f getPixelColor( int x, int y ) {
		// check range
		if (x < 0) {
			x = 0;
		} else if (x >= this.width) {
			x = this.width-1;
		}

		if (y < 0) {
			y = 0;
		} else if (y >= this.height) {
			y = this.height-1;
		}
				
		// extract the color information
		int pixel = pixels[x+(width*y)];
	    int alpha = (pixel >> 24) & 0xff;
	    int red   = (pixel >> 16) & 0xff;
	    int green = (pixel >>  8) & 0xff;
	    int blue  = (pixel      ) & 0xff;
 
		return new Color3f( (float)red/255, (float)green/255, (float)blue/255 );
	}
		
	final Color3f getMapColor( double x, boolean left, double y, boolean top ) {
		// change range from [1:.4487] to [0:1]

		
		int halfWide = this.width / 2;
		int halfHigh = this.height / 2;

		// scale from 0..
		x -= 1.0;
		y -= 1.0;
		
		x *= halfWide;
		y *= halfHigh;
		
		
		
		if (!left) {
			x += halfWide;
		} else {
			x = halfWide - x;
		}		
		
		if (top) {
			y += halfHigh;
		} else {
			y = halfWide - y;
		}
		
		// get pixel
		return this.getPixelColor((int)x, (int)y);
	}
}
