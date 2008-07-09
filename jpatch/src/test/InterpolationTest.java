package test;

import com.jpatch.entity.*;

import java.awt.*;
import java.awt.image.*;

import javax.swing.*;

public class InterpolationTest {
	
	public static void main(String[] args) {
		final Morph morph = new Morph(2, 3);
		morph.addMorphTarget(new double[] { 0.0, 0.0 }).setValue(1.0, 1.0, 1.0);
		morph.addMorphTarget(new double[] { 1.0, 0.0 }).setValue(1.0, 1.0, 1.0);
		morph.addMorphTarget(new double[] { 0.33, 0.0 }).setValue(0.0, 0.0, 0.0);
		morph.addMorphTarget(new double[] { 0.66, 0.0 });
		morph.computePreWeights();
		
//		morph.addMorphTarget(new MorphTarget(new double[] { 0.33, 0.0 }, new double[] { 0.0, 0.0, 0.0 }));
//		morph.addMorphTarget(new MorphTarget(new double[] { 0.66, 0.0 }, new double[] { 0.5, 0.5, 0.5 }));
//		morph.addMorphTarget(new MorphTarget(new double[] { 1.0, 0.0 }, new double[] { 1.0, 1.0, 1.0 }));
//		morph.addMorphTarget(new MorphTarget(new double[] { 0.5, 1.0 }, new double[] { 0.5, 0.5, 0.5 }));
//		morph.addMorphTarget(new MorphTarget(new double[] { 1.0, 1.0 }, new double[] { 1.0, 1.0, 1.0 }));
//		morph.addMorphTarget(new MorphTarget(new double[] { 0.8, 0.0 }, new double[] { 0, 0, 0 }));
//		morph.addMorphTarget(new MorphTarget(new double[] { 1, 1, 0 }, new double[] { 0, 0, 0 }));
//		morph.addMorphTarget(new MorphTarget(new double[] { 0, 1, 0 }, new double[] { 1, 1, 1 }));
//		morph.addMorphTarget(new MorphTarget(new double[] { 0.5, 0, 0 }, new double[] { 1, 1, 1 }));
//		morph.addMorphTarget(new MorphTarget(new double[] { 0.5, 1, 0 }, new double[] { 0, 0, 0 }));
		
		final double[] position = new double[2];
		final double[] result = new double[3];
		
		final int width = 400;
		final int height = 400;
		final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		double xStep = 1.0 / width;
		double yStep = 1.0 / height;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				position[0] = x * xStep;
				position[1] = y * yStep;
				morph.interpolate(position, true, result);
				int r = Math.max(0, Math.min(255, (int) (result[0] * 255)));
				int g = Math.max(0, Math.min(255, (int) (result[1] * 255)));
				int b = Math.max(0, Math.min(255, (int) (result[2] * 255)));
				int rgb = (r << 16) | (g << 8) | b;
				img.setRGB(x, y, rgb);
			}
		}
		for (int x = 0; x < width; x++) {
			position[0] = x * xStep;
			position[1] = 0.0;
			morph.interpolate(position, true, result);
			img.setRGB(x, height - 1 - ((int) (result[0] * (height - 1))), 0xff0000);
		}
		
		JFrame frame = new JFrame("Interpolation Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JComponent imageComponent = new JComponent() {
			public void paint(Graphics g) {
				g.drawImage(img, 0, 0, null);
			}
		};
		imageComponent.setPreferredSize(new Dimension(width, height));
		frame.add(imageComponent);
		frame.pack();
		frame.setVisible(true);
	}
	
	
	
	
}
