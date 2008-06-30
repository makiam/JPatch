package test;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.swing.*;

public class NaturalNeighborTest {
	static final File DIR = new File("/home/sascha/natural_neighbor/nna/");
	static final String[] CMD = new String[] { "/home/sascha/natural_neighbor/nna/nnaver", "query.dat", "test.dat" };
	static final File QUERY = new File(DIR, "query.dat");
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		NaturalNeighborTest nnt = new NaturalNeighborTest();
		int DIM = 50;
		double f = 1.0 / DIM;
		final BufferedImage img = new BufferedImage(DIM * 6, DIM * 6, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.createGraphics();
		for (int y = 0; y < DIM; y++) {
			for (int x = 0; x < DIM; x++) {
				float d = (float) Math.max(0, Math.min(nnt.test(x * f, y * f), 1));
//				float d = 0.5f;
				g.setColor(new Color(d, d, 1));
				g.fillRect(x * 4 + y, DIM + y * 4 - (int) (DIM * d * 2), 1, 1);
			}
			System.out.println(y);
		}
		JFrame frame = new JFrame("Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel() {
			public void paintComponent(Graphics g) {
				g.drawImage(img, 0, 0, null);
			}
		};
		panel.setPreferredSize(new Dimension(DIM * 6, DIM * 6));
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		
	}
	
	double test(double x, double y) throws IOException {
		FileWriter writer = new FileWriter(QUERY);
		writer.append(Double.toString(x));
		writer.append(' ');
		writer.append(Double.toString(y));
		writer.append('\n');
		writer.close();
		Process proc = Runtime.getRuntime().exec(CMD, null, DIR);
		BufferedReader in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		String line;
		double accumulate = 0;
		while ((line = in.readLine()) != null) {
//			System.out.println(line);
			String[] s = line.split("\\s+");
//			System.out.println(s.length);
			if (s.length == 5) {
				accumulate = Double.parseDouble(s[4]);
			}
		}
		return accumulate;
	}
}
