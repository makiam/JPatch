package jpatch.entity.attributes2;

import javax.vecmath.*;

public class Tuple2 {
	protected final DoubleAttr xAttr;
	protected final DoubleAttr yAttr;
	
	public Tuple2() {
		this(new DoubleAttr(), new DoubleAttr());
	}
	
	public Tuple2(double x, double y) {
		this(new DoubleAttr(x), new DoubleAttr(y));
	}
	
	public Tuple2(DoubleAttr x, DoubleAttr y) {
		xAttr = x;
		yAttr = y;
	}
	
	public DoubleAttr getXAttr() {
		return xAttr;
	}
	
	public DoubleAttr getYAttr() {
		return yAttr;
	}
	
	public double getX() {
		return xAttr.getDouble();
	}
	
	public double getY() {
		return yAttr.getDouble();
	}
	
	public void setX(double x) {
		xAttr.setDouble(x);
	}
	
	public void setY(double y) {
		yAttr.setDouble(y);
	}
	
	public void getTuple(Tuple2d tuple) {
		tuple.x = xAttr.getDouble();
		tuple.y = yAttr.getDouble();
	}
	
	public void getTuple(Tuple2f tuple) {
		tuple.x = (float) xAttr.getDouble();
		tuple.y = (float) yAttr.getDouble();
	}
	
	public void setTuple(Tuple2 tuple) {
		setTuple(tuple.getX(), tuple.getY());
	}
	
	public void setTuple(Tuple2d tuple) {
		setTuple(tuple.x, tuple.y);
	}
	
	public void setTuple(Tuple2f tuple) {
		setTuple(tuple.x, tuple.y);
	}
	
	public void setTuple(double x, double y) {
		xAttr.setDouble(x);
		yAttr.setDouble(y);
	}
}
