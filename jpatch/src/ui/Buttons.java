package ui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import static java.awt.RenderingHints.*;

public class Buttons {
	public static final int BUTTON_HEIGHT = 21;
	public static final int BUTTON_WIDTH = 23;
	public static final int BUTTON_SIDE = 4;

	public static void main(String[] args) {
		new Buttons().test();
	}
	
	private void paintGroupButtons(BufferedImage image, boolean selected) {
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
		g.setPaint(new GradientPaint(0, 0, new Color(0x949494), 0, 24, new Color(0xc9c9c9)));
		g.draw(new RoundRectangle2D.Float(0, 0, 8 + 3 * 23 + 2, 24, 8, 8));
		g.setClip(new RoundRectangle2D.Float(0, 1, 8 + 3 * 23 + 2, 22, 8, 8));
		g.setPaint(new GradientPaint(0, 2, new Color(0xeaeaea), 0, 20, new Color(0xf9f9f9)));
		int right = 2 + 2 * BUTTON_SIDE + 2 + 3 * BUTTON_WIDTH;
		g.fillRect(1, 2, right, 20);
		GeneralPath path = new GeneralPath();
		path.moveTo(1, 22);
		path.curveTo(1, 13, 10, 13, 27, 13);
		path.lineTo(1 + 27 + 1 + 23 + 1, 13);
		path.curveTo(right - 12, 13, right - 2, 13, right - 2, 2);
		path.lineTo(right - 2, 23);
		path.lineTo(1, 23);
		path.closePath();
		g.setPaint(new GradientPaint(0, 3, new Color(0xc2c2c2), 0, 22, new Color(0xf2f2f2)));
		g.fill(path);
		g.setColor(new Color(0, 0, 0, 32));
		g.drawLine(26, 2, 26, 22);
		g.drawLine(27, 2, 27, 22);
		g.drawLine(right - 29, 2, right - 29, 22);
		g.drawLine(right - 28, 2, right - 28, 22);
		g.setClip(null);
		g.setColor(new Color(0x666666));
		g.draw(new RoundRectangle2D.Float(0, 1, 8 + 3 * 23 + 2, 22, 8, 8));
	}
	
	private void test() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(640, 480);
		final BufferedImage img = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
		paintGroupButtons(img, false);
		JComponent component = new JComponent() {
			public void paintComponent(Graphics g) {
				g.drawImage(img, 0, 0, null);
			}
		};
		frame.setBackground(new Color(0xaaaaaa));
		frame.add(component);
		frame.setVisible(true);
	}
}


