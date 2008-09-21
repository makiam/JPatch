package jpatch.entity.attributes2;

public class LinearMapping implements Mapping {
	private static final LinearMapping INSTANCE = new LinearMapping();
	
	public static Mapping getInstance() {
		return INSTANCE;
	}
	
	private LinearMapping() {
		// singleton pattern
	}
	
	public double getMappedValue(double value) {
		return value;
	}

	public double getValue(double mappedValue) {
		return mappedValue;
	}

}
