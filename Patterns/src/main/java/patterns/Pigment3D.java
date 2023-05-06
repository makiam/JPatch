package patterns;

import javax.vecmath.*;

/**
* The interface for 3D pigments. A pigment must return a color for every point in 3D space.
* The return type is of javax.vecmath.Color3f
**/

public interface Pigment3D {
	/**
	* Computes the color for a given point in 3D space
	* @param x the x coordinate
	* @param y the y coordinate
	* @param z the z coordinate
	* @return the color at (x,y,z)
	**/
	Color3f colorAt(float x, float y, float z);
}
