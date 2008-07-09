package javaview_test;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

public class BilinearTest {
	private List<BilinearPatch> patches = new ArrayList<BilinearPatch>();
	
	BilinearTest() {
		patches.add(new BilinearPatch(0, 0, 1, 1, new double[] {0, 0, 0}, new double[] {1, 1, 1}, new double[] {1, 1, 1}, new double[] {0, 0, 0}));
	}
	
	private final void draw(Image img) {
		Graphics g = img.getGraphics();
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		double[] vector = new double[3];
		for (int y = 0; y < h; y++) {
			double t = (double) y / (h - 1);
			for (int x = 0; x < w; x++) {
				double s = (double) x / (w - 1);
				interpolate(s, t, vector);
				g.setColor(new Color((float) vector[0], (float) vector[1], (float) vector[2]));
				g.fillRect(x, y, 1, 1);
			}
		}
	}
	
	private final void add(double s, double t, double[] vector) {
		List<BilinearPatch> newPatches = new ArrayList<BilinearPatch>();
		List<BilinearPatch> oldPatches = new ArrayList<BilinearPatch>();
		for (BilinearPatch patch : patches) {
			if (patch.contains(s, t)) {
				newPatches.addAll(patch.split(s, t, vector));
				oldPatches.add(patch);
			}
		}
		patches.removeAll(oldPatches);
		patches.addAll(newPatches);
	}
	
	private final double[] interpolate(double s, double t, double[] vector) {
		for (BilinearPatch patch : patches) {
			if (patch.contains(s, t)) {
				return patch.interpolate(s, t, vector);
			}
		}
		throw new RuntimeException();
	}
	
	public static void main(String[] args) {
		final BilinearTest test = new BilinearTest();

//		for (int i = 0; i < 100; i++) {
//			test.add(Math.random(), Math.random(), new double[] { Math.random(), Math.random(), Math.random() });
//		}
		
		for (int i = 0; i < 10; i++) {
			test.add(0.5 + 0.4 * Math.cos(i * Math.PI / 5), 0.5 + 0.4 * Math.sin(i * Math.PI / 5), new double[] { Math.random(), Math.random(), Math.random() });
			if (i == 3) {
				break;
			}
		}
			
//		test.add(0.25, 0.25, new double[] { Math.random(), Math.random(), Math.random() });
//		test.add(0.25, 0.50, new double[] { Math.random(), Math.random(), Math.random() });
//		test.add(0.25, 0.75, new double[] { Math.random(), Math.random(), Math.random() });
//		test.add(0.50, 0.25, new double[] { Math.random(), Math.random(), Math.random() });
//		test.add(0.50, 0.50, new double[] { Math.random(), Math.random(), Math.random() });
//		test.add(0.50, 0.75, new double[] { Math.random(), Math.random(), Math.random() });
//		test.add(0.75, 0.25, new double[] { Math.random(), Math.random(), Math.random() });
//		test.add(0.75, 0.50, new double[] { Math.random(), Math.random(), Math.random() });
//		test.add(0.75, 0.75, new double[] { Math.random(), Math.random(), Math.random() });
//
//		test.add(0.6, 0.6, new double[] { Math.random(), Math.random(), Math.random() });
//		test.add(0.6, 0.2, new double[] { Math.random(), Math.random(), Math.random() });
		
		final Image img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
		test.draw(img);
		final JPanel panel = new JPanel() {
			public void paintComponent(Graphics g) {
				g.drawImage(img, 0, 0, null);
				for (BilinearPatch patch : test.patches) {
					g.drawRect(
							(int) (patch.s0 * 600),
							(int) (patch.t0 * 600),
							(int) ((patch.s1 - patch.s0) * 600),
							(int) ((patch.t1 - patch.t0) * 600)
					);
				}
			}
		};
		panel.setPreferredSize(new Dimension(600, 600));
		JFrame frame = new JFrame("Bilinear test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
}
