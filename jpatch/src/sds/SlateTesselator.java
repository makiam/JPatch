package sds;

import com.sun.opengl.util.BufferUtil;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.*;

import static java.lang.Math.*;

public class SlateTesselator {
	private static final int UNUSED = 0;
	private static final int EDGE_H = 1;
	private static final int EDGE_V = 2;
	private static final int FACE = 3;
	private static final int POINT = 4;
	private static final int CREASE_4_5 = 5;
	private static final int CREASE_4_6 = 6;
	private static final int CREASE_4_7 = 7;
	private static final int CREASE_5_6 = 8;
	private static final int CREASE_5_7 = 9;
	private static final int CREASE_6_7 = 10;
	
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
	private static final int GRID_START = MAX_CORNER_LENGTH * 4;
	private final float[][][] subdivPoints = new float[MAX_SUBDIV][][];							// [level][index][0=x,1=y,2=z] index = row * dim + column
	private final float[][][] limitPoints = new float[MAX_SUBDIV][][];							// [level][index][0=x,1=y,2=z] index = row * dim + column
	private final float[][][] limitNormals = new float[MAX_SUBDIV][][];							// [level][index][0=x,1=y,2=z] index = row * dim + column
	private final int[][][] patchStencil = new int[MAX_SUBDIV][][];								// [level][index][stencil]
	private final int[][][][][] fanStencil = new int[MAX_SUBDIV][MAX_VALENCE - 2][4][][];		// [level][valence][corner][index][stencil]
	private final int[][][][] cornerStencil = new int[MAX_SUBDIV][MAX_VALENCE- 2][4][];			// [level][valence][corner][stencil]
	private final int[][][] patchLimitStencil = new int[MAX_SUBDIV][][];						// [level][index][stencil]
	private final int[][][][] cornerLimitStencil = new int[MAX_SUBDIV][MAX_VALENCE- 2][4][];			// [level][valence][corner][stencil]
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
	
	public SlateTesselator() {
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
				for (int corner = 0; corner < 4; corner++) {
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
								EDGE_H,
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
										EDGE_H,
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
					if (row < 2 || row > dim - 3) {
						if (column < 2 || column > dim - 3) {
							patchStencil[level][index] = new int[] { UNUSED, 0 };
							continue;
						}
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
					if ((row == 1 && column == 1) || (row == dim - 2 && column == 1) || (row == 1 && column == dim - 2) || (row == dim - 2 && column == dim - 2)) {
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
		
		pt = boundary[2][0];
		geo[GRID_START + 10][0] = pt.x;
		geo[GRID_START + 10][1] = pt.y;
		geo[GRID_START + 10][2] = pt.z;
		
		pt = boundary[3][0];
		geo[GRID_START + 9][0] = pt.x;
		geo[GRID_START + 9][1] = pt.y;
		geo[GRID_START + 9][2] = pt.z;
		
		// test crease stencils
//		patchStencil[1][7][1] = 1;
//		patchStencil[1][11][1] = 1;
//		patchStencil[1][13][1] = 1;
//		patchStencil[1][17][1] = 1;
		
		for (int corner = 0; corner < 4; corner++) {
			final int valence = boundary[corner].length / 2 + 2;
			cornerStencil[1][valence - 3][corner][0] = slate.corners[corner].sharpness.get() - 1;
			
			final Point3f[] c = boundary[corner];
			
			
			
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
			int[][] fan = fanStencil[1][valence][corner];
			for (int i = 0; i < fan.length; i += 2) {
				fan[i][1] = 1;
			}
		}
		
		/*
		 * subdivide maxLevel times
		 */
		
		
		
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
					if (s[1] > 0) {
						out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[5]][0]) * CREASE1;
						out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[5]][1]) * CREASE1;
						out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[5]][2]) * CREASE1;
						nextLevel[s[2]][1] = s[1] - 1;
						nextLevel[s[2]][0] = CREASE_4_5;
					} else {
						// smooth
						out[outIndex][0] = in[s[3]][0] * VERTEX0 + ((in[s[4]][0] + in[s[6]][0]) + (in[s[5]][0] + in[s[7]][0])) * VERTEX1 + ((in[s[8]][0] + in[s[10]][0]) + (in[s[9]][0] + in[s[11]][0])) * VERTEX2;
						out[outIndex][1] = in[s[3]][1] * VERTEX0 + ((in[s[4]][1] + in[s[6]][1]) + (in[s[5]][1] + in[s[7]][1])) * VERTEX1 + ((in[s[8]][1] + in[s[10]][1]) + (in[s[9]][1] + in[s[11]][1])) * VERTEX2;
						out[outIndex][2] = in[s[3]][2] * VERTEX0 + ((in[s[4]][2] + in[s[6]][2]) + (in[s[5]][2] + in[s[7]][2])) * VERTEX1 + ((in[s[8]][2] + in[s[10]][2]) + (in[s[9]][2] + in[s[11]][2])) * VERTEX2;
						nextLevel[s[2]][1] = 0;
						nextLevel[s[2]][0] = POINT;
					}
					break;
				case CREASE_4_6:
					if (s[1] > 0) {
						out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[6]][0]) * CREASE1;
						out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[6]][1]) * CREASE1;
						out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[6]][2]) * CREASE1;
						nextLevel[s[2]][1] = s[1] - 1;
						nextLevel[s[2]][0] = CREASE_4_6;
					} else {
						// smooth
						out[outIndex][0] = in[s[3]][0] * VERTEX0 + ((in[s[4]][0] + in[s[6]][0]) + (in[s[5]][0] + in[s[7]][0])) * VERTEX1 + ((in[s[8]][0] + in[s[10]][0]) + (in[s[9]][0] + in[s[11]][0])) * VERTEX2;
						out[outIndex][1] = in[s[3]][1] * VERTEX0 + ((in[s[4]][1] + in[s[6]][1]) + (in[s[5]][1] + in[s[7]][1])) * VERTEX1 + ((in[s[8]][1] + in[s[10]][1]) + (in[s[9]][1] + in[s[11]][1])) * VERTEX2;
						out[outIndex][2] = in[s[3]][2] * VERTEX0 + ((in[s[4]][2] + in[s[6]][2]) + (in[s[5]][2] + in[s[7]][2])) * VERTEX1 + ((in[s[8]][2] + in[s[10]][2]) + (in[s[9]][2] + in[s[11]][2])) * VERTEX2;
						nextLevel[s[2]][1] = 0;
						nextLevel[s[2]][0] = POINT;
					}
					break;
				case CREASE_4_7:
					if (s[1] > 0) {
						out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[7]][0]) * CREASE1;
						out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[7]][1]) * CREASE1;
						out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[7]][2]) * CREASE1;
						nextLevel[s[2]][1] = s[1] - 1;
						nextLevel[s[2]][0] = CREASE_4_7;
					} else {
						// smooth
						out[outIndex][0] = in[s[3]][0] * VERTEX0 + ((in[s[4]][0] + in[s[6]][0]) + (in[s[5]][0] + in[s[7]][0])) * VERTEX1 + ((in[s[8]][0] + in[s[10]][0]) + (in[s[9]][0] + in[s[11]][0])) * VERTEX2;
						out[outIndex][1] = in[s[3]][1] * VERTEX0 + ((in[s[4]][1] + in[s[6]][1]) + (in[s[5]][1] + in[s[7]][1])) * VERTEX1 + ((in[s[8]][1] + in[s[10]][1]) + (in[s[9]][1] + in[s[11]][1])) * VERTEX2;
						out[outIndex][2] = in[s[3]][2] * VERTEX0 + ((in[s[4]][2] + in[s[6]][2]) + (in[s[5]][2] + in[s[7]][2])) * VERTEX1 + ((in[s[8]][2] + in[s[10]][2]) + (in[s[9]][2] + in[s[11]][2])) * VERTEX2;
						nextLevel[s[2]][1] = 0;
						nextLevel[s[2]][0] = POINT;
					}
					break;
				case CREASE_5_6:
					if (s[1] > 0) {
						out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[5]][0] + in[s[6]][0]) * CREASE1;
						out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[5]][1] + in[s[6]][1]) * CREASE1;
						out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[5]][2] + in[s[6]][2]) * CREASE1;
						nextLevel[s[2]][1] = s[1] - 1;
						nextLevel[s[2]][0] = CREASE_5_6;
					} else {
						// smooth
						out[outIndex][0] = in[s[3]][0] * VERTEX0 + ((in[s[4]][0] + in[s[6]][0]) + (in[s[5]][0] + in[s[7]][0])) * VERTEX1 + ((in[s[8]][0] + in[s[10]][0]) + (in[s[9]][0] + in[s[11]][0])) * VERTEX2;
						out[outIndex][1] = in[s[3]][1] * VERTEX0 + ((in[s[4]][1] + in[s[6]][1]) + (in[s[5]][1] + in[s[7]][1])) * VERTEX1 + ((in[s[8]][1] + in[s[10]][1]) + (in[s[9]][1] + in[s[11]][1])) * VERTEX2;
						out[outIndex][2] = in[s[3]][2] * VERTEX0 + ((in[s[4]][2] + in[s[6]][2]) + (in[s[5]][2] + in[s[7]][2])) * VERTEX1 + ((in[s[8]][2] + in[s[10]][2]) + (in[s[9]][2] + in[s[11]][2])) * VERTEX2;
						nextLevel[s[2]][1] = 0;
						nextLevel[s[2]][0] = POINT;
					}
					break;
				case CREASE_5_7:
					if (s[1] > 0) {
						out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[5]][0] + in[s[7]][0]) * CREASE1;
						out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[5]][1] + in[s[7]][1]) * CREASE1;
						out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[5]][2] + in[s[7]][2]) * CREASE1;
						nextLevel[s[2]][1] = s[1] - 1;
						nextLevel[s[2]][0] = CREASE_5_7;
					} else {
						// smooth
						out[outIndex][0] = in[s[3]][0] * VERTEX0 + ((in[s[4]][0] + in[s[6]][0]) + (in[s[5]][0] + in[s[7]][0])) * VERTEX1 + ((in[s[8]][0] + in[s[10]][0]) + (in[s[9]][0] + in[s[11]][0])) * VERTEX2;
						out[outIndex][1] = in[s[3]][1] * VERTEX0 + ((in[s[4]][1] + in[s[6]][1]) + (in[s[5]][1] + in[s[7]][1])) * VERTEX1 + ((in[s[8]][1] + in[s[10]][1]) + (in[s[9]][1] + in[s[11]][1])) * VERTEX2;
						out[outIndex][2] = in[s[3]][2] * VERTEX0 + ((in[s[4]][2] + in[s[6]][2]) + (in[s[5]][2] + in[s[7]][2])) * VERTEX1 + ((in[s[8]][2] + in[s[10]][2]) + (in[s[9]][2] + in[s[11]][2])) * VERTEX2;
						nextLevel[s[2]][1] = 0;
						nextLevel[s[2]][0] = POINT;
					}
					break;
				case CREASE_6_7:
					if (s[1] > 0) {
						out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[6]][0] + in[s[7]][0]) * CREASE1;
						out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[6]][1] + in[s[7]][1]) * CREASE1;
						out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[6]][2] + in[s[7]][2]) * CREASE1;
						nextLevel[s[2]][1] = s[1] - 1;
						nextLevel[s[2]][0] = CREASE_6_7;
					} else {
						// smooth
						out[outIndex][0] = in[s[3]][0] * VERTEX0 + ((in[s[4]][0] + in[s[6]][0]) + (in[s[5]][0] + in[s[7]][0])) * VERTEX1 + ((in[s[8]][0] + in[s[10]][0]) + (in[s[9]][0] + in[s[11]][0])) * VERTEX2;
						out[outIndex][1] = in[s[3]][1] * VERTEX0 + ((in[s[4]][1] + in[s[6]][1]) + (in[s[5]][1] + in[s[7]][1])) * VERTEX1 + ((in[s[8]][1] + in[s[10]][1]) + (in[s[9]][1] + in[s[11]][1])) * VERTEX2;
						out[outIndex][2] = in[s[3]][2] * VERTEX0 + ((in[s[4]][2] + in[s[6]][2]) + (in[s[5]][2] + in[s[7]][2])) * VERTEX1 + ((in[s[8]][2] + in[s[10]][2]) + (in[s[9]][2] + in[s[11]][2])) * VERTEX2;
						nextLevel[s[2]][1] = 0;
						nextLevel[s[2]][0] = POINT;
					}
					break;
				}
			}
			
			/*
			 * apply stencils on corners and fans
			 */
			for (int corner = 0; corner < 4; corner++) {
				
				final int valence = boundary[corner].length / 2 + 2;
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
					case EDGE_H:
					case EDGE_V:	// fallthrough intended!
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
		for (int corner = 0; corner < 4; corner++) {
			final int valence = boundary[corner].length / 2 + 2;
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
		if (row == 0) {
			if (column == 1) {
				return 0 * MAX_CORNER_LENGTH + 0;
			} else if (column == dim - 2) {
				return 1 * MAX_CORNER_LENGTH + 1;
			} else if (column == 0 || column == dim - 1) {
				throw new IllegalArgumentException("level=" + level + " row=" + row + " column=" + column);
			}
		} else if (row == dim - 1) {
			if (column == 1) {
				return 3 * MAX_CORNER_LENGTH + 1;
			} else if (column == dim - 2) {
				return 2 * MAX_CORNER_LENGTH + 0;
			} else if (column == 0 || column == dim - 1) {
				throw new IllegalArgumentException("level=" + level + " row=" + row + " column=" + column);
			}
		} else if (column == 0) {
			if (row == 1) {
				return 0 * MAX_CORNER_LENGTH + 1;
			} else if (row == dim - 2) {
				return 3 * MAX_CORNER_LENGTH + 0;
			}
		} else if (column == dim - 1) {
			if (row == 1) {
				return 1 * MAX_CORNER_LENGTH + 0;
			} else if (row == dim - 2) {
				return 2 * MAX_CORNER_LENGTH + 1;
			}
		}
		return GRID_START + row * dim + column;
	}
	
	private static int patchCornerIndex(int corner, int level, int row, int column) {
		int dim = (1 << (level - 1)) + 3;
		switch (corner) {
		case 0:
			return patchIndex(level, row, column);
		case 1:
			return patchIndex(level, column, dim - 1 - row);
		case 2:
			return patchIndex(level, dim - 1 - row, dim - 1 - column);
		case 3:
			return patchIndex(level, dim - 1 - column, row);
		default:
			throw new IllegalArgumentException("corner > 3 :" + corner);
		}
	}
	
	private static int cornerIndex(int corner, int valence, int i) {
		int max = cornerStencilLength(valence);
		int offset = MAX_CORNER_LENGTH * corner;
		i--;
		if (i == -1) {
			return offset;
		} else if (i < -1) {
			return offset + max + 1 + i;
		} else if (i == max - 1) {
			return offset;
		} else {
			return offset + i + 1;
		}
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
}