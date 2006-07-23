package jpatch.entity;

import javax.vecmath.*;

import jpatch.auxilary.*;

import jpatch.auxilary.Rotation3d;

public class Bone extends AbstractTransform {
	private static int col = 0;
	private static final Color3f[] COLORS = new Color3f[] {
		new Color3f(1, 0, 0),
		new Color3f(0, 1, 0),
		new Color3f(0, 0, 1),
		new Color3f(1, 1, 0),
		new Color3f(1, 0, 1),
		new Color3f(0, 1, 1),
	};
	
	public static enum WeightingMode {
		RIGID, SOFT, SMOOTH;
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	};
	
	public Attribute.Tuple color = new Attribute.Tuple("Color", COLORS[col++ % COLORS.length], false);
	public Attribute.Enum rotationOrder = new Attribute.Enum("Order", Rotation3d.Order.XYZ);
	public Attribute.Enum axisRotationOrder = new Attribute.Enum("Order", Rotation3d.Order.XYZ);
	public Attribute.Tuple extent = new Attribute.Tuple("Extent", 0, 0, 1, false);
	public Attribute.Tuple up = new Attribute.Tuple("Up", 0, 1, 0, false);
	public Attribute.Tuple axisRotation = new Attribute.Tuple("Axis Rotation", 0, 0, 0, false);
	public Attribute.Tuple translation = new Attribute.Tuple("Translation", 0, 0, 0, false);
	public Attribute.Tuple position = new Attribute.Tuple("Position", 0, 0, 0, false);
	public Attribute.Tuple rotation = new Attribute.Tuple("Rotation", 0, 0, 0, true);
	public Attribute.Tuple orientation = new Attribute.Tuple("Orientation", 0, 0, 0, false);
	public Attribute.Enum weightingX = new Attribute.Enum("Weighting X", WeightingMode.RIGID);
	public Attribute.Enum weightingY = new Attribute.Enum("Weighting Y", WeightingMode.RIGID);
	public Attribute.Enum weightingZ = new Attribute.Enum("Weighting Z", WeightingMode.RIGID);
	public Attribute.Tuple scale = new Attribute.Tuple("Scale", 1, 1, 1, true);
	
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
