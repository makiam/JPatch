package com.jpatch.afw.vecmath;

import java.util.Arrays;

import javax.vecmath.*;

/**
 * A utility that allows to transform points and vectors between
 * various spaces. This class is abstract and therefor intended
 * to be subclasses. It defines only one space (space 0, world space)
 * and subclasses should add other spaced during construction.
 */
public abstract class AbstractTransformUtil {
	/** constant identifying world space */
	public static final int WORLD = 0;
	/** a 2D array of matrices used to transform between arbitrary spaces */
	private final Matrix4d[][] matrices;
	/** a 2D array of flags to keep track of valid/invalid matrices for lazy evaluation */
	private final boolean[][] matricesValid;
	/** the names of the spaces */
	private final String[] spaceNames;
	
	/**
	 * Creates a new AbstractTransformUtil with the specified spaces.
	 * The specified spaces will be added to the predefined WORLD space,
	 * so the first specified space will be #1 (world is #0)
	 * @param spaceNames
	 */
	protected AbstractTransformUtil(String... additionalSpaceNames) {
		spaceNames = concatenateSpaceNames(new String[] { "world" }, additionalSpaceNames);
		matrices = new Matrix4d[spaceNames.length][spaceNames.length];
		matricesValid = new boolean[spaceNames.length][spaceNames.length];
		for (int i = 0; i < spaceNames.length; i++) {
			for (int j = 0; j < spaceNames.length; j++) {
				matrices[i][j] = Utils3d.createIdentityMatrix4d();
				matricesValid[i][j] = true;
			}
		}
	}
	
	protected static String[] concatenateSpaceNames(String[] start, String[] end) {
		String[] concat = new String[start.length + end.length];
		System.arraycopy(start, 0, concat, 0, start.length);
		System.arraycopy(end, 0, concat, start.length, end.length);
		return concat;
	}
	
	/**
	 * Sets the transformation matrix for transforming from world space
	 * to the specified space.
	 * @param space
	 * @param matrix
	 */
	public void setWorld2Space(int space, Matrix4d matrix) {
		setWorld2SpaceImpl(space, matrix);
	}
	
	/**
	 * Sets the transformation matrix for transforming from world space
	 * to the specified toSpace via the specified fromSpace. The specified matrix
	 * represents the transformation from fromSpace to toSpace.
	 * @param fromSpace
	 * @param toSpace
	 * @param matrix
	 * @throws IllegalStateException if the World to fromSpace matrix can't be validated
	 */
	public void setWorldToSpace(int fromSpace, int toSpace, Matrix4d matrix) {
		validateMatrix(WORLD, fromSpace);
		setWorld2SpaceImpl(toSpace, matrix);
		matrices[WORLD][toSpace].mul(matrices[WORLD][fromSpace]);
	}
	
	/**
	 * private implementation of the setWorld2Space method
	 */
	private final void setWorld2SpaceImpl(int space, Matrix4d matrix) {
		for (int i = 0; i < spaceNames.length; i++) {
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
		setSpace2WorldImpl(space, matrix);
	}
	
	/**
	 * Sets the transformation matrix for transforming from the specified fromSpace
	 * to world space via the specified toSpace. The specified matrix
	 * represents the transformation from fromSpace to toSpace.
	 * @param fromSpace
	 * @param toSpace
	 * @param matrix
	 * @throws IllegalStateException if the toSpace to world matrix can't be validated
	 */
	public void setSpace2World(int fromSpace, int toSpace, Matrix4d matrix) {
		validateMatrix(toSpace, WORLD);
		setSpace2WorldImpl(fromSpace, matrices[toSpace][WORLD]);
		matrices[fromSpace][WORLD].mul(matrix);
	}
	
	/**
	 * private implementation of the setSpace2World method
	 */
	private final void setSpace2WorldImpl(int space, Matrix4d matrix) {
		for (int i = 0; i < spaceNames.length; i++) {
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
	 * Sets the specified matrix to the rotatisetSpace2Worldon/scale component of the matrix
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
	 * Sets the scale of the transformation matrix used to transform from fromSpace to
	 * toSpace to the specified value. Either fromSpace or toSpace must be WORLD.
	 * @param fromSpace
	 * @param toSpace
	 * @param scale
	 * @throws IllegalArgumentException if neither fromSpace nor toSpace == WORLD
	 * @throws IllegalStateException if the formSpace->toSpace matrix can't be validated
	 */
	public void setScale(int fromSpace, int toSpace, double scale) {
		if (fromSpace != WORLD && toSpace != WORLD) {
			throw new IllegalArgumentException("neither fromSpace nor toSpace == world");
		}
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].setScale(scale);
		if (fromSpace == WORLD) {
			setWorld2SpaceImpl(toSpace, matrices[fromSpace][toSpace]);
		} else {
			setSpace2WorldImpl(fromSpace, matrices[fromSpace][toSpace]);
		}
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
	public void flipZAxis(int space) {
		Matrix4d m = matrices[space][WORLD];
		m.m00 = -m.m00;
		m.m01 = -m.m01;
		m.m02 = -m.m02;
		m.m20 = -m.m20;
		m.m21 = -m.m21;
		m.m22 = -m.m22;
	}
	
	public String getValidMatrices() {
		matricesValid[0][0] = false;
		StringBuilder sb = new StringBuilder();
		int maxStringLength = 0;
		for (int i = 0; i < spaceNames.length; i++) {
			maxStringLength = Math.max(maxStringLength, spaceNames[i].length());
		}
		char[] spaces = new char[maxStringLength + 1];
		Arrays.fill(spaces, ' ');
		sb.append(spaces);
		for (int i = 0; i < spaceNames.length; i++) {
			sb.append(spaceNames[i]).append(' ');
		}
		sb.append('\n');
		for (int to = 0; to < spaceNames.length; to++) {
			sb.append(spaceNames[to]);
			sb.append(spaces, 0, maxStringLength - spaceNames[to].length());
			sb.append(' ');
			for (int from = 0; from < spaceNames.length; from++) {
				sb.append(spaces, 0, spaceNames[from].length() / 2);
				if (matricesValid[from][to]) {
					sb.append('\u2612');
				} else {
					sb.append('\u2610');
				}
				sb.append(spaces, 0, spaceNames[from].length() - spaceNames[from].length() / 2 - 1);
				sb.append(' ');
			}
			sb.append('\n');
		}
		return sb.toString();
	}
}
