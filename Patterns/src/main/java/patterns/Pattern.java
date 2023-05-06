package patterns;

/**
* The interface for patterns. A pattern must return a float between 0 and 1 for every point in 2D-, 3D- or 4D space.
**/

public interface Pattern {
	/**
	* Computes the pattern value at a given 2D coordinate
	* @param u the u coordinate
	* @param v the v coordinate
	* @return the patterns value at (u,v) - a float between 0 and 1
	**/
	float valueAt(float u, float v);
	
	/**
	* Computes the pattern value at a given 3D coordinate
	* @param x the x coordinate
	* @param y the y coordinate
	* @param z the z coordinate
	* @return the patterns value at (x,y,z) - a float between 0 and 1
	**/
	float valueAt(float x, float y, float z);
	
	/**
	* Computes the pattern value at a given 4D coordinate
	* @param x the x coordinate
	* @param y the y coordinate
	* @param z the z coordinate
	* @return the patterns value at (x,y,z,w) - a float between 0 and 1
	**/
	float valueAt(float x, float y, float z, float w);
}
