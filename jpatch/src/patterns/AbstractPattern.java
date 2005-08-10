package patterns;

/**
* An abstract class implementing the Pattern interface. Subclasses must at least override the
* valueAt(float x, float y, float z) method.
**/

public abstract class AbstractPattern implements Pattern {
	/**
	* This implementation calls valueAt(float x, float y, float z), with x set to the specified
	* u coordinate, y set to the specified v coordinate and z set to 0.
	**/
	public float valueAt(float u, float v) {
		return valueAt(u, v, 0);
	}
	
	/**
	* Subclasses must override this method. It throws an UnsupportedOperationException.
	**/
	public float valueAt(float x, float y, float z) {
		throw new UnsupportedOperationException();
	}
	
	/**
	* This implementation calls valueAt(float x, float y, float z), the specified w coordinate
	* will be ignored.
	**/
	public float valueAt(float x, float y, float z, float w) {
		return valueAt(x, y, z);
	}
}

