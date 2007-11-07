package com.jpatch.afw.vecmath;

import javax.vecmath.*;

/**
 * A utility that allows to transform points and vectors between
 * various spaces. This class is abstract and therefor intended
 * to be subclasses. It defines only one space (space 0, world space)
 * and subclasses should add other spaced during construction.
 */
public abstract class AbstractTransformUtil {
	private static final int MAX = 10;
	/** a 2D array of matrices used to transform between arbitrary spaces */
	private final Matrix4d[][] matrices = new Matrix4d[MAX][MAX];
	/** a 2D array of flags to keep track of valid/invalid matrices for lazy evaluation */
	private final boolean[][] matricesValid = new boolean[MAX][MAX];
	/** the number of spaces */
	private int numSpaces = 0;
	/** the names of the spaces */
	private final String[] spaceNames = new String[10];
	/** constant identifying world space */
	public final int WORLD = addSpace("world");
	
	public int addSpace(String spaceName) {
		/* search next free space */
		for (int space = 0; space < MAX; space++) {
			if (spaceNames[space] == null) {
				spaceNames[space] = spaceName;
				computeNumSpaces();
				for (int i = 0; i < numSpaces; i++) {
					matrices[space][i] = new Matrix4d();
					matricesValid[space][i] = false;
					if (space != i) {
						matrices[i][space] = new Matrix4d();
						matricesValid[i][space] = false;
					}
				}
				return space;
			}
		}
		throw new IllegalStateException("All spaces are occupied (max=" + MAX + ")");
	}
	
	public void removeSpace(int space) {
		spaceNames[space] = null;
		for (int i = 0; i < numSpaces; i++) {
			matrices[space][i] = null;
			matricesValid[space][i] = false;
			if (space != i) {
				matrices[i][space] = null;
				matricesValid[i][space] = false;
			}
		}
		computeNumSpaces();
	}
	
	private void computeNumSpaces() {
		for (int space = MAX - 1; space >= 0; space--) {
			if (spaceNames[space] != null) {
				numSpaces = space + 1;
				return;
			}
		}
		numSpaces = 0;
	}
	
	/**
	 * Sets the transformation matrix for transforming from world space
	 * to the specified space.
	 * @param space
	 * @param matrix
	 */
	public void setWorld2Space(int space, Matrix4d matrix) {
		for (int i = 0; i < numSpaces; i++) {
			matricesValid[space][i] = false;
			matricesValid[i][space] = false;
		}
		matrices[WORLD][space].set(matrix);
		matricesValid[WORLD][space] = true;
	}
	
	/**
	 * Sets the transformation matrix for transforming from the specified space
	 * to world space.
	 * @param space
	 * @param matrix
	 */
	public void setSpace2World(int space, Matrix4d matrix) {
		for (int i = 0; i < numSpaces; i++) {
			matricesValid[space][i] = false;
			matricesValid[i][space] = false;
		}
		matrices[space][WORLD].set(matrix);
		matricesValid[space][WORLD] = true;
	}
	
	/**
	 * Sets toPoint to the coordinates of fromPoint, transformed from fromSpace to toSpace
	 * @param fromSpace
	 * @param fromPoint
	 * @param toSpace
	 * @param toPoint
	 */
	public void transform(int fromSpace, Point3d fromPoint, int toSpace, Point3d toPoint) {
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].transform(fromPoint, toPoint);
	}
	
	/**
	 * Sets toVector to the coordinates of fromVector, transformed from fromSpace to toSpace
	 * @param fromSpace
	 * @param fromVector
	 * @param toSpace
	 * @param toVector
	 */
	public void transform(int fromSpace, Vector3d fromVector, int toSpace, Vector3d toVector) {
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].transform(fromVector, toVector);
	}
	
	/**
	 * Sets toPoint to the coordinates of fromPoint, transformed from fromSpace to toSpace
	 * @param fromSpace
	 * @param fromPoint
	 * @param toSpace
	 * @param toPoint
	 */
	public void transform(int fromSpace, Point3f fromPoint, int toSpace, Point3f toPoint) {
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].transform(fromPoint, toPoint);
	}
	
	/**
	 * Sets toVector to the coordinates of fromVector, transformed from fromSpace to toSpace
	 * @param fromSpace
	 * @param fromVector
	 * @param toSpace
	 * @param toVector
	 */
	public void transform(int fromSpace, Vector3f fromVector, int toSpace, Vector3f toVector) {
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].transform(fromVector, toVector);
	}
	
	/**
	 * Sets the specified matrix to the transformation matrix for transforming
	 * from fromSpace to toSpace
	 * @param fromSpace
	 * @param toSpace
	 * @param matrix
	 * @return
	 */
	public Matrix4d getMatrix(int fromSpace, int toSpace, Matrix4d matrix) {
		validateMatrix(fromSpace, toSpace);
		matrix.set(matrices[fromSpace][toSpace]);
		return matrix;
	}
	
	/**
	 * Sets the specified matrix to the transformation matrix for transforming
	 * from fromSpace to toSpace
	 * @param fromSpace
	 * @param toSpace
	 * @param matrix
	 * @return
	 */
	public Matrix4f getMatrix(int fromSpace, int toSpace, Matrix4f matrix) {
		validateMatrix(fromSpace, toSpace);
		matrix.set(matrices[fromSpace][toSpace]);
		return matrix;
	}
	
	/**
	 * Sets the specified matrix to the rotation/scale component of the matrix
	 * for transforming from fromSpace to toSpace
	 * @param fromSpace
	 * @param toSpace
	 * @param matrix
	 * @return
	 */
	public Matrix3d getRotationScaleMatrix(int fromSpace, int toSpace, Matrix3d matrix) {
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].getRotationScale(matrix);
		return matrix;
	}
	
	/**
	 * Sets the specified matrix to the rotation/scale component of the matrix
	 * for transforming from fromSpace to toSpace
	 * @param fromSpace
	 * @param toSpace
	 * @param matrix
	 * @return
	 */
	public Matrix3f getRotationScaleMatrix(int fromSpace, int toSpace, Matrix3f matrix) {
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].getRotationScale(matrix);
		return matrix;
	}
	/**
	 * Checks wheter the matrix for transforming from fromSpace to toSpace is valid.
	 * If not, the matrix is computed.
	 * @param fromSpace
	 * @param toSpace
	 * @throws IllegalStateException if the matrix cannot be computed (should never happen)
	 */
	private void validateMatrix(int fromSpace, int toSpace) {
		if (matricesValid[fromSpace][toSpace]) {
			return;
		}
		if (fromSpace == WORLD) {
			if (!matricesValid[WORLD][toSpace]) {
				if (matricesValid[toSpace][WORLD]) {
					matrices[WORLD][toSpace].invert(matrices[toSpace][WORLD]);
					matricesValid[WORLD][toSpace] = true;
					return;
				} else {
					throw new IllegalStateException("transform " + spaceNames[toSpace] + " -> world is invalid, can't invert");
				}
			}
		}
		if (toSpace == WORLD) {
			if (!matricesValid[fromSpace][WORLD]) {
				if (matricesValid[WORLD][fromSpace]) {
					matrices[fromSpace][WORLD].invert(matrices[WORLD][fromSpace]);
					matricesValid[fromSpace][WORLD] = true;
					return;
				} else {
					throw new IllegalStateException("transform WORLD -> " + spaceNames[fromSpace] + " is invalid, can't invert");
				}
			}
		}
		validateMatrix(WORLD, toSpace);
		validateMatrix(fromSpace, WORLD);
		matrices[fromSpace][toSpace].set(matrices[WORLD][toSpace]);
		matrices[fromSpace][toSpace].mul(matrices[fromSpace][WORLD]);
		matricesValid[fromSpace][toSpace] = true;
	}
	
	/**
	 * Flips the z-axis of the space-to-world matrix for the specified space
	 * Needed for cameras to turn them to face down the positive z-axis.
	 * @param space
	 */
	protected void flipZAxis(int space) {
		Matrix4d m = matrices[space][WORLD];
		m.m00 = -m.m00;
		m.m01 = -m.m01;
		m.m02 = -m.m02;
		m.m20 = -m.m20;
		m.m21 = -m.m21;
		m.m22 = -m.m22;
	}
	
	void dump() {
		for (int i = 0; i < MAX; i++) {
			for (int j = 0; j < MAX ; j++) {
				System.out.print((matrices[i][j] == null) ? ". " : "* ");
			}
			System.out.println();
		}
	}
}
