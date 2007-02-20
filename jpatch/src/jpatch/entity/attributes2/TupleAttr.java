package jpatch.entity.attributes2;

import javax.vecmath.*;

public class TupleAttr<T extends Tuple3d> {
	private T tuple;
	
	private DoubleValue x = new DoubleValue() {
		return new DoubleValue() {
			public double getDouble() {
				return tuple.x;
			}

			public void setDouble(double value) {
				tuple.x = value;
			}
		};
	}
}
