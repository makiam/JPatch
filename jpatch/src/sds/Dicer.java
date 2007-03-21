package sds;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.FloatBuffer;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.*;

import jpatch.auxilary.ArrayImage;
import jpatch.auxilary.ImagePanel;
import jpatch.boundary.settings.RealtimeRendererSettings;
import jpatch.boundary.settings.Settings;

import static java.lang.Math.*;
import static sds.SdsConstants.*;
import static sds.SdsWeights.*;

public class Dicer {
	private static RealtimeRendererSettings RENDERER_SETTINGS = Settings.getInstance().realtimeRenderer;
	
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
	
	
	
//	private static final float[][] TANGENT_FACE_WEIGHT = new float[MAX_VALENCE - 2][];			// [valence][index]
//	private static final float[][] TANGENT_EDGE_WEIGHT = new float[MAX_VALENCE - 2][];			// [valence][index]

	private static final int MAX_CORNER_LENGTH = MAX_VALENCE * 2 - 5;
	public static final int GRID_START = MAX_CORNER_LENGTH * 2;
	private final float[][][] subdivPoints = new float[MAX_SUBDIV][][];							// [level][index][0=x,1=y,2=z] index = row * dim + column
	private final float[][][] limitPoints = new float[MAX_SUBDIV][][];							// [level][index][0=x,1=y,2=z] index = row * dim + column
	private final float[][][] limitNormals = new float[MAX_SUBDIV][][];							// [level][index][0=x,1=y,2=z] index = row * dim + column
	private final int[][][] patchStencil = new int[MAX_SUBDIV][][];								// [level][index][stencil]
	private final int[][][][][] fanStencil = new int[MAX_SUBDIV][MAX_VALENCE - 2][2][][];		// [level][valence - 3][corner][index][stencil]
	private final int[][][][] cornerStencil = new int[MAX_SUBDIV][MAX_VALENCE- 2][2][];			// [level][valence - 3][corner][stencil]
	private final int[][][] patchLimitStencil = new int[MAX_SUBDIV][][];						// [level][index][stencil]
	private final int[][][][] cornerLimitStencil = new int[MAX_SUBDIV][MAX_VALENCE- 2][2][];	// [level][valence - 3][corner][stencil]
//	private final float[][] quadVertexArrays = new float[MAX_SUBDIV][];
//	private final float[][] quadNormalArrays = new float[MAX_SUBDIV][];
	
//	private final float[][][][][][] fastLimitWeights = new float[MAX_SUBDIV][MAX_FAST_VALENCE - 2][MAX_FAST_VALENCE - 2][][][];    // [level][valence0][valence2][index][corner][vertex];
//	private final float[][][][][][] fastTangent0Weights = new float[MAX_SUBDIV][MAX_FAST_VALENCE - 2][MAX_FAST_VALENCE - 2][][][]; // [level][valence0][valence2][index][corner][vertex];
//	private final float[][][][][][] fastTangent1Weights = new float[MAX_SUBDIV][MAX_FAST_VALENCE - 2][MAX_FAST_VALENCE - 2][][][]; // [level][valence0][valence2][index][corner][vertex];
	
	
	int[][][] rim0 = new int[MAX_SUBDIV][][];								//[level][side][index] outer grid rim
	int[][][] rim1 = new int[MAX_SUBDIV][][];								//[level][side][index] inner grid rim
	int[][][][] rimTriangles = new int[MAX_SUBDIV][MAX_SUBDIV][][]; 		// [thisLevel][pairLevel][side][index]
	int[][][][] rimTriangleNormals = new int[MAX_SUBDIV][MAX_SUBDIV][][]; 	// [thisLevel][pairLevel][side][index]
	
//	static {
//		for (int valence = 3; valence <= MAX_VALENCE; valence++) {
//			int i = valence - 3;
//			TANGENT_FACE_WEIGHT[i] = new float[valence];
//			TANGENT_EDGE_WEIGHT[i] = new float[valence];
//			float An = (float) (1 + cos(2 * PI / valence) + cos(PI / valence) * sqrt(2 * (9 + cos(2 * PI / valence))));
//			for (int j = 0; j < valence; j++) {
//				TANGENT_EDGE_WEIGHT[i][j] = (float) (An * cos(2 * PI  * j / valence));
//				TANGENT_FACE_WEIGHT[i][j] = (float) (cos(2 * PI * j / valence) + cos(2 * PI * (j + 1) / valence));
//			}
//		}
//	}
	
//	public static void main(String[] args) {
//		Dicer slateTesselator = new Dicer();
//		slateTesselator.new Tester(); 
//	}
	
	public Dicer() {
		for (int level = 0; level < MAX_SUBDIV; level++) {
			final int dim = ((1 << level)) + 3;
			
			System.out.println("level = " + level + " dim = " + dim);
			if (level == 0) {
				rim0[0] = new int[4][2];
				rim0[0][0] = new int[] {5, 6};
				rim0[0][1] = new int[] {6, 10};
				rim0[0][2] = new int[] {10, 9};
				rim0[0][3] = new int[] {9, 5};
				rim1[0] = new int[0][];
				rimTriangles[0][0] = new int[4][0];
				rimTriangleNormals[0][0] = new int[4][0];
//				quadVertexArrays[0] = new float[15];
//				quadNormalArrays[0] = new float[15];
			} else {
//				quadVertexArrays[level] = new float[(dim - 3) * (dim - 3) * 12 + 3];
//				quadNormalArrays[level] = new float[(dim - 3) * (dim - 3) * 12 + 3];
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
					rimTriangleNormals[level][pairLevel] = new int[4][];
					int[] tmpTriangles = new int[dim * 6];
					int[] tmpNormals = new int[dim * 6];
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
							int innerNormal = ((side + 2) % 4) * 3;
							if (ii >= rim1[level][side].length) {
								ii = rim1[level][side].length - 1;
								innerNormal = ((side + 3) % 4) * 3;
							}
							tmpNormals[j] = side * 3;
							tmpTriangles[j++] = rim0[level][side][i] + GRID_START;
							tmpNormals[j] = ((side + 1) % 4) * 3;
							tmpTriangles[j++] = rim0[level][side][i + step] + GRID_START;
							tmpNormals[j] = innerNormal;
							tmpTriangles[j++] = rim1[level][side][ii] + GRID_START;
						}
						for (int i = 0; i < (dim - 5); i++) {
							int ii = ((i + (step / 2) + 1) / step) * step;
							int outerNormal = ((i + 1) % step < step / 2) ? side * 3 : ((side + 1) % 4) * 3;
							tmpNormals[j] = outerNormal;
							tmpTriangles[j++] = rim0[level][side][ii] + GRID_START;
							tmpNormals[j] = ((side + 2) % 4) * 3;
							tmpTriangles[j++] = rim1[level][side][i + 1] + GRID_START;
							tmpNormals[j] = ((side + 3) % 4) * 3;
							tmpTriangles[j++] = rim1[level][side][i] + GRID_START;
						}
						rimTriangles[level][pairLevel][side] = new int[j];
						rimTriangleNormals[level][pairLevel][side] = new int[j];
						System.arraycopy(tmpTriangles, 0, rimTriangles[level][pairLevel][side], 0, j);
						System.arraycopy(tmpNormals, 0, rimTriangleNormals[level][pairLevel][side], 0, j);
					}
				}
			}
			
			subdivPoints[level] = new float[dim * dim + GRID_START][3];
			limitPoints[level] = new float[dim * dim + GRID_START][3];
			limitNormals[level] = new float[dim * dim + GRID_START][12];
			
//			System.out.println("geometryarray level " + level + " size=" + geometryArray[level].length + " (" + dim + "x" + dim + " + " + GRID_START + ")");
//			if (level == 0) {
//				continue;
//			}
			
			/*
			 * populate subdivision stencil tables for corners
			 */
			for (int valence = 3; valence <= MAX_VALENCE; valence++) {
				for (int corner = 0; corner < 2; corner ++) {
					cornerStencil[level][valence - 3][corner] = new int[valence * 2 + 6];
					cornerStencil[level][valence - 3][corner][0] = 0;							// corner sharpness
					cornerStencil[level][valence - 3][corner][1] = 0;							// crease sharpness
					cornerStencil[level][valence - 3][corner][2] = 0;							// index of 1st crease edge
					cornerStencil[level][valence - 3][corner][3] = 0;							// index of 2nd craase edge
					cornerStencil[level][valence - 3][corner][4] = patchCornerIndex(corner, level + 1, 1, 1);	// target!
					cornerStencil[level][valence - 3][corner][5] = patchCornerIndex(corner, level, 1, 1);
					cornerStencil[level][valence - 3][corner][6] = patchCornerIndex(corner, level, 0, 2);
					cornerStencil[level][valence - 3][corner][7] = patchCornerIndex(corner, level, 1, 2);
					cornerStencil[level][valence - 3][corner][8] = patchCornerIndex(corner, level, 2, 2);
					cornerStencil[level][valence - 3][corner][9] = patchCornerIndex(corner, level, 2, 1);
					cornerStencil[level][valence - 3][corner][10] = patchCornerIndex(corner, level, 2, 0);
					for (int i = 0, n = valence * 2 - 5; i < n; i++) {
						int index = i + 1;
						if (index == n) {
							index = 0;
						}
						cornerStencil[level][valence - 3][corner][i + 11] = cornerIndex(corner, valence, index);
					}

					cornerLimitStencil[level][valence - 3][corner] = new int[valence * 2 + 6];
					cornerLimitStencil[level][valence - 3][corner][0] = 0;							// corner sharpness
					cornerLimitStencil[level][valence - 3][corner][1] = 0;							// crease sharpness
					cornerLimitStencil[level][valence - 3][corner][2] = 0;							// index of 1st crease edge
					cornerLimitStencil[level][valence - 3][corner][3] = 0;							// index of 2nd craase edge
					cornerLimitStencil[level][valence - 3][corner][4] = patchCornerIndex(corner, level + 1, 1, 1);	// target!
					cornerLimitStencil[level][valence - 3][corner][5] = patchCornerIndex(corner, level + 1, 1, 1);
					cornerLimitStencil[level][valence - 3][corner][6] = patchCornerIndex(corner, level + 1, 0, 2);
					cornerLimitStencil[level][valence - 3][corner][7] = patchCornerIndex(corner, level + 1, 1, 2);
					cornerLimitStencil[level][valence - 3][corner][8] = patchCornerIndex(corner, level + 1, 2, 2);
					cornerLimitStencil[level][valence - 3][corner][9] = patchCornerIndex(corner, level + 1, 2, 1);
					cornerLimitStencil[level][valence - 3][corner][10] = patchCornerIndex(corner, level + 1, 2, 0);
					for (int i = 0, n = valence * 2 - 5; i < n; i++) {
						int index = i + 1;
						if (index == n) {
							index = 0;
						}
						cornerLimitStencil[level][valence - 3][corner][i + 11] = cornerIndex(corner, valence, index);
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
					
					/*
					 * mask out unused elements (these points are handled in the corner and fan tables)
					 */
					if ((row < 2 && column < 2) || (row > dim - 3 && column > dim - 3)) {
						patchStencil[level][index] = new int[] { UNUSED, 0 };
						continue;
					} 
					
					int nextRow = row * 2 - 1;
					int nextColumn = column * 2 - 1;
					
//					int nextRow = (row == 0) ? 0 : (row == dim - 1) ? dim * 2 - 4 : row * 2 - 1;
//					int nextColumn = (column == 0) ? 0 : (column == dim - 1) ? dim * 2 - 4 : column * 2 - 1;
					int nextLevelIndex_0 = getStencilIndex(level + 1, nextRow, nextColumn);
//					if (nextLevelIndex_0 == 0) throw new IllegalStateException("level=" + level + " dim=" + dim + " row=" + row + " column=" + column + " nextRow=" + nextRow + " nextColumn=" + nextColumn);
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
							patchIndex(level + 1, row + 1, column - 1),
//							quadNormalArrays[level].length - 3,				// upper left corner in output quad array
//							quadNormalArrays[level].length - 3,				// upper right corner in output quad array
//							quadNormalArrays[level].length - 3,				// lower right corner in output quad array
//							quadNormalArrays[level].length - 3				// lower left corner in output quad array
					};
				}
			}
		
		
//			/*
//			 * Setup quad-array indexes in patchLimitStencils
//			 */
//			final int start = 1;
//			final int end = dim - 2;
//			int ydim = start * dim;
//			System.out.println("level=" + level + " dim=" + dim + " ydim=" + ydim + " size=" + patchLimitStencil[level].length);
//			int index = 0;
//			int gsydim = ydim;
//			for (int y = start; y < end; y++) {
//				int gsydim1 = gsydim + dim;
////				System.out.println("gsydim=" + gsydim + " gsydim1=" + gsydim1);
//				for (int x = start; x < end; x++) {
//					System.out.println("x=" + x + " y=" + y + " upperLeft=" + (gsydim + x) + " lowerRight=" + (gsydim1 + x + 1));
//					int ul = gsydim + x;
//					
//					int[] upperLeft = patchLimitStencil[level][gsydim + x];
//					int[] upperRight = patchLimitStencil[level][gsydim + x + 1];
//					int[] lowerRight = patchLimitStencil[level][gsydim1 + x + 1];
//					int[] lowerLeft = patchLimitStencil[level][gsydim1 + x];
//					if (upperLeft != null) {
//						upperLeft[9] = index;
//						System.out.print(index + " ");
//					}
//					index += 3;
//					if (upperRight != null) {
//						upperRight[10] = index;
//						System.out.print(index + " ");
//					}
//					index += 3;
//					if (lowerRight != null) {
//						lowerRight[11] = index;
//						System.out.print(index + " ");
//					}
//					index += 3;
//					if (lowerLeft != null) {
//						lowerLeft[12] = index;
//						System.out.print(index + " ");
//					}
//					index += 3;
//					System.out.println();
//				}
//				gsydim += dim;
//			}
//			System.out.println();
////			if (level == 1) System.exit(0);
		}
		
		
//		/*
//		 * compute fast-weight arrays (basis functions)
//		 */
//		for (int depth = 0; depth <MAX_SUBDIV; depth++) {
//			System.out.println("creating basisfunction tables for depth " + depth);
//			final int dim = (1 << depth) + 3;
//			final int size = dim * dim;
//			
//			for (int valence0 = 3; valence0 <= MAX_FAST_VALENCE; valence0++) {
//				for (int valence1 = 3; valence1 <= MAX_FAST_VALENCE; valence1++) {
//					fastLimitWeights[depth][valence0 - 3][valence1 - 3] = new float[size][4][];
//					fastTangent0Weights[depth][valence0 - 3][valence1 - 3] = new float[size][4][];
//					fastTangent1Weights[depth][valence0 - 3][valence1 - 3] = new float[size][4][];
//					for (int i = 0; i < size; i++) {
//						for (int corner = 0; corner < 4; corner++) {
//							final int val = (corner == 0) ? valence0 : (corner == 2) ? valence1 : 4;
//							final int n = val * 2 - 4;
//							fastLimitWeights[depth][valence0 - 3][valence1 - 3][i][corner] = new float[n];
//							fastTangent0Weights[depth][valence0 - 3][valence1 - 3][i][corner] = new float[n];
//							fastTangent1Weights[depth][valence0 - 3][valence1 - 3][i][corner] = new float[n];
//						}
//					}
//				}
//			}
//			for (int valence0 = 3; valence0 <= MAX_FAST_VALENCE; valence0++) {
//				for (int valence1 = 3; valence1 <= MAX_FAST_VALENCE; valence1++) {
//					final int[] valence = new int[] { valence0, valence1 };
//					
//					for (int corner = 0; corner < 4; corner++) {
//						int val = (corner == 0) ? valence0 : (corner == 2) ? valence1 : 4;
//						final int n = val * 2 - 4;
//						for (int vertex = 0; vertex < n; vertex++) {
//							/* clear geometry array (x) */
//							for (int i = 0; i < subdivPoints[0].length; i++) {
//								subdivPoints[0][i][0] = 0;
//							}
//							
//							/* set vertex to 1 and dice */
//							if (vertex == 0) {
//								switch (corner) {
//								case 0:
//									subdivPoints[0][GRID_START + 5][0] = 1;
//									break;
//								case 1:
//									subdivPoints[0][GRID_START + 6][0] = 1;
//									break;
//								case 2:
//									subdivPoints[0][GRID_START + 10][0] = 1;
//									break;
//								case 3:
//									subdivPoints[0][GRID_START + 9][0] = 1;
//									break;
//								}
//							} else {
//								switch (corner) {
//								case 0:
//									subdivPoints[0][vertex - 1][0] = 1;
//									if (n == 2) {
//										subdivPoints[0][vertex][0] = 1;
//									}
//									break;
//								case 1:
//									switch (vertex) {
//									case 1:
//										subdivPoints[0][GRID_START + 2][0] = 1;
//										break;
//									case 2:
//										subdivPoints[0][GRID_START + 3][0] = 1;
//										break;
//									case 3:
//										subdivPoints[0][GRID_START + 7][0] = 1;
//										break;
//									}
//									break;
//								case 2:
//									subdivPoints[0][MAX_CORNER_LENGTH + vertex - 1][0] = 1;
//									if (n == 2) {
//										subdivPoints[0][MAX_CORNER_LENGTH + vertex][0] = 1;
//									}
//									break;
//								case 3:
//									switch (vertex) {
//									case 1:
//										subdivPoints[0][GRID_START + 13][0] = 1;
//										break;
//									case 2:
//										subdivPoints[0][GRID_START + 12][0] = 1;
//										break;
//									case 3:
//										subdivPoints[0][GRID_START + 8][0] = 1;
//										break;
//									}
//									break;
//								}
//							}
//							quickDice(valence, depth + 1);
//							
//							if (depth == 2) {
//								System.out.println("\ncorner=" + corner + " vertex=" + vertex);
//							}
//							/* copy result to fast weight tables */
//							for (int i = 0; i < size; i++) {
//								fastLimitWeights[depth][valence0 - 3][valence1 - 3][i][corner][vertex] = limitPoints[depth][i + GRID_START][0];
//								if (depth == 2) {
//									if (i % dim == 0) {
//										System.out.println();
//									}
//									System.out.print(limitPoints[depth][i + GRID_START][1] + " ");
//								}
//								fastTangent0Weights[depth][valence0 - 3][valence1 - 3][i][corner][vertex] = limitPoints[depth][i + GRID_START][1];
//								fastTangent1Weights[depth][valence0 - 3][valence1 - 3][i][corner][vertex] = limitPoints[depth][i + GRID_START][2];
//							}
//						}
//					}
//				}
//			}
//		}
//		System.out.println("done.");
//		System.exit(0);
	}
	
	private int getStencilIndex(int level, int row, int column) {
		final int dim = ((1 << level)) + 3;
		if (row < 0 | row > dim - 1 | column < 0 | column > dim -1) {
			return 0;
		}
		return row * dim + column;
	}
	
	public float[][] getSubdivVertices(int level) {
		return subdivPoints[level];
	}
	
	public int[][] getStencils(int level) {
		return patchStencil[level];
	}
	
	public float[][] getLimitVertices(int level) {
		return limitPoints[level];
	}
	
	public float[][] getLimitNormals(int level) {
		return limitNormals[level];
	}
	
//	public float[] getQuadVertexArray(int level) {
//		return quadVertexArrays[level];
//	}
//	
//	public float[] getQuadNormalArray(int level) {
//		return quadNormalArrays[level];
//	}
	
	public int[] getRimTriangles(int level, int side, int pairLevel) {
		if (pairLevel > level) {
			pairLevel = level;
		}
		return rimTriangles[level][pairLevel][side];
	}
	
	public int[] getRimTriangleNormals(int level, int side, int pairLevel) {
		if (pairLevel > level) {
			pairLevel = level;
		}
		return rimTriangleNormals[level][pairLevel][side];
	}
	
	public int[] getRim(int level, int side) {
		return rim0[level][side];
	}
	
	public int dice(final Slate2 slate, final int depth) {
		if (depth == 1) {
//			float[] va = quadVertexArrays[1];
//			float[] na = quadNormalArrays[1];
			float[][] lp = limitPoints[0];
			float[][] ln = limitNormals[0];
			Level2Vertex v0 = slate.corners[0][0].vertex;
			Level2Vertex v1 = slate.corners[1][0].vertex;
			Level2Vertex v2 = slate.corners[2][0].vertex;
			Level2Vertex v3 = slate.corners[3][0].vertex;
		
			
			lp[5 + GRID_START][0] = v0.projectedLimit.x;
			lp[5 + GRID_START][1] = v0.projectedLimit.y;
			lp[5 + GRID_START][2] = v0.projectedLimit.z;
			lp[6 + GRID_START][0] = v1.projectedLimit.x;
			lp[6 + GRID_START][1] = v1.projectedLimit.y;
			lp[6 + GRID_START][2] = v1.projectedLimit.z;
			lp[10 + GRID_START][0] = v2.projectedLimit.x;
			lp[10 + GRID_START][1] = v2.projectedLimit.y;
			lp[10 + GRID_START][2] = v2.projectedLimit.z;
			lp[9 + GRID_START][0] = v3.projectedLimit.x;
			lp[9 + GRID_START][1] = v3.projectedLimit.y;
			lp[9 + GRID_START][2] = v3.projectedLimit.z;
			ln[5 + GRID_START][0] = ln[5 + GRID_START][3] = ln[5 + GRID_START][6] = ln[5 + GRID_START][9] = v0.projectedNormal.x;
			ln[5 + GRID_START][1] = ln[5 + GRID_START][4] = ln[5 + GRID_START][7] = ln[5 + GRID_START][10] = v0.projectedNormal.y;
			ln[5 + GRID_START][2] = ln[5 + GRID_START][5] = ln[5 + GRID_START][8] = ln[5 + GRID_START][11] = v0.projectedNormal.z;
			ln[6 + GRID_START][0] = ln[6 + GRID_START][3] = ln[6 + GRID_START][6] = ln[6 + GRID_START][9] = v1.projectedNormal.x;
			ln[6 + GRID_START][1] = ln[6 + GRID_START][4] = ln[6 + GRID_START][7] = ln[6 + GRID_START][10] = v1.projectedNormal.y;
			ln[6 + GRID_START][2] = ln[6 + GRID_START][5] = ln[6 + GRID_START][8] = ln[6 + GRID_START][11] = v1.projectedNormal.z;
			ln[10 + GRID_START][0] = ln[10 + GRID_START][3] = ln[10 + GRID_START][6] = ln[10 + GRID_START][9] = v2.projectedNormal.x;
			ln[10 + GRID_START][1] = ln[10 + GRID_START][4] = ln[10 + GRID_START][7] = ln[10 + GRID_START][10] = v2.projectedNormal.y;
			ln[10 + GRID_START][2] = ln[10 + GRID_START][5] = ln[10 + GRID_START][8] = ln[10 + GRID_START][11] = v2.projectedNormal.z;
			ln[9 + GRID_START][0] = ln[9 + GRID_START][3] = ln[9 + GRID_START][6] = ln[9 + GRID_START][9] = v3.projectedNormal.x;
			ln[9 + GRID_START][1] = ln[9 + GRID_START][4] = ln[9 + GRID_START][7] = ln[9 + GRID_START][10] = v3.projectedNormal.y;
			ln[9 + GRID_START][2] = ln[9 + GRID_START][5] = ln[9 + GRID_START][8] = ln[9 + GRID_START][11] = v3.projectedNormal.z;
			return 12;
		}
		final int dim = (1 << (depth - 1)) + 3;
		
//		/* test wheter fast evaluation arrays can be used */
//		final int valence0 = slate.corners[0].length;
//		final int valence1 = slate.corners[2].length;
//		boolean useFastPath = (valence0 <= MAX_FAST_VALENCE && valence1 <= MAX_FAST_VALENCE);
//		if (useFastPath) {
//			fast_test:
//			for (int corner = 0; corner < 4; corner++) {
//				SlateEdge[] slateEdges = slate.corners[corner];
//				if (slateEdges[0].vertex.corner > 0) {
//					useFastPath = false;
//					break;
//				}
//				for (int i = 0; i < slateEdges.length; i++) {
//					if (slateEdges[i].getSharpness() > 0) {
//						useFastPath = false;
//						break fast_test;
//					}
//				}
//			}
//		}
//		if (useFastPath) {
//			final int level = depth - 1;
//			final float[][] out = limitPoints[level];
//			final float[][] norm = limitNormals[level];
//			final int size = fastLimitWeights[level][valence0 - 3][valence1 - 3].length;
//			for (int i = 0; i < size; i++) {
//				final float lwa[][] = fastLimitWeights[level][valence0 - 3][valence1 - 3][i];
//				final float uwa[][] = fastTangent0Weights[level][valence0 - 3][valence1 - 3][i];
//				final float vwa[][] = fastTangent1Weights[level][valence0 - 3][valence1 - 3][i];
//				float lx = 0, ly = 0, lz = 0;
//				float ux = 0, uy = 0, uz = 0;
//				float vx = 0, vy = 0, vz = 0;
//				float sum = 0;
//				for (int corner = 0; corner < 4; corner++) {
//					Point3f[] fans = slate.fans[corner];
//					for (int vertex = 0; vertex < fans.length - 5; vertex++) {
//						int offset = (vertex == 0) ? 0 : 4;
//						final float lw = lwa[corner][vertex];
//						final float uw = uwa[corner][vertex];
//						final float vw = vwa[corner][vertex];
//						final float x = fans[vertex + offset].x;
//						final float y = fans[vertex + offset].y;
//						final float z = fans[vertex + offset].z;
//						lx += x * lw;
//						ly += y * lw;
//						lz += z * lw;
//						ux += x * uw;
//						uy += y * uw;
//						uz += z * uw;
//						vx += x * vw;
//						vy += y * vw;
//						vz += z * vw;
//						sum += lw;
//					}
//				}
//				final int outIndex = GRID_START + i;
//				out[outIndex][0] = lx;
//				out[outIndex][1] = ly;
//				out[outIndex][2] = lz;
//				if (RENDERER_SETTINGS.softwareNormalize) {
//					float nx = vy * uz - vz * uy;		// cross product
//					float ny = vz * ux - vx * uz;
//					float nz = vx * uy - vy * ux;
//					float nl = 1.0f / (float) sqrt(nx * nx + ny * ny + nz * nz);	// normalize
//					norm[outIndex][0] = nx * nl;
//					norm[outIndex][1] = ny * nl;
//					norm[outIndex][2] = nz * nl;
//				} else {
//					norm[outIndex][0] = vy * uz - vz * uy;		// cross product
//					norm[outIndex][1] = vz * ux - vx * uz;
//					norm[outIndex][2] = vx * uy - vy * ux;
//				}
//			}
//		} else {
			final Point3f[][] boundary = slate.fans;
			final SlateEdge[][] edges = slate.corners;
			/*
			 * initialize top-level geometry array
			 */
			float[][] geo = subdivPoints[0];
			/*
			 * initialize 2x2 grid
			 */
			geo[GRID_START + 5][0] = boundary[0][0].x;
			geo[GRID_START + 5][1] = boundary[0][0].y;
			geo[GRID_START + 5][2] = boundary[0][0].z;
			
			geo[GRID_START + 6][0] = boundary[1][0].x;
			geo[GRID_START + 6][1] = boundary[1][0].y;
			geo[GRID_START + 6][2] = boundary[1][0].z;
			
			geo[GRID_START + 2][0] = boundary[1][5].x;
			geo[GRID_START + 2][1] = boundary[1][5].y;
			geo[GRID_START + 2][2] = boundary[1][5].z;
			
			geo[GRID_START + 3][0] = boundary[1][6].x;
			geo[GRID_START + 3][1] = boundary[1][6].y;
			geo[GRID_START + 3][2] = boundary[1][6].z;
			
			geo[GRID_START + 7][0] = boundary[1][7].x;
			geo[GRID_START + 7][1] = boundary[1][7].y;
			geo[GRID_START + 7][2] = boundary[1][7].z;
			
			geo[GRID_START + 10][0] = boundary[2][0].x;
			geo[GRID_START + 10][1] = boundary[2][0].y;
			geo[GRID_START + 10][2] = boundary[2][0].z;
			
			geo[GRID_START + 9][0] = boundary[3][0].x;
			geo[GRID_START + 9][1] = boundary[3][0].y;
			geo[GRID_START + 9][2] = boundary[3][0].z;
			
			geo[GRID_START + 13][0] = boundary[3][5].x;
			geo[GRID_START + 13][1] = boundary[3][5].y;
			geo[GRID_START + 13][2] = boundary[3][5].z;
			
			geo[GRID_START + 12][0] = boundary[3][6].x;
			geo[GRID_START + 12][1] = boundary[3][6].y;
			geo[GRID_START + 12][2] = boundary[3][6].z;
			
			geo[GRID_START + 8][0] = boundary[3][7].x;
			geo[GRID_START + 8][1] = boundary[3][7].y;
			geo[GRID_START + 8][2] = boundary[3][7].z;
	//		// test crease stencils
			patchStencil[1][7][1] = slate.corners[0][0].getSharpness();
			patchStencil[1][13][1] = slate.corners[1][0].getSharpness();
			patchStencil[1][17][1] = slate.corners[2][0].getSharpness();
			patchStencil[1][11][1] = slate.corners[3][0].getSharpness();
			
			patchStencil[1][3][1] = slate.corners[1][2] == null ? 0 : slate.corners[1][2].getSharpness();
			patchStencil[1][9][1] = slate.corners[1][3] == null ? 0 : slate.corners[1][3].getSharpness();
			patchStencil[1][21][1] = slate.corners[3][2] == null ? 0 : slate.corners[3][2].getSharpness();
			patchStencil[1][15][1] = slate.corners[3][3] == null ? 0 : slate.corners[3][3].getSharpness();
					
			patchStencil[1][8][1] = (int) (slate.corners[1][0].vertex.crease * 0x10000);
			patchStencil[0][6][1] = (int) (slate.corners[1][0].vertex.crease * 0x10000);
			if (slate.corners[1][0].vertex.crease > 0) {
				patchStencil[1][8][0] = CREASE_5_7;
				patchStencil[0][6][0] = CREASE_5_7;
			} else {
				patchStencil[1][8][0] = POINT;
				patchStencil[0][6][0] = POINT;
			}
			
			patchStencil[1][16][1] = (int) (slate.corners[3][0].vertex.crease * 0x10000);
			patchStencil[0][9][1] = (int) (slate.corners[3][0].vertex.crease * 0x10000);
			if (slate.corners[3][0].vertex.crease > 0) {
				patchStencil[1][16][0] = CREASE_4_6;
				patchStencil[0][9][0] = CREASE_4_6;
			} else {
				patchStencil[1][16][0] = POINT;
				patchStencil[0][9][0] = POINT;
			}
			
			
	//		patchStencil[1][6][1] = slate.corners[0][0].vertex.corner;
	//		patchStencil[1][8][1] = 0;
	//		patchStencil[1][16][1] = 0;
	//		patchStencil[1][18][1] = slate.corners[3][0].vertex.corner;
			
			for (int corner = 0; corner < 2; corner ++) {
				final Point3f[] c = boundary[corner * 2];
				final int valence = Math.max(3, c.length / 2);
				final int n = c.length - 5;
				final int start = corner * MAX_CORNER_LENGTH;
				
	//			cornerStencil[1][valence - 3][corner][0] = Integer.MAX_VALUE;
				
				cornerStencil[0][valence - 3][corner][0] = (int) (slate.corners[corner * 2][0].vertex.corner * 0x10000);
				cornerStencil[0][valence - 3][corner][1] = (int) (slate.corners[corner * 2][0].vertex.crease * 0x10000);
				cornerStencil[1][valence - 3][corner][0] = (int) (slate.corners[corner * 2][0].vertex.corner * 0x10000);
				cornerStencil[1][valence - 3][corner][1] = (int) (slate.corners[corner * 2][0].vertex.crease * 0x10000);
				if (slate.corners[corner * 2][0].vertex.crease > 1) {
					int ci0 = slate.getEdgeIndex(corner * 2, slate.corners[corner * 2][0].vertex.creaseEdge0.slateEdge0);
					int ci1 = slate.getEdgeIndex(corner * 2, slate.corners[corner * 2][0].vertex.creaseEdge1.slateEdge0);
					cornerStencil[0][valence - 3][corner][2] = 2 * ci0 + 7;
					cornerStencil[0][valence - 3][corner][3] = 2 * ci1 + 7;
					cornerStencil[1][valence - 3][corner][2] = 2 * ci0 + 7;
					cornerStencil[1][valence - 3][corner][3] = 2 * ci1 + 7;
				}
				
				
				/*
				 * initialize corner arrays
				 */
				for (int i = 1; i < n; i++) {
					final int index = start + (i % (n - 1));
					final int i4 = i + 4;
					geo[index][0] = c[i4].x;
					geo[index][1] = c[i4].y;
					geo[index][2] = c[i4].z;
				}
				if (n == 2) {
					geo[start + 1][0] = c[5].x;
					geo[start + 1][1] = c[5].y;
					geo[start + 1][2] = c[5].z;
				}
				
				/* 
				 * initialize fan stencils
				 */
				for (int i = 2; i < slate.corners[corner * 2].length; i++) {
					int index = (i == slate.corners[corner * 2].length - 1) ? 0 : i * 2 - 3;
	//				System.out.println("corner=" + corner + " length=" + slate.corners[corner * 2].length + " i=" + i + " index=" + index);
					fanStencil[1][valence - 3][corner][index][1] = slate.corners[corner * 2][i] == null ? 0 : slate.corners[corner * 2][i].getSharpness();
				}
				if (n == 2) {
					fanStencil[1][valence - 3][corner][1][1] = fanStencil[1][valence - 3][corner][0][1];
				}
				
				// test crease stencils
	//			int[][] fan = fanStencil[1][valence][corner];
	//			for (int i = 0; i < fan.length; i += 2) {
	//				fan[i][1] = 1;
	//			}
			}
			
	//		boolean dump = dumpStencil(1);
			/*
			 * subdivide maxLevel times
			 */
	//		int[] stencilTypes = new int[11];
			for (int level = 1; level < depth; level++) {
				
	//			if (dump && level > 1) {
	//				dumpStencil(level);
	//			}
				
				final boolean rewriteStencils = (level < depth - 1 && level < MAX_SUBDIV - 1);
				final int[][] stencil = patchStencil[level];
				final int[][] nextLevel = rewriteStencils ? patchStencil[level + 1] : null;
				final float[][] out = subdivPoints[level];
				final float[][] in = subdivPoints[level - 1];
				final int n = stencil.length - 2;
					
				
				
				/*
				 * apply stencils on rectangular inner grid
				 */
				if (rewriteStencils) {
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
	//							if (s[2] == 0) throw new IllegalStateException();
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
	//							if (s[2] == 0) throw new IllegalStateException();
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
	//						if (s[2] == 0) throw new IllegalStateException();
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
				}else {
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
							} else {
								// smooth
								out[outIndex][0] = (in[s[7]][0] + in[s[8]][0]) * EDGE0 + ((in[s[9]][0] + in[s[10]][0]) + (in[s[11]][0] + in[s[12]][0])) * EDGE1;
								out[outIndex][1] = (in[s[7]][1] + in[s[8]][1]) * EDGE0 + ((in[s[9]][1] + in[s[10]][1]) + (in[s[11]][1] + in[s[12]][1])) * EDGE1;
								out[outIndex][2] = (in[s[7]][2] + in[s[8]][2]) * EDGE0 + ((in[s[9]][2] + in[s[10]][2]) + (in[s[11]][2] + in[s[12]][2])) * EDGE1;
							}
							break;
						case EDGE_V:
							if (s[1] > 0) {
								// crease
								out[outIndex][0] = (in[s[7]][0] + in[s[8]][0]) * 0.5f;
								out[outIndex][1] = (in[s[7]][1] + in[s[8]][1]) * 0.5f;
								out[outIndex][2] = (in[s[7]][2] + in[s[8]][2]) * 0.5f;
							} else {
								// smooth
								out[outIndex][0] = (in[s[7]][0] + in[s[8]][0]) * EDGE0 + ((in[s[9]][0] + in[s[10]][0]) + (in[s[11]][0] + in[s[12]][0])) * EDGE1;
								out[outIndex][1] = (in[s[7]][1] + in[s[8]][1]) * EDGE0 + ((in[s[9]][1] + in[s[10]][1]) + (in[s[11]][1] + in[s[12]][1])) * EDGE1;
								out[outIndex][2] = (in[s[7]][2] + in[s[8]][2]) * EDGE0 + ((in[s[9]][2] + in[s[10]][2]) + (in[s[11]][2] + in[s[12]][2])) * EDGE1;
							}
							break;
						case POINT:
							if (s[1] > 0) {
								// corner
								out[outIndex][0] = in[s[3]][0];
								out[outIndex][1] = in[s[3]][1];
								out[outIndex][2] = in[s[3]][2];
							} else {
								// smooth
								out[outIndex][0] = in[s[3]][0] * VERTEX0 + ((in[s[4]][0] + in[s[6]][0]) + (in[s[5]][0] + in[s[7]][0])) * VERTEX1 + ((in[s[8]][0] + in[s[10]][0]) + (in[s[9]][0] + in[s[11]][0])) * VERTEX2;
								out[outIndex][1] = in[s[3]][1] * VERTEX0 + ((in[s[4]][1] + in[s[6]][1]) + (in[s[5]][1] + in[s[7]][1])) * VERTEX1 + ((in[s[8]][1] + in[s[10]][1]) + (in[s[9]][1] + in[s[11]][1])) * VERTEX2;
								out[outIndex][2] = in[s[3]][2] * VERTEX0 + ((in[s[4]][2] + in[s[6]][2]) + (in[s[5]][2] + in[s[7]][2])) * VERTEX1 + ((in[s[8]][2] + in[s[10]][2]) + (in[s[9]][2] + in[s[11]][2])) * VERTEX2;
							}
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
							break;
						case CREASE_4_6:
							out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[6]][0]) * CREASE1;
							out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[6]][1]) * CREASE1;
							out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[6]][2]) * CREASE1;
							break;
						case CREASE_4_7:
							out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[7]][0]) * CREASE1;
							out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[7]][1]) * CREASE1;
							out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[7]][2]) * CREASE1;
							break;
						case CREASE_5_6:
							out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[5]][0] + in[s[6]][0]) * CREASE1;
							out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[5]][1] + in[s[6]][1]) * CREASE1;
							out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[5]][2] + in[s[6]][2]) * CREASE1;
							break;
						case CREASE_5_7:
							out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[5]][0] + in[s[7]][0]) * CREASE1;
							out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[5]][1] + in[s[7]][1]) * CREASE1;
							out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[5]][2] + in[s[7]][2]) * CREASE1;
							break;
						case CREASE_6_7:
							out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[6]][0] + in[s[7]][0]) * CREASE1;
							out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[6]][1] + in[s[7]][1]) * CREASE1;
							out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[6]][2] + in[s[7]][2]) * CREASE1;
							break;
						}
					}
				}
				
				/*
				 * apply stencils on corners and fans
				 */
				for (int corner = 0; corner < 2; corner++) {
					final int valence = Math.max(3, boundary[corner * 2].length / 2);
					final int[] cs = cornerStencil[level][valence - 3][corner];
					final int outIndex = cs[4];
					float x = 0, y = 0, z = 0;
					if (cs[0] > 0) {
						System.out.println("corner");
						//corner//
						x = in[cs[5]][0];
						y = in[cs[5]][1];
						z = in[cs[5]][2];
						if (rewriteStencils) {
							cornerStencil[level + 1][valence - 3][corner][0] = cornerStencil[level][valence - 3][corner][0] - 0x10000;
							cornerStencil[level + 1][valence - 3][corner][1] = cornerStencil[level][valence - 3][corner][1] - 0x10000;
							cornerStencil[level + 1][valence - 3][corner][2] = cornerStencil[level][valence - 3][corner][2];
							cornerStencil[level + 1][valence - 3][corner][3] = cornerStencil[level][valence - 3][corner][3];
						}
					} else if (cs[1] > 0) {
						//crease//
						x = in[cs[5]][0] * CREASE0 + (in[cs[cs[2]]][0] + in[cs[cs[3]]][0]) * CREASE1;
						y = in[cs[5]][1] * CREASE0 + (in[cs[cs[2]]][1] + in[cs[cs[3]]][1]) * CREASE1;
						z = in[cs[5]][2] * CREASE0 + (in[cs[cs[2]]][2] + in[cs[cs[3]]][2]) * CREASE1;
						if (rewriteStencils) {
							cornerStencil[level + 1][valence - 3][corner][0] = cornerStencil[level][valence - 3][corner][0] - 0x10000;
							cornerStencil[level + 1][valence - 3][corner][1] = cornerStencil[level][valence - 3][corner][1] - 0x10000;
							cornerStencil[level + 1][valence - 3][corner][2] = cornerStencil[level][valence - 3][corner][2];
							cornerStencil[level + 1][valence - 3][corner][3] = cornerStencil[level][valence - 3][corner][3];
						}
					}
					if (cs[0] > 0x10000 || cs[1] > 0x10000) {
						// use only corner or crease rules
						out[outIndex][0] = x;
						out[outIndex][1] = y;
						out[outIndex][2] = z;
					} else {
						//smooth//
						float f0 = 0, f1 = 0, f2 = 0;
						float e0 = 0, e1 = 0, e2 = 0;
						for (int p = 6; p < cs.length; p++) {
							f0 += in[cs[p]][0];
							f1 += in[cs[p]][1];
							f2 += in[cs[p++]][2];
							e0 += in[cs[p]][0];
							e1 += in[cs[p]][1];
							e2 += in[cs[p]][2];
						}
						float smoothX = f0 * VERTEX_FACE[valence] + e0 * VERTEX_EDGE[valence] + in[cs[5]][0] * VERTEX_POINT[valence];
						float smoothY = f1 * VERTEX_FACE[valence] + e1 * VERTEX_EDGE[valence] + in[cs[5]][1] * VERTEX_POINT[valence];
						float smoothZ = f2 * VERTEX_FACE[valence] + e2 * VERTEX_EDGE[valence] + in[cs[5]][2] * VERTEX_POINT[valence];
						if (rewriteStencils) {
							cornerStencil[level + 1][valence - 3][corner][0] = 0;
							cornerStencil[level + 1][valence - 3][corner][1] = 0;
						}
						if (cs[0] <= 0 && cs[1] <= 0) {
							// use only smooth rule
							out[outIndex][0] = smoothX;
							out[outIndex][1] = smoothY;
							out[outIndex][2] = smoothZ;
						} else {
							//interpolate between smooth and corner/crease rule
							float t = (cs[0] > 0) ? ((float) cs[0]) / 0x10000 : ((float) cs[1]) / 0x10000;
							float t1 = 1 - t;
							out[outIndex][0] = smoothX * t1 + x * t;
							out[outIndex][1] = smoothY * t1 + y * t;
							out[outIndex][2] = smoothZ * t1 + z * t;
						}
					}
	
					final int[][] array = fanStencil[level][valence - 3][corner];
					final int[][] nextArray = rewriteStencils ? fanStencil[level + 1][valence - 3][corner] : null;
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
								if (rewriteStencils) {
									nextArray[i][1] = s[1] - 1;
								}
							} else {
								out[oi][0] = (in[s[2]][0] + in[s[3]][0]) * EDGE0 + ((in[s[4]][0] + in[s[5]][0]) + (in[s[6]][0] + in[s[7]][0])) * EDGE1;
								out[oi][1] = (in[s[2]][1] + in[s[3]][1]) * EDGE0 + ((in[s[4]][1] + in[s[5]][1]) + (in[s[6]][1] + in[s[7]][1])) * EDGE1;
								out[oi][2] = (in[s[2]][2] + in[s[3]][2]) * EDGE0 + ((in[s[4]][2] + in[s[5]][2]) + (in[s[6]][2] + in[s[7]][2])) * EDGE1;
								if (rewriteStencils) {
									nextArray[i][1] = 0;
								}
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
			final int level = depth - 1;
			final int[][] limitStencil = patchLimitStencil[level];
			final int[][] stencil = patchStencil[level];
			final float[][] out = limitPoints[level];
			final float[][] norm = limitNormals[level];
			float limitX = 0, limitY = 0, limitZ = 0;
			float normalX = 0, normalY = 0, normalZ = 0;
			final float[][] in = subdivPoints[level];
	//		final int n = limitStencil.length;
	//		float ax, ay, az, bx, by, bz, nx, ny, nz, nl;
			
			for (int i = dim + 2, n = limitStencil.length - dim - 2; i < n; i++) {
				final int[] ls = limitStencil[i];
				final int[] s = stencil[i];
				if (ls != null) {
					final int outIndex = GRID_START + i;
					if (s[0] == FACE || s[1] == 0) {
						limitX = in[ls[0]][0] * LIMIT0 + ((in[ls[1]][0] + in[ls[3]][0]) + (in[ls[2]][0] + in[ls[4]][0])) * LIMIT1 + ((in[ls[5]][0] + in[ls[7]][0]) + (in[ls[6]][0] + in[ls[8]][0])) * LIMIT2;
						limitY = in[ls[0]][1] * LIMIT0 + ((in[ls[1]][1] + in[ls[3]][1]) + (in[ls[2]][1] + in[ls[4]][1])) * LIMIT1 + ((in[ls[5]][1] + in[ls[7]][1]) + (in[ls[6]][1] + in[ls[8]][1])) * LIMIT2;
						limitZ = in[ls[0]][2] * LIMIT0 + ((in[ls[1]][2] + in[ls[3]][2]) + (in[ls[2]][2] + in[ls[4]][2])) * LIMIT1 + ((in[ls[5]][2] + in[ls[7]][2]) + (in[ls[6]][2] + in[ls[8]][2])) * LIMIT2;
					} else {
						switch (s[0]) {
						case POINT:
							limitX = in[ls[0]][0];
							limitY = in[ls[0]][1];
							limitZ = in[ls[0]][2];
							break;
						case EDGE_H:
							limitX = (in[ls[2]][0] + in[ls[4]][0]) * 0.5f;
							limitY = (in[ls[2]][1] + in[ls[4]][1]) * 0.5f;
							limitZ = (in[ls[2]][2] + in[ls[4]][2]) * 0.5f;
							break;
						case EDGE_V:
							limitX = (in[ls[1]][0] + in[ls[3]][0]) * 0.5f;
							limitY = (in[ls[1]][1] + in[ls[3]][1]) * 0.5f;
							limitZ = (in[ls[1]][2] + in[ls[3]][2]) * 0.5f;
							break;
						case CREASE_4_5:
							limitX = in[ls[0]][0] * CREASE_LIMIT0 + (in[ls[1]][0] + in[ls[2]][0]) * CREASE_LIMIT1;
							limitY = in[ls[0]][1] * CREASE_LIMIT0 + (in[ls[1]][1] + in[ls[2]][1]) * CREASE_LIMIT1;
							limitZ = in[ls[0]][2] * CREASE_LIMIT0 + (in[ls[1]][2] + in[ls[2]][2]) * CREASE_LIMIT1;
							break;
						case CREASE_4_6:
							limitX = in[ls[0]][0] * CREASE_LIMIT0 + (in[ls[1]][0] + in[ls[3]][0]) * CREASE_LIMIT1;
							limitY = in[ls[0]][1] * CREASE_LIMIT0 + (in[ls[1]][1] + in[ls[3]][1]) * CREASE_LIMIT1;
							limitZ = in[ls[0]][2] * CREASE_LIMIT0 + (in[ls[1]][2] + in[ls[3]][2]) * CREASE_LIMIT1;
							break;
						case CREASE_4_7:
							limitX = in[ls[0]][0] * CREASE_LIMIT0 + (in[ls[1]][0] + in[ls[4]][0]) * CREASE_LIMIT1;
							limitY = in[ls[0]][1] * CREASE_LIMIT0 + (in[ls[1]][1] + in[ls[4]][1]) * CREASE_LIMIT1;
							limitZ = in[ls[0]][2] * CREASE_LIMIT0 + (in[ls[1]][2] + in[ls[4]][2]) * CREASE_LIMIT1;
							break;
						case CREASE_5_6:
							limitX = in[ls[0]][0] * CREASE_LIMIT0 + (in[ls[2]][0] + in[ls[3]][0]) * CREASE_LIMIT1;
							limitY = in[ls[0]][1] * CREASE_LIMIT0 + (in[ls[2]][1] + in[ls[3]][1]) * CREASE_LIMIT1;
							limitZ = in[ls[0]][2] * CREASE_LIMIT0 + (in[ls[2]][2] + in[ls[3]][2]) * CREASE_LIMIT1;
							break;
						case CREASE_5_7:
							limitX = in[ls[0]][0] * CREASE_LIMIT0 + (in[ls[2]][0] + in[ls[4]][0]) * CREASE_LIMIT1;
							limitY = in[ls[0]][1] * CREASE_LIMIT0 + (in[ls[2]][1] + in[ls[4]][1]) * CREASE_LIMIT1;
							limitZ = in[ls[0]][2] * CREASE_LIMIT0 + (in[ls[2]][2] + in[ls[4]][2]) * CREASE_LIMIT1;
							break;
						case CREASE_6_7:
							limitX = in[ls[0]][0] * CREASE_LIMIT0 + (in[ls[3]][0] + in[ls[4]][0]) * CREASE_LIMIT1;
							limitY = in[ls[0]][1] * CREASE_LIMIT0 + (in[ls[3]][1] + in[ls[4]][1]) * CREASE_LIMIT1;
							limitZ = in[ls[0]][2] * CREASE_LIMIT0 + (in[ls[3]][2] + in[ls[4]][2]) * CREASE_LIMIT1;
							break;
						}
					}
//					System.out.println("depth=" + depth + " ul=" + ls[9] + " ur=" + ls[10] + " lr=" + ls[11] + " ll=" + ls[12]);
//					quadVertexArrays[depth][ls[9]] = quadVertexArrays[depth][ls[10]] = quadVertexArrays[depth][ls[11]] = quadVertexArrays[depth][ls[12]] = limitX;
//					quadVertexArrays[depth][ls[9] + 1] = quadVertexArrays[depth][ls[10] + 1] = quadVertexArrays[depth][ls[11] + 1] = quadVertexArrays[depth][ls[12] + 1] = limitY;
//					quadVertexArrays[depth][ls[9] + 2] = quadVertexArrays[depth][ls[10] + 2] = quadVertexArrays[depth][ls[11] + 2] = quadVertexArrays[depth][ls[12] + 2] = limitZ;
//					
					
					out[outIndex][0] = limitX;
					out[outIndex][1] = limitY;
					out[outIndex][2] = limitZ;
					
					if (s[0] == CREASE_4_6 || (s[0] == EDGE_V && s[1] > 0)) {
						final float ux = in[ls[1]][0] - in[ls[3]][0];
						final float uy = in[ls[1]][1] - in[ls[3]][1];
						final float uz = in[ls[1]][2] - in[ls[3]][2];
						final float vx0 = in[ls[2]][0] - in[ls[0]][0];
						final float vy0 = in[ls[2]][1] - in[ls[0]][1];
						final float vz0 = in[ls[2]][2] - in[ls[0]][2];
						final float vx1 = in[ls[0]][0] - in[ls[4]][0];
						final float vy1 = in[ls[0]][1] - in[ls[4]][1];
						final float vz1 = in[ls[0]][2] - in[ls[4]][2];
						final float[] normal = norm[outIndex];
						computeNormal(vx0, vy0, vz0, ux, uy, uz, normal, 0);
						computeNormal(vx1, vy1, vz1, ux, uy, uz, normal, 3);
//						normal[0] = normal[1] = normal[2] = 0;
//						normal[3] = normal[4] = normal[5] = 0;
						normal[9] = normal[0];
						normal[10] = normal[1];
						normal[11] = normal[2];
						normal[6] = normal[3];
						normal[7] = normal[4];
						normal[8] = normal[5];
					} else if (s[0] == CREASE_5_7 || (s[0] == EDGE_H && s[1] > 0)) {
						final float ux = in[ls[2]][0] - in[ls[4]][0];
						final float uy = in[ls[2]][1] - in[ls[4]][1];
						final float uz = in[ls[2]][2] - in[ls[4]][2];
						final float vx0 = in[ls[3]][0] - in[ls[0]][0];
						final float vy0 = in[ls[3]][1] - in[ls[0]][1];
						final float vz0 = in[ls[3]][2] - in[ls[0]][2];
						final float vx1 = in[ls[0]][0] - in[ls[1]][0];
						final float vy1 = in[ls[0]][1] - in[ls[1]][1];
						final float vz1 = in[ls[0]][2] - in[ls[1]][2];
						final float[] normal = norm[outIndex];
						computeNormal(vx0, vy0, vz0, ux, uy, uz, normal, 0);
						computeNormal(vx1, vy1, vz1, ux, uy, uz, normal, 9);
//						normal[0] = normal[1] = normal[2] = 0;
//						normal[9] = normal[10] = normal[11] = 0;
						normal[3] = normal[0];
						normal[4] = normal[1];
						normal[5] = normal[2];
						normal[6] = normal[9];
						normal[7] = normal[10];
						normal[8] = normal[11];
					} else {
						final float ux = (in[ls[2]][0] - in[ls[4]][0]) * 4 + (in[ls[6]][0] - in[ls[5]][0]) + (in[ls[7]][0] - in[ls[8]][0]);
						final float uy = (in[ls[2]][1] - in[ls[4]][1]) * 4 + (in[ls[6]][1] - in[ls[5]][1]) + (in[ls[7]][1] - in[ls[8]][1]);
						final float uz = (in[ls[2]][2] - in[ls[4]][2]) * 4 + (in[ls[6]][2] - in[ls[5]][2]) + (in[ls[7]][2] - in[ls[8]][2]);
						final float vx = (in[ls[1]][0] - in[ls[3]][0]) * 4 + (in[ls[5]][0] - in[ls[8]][0]) + (in[ls[6]][0] - in[ls[7]][0]);
						final float vy = (in[ls[1]][1] - in[ls[3]][1]) * 4 + (in[ls[5]][1] - in[ls[8]][1]) + (in[ls[6]][1] - in[ls[7]][1]);
						final float vz = (in[ls[1]][2] - in[ls[3]][2]) * 4 + (in[ls[5]][2] - in[ls[8]][2]) + (in[ls[6]][2] - in[ls[7]][2]);
						final float[] normal = norm[outIndex];
						computeNormal(ux, uy, uz, vx, vy, vz, normal, 0);
						normal[9] = normal[6] = normal[3] = normal[0];
						normal[10] = normal[7] = normal[4] = normal[1];
						normal[11] = normal[8] = normal[5] = normal[2];
					}
				}
			}
			
			/*
			 * apply limit stencils on corners
			 */
			for (int corner = 0; corner < 2; corner ++) {
				final int valence = Math.max(3, boundary[corner * 2].length / 2);
				final int[] cps = cornerStencil[level][valence - 3][corner];
				final int[] cs = cornerLimitStencil[level][valence - 3][corner];
				
				final int outIndex = cs[4];
				
				if (cps[0] > 0) {
					// corner
					out[outIndex][0] = in[cs[5]][0];
					out[outIndex][1] = in[cs[5]][1];
					out[outIndex][2] = in[cs[5]][2];
				} else if (cps[1] > 0) {
					//crease//
					out[outIndex][0] = in[cs[5]][0] * CREASE_LIMIT0 + (in[cs[cps[2]]][0] + in[cs[cps[3]]][0]) * CREASE_LIMIT1;
					out[outIndex][1] = in[cs[5]][1] * CREASE_LIMIT0 + (in[cs[cps[2]]][1] + in[cs[cps[3]]][1]) * CREASE_LIMIT1;
					out[outIndex][2] = in[cs[5]][2] * CREASE_LIMIT0 + (in[cs[cps[2]]][2] + in[cs[cps[3]]][2]) * CREASE_LIMIT1;
				} else {
					float f0 = 0, f1 = 0, f2 = 0;
					float e0 = 0, e1 = 0, e2 = 0;
					for (int p = 6; p < cs.length; p++) {
						f0 += in[cs[p]][0];
						f1 += in[cs[p]][1];
						f2 += in[cs[p++]][2];
						e0 += in[cs[p]][0];
						e1 += in[cs[p]][1];
						e2 += in[cs[p]][2];
					}
					out[outIndex][0] = e0 * VERTEX_EDGE_LIMIT[valence] + f0 * VERTEX_FACE_LIMIT[valence] + in[cs[5]][0] * VERTEX_POINT_LIMIT[valence];
					out[outIndex][1] = e1 * VERTEX_EDGE_LIMIT[valence] + f1 * VERTEX_FACE_LIMIT[valence] + in[cs[5]][1] * VERTEX_POINT_LIMIT[valence];
					out[outIndex][2] = e2 * VERTEX_EDGE_LIMIT[valence] + f2 * VERTEX_FACE_LIMIT[valence] + in[cs[5]][2] * VERTEX_POINT_LIMIT[valence];
				}
				/* normal */
				
				if (cps[0] > 0) {
					// corner //
					final float vx = in[cs[7]][0] - in[cs[5]][0];
					final float vy = in[cs[7]][1] - in[cs[5]][1];
					final float vz = in[cs[7]][2] - in[cs[5]][2];
					final float ux = in[cs[9]][0] - in[cs[5]][0];
					final float uy = in[cs[9]][1] - in[cs[5]][1];
					final float uz = in[cs[9]][2] - in[cs[5]][2];
					float[] normal = norm[outIndex];
					computeNormal(ux, uy, uz, vx, vy, vz, normal, 0);
				} else if (cps[1] > 0) {
					// crease
					int v = cps.length - 6;
					int a = cps[2] - 6;
					int b = cps[3] - 6;
					int aMinusB = (a + v - b) % v;
					int bMinusA = (b + v - a) % v;
					if (aMinusB < bMinusA) {
						int tmp = b;
						b = a;
						a = tmp;
					}
					int next = (a == 1) ? 2 : (b + 2) % v;
					final float vx = in[cs[a + 6]][0] - in[cs[b + 6]][0];
					final float vy = in[cs[a + 6]][1] - in[cs[b + 6]][1];
					final float vz = in[cs[a + 6]][2] - in[cs[b + 6]][2];
					final float ux = in[cs[next + 6]][0] - in[cs[5]][0];
					final float uy = in[cs[next + 6]][1] - in[cs[5]][1];
					final float uz = in[cs[next + 6]][2] - in[cs[5]][2];
					float[] normal = norm[outIndex];
					computeNormal(vx, vy, vz, ux, uy, uz, normal, 0);
				} else {
					// smooth //
					float ux = 0, uy = 0, uz = 0, vx = 0, vy = 0, vz = 0;
					for (int j = 0; j < valence; j++) {
						int c2fi = j * 2 + 6;
						int c2ei = j * 2 + 5;
						if (c2ei == 5) {
							c2ei = cs.length - 1;
						}
						int c3fi = c2fi + 2;
						if (c3fi >= cs.length) {
							c3fi -= cs.length - 6;
						}
						int c3ei = c2ei + 2;
						if (c3ei >= cs.length) {
							c3ei -= cs.length - 6;
						}
						float ew = TANGENT_EDGE_WEIGHT[valence][j];
						float fw = TANGENT_FACE_WEIGHT[valence][j];
						ux += in[cs[c3fi]][0] * fw;
						uy += in[cs[c3fi]][1] * fw;
						uz += in[cs[c3fi]][2] * fw;
						ux += in[cs[c3ei]][0] * ew;
						uy += in[cs[c3ei]][1] * ew;
						uz += in[cs[c3ei]][2] * ew;
						vx += in[cs[c2fi]][0] * fw;
						vy += in[cs[c2fi]][1] * fw;
						vz += in[cs[c2fi]][2] * fw;
						vx += in[cs[c2ei]][0] * ew;
						vy += in[cs[c2ei]][1] * ew;
						vz += in[cs[c2ei]][2] * ew;
						float[] normal = norm[outIndex];
						computeNormal(ux, uy, uz, vx, vy, vz, normal, 0);
						normal[9] = normal[6] = normal[3] = normal[0];
						normal[10] = normal[7] = normal[4] = normal[1];
						normal[11] = normal[8] = normal[5] = normal[2];
					}
				}
			}
//		}	
		
//		final int level = depth - 1;
//		final float[][] out = limitPoints[level];
//		final float[][] norm = limitNormals[level];
//		final float[] va = quadVertexArrays[depth];
//		final float[] na = quadNormalArrays[depth];
//		final int start, end;
////		if (depth < 2) {
////			start = 1;
////			end = dim - 2;
////		} else {
//			start = 2;
//			end = dim - 3;
////		}
//		int ydim = start * dim;
//		
//		int vi = 0, ni = 0;
//		for (int y = start; y < end; y++) {
//			int gsydim = GRID_START + ydim;
//			int gsydim1 = gsydim + dim;
//			for (int x = start; x < end; x++) {
//				na[ni++] = norm[gsydim + x][0];
//				na[ni++] = norm[gsydim + x][1];
//				na[ni++] = norm[gsydim + x][2];
//				na[ni++] = norm[gsydim + x + 1][0];
//				na[ni++] = norm[gsydim + x + 1][1];
//				na[ni++] = norm[gsydim + x + 1][2];
//				na[ni++] = norm[gsydim1 + x + 1][0];
//				na[ni++] = norm[gsydim1 + x + 1][1];
//				na[ni++] = norm[gsydim1 + x + 1][2];
//				na[ni++] = norm[gsydim1 + x][0];
//				na[ni++] = norm[gsydim1 + x][1];
//				na[ni++] = norm[gsydim1 + x][2];
//				
////				for (int i = vi; i < vi + 12; i++) {
////					System.out.print(va[i] + " ");
////				}
////				System.out.println();
//				
////				va[vi++] = out[gsydim + x][0];
////				va[vi++] = out[gsydim + x][1];
////				va[vi++] = out[gsydim + x][2];
////				va[vi++] = out[gsydim + x + 1][0];
////				va[vi++] = out[gsydim + x + 1][1];
////				va[vi++] = out[gsydim + x + 1][2];
////				va[vi++] = out[gsydim1 + x + 1][0];
////				va[vi++] = out[gsydim1 + x + 1][1];
////				va[vi++] = out[gsydim1 + x + 1][2];
////				va[vi++] = out[gsydim1 + x][0];
////				va[vi++] = out[gsydim1 + x][1];	
////				va[vi++] = out[gsydim1 + x][2];
//				
////				for (int i = vi - 12; i < vi; i++) {
////					System.out.print(va[i] + " ");
////				}
////				System.out.println("\n");
//			}
//			ydim += dim;
//		}
		
//		int j = 0;
//		while (j < i) {
//			System.out.println(ia[j++] + "," + ia[j++] + "," + ia[j++] + "    " + ia[j++] + "," + ia[j++] + "," + ia[j++]);
//		}
//		buffer.rewind();
//		buffer.put(ia, 0, i);
//		System.out.println("*" + vi);
		return 0;
	}
	
	
	private void computeNormal(final float ux, final float uy, final float uz, final float vx, final float vy, final float vz, final float[] normal, final int offset) {
		if (RENDERER_SETTINGS.softwareNormalize) {
			final float nx = vy * uz - vz * uy;									// cross product
			final float ny = vz * ux - vx * uz;
			final float nz = vx * uy - vy * ux;
			final float nl = 1.0f / (float) sqrt(nx * nx + ny * ny + nz * nz);	// normalize
			normal[offset] = nx * nl;
			normal[offset + 1] = ny * nl;
			normal[offset + 2] = nz * nl;
		} else {
			normal[offset] = vy * uz - vz * uy;								// cross product
			normal[offset + 1] = vz * ux - vx * uz;
			normal[offset + 2] = vx * uy - vy * ux;
		}
	}
	
	private void quickDice(int[] valence, int depth) {
		
		/* subdivide depth levels */
		for (int level = 1; level < depth; level++) {
			final int[][] stencil = patchStencil[level];
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
				case EDGE_V: // fallthrough is intentional
					out[outIndex][0] = (in[s[7]][0] + in[s[8]][0]) * EDGE0 + ((in[s[9]][0] + in[s[10]][0]) + (in[s[11]][0] + in[s[12]][0])) * EDGE1;
				break;
				case POINT:
					out[outIndex][0] = in[s[3]][0] * VERTEX0 + ((in[s[4]][0] + in[s[6]][0]) + (in[s[5]][0] + in[s[7]][0])) * VERTEX1 + ((in[s[8]][0] + in[s[10]][0]) + (in[s[9]][0] + in[s[11]][0])) * VERTEX2;
				break;
				case FACE:
					out[outIndex][0] = ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * FACE0;
				break;
				}
			}
			
			/*
			 * apply stencils on corners and fans
			 */
			for (int corner = 0; corner < 2; corner++) {
				
				final int val = valence[corner];
				final int[] cs = cornerStencil[level][val - 3][corner];
				final int outIndex = cs[4];
				float f0 = 0, e0 = 0;
				for (int p = 6; p < cs.length; p++) {
					f0 += in[cs[p++]][0];
					e0 += in[cs[p]][0];
				}
				out[outIndex][0] = f0 * VERTEX_FACE[val] + e0 * VERTEX_EDGE[val] + in[cs[5]][0] * VERTEX_POINT[val];
				
				final int[][] array = fanStencil[level][val - 3][corner];
				final int m = array.length;
				for (int i = 0; i < m; i++) {
					final int oi = MAX_CORNER_LENGTH * corner + i;
					final int[] s = array[i];
					switch (s[0]) {
					case EDGE:
						out[oi][0] = (in[s[2]][0] + in[s[3]][0]) * EDGE0 + ((in[s[4]][0] + in[s[5]][0]) + (in[s[6]][0] + in[s[7]][0])) * EDGE1;
						break;
					case FACE:
						out[oi][0] = ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * FACE0;
						break;
					}
				}
			}			
		}
		
		/*
		 * project vertices to limit surface
		 *
		 * apply limit stencils on rectangular inner grid
		 */
		final int level = depth - 1;
		final int[][] limitStencil = patchLimitStencil[level];
		final float[][] out = limitPoints[level];
		final float[][] in = subdivPoints[level];
	
		final int dim = (1 << (depth - 1)) + 3;
		
		for (int i = dim + 2, n = limitStencil.length - dim - 2; i < n; i++) {
			final int[] ls = limitStencil[i];
			if (ls == null) {
				continue;
			}
			final int outIndex = GRID_START + i;
			// 0 ... limit projection
			out[outIndex][0] = in[ls[0]][0] * LIMIT0 + ((in[ls[1]][0] + in[ls[3]][0]) + (in[ls[2]][0] + in[ls[4]][0])) * LIMIT1 + ((in[ls[5]][0] + in[ls[7]][0]) + (in[ls[6]][0] + in[ls[8]][0])) * LIMIT2;
			// 1 ... u-tangent
			// 2 ... v-tangent
			out[outIndex][1] = (in[ls[2]][0] - in[ls[4]][0]) * 4 + (in[ls[6]][0] - in[ls[5]][0]) + (in[ls[7]][0] - in[ls[8]][0]);
			out[outIndex][2] = (in[ls[1]][0] - in[ls[3]][0]) * 4 + (in[ls[5]][0] - in[ls[8]][0]) + (in[ls[6]][0] - in[ls[7]][0]);
		}
		
		/*
		 * apply limit stencils on corners
		 */
		for (int corner = 0; corner < 2; corner ++) {
			final int val = valence[corner];
			final int[] cs = cornerLimitStencil[level][val - 3][corner];
			
			final int outIndex = cs[4];
			
			float f0 = 0, e0 = 0;
			for (int p = 6; p < cs.length; p++) {
				f0 += in[cs[p++]][0];
				e0 += in[cs[p]][0];
			}
			out[outIndex][0] = e0 * VERTEX_EDGE_LIMIT[val] + f0 * VERTEX_FACE_LIMIT[val] + in[cs[5]][0] * VERTEX_POINT_LIMIT[val];			
			
			/* tangents */
			float ax = 0;
			float bx = 0;
			for (int j = 0; j < val; j++) {
				int c2fi = j * 2 + 6;
				int c2ei = j * 2 + 5;
				if (c2ei == 5) {
					c2ei = cs.length - 1;
				}
				int c3fi = c2fi + 2;
				if (c3fi >= cs.length) {
					c3fi -= cs.length - 6;
				}
				int c3ei = c2ei + 2;
				if (c3ei >= cs.length) {
					c3ei -= cs.length - 6;
				}
				float ew = TANGENT_EDGE_WEIGHT[val][j];
				float fw = TANGENT_FACE_WEIGHT[val][j];
				ax += in[cs[c3fi]][0] * fw;
				ax += in[cs[c3ei]][0] * ew;
				bx += in[cs[c2fi]][0] * fw;
				bx += in[cs[c2ei]][0] * ew;
			}
			out[outIndex][1] = ax;
			out[outIndex][2] = bx;
		}
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
	
	private boolean dumpStencil(int level) {
		boolean result = false;
		int dim = (1 << (level)) + 3;
		System.out.println();
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				int index = i * dim + j;
				switch (patchStencil[level][index][0]) {
				case UNUSED: System.out.print("."); break;
				case POINT: System.out.print("P"); break;
				case FACE: System.out.print("F"); break;
				case EDGE_H: System.out.print("Eh"); break;
				case EDGE_V: System.out.print("Hv"); break;
				case CREASE_5_7: System.out.print("Ch"); break;
				case CREASE_4_6: System.out.print("Cv"); break;
				}
				if (patchStencil[level][index][0] != FACE && patchStencil[level][index][0] != UNUSED) {
					if (patchStencil[level][index][1] > 0) {
						result = true;
					}
					System.out.print(patchStencil[level][index][1]);
				}
				System.out.print("\t");
			}
			System.out.println();
		}
		return result;
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
	
//	public static void main(String[] args) {
//		Dicer dicer = new Dicer();
//		
//		JFrame frame = new JFrame("Dicer test");
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		final int depth = 6;
//		final int dim = (1 << (depth - 1)) + 3;
//		final int valence = 4;
//		frame.setLayout(new GridLayout(4, 16));
//		for (int corner = 0; corner < 4; corner++) {
//			final int n = dicer.fastLimitWeights[depth - 1][valence - 3][valence - 3][0][corner].length;
//			for (int vertex = 0; vertex < n; vertex++) {
//				ArrayImage arrayImage = new ArrayImage(dim, dim);
//				int[] buffer = arrayImage.getBuffer();
//				for (int i = 0; i < dicer.fastLimitWeights[depth - 1][valence - 3][valence - 3].length; i++) {
//					float weight = dicer.fastTangent1Weights[depth - 1][valence - 3][valence - 3][i][corner][vertex];
//					System.out.println(weight);
//					int level = Math.max(0, Math.min(255, (int) (weight * 1000)));
//					buffer[i] = level | (level << 8) | (level << 16);
//				}
//				JComponent component = new ImagePanel(arrayImage.getImage()).getComponent();
//				frame.add(component);
//			}
//			for (int i = n; i < 16; i++) {
//				frame.add(new JPanel());
//			}
//		}
//		frame.pack();
//		frame.setVisible(true);
//		
//		
//		
//	}
}
