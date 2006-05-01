package jpatch.auxilary;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

/**
 * @author David Cuny
 *
 * License: Public Domain 
 */


public final class ReadTiff {
	
	private int buffer[];				// buffer holding the file
	private int bufferAt = 0;			// position in buffer
	
	private boolean lsb = false;			// if true, data is in least significant byte format
	private int imageLength = 0;			// how high is the image?
	private int imageWidth = 0;			// how wide is the image?
	private int stripOffsets;			// address of stripOffsets table
	private int stripOffsetsTable[];		// table with pointers to the strips
	private int stripByteCounts;			// address of stripByteCounts table
	private int stripByteCountsTable[];		// table holding byte count of each strip
	private int rowsPerStrip = 0;			// how many rows are stored in one strip?
	private int strips = 0;				// number of strips
	
	// SL: changed constants declaration from private to public

	// data types
	public final static int BYTE			= 1;
	public final static int ASCII			= 2;
	public final static int SHORT			= 3;
	public final static int LONG 			= 4;
	public final static int RATIONAL		= 5;

	// lsb and msb tags
	public final static int LSB_TAG		= 0x4949;
	public final static int MSB_TAG		= 0x4d4d;
	
	// tag types
	public final static int NEW_SUBFILE_TYPE	= 0xFE;
	public final static int SUBFILE_TYPE		= 0xFF;
	public final static int IMAGE_WIDTH		= 0x100;
	public final static int IMAGE_LENGTH		= 0x101;
	public final static int BITS_PER_SAMPLE	= 0x102;
	public final static int COMPRESSION 		= 0x103;
	public final static int UNCOMPRESSED		= 1;
	public final static int CCITT_1D		= 2;
	public final static int GROUP_3_FAX		= 3;
	public final static int GROUP_4_FAX		= 4;
	public final static int LZW			= 5;
	public final static int JPEG			= 6;
	public final static int PACKBITS		= 32773;
	public final static int PHOTOMETRIC		= 0x106;
	public final static int WHITEISZERO		= 0;
	public final static int BLACKISZERO		= 1;
	public final static int RGB			= 2;
	public final static int RGB_PALETTE		= 3;
	public final static int TRANSPARENCY_MASK	= 4;
	public final static int CYMK			= 5;
	public final static int YCBCR			= 6;
	public final static int CIELAB			= 8;
	public final static int THRESHOLDING		= 0x107;
	public final static int CELL_WIDTH		= 0x108;
	public final static int CELL_LENGTH		= 0x109;
	public final static int FILL_ORDER		= 0x10a;
	public final static int DOCUMENT_NAME		= 0x10d;
	public final static int IMAGE_DESCRIPTION	= 0x10e;
	public final static int MAKE			= 0x10f;
	public final static int MODEL			= 0x110;
	public final static int STRIP_OFFSETS		= 0x111;
	public final static int ORIENTATION		= 0x112;
	public final static int SAMPLES_PER_PIXEL	= 0x115;
	public final static int ROWS_PER_STRIP		= 0x116;
	public final static int STRIP_BYTE_COUNTS	= 0x117;
	public final static int MIN_SAMPLE_VALUE	= 0x118;
	public final static int MAX_SAMPLE_VALUE	= 0x119;
	public final static int X_RESOLUTION		= 0x11a;
	public final static int Y_RESOLUTION		= 0x11b;
	public final static int PLANAR_CONFIGURATION	= 0x11c;
	public final static int PAGE_NAME		= 0x11d;
	public final static int X_POSITION		= 0x11e;
	public final static int Y_POSITION		= 0x11f;
	public final static int FREE_OFFSETS		= 0x120;
	public final static int FREE_BYTE_COUNTS	= 0x121;
	public final static int GRAY_RESPONSE_UNIT	= 0x122;
	public final static int GRAY_RESPONSE_CURVE	= 0x123;
	public final static int T4_OPTIONS		= 0x124;
	public final static int T6_OPTIONS		= 0x125;
	public final static int RESOLUTION_UNIT	= 0x128;
	public final static int PAGE_NUMBER		= 0x129;
	public final static int TRANSFER_FUNCTION	= 0x12d;
	public final static int SOFTWARE		= 0x131;	// ascii
	public final static int DATE_TIME		= 0x132;	// ascii
	public final static int ARTIST			= 0x133;	// ascii
	public final static int PREDICTOR		= 0x13d;
	public final static int COLOR_MAP		= 0x140;


	//public void main(String args[]) {
	//	
	//	// the file name
	//	String fileName = new String("aqsis.tif");
	//	
	//	try {
	//		// read the file into the buffer
	//		readFileToBuffer( fileName );
	//	
	//		// read the header and directory entries
	//		readHeader();
	//		readDirEntries();
	//		
	//		// read the strip tables
	//		readStripOffsets();
	//		readStripByteCounts();
	//		
	//		// create an image based on the data in the strips
	//		Image image = createImage(); 
	//		
	//		// write out the result
	//		File file = new File("test.png");
	//		ImageIO.write((RenderedImage) image, "png", file);
	//	
	//		System.out.println("Test file 'test.png' written" );
	//					
	//		// release the file
	//		file = null;
	//		
	//	} catch (Exception e) {
	//		System.out.println("Error: " + e.getMessage() );
	//	}
	//		
	//}

	public BufferedImage loadImage(File file) throws Exception {
		readFileToBuffer(file);
		readHeader();
		readDirEntries();
		readStripOffsets();
		readStripByteCounts();
		return createImage();
	}
	
	private BufferedImage createImage() {
	
		// create an RGB buffer to hold the image
	    BufferedImage image = new BufferedImage(imageWidth, imageLength, BufferedImage.TYPE_INT_RGB);
	    
	    // create a graphics context to work with 
	    Graphics g = image.getGraphics();
		
		// position in the Image buffer
		int y = 0;
		int x = 0;
		
		// iterate through the strips
		for (int i = 0; i < strips; i++) {
		
			// reset to start of the current line
			x = 0;
		
			// jump to the address of the strip
			int at = stripOffsetsTable[i];

			// end position
			int end = at + stripByteCountsTable[i] + 1;

			// iterate through the bytes three at a time
			for (int j = at; j < end; j += 3 ) {

				// set the pixel to the values in the buffer
				g.setColor( new Color( buffer[j], buffer[j+1], buffer[j+2] ) );
			
				// set the pixel
				g.drawRect( x, y, 1, 1 );
			
				// move to the next pixel
				x++;
			
				// end of the column?
				if (x >= imageWidth) {
					// move down to the next row
					x = 0;
					y++;
					
					// end of data?
					if (y > imageLength) {
						break;
					}
				}
			} 
		}

		return image;
	}

	
//	private void readFileToBuffer( String fileName ) throws Exception {
//		readFileToBuffer(new File(fileName));
//	}
	
	private void readFileToBuffer( File file ) throws Exception {
		try {
			// get the file length
			int length = (int)file.length();
			
			// allocate space in the buffer
			buffer = new int[length];
		
			// open the file
			FileInputStream fileInputStream = new FileInputStream(file);
			
			// load the file into the buffer
			// don't use the native read method, because it returns signed bytes (argh!)
			for (int i = 0; i < length; i++) {
				buffer[i] = fileInputStream.read();
			}
			
			// close the file
			fileInputStream.close();
		
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Read the file header, and jump to the directory entries section.
	 */
	private void readHeader() throws Exception {
		
		// read the first byte
		switch (readWord()) {			
			case LSB_TAG:
				lsb = true;
				break;
			case MSB_TAG:
				lsb = false;
				break;
			default:			
			throw(new Exception("Error in header LSB/MSB flag"));
		}
				
		// version '42'
		if (readWord() != 0x2a ) {
			throw(new Exception("Error in version number"));
		}
		
		// jump to the location of the directory entries
		bufferAt = readDouble();
	}
	

	/**
	 * Read the directory entry tags and values
	 */
	private void readDirEntries () throws Exception {
		int dirEntries = readWord();
		
		// read each entry
		for (int i = 0; i < dirEntries; i++) {
			int tag = readWord();
			int fieldType = readWord();
			int fieldCount = readDouble();
			int valueOffset = readDouble();
			
			// interpret the tag
			switch (tag) {
				case IMAGE_WIDTH:
					imageWidth = valueOffset;
					break;

				case IMAGE_LENGTH:
					imageLength = valueOffset;
					break;
							
				case COMPRESSION:
					// only supports Uncompressed
					if (valueOffset != UNCOMPRESSED) {						
						throw(new Exception("File is compressed"));
					}
					break;

				case PHOTOMETRIC:
					// only supports RGB
					if (valueOffset != RGB) {						
						throw(new Exception("File does not use RGB format"));
					}
					break;

				case STRIP_OFFSETS:
					stripOffsets = valueOffset;
					break;						
					
				case ROWS_PER_STRIP:
					rowsPerStrip = valueOffset;
					break;

				case STRIP_BYTE_COUNTS:
					stripByteCounts = valueOffset;
					break;
										
				default:
			}
		}

		// calculate the number of strips
		strips = imageLength / rowsPerStrip;
				
	}

	private void readStripOffsets() throws Exception {
				
		// allocate space for the strip addresses
		stripOffsetsTable = new int[strips];
		
		// jump to strip addresses table
		bufferAt = stripOffsets;
		
		// read the strip addresses
		for (int i = 0; i < strips; i++) {
			stripOffsetsTable[i] = readDouble();
		}
	}
	
	private void readStripByteCounts() throws Exception {
		
		// allocate space for the strip byte counts
		stripByteCountsTable = new int[strips];

		// jump to strip addresses table
		bufferAt = stripByteCounts; 

		// read the strip addresses
		for (int i = 0; i < strips; i++) {
			stripByteCountsTable[i] = readDouble();
		}
	}

	
	private int readByte() {
		return buffer[bufferAt++];
	}
	
	private int readWord() {
		// read a 2 byte word
		int result = 0;
		if (lsb) {
			// low byte, then high byte
			result = buffer[bufferAt] | (buffer[bufferAt+1] << 8);				
		} else {
			// high byte, then low byte
			result = buffer[bufferAt+1] | (buffer[bufferAt] << 8);
		}
		
		// move buffer ahead two bytes
		bufferAt += 2;
		
		return result;
	}

	private int readDouble() {
		// read a 4 byte word
		int result = 0;

		if (lsb) {
			// low to high bytes
			result = buffer[bufferAt] | (buffer[bufferAt+1] << 8) | (buffer[bufferAt+2] << 16) | (buffer[bufferAt+3] << 24);
		} else {
			// high, then low
			result = buffer[bufferAt+3] | (buffer[bufferAt+2] << 8) | (buffer[bufferAt+1] << 16) | (buffer[bufferAt] << 24);
		}
		
		// move buffer ahead four bytes
		bufferAt += 4;
		
		return result;
	}

	
}
