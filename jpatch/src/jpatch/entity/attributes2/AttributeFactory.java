package jpatch.entity.attributes2;

public class AttributeFactory {
	public static Tuple3 createTuple3(double x, double y, double z) {
		return new Tuple3(x, y, z);
	}
	
	public static Tuple3 createHardBoundedTuple3(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return new Tuple3(
				new HardBoundedDoubleAttr(minX, maxX),
				new HardBoundedDoubleAttr(minY, maxY),
				new HardBoundedDoubleAttr(minZ, maxZ)
		);
	}
	
	public static Tuple3 createSoftBoundedTuple3() {
		return new Tuple3(
				new SoftBoundedDoubleAttr(0, 0),
				new SoftBoundedDoubleAttr(0, 0),
				new SoftBoundedDoubleAttr(0, 0)
		);
	}
	
	public static TransformedPoint3 createTransformedPoint3(double x, double y, double z) {
		return new TransformedPoint3(new DoubleAttr(x), new DoubleAttr(y), new DoubleAttr(z), false);
	}
	
	public static TransformedPoint3 createHardBoundedTransformedPoint3(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return new TransformedPoint3(
				new HardBoundedDoubleAttr(minX, maxX),
				new HardBoundedDoubleAttr(minY, maxY),
				new HardBoundedDoubleAttr(minZ, maxZ),
				true
		);
	}
	
	public static TransformedPoint3 createSoftBoundedTransformedPoint3() {
		return new TransformedPoint3(
				new SoftBoundedDoubleAttr(0, 0),
				new SoftBoundedDoubleAttr(0, 0),
				new SoftBoundedDoubleAttr(0, 0),
				true
		);	
	}
	
	public static TransformedVector3 createTransformedVector3(double x, double y, double z) {
		return new TransformedVector3(new DoubleAttr(x), new DoubleAttr(y), new DoubleAttr(z), false);
	}
	
	public static TransformedVector3 createHardBoundedTransformedVector3(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return new TransformedVector3(
				new HardBoundedDoubleAttr(minX, maxX),
				new HardBoundedDoubleAttr(minY, maxY),
				new HardBoundedDoubleAttr(minZ, maxZ),
				true
		);
	}
	
	public static TransformedVector3 createSoftBoundedTransformedVector3() {
		return new TransformedVector3(
				new SoftBoundedDoubleAttr(0, 0),
				new SoftBoundedDoubleAttr(0, 0),
				new SoftBoundedDoubleAttr(0, 0),
				true
		);	
	}
}
