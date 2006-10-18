package sds;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Kernel {
	public static final int MAX = 3;
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
		int[][] lookupTable = new int[10][arraySize];
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
					label[xcoord[index] + RINGS][ycoord[index] + RINGS] = index - ringStart[ring];
					
					boolean even = (i & 1) == 0;
					if (evenRing) {
						if (even) {
							lookupTable[0][index] = VERTEX;						
						} else {
							lookupTable[0][index] = EDGE;
						}
					} else {
						int upSpanSize = spanSize + 2;
						int downSpanSize = spanSize - 2;
						int upRing = ring + 1;
						int downRing = ring - 1;
						if (even) {
							lookupTable[0][index] = FACE;
							lookupTable[1][index] = ringStart[upRing] + upSpanSize * span + i;
							lookupTable[2][index] = ringStart[upRing] + upSpanSize * span + i + 2;
							if (ring == 1) {
								lookupTable[3][index] = 0;
							} else {
								lookupTable[3][index] = ringStart[downRing] + (downSpanSize * span + i) % (valence * downSpanSize);
							}
							if (i == 0) {
								lookupTable[4][index] = ringStart[upRing] + (upSpanSize * span + valence * upSpanSize - 2) % (valence * upSpanSize);
							} else {
								lookupTable[4][index] = ringStart[downRing] + downSpanSize * span + i - 2;
							}
						} else {
							lookupTable[0][index] = EDGE;
							lookupTable[1][index] = ringStart[upRing] + upSpanSize * span + i - 1;
							lookupTable[2][index] = ringStart[upRing] + upSpanSize * span + i + 1;
							lookupTable[3][index] = ringStart[upRing] + (upSpanSize * span + i + 3) % (valence * upSpanSize);
							if (ring == 1) {
								lookupTable[5][index] = 0;
							} else {
								lookupTable[5][index] = ringStart[downRing] + (downSpanSize * span + i - 1) % (valence * downSpanSize);
							}
							if (i == 1) {
								lookupTable[4][index] = ringStart[upRing] + (upSpanSize * span + i - 3 + upSpanSize * valence) % (valence * upSpanSize);
								lookupTable[6][index] = ringStart[downRing] + downSpanSize * span + i + 1;
							} else if (i == spanSize - 1) {
								lookupTable[4][index] = ringStart[downRing] + downSpanSize * span + i - 3;
								lookupTable[6][index] = ringStart[upRing] + (upSpanSize * span + i + 5) % (valence * upSpanSize);
							} else {
								lookupTable[4][index] = ringStart[downRing] + downSpanSize * span + i - 3;
								lookupTable[6][index] = ringStart[downRing] + (downSpanSize * span + i + 1) % (valence * downSpanSize);
							}
						}
					}
					index++;
				}
			}
		}
		return lookupTable;
	}
	
	public static void main(String[] args) {
		new Tester();
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
					for (int y = 0; y < RINGS * 2 + 1; y++) {
						for (int x = 0; x < RINGS * 2 + 1; x++) {
							int index = pos[x][y];
							switch(lookupTable[0][index]) {
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
					switch(lookupTable[0][index]) {
					case 1:
						g.drawOval((RINGS + xcoord[lookupTable[1][index]]) * 32, (RINGS + ycoord[lookupTable[1][index]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[2][index]]) * 32, (RINGS + ycoord[lookupTable[2][index]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[3][index]]) * 32, (RINGS + ycoord[lookupTable[3][index]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[4][index]]) * 32, (RINGS + ycoord[lookupTable[4][index]]) * 32, 32, 32);
						break;
					case 2:
						g.drawOval((RINGS + xcoord[lookupTable[1][index]]) * 32, (RINGS + ycoord[lookupTable[1][index]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[2][index]]) * 32, (RINGS + ycoord[lookupTable[2][index]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[3][index]]) * 32, (RINGS + ycoord[lookupTable[3][index]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[4][index]]) * 32, (RINGS + ycoord[lookupTable[4][index]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[5][index]]) * 32, (RINGS + ycoord[lookupTable[5][index]]) * 32, 32, 32);
						g.drawOval((RINGS + xcoord[lookupTable[6][index]]) * 32, (RINGS + ycoord[lookupTable[6][index]]) * 32, 32, 32);
						break;
//					case 3:
//						g.drawOval((RINGS + xcoord[lookupTable[1][index]]) * 32, (RINGS + ycoord[lookupTable[1][index]]) * 32, 32, 32);
//						g.drawOval((RINGS + xcoord[lookupTable[2][index]]) * 32, (RINGS + ycoord[lookupTable[2][index]]) * 32, 32, 32);
//						g.drawOval((RINGS + xcoord[lookupTable[3][index]]) * 32, (RINGS + ycoord[lookupTable[3][index]]) * 32, 32, 32);
//						g.drawOval((RINGS + xcoord[lookupTable[4][index]]) * 32, (RINGS + ycoord[lookupTable[4][index]]) * 32, 32, 32);
//						g.drawOval((RINGS + xcoord[lookupTable[5][index]]) * 32, (RINGS + ycoord[lookupTable[2][index]]) * 32, 32, 32);
//						g.drawOval((RINGS + xcoord[lookupTable[6][index]]) * 32, (RINGS + ycoord[lookupTable[3][index]]) * 32, 32, 32);
//						g.drawOval((RINGS + xcoord[lookupTable[7][index]]) * 32, (RINGS + ycoord[lookupTable[4][index]]) * 32, 32, 32);
//						break;
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
}
