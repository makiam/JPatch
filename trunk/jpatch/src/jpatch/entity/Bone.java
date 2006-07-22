package jpatch.entity;

import javax.vecmath.*;

import jpatch.auxilary.*;

import jpatch.auxilary.Rotation3d;

public class Bone extends AbstractTransform {
	public static enum WeightingMode {
		RIGID, SOFT, SMOOTH;
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	};
	
	public Attribute.Enum rotationOrder = new Attribute.Enum("Order", Rotation3d.Order.XYZ);
	public Attribute.Enum axisRotationOrder = new Attribute.Enum("Order", Rotation3d.Order.XYZ);
	public Attribute.Vector3d extent = new Attribute.Vector3d("Extent", new Vector3d(0, 0, 1), false);
	public Attribute.Vector3d up = new Attribute.Vector3d("Up", new Vector3d(0, 1, 0), false);
	public Attribute.Rotation3d axisRotation = new Attribute.Rotation3d("Axis Rotation", new Rotation3d(), false);
	public Attribute.Vector3d translation = new Attribute.Vector3d("Translation", new Vector3d(), false);
	public Attribute.Vector3d position = new Attribute.Vector3d("Position", new Vector3d(), false);
	public Attribute.Rotation3d rotation = new Attribute.Rotation3d("Rotation", new Rotation3d(), true);
	public Attribute.Rotation3d orientation = new Attribute.Rotation3d("Orientation", new Rotation3d(), false);
	public Attribute.Enum weightingX = new Attribute.Enum("Weighting X", WeightingMode.RIGID);
	public Attribute.Enum weightingY = new Attribute.Enum("Weighting Y", WeightingMode.RIGID);
	public Attribute.Enum weightingZ = new Attribute.Enum("Weighting Z", WeightingMode.RIGID);
	public Attribute.Scale3d scale = new Attribute.Scale3d("Scale", new Scale3d(1, 1, 1), true);
	
	private Rotation3d axisRotationTuple = new Rotation3d();
	private Vector3d translationTuple = new Vector3d();
	private Rotation3d rotationTuple = new Rotation3d();
	private Vector3d extentTuple = new Vector3d();
	private Scale3d scaleTuple = new Scale3d();
	private Matrix3d rotationMatrix = new Matrix3d();
	private Matrix3d scaleMatrix = new Matrix3d();
	
	public void setParent(JPatchObject parent) {
		parent = (Bone) parent;
	}

	protected void computeMatrix() {
		scale.get(scaleTuple);
		scaleTuple.setMatrixScale(scaleMatrix);
		rotation.get(rotationTuple);
		rotationTuple.setMatrixRotation(rotationMatrix);
		scaleMatrix.mul(rotationMatrix);
		matrix.setRotationScale(scaleMatrix);
		translation.get(translationTuple);
		matrix.setTranslation(translationTuple);
		if (parent != null) {
			parent.multiply(matrix);
		}
		inverseInvalid = true;
	}	
}
