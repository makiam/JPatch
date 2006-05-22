package patterns;

import javax.vecmath.*;

/**
* This class implements the Pigment3D interface. It returns a the same color for every point in space
**/

public class SolidColor implements Pigment3D {
	/** The color **/
	private Color3f color;
	
	/**
	* Creates a SolidColor object with the specified color
	* @param r the red component of the color
	* @param g the green component of the color
	* @param b the blue component of the color
	**/
	public SolidColor(float r, float g, float b) {
		this.color = new Color3f(r, g, b);
	}
	
	public Color3f colorAt(float x, float y, float z) {
		return color;
	}
}
