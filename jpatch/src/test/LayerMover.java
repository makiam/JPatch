package test;
/**
 * 
 */

/**
 * @author sascha
 *
 */

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.text.*;
import javax.imageio.*;
import javax.swing.*;

import jpatch.entity.MotionCurve2;
import jpatch.entity.MotionKey2;

public class LayerMover {
	private int width = 384;
	private int height = 216;
	private BufferedImage layer1;
	private BufferedImage layer2;
	private Color back1 = new Color(0.80f, 0.90f, 1.00f);
	private Color back2 = new Color(0.40f, 0.45f, 1.00f);
	public static void main(String[] args) throws Exception {
		new LayerMover();
	}
	
	public LayerMover() throws Exception {
		layer1 = ImageIO.read(new File("/home/sascha/foreground-clouds.png"));
		layer2 = ImageIO.read(new File("/home/sascha/background-clouds.png"));
		final BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final BufferedImage aBuf = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) aBuf.getGraphics();
//		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		AffineTransform t = new AffineTransform();
		DecimalFormat df = new DecimalFormat("000000");
		JFrame frame = new JFrame();
		frame.setSize(width * 2, height);
		JPanel panel = new JPanel() {
			public void paintComponent(Graphics g) {
				g.drawImage(im, 0, 0, null);
				g.drawImage(aBuf, 384, 0, null);
			}
		};
		frame.add(panel);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		float p = 0;
		int shift = 3;
		int blur = 1 << shift;
		int[] a = ((DataBufferInt) aBuf.getRaster().getDataBuffer()).getData();
		int[] img = ((DataBufferInt) im.getRaster().getDataBuffer()).getData();
		int[] red = new int[img.length];
		int[] green = new int[img.length];
		int[] blue = new int[img.length];
		double theta;
		MotionCurve2.Float mc = MotionCurve2.createSizeCurve();
		mc.addKey(new MotionKey2.Float(200 * blur, (float) Math.PI));
		mc.addKey(new MotionKey2.Float(240 * blur, -10.0f / 180.0f * (float) Math.PI));
		mc.addKey(new MotionKey2.Float(248 * blur, -10.0f / 180.0f * (float) Math.PI));
		mc.addKey(new MotionKey2.Float(258 * blur, 5.0f / 180.0f * (float) Math.PI));
		mc.addKey(new MotionKey2.Float(264 * blur, 0));
		
		for (int i = 0 * blur; i < 350 * blur; i++) {
			float s = 1.0f / blur;
			if (i >= 200 * blur) {
//				float f = (225.0f * blur - i) / 25;
//				System.out.println(f);
//				
//				if (theta > Math.PI / 2)
//					ta -= 0.003f;
//				else if (theta > 0)
//					ta += 0.003f;
//				//ta = ((float) i - 200 * blur) / 50 / blur * Math.PI / 50 / blur;
//				//theta -= ((float) i - 200 * blur) / 50 / blur * Math.PI;
//				theta += ta;
				s = (1 - ((float) i - 200 * blur) / 75 / blur) / blur;
			}
			theta = mc.getFloatAt(i);
			if (s < 0.0f / blur)
				s = 0.0f / blur;
//			if (i >= 225 * blur) {
//				ta = (250 * blur - (float) i) / 50 / blur * Math.PI / 50 / blur;
//				//theta -= ((float) i - 200 * blur) / 50 / blur * Math.PI;
//				theta += ta * 2;
////				s = (1 - ((float) i - 225 * blur) / 75 / blur) / blur;
//			}
//			System.out.println("\t" + ta + "\t" + (int) (theta * 180 / Math.PI));
//			if (i > 250 * blur) {
//				theta = 0;
//				s = (1 - ((float) i - 200 * blur) / 75 / blur) / blur;
//			}
//			if (s < 0)
//				s = 0;
			t.setTransform(
					Math.cos(theta), -Math.sin(theta),
					Math.sin(theta), Math.cos(theta),
					width / 2, height / 2
			);
//			System.out.println(t);
//			t.setToRotation((double) i / 500 * Math.PI, -width / 2, -height / 2);
			g.setTransform(t);
//			g.setPaintMode();
			drawBackground(g);
//			drawLayer(g, layer2, (p * 2.1f) % layer2.getHeight());
//			drawLayer(g, layer1, (p * 47.3f) % layer1.getHeight());
			drawLayer(g, layer2, (p * 2.1f) % layer2.getHeight());
			drawLayer(g, layer1, (p * 9.3f) % layer1.getHeight());
			
//			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.125f));
//			for (int j = 1; j < 8; j++) {
////				drawBackground(g);
//				float pp = p - s * (float) j / 8;
//				System.out.println(pp);
//				drawLayer(g, layer2, (pp * 2.1f) % layer2.getHeight());
//				drawLayer(g, layer1, (pp * 7.3f) % layer1.getHeight());
//			}
			
			
			
			for (int j = 0; j < img.length; j++) {
				red[j] += a[j] & 0x00ff0000;
				green[j] += a[j] & 0x0000ff00;
				blue[j] += a[j] & 0x000000ff;
//				img[j] = 0x0000ff00; //a[j];
//				System.out.println(j);
			}
			if (i % blur == (blur - 1)) {
				for (int j = 0; j < img.length; j++)
					img[j] = (red[j] >> shift) & 0x00ff0000 | (green[j] >> shift) & 0x0000ff00 | (blue[j] >> shift) & 0x000000ff;
			}
			panel.repaint();
			if (i % blur == (blur - 1)) {
				System.out.println(i / blur);
				ImageIO.write(im, "png", (File) new File("/home/sascha/test" + df.format(i / blur) + ".png"));
			}
			if (i % blur == (blur - 1)) {
				for (int j = 0; j < img.length; j++)
					red[j] = green[j] = blue[j] = 0;
			}
			p += s;
		}
	}
	
	void drawBackground(Graphics2D g) {
		GradientPaint gradient = new GradientPaint(0, -240, back2, 0, 240, back1);
		g.setPaint(gradient);
		g.fill(new Rectangle2D.Float(-240, -240, 480, 480));
	}

	void drawLayer(Graphics2D g, BufferedImage layer, float offset) {
		AffineTransform t = new AffineTransform();
		t.setTransform(
				1, 0,
				0, 1,
				-240, offset % layer.getHeight() - 240
		);
		g.drawImage(layer, t, null);
		t.setTransform(
				1, 0,
				0, 1,
				-240, offset % layer.getHeight() - layer.getHeight() - 240
		);
		g.drawImage(layer, t, null);
	}
}
