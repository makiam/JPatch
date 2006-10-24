package sds;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Slate {
	private static final int UNUSED = 0;
	private static final int EDGE = 1;
	private static final int FACE = 2;
	private static final int POINT = 4;
	
	private static final int MAX_SUBDIV = 3;
	private static final int MAX_VALENCE = 4;
	
	private static final float FACE0 = 1.0f / 4.0f;
	private static final float EDGE0 = 3.0f / 8.0f;
	private static final float EDGE1 = 1.0f / 16.0f;
	private static final float VERTEX0 = 9.0f / 16.0f;
	private static final float VERTEX1 = 3.0f / 32.0f;
	private static final float VERTEX2 = 1.0f / 64.0f;
	
	private static final int MAX_CORNER_LENGTH = MAX_VALENCE * 2 - 5;
	private static final int GRID_START = MAX_CORNER_LENGTH * 4;
	private final float[][][] geometryArray = new float[MAX_SUBDIV][][];
	private final int[][][] stencilTable = new int[MAX_SUBDIV][][];
	private final int[][][][][] stencilCorner = new int[MAX_SUBDIV][MAX_VALENCE - 2][4][][];
	
	public Slate() {
		System.out.println("MAX_CORNER_LENGTH=" + MAX_CORNER_LENGTH);
		for (int level = 0; level < MAX_SUBDIV; level++) {
			int dim = ((1 << level)) + 3;
			
			geometryArray[level] = new float[dim * dim + GRID_START][3];
//			System.out.println("geometryarray level " + level + " size=" + geometryArray[level].length + " (" + dim + "x" + dim + " + " + GRID_START + ")");
			if (level == 0) {
				continue;
			}
			/*
			 * populate stencil tables for corners
			 */
			for (int valence = 3; valence <= MAX_VALENCE; valence++) {
				for (int corner = 0; corner < 4; corner++) {
					int[][] array = new int[6 + (valence - 2) * 2][6 + (valence - 2) * 2];
					stencilCorner[level][valence - 3][corner] = array;
					
					array[0] = new int[] {
							EDGE,
							patchCornerIndex2(corner, level + 1, valence, 0, 3, false),
							patchCornerIndex(corner, level, valence, 0, 2),
							patchCornerIndex(corner, level, valence, 1, 2),
							patchCornerIndex(corner, level, valence, 0, 3),
							patchCornerIndex(corner, level, valence, 1, 3),
							patchCornerIndex(corner, level, valence, 0, 1),
							patchCornerIndex(corner, level, valence, 1, 1)
					};
					
					array[1] = new int[] {
							FACE,
							patchCornerIndex(corner, level + 1, valence, 0, 2),
							cornerIndex(corner, valence, -1),
							patchCornerIndex(corner, level, valence, 0, 2),
							patchCornerIndex(corner, level, valence, 1, 2),
							patchCornerIndex(corner, level, valence, 1, 1)
					};
					
					array[2] = new int[] {
							EDGE,
							patchCornerIndex(corner, level + 1, valence, 1, 2),
							patchCornerIndex(corner, level, valence, 1, 1),
							patchCornerIndex(corner, level, valence, 1, 2),
							cornerIndex(corner, valence, -1),
							patchCornerIndex(corner, level, valence, 0, 2),
							patchCornerIndex(corner, level, valence, 2, 1),
							patchCornerIndex(corner, level, valence, 2, 2)
					};
					
					array[3] = new int[3 + valence * 2];
					array[3][0] = POINT;
					array[3][1] = patchCornerIndex(corner, level + 1, valence, 1, 1);
					array[3][2] = patchCornerIndex(corner, level, valence, 1, 1);
					array[3][3] = patchCornerIndex(corner, level, valence, 0, 2);
					array[3][4] = patchCornerIndex(corner, level, valence, 1, 2);
					array[3][5] = patchCornerIndex(corner, level, valence, 2, 2);
					array[3][6] = patchCornerIndex(corner, level, valence, 2, 1);
					array[3][7] = patchCornerIndex(corner, level, valence, 2, 0);
					for (int i = 0, n = (valence - 2) * 2 - 1; i < n; i++) {
						array[3][i + 8] = cornerIndex(corner, valence, i);
					}
					
					array[4] = new int[] {
							EDGE,
							patchCornerIndex(corner, level + 1, valence, 2, 1),
							patchCornerIndex(corner, level, valence, 1, 1),
							patchCornerIndex(corner, level, valence, 2, 1),
							patchCornerIndex(corner, level, valence, 1, 2),
							patchCornerIndex(corner, level, valence, 2, 2),
							cornerIndex(corner, valence, 0),
							patchCornerIndex(corner, level, valence, 2, 0)
					};
					
					array[5] = new int[] {
							FACE,
							patchCornerIndex(corner, level + 1, valence, 2, 0),
							cornerIndex(corner, valence, 0),
							patchCornerIndex(corner, level, valence, 1, 1),
							patchCornerIndex(corner, level, valence, 2, 1),
							patchCornerIndex(corner, level, valence, 2, 0)
					};
					
					array[6] = new int[] {
							EDGE,
							patchCornerIndex2(corner, level + 1, valence, 3, 0, false),
							patchCornerIndex(corner, level, valence, 2, 0),
							patchCornerIndex(corner, level, valence, 2, 1),
							patchCornerIndex(corner, level, valence, 1, 0),
							patchCornerIndex(corner, level, valence, 1, 1),
							patchCornerIndex(corner, level, valence, 3, 0),
							patchCornerIndex(corner, level, valence, 3, 1)
					};
					
					for (int i = 0, n = (valence - 2) * 2 - 1; i < n; i++) {
						if ((i & 1) == 0) {			// even -> edge
							array[i + 7] = new int[] {
									EDGE,
									cornerIndex2(corner, level + 1, valence, i),
									cornerIndex2(corner, level, valence, i),
									patchCornerIndex(corner, level, valence, 1, 1),
									cornerIndex2(corner, level, valence, i + 1),
									cornerIndex2(corner, level, valence, i + 2),
									cornerIndex2(corner, level, valence, i - 1),
									cornerIndex2(corner, level, valence, i - 2)
							};
						} else {					// odd -> face
							array[i + 5] = new int[] {
									FACE,
									cornerIndex2(corner, level + 1, valence, i),
									cornerIndex2(corner, level, valence, i),
									cornerIndex2(corner, level, valence, i + 1),
									patchCornerIndex(corner, level, valence, 1, 1),
									cornerIndex2(corner, level, valence, i - 1)
							};
						}
					}
				}
			}
			
			/*
			 * populate stencil tables for rectangular patch without corners
			 */
			stencilTable[level] = new int[dim * dim][];
			for (int row = 0; row < dim; row++) {
				int rowStart = row * dim;
				for (int column = 0; column < dim; column++) {
					int index = rowStart + column;
//					int a = column - row;
//					int b = dim - 2 - column - row;
//					int quadrant = (a < 0) ? (b < 0) ? 2 : 3 : (b < 0) ? 1 : 0;		// nice, eh?
					
					/*
					 * mask out unused elements (these points are handled in the corner tables)
					 */
					if (row == 3 || row == dim - 4) {
						if (column < 1 || column >= dim - 1) {
							stencilTable[level][index] = new int[] { UNUSED };
							continue;
						}
					} else if (row == 2 || row == dim - 3) {
						if (column < 2 || column >= dim - 2) {
							stencilTable[level][index] = new int[] { UNUSED };
							continue;
						}
					} else if (row == 1 || row == dim - 2) {
						if (column < 3 || column >= dim - 3) {
							stencilTable[level][index] = new int[] { UNUSED };
							continue;
						}
					} else if (row == 0 || row == dim - 1) {
						if (column < 4 || column >= dim - 4) {
							stencilTable[level][index] = new int[] { UNUSED };
							continue;
						}
					} 
					
					if ((column & 1) == 0) {										// column is even
						if ((row & 1) == 0) {										// column and row are even -> Face
							int c = column / 2;
							int r = row / 2;
							stencilTable[level][index] = new int[] {
									FACE,
									patchIndex(level, r, c),
									patchIndex(level, r, c + 1),
									patchIndex(level, r + 1, c + 1),
									patchIndex(level, r + 1, c)
							};
						} else {													// column is even, row is odd -> Edge							
							int c = column / 2;
							int r = (row + 1) / 2;
							stencilTable[level][index] = new int[] {
									EDGE,
									patchIndex(level, r, c),
									patchIndex(level, r, c + 1),
									patchIndex(level, r - 1, c),
									patchIndex(level, r - 1, c + 1),
									patchIndex(level, r + 1, c),
									patchIndex(level, r + 1, c + 1)
							};
						}
					} else {														// x is odd
						if ((row & 1) == 0) {											// x is odd, y is even -> Edge
							int c = (column + 1) / 2;
							int r = row / 2;
							stencilTable[level][index] = new int[] {
									EDGE,
									patchIndex(level, r, c),
									patchIndex(level, r + 1, c),
									patchIndex(level, r, c + 1),
									patchIndex(level, r + 1, c + 1),
									patchIndex(level, r, c - 1),
									patchIndex(level, r + 1, c - 1)
							};
						} else {													// x and y are odd -> Point
							int c = (column + 1) / 2;
							int r = (row + 1) / 2;
							stencilTable[level][index] = new int[] {
									POINT,
									patchIndex(level, r, c),
									patchIndex(level, r - 1, c),
									patchIndex(level, r, c + 1),
									patchIndex(level, r + 1, c),
									patchIndex(level, r, c - 1),
									patchIndex(level, r - 1, c - 1),
									patchIndex(level, r - 1, c + 1),
									patchIndex(level, r + 1, c + 1),
									patchIndex(level, r + 1, c - 1)
							};
						}
					}
				}
			}
		}
	}
	
	public int getGridStart() {
		return GRID_START;
	}
	
	public float[][] getGeo(int level) {
		return geometryArray[level];
	}
	
	public void subdivide(final int maxLevel, final float[][][] boundary) {
		for (int i = 0; i < boundary.length; i++) {
			System.out.print("\n" + i + ":");
			for (int j = 0; j < boundary[i].length; j++) {
				System.out.print("\n\t" + j + ":   ");
				for (int k = 0; k < boundary[i][j].length; k++) {
					System.out.print(" " + boundary[i][j][k]);
				}
			}
		}
		System.out.println();
		
		/*
		 * initialize top-level geometry array
		 */
		float[][] geo = geometryArray[0];
		/*
		 * initialize 2x2 grid
		 */
		geo[GRID_START + 5][0] = boundary[0][0][0];
		geo[GRID_START + 5][1] = boundary[0][0][1];
		geo[GRID_START + 5][2] = boundary[0][0][2];
		geo[GRID_START + 6][0] = boundary[1][0][0];
		geo[GRID_START + 6][1] = boundary[1][0][1];
		geo[GRID_START + 6][2] = boundary[1][0][2];
		geo[GRID_START + 10][0] = boundary[2][0][0];
		geo[GRID_START + 10][1] = boundary[2][0][1];
		geo[GRID_START + 10][2] = boundary[2][0][2];
		geo[GRID_START + 9][0] = boundary[3][0][0];
		geo[GRID_START + 9][1] = boundary[3][0][1];
		geo[GRID_START + 9][2] = boundary[3][0][2];
		
		for (int corner = 0; corner < 4; corner++) {
			final float[][] c = boundary[corner];
			
			
			
			final int n = c.length;
//			geo[gc][0] = c[0][0];
//			geo[gc][1] = c[0][1];
//			geo[gc][2] = c[0][2];
			final int start = corner * MAX_CORNER_LENGTH;
			
			/*
			 * initialize corner arrays
			 */
			for (int i = 1; i < n; i++) {
				final int index = start + i - 1;
				geo[index][0] = c[i][0];
				geo[index][1] = c[i][1];
				geo[index][2] = c[i][2];
			}
		}
		
		
		/*
		 * subdivide maxLevel times
		 */
		for (int level = 1; level < maxLevel; level++) {
//			System.out.println("level=" + level);
			final int[][] stencil = stencilTable[level];
			final float[][] out = geometryArray[level];
			final float[][] in = geometryArray[level - 1];
			final int n = stencil.length;
			
			/*
			 * apply stencils on rectangular inner grid
			 */
			for (int i = 0; i < n; i++) {
				final int[] s = stencil[i];
				final int outIndex = GRID_START + i;
				
//				System.out.println(outIndex + ":");
//				for (int ii : s) {
//					System.out.print(ii + " ");
//				}
//				System.out.println();
				System.out.print("outIndex " + outIndex + " grid stencil type " + s[0] + ":");
				for (int st = 1; st < s.length; st++) {
					System.out.print(" " + s[st]);
				}
				System.out.println();
				switch (s[0]) {
				case EDGE:
					for (int st = 1; st < s.length; st++) {
						System.out.println(st + ":" + s[st] + "=" + in[s[st]][0] + " " + in[s[st]][1] + " " + in[s[st]][2]);
					}
					out[outIndex][0] = (in[s[1]][0] + in[s[2]][0]) * EDGE0 + ((in[s[3]][0] + in[s[4]][0]) + (in[s[5]][0] + in[s[6]][0])) * EDGE1;
					out[outIndex][1] = (in[s[1]][1] + in[s[2]][1]) * EDGE0 + ((in[s[3]][1] + in[s[4]][1]) + (in[s[5]][1] + in[s[6]][1])) * EDGE1;
					out[outIndex][2] = (in[s[1]][2] + in[s[2]][2]) * EDGE0 + ((in[s[3]][2] + in[s[4]][2]) + (in[s[5]][2] + in[s[6]][2])) * EDGE1;
					System.out.println(outIndex + ":" + out[outIndex][0] + " " + out[outIndex][1] + " " + out[outIndex][2]);
					break;
				case POINT:
					out[outIndex][0] = in[s[1]][0] * VERTEX0 + ((in[s[2]][0] + in[s[4]][0]) + (in[s[3]][0] + in[s[5]][0])) * VERTEX1 + ((in[s[6]][0] + in[s[8]][0]) + (in[s[7]][0] + in[s[9]][0])) * VERTEX2;
					out[outIndex][1] = in[s[1]][1] * VERTEX0 + ((in[s[2]][1] + in[s[4]][1]) + (in[s[3]][1] + in[s[5]][1])) * VERTEX1 + ((in[s[6]][1] + in[s[8]][1]) + (in[s[7]][1] + in[s[9]][1])) * VERTEX2;
					out[outIndex][2] = in[s[1]][2] * VERTEX0 + ((in[s[2]][2] + in[s[4]][2]) + (in[s[3]][2] + in[s[5]][2])) * VERTEX1 + ((in[s[6]][2] + in[s[8]][2]) + (in[s[7]][2] + in[s[9]][2])) * VERTEX2;
					break;
				case FACE:
//					System.out.println(outIndex + ": " + s[1] + " " + s[2] + " " + s[3] + " " + s[4]);
					out[outIndex][0] = ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * FACE0;
					out[outIndex][1] = ((in[s[1]][1] + in[s[3]][1]) + (in[s[2]][1] + in[s[4]][1])) * FACE0;
					out[outIndex][2] = ((in[s[1]][2] + in[s[3]][2]) + (in[s[2]][2] + in[s[4]][2])) * FACE0;
//					System.out.println(out[outIndex][0] + " " + out[outIndex][1] + " " + out[outIndex][2]);
					break;
				}
			}
			
			/*
			 * apply stencils on corners
			 */
			for (int corner = 0; corner < 4; corner++) {
				final int valence = boundary[corner].length / 2 + 2;
				final int[][] array = stencilCorner[level][valence - 3][corner];
				final int offset = MAX_CORNER_LENGTH * corner;
				final int m = array.length;
				System.out.println("arrayLength=" + m + " valence=" + valence);
				for (int i = 0; i < m; i++) {
					final int[] s = array[i];
					final int outIndex = s[1];
					
					System.out.print("outIndex " + outIndex + " corner stencil type " + s[0] + ":");
					for (int st = 2; st < s.length; st++) {
						System.out.print(" " + s[st]);
					}
					System.out.println();
					
					switch (s[0]) {
					case EDGE:
						out[outIndex][0] = (in[s[2]][0] + in[s[3]][0]) * EDGE0 + ((in[s[4]][0] + in[s[5]][0]) + (in[s[6]][0] + in[s[7]][0])) * EDGE1;
						out[outIndex][1] = (in[s[2]][1] + in[s[3]][1]) * EDGE0 + ((in[s[4]][1] + in[s[5]][1]) + (in[s[6]][1] + in[s[7]][1])) * EDGE1;
						out[outIndex][2] = (in[s[2]][2] + in[s[3]][2]) * EDGE0 + ((in[s[4]][2] + in[s[5]][2]) + (in[s[6]][2] + in[s[7]][2])) * EDGE1;
						break;
					case FACE:
						out[outIndex][0] = ((in[s[2]][0] + in[s[4]][0]) + (in[s[3]][0] + in[s[5]][0])) * FACE0;
						out[outIndex][1] = ((in[s[2]][1] + in[s[4]][1]) + (in[s[3]][1] + in[s[5]][1])) * FACE0;
						out[outIndex][2] = ((in[s[2]][2] + in[s[4]][2]) + (in[s[3]][2] + in[s[5]][2])) * FACE0;
						break;
					case POINT:
						float a0 = 0;
						float a1 = 0;
						float a2 = 0;
						float b0 = 0;
						float b1 = 0;
						float b2 = 0;
						for (int p = 3; p < s.length; p++) {
							a0 += in[s[p]][0];
							a1 += in[s[p]][1];
							a2 += in[s[p++]][2];
							b0 += in[s[p]][0];
							b1 += in[s[p]][1];
							b2 += in[s[p]][2];
						}
						final float ik = 1.0f / valence;			// TODO:
						final float bb = 1.5f * ik;				// precompute these values
						final float aa = 0.25f * ik;			// for each valence and
						final float cc = valence - 1.75f;		// use loopup table
						a0 *= aa;
						a1 *= aa;
						a2 *= aa;
						b0 *= bb;
						b1 *= bb;
						b2 *= bb;
						out[outIndex][0] = (a0 + b0 + in[s[2]][0] * cc) * ik;
						out[outIndex][1] = (a1 + b1 + in[s[2]][1] * cc) * ik;
						out[outIndex][2] = (a2 + b2 + in[s[2]][2] * cc) * ik;
					}
				}
			}
			System.out.println("\nin" + (level - 1) + "\n==");
			for (int i = 0; i < in.length; i++) {
				if (i == GRID_START) {
					System.out.println("====");
				}
				System.out.println(i + ":" + in[i][0] + " " + in[i][1] + " " + in[i][2]);
			}
			System.out.println("\nout" + level + "\n===");
			for (int i = 0; i < out.length; i++) {
				if (i == GRID_START) {
					System.out.println("====");
				}
				System.out.println(i + ":" + out[i][0] + " " + out[i][1] + " " + out[i][2]);
			}
		}
	}
	
	private static int patchIndex(int level, int row, int column) {
		int i = patchIndex2(level, row, column);
//		System.out.println("patchIndex level " + level + " row " + row + " column " + column + " = " + i);
		return i;
	}
	
	private static int patchIndex2(int level, int row, int column) {
		int dim = (1 << (level - 1)) + 3;
		if (row < 0) {
			row += dim;
		}
		if (column < 0) {
			column += dim;
		}
		if (column == 0) {
			if (row == 0) {
				//return patchCornerIndex(0, level, )
			}
		}
		return GRID_START + row * dim + column;
	}
	
	private static int patchCornerIndex(int corner, int level, int valence, int row, int column) {
		int i = patchCornerIndex2(corner, level, valence, row, column, true);
		System.out.println("patchCornerIndex corner " + corner + " level " + level + " row " + row + " column " + column + " = " + i);
		return i;
	}
	
	private static int patchCornerIndex2(int corner, int level, int valence, int row, int column, boolean correct) {
		int dim = (1 << (level - 1)) + 3;	// TODO ???
		if (level == 1) {
			if (row == 0 && column == 2) {
				return (cornerIndex((corner + 1) % 4, valence, 0));
			} else if (row == 2 && column == 0) {
				return (cornerIndex((corner + 3) % 4, valence, -1));
			}
		}
		if (correct) {
			if (row == 0) {
				if (column == 0) {
					return cornerIndex(corner, valence, -2);
				} else if (column == 1) {
					return cornerIndex(corner, valence, -1);
				} else if (column == dim - 2) {
					return cornerIndex((corner + 1) % 4, valence, 0);
				} else if (column == dim - 1) {
					return cornerIndex((corner + 1) % 4, valence, 1);
				}
			} else if (row == 1) {
				if (column == dim - 1) {
					return cornerIndex((corner + 1) % 4, valence, 2);
				}
			} else if (column == 0) {
				if (row == 0) {
					return cornerIndex(corner, valence, 1);
				} else if (row == 1) {
					return cornerIndex(corner, valence, 0);
				} else if (row == dim - 2) {
					return cornerIndex((corner + 3) % 4, valence, -1);
				} else if (row == dim - 1) {
					return cornerIndex((corner + 3) % 4, valence, -2);
				}
			} else if (column == 1) {
				if (row == dim - 1) {
					return cornerIndex((corner + 3) % 4, valence, -3);
				}
			}
		}
		if (row < 0) {
			row += dim;
		}
		if (column < 0) {
			column += dim;
		}
		System.out.println(dim + " " + GRID_START + " " + row + " " + column);
		switch (corner) {
		case 0:
			return GRID_START + row * dim + column;
		case 1:
			return GRID_START + column * dim + dim - 1 - row;
		case 2:
			return GRID_START + (dim - 1 - row) * dim + dim - 1 - column;
		case 3:
			return GRID_START + (dim - 1 - column) * dim + row;
		default:
			throw new IllegalArgumentException("corner > 3 :" + corner);
		}
	}
	
	private static int cornerIndex(int corner, int valence, int i) {
		int max = valence * 2 - 5;
		return i >= 0 ? MAX_CORNER_LENGTH * corner + i : MAX_CORNER_LENGTH * corner + max + i;
	}
	
	private static int cornerIndex2(int corner, int level, int valence, int i) {
		int max = valence * 2 - 5;
		if (i == -2) {
			return patchCornerIndex(corner, level, valence, 2, 1);
		} else if (i == -1) {
			return patchCornerIndex(corner, level, valence, 2, 0);
		} else if (i == max) {
			return patchCornerIndex(corner, level, valence, 0, 2);
		} else if (i == max + 1) {
			return patchCornerIndex(corner, level, valence, 1, 2);
		} else {
			return MAX_CORNER_LENGTH * corner + i;
		}
	}
	
	
	
	
	private static class Tester {
		int activeX, activeY;
		final Slate slate;
		
		Tester() {
			slate = new Slate();
			JFrame frame = new JFrame();
			final JPanel panel = new JPanel() {
				public void paint(Graphics g) {
					super.paint(g);
//					for (int i = 0; i < table[0].length; i++) {
//						int x = xcoord[i] * 24 + getWidth() / 2;
//						int y = ycoord[i] * 24 + getHeight() / 2;
//						g.drawString(String.valueOf(i), x, y);
//					}
					int level = 1;
					int[][] table = slate.stencilTable[level];
					int dim = (1 << level) + 3;
					for (int row = 0; row < dim; row++) {
						for (int column = 0; column < dim; column++) {
							int index = row * dim + column;
							switch(table[index][0]) {
							case UNUSED:
								g.setColor(Color.GRAY);
								break;
							case EDGE:
								g.setColor(Color.RED);
								break;
							case FACE:
								g.setColor(Color.GREEN);
								break;
							case POINT:
								g.setColor(Color.BLUE);
								break;
							}
							g.drawString(String.valueOf(index), column * 32 + 8, row * 32 + 24);
						}
					}
					
					int dim2 = (1 << (level - 1)) + 3;
					for (int row = 0; row < dim2; row++) {
						for (int column = 0; column < dim2; column++) {
							int index = row * dim2 + column;
							g.setColor(Color.BLACK);
							g.drawString(String.valueOf(index), column * 32 + 8 + 400, row * 32 + 24);
						}
					}
					
					
					if (activeX < dim && activeY < dim) {
						int index = activeY * dim + activeX;
						g.setColor(Color.BLACK);
						g.drawRect(activeX * 32, activeY * 32, 32, 32);
						switch(table[index][0]) {
						case FACE:
							g.drawOval(400 + (xcoord(level, table[index][1])) * 32, (ycoord(level, table[index][1])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, table[index][2])) * 32, (ycoord(level, table[index][2])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, table[index][3])) * 32, (ycoord(level, table[index][3])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, table[index][4])) * 32, (ycoord(level, table[index][4])) * 32, 32, 32);
							break;
						case EDGE:
							g.drawOval(400 + (xcoord(level, table[index][1])) * 32, (ycoord(level, table[index][1])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, table[index][2])) * 32, (ycoord(level, table[index][2])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, table[index][3])) * 32 + 4, (ycoord(level, table[index][3])) * 32 + 4, 24, 24);
							g.drawOval(400 + (xcoord(level, table[index][4])) * 32 + 4, (ycoord(level, table[index][4])) * 32 + 4, 24, 24);
							g.drawOval(400 + (xcoord(level, table[index][5])) * 32 + 4, (ycoord(level, table[index][5])) * 32 + 4, 24, 24);
							g.drawOval(400 + (xcoord(level, table[index][6])) * 32 + 4, (ycoord(level, table[index][6])) * 32 + 4, 24, 24);
							break;
						case POINT:
							g.drawOval(400 + (xcoord(level, table[index][1])) * 32, (ycoord(level, table[index][1])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, table[index][2])) * 32, (ycoord(level, table[index][2])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, table[index][3])) * 32, (ycoord(level, table[index][3])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, table[index][4])) * 32, (ycoord(level, table[index][4])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, table[index][5])) * 32, (ycoord(level, table[index][5])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, table[index][6])) * 32 + 4, (ycoord(level, table[index][6])) * 32 + 4, 24, 24);
							g.drawOval(400 + (xcoord(level, table[index][7])) * 32 + 4, (ycoord(level, table[index][7])) * 32 + 4, 24, 24);
							g.drawOval(400 + (xcoord(level, table[index][8])) * 32 + 4, (ycoord(level, table[index][8])) * 32 + 4, 24, 24);
							g.drawOval(400 + (xcoord(level, table[index][9])) * 32 + 4, (ycoord(level, table[index][9])) * 32 + 4, 24, 24);
							break;
						}
						}
					
//					int index = pos[activeX][activeY];
//					switch(table[index][9]) {
//					case FACE:
//						g.drawOval((xcoord[table[index][0]]) * 32, (ycoord[table[index][0]]) * 32, 32, 32);
//						g.drawOval((xcoord[table[index][1]]) * 32, (ycoord[table[index][1]]) * 32, 32, 32);
//						g.drawOval((xcoord[table[index][2]]) * 32, (ycoord[table[index][2]]) * 32, 32, 32);
//						g.drawOval((xcoord[table[index][3]]) * 32, (ycoord[table[index][3]]) * 32, 32, 32);
//						break;
//					case EDGE:
//						g.drawOval((xcoord[table[index][0]]) * 32, (ycoord[table[index][0]]) * 32, 32, 32);
//						g.drawOval((xcoord[table[index][1]]) * 32, (ycoord[table[index][1]]) * 32, 32, 32);
//						g.drawOval((xcoord[table[index][2]]) * 32 + 4, (ycoord[table[index][2]]) * 32 + 4, 24, 24);
//						g.drawOval((xcoord[table[index][3]]) * 32 + 4, (ycoord[table[index][3]]) * 32 + 4, 24, 24);
//						g.drawOval((xcoord[table[index][4]]) * 32 + 4, (ycoord[table[index][4]]) * 32 + 4, 24, 24);
//						g.drawOval((xcoord[table[index][5]]) * 32 + 4, (ycoord[table[index][5]]) * 32 + 4, 24, 24);
//						break;
//					case VERTEX:
//						g.drawOval((xcoord[table[index][0]]) * 32, (ycoord[table[index][0]]) * 32, 32, 32);
//						g.drawOval((xcoord[table[index][1]]) * 32, (ycoord[table[index][1]]) * 32, 32, 32);
//						g.drawOval((xcoord[table[index][2]]) * 32, (ycoord[table[index][2]]) * 32, 32, 32);
//						g.drawOval((xcoord[table[index][3]]) * 32, (ycoord[table[index][3]]) * 32, 32, 32);
//						g.drawOval((xcoord[table[index][4]]) * 32, (ycoord[table[index][4]]) * 32, 32, 32);
//						g.drawOval((xcoord[table[index][5]]) * 32 + 4, (ycoord[table[index][5]]) * 32 + 4, 24, 24);
//						g.drawOval((xcoord[table[index][6]]) * 32 + 4, (ycoord[table[index][6]]) * 32 + 4, 24, 24);
//						g.drawOval((xcoord[table[index][7]]) * 32 + 4, (ycoord[table[index][7]]) * 32 + 4, 24, 24);
//						g.drawOval((xcoord[table[index][8]]) * 32 + 4, (ycoord[table[index][8]]) * 32 + 4, 24, 24);
//						break;
//					}
				}
			};
			panel.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseMoved(MouseEvent e) {
					activeX = e.getX() / 32;
					activeY = e.getY() / 32;
					panel.repaint();
				}
			});
			frame.add(panel);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(900, 900);
			frame.setVisible(true);
		}
		
		private int xcoord(int level, int index) {
			int dim = (1 << level - 1) + 3;
			return (index - GRID_START) % dim;
		}
		
		private int ycoord(int level, int index) {
			int dim = (1 << level - 1) + 3;
			return (index - GRID_START) / dim;
		}
	}
	
	public static void main(String[] args) {
		new Tester();
	}
}
