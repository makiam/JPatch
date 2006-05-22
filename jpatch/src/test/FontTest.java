package test;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class FontTest {
	
	public FontTest() {
		FontRenderContext frc = new FontRenderContext(null, false, false);
		Font font = new Font("Sans Serif", Font.PLAIN, 12);
		BufferedImage image = new BufferedImage(16, 14, BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g = image.createGraphics();
		byte[] b = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		float[] advance = new float[256];
		System.out.println("\tbyte[][] characters = new byte[][] {");
		
		for (int i = 0; i < 256; i++) {
			GlyphVector glyphVector = font.createGlyphVector(frc, new char[] { (char) i });
			GlyphMetrics glyphMetrics = glyphVector.getGlyphMetrics(0);
			advance[i] = glyphMetrics.getAdvanceX();
			g.clearRect(0, 0, 16, 14);
			g.drawString(new String(new char[] { (char) i } ), 0, 12);
			System.out.print("\t\t{");
			for (int j = 13; j > 0; System.out.print(b[j * 2] + "," + b[j-- * 2 + 1] + ","));
			System.out.println(b[0] + "," + b[1] + "},");
		}
		System.out.println("\t};");
		System.out.println("\tfloat[] advance = new float[] {");
		int k = 0;
		for (int i = 0; i < 8; i++) {
			System.out.print("\t\t");
			for (int j = 0; j < 32; j++) {
				System.out.print((int) advance[k++]);
				if (k != 256)
					System.out.print(",");
			}
			System.out.println();
		}
		System.out.println("\t};");
	}
//			float maxHeight = 0;
//			for (int i = 0, n = glyphVector.getNumGlyphs(); i < n; i++) {
//				GlyphMetrics glyphMetrics = glyphVector.getGlyphMetrics(i);
//				float height = (float) glyphMetrics.getBounds2D().getHeight();
//				if (height > maxHeight) maxHeight = height;
//			}
//			System.out.println(maxHeight);
//			
//			final Frame frame = new Frame("Font Test");
//			Rectangle r = glyphVector.getPixelBounds(frc, 0, 0);
//			final BufferedImage image = new BufferedImage(8, 12, BufferedImage.TYPE_BYTE_BINARY);
//			byte[] b = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//			System.out.println(b.length * 8+ " " + image.getWidth() * image.getHeight());
//			Canvas canvas = new Canvas() {
//				public void paint(Graphics g) {
//					g.drawImage(image, 0, 0, null);
//				}
//			};
//			canvas.setSize(image.getWidth(), image.getHeight());
//			frame.addWindowListener(new WindowAdapter() {
//				public void windowClosing(WindowEvent e) {
//					frame.dispose();
//					System.exit(0);
//				}
//			});
//			frame.add(canvas);
//			frame.pack();
//			frame.setVisible(true);
//			Graphics2D g = image.createGraphics();
//			System.out.println(glyphVector.getPixelBounds(frc, 0, 0));
//			FontMetrics fontMetrics = g.getFontMetrics(font);
//			//for (int i = 0, n = glyphVector.getNumGlyphs(); i < n; i++) {
//			g.drawGlyphVector(glyphVector, 0, fontMetrics.getAscent() - 1);
//				//g.drawString(new String(c), 0, r.height);
//			//}
//			System.out.println(fontMetrics.getAscent());
//			frame.doLayout();
//		}
//		
//	}
	
	public static void main(String[] args) {
		new FontTest();
	}
}
