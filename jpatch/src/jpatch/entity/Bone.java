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
	
	public ScalarAttribute.Tuple3Attr color = new ScalarAttribute.Tuple3Attr("Color", COLORS[col++ % COLORS.length], false);
	public ScalarAttribute.Enum rotationOrder = new ScalarAttribute.Enum(Rotation3d.Order.XYZ);
	public ScalarAttribute.Enum axisRotationOrder = new ScalarAttribute.Enum(Rotation3d.Order.XYZ);
	public ScalarAttribute.Tuple3Attr extent = new ScalarAttribute.Tuple3Attr("Extent", 0, 0, 1, false);
	public ScalarAttribute.Tuple3Attr up = new ScalarAttribute.Tuple3Attr("Up", 0, 1, 0, false);
	public ScalarAttribute.Tuple3Attr axisRotation = new ScalarAttribute.Tuple3Attr("Axis Rotation", 0, 0, 0, false);
	public ScalarAttribute.Tuple3Attr translation = new ScalarAttribute.Tuple3Attr("Translation", 0, 0, 0, false);
	public ScalarAttribute.Tuple3Attr position = new ScalarAttribute.Tuple3Attr("Position", 0, 0, 0, false);
	public ScalarAttribute.Tuple3Attr rotation = new ScalarAttribute.Tuple3Attr("Rotation", 0, 0, 0, true);
	public ScalarAttribute.Tuple3Attr orientation = new ScalarAttribute.Tuple3Attr("Orientation", 0, 0, 0, false);
	public ScalarAttribute.Enum weightingX = new ScalarAttribute.Enum(WeightingMode.RIGID);
	public ScalarAttribute.Enum weightingY = new ScalarAttribute.Enum(WeightingMode.RIGID);
	public ScalarAttribute.Enum weightingZ = new ScalarAttribute.Enum(WeightingMode.RIGID);
	public ScalarAttribute.Tuple3Attr scale = new ScalarAttribute.Tuple3Attr("Scale", 1, 1, 1, true);
	
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
