package sds;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.*;
import javax.vecmath.*;

public class FragmentTesselator {
	public static final int MAX = 5;
	public static final int MAX_VALENCE = 16;
	
	private static final int FACE = 1;
	private static final int EDGE = 2;
	private static final int VERTEX = 3;
	
	private static final int RINGS = (1 << (MAX - 1)) + 1;
	
	private static final float FACE0 = 1.0f / 4.0f;
	private static final float EDGE0 = 3.0f / 8.0f;
	private static final float EDGE1 = 1.0f / 16.0f;
	private static final float VERTEX0 = 9.0f / 16.0f;
	private static final float VERTEX1 = 3.0f / 32.0f;
	private static final float VERTEX2 = 1.0f / 64.0f;
	
	public static final int[][] TABLE_SIZES = new int[MAX_VALENCE - 2][MAX + 1];
	
	public final int[][][] lookupTables = new int[MAX_VALENCE - 2][][];
	private final float[][][] vertexBuffer = new float[MAX + 1][][];
	
	private static Matrix3f mat = new Matrix3f();
	private static float rotx = 0;
	private static float roty = 0;
	
	private static int xcoord[];
	private static int ycoord[];
	private static int pos[][];
	private static int label[][];
	
	public FragmentTesselator() {
		int sum = 0;
		for (int i = 0; i <= MAX_VALENCE - 3; i++) {
			int valence = i + 3;
			lookupTables[i] = buildLookupTable(valence);
			for (int j = 0; j <= MAX; j++) {
				int rings = (1 << j) + 1;
				TABLE_SIZES[i][j] = valence * rings * (rings + 1) + 1;
				if (i == MAX_VALENCE - 3) {
					vertexBuffer[j] = new float[TABLE_SIZES[i][j]][3];
				}
//				System.out.println("valence " + valence + " depth " + j + " rings " + rings + " tablesize " + TABLE_SIZES[i][j]);
			}
		}
		System.out.println(sum + " " + sum * 3 * 4);
		System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
	}
	
	private final int[][] buildLookupTable(int valence) {
		int arraySize = 0;
		int[] ringStart = new int[RINGS + 2];
		for (int i = 0; i <= RINGS; i++) {
			if (i > 0) {
				ringStart[i] = arraySize + 1;
			}
			arraySize += i * 2 * valence;
//			System.out.println("ring " + i + "\tringstart " + ringStart[i]);
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
							}switch (span) {
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
	
	public static void main(String[] args) throws Exception {
		new Tester2();
	}
	
	private static class Tester2 {
		Tester2() throws Exception {
			FragmentTesselator kernel = new FragmentTesselator();
			Sds sds = new Sds(new FileInputStream("/home/sascha/off/cube.off"));
//			final float[][] vb = kernel.getVertexBuffer(0);
//			vb[0][0] = 0; vb[0][1] = 0; vb[0][2] = 0;
//			vb[1][0] =-1; vb[1][1] = 1; vb[1][2] = 0;
//			vb[2][0] = 0; vb[2][1] = 1; vb[2][2] = 0;
//			vb[3][0] = 1; vb[3][1] = 1; vb[3][2] = 0;
//			vb[4][0] = 1; vb[4][1] = 0; vb[4][2] = 0;
//			vb[5][0] = 1; vb[5][1] =-1; vb[5][2] = 0;
//			vb[6][0] = 0; vb[6][1] =-1; vb[6][2] = 0;
//			vb[7][0] =-1; vb[7][1] =-1; vb[7][2] = 0;
//			vb[8][0] =-1; vb[8][1] = 0; vb[8][2] = 0;
//			
//			vb[9][0] =-2; vb[9][1] = 2; vb[9][2] = 0;
//			vb[10][0] =-1; vb[10][1] = 2; vb[10][2] = 0;
//			vb[11][0] = 0; vb[11][1] = 2; vb[11][2] = 0;
//			vb[12][0] = 1; vb[12][1] = 2; vb[12][2] = 0;
//			vb[13][0] = 2; vb[13][1] = 2; vb[13][2] = 0;
//			vb[14][0] = 2; vb[14][1] = 1; vb[14][2] = 0;
//			vb[15][0] = 2; vb[15][1] = 0; vb[15][2] = 0;
//			vb[16][0] = 2; vb[16][1] =-1; vb[16][2] = 0;
//			vb[17][0] = 2; vb[17][1] =-2; vb[17][2] = 0;
//			vb[18][0] = 1; vb[18][1] =-2; vb[18][2] = 0;
//			vb[19][0] = 0; vb[19][1] =-2; vb[19][2] = 0;
//			vb[20][0] =-1; vb[20][1] =-2; vb[20][2] = 0;
//			vb[21][0] =-2; vb[21][1] =-2; vb[21][2] = 0;
//			vb[22][0] =-2; vb[22][1] =-1; vb[22][2] = 0;
//			vb[23][0] =-2; vb[23][1] = 0; vb[23][2] = 0;
//			vb[24][0] =-2; vb[24][1] = 1; vb[24][2] = 0;
			sds.subdivide();
			Vertex vertex = sds.topLevelVertices[0];
			final int valence = vertex.valence();
			vertex.initFragment(valence, kernel.getVertexBuffer(0));
			
			final int level = 1;
			
			kernel.subdivideFragment(valence, level);
			
//			long t = System.currentTimeMillis();
//			for (int i = 0; i < 1000; i++) {
//				kernel.subdivideFragment(4, MAX);
//			}
//			System.out.println(System.currentTimeMillis() - t);
//			
//			t = System.currentTimeMillis();
//			for (int i = 0; i < 1000; i++) {
//				kernel.subdivideFragment(4, MAX);
//			}
//			System.out.println(System.currentTimeMillis() - t);
			
			JFrame frame = new JFrame();
			
			final float[][] m = kernel.getVertexBuffer(level);
			final JPanel panel = new JPanel() {
				Point3f p = new Point3f();
				public void paint(Graphics g) {
					super.paint(g);
					p.set(m[0]);
					mat.transform(p);
					float x = (p.x * 400 + 400);
					float y = (p.y * 400 + 400);
//					g.drawRect(x - 1, y - 1, 3, 3);
//					g.drawString("7", x, y);
					GeneralPath path = new GeneralPath();
					path.moveTo(x, y);
					for (int i = 1; i < TABLE_SIZES[valence - 3][level]; i++) {
						p.set(m[i]);
						mat.transform(p);
						x = (p.x * 400 + 400);
						y = (p.y * 400 + 400);
						path.lineTo(x, y);
//						g.drawString(String.valueOf(i), x, y);
					}
					((Graphics2D) g).draw(path);
				}
			};
			panel.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseMoved(MouseEvent e) {
					rotx = e.getY() / 100.0f;
					roty = e.getX() / 100.0f;
					mat.rotY(roty);
					Matrix3f mm = new Matrix3f();
					mm.rotX(rotx);
					mat.mul(mm);
					panel.repaint();
				}
			});
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
			FragmentTesselator kernel = new FragmentTesselator();
			lookupTable = kernel.buildLookupTable(4);
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
	
	public float[][] getVertexBuffer(int level) {
		return vertexBuffer[level];
	}
	
	public void subdivideFragment(final int valence, final int depth) {
		int[][] lookupTable = lookupTables[valence - 3];
		for (int level = 1; level <= depth; level++) {
			float[][] in = vertexBuffer[level - 1];
			float[][] out = vertexBuffer[level];
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
			float n42 = 4 * valence * valence;
			a0 *= 6;
			a1 *= 6;
			a2 *= 6;
			a0 += b0;
			a1 += b1;
			a2 += b2;
			float c = n42 - 7 * valence;
			out[0][0] = (c * in[0][0] + a0) / n42;
			out[0][1] = (c * in[0][1] + a1) / n42;
			out[0][2] = (c * in[0][2] + a2) / n42;
			int limit = TABLE_SIZES[valence - 3][level];
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
}
