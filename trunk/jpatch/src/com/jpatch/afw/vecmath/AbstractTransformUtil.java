package com.jpatch.afw.vecmath;

import javax.vecmath.*;

public abstract class AbstractTransformUtil {
	public static final int WORLD = 0;
	private final Matrix4d[][] matrices;
	private final boolean[][] matricesValid;
	private final int numSpaces;
	private final String[] spaceNames;
	
	AbstractTransformUtil(String... spaceNames) {
		numSpaces = spaceNames.length + 1;
		this.spaceNames = new String[numSpaces];
		this.spaceNames[0] = "world";
		System.arraycopy(spaceNames, 0, this.spaceNames, 1, spaceNames.length);
		matrices = new Matrix4d[numSpaces][numSpaces];
		matricesValid = new boolean[numSpaces][numSpaces];
		for (int i = 0; i < numSpaces; i++) {
			for (int j = 0; j < numSpaces; j++) {
				matrices[i][j] = Utils3d.createIdentityMatrix4d();
				matricesValid[i][j] = true;
			}
		}
	}
	
	public void setWorld2Space(int space, Matrix4d matrix) {
		for (int i = 0; i < numSpaces; i++) {
			matricesValid[space][i] = false;
			matricesValid[i][space] = false;
		}
		matrices[WORLD][space].set(matrix);
		matricesValid[WORLD][space] = true;
	}
	
	public void setSpace2World(int space, Matrix4d matrix) {
		for (int i = 0; i < numSpaces; i++) {
			matricesValid[space][i] = false;
			matricesValid[i][space] = false;
		}
		matrices[space][WORLD].set(matrix);
		matricesValid[space][WORLD] = true;
	}
	
	public void transform(int fromSpace, Point3d fromPoint, int toSpace, Point3d toPoint) {
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].transform(fromPoint, toPoint);
	}
	
	public void transform(int fromSpace, Vector3d fromVector, int toSpace, Vector3d toVector) {
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].transform(fromVector, toVector);
	}
	
	public void transform(int fromSpace, Point3f fromPoint, int toSpace, Point3f toPoint) {
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].transform(fromPoint, toPoint);
	}
	
	public void transform(int fromSpace, Vector3f fromVector, int toSpace, Vector3f toVector) {
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].transform(fromVector, toVector);
	}
	
	public Matrix4d getMatrix(int fromSpace, int toSpace, Matrix4d matrix) {
		validateMatrix(fromSpace, toSpace);
		matrix.set(matrices[fromSpace][toSpace]);
		return matrix;
	}
	
	public Matrix4f getMatrix(int fromSpace, int toSpace, Matrix4f matrix) {
		validateMatrix(fromSpace, toSpace);
		matrix.set(matrices[fromSpace][toSpace]);
		return matrix;
	}
	
	public Matrix3d getRotationScaleMatrix(int fromSpace, int toSpace, Matrix3d matrix) {
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].getRotationScale(matrix);
		return matrix;
	}
	
	public Matrix3f getRotationScaleMatrix(int fromSpace, int toSpace, Matrix3f matrix) {
		validateMatrix(fromSpace, toSpace);
		matrices[fromSpace][toSpace].getRotationScale(matrix);
		return matrix;
	}
	
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
	
	protected void flipZAxis(int space) {
		Matrix4d m = matrices[space][WORLD];
		m.m00 = -m.m00;
		m.m01 = -m.m01;
		m.m02 = -m.m02;
		m.m20 = -m.m20;
		m.m21 = -m.m21;
		m.m22 = -m.m22;
	}
}
