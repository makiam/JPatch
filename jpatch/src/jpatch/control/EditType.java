package jpatch.control;

public enum EditType {
	DELETE_CONTROLPOINT, REMOVE_CONTROLPOINT,
	ADD_CURVE_SEGMENT;
	
	@Override
	public String toString() {
		return name().toLowerCase().replace('_', ' ');
	}
}
