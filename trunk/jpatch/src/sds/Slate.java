package sds;

public class Slate {
	private static final int UNUSED = 0;
	private static final int EDGE = 1;
	private static final int FACE = 2;
	private static final int POINT = 4;
	
	private static final int MAX_SUBDIV = 6;
	private static final int MAX_VALENCE = 32;
	
	private static final float FACE0 = 1.0f / 4.0f;
	private static final float EDGE0 = 3.0f / 8.0f;
	private static final float EDGE1 = 1.0f / 16.0f;
	private static final float VERTEX0 = 9.0f / 16.0f;
	private static final float VERTEX1 = 3.0f / 32.0f;
	private static final float VERTEX2 = 1.0f / 64.0f;
	
	private static final int MAX_CORNER_LENGTH = 4 + (MAX_VALENCE - 2) * 2;
	private static final int GRID_START = (MAX_VALENCE - 2) * 8;
	private final float[][][] geometryArray = new float[MAX_SUBDIV][][];
	private final int[][][] stencilTable = new int[MAX_SUBDIV][][];
	private final int[][][][][] stencilCorner = new int[MAX_SUBDIV][MAX_VALENCE - 2][4][][];
	
	Slate() {
		for (int level = 0; level < MAX_SUBDIV; level++) {
			int dim = 1 << level + 3;
			
			geometryArray[level] = new float[dim * dim + GRID_START][3];
			
			/*
			 * populate stencil tables for corners
			 */
			for (int valence = 3; valence <= MAX_VALENCE; valence++) {
				for (int corner = 0; corner < 4; corner++) {
					int[][] array = new int[4 + (valence - 2) * 2][4 + (valence - 2) * 2];
					stencilCorner[level][valence - 2][corner] = array;
					
					array[0] = new int[] {
							FACE,
							cornerIndex(corner, valence, -1),
							patchCornerIndex(corner, level, 0, 2),
							patchCornerIndex(corner, level, 1, 2),
							patchCornerIndex(corner, level, 1, 1)
					};
					
					array[1] = new int[] {
							EDGE,
							patchCornerIndex(corner, level, 1, 1),
							patchCornerIndex(corner, level, 1, 2),
							cornerIndex(corner, valence, -1),
							patchCornerIndex(corner, level, 0, 2),
							patchCornerIndex(corner, level, 2, 1),
							patchCornerIndex(corner, level, 2, 2)
					};
					
					array[2] = new int[2 + valence * 2];
					array[2][0] = POINT;
					array[2][1] = patchCornerIndex(corner, level, 1, 1);
					array[2][2] = patchCornerIndex(corner, level, 0, 2);
					array[2][4] = patchCornerIndex(corner, level, 1, 2);
					array[2][5] = patchCornerIndex(corner, level, 2, 2);
					array[2][6] = patchCornerIndex(corner, level, 2, 1);
					array[2][7] = patchCornerIndex(corner, level, 2, 0);
					for (int i = 0, n = (valence - 2) * 2; i < n; i++) {
						array[2][i + 8] = cornerIndex(corner, valence, i);
					}
					
					array[3] = new int[] {
							EDGE,
							patchCornerIndex(corner, level, 1, 1),
							patchCornerIndex(corner, level, 2, 1),
							patchCornerIndex(corner, level, 1, 2),
							patchCornerIndex(corner, level, 2, 2),
							cornerIndex(corner, valence, 0),
							patchCornerIndex(corner, level, 2, 0)
					};
					
					array[4] = new int[] {
							FACE,
							cornerIndex(corner, valence, 0),
							patchCornerIndex(corner, level, 1, 1),
							patchCornerIndex(corner, level, 2, 1),
							patchCornerIndex(corner, level, 2, 0)
					};
					
					for (int i = 0, n = (valence - 2) * 2 - 1; i < n; i++) {
						if ((i & 1) == 0) {			// even -> edge
							array[i + 5] = new int[] {
									EDGE,
									cornerIndex2(corner, level, valence, i),
									patchCornerIndex(corner, level, 1, 1),
									cornerIndex2(corner, level, valence, i + 1),
									cornerIndex2(corner, level, valence, i + 2),
									cornerIndex2(corner, level, valence, i - 1),
									cornerIndex2(corner, level, valence, i - 2)
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
					if (row < 2 || row >= dim - 3) {
						if (column < 3 || column >= dim - 3) {
							stencilTable[level][index] = new int[] { UNUSED };
							continue;
						}
					} else if (row == 2 || row == dim - 3) {
						if (column < 2 || column >= dim - 2) {
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
									FACE,
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
	
	void subdivide(final int maxLevel, final float[][][] boundary) {
		/*
		 * initialize top-level geometry array
		 */
		float[][] geo = geometryArray[0];
		for (int corner = 0; corner < 4; corner++) {
			
			/*
			 * initialize 2x2 grid
			 */
			final int gc = GRID_START + corner;
			final float[][] c = boundary[corner];
			final int n = c.length;
			geo[gc][0] = c[0][0];
			geo[gc][1] = c[0][1];
			geo[gc][2] = c[0][2];
			final int start = corner * MAX_CORNER_LENGTH;
			
			/*
			 * initialize corner arrays
			 */
			for (int i = 1; i < n; i++) {
				final int index = start + i;
				geo[index][0] = c[i][0];
				geo[index][1] = c[i][1];
				geo[index][2] = c[i][2];
			}
		}
		
		/*
		 * subdivide maxLevel times
		 */
		for (int level = 1; level < maxLevel; level++) {
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
				switch (s[0]) {
				case EDGE:
					out[outIndex][0] = (in[s[1]][0] + in[s[2]][0]) * EDGE0 + ((in[s[3]][0] + in[s[4]][0]) + (in[s[5]][0] + in[s[6]][0])) * EDGE1;
					out[outIndex][1] = (in[s[1]][1] + in[s[2]][1]) * EDGE0 + ((in[s[3]][1] + in[s[4]][1]) + (in[s[5]][1] + in[s[6]][1])) * EDGE1;
					out[outIndex][2] = (in[s[1]][2] + in[s[2]][2]) * EDGE0 + ((in[s[3]][2] + in[s[4]][2]) + (in[s[5]][2] + in[s[6]][2])) * EDGE1;
					break;
				case POINT:
					out[outIndex][0] = in[s[1]][0] * VERTEX0 + ((in[s[2]][0] + in[s[4]][0]) + (in[s[3]][0] + in[s[5]][0])) * VERTEX1 + ((in[s[6]][0] + in[s[8]][0]) + (in[s[7]][0] + in[s[9]][0])) * VERTEX2;
					out[outIndex][1] = in[s[1]][1] * VERTEX0 + ((in[s[2]][1] + in[s[4]][1]) + (in[s[3]][1] + in[s[5]][1])) * VERTEX1 + ((in[s[6]][1] + in[s[8]][1]) + (in[s[7]][1] + in[s[9]][1])) * VERTEX2;
					out[outIndex][2] = in[s[1]][2] * VERTEX0 + ((in[s[2]][2] + in[s[4]][2]) + (in[s[3]][2] + in[s[5]][2])) * VERTEX1 + ((in[s[6]][2] + in[s[8]][2]) + (in[s[7]][2] + in[s[9]][2])) * VERTEX2;
					break;
				case FACE:
					out[outIndex][0] = ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * FACE0;
					out[outIndex][1] = ((in[s[1]][1] + in[s[3]][1]) + (in[s[2]][1] + in[s[4]][1])) * FACE0;
					out[outIndex][2] = ((in[s[1]][2] + in[s[3]][2]) + (in[s[2]][2] + in[s[4]][2])) * FACE0;
					break;
				}
			}
			
			/*
			 * apply stencils on corners
			 */
			for (int corner = 0; corner < 4; corner++) {
				final int valence = boundary[corner].length / 2 - 1;
				final int[][] array = stencilCorner[level][valence + 2][corner];
				final int offset = MAX_CORNER_LENGTH * corner;
				final int m = array.length;
				for (int i = 0; i < m; i++) {
					final int[] s = array[i];
					final int outIndex = offset + 1;
					switch (s[0]) {
					case EDGE:
						out[outIndex][0] = (in[s[1]][0] + in[s[2]][0]) * EDGE0 + ((in[s[3]][0] + in[s[4]][0]) + (in[s[5]][0] + in[s[6]][0])) * EDGE1;
						out[outIndex][1] = (in[s[1]][1] + in[s[2]][1]) * EDGE0 + ((in[s[3]][1] + in[s[4]][1]) + (in[s[5]][1] + in[s[6]][1])) * EDGE1;
						out[outIndex][2] = (in[s[1]][2] + in[s[2]][2]) * EDGE0 + ((in[s[3]][2] + in[s[4]][2]) + (in[s[5]][2] + in[s[6]][2])) * EDGE1;
						break;
					case FACE:
						out[outIndex][0] = ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * FACE0;
						out[outIndex][1] = ((in[s[1]][1] + in[s[3]][1]) + (in[s[2]][1] + in[s[4]][1])) * FACE0;
						out[outIndex][2] = ((in[s[1]][2] + in[s[3]][2]) + (in[s[2]][2] + in[s[4]][2])) * FACE0;
						break;
					case POINT:
						float a0 = 0;
						float a1 = 0;
						float a2 = 0;
						float b0 = 0;
						float b1 = 0;
						float b2 = 0;
						for (int p = 2; p < valence; p++) {
							a0 += in[s[p]][0];
							a1 += in[s[p]][0];
							a2 += in[s[p++]][0];
							b0 += in[s[p]][0];
							b1 += in[s[p]][0];
							b2 += in[s[p]][0];
						}
						final float ik = 1 / valence;			// TODO:
						final float aa = 1.5f * ik;				// precompute these values
						final float bb = 0.25f * ik;			// for each valence and
						final float cc = valence - 1.75f;		// use loopup table
						a0 *= aa;
						a1 *= aa;
						a2 *= aa;
						b0 *= bb;
						b1 *= bb;
						b2 *= bb;
						out[outIndex][0] = (a0 + b0 + in[s[1]][0] * cc) * ik;
						out[outIndex][1] = (a1 + b1 + in[s[1]][1] * cc) * ik;
						out[outIndex][2] = (a2 + b2 + in[s[1]][2] * cc) * ik;
					}
				}
			}
		}
	}
	
	private static int patchIndex(int level, int row, int column) {
		int dim = 1 << level + 3;
		if (row < dim) {
			row += dim;
		}
		if (column < dim) {
			column += dim;
		}
		return GRID_START + row * dim + column;
	}
	
	private static int patchCornerIndex(int corner, int level, int row, int column) {
		int dim = 1 << level + 3;
		if (row < dim) {
			row += dim;
		}
		if (column < dim) {
			column += dim;
		}
		switch (corner) {
		case 0:
			return GRID_START + row * dim + column;
		case 1:
			return GRID_START + column * dim + dim - 1 - row;
		case 2:
			return GRID_START + (dim - 1 - row) * dim + dim - 1 - column;
		case 4:
			return GRID_START + (dim - 1 - column) * dim + row;
		default:
			throw new IllegalArgumentException("corner > 3 :" + corner);
		}
	}
	
	private static int cornerIndex(int corner, int valence, int i) {
		return i > 0 ? MAX_CORNER_LENGTH * corner + i : MAX_CORNER_LENGTH * corner + (valence - 2) * 2 + i;
	}
	
	private static int cornerIndex2(int corner, int level, int valence, int i) {
		int max = (valence - 2) * 2 - 1;
		if (i == -2) {
			return patchCornerIndex(corner, level, 2, 1);
		} else if (i == -1) {
			return patchCornerIndex(corner, level, 2, 0);
		} else if (i == max) {
			return patchCornerIndex(corner, level, 0, 2);
		} else if (i == max + 1) {
			return patchCornerIndex(corner, level, 1, 2);
		} else {
			return MAX_CORNER_LENGTH * corner + i;
		}
	}
}
