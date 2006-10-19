package sds;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Kernel {
	public static final int MAX = 5;
	private static final int FACE = 1;
	private static final int EDGE = 2;
	private static final int VERTEX = 3;
	
	private static final int RINGS = (1 << MAX) + 1;
	
	private static int xcoord[];
	private static int ycoord[];
	private static int pos[][];
	private static int label[][];
	
	private static final int[][] buildLookupTable(int valence) {
		int arraySize = 0;
		int[] ringStart = new int[RINGS + 2];
		for (int i = 0; i <= RINGS; i++) {
			if (i > 0) {
				ringStart[i] = arraySize + 1;
			}
			arraySize += i * 2 * valence;
			System.out.println("ring " + i + "\tringstart " + ringStart[i]);
		}
		arraySize++;
		int[][] lookupTable = new int[arraySize][10];
		xcoord = new int[arraySize];
		ycoord = new int[arraySize];
		pos = new int[RINGS * 2 + 1][RINGS * 2 + 1];
		label = new int[RINGS * 2 + 1][RINGS * 2 + 1];
		
		int index = 1;
		for (int ring = 1; ring <= RINGS; ring++) {
			boolean evenRing = (ring & 1) == 0;
			for (int span = 0; span < valence; span++) {
				int spanSize = ring * 2;
				
				for (int i = 0; i < spanSize; i++) {
					switch (span) {
					case 0:
						xcoord[index] = -ring + i;
						ycoord[index] = -ring;
						break;
					case 1:
						xcoord[index] = ring;
						ycoord[index] = -ring + i;
						break;
					case 2:
						xcoord[index] = ring - i;
						ycoord[index] = ring;
						break;
					case 3:
						xcoord[index] = -ring;
						ycoord[index] = ring - i;
					}
					pos[xcoord[index] + RINGS][ycoord[index] + RINGS] = index;
					label[xcoord[index] + RINGS][ycoord[index] + RINGS] = i;
					
					int tr = ring / 2;
					int ti = i / 2;
					boolean even = (i & 1) == 0;
					if (evenRing) {
						if (even) {
							lookupTable[index][9] = VERTEX;
							lookupTable[index][0] = ring(ringStart, valence, tr, span, ti);
							lookupTable[index][1] = ring(ringStart, valence, tr + 1, span, ti + 1);
							if (ti == 0) {
								lookupTable[index][2] = ring(ringStart, valence, tr + 1, span, ti - 1);
							} else {
								lookupTable[index][2] = ring(ringStart, valence, tr - 1, span, ti - 1);
							}
							lookupTable[index][3] = ring(ringStart, valence, tr, span, ti - 1);
							lookupTable[index][4] = ring(ringStart, valence, tr, span, ti + 1);
							
							lookupTable[index][5] = ring(ringStart, valence, tr + 1, span, ti);
							lookupTable[index][6] = ring(ringStart, valence, tr + 1, span, ti + 2);
							if (ti == 0) {
								lookupTable[index][7] = ring(ringStart, valence, tr + 1, span, ti - 2);
							} else if (ti == 1) {
								lookupTable[index][7] = ring(ringStart, valence, tr, span, ti - 2);
							} else {
								lookupTable[index][7] = ring(ringStart, valence, tr - 1, span, ti - 2);
							}
							if (i == spanSize - 2) {
								lookupTable[index][8] = ring(ringStart, valence, tr, span, ti + 2);
							} else {
								lookupTable[index][8] = ring(ringStart, valence, tr - 1, span, ti);
							}							
						} else {
							lookupTable[index][9] = EDGE;
							lookupTable[index][0] = ring(ringStart, valence, tr, span, ti);
							lookupTable[index][1] = ring(ringStart, valence, tr, span, ti + 1);
							lookupTable[index][2] = ring(ringStart, valence, tr + 1, span, ti + 1);
							lookupTable[index][3] = ring(ringStart, valence, tr + 1, span, ti + 2);
							if (i == 1) {
								lookupTable[index][4] = ring(ringStart, valence, tr, span, ti - 1);
							} else {
								lookupTable[index][4] = ring(ringStart, valence, tr - 1, span, ti - 1);
							}
							if (i == spanSize - 1) {
								lookupTable[index][5] = ring(ringStart, valence, tr, span, ti + 2);
							} else {
								lookupTable[index][5] = ring(ringStart, valence, tr - 1, span, ti);
							}
						}
					} else {
						if (even) {
							lookupTable[index][9] = FACE;
							
							lookupTable[index][0] = ring(ringStart, valence, tr, span, ti);
							if (ti == 0) {
								lookupTable[index][1] = ring(ringStart, valence, tr + 1, span, ti - 1);
							} else {
								lookupTable[index][1] = ring(ringStart, valence, tr, span, ti - 1);
							}
							lookupTable[index][2] = ring(ringStart, valence, tr + 1, span, ti);
							lookupTable[index][3] = ring(ringStart, valence, tr + 1, span, ti + 1);
						} else {
							lookupTable[index][9] = EDGE;
							lookupTable[index][0] = ring(ringStart, valence, tr, span, ti);
							lookupTable[index][1] = ring(ringStart, valence, tr + 1, span, ti + 1);
							if (i == 1) {
								lookupTable[index][2] = ring(ringStart, valence, tr + 1, span, ti - 1);
							} else {
								lookupTable[index][2] = ring(ringStart, valence, tr, span, ti - 1);
							}
							lookupTable[index][3] = ring(ringStart, valence, tr + 1, span, ti + 0);
							if (i == spanSize - 1) {
								lookupTable[index][4] = ring(ringStart, valence, tr + 1, span, ti + 3);
							} else {
								lookupTable[index][4] = ring(ringStart, valence, tr, span, ti + 1);
							}
							lookupTable[index][5] = ring(ringStart, valence, tr + 1, span, ti + 2);
						}
					}
					index++;
				}
			}
		}
		return lookupTable;
	}
	
	
	
	private static int ring(int[] ringStart, int valence, int ring, int span, int offset) {
		if (ring == 0) {
			return 0;
		}
		int spanSize = ring * 2;
		return ringStart[ring] + (spanSize * span + offset + valence * spanSize) % (valence * spanSize);	
	}
	
	public static void main(String[] args) {
		new Tester2();
	}
	
	private static class Tester2 {
		Tester2() {
			final int[][] lookupTable = buildLookupTable(4);
			final float[][] f0 = new float[lookupTable.length][3];
			final float[][] f1 = new float[lookupTable.length][3];
			f0[0][0] = 0; f0[0][1] = 0; f0[0][2] = 0;
			f0[1][0] =-1; f0[1][1] = 1; f0[1][2] = 0;
			f0[2][0] = 0; f0[2][1] = 1; f0[2][2] = 0;
			f0[3][0] = 1; f0[3][1] = 1; f0[3][2] = 0;
			f0[4][0] = 1; f0[4][1] = 0; f0[4][2] = 0;
			f0[5][0] = 1; f0[5][1] =-1; f0[5][2] = 0;
			f0[6][0] = 0; f0[6][1] =-1; f0[6][2] = 0;
			f0[7][0] =-1; f0[7][1] =-1; f0[7][2] = 0;
			f0[8][0] =-1; f0[8][1] = 0; f0[8][2] = 0;
			
			f0[9][0] =-2; f0[9][1] = 2; f0[9][2] = 0;
			f0[10][0] =-1; f0[10][1] = 2; f0[10][2] = 0;
			f0[11][0] = 0; f0[11][1] = 2; f0[11][2] = 0;
			f0[12][0] = 1; f0[12][1] = 2; f0[12][2] = 0;
			f0[13][0] = 2; f0[13][1] = 2; f0[13][2] = 0;
			f0[14][0] = 2; f0[14][1] = 1; f0[14][2] = 0;
			f0[15][0] = 2; f0[15][1] = 0; f0[15][2] = 0;
			f0[16][0] = 2; f0[16][1] =-1; f0[16][2] = 0;
			f0[17][0] = 2; f0[17][1] =-2; f0[17][2] = 0;
			f0[18][0] = 1; f0[18][1] =-2; f0[18][2] = 0;
			f0[19][0] = 0; f0[19][1] =-2; f0[19][2] = 0;
			f0[20][0] =-1; f0[20][1] =-2; f0[20][2] = 0;
			f0[21][0] =-2; f0[21][1] =-2; f0[21][2] = 0;
			f0[22][0] =-2; f0[22][1] =-1; f0[22][2] = 0;
			f0[23][0] =-2; f0[23][1] = 0; f0[23][2] = 0;
			f0[24][0] =-2; f0[24][1] = 1; f0[24][2] = 0;
			
			
			for (int i = 0; i < 100; i++) {
				run(4, f0, f1, lookupTable, lookupTable.length);
				run(4, f1, f0, lookupTable, lookupTable.length);
				run(4, f0, f1, lookupTable, lookupTable.length);
			}
			
			f0[0][0] = 0; f0[0][1] = 0; f0[0][2] = 0;
			f0[1][0] =-1; f0[1][1] = 1; f0[1][2] = 0;
			f0[2][0] = 0; f0[2][1] = 1; f0[2][2] = 0;
			f0[3][0] = 1; f0[3][1] = 1; f0[3][2] = 0;
			f0[4][0] = 1; f0[4][1] = 0; f0[4][2] = 0;
			f0[5][0] = 1; f0[5][1] =-1; f0[5][2] = 0;
			f0[6][0] = 0; f0[6][1] =-1; f0[6][2] = 0;
			f0[7][0] =-1; f0[7][1] =-1; f0[7][2] = 0;
			f0[8][0] =-1; f0[8][1] = 0; f0[8][2] = 0;
			
			f0[9][0] =-2; f0[9][1] = 2; f0[9][2] = 0;
			f0[10][0] =-1; f0[10][1] = 2; f0[10][2] = 0;
			f0[11][0] = 0; f0[11][1] = 2; f0[11][2] = 0;
			f0[12][0] = 1; f0[12][1] = 2; f0[12][2] = 0;
			f0[13][0] = 2; f0[13][1] = 2; f0[13][2] = 0;
			f0[14][0] = 2; f0[14][1] = 1; f0[14][2] = 0;
			f0[15][0] = 2; f0[15][1] = 0; f0[15][2] = 0;
			f0[16][0] = 2; f0[16][1] =-1; f0[16][2] = 0;
			f0[17][0] = 2; f0[17][1] =-2; f0[17][2] = 0;
			f0[18][0] = 1; f0[18][1] =-2; f0[18][2] = 0;
			f0[19][0] = 0; f0[19][1] =-2; f0[19][2] = 0;
			f0[20][0] =-1; f0[20][1] =-2; f0[20][2] = 0;
			f0[21][0] =-2; f0[21][1] =-2; f0[21][2] = 0;
			f0[22][0] =-2; f0[22][1] =-1; f0[22][2] = 0;
			f0[23][0] =-2; f0[23][1] = 0; f0[23][2] = 0;
			f0[24][0] =-2; f0[24][1] = 1; f0[24][2] = 0;
			
			long t = System.currentTimeMillis();
			for (int i = 0; i < 1000; i++) {
				run(4, f0, f1, lookupTable, lookupTable.length);
				run(4, f1, f0, lookupTable, lookupTable.length);
				run(4, f0, f1, lookupTable, lookupTable.length);
			}
			System.out.println(System.currentTimeMillis() - t);
			
			JFrame frame = new JFrame();
			
			final float[][] mesh = f1;
			final JPanel panel = new JPanel() {
				public void paint(Graphics g) {
					super.paint(g);
					for (int i = 0; i < lookupTable.length; i++) {
				
						g.drawString(String.valueOf(i), (int) (mesh[i][0] * 300 + 400), (int) (-mesh[i][1] * 300 + 400));
					}
				}
			};
			frame.add(panel);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(900, 900);
			frame.setVisible(true);
		}
	}
	
	private static class Tester {
		int[][] lookupTable;
		int activeX, activeY;
		Tester() {
			lookupTable = buildLookupTable(4);
			JFrame frame = new JFrame();
			final JPanel panel = new JPanel() {
				public void paint(Graphics g) {
					super.paint(g);
//					for (int i = 0; i < lookupTable[0].length; i++) {
//						int x = xcoord[i] * 24 + getWidth() / 2;
//						int y = ycoord[i] * 24 + getHeight() / 2;
//						g.drawString(String.valueOf(i), x, y);
//					}
					g.setColor(Color.WHITE);
					int o = 0;
					for (int width = RINGS * 32 * 2 + 32; width > 0; width -= 64) {
						g.drawRect(o, o, width, width);
						o += 32;
					}
					for (int y = 0; y < RINGS * 2 + 1; y++) {
						for (int x = 0; x < RINGS * 2 + 1; x++) {
							int index = pos[x][y];
							switch(lookupTable[index][9]) {
							case 0:
								g.setColor(Color.GRAY);
								break;
							case 1:
								g.setColor(Color.RED);
								break;
							case 2:
								g.setColor(Color.GREEN);
								break;
							case 3:
								g.setColor(Color.BLUE);
								break;
							}
							g.drawString(String.valueOf(label[x][y]), x * 32 + 8, y * 32 + 24);
						}
					}
					g.setColor(Color.BLACK);
					g.drawRect(activeX * 32, activeY * 32, 32, 32);
					int index = pos[activeX][activeY];
					switch(lookupTable[index][9]) {
					case FACE:
						g.drawOval((RINGS + xcoord[lookupTable[index][0]]) * 32, (RINGS + ycoord[lookupTable[index][0]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[index][1]]) * 32, (RINGS + ycoord[lookupTable[index][1]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[index][2]]) * 32, (RINGS + ycoord[lookupTable[index][2]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[index][3]]) * 32, (RINGS + ycoord[lookupTable[index][3]]) * 32, 32, 32);
						break;
					case EDGE:
						g.drawOval((RINGS + xcoord[lookupTable[index][0]]) * 32, (RINGS + ycoord[lookupTable[index][0]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[index][1]]) * 32, (RINGS + ycoord[lookupTable[index][1]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[index][2]]) * 32 + 4, (RINGS + ycoord[lookupTable[index][2]]) * 32 + 4, 24, 24);
						g.drawOval((RINGS + xcoord[lookupTable[index][3]]) * 32 + 4, (RINGS + ycoord[lookupTable[index][3]]) * 32 + 4, 24, 24);
						g.drawOval((RINGS + xcoord[lookupTable[index][4]]) * 32 + 4, (RINGS + ycoord[lookupTable[index][4]]) * 32 + 4, 24, 24);
						g.drawOval((RINGS + xcoord[lookupTable[index][5]]) * 32 + 4, (RINGS + ycoord[lookupTable[index][5]]) * 32 + 4, 24, 24);
						break;
					case VERTEX:
						g.drawOval((RINGS + xcoord[lookupTable[index][0]]) * 32, (RINGS + ycoord[lookupTable[index][0]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[index][1]]) * 32, (RINGS + ycoord[lookupTable[index][1]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[index][2]]) * 32, (RINGS + ycoord[lookupTable[index][2]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[index][3]]) * 32, (RINGS + ycoord[lookupTable[index][3]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[index][4]]) * 32, (RINGS + ycoord[lookupTable[index][4]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[index][5]]) * 32 + 4, (RINGS + ycoord[lookupTable[index][5]]) * 32 + 4, 24, 24);
						g.drawOval((RINGS + xcoord[lookupTable[index][6]]) * 32 + 4, (RINGS + ycoord[lookupTable[index][6]]) * 32 + 4, 24, 24);
						g.drawOval((RINGS + xcoord[lookupTable[index][7]]) * 32 + 4, (RINGS + ycoord[lookupTable[index][7]]) * 32 + 4, 24, 24);
						g.drawOval((RINGS + xcoord[lookupTable[index][8]]) * 32 + 4, (RINGS + ycoord[lookupTable[index][8]]) * 32 + 4, 24, 24);
						break;
					}
				}
			};
			panel.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseMoved(MouseEvent e) {
					int x = e.getX() / 32;
					int y = e.getY() / 32;
					if (x < pos[0].length && y < pos[0].length) {
						activeX = x;
						activeY = y;
						panel.repaint();
					}
				}
			});
			frame.add(panel);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(900, 900);
			frame.setVisible(true);
		}
	}
	
	private static final float FACE0 = 1.0f / 4.0f;
	private static final float EDGE0 = 3.0f / 8.0f;
	private static final float EDGE1 = 1.0f / 16.0f;
	private static final float VERTEX0 = 9.0f / 16.0f;
	private static final float VERTEX1 = 3.0f / 32.0f;
	private static final float VERTEX2 = 1.0f / 64.0f;
	
	private static void run(final int valence, final float[][] in, final float[][] out, final int[][] lookupTable, final int limit) {
		float a0 = 0, a1 = 0, a2 = 0;
		float b0 = 0, b1 = 0, b2 = 0;
		for (int i = 0; i < valence; i++) {
			int i0 = 2 * i + 1;
			int i1 = i0 + 1;
			a0 += in[i0][0];
			a1 += in[i0][1];
			a2 += in[i0][2];
			b0 += in[i1][0];
			b1 += in[i1][1];
			b2 += in[i1][2];
		}
		a0 *= 4;
		a1 *= 4;
		a2 *= 4;
		a0 += b0;
		a1 += b1;
		a2 += b2;
		a0 /= valence;
		a1 /= valence;
		a2 /= valence;
		int c = 3 * valence - 9;
		out[0][0] = c * in[0][0] + a0;
		out[0][1] = c * in[0][1] + a1;
		out[0][2] = c * in[0][2] + a2;
		for (int i = 1; i < limit; i++) {
			int[] l = lookupTable[i];
			switch (l[9]) {
			case FACE:
				out[i][0] = (in[l[0]][0] + in[l[1]][0] + in[l[2]][0] + in[l[3]][0]) * FACE0;
				out[i][1] = (in[l[0]][1] + in[l[1]][1] + in[l[2]][1] + in[l[3]][1]) * FACE0;
				out[i][2] = (in[l[0]][2] + in[l[1]][2] + in[l[2]][2] + in[l[3]][2]) * FACE0;
				break;
			case EDGE:
				out[i][0] = (in[l[0]][0] + in[l[1]][0]) * EDGE0 + (in[l[2]][0] + in[l[3]][0] + in[l[4]][0] + in[l[5]][0]) * EDGE1;
				out[i][1] = (in[l[0]][1] + in[l[1]][1]) * EDGE0 + (in[l[2]][1] + in[l[3]][1] + in[l[4]][1] + in[l[5]][1]) * EDGE1;
				out[i][2] = (in[l[0]][2] + in[l[1]][2]) * EDGE0 + (in[l[2]][2] + in[l[3]][2] + in[l[4]][2] + in[l[5]][2]) * EDGE1;
				break;
			case VERTEX:
				out[i][0] = in[l[0]][0] * VERTEX0 + (in[l[1]][0] + in[l[2]][0] + in[l[3]][0] + in[l[4]][0]) * VERTEX1 + (in[l[5]][0] + in[l[6]][0] + in[l[7]][0] + in[l[8]][0]) * VERTEX2;
				out[i][1] = in[l[0]][1] * VERTEX0 + (in[l[1]][1] + in[l[2]][1] + in[l[3]][1] + in[l[4]][1]) * VERTEX1 + (in[l[5]][1] + in[l[6]][1] + in[l[7]][1] + in[l[8]][1]) * VERTEX2;
				out[i][2] = in[l[0]][2] * VERTEX0 + (in[l[1]][2] + in[l[2]][2] + in[l[3]][2] + in[l[4]][2]) * VERTEX1 + (in[l[5]][2] + in[l[6]][2] + in[l[7]][2] + in[l[8]][2]) * VERTEX2;
				break;
			}
		}
	}
}
