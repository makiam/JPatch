package sds;


import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.FloatBuffer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.*;

import static java.lang.Math.*;

public class SlateTesselator2 {
	private static final int UNUSED = 0;
	private static final int EDGE = 1;
	private static final int EDGE_H = 2;
	private static final int EDGE_V = 3;
	private static final int FACE = 4;
	private static final int POINT = 5;
	private static final int CREASE_4_5 = 6;
	private static final int CREASE_4_6 = 7;
	private static final int CREASE_4_7 = 8;
	private static final int CREASE_5_6 = 9;
	private static final int CREASE_5_7 = 10;
	private static final int CREASE_6_7 = 11;
	
	private static final int MAX_SUBDIV = 6;
	private static final int MAX_VALENCE = 16;
	
	private static final float FACE0 = 1.0f / 4.0f;
	private static final float EDGE0 = 3.0f / 8.0f;
	private static final float EDGE1 = 1.0f / 16.0f;
	private static final float VERTEX0 = 9.0f / 16.0f;
	private static final float VERTEX1 = 3.0f / 32.0f;
	private static final float VERTEX2 = 1.0f / 64.0f;
	private static final float LIMIT0 = 16.0f / 36.0f;
	private static final float LIMIT1 = 4.0f / 36.0f;
	private static final float LIMIT2 = 1.0f / 36.0f;
//	private static final float LIMIT0 = 1;
//	private static final float LIMIT1 = 0;
//	private static final float LIMIT2 = 0f;
	private static final float CREASE0 = 3.0f / 4.0f;
	private static final float CREASE1 = 1.0f / 8.0f;
	
	private static final float[][] TANGENT_FACE_WEIGHT = new float[MAX_VALENCE - 2][];			// [valence][index]
	private static final float[][] TANGENT_EDGE_WEIGHT = new float[MAX_VALENCE - 2][];			// [valence][index]

	private static final int MAX_CORNER_LENGTH = MAX_VALENCE * 2 - 5;
	private static final int GRID_START = MAX_CORNER_LENGTH * 2;
	private final float[][][] subdivPoints = new float[MAX_SUBDIV][][];							// [level][index][0=x,1=y,2=z] index = row * dim + column
	private final float[][][] limitPoints = new float[MAX_SUBDIV][][];							// [level][index][0=x,1=y,2=z] index = row * dim + column
	private final float[][][] limitNormals = new float[MAX_SUBDIV][][];							// [level][index][0=x,1=y,2=z] index = row * dim + column
	private final int[][][] patchStencil = new int[MAX_SUBDIV][][];								// [level][index][stencil]
	private final int[][][][][] fanStencil = new int[MAX_SUBDIV][MAX_VALENCE - 2][2][][];		// [level][valence - 3][corner][index][stencil]
	private final int[][][][] cornerStencil = new int[MAX_SUBDIV][MAX_VALENCE- 2][2][];			// [level][valence - 3][corner][stencil]
	private final int[][][] patchLimitStencil = new int[MAX_SUBDIV][][];						// [level][index][stencil]
	private final int[][][][] cornerLimitStencil = new int[MAX_SUBDIV][MAX_VALENCE- 2][2][];			// [level][valence - 3][corner][stencil]
	private final float[] interleavedArray;
	private final FloatBuffer buffer;
	
	int[][][] rim0 = new int[MAX_SUBDIV][][];								//[level][side][index] outer grid rim
	int[][][] rim1 = new int[MAX_SUBDIV][][];								//[level][side][index] inner grid rim
	int[][][][] rimTriangles = new int[MAX_SUBDIV][MAX_SUBDIV][][]; 		// [thisLevel][pairLevel][side][index]
	
	static {
		for (int valence = 3; valence <= MAX_VALENCE; valence++) {
			int i = valence - 3;
			TANGENT_FACE_WEIGHT[i] = new float[valence];
			TANGENT_EDGE_WEIGHT[i] = new float[valence];
			float An = (float) (1 + cos(2 * PI / valence) + cos(PI / valence) * sqrt(2 * (9 + cos(2 * PI / valence))));
			for (int j = 0; j < valence; j++) {
				TANGENT_EDGE_WEIGHT[i][j] = (float) (An * cos(2 * PI  * j / valence));
				TANGENT_FACE_WEIGHT[i][j] = (float) (cos(2 * PI * j / valence) + cos(2 * PI * (j + 1) / valence));
			}
		}
	}
	
	public static void main(String[] args) {
		SlateTesselator2 slateTesselator = new SlateTesselator2();
		slateTesselator.new Tester(); 
	}
	
	public SlateTesselator2() {
//		System.out.println("MAX_CORNER_LENGTH=" + MAX_CORNER_LENGTH);
		int maxdim = ((1 << (MAX_SUBDIV - 1))) + 3;
		interleavedArray = new float[(maxdim - 3) * (maxdim - 3) * 4 * 6];
		System.out.println("ia_length=" + interleavedArray.length);
//		buffer = BufferUtil.newFloatBuffer(interleavedArray.length * 4);
//		buffer = FloatBuffer.allocate(interleavedArray.length * 4);
		buffer = FloatBuffer.wrap(interleavedArray);
		System.out.println(buffer.isDirect());
//		System.exit(0);
//		buffer = ByteBuffer.allocateDirect(interleavedArray.length * 16).asFloatBuffer();
//		buffer = javax.mediaByteBuffer.allocateDirect(interleavedArray.length * 4).asFloatBuffer();
		
		for (int level = 0; level < MAX_SUBDIV; level++) {
			final int dim = ((1 << level)) + 3;
			final int nextDim = ((1 << (level + 1))) + 3;
			
			if (level > 0) {
				/*
				 * create rim arrays
				 */
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
							tmp[j++] = rim0[level][side][i] + GRID_START;
							tmp[j++] = rim0[level][side][i + step] + GRID_START;
							tmp[j++] = rim1[level][side][ii] + GRID_START;
						}
						for (int i = 0; i < (dim - 5); i++) {
							int ii = ((i + (step / 2) + 1) / step) * step;
							tmp[j++] = rim0[level][side][ii] + GRID_START;
							tmp[j++] = rim1[level][side][i + 1] + GRID_START;
							tmp[j++] = rim1[level][side][i] + GRID_START;
						}
						rimTriangles[level][pairLevel][side] = new int[j];
						System.arraycopy(tmp, 0, rimTriangles[level][pairLevel][side], 0, j);
					}
				}
			}
			
			subdivPoints[level] = new float[dim * dim + GRID_START][3];
			limitPoints[level] = new float[dim * dim + GRID_START][3];
			limitNormals[level] = new float[dim * dim + GRID_START][3];
			
//			System.out.println("geometryarray level " + level + " size=" + geometryArray[level].length + " (" + dim + "x" + dim + " + " + GRID_START + ")");
			if (level == 0) {
//				continue;
			}
			
			/*
			 * populate subdivision stencil tables for corners
			 */
			for (int valence = 3; valence <= MAX_VALENCE; valence++) {
				for (int corner = 0; corner < 2; corner ++) {
					cornerStencil[level][valence - 3][corner] = new int[valence * 2 + 3];
					cornerStencil[level][valence - 3][corner][0] = 0;							// sharpness
					cornerStencil[level][valence - 3][corner][1] = patchCornerIndex(corner, level + 1, 1, 1);	// target!
					cornerStencil[level][valence - 3][corner][2] = patchCornerIndex(corner, level, 1, 1);
					cornerStencil[level][valence - 3][corner][3] = patchCornerIndex(corner, level, 0, 2);
					cornerStencil[level][valence - 3][corner][4] = patchCornerIndex(corner, level, 1, 2);
					cornerStencil[level][valence - 3][corner][5] = patchCornerIndex(corner, level, 2, 2);
					cornerStencil[level][valence - 3][corner][6] = patchCornerIndex(corner, level, 2, 1);
					cornerStencil[level][valence - 3][corner][7] = patchCornerIndex(corner, level, 2, 0);
					for (int i = 0, n = valence * 2 - 5; i < n; i++) {
						int index = i + 1;
						if (index == n) {
							index = 0;
						}
						cornerStencil[level][valence - 3][corner][i + 8] = cornerIndex(corner, valence, index);
					}

					cornerLimitStencil[level][valence - 3][corner] = new int[valence * 2 + 3];
					cornerLimitStencil[level][valence - 3][corner][0] = 0;											// sharpness
					cornerLimitStencil[level][valence - 3][corner][1] = patchCornerIndex(corner, level + 1, 1, 1);	// target!
					cornerLimitStencil[level][valence - 3][corner][2] = patchCornerIndex(corner, level + 1, 1, 1);
					cornerLimitStencil[level][valence - 3][corner][3] = patchCornerIndex(corner, level + 1, 0, 2);
					cornerLimitStencil[level][valence - 3][corner][4] = patchCornerIndex(corner, level + 1, 1, 2);
					cornerLimitStencil[level][valence - 3][corner][5] = patchCornerIndex(corner, level + 1, 2, 2);
					cornerLimitStencil[level][valence - 3][corner][6] = patchCornerIndex(corner, level + 1, 2, 1);
					cornerLimitStencil[level][valence - 3][corner][7] = patchCornerIndex(corner, level + 1, 2, 0);
					for (int i = 0, n = valence * 2 - 5; i < n; i++) {
						int index = i + 1;
						if (index == n) {
							index = 0;
						}
						cornerLimitStencil[level][valence - 3][corner][i + 8] = cornerIndex(corner, valence, index);
					}
					
					int[][] array = new int[cornerStencilLength(valence)][];
					fanStencil[level][valence - 3][corner] = array;
					
					
					
					if (valence == 3) {
						array[0] = new int[] {
								EDGE,
								0,
								cornerIndex2(corner, level, valence, 0),
								patchCornerIndex(corner, level, 1, 1),
								cornerIndex2(corner, level, valence, 2),
								cornerIndex2(corner, level, valence, 3),
								cornerIndex2(corner, level, valence, -1),
								cornerIndex2(corner, level, valence, -2)
						};
						array[1] = array[0].clone();
					} else {
						for (int i = 0, n = valence * 2 - 5; i < n; i++) {
							int index = i + 1;
							if (index == n) {
								index = 0;
							}
							if ((i & 1) == 0) {			// even -> edge
								array[index] = new int[] {
										EDGE,
										0,
										cornerIndex2(corner, level, valence, i),
										patchCornerIndex(corner, level, 1, 1),
										cornerIndex2(corner, level, valence, i + 1),
										cornerIndex2(corner, level, valence, i + 2),
										cornerIndex2(corner, level, valence, i - 1),
										cornerIndex2(corner, level, valence, i - 2)
								};
							} else {					// odd -> face
								array[index] = new int[] {
										FACE,
										cornerIndex2(corner, level, valence, i),
										cornerIndex2(corner, level, valence, i + 1),
										patchCornerIndex(corner, level, 1, 1),
										cornerIndex2(corner, level, valence, i - 1)
								};
							}
						}
					}
				}
			}
			
			/*
			 * populate subdivision stencil tables for rectangular patch without corners
			 */
			patchStencil[level] = new int[dim * dim][];
			for (int row = 0; row < dim; row++) {
				int rowStart = row * dim;
				for (int column = 0; column < dim; column++) {
					int index = rowStart + column;
					int nextRow = row * 2 - 1;
					int nextColumn = column * 2 - 1;
					int nextLevelIndex_0 = getStencilIndex(level + 1, nextRow, nextColumn);
					int nextLevelIndex_1 = getStencilIndex(level + 1, nextRow - 1, nextColumn);
					int nextLevelIndex_2 = getStencilIndex(level + 1, nextRow, nextColumn + 1);
					int nextLevelIndex_3 = getStencilIndex(level + 1, nextRow + 1, nextColumn);
					int nextLevelIndex_4 = getStencilIndex(level + 1, nextRow, nextColumn - 1);
//					System.out.println(
//							nextLevelIndex_0 + " " +
//							nextLevelIndex_1 + " " +
//							nextLevelIndex_2 + " " +
//							nextLevelIndex_3 + " " +
//							nextLevelIndex_4 + " "
//					);
					
					/*
					 * mask out unused elements (these points are handled in the corner and fan tables)
					 */
					if ((row < 2 && column < 2) || (row > dim - 3 && column > dim - 3)) {
						patchStencil[level][index] = new int[] { UNUSED, 0 };
						continue;
					} 
					
					if ((column & 1) == 0) {										// column is even
						if ((row & 1) == 0) {										// column and row are even -> Face
							int c = column / 2;
							int r = row / 2;
							patchStencil[level][index] = new int[] {
									FACE,
									patchIndex(level, r, c),
									patchIndex(level, r, c + 1),
									patchIndex(level, r + 1, c + 1),
									patchIndex(level, r + 1, c)
							};
						} else {													// column is even, row is odd -> Edge							
							int c = column / 2;
							int r = (row + 1) / 2;
							patchStencil[level][index] = new int[] {
									EDGE_H,
									0,												// sharpness
									nextLevelIndex_0,									// next level stencil index
									nextLevelIndex_1,
									nextLevelIndex_2,
									nextLevelIndex_3,
									nextLevelIndex_4,
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
							patchStencil[level][index] = new int[] {
									EDGE_V,
									0,												// sharpness
									nextLevelIndex_0,									// next level stencil index
									nextLevelIndex_1,
									nextLevelIndex_2,
									nextLevelIndex_3,
									nextLevelIndex_4,
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
							patchStencil[level][index] = new int[] {
									POINT,
									0,												// sharpness
									nextLevelIndex_0,									// next level stencil index
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
			
			/*
			 * populate limit/normal stencil tables for rectangular patch without corners
			 */
			patchLimitStencil[level] = new int[dim * dim][];
			for (int row = 1; row < dim - 1; row++) {
				int rowStart = row * dim;
				for (int column = 1; column < dim - 1; column++) {
					/*
					 * mask out unused elements (these points are handled in the cornerLimit table)
					 */
					if ((row == 1 && column == 1) || (row == dim - 2 && column == dim - 2)) {
						continue;
					} 
					
					int index = rowStart + column;
					patchLimitStencil[level][index] = new int[] {
							patchIndex(level + 1, row, column),
							patchIndex(level + 1, row - 1, column),
							patchIndex(level + 1, row, column + 1),
							patchIndex(level + 1, row + 1, column),
							patchIndex(level + 1, row, column - 1),
							patchIndex(level + 1, row - 1, column - 1),
							patchIndex(level + 1, row - 1, column + 1),
							patchIndex(level + 1, row + 1, column + 1),
							patchIndex(level + 1, row + 1, column - 1)
					};
				}
			}
		}
	}
	
	private int getStencilIndex(int level, int row, int column) {
		final int dim = ((1 << level)) + 3;
		if (row < 0 | row >= dim - 1 | column < 0 | column >= dim -1) {
			return 0;
		}
		return row * dim + column;
	}
	
	public int getGridStart() {
		return GRID_START;
	}
	
	public float[][] getLimitVertices(int level) {
		return limitPoints[level];
	}
	
	public float[][] getLimitNormals(int level) {
		return limitNormals[level];
	}
	
	public float[] getInterleavedArray() {
		return interleavedArray;
	}
	
	public FloatBuffer getBuffer() {
//		buffer.rewind();
		return buffer;
	}
	
	public int[] getRimTriangles(int level, int side, int pairLevel) {
//		System.out.println("level=" + level + " pairlevel=" + pairLevel);
		if (pairLevel > level) {
			pairLevel = level;
		}
		return rimTriangles[level][pairLevel][side];
	}
	
	public int[] getRim(int level, int side) {
		return rim0[level][side];
	}
	
	public int tesselate(final Slate slate, final int depth) {
		Point3f pt;
		final Point3f[][] boundary = slate.screenFans;
		
		/*
		 * initialize top-level geometry array
		 */
		float[][] geo = subdivPoints[0];
		/*
		 * initialize 2x2 grid
		 */
		pt = boundary[0][0];
		geo[GRID_START + 5][0] = pt.x;
		geo[GRID_START + 5][1] = pt.y;
		geo[GRID_START + 5][2] = pt.z;
		
		pt = boundary[1][0];
		geo[GRID_START + 6][0] = pt.x;
		geo[GRID_START + 6][1] = pt.y;
		geo[GRID_START + 6][2] = pt.z;
		
		pt = boundary[1][1];
		geo[GRID_START + 2][0] = pt.x;
		geo[GRID_START + 2][1] = pt.y;
		geo[GRID_START + 2][2] = pt.z;
		
		pt = boundary[1][2];
		geo[GRID_START + 3][0] = pt.x;
		geo[GRID_START + 3][1] = pt.y;
		geo[GRID_START + 3][2] = pt.z;
		
		pt = boundary[1][3];
		geo[GRID_START + 7][0] = pt.x;
		geo[GRID_START + 7][1] = pt.y;
		geo[GRID_START + 7][2] = pt.z;
		
		pt = boundary[2][0];
		geo[GRID_START + 10][0] = pt.x;
		geo[GRID_START + 10][1] = pt.y;
		geo[GRID_START + 10][2] = pt.z;
		
		pt = boundary[3][0];
		geo[GRID_START + 9][0] = pt.x;
		geo[GRID_START + 9][1] = pt.y;
		geo[GRID_START + 9][2] = pt.z;
		
		pt = boundary[3][1];
		geo[GRID_START + 13][0] = pt.x;
		geo[GRID_START + 13][1] = pt.y;
		geo[GRID_START + 13][2] = pt.z;
		
		pt = boundary[3][2];
		geo[GRID_START + 12][0] = pt.x;
		geo[GRID_START + 12][1] = pt.y;
		geo[GRID_START + 12][2] = pt.z;
		
		pt = boundary[3][3];
		geo[GRID_START + 8][0] = pt.x;
		geo[GRID_START + 8][1] = pt.y;
		geo[GRID_START + 8][2] = pt.z;
//		// test crease stencils
//		patchStencil[1][7][1] = 1;
//		patchStencil[1][11][1] = 1;
//		patchStencil[1][13][1] = 1;
//		patchStencil[1][17][1] = 1;
		
		
//		patchStencil[1][6][1] = slate.corners[0].sharpness.get();
		patchStencil[1][8][1] = slate.corners[1].sharpness.get();
		patchStencil[1][16][1] = slate.corners[3].sharpness.get();
//		patchStencil[1][18][1] = slate.corners[3].sharpness.get();
		
		for (int corner = 0; corner < 2; corner ++) {
			final Point3f[] c = boundary[corner * 2];
			final int valence = c.length / 2 + 2;
			cornerStencil[1][valence - 3][corner][0] = slate.corners[corner * 2].sharpness.get() - 1;
//			cornerStencil[1][valence - 3][corner][0] = 0;
			
			
			
			
			
			final int n = c.length;
			final int start = corner * MAX_CORNER_LENGTH;
			
			/*
			 * initialize corner arrays
			 */
			for (int i = 1; i < n; i++) {
				final int index = start + (i % (n - 1));
				pt = c[i];
				geo[index][0] = pt.x;
				geo[index][1] = pt.y;
				geo[index][2] = pt.z;
			}
			if (n == 2) {
				pt = c[1];
				geo[start + 1][0] = pt.x;
				geo[start + 1][1] = pt.y;
				geo[start + 1][2] = pt.z;
			}
			
			// test crease stencils
//			int[][] fan = fanStencil[1][valence][corner];
//			for (int i = 0; i < fan.length; i += 2) {
//				fan[i][1] = 1;
//			}
		}
		
		/*
		 * subdivide maxLevel times
		 */
//		int[] stencilTypes = new int[11];
		for (int level = 1; level < depth; level++) {
			final int[][] stencil = patchStencil[level];
			final int[][] nextLevel = patchStencil[level + 1];
			final float[][] out = subdivPoints[level];
			final float[][] in = subdivPoints[level - 1];
			final int n = stencil.length - 2;
					
			/*
			 * apply stencils on rectangular inner grid
			 */
			for (int i = 2; i < n; i++) {
				final int[] s = stencil[i];
				final int outIndex = GRID_START + i;
//				stencilTypes[s[0]]++;
				switch (s[0]) {
				case EDGE_H:
					if (s[1] > 0) {
						// crease
						out[outIndex][0] = (in[s[7]][0] + in[s[8]][0]) * 0.5f;
						out[outIndex][1] = (in[s[7]][1] + in[s[8]][1]) * 0.5f;
						out[outIndex][2] = (in[s[7]][2] + in[s[8]][2]) * 0.5f;
						nextLevel[s[2]][1] = s[1] - 1;
						nextLevel[s[3]][1] = 0;
						nextLevel[s[4]][1] = s[1] - 1;
						nextLevel[s[5]][1] = 0;
						nextLevel[s[6]][1] = s[1] - 1;
						nextLevel[s[2]][0] = CREASE_5_7;
					} else {
						// smooth
						out[outIndex][0] = (in[s[7]][0] + in[s[8]][0]) * EDGE0 + ((in[s[9]][0] + in[s[10]][0]) + (in[s[11]][0] + in[s[12]][0])) * EDGE1;
						out[outIndex][1] = (in[s[7]][1] + in[s[8]][1]) * EDGE0 + ((in[s[9]][1] + in[s[10]][1]) + (in[s[11]][1] + in[s[12]][1])) * EDGE1;
						out[outIndex][2] = (in[s[7]][2] + in[s[8]][2]) * EDGE0 + ((in[s[9]][2] + in[s[10]][2]) + (in[s[11]][2] + in[s[12]][2])) * EDGE1;
						nextLevel[s[2]][1] = 0;
						nextLevel[s[3]][1] = 0;
						nextLevel[s[4]][1] = 0;
						nextLevel[s[5]][1] = 0;
						nextLevel[s[6]][1] = 0;
						nextLevel[s[2]][0] = POINT;
					}
					break;
				case EDGE_V:
					if (s[1] > 0) {
						// crease
						out[outIndex][0] = (in[s[7]][0] + in[s[8]][0]) * 0.5f;
						out[outIndex][1] = (in[s[7]][1] + in[s[8]][1]) * 0.5f;
						out[outIndex][2] = (in[s[7]][2] + in[s[8]][2]) * 0.5f;
						nextLevel[s[2]][1] = s[1] - 1;
						nextLevel[s[3]][1] = s[1] - 1;
						nextLevel[s[4]][1] = 0;
						nextLevel[s[5]][1] = s[1] - 1;
						nextLevel[s[6]][1] = 0;
						nextLevel[s[2]][0] = CREASE_4_6;
					} else {
						// smooth
						out[outIndex][0] = (in[s[7]][0] + in[s[8]][0]) * EDGE0 + ((in[s[9]][0] + in[s[10]][0]) + (in[s[11]][0] + in[s[12]][0])) * EDGE1;
						out[outIndex][1] = (in[s[7]][1] + in[s[8]][1]) * EDGE0 + ((in[s[9]][1] + in[s[10]][1]) + (in[s[11]][1] + in[s[12]][1])) * EDGE1;
						out[outIndex][2] = (in[s[7]][2] + in[s[8]][2]) * EDGE0 + ((in[s[9]][2] + in[s[10]][2]) + (in[s[11]][2] + in[s[12]][2])) * EDGE1;
						nextLevel[s[2]][1] = 0;
						nextLevel[s[3]][1] = 0;
						nextLevel[s[4]][1] = 0;
						nextLevel[s[5]][1] = 0;
						nextLevel[s[6]][1] = 0;
						nextLevel[s[2]][0] = POINT;
					}
					break;
				case POINT:
					if (s[1] > 0) {
						// corner
						out[outIndex][0] = in[s[3]][0];
						out[outIndex][1] = in[s[3]][1];
						out[outIndex][2] = in[s[3]][2];
						nextLevel[s[2]][1] = s[1] - 1;
					} else {
						// smooth
						out[outIndex][0] = in[s[3]][0] * VERTEX0 + ((in[s[4]][0] + in[s[6]][0]) + (in[s[5]][0] + in[s[7]][0])) * VERTEX1 + ((in[s[8]][0] + in[s[10]][0]) + (in[s[9]][0] + in[s[11]][0])) * VERTEX2;
						out[outIndex][1] = in[s[3]][1] * VERTEX0 + ((in[s[4]][1] + in[s[6]][1]) + (in[s[5]][1] + in[s[7]][1])) * VERTEX1 + ((in[s[8]][1] + in[s[10]][1]) + (in[s[9]][1] + in[s[11]][1])) * VERTEX2;
						out[outIndex][2] = in[s[3]][2] * VERTEX0 + ((in[s[4]][2] + in[s[6]][2]) + (in[s[5]][2] + in[s[7]][2])) * VERTEX1 + ((in[s[8]][2] + in[s[10]][2]) + (in[s[9]][2] + in[s[11]][2])) * VERTEX2;
						nextLevel[s[2]][1] = 0;
					}
					nextLevel[s[2]][0] = POINT;
					break;
				case FACE:
					out[outIndex][0] = ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * FACE0;
					out[outIndex][1] = ((in[s[1]][1] + in[s[3]][1]) + (in[s[2]][1] + in[s[4]][1])) * FACE0;
					out[outIndex][2] = ((in[s[1]][2] + in[s[3]][2]) + (in[s[2]][2] + in[s[4]][2])) * FACE0;
					break;
				case CREASE_4_5:
					out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[5]][0]) * CREASE1;
					out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[5]][1]) * CREASE1;
					out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[5]][2]) * CREASE1;
					nextLevel[s[2]][1] = s[1] - 1;
					nextLevel[s[2]][0] = s[1] > 1 ? CREASE_4_5 : POINT;
					break;
				case CREASE_4_6:
					out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[6]][0]) * CREASE1;
					out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[6]][1]) * CREASE1;
					out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[6]][2]) * CREASE1;
					nextLevel[s[2]][1] = s[1] - 1;
					nextLevel[s[2]][0] = s[1] > 1 ? CREASE_4_6 : POINT;
					break;
				case CREASE_4_7:
					out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[7]][0]) * CREASE1;
					out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[7]][1]) * CREASE1;
					out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[7]][2]) * CREASE1;
					nextLevel[s[2]][1] = s[1] - 1;
					nextLevel[s[2]][0] = s[1] > 1 ? CREASE_4_7 : POINT;
					break;
				case CREASE_5_6:
					out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[5]][0] + in[s[6]][0]) * CREASE1;
					out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[5]][1] + in[s[6]][1]) * CREASE1;
					out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[5]][2] + in[s[6]][2]) * CREASE1;
					nextLevel[s[2]][1] = s[1] - 1;
					nextLevel[s[2]][0] = s[1] > 1 ? CREASE_5_6 : POINT;
					break;
				case CREASE_5_7:
					out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[5]][0] + in[s[7]][0]) * CREASE1;
					out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[5]][1] + in[s[7]][1]) * CREASE1;
					out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[5]][2] + in[s[7]][2]) * CREASE1;
					nextLevel[s[2]][1] = s[1] - 1;
					nextLevel[s[2]][0] = s[1] > 1 ? CREASE_5_7 : POINT;
					break;
				case CREASE_6_7:
					out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[6]][0] + in[s[7]][0]) * CREASE1;
					out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[6]][1] + in[s[7]][1]) * CREASE1;
					out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[6]][2] + in[s[7]][2]) * CREASE1;
					nextLevel[s[2]][1] = s[1] - 1;
					nextLevel[s[2]][0] = s[1] > 1 ? CREASE_6_7 : POINT;
					break;
				}
			}
			
			/*
			 * apply stencils on corners and fans
			 */
			for (int corner = 0; corner < 2; corner++) {
				
				final int valence = boundary[corner * 2].length / 2 + 2;
				final int[] cs = cornerStencil[level][valence - 3][corner];
				final int outIndex = cs[1];
				if (cs[0] > 0) {
					out[outIndex][0] = in[cs[2]][0];
					out[outIndex][1] = in[cs[2]][1];
					out[outIndex][2] = in[cs[2]][2];
					cornerStencil[level + 1][valence - 3][corner][0] = cornerStencil[level][valence - 3][corner][0] - 1;
				} else {
					float a0 = 0;
					float a1 = 0;
					float a2 = 0;
					float b0 = 0;
					float b1 = 0;
					float b2 = 0;
					for (int p = 3; p < cs.length; p++) {
						a0 += in[cs[p]][0];
						a1 += in[cs[p]][1];
						a2 += in[cs[p++]][2];
						b0 += in[cs[p]][0];
						b1 += in[cs[p]][1];
						b2 += in[cs[p]][2];
					}
					final float ik = 1.0f / valence;			// TODO:
					final float bb = 1.5f * ik;					// precompute these values
					final float aa = 0.25f * ik;				// for each valence and
					final float cc = valence - 1.75f;			// use loopup table
					a0 *= aa;
					a1 *= aa;
					a2 *= aa;
					b0 *= bb;
					b1 *= bb;
					b2 *= bb;
					
					out[outIndex][0] = (a0 + b0 + in[cs[2]][0] * cc) * ik;
					out[outIndex][1] = (a1 + b1 + in[cs[2]][1] * cc) * ik;
					out[outIndex][2] = (a2 + b2 + in[cs[2]][2] * cc) * ik;
					cornerStencil[level + 1][valence - 3][corner][0] = 0;
				}

				final int[][] array = fanStencil[level][valence - 3][corner];
				final int[][] nextArray = fanStencil[level + 1][valence - 3][corner];
				final int m = array.length;
				for (int i = 0; i < m; i++) {
					final int oi = MAX_CORNER_LENGTH * corner + i;
					final int[] s = array[i];
					switch (s[0]) {
					case EDGE:
						if (s[1] > 0) {
							// crease
							out[oi][0] = (in[s[2]][0] + in[s[3]][0]) * 0.5f;
							out[oi][1] = (in[s[2]][1] + in[s[3]][1]) * 0.5f;
							out[oi][2] = (in[s[2]][2] + in[s[3]][2]) * 0.5f;
							nextArray[i][1] = s[1] - 1;
						} else {
							out[oi][0] = (in[s[2]][0] + in[s[3]][0]) * EDGE0 + ((in[s[4]][0] + in[s[5]][0]) + (in[s[6]][0] + in[s[7]][0])) * EDGE1;
							out[oi][1] = (in[s[2]][1] + in[s[3]][1]) * EDGE0 + ((in[s[4]][1] + in[s[5]][1]) + (in[s[6]][1] + in[s[7]][1])) * EDGE1;
							out[oi][2] = (in[s[2]][2] + in[s[3]][2]) * EDGE0 + ((in[s[4]][2] + in[s[5]][2]) + (in[s[6]][2] + in[s[7]][2])) * EDGE1;
							nextArray[i][1] = 0;
						}
						break;
					case FACE:
						out[oi][0] = ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * FACE0;
						out[oi][1] = ((in[s[1]][1] + in[s[3]][1]) + (in[s[2]][1] + in[s[4]][1])) * FACE0;
						out[oi][2] = ((in[s[1]][2] + in[s[3]][2]) + (in[s[2]][2] + in[s[4]][2])) * FACE0;
						break;
					}
				}
			}			
		}
		
		/*
		 * project vertices to limit surface
		 */
//		for (int i = 0; i < 11; i++) {
//			System.out.println("stencil " + i + ": " + stencilTypes[i]);
//		}
		/*
		 * apply limit stencils on rectangular inner grid
		 */
		int level = depth - 1;
		final int[][] stencil = patchLimitStencil[level];
		final float[][] out = limitPoints[level];
		final float[][] norm = limitNormals[level];
		final float[][] in = subdivPoints[level];
		final int n = stencil.length;
		float ax, ay, az, bx, by, bz, nx, ny, nz, nl;
		
		for (int i = 0; i < n; i++) {
			final int[] s = stencil[i];
			if (s == null) {
				continue;
			}
			final int outIndex = GRID_START + i;
			out[outIndex][0] = in[s[0]][0] * LIMIT0 + ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * LIMIT1 + ((in[s[5]][0] + in[s[7]][0]) + (in[s[6]][0] + in[s[8]][0])) * LIMIT2;
			out[outIndex][1] = in[s[0]][1] * LIMIT0 + ((in[s[1]][1] + in[s[3]][1]) + (in[s[2]][1] + in[s[4]][1])) * LIMIT1 + ((in[s[5]][1] + in[s[7]][1]) + (in[s[6]][1] + in[s[8]][1])) * LIMIT2;
			out[outIndex][2] = in[s[0]][2] * LIMIT0 + ((in[s[1]][2] + in[s[3]][2]) + (in[s[2]][2] + in[s[4]][2])) * LIMIT1 + ((in[s[5]][2] + in[s[7]][2]) + (in[s[6]][2] + in[s[8]][2])) * LIMIT2;
		
			ax = (in[s[2]][0] - in[s[4]][0]) * 4 + (in[s[6]][0] - in[s[5]][0]) + (in[s[7]][0] - in[s[8]][0]);
			ay = (in[s[2]][1] - in[s[4]][1]) * 4 + (in[s[6]][1] - in[s[5]][1]) + (in[s[7]][1] - in[s[8]][1]);
			az = (in[s[2]][2] - in[s[4]][2]) * 4 + (in[s[6]][2] - in[s[5]][2]) + (in[s[7]][2] - in[s[8]][2]);
			
			bx = (in[s[1]][0] - in[s[3]][0]) * 4 + (in[s[5]][0] - in[s[8]][0]) + (in[s[6]][0] - in[s[7]][0]);
			by = (in[s[1]][1] - in[s[3]][1]) * 4 + (in[s[5]][1] - in[s[8]][1]) + (in[s[6]][1] - in[s[7]][1]);
			bz = (in[s[1]][2] - in[s[3]][2]) * 4 + (in[s[5]][2] - in[s[8]][2]) + (in[s[6]][2] - in[s[7]][2]);
			
			nx = by * az - bz * ay;		// cross product
			ny = bz * ax - bx * az;
			nz = bx * ay - by * ax;
			nl = 1.0f / (float) sqrt(nx * nx + ny * ny + nz * nz);	// normalize
			norm[outIndex][0] = nx * nl;
			norm[outIndex][1] = ny * nl;
			norm[outIndex][2] = nz * nl;
		}
		
		/*
		 * apply limit stencils on corners
		 */
		for (int corner = 0; corner < 2; corner ++) {
			final int valence = boundary[corner * 2].length / 2 + 2;
			final int[] cps = cornerStencil[level][valence - 3][corner];
			final int[] cs = cornerLimitStencil[level][valence - 3][corner];
			
			final int outIndex = cs[1];
			
			if (cps[0] > 0) {	
				out[outIndex][0] = in[cs[2]][0];
				out[outIndex][1] = in[cs[2]][1];
				out[outIndex][2] = in[cs[2]][2];
			} else {
				float f0 = 0;
				float f1 = 0;
				float f2 = 0;
				float e0 = 0;
				float e1 = 0;
				float e2 = 0;
				for (int p = 3; p < cs.length; p++) {
					f0 += in[cs[p]][0];
					f1 += in[cs[p]][1];
					f2 += in[cs[p++]][2];
					e0 += in[cs[p]][0];
					e1 += in[cs[p]][1];
					e2 += in[cs[p]][2];
				}
				final float ik = 1.0f / (valence * (valence + 5));			// TODO:
				final float pointWeight = valence * valence;				// use loopup table?
				
				out[outIndex][0] = (e0 * 4 + f0 + in[cs[2]][0] * pointWeight) * ik;
				out[outIndex][1] = (e1 * 4 + f1 + in[cs[2]][1] * pointWeight) * ik;
				out[outIndex][2] = (e2 * 4 + f2 + in[cs[2]][2] * pointWeight) * ik;
			}
			/* normal */
			ax = 0;
			ay = 0;
			az = 0;
			bx = 0;
			by = 0;
			bz = 0;
			for (int j = 0; j < valence; j++) {
				int c2fi = j * 2 + 3;
				int c2ei = j * 2 + 2;
				if (c2ei == 2) {
					c2ei = cs.length - 1;
				}
				int c3fi = c2fi + 2;
				if (c3fi >= cs.length) {
					c3fi -= cs.length - 3;
				}
				int c3ei = c2ei + 2;
				if (c3ei >= cs.length) {
					c3ei -= cs.length - 3;
				}
				float ew = TANGENT_EDGE_WEIGHT[valence - 3][j];
				float fw = TANGENT_FACE_WEIGHT[valence - 3][j];
				ax += in[cs[c3fi]][0] * fw;
				ay += in[cs[c3fi]][1] * fw;
				az += in[cs[c3fi]][2] * fw;
				ax += in[cs[c3ei]][0] * ew;
				ay += in[cs[c3ei]][1] * ew;
				az += in[cs[c3ei]][2] * ew;
				bx += in[cs[c2fi]][0] * fw;
				by += in[cs[c2fi]][1] * fw;
				bz += in[cs[c2fi]][2] * fw;
				bx += in[cs[c2ei]][0] * ew;
				by += in[cs[c2ei]][1] * ew;
				bz += in[cs[c2ei]][2] * ew;
			}
			nx = by * az - bz * ay;		// cross product
			ny = bz * ax - bx * az;
			nz = bx * ay - by * ax;
			nl = 1.0f / (float) sqrt(nx * nx + ny * ny + nz * nz);	// normalize
			norm[outIndex][0] = nx * nl;
			norm[outIndex][1] = ny * nl;
			norm[outIndex][2] = nz * nl;
		}
		
		int dim = (1 << (depth - 1)) + 3;
		int i = 0;
		float[] ia = interleavedArray;
		int ydim = 2 * dim;
		for (int y = 2; y < dim - 3; y++) {
			int gsydim = GRID_START + ydim;
			int gsydim1 = gsydim + dim;
			for (int x = 2; x < dim - 3; x++) {
				ia[i++] = norm[gsydim + x][0];
				ia[i++] = norm[gsydim + x][1];
				ia[i++] = norm[gsydim + x][2];
				ia[i++] = out[gsydim + x][0];
				ia[i++] = out[gsydim + x][1];
				ia[i++] = out[gsydim + x][2];
				ia[i++] = norm[gsydim + x + 1][0];
				ia[i++] = norm[gsydim + x + 1][1];
				ia[i++] = norm[gsydim + x + 1][2];
				ia[i++] = out[gsydim + x + 1][0];
				ia[i++] = out[gsydim + x + 1][1];
				ia[i++] = out[gsydim + x + 1][2];
				ia[i++] = norm[gsydim1 + x + 1][0];
				ia[i++] = norm[gsydim1 + x + 1][1];
				ia[i++] = norm[gsydim1 + x + 1][2];
				ia[i++] = out[gsydim1 + x + 1][0];
				ia[i++] = out[gsydim1 + x + 1][1];
				ia[i++] = out[gsydim1 + x + 1][2];
				ia[i++] = norm[gsydim1 + x][0];
				ia[i++] = norm[gsydim1 + x][1];
				ia[i++] = norm[gsydim1 + x][2];
				ia[i++] = out[gsydim1 + x][0];
				ia[i++] = out[gsydim1 + x][1];	
				ia[i++] = out[gsydim1 + x][2];
			}
			ydim += dim;
		}
		
//		int j = 0;
//		while (j < i) {
//			System.out.println(ia[j++] + "," + ia[j++] + "," + ia[j++] + "    " + ia[j++] + "," + ia[j++] + "," + ia[j++]);
//		}
//		buffer.rewind();
//		buffer.put(ia, 0, i);
		
		return i;
	}
	
	private static int cornerStencilLength(int valence) {
		return max(valence * 2 - 5, 2);
	}
	
	private static int patchIndex(int level, int row, int column) {
		int dim = (1 << (level - 1)) + 3;
		if (row < 0) {
			row += dim;
		}
		if (column < 0) {
			column += dim;
		}
		
		if ((row == 0 && column == 0) || (row == dim - 1 && column == dim - 1)) {
			throw new IllegalArgumentException("level=" + level + " row=" + row + " column=" + column);
		} else if (row == 0 && column == 1) {
			return 0;
		} else if (row == 1 && column == 0) {
			return 1;
		} else if (row == dim - 1 && column == dim - 2) {
			return MAX_CORNER_LENGTH;
		} else if (row == dim - 2 && column == dim - 1) {
			return MAX_CORNER_LENGTH + 1;
		} else {
			return GRID_START + row * dim + column;
		}
//		if (row == 0) {
//			if (column == 1) {
//				return 0 * MAX_CORNER_LENGTH + 0;
////			} else if (column == dim - 2) {
////				return 1 * MAX_CORNER_LENGTH + 1;
//			} else if (column == 0) {
//				throw new IllegalArgumentException("level=" + level + " row=" + row + " column=" + column);
//			}
//		} else if (row == dim - 1) {
////			if (column == 1) {
////				return 3 * MAX_CORNER_LENGTH + 1;
//			if (column == dim - 2) {
//				return 2 * MAX_CORNER_LENGTH + 0;
//			} else if (column == dim - 1) {
//				throw new IllegalArgumentException("level=" + level + " row=" + row + " column=" + column);
//			}
//		} else if (column == 0) {
//			if (row == 1) {
//				return 0 * MAX_CORNER_LENGTH + 1;
////			} else if (row == dim - 2) {
////				return 3 * MAX_CORNER_LENGTH + 0;
//			}
//		} else if (column == dim - 1) {
////			if (row == 1) {
////				return 1 * MAX_CORNER_LENGTH + 0;
//			if (row == dim - 2) {
//				return 2 * MAX_CORNER_LENGTH + 1;
//			}
//		}
//		return GRID_START + row * dim + column;
	}
	
	/**
	 * Computes the geometry array index for the specified row/column and rotating the specified corner
	 * to the opper left side.
	 * @param corner 0 for the upper left (outer) corner, 1 for the lower right (inner) corner
	 * @param level
	 * @param row
	 * @param column
	 * @return
	 */
	private static int patchCornerIndex(int corner, int level, int row, int column) {
		int dim = (1 << (level - 1)) + 3;
		switch (corner) {
		case 0:
			return patchIndex(level, row, column);
		case 1:
			return patchIndex(level, dim - 1 - row, dim - 1 - column);
		default:
			throw new IllegalArgumentException("" + corner);
		}
	}
	
	/**
	 * Computes the geometry array index for a given corner (fan) element.
	 */
	private static int cornerIndex(int corner, int valence, int i) {
		if (i < 0 || i >= cornerStencilLength(valence)) {
			throw new IllegalArgumentException(Integer.toString(i));
		}
		return MAX_CORNER_LENGTH * corner + i;
		
//		int max = cornerStencilLength(valence);
//		int offset = MAX_CORNER_LENGTH * corner;
		
		
//		System.out.println("i=" + i + " " + (i == max));
//		if (i == 0) {
//			return offset;
//		} else if (i < 0) {
//			return offset + max + i;
//		} else if (i == max) {
//			return offset;
//		} else {
//			return offset + i;
//		}
	}
	
	private static int cornerIndex2(int corner, int level, int valence, int i) {
		int max = cornerStencilLength(valence);
		if (i == -2) {
			return patchCornerIndex(corner, level, 2, 1);
		} else if (i == -1) {
			return patchCornerIndex(corner, level, 2, 0);
		} else if (i == max) {
			return patchCornerIndex(corner, level, 0, 2);
		} else if (i == max + 1) {
			return patchCornerIndex(corner, level, 1, 2);
		} else {
			if (i == max - 1) {
				return cornerIndex(corner, valence, 0);
			} else {
				return cornerIndex(corner, valence, i + 1);
			}
		}
	}
	
	private class Tester {
		JFrame frame = new JFrame();
		int level = 4;
		int valence = 3;
		final String[] TYPE = { ".", "E", "-", "|", "F", "P", "C" };
		final int SIZE = 300;
		final int OFF = 250;
		int sx, sy;
		int[][][] looktable = new int[99][99][];
		
		JPanel panel = new JPanel() {
			public void paintComponent(Graphics g) {
//				System.out.println(sx + "/" + sy);
				super.paintComponent(g);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.translate(OFF, OFF);
				g.setFont(new Font("sans serif", Font.PLAIN, 8));
				int dim = getDimension(level);
				int dim1 = getDimension(level - 1);
				Point p = new Point();
				Point p1 = new Point();
//				int selectedIndex = getIndex(level, sy + 1, sx + 1);
				int[] selectedStencil = null;
				if (sx > -10 && sy > -10) {
					selectedStencil = looktable[sx + 10][sy + 10];
				}
				if (selectedStencil != null) {
					int stencil = selectedStencil[0];
					System.out.println(stencil);
					g.setColor(Color.RED);
					g.fillRect(sx * SIZE / (dim - 3), sy * SIZE / (dim - 3) - 8, 9, 9);
					int start = 0; 
					int end = 0;
					if (stencil == FACE) {
						start = 1;
						end = 5;
					} else if (stencil == POINT) {
						start = 3;
						end = 12;
					} else if (stencil == EDGE_H || stencil == EDGE_V) {
						start = 7;
						end = 13;
					} else if (stencil == EDGE) {
						start = 2;
						end = 8;
					} else if (stencil == 6) {
						start = 2;
						end = valence * 2 + 3;
					}
					for (int j = start; j < end; j++) {
						int idx = selectedStencil[j];
						g.drawString(Integer.toString(idx), -200, (j - start + 1) * 16);
						getPos(level - 1, idx, p1);
						if (p1.x > 0) {
							if (p1.x < dim1 - 2) {
								p1.x *= 2;
							} else {
								p1.x = dim + p1.x - dim1;
							}
						}
						if (p1.y > 0) {
							if (p1.y < dim1 - 2) {
								p1.y *= 2;
							} else {
								p1.y = dim + p1.y - dim1;
							}
						}
						g.drawRect(p1.x * SIZE / (dim - 3) - 2, p1.y * SIZE / (dim - 3) - 10, 12, 12);
//						System.out.println(idx);
					}
				}
				for (int i = 0, n = dim * dim; i < n; i++) {
					int index = GRID_START + i;
					getPos(level, index, p);
					int stencil = patchStencil[level][i][0];
					
					
					
					boolean hl = (p.x & 1) == 0 && (p.y & 1) == 0;
					g.setColor(hl ? Color.BLACK : Color.LIGHT_GRAY);
					
					
					g.drawString(TYPE[stencil], p.x * SIZE / (dim - 3), p.y * SIZE / (dim - 3));
					
					
				}
				for (int corner = 0; corner < 2; corner ++) {
					for (int i = 0; i < valence * 2 - 5; i++) {
						getFanPos(level, valence, corner, i, p);
						int stencil = fanStencil[level][valence - 3][corner][i][0];
						g.drawString(TYPE[stencil] + i, p.x * SIZE / (dim - 3), p.y * SIZE / (dim - 3));
					}
				}
//				g.drawString("" + , 0, -230);
			}
		};
		
		Tester() {
			fillLooktable();
			panel.setBackground(Color.WHITE);
			panel.addMouseWheelListener(new MouseWheelListener() {
				public void mouseWheelMoved(MouseWheelEvent e) {
					level += e.getWheelRotation();
					if (level < 1) level = 1;
					if (level > 4) level = 4;
					fillLooktable();
					panel.repaint();
				}
			});
			panel.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseMoved(MouseEvent e) {
					int dim = getDimension(level);
					int off = SIZE / (dim - 3) / 2;
					sx = (int) Math.floor(((e.getX() - OFF + off) * (dim - 3.0) + SIZE) / SIZE - 1);
					sy = (int) Math.floor(((e.getY() - OFF + 10 + off) * (dim - 3.0) + SIZE) / SIZE - 1);
					panel.repaint();
				}
			});
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800, 800);
			frame.add(panel);
			frame.setVisible(true);
		}
		
		private void fillLooktable() {
			for (int i = 0; i < looktable.length; i++) {
				for (int j = 0; j < looktable[i].length; j++) {
					looktable[i][j] = null;
				}
			}
			int dim = getDimension(level);
			Point p = new Point();
			
			for (int i = 0, n = dim * dim; i < n; i++) {
				int index = GRID_START + i;
				getPos(level, index, p);
				if (p.x > -10 && p.y > -10) {
					looktable[p.x + 10][p.y + 10] = patchStencil[level][i];
				}
			}
			
			for (int corner = 0; corner < 2; corner++) {
				for (int i = 0; i < valence * 2 - 5; i++) {
					getFanPos(level, valence, corner, i, p);
					looktable[p.x + 10][p.y + 10] = fanStencil[level][valence - 3][corner][i];
				}
			}
			
			looktable[10][10] = cornerStencil[level][valence - 3][0];
			looktable[10][10][0] = 6;
			
			looktable[dim + 7][dim + 7] = cornerStencil[level][valence - 3][1];
			looktable[dim + 7][dim + 7][0] = 6;
		}
		
		private void getPos(int level, int index, Point p) {
			int dim = getDimension(level);
			if (index >= GRID_START) {
				int row = (index - GRID_START) / dim;
				int column = (index - GRID_START) % dim;
				p.setLocation(column - 1, row - 1);
			} else {
				if (index < MAX_CORNER_LENGTH) {
					getFanPos(level, valence, 0, index, p);
				} else {
					getFanPos(level, valence, 1, index -  MAX_CORNER_LENGTH, p);
				}
			}
		}
		
		private void getFanPos(int level, int valence, int corner ,int index, Point p) {
			int[][] stencil = fanStencil[level][valence - 3][corner];
			int m = valence - 2;
			int dim = getDimension(level);
			if (index == 0) {
				index = valence * 2 - 5;
			}
			if (corner == 0) {
				if (index < m) {
					p.setLocation(-3, -3 + m - index);
				} else {
					p.setLocation(-3 - m + index, -3);
				}
			} else if (corner == 1) {
				if (index < m) {
					p.setLocation(dim, dim - m + index);
				} else {
					p.setLocation(dim + m - index, dim);
				}
			} else {
				throw new IllegalArgumentException();
			}
		}
		
		private int getIndex(int level, int row, int column) {
			int dim = getDimension(level);
			return GRID_START + row * dim + column;
		}
		
		private int getDimension(int level) {
			return ((1 << level)) + 3;
		}
	}
}
