package sds;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import static java.lang.Math.*;

public class Slate {
	private static final int UNUSED = 0;
	private static final int EDGE = 1;
	private static final int FACE = 2;
	private static final int POINT = 4;
	
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
	
	public Slate() {
//		System.out.println("MAX_CORNER_LENGTH=" + MAX_CORNER_LENGTH);
		for (int level = 0; level < MAX_SUBDIV; level++) {
			int dim = ((1 << level)) + 3;
			
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
					cornerStencil[level][valence - 3][corner] = new int[valence * 2 + 2];
					cornerStencil[level][valence - 3][corner][0] = patchCornerIndex(corner, level + 1, 1, 1);	// target!
					cornerStencil[level][valence - 3][corner][1] = patchCornerIndex(corner, level, 1, 1);
					cornerStencil[level][valence - 3][corner][2] = patchCornerIndex(corner, level, 0, 2);
					cornerStencil[level][valence - 3][corner][3] = patchCornerIndex(corner, level, 1, 2);
					cornerStencil[level][valence - 3][corner][4] = patchCornerIndex(corner, level, 2, 2);
					cornerStencil[level][valence - 3][corner][5] = patchCornerIndex(corner, level, 2, 1);
					cornerStencil[level][valence - 3][corner][6] = patchCornerIndex(corner, level, 2, 0);
					for (int i = 0, n = valence * 2 - 5; i < n; i++) {
						int index = i + 1;
						if (index == n) {
							index = 0;
						}
						cornerStencil[level][valence - 3][corner][i + 7] = cornerIndex(corner, valence, index);
					}
					
					cornerLimitStencil[level][valence - 3][corner] = new int[valence * 2 + 2];
					cornerLimitStencil[level][valence - 3][corner][0] = patchCornerIndex(corner, level + 1, 1, 1);	// target!
					cornerLimitStencil[level][valence - 3][corner][1] = patchCornerIndex(corner, level + 1, 1, 1);
					cornerLimitStencil[level][valence - 3][corner][2] = patchCornerIndex(corner, level + 1, 0, 2);
					cornerLimitStencil[level][valence - 3][corner][3] = patchCornerIndex(corner, level + 1, 1, 2);
					cornerLimitStencil[level][valence - 3][corner][4] = patchCornerIndex(corner, level + 1, 2, 2);
					cornerLimitStencil[level][valence - 3][corner][5] = patchCornerIndex(corner, level + 1, 2, 1);
					cornerLimitStencil[level][valence - 3][corner][6] = patchCornerIndex(corner, level + 1, 2, 0);
					for (int i = 0, n = valence * 2 - 5; i < n; i++) {
						int index = i + 1;
						if (index == n) {
							index = 0;
						}
						cornerLimitStencil[level][valence - 3][corner][i + 7] = cornerIndex(corner, valence, index);
					}
					
					int[][] array = new int[cornerStencilLength(valence)][];
					fanStencil[level][valence - 3][corner] = array;
					
					
					
					if (valence == 3) {
						array[0] = new int[] {
								EDGE,
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
					if (row < 2 || row > dim - 3) {
						if (column < 2 || column > dim - 3) {
							patchStencil[level][index] = new int[] { UNUSED };
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
							patchStencil[level][index] = new int[] {
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
							patchStencil[level][index] = new int[] {
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
	
	public int getGridStart() {
		return GRID_START;
	}
	
	public float[][] getGeo(int level) {
		return limitPoints[level];
	}
	
	public float[][] getNormals(int level) {
		return limitNormals[level];
	}
	
	public void subdivide(final int maxLevel, final float[][][] boundary) {

		/*
		 * initialize top-level geometry array
		 */
		float[][] geo = subdivPoints[0];
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
			final int start = corner * MAX_CORNER_LENGTH;
			
			/*
			 * initialize corner arrays
			 */
			for (int i = 1; i < n; i++) {
				final int index = start + (i % (n - 1));
				geo[index][0] = c[i][0];
				geo[index][1] = c[i][1];
				geo[index][2] = c[i][2];
			}
			if (n == 2) {
				geo[start + 1][0] = c[1][0];
				geo[start + 1][1] = c[1][1];
				geo[start + 1][2] = c[1][2];
			}
		}
		
		/*
		 * subdivide maxLevel times
		 */
		for (int level = 1; level < maxLevel; level++) {
			final int[][] stencil = patchStencil[level];
			final float[][] out = subdivPoints[level];
			final float[][] in = subdivPoints[level - 1];
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
			 * apply stencils on corners and fans
			 */
			for (int corner = 0; corner < 4; corner++) {
				final int valence = boundary[corner].length / 2 + 2;
				final int[] cs = cornerStencil[level][valence - 3][corner];
				float a0 = 0;
				float a1 = 0;
				float a2 = 0;
				float b0 = 0;
				float b1 = 0;
				float b2 = 0;
				for (int p = 2; p < cs.length; p++) {
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
				final int outIndex = cs[0];
				out[outIndex][0] = (a0 + b0 + in[cs[1]][0] * cc) * ik;
				out[outIndex][1] = (a1 + b1 + in[cs[1]][1] * cc) * ik;
				out[outIndex][2] = (a2 + b2 + in[cs[1]][2] * cc) * ik;

				final int[][] array = fanStencil[level][valence - 3][corner];
				final int m = array.length;
				for (int i = 0; i < m; i++) {
					final int oi = MAX_CORNER_LENGTH * corner + i;
					final int[] s = array[i];
					switch (s[0]) {
					case EDGE:
						out[oi][0] = (in[s[1]][0] + in[s[2]][0]) * EDGE0 + ((in[s[3]][0] + in[s[4]][0]) + (in[s[5]][0] + in[s[6]][0])) * EDGE1;
						out[oi][1] = (in[s[1]][1] + in[s[2]][1]) * EDGE0 + ((in[s[3]][1] + in[s[4]][1]) + (in[s[5]][1] + in[s[6]][1])) * EDGE1;
						out[oi][2] = (in[s[1]][2] + in[s[2]][2]) * EDGE0 + ((in[s[3]][2] + in[s[4]][2]) + (in[s[5]][2] + in[s[6]][2])) * EDGE1;
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
		int level = maxLevel - 1;
		final int[][] stencil = patchLimitStencil[level];
		final float[][] out = limitPoints[level];
		final float[][] norm = limitNormals[level];
		final float[][] in = subdivPoints[level];
		final int n = stencil.length;
		float ax, ay, az, bx, by, bz;
		
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
			
			norm[outIndex][0] = by * az - bz * ay;		// cross product
			norm[outIndex][1] = bz * ax - bx * az;
			norm[outIndex][2] = bx * ay - by * ax;
		}
		
		/*
		 * apply limit stencils on corners
		 */
		for (int corner = 0; corner < 4; corner++) {
			final int valence = boundary[corner].length / 2 + 2;
			final int[] cs = cornerLimitStencil[level][valence - 3][corner];
			float f0 = 0;
			float f1 = 0;
			float f2 = 0;
			float e0 = 0;
			float e1 = 0;
			float e2 = 0;
			for (int p = 2; p < cs.length; p++) {
				f0 += in[cs[p]][0];
				f1 += in[cs[p]][1];
				f2 += in[cs[p++]][2];
				e0 += in[cs[p]][0];
				e1 += in[cs[p]][1];
				e2 += in[cs[p]][2];
			}
			final float ik = 1.0f / (valence * (valence + 5));			// TODO:
			final float edgeWeight = 4;										// precompute these values
			final float faceWeight = 1;										// for each valence and
			final float pointWeight = valence * valence;						// use loopup table
			f0 *= faceWeight;
			f1 *= faceWeight;
			f2 *= faceWeight;
			e0 *= edgeWeight;
			e1 *= edgeWeight;
			e2 *= edgeWeight;
			final int outIndex = cs[0];
			out[outIndex][0] = (e0 + f0 + in[cs[1]][0] * pointWeight) * ik;
			out[outIndex][1] = (e1 + f1 + in[cs[1]][1] * pointWeight) * ik;
			out[outIndex][2] = (e2 + f2 + in[cs[1]][2] * pointWeight) * ik;
			
			/* normal */
			float An = (float) (1 + cos(2 * PI / valence) + cos(PI / valence) * sqrt(2 * (9 + cos(2 * PI / valence))));
			ax = 0;
			ay = 0;
			az = 0;
			bx = 0;
			by = 0;
			bz = 0;
			for (int j = 0; j < valence; j++) {
				int c2fi = j * 2 + 2;
				int c2ei = j * 2 + 1;
				if (c2ei == 1) {
					c2ei = cs.length - 1;
				}
				int c3fi = c2fi + 2;
				if (c3fi >= cs.length) {
					c3fi -= cs.length - 2;
				}
				int c3ei = c2ei + 2;
				if (c3ei >= cs.length) {
					c3ei -= cs.length - 2;
				}
				float ew = (float) (An * cos(2 * PI  * j / valence));
				float fw = (float) (cos(2 * PI * j / valence) + cos(2 * PI * (j + 1) / valence));
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
			norm[outIndex][0] = by * az - bz * ay;		// cross product
			norm[outIndex][1] = bz * ax - bx * az;
			norm[outIndex][2] = bx * ay - by * ax;
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
//		System.out.println("max=" + max + " i=" + i);
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
					int level = 2;
					int[][] table = slate.patchStencil[level];
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
							g.drawString(String.valueOf(GRID_START + index), column * 32 + 8, row * 32 + 24);
						}
					}
					
					int dim2 = (1 << (level - 1)) + 3;
					for (int row = 0; row < dim2; row++) {
						for (int column = 0; column < dim2; column++) {
							int index = row * dim2 + column;
							g.setColor(Color.BLACK);
							g.drawString(String.valueOf(GRID_START + index), column * 32 + 8 + 400, row * 32 + 24);
						}
					}
					for (int i = 0; i < GRID_START; i++) {
						g.drawString(String.valueOf(i), i * 32 + 8 + 400, 400);
					}
					
					if (activeX < dim && activeY < dim) {
						int index = activeY * dim + activeX;
						g.setColor(Color.BLACK);
						g.drawRect(activeX * 32, activeY * 32, 32, 32);
						
						int[] s = table[index];
						int type = s[0];
						if (activeX == 1 && activeY == 1) {
							s = slate.cornerStencil[level][1][0];
							type = POINT;
						}
						
						switch(type) {
						case FACE:
							g.drawOval(400 + (xcoord(level, s[1])) * 32, (ycoord(level, s[1])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, s[2])) * 32, (ycoord(level, s[2])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, s[3])) * 32, (ycoord(level, s[3])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, s[4])) * 32, (ycoord(level, s[4])) * 32, 32, 32);
							break;
						case EDGE:
							g.drawOval(400 + (xcoord(level, s[1])) * 32, (ycoord(level, s[1])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, s[2])) * 32, (ycoord(level, s[2])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, s[3])) * 32 + 4, (ycoord(level, s[3])) * 32 + 4, 24, 24);
							g.drawOval(400 + (xcoord(level, s[4])) * 32 + 4, (ycoord(level, s[4])) * 32 + 4, 24, 24);
							g.drawOval(400 + (xcoord(level, s[5])) * 32 + 4, (ycoord(level, s[5])) * 32 + 4, 24, 24);
							g.drawOval(400 + (xcoord(level, s[6])) * 32 + 4, (ycoord(level, s[6])) * 32 + 4, 24, 24);
							break;
						case POINT:
							g.drawOval(400 + (xcoord(level, s[1])) * 32, (ycoord(level, s[1])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, s[2])) * 32, (ycoord(level, s[2])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, s[3])) * 32, (ycoord(level, s[3])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, s[4])) * 32, (ycoord(level, s[4])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, s[5])) * 32, (ycoord(level, s[5])) * 32, 32, 32);
							g.drawOval(400 + (xcoord(level, s[6])) * 32 + 4, (ycoord(level, s[6])) * 32 + 4, 24, 24);
							g.drawOval(400 + (xcoord(level, s[7])) * 32 + 4, (ycoord(level, s[7])) * 32 + 4, 24, 24);
							g.drawOval(400 + (xcoord(level, s[8])) * 32 + 4, (ycoord(level, s[8])) * 32 + 4, 24, 24);
							g.drawOval(400 + (xcoord(level, s[9])) * 32 + 4, (ycoord(level, s[9])) * 32 + 4, 24, 24);
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
			
			slate.subdivide(3, new float[][][] {
					{ { -1, -1, 1 }, { -3, -1, 1 }, { -3, -3, 1 }, { -1, -3, 1 } },
					{ {  1, -1, 1 }, {  1, -3, 1 }, {  3, -3, 1 }, {  3, -1, 1 } },
					{ {  1,  1, 1 }, {  3,  1, 1 }, {  3,  3, 1 }, {  1,  3, 1 } },
					{ { -1,  1, 1 }, { -1,  3, 1 }, { -3,  3, 1 }, { -3,  1, 1 } }
			});
		}
		
		private int xcoord(int level, int index) {
			int dim = (1 << level - 1) + 3;
			if (index >= GRID_START) {
				return (index - GRID_START) % dim;
			} else {
				return index;
			}
		}
		
		private int ycoord(int level, int index) {
			int dim = (1 << level - 1) + 3;
			if (index >= GRID_START) {
				return (index - GRID_START) / dim;
			} else {
				return 12;
			}
		}
	}
	
	public static void main(String[] args) {
//		System.out.println(Slate.cornerIndex(0, 4, 0));
//		System.out.println(Slate.cornerIndex(0, 4, 1));
//		System.out.println(Slate.cornerIndex(0, 4, 2));
//		System.out.println();
		System.out.println(Slate.cornerIndex2(0, 1, 4, -2));
		System.out.println(Slate.cornerIndex2(0, 1, 4, -1));
		System.out.println(Slate.cornerIndex2(0, 1, 4, 0));
		System.out.println(Slate.cornerIndex2(0, 1, 4, 1));
		System.out.println(Slate.cornerIndex2(0, 1, 4, 2));
		System.out.println(Slate.cornerIndex2(0, 1, 4, 3));
		System.out.println(Slate.cornerIndex2(0, 1, 4, 4));
		System.out.println();
		System.out.println(Slate.cornerIndex2(0, 1, 3, -2));
		System.out.println(Slate.cornerIndex2(0, 1, 3, -1));
		System.out.println(Slate.cornerIndex2(0, 1, 3, 0));
		System.out.println(Slate.cornerIndex2(0, 1, 3, 1));
		System.out.println(Slate.cornerIndex2(0, 1, 3, 2));
		System.out.println(Slate.cornerIndex2(0, 1, 3, 3));
		
//		new Tester();

	}
}
