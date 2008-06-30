import java.awt.*;

import javax.swing.*;

public class CoC {
	public static void main(String[] args) {
		JPanel panel = new JPanel() {
			public void paint(Graphics g) {
				super.paint(g);
				long t = System.currentTimeMillis();
				if (false) {
					for (int i = 0; i < 100000; i++) {
						double r = Math.sqrt(Math.random());
						double a = Math.random() * 2 * Math.PI;
						double x = r * Math.cos(a);
						double y = r * Math.sin(a);
						int sx = (int) (200 + x * 200);
						int sy = (int) (200 + y * 200);
//						g.drawLine(sx, sy, sx, sy);
					}
				} else {
					for (int i = 0; i < 100000; i++) {
						double x = 0, y = 0;
						do {
							x = Math.random() * 2 - 1;
							y = Math.random() * 2 - 1;
						} while (x * x + y * y > 1);
						int sx = (int) (200 + x * 200);
						int sy = (int) (200 + y * 200);
//						g.drawLine(sx, sy, sx, sy);
					}
				}
				g.drawString(Long.toString(System.currentTimeMillis() - t), 0, 16);
			}
		};
		
		panel.setPreferredSize(new Dimension(400, 400));
		
		JFrame frame = new JFrame("Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		while(true) {
			panel.repaint();
		}
	}
}
