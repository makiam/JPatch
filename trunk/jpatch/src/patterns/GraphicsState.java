package patterns;

import java.util.*;
import javax.vecmath.*;

/**
* The GraphicsState allows to store transformation matrices (of type javax.vecmath.Matrix4f) on a stack and to
* apply transformations to those matrices
**/

public class GraphicsState {
	private List listTransforms = new ArrayList();
	private Matrix4f m4CurrentTransform;
	
	public GraphicsState() {
		m4CurrentTransform = new Matrix4f();
		m4CurrentTransform.setIdentity();
	}
	
	/**
	* Begins a new transformation. The current transformation is pushed on the stack and a new
	* current transformation will be created (being a copy of the old one)
	**/
	public void transformBegin() {
		listTransforms.add(m4CurrentTransform);
		m4CurrentTransform = new Matrix4f();
		m4CurrentTransform.setIdentity();
	}
	
	/**
	* Ends a transformation. The current transformation will be popped from the stack
	**/
	public void transformEnd() {
		m4CurrentTransform = (Matrix4f) listTransforms.get(listTransforms.size() - 1);
		listTransforms.remove(listTransforms.size() - 1);
	}
	
	/**
	* Applies a translation to the current transformation.
	* @param x the x part of the translation
	* @param y the y part of the translation
	* @param z the z part of the translation
	**/
	public void translate(float x, float y, float z) {
		Matrix4f matrix = new Matrix4f( 1, 0, 0, x,
						0, 1, 0, y,
						0, 0, 1, z,
						0, 0, 0, 1);
		m4CurrentTransform.mul(matrix);
	}
	
	/**
	* Applies a unifor scale operation to the current transformation.
	* @param s the amount to scale
	**/
	public void scale(float s) {
		scale(s, s, s);
	}
	
	/**
	* Applies a scale operation to the current transformation.
	* @param x the x part of the scale operation
	* @param y the y part of the scale operation
	* @param z the z part of the scale operation
	**/
	public void scale(float x, float y, float z) {
		Matrix4f matrix = new Matrix4f( 1f / x, 0, 0, 0,
						0, 1f / y, 0, 0,
						0, 0, 1f / z, 0,
						0, 0, 0, 1);
		m4CurrentTransform.mul(matrix);
	}
	
	/**
	* Applies a rotation to the current transformation
	* @param x the angle (in degrees) to rotate around the x axis
	* @param y the angle (in degrees) to rotate around the y axis
	* @param z the angle (in degrees) to rotate around the z axis
	**/
	public void rotate(float x, float y, float z) {
		Matrix4f matrix = new Matrix4f();
		matrix.rotX(x / 180f * (float) Math.PI);
		m4CurrentTransform.mul(matrix);
		matrix.rotY(y / 180f * (float) Math.PI);
		m4CurrentTransform.mul(matrix);
		matrix.rotZ(z / 180f * (float) Math.PI);
		m4CurrentTransform.mul(matrix);
	}
	
	/**
	* Applies a matrix transformation to the current transformation
	* @param matrix the transformation to apply
	**/
	public void transform(Matrix4f matrix) {
		m4CurrentTransform.mul(matrix);
	}
	
	/**
	* Returns a copy of the current transformation matrix
	**/
	public Matrix4f getMatrix() {
		return new Matrix4f(m4CurrentTransform);
	}
}
