package test;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.*;

public class StitchTest {

	int[] pairLevels = new int[] { 0, 1, 2, 3 };
	int[][][] rim0 = new int[7][][];					//[level][side][index] outer grid rim
	int[][][] rim1 = new int[7][][];					//[level][side][index] inner grid rim
	int[][][][] rimTriangles = new int[7][7][][]; 		// [thisLevel][pairLevel][side][index]
	int level, dim;
	Point[] coords;
	
	public static void main(String[] args) {
		new StitchTest();
	}
	
	private StitchTest() {
		JFrame frame = new JFrame("StitchTest");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		init();
		init(3);
		
		JPanel panel = new JPanel() {
			public void paint(Graphics g) {
				super.paint(g);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.translate(200, 200);
				for (int y = 2; y < (dim - 3); y++) {
					for (int x = 2; x < (dim - 3); x++) {
						Point p0 = coords[dim * y + x];
						Point p1 = coords[dim * y + x + 1];
						Point p2 = coords[dim * y + dim + x + 1];
						Point p3 = coords[dim * y + dim + x];
						g.setColor(Color.GRAY);
						g.fillPolygon(new int[] { p0.x, p1.x, p2.x, p3.x }, new int[] { p0.y, p1.y, p2.y, p3.y }, 4);
						g.setColor(Color.DARK_GRAY);
						g.drawPolygon(new int[] { p0.x, p1.x, p2.x, p3.x }, new int[] { p0.y, p1.y, p2.y, p3.y }, 4);
					}
				}
				g.setColor(Color.BLACK);
				for (Point p : coords) {
					g.fillRect(p.x - 1, p.y - 1, 3, 3);
				}
				g.setColor(Color.RED);
				for (int x = 0; x < (dim - 2); x++) {
					g.drawString(Integer.toString(x), (int) (x * 400.0 / (dim - 3)) - 5, -5);
					g.drawString(Integer.toString(x), -15, (int) (x * 400.0 / (dim - 3)) + 5);
				}
				
				for (int side = 0; side < 4; side++) {
					Color c = null;
					switch (side) {
					case 0:
						c = new Color(255, 0, 0, 64);
						break;
					case 1:
						c = new Color(0, 255, 0, 64);
						break;
					case 2:
						c = new Color(0, 0, 255, 64);
						break;
					case 3:
						c = new Color(255, 255, 0, 64);
						break;
					}
					int levelDelta = level - pairLevels[side];
					g.setColor(Color.GREEN);
					if (levelDelta < 0) {
						levelDelta = 0;
					}
					
					int step = 1 << levelDelta;
					int correction = step == 1 ? 0 : -1;
//					for (int i = 0; i < (dim - 2); i += step) {
//						Point p = coords[rim0[level][side][i]];
//						g.fillRect(p.x - 2, p.y - 2, 5, 5);
//					}
//					for (int i = 0; i < (dim - 2 - 1 * step); i += step) {
//						int ii = i + step / 2 + correction;
//						if (ii >= rim1[level][side].length) {
//							ii = rim1[level][side].length - 1;
//						}
//						drawTriangle(g, rim0[level][side][i], rim0[level][side][i + step], rim1[level][side][ii], c);
//					}
//					for (int i = 0; i < (dim - 5); i++) {
//						int ii = ((i + (step / 2) + 1) / step) * step;
//						drawTriangle(g, rim1[level][side][i], rim1[level][side][i + 1], rim0[level][side][ii], c);
//					}
					
					int pairLevel = Math.min(level, pairLevels[side]);
					int[] triangles = rimTriangles[level][pairLevel][side];
					for (int i = 0; i < triangles.length; ) {
						drawTriangle(g, triangles[i++], triangles[i++], triangles[i++], c);
					}
//					g.setColor(Color.BLUE);
//					for (int i = 0; i < (dim - 4); i++) {
//						Point p = coords[rim1[level][side][i]];
//						g.fillRect(p.x - 2, p.y - 2, 5, 5);
//					}
				}
			}
		};
		panel.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				level += e.getWheelRotation();
				if (level < 1) level = 1;
				if (level > 5) level = 5;
				init(level);
				((JPanel) e.getSource()).repaint();
			}
		});
		panel.setBackground(Color.WHITE);
		frame.add(panel);
		frame.setSize(800, 800);
		frame.setVisible(true);
	}
	
	private void drawTriangle(Graphics g, int i0, int i1, int i2, Color c) {
		Point p0 = coords[i0];
		Point p1 = coords[i1];
		Point p2 = coords[i2];
		Polygon polygon = new Polygon(new int[] { p0.x, p1.x, p2.x }, new int[] { p0.y, p1.y, p2.y }, 3);
		g.setColor(c);
		g.fillPolygon(polygon);
		g.setColor(Color.DARK_GRAY);
		g.drawPolygon(polygon);
	}
	
	private void init() {
		for (int level = 1; level < 7; level++) {
			dim = ((1 << level)) + 3;
			rim0[level] = new int[4][dim - 2];
			for (int i = 0; i < (dim - 2); i++) {
				rim0[level][0][i] = dim + i + 1;
				rim0[level][1][i] = dim + dim - 2 + (dim * i);
				rim0[level][2][i] = dim * dim - dim - 2 - i;
				rim0[level][3][i] = dim * dim - 2 * dim - (dim * i) + 1;
			}
			rim1[level] = new int[4][dim - 4];
			for (int i = 0; i < (dim - 4); i++) {
				rim1[level][0][i] = 2 * dim + i + 2;
				rim1[level][1][i] = 2 * dim + dim - 3 + (dim * i);
				rim1[level][2][i] = dim * dim - 2 * dim - 3 - i;
				rim1[level][3][i] = dim * dim - 3 * dim - (dim * i) + 2;
			}
			
			for (int pairLevel = level; pairLevel >= 0; pairLevel--) {
				rimTriangles[level][pairLevel] = new int[4][];
				int[] tmp = new int[dim * 6];
				for (int side = 0; side < 4; side++) {
					int j = 0;
					int levelDelta = level - pairLevel;
					if (levelDelta < 0) {
						continue;
					}
					int step = 1 << levelDelta;
					int correction = step == 1 ? 0 : -1;
					for (int i = 0; i < (dim - 2 - 1 * step); i += step) {
						int ii = i + step / 2 + correction;
						if (ii >= rim1[level][side].length) {
							ii = rim1[level][side].length - 1;
						}
						tmp[j++] = rim0[level][side][i];
						tmp[j++] = rim0[level][side][i + step];
						tmp[j++] = rim1[level][side][ii];
					}
					for (int i = 0; i < (dim - 5); i++) {
						int ii = ((i + (step / 2) + 1) / step) * step;
						tmp[j++] = rim1[level][side][i];
						tmp[j++] = rim1[level][side][i + 1];
						tmp[j++] = rim0[level][side][ii];
					}
					rimTriangles[level][pairLevel][side] = new int[j];
					System.arraycopy(tmp, 0, rimTriangles[level][pairLevel][side], 0, j);
				}
			}
		}
	}
	
	private void init(int level) {
		this.level = level;
		dim = ((1 << level)) + 3;
		coords = new Point[dim * dim];
		for (int row = 0; row < dim; row++) {
			for (int column = 0; column < dim; column++) {
				coords[row * dim + column] = new Point((int) ((column - 1) * 400.0 / (dim - 3)), (int) ((row - 1) * 400.0 / (dim - 3)));
			}
		}
	}
}
