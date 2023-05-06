// Copyright (c) 2004 David Cuny
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
// associated documentation files (the "Software"), to deal in the Software without restriction, including 
// without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
// sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
// subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in all copies or substantial 
// portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT 
// NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
// IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
// SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


// rtCanvas
// canvas for raytracer

package inyo;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;


/**
 * 
 * @author David Cuny
 *
 * This is a graphics buffer for rendering into. It only supports minimal
 * operations (setting pixels). Pixels can be flagged for debugging - for
 * example, when an irradiance sample is taken at a point, the <b>flag</b>
 * attribute for that pixel can be set.
 * 
 * Pixels are <b>double</b> values, which allows them to hold high values without
 * necessarily clipping.
 * 
 * In general, the values <b>{x, y}</b> indicate canvas cooridinates where <b>{0, 0}</b>
 * is the center of the canvas, and <b>{a, b}</b} refer to the actual array indices.
 * 
 */
class RtCanvas {

	public int high, wide, halfHigh, halfWide;
	BufferedImage image;
	int frameBuffer[];
	
	static final int CARTESIAN = 0;	// (0,0) is located at center of canvas
	static final int ABSOLUTE = 1;	// (0,0) is located at top left of canvas
	
	/**
	 * Create a simple canvas of the give size. The default color is set to 
	 * <b>{ -1, -1, -1 }<b/>.
	 * 
	 * @param theWide	Canvas width
	 * @param theHigh	Canvas height
	 */
	public RtCanvas( int wide, int high ) {

		// FIXME: make sure these are divisble
		high++;
		wide++;
		
		// set metrics
		this.high = high;
		this.wide = wide;
		this.halfHigh = high/2;
		this.halfWide = wide/2;

		// create an empty image 
		image = new BufferedImage(wide, high, BufferedImage.TYPE_INT_RGB); 
	 				
		// load the image into the framebuffer
	    frameBuffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		
	}
	
	public RtCanvas( RtCanvas canvas ) {
		// copy prior metrics
		this.high = canvas.high;
		this.wide = canvas.wide;
		this.halfHigh = canvas.halfHigh;
		this.halfWide = canvas.halfWide;

		// get a writeable raster of the original image
		WritableRaster wr = canvas.getImage().copyData(null);

		// create a new image to hold the copy
		image = new BufferedImage(wide, high, BufferedImage.TYPE_INT_RGB); 

		// copy the data
		image.setData(wr);
		
		// load the image into the framebuffer
	    frameBuffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

	}
	
	/**
	 * Return integer with red, green and blue values packed into a 24 bit integer
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	int rgb( int red, int green, int blue ) {
		return 0xff000000 | (red & 0xff) << 16 | (green & 0xff) << 8 | (blue & 0xff);
	}
	
	/**
	 * Fill the canvas with the default color.
	 * 
	 * @param fillRed		Default red amount
	 * @param fillGreen		Default green amount
	 * @param fillBlue		Default blue amount
	 */
	final void fill( int red, int green, int blue ) {
		// calculate pixel color
		int fillColor = rgb( red, green, blue );
		
		// number of pixels
		int total = this.wide * this.high;
		
		// iterate over all the pixels
		for (int i = 0; i < total; i++) {
			// set the pixel color
			frameBuffer[i] = fillColor;
		}
	}

	/**
	 * Set the <b>pixelPos</b> to the absolute <b>x</b>, <b>y</b> position.
	 * @param x
	 * @param y
	 */
	final int getIndex( int originType, int x, int y) {
		if (originType == RtCanvas.ABSOLUTE) {
			// (0,0) is at upper left corner
			return x + (y * this.wide);
		} else {
			// (0,0) is at center of image
			// offset to center
			int pixelX = this.halfWide + x;
			int pixelY = -y + this.halfHigh;
			
			// calculate index into data
			return pixelX + (pixelY * this.wide);
		}
	}

	
	final void setPixel( int originType, int x, int y, int red, int green, int blue ) {
		// offset into array
		int index = getIndex(originType, x, y);
		
		// set the pixel				
		frameBuffer[index] = 0xff000000 | (red & 0xff) << 16 | (green & 0xff) << 8 | (blue & 0xff);
	}
	

	/**
	 * Converts <b>red</b>, <b>green</b>, <b>blue</b> values from <b>doubles</b> in the
	 * range of (0..1) to bytes in the range of (0..255) and sets the pixel.
	 * @param x
	 * @param y
	 * @param red
	 * @param green
	 * @param blue
	 */
	final void setPixelScaled( int originType, int x, int y, double red, double green, double blue ) {
		// scale values from (0..1) to (0..255)
		int r = clip(0, 255, (int)(red*255f));
		int g = clip(0, 255, (int)(green*255f));
		int b = clip(0, 255, (int)(blue*255f));

		setPixel(originType, x, y, r, g, b);
	}
	
	/**
	 * Clip an integer between <b>min</b> and <b>max</b>.
	 * @param min Lowest possible value.
	 * @param max Highest possible value.
	 * @param value Value to be clipped.
	 * @return Value clipped between <b>min</b> and <b>max</b>
	 */
	final int clip( int min, int max, int value ) {
		if (value < min) return 0;
		if (value > max) return max;
		return value;
	}

	/**
	 * Return a packed pixel value from the buffer
	 * @param originType
	 * @param x
	 * @param y
	 * @return
	 */
	final int getRGB( int originType, int x, int y ) {
		return this.frameBuffer[ getIndex(originType,x,y) ];
	}
	
	/**
	 * Return the red byte from the packed RGB value.
	 * @param rgb
	 * @return
	 */
	final int redByte( int rgb ) {
		return (rgb >> 16) & 0xff;
	}

	/**
	 * Return the green byte from the packed RGB value
	 * @param rgb
	 * @return
	 */
	final int greenByte( int rgb ) {
		return (rgb >> 8) & 0xff;
	}

	/**
	 * Return the blue byte from the packed RGB value
	 * @param rgb
	 * @return
	 */
	final int blueByte( int rgb ) {
		return rgb & 0xff;
	}
	

	final boolean outOfRange( int originType, int x, int y ) {
		if (originType == RtCanvas.ABSOLUTE) {
			// check against absolute values
			if (x < 0 || y < 0 || x >= this.wide || y >= this.high) {
				return true;
			}
		} else {
			// check against cartesian values
			if (x < -this.halfWide  || y < -this.halfHigh || x >= this.halfWide || y >= this.halfHigh) {
				return true;
			}
		}
		return false;
	}

	
	final void addPixel( int originType, int x, int y, int red, int green, int blue ) {
		// check range
		if (outOfRange( originType, x, y )) {
			return;
		}

		// index into data
		int index = getIndex(originType, x,y);
		
		// get the data
		int pixel = this.frameBuffer[index];
			
		// add new values to old values
		red = clip(0, 255, red+redByte(pixel));
		green = clip(0, 255, green+greenByte(pixel));
		blue = clip(0, 255, blue+blueByte(pixel));
			
		// set the pixel				
		frameBuffer[index] = 0xff000000 | (red & 0xff) << 16 | (green & 0xff) << 8 | (blue & 0xff);
	}

		
	/**
	 * Return the red contribution at <b>{x, y}</b>
	 * @param x
	 * @param y
	 * @return
	 */
	final double getRed( int originType, int x, int y ) {
		// get the red component
		return redByte(this.frameBuffer[getIndex(originType,x,y)]);
	}
	
	/**
	 * Return the green contribution at <b>{x, y}</b>
	 * @param x
	 * @param y
	 * @return
	 */
	final double getGreen( int originType, int x, int y ) {
		// get the green component
		return greenByte(this.frameBuffer[getIndex(originType,x,y)]);
	}

	/**
	 * Return the blue contribution at <b>{x, y}</b>
	 * @param x
	 * @param y
	 * @return
	 */
	final double getBlue( int originType, int x, int y ) {
		// get the red component
		return blueByte(this.frameBuffer[getIndex(originType,x,y)]);
	}
	

	/**
	 * Return true if the color difference between the source and destination
	 * pixels do not exceed the allowable amount.
	 * 
	 * @param srcX		Source pixel x location
	 * @param srcY		Source pixel y location
	 * @param dstX		Destination pixel x location
	 * @param dstY		Destination pixel y location
	 * @param allowable	Allowable amount of difference between pixels
	 * @return			True if pixel colors do not differ more than allowed
	 */
	final boolean sameColor( int originType, int srcX, int srcY, int dstX, int dstY, double allowable ) {

		// offset to pixel position
		int srcPixel = this.frameBuffer[ getIndex(originType, srcX, srcY) ];
		int dstPixel = this.frameBuffer[ getIndex(originType, srcX, srcY) ];

		// get difference
		double redDiff = Math.abs( redByte(srcPixel) - redByte(dstPixel) );
		double greenDiff = Math.abs( greenByte(srcPixel) - greenByte(dstPixel) );
		double blueDiff = Math.abs( blueByte(srcPixel) - blueByte(dstPixel) );

        // true if combined difference is greater than error
        return (redDiff < allowable 
        && greenDiff < allowable 
        && blueDiff < allowable
        && (redDiff+greenDiff+blueDiff)/3 < allowable);
	}
	
	/**
	 * Return a linear interpolated value between <b>a</b> and <b>b</b>, where
	 * <b>t</t> is between 0 and 1. When <b>t == 1</b>, <b>a</b> is returned; when
	 * <b>t == 1</b>, <b>b</b> is returned.
	 * 	 
	 * @param a		Value to return when t == 0.
	 * @param b		Value to return when t == 1
	 * @param t		Interpolation amount between a and b
	 * @return		Interpolated value
	 */
	final double linear( double a, double b, double t ) {
		return a + ((b-a)*t);
	}
	
	
	/**
	 * Perform a bilinear interpolation. First, a starting value is interpolated
	 * between a and b. Next, and ending value is interpolated based on c and d.
	 * Finally, value is interpolated between the start and end value.
	 * @param a	
	 * @param b
	 * @param c
	 * @param d
	 * @param t1
	 * @param t2
	 * @return
	 */
	final int bilinear(int a, int b, int c, int d, double t1, double t2) {
		// interpolate horizontally
		double a1 = linear( a, b, t1 );
		double b1 = linear( c, d, t1 );
		
		// interpolate vertically
		return (int)linear( a1, b1, t2 );
	}
		
	/**
	 * Interpolate bilinearly between the pixels at <b>{x, y}</b> and <b>{x+gap, y+gap}</b>.
	 * @param x
	 * @param y
	 * @param gap
	 */
	final void interpolate( int originType, int x, int y, int gap) {

		// offset to center
		x += this.halfWide;
		y = -y + this.halfHigh;
		
		int x1 = x + gap;
		int y1 = y + gap;
		
		// get the colors at each corner
		int pixel = this.frameBuffer[ getIndex(originType,x,y) ];
		int red00 = redByte(pixel);
		int green00 = greenByte(pixel);
		int blue00 = blueByte(pixel);

		pixel = this.frameBuffer[ getIndex(originType,x1,y) ];
		int red10 = redByte(pixel);
		int green10 = greenByte(pixel);
		int blue10 = blueByte(pixel);

		pixel = this.frameBuffer[ getIndex(originType,x,y1) ];
		int red01 = redByte(pixel);
		int green01 = greenByte(pixel);
		int blue01 = blueByte(pixel);
		
		pixel = this.frameBuffer[ getIndex(originType,x1,y1) ];
		int red11 = redByte(pixel);
		int green11 = greenByte(pixel);
		int blue11 = blueByte(pixel);

		// interate through y
		double delta = 1.0/(double)(gap+1);

		double dy = 0.0;
		for ( int y0 = y; y0 < y1; y0++ ) {
			double dx = 0.0;
			for ( int x0 = x; x0 < x1; x0++ ) {
				// calculate index
				int index = getIndex(originType,x0,y0);
				
				// bilinearly interpolate rgb value				
				int newRed =  bilinear( red00, red10, red01, red11, dx, dy );
				int newGreen =  bilinear( green00, green10, green01, green11, dx, dy );
				int newBlue =  bilinear( blue00, blue10, blue01, blue11, dx, dy );
				
				// update value
				this.frameBuffer[index] = rgb(newRed, newGreen, newBlue);
				
				// increment delta x
				dx += delta;
				
			}
			// increment delta y
			dy += delta;
		}
	}

	
	/**
	 * Return a <b>BufferedImage</b> based on the information in the canvas.
	 * @return	Returns an <b>Image</b> of the canvas.
	 */
	final BufferedImage getImage() {
		return this.image;
	}
	

	/**
	 * Returns true if the pixel at <b>{a, b}</b> varied too much from its neighbor.
	 * This is used to determine if the pixel should be supersampled.
	 * 
	 * @param a
	 * @param b
	 * @param tolerance
	 * @return
	 */
	boolean needsResampling( int originType, int a, int b, double tolerance ) {		

		// check range
		if (outOfRange(originType, a, b)) {
			return false;
		}
		
		// get the colors being tested
		int srcPixel = frameBuffer[ getIndex(originType,a,b)];
		int srcRed =  redByte(srcPixel);
		int srcGreen = greenByte(srcPixel);
		int srcBlue= blueByte(srcPixel);
		
		// compare with the pixels around it
		for (int db = -1; db <= 1; db++ ) {
            int bb = b + db;

			for (int da = -1; da <= 1; da++ ) {
				
				// get position of neighbor
				int aa = a + da;
				
				// don't test against itself
				if ((aa == a) && (bb == b)) {
					// ignore
				// don't fall off edge
				} else if (outOfRange(originType, aa, bb)) {
					// ignore
				} else {
					int dstPixel = frameBuffer[ getIndex(originType,aa,bb)];
					int dstRed = redByte(dstPixel);
					int dstGreen = greenByte(dstPixel);
					int dstBlue = blueByte(dstPixel);
					if (Math.abs( redByte(dstPixel) - srcRed ) > tolerance ) return true;
					if (Math.abs( greenByte(dstPixel) - srcGreen ) > tolerance ) return true;
					if (Math.abs( blueByte(dstPixel) - srcBlue ) > tolerance ) return true;
				}
			}
		}
		
		// no difference
		return false;
		
	}
	
	/**
	 * Replace the pixel at (a,b) with an averaged version. This is used as a cheaper
	 * alternative to oversampling.
	 * @param originType
	 * @param source
	 * @param a
	 * @param b
	 */
	void averagePixel(int originType, RtCanvas source, int a, int b ) {
				
		// check range
		if (outOfRange(originType, a, b)) {
			return;
		}
		
		// 3x3 grid weights
		final int weight[] = {1,4,2,2,8,4,2,4,2};
		final int aOffset[] = { -1,0,1,-1,0,1,-1,0,1};
		final int bOffset[] = { -1,-1,-1,0,0,0,1,1,1};
		
		int sumWeights = 0;
		int pixel = 0;
		int red = 0;
		int green = 0;
		int blue = 0;
		
		// this is a 3x3 grid
		for (int i = 0; i < 9; i++) {
			// get position in grid
			int aa = a + aOffset[i];
			int bb = b + bOffset[i];
			// in range?
			if (!outOfRange(originType,aa,bb)) {
				pixel = source.frameBuffer[ getIndex(originType,aa,bb) ];
				
				// accumulate
				red += redByte(pixel) * weight[i];
				green += greenByte(pixel) * weight[i];
				blue += blueByte(pixel) * weight[i];
				sumWeights += weight[i];
			}
		}
		
		// divide by total weights
		red /= sumWeights;
		green /= sumWeights;
		blue /= sumWeights;
		
		// update pixel in destination
		this.setPixel( originType, a, b, (int)red, (int)green, (int)blue );
	}
}