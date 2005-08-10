package patterns;

import javax.vecmath.*;

/**
* A ColorMap is a sorted set of colors. Each color has a position between 0 and 1 in the set.
* The method colorAt(float position) will return an interpolated color at that position
**/
public class ColorMap extends ObjectMap{

	/**
	* Factory method to create a new ColorMap which fades from black (at position 0) to white (at position 1)
	**/
	public static ColorMap createBlackToWhiteColorMap() {
		ColorMap cm = new ColorMap();
		cm.addEntry(0, new Color3f(0,0,0));
		cm.addEntry(1, new Color3f(1,1,1));
		return cm;
	}
	
	/**
	* The map must be filled in ascending order, starting with an entry at position 0 and ending with an entry
	* at position 1. It is possible to create two color entries at the same position.
	* @param position position for this color map entry. Must be between 0 and 1 and in ascending order!
	* @param color the color to add at the specified position
	**/
	public void addEntry(float position, Color3f color) {
		super.addEntry(position, color);
	}
	
	/**
	* Returns the interpolated color at a given position. This implementation linearly interpolates between
	* the previous and the next color for the specified position.
	* @param position the position (between 0 and 1)
	* @return the interpolated color at the specified position
	**/
	public Color3f colorAt(float position) {
		if (iState != COMPLETE) throw new IllegalStateException("This colormap is not complete");
		else if (position <= 0) return (Color3f) map[0].object;
		else if (position >= 1) return (Color3f) map[map.length - 1].object;
		else {
			int i = lowIndex(position);
			float a = map[i].position;
			float b = map[i + 1].position;
			float s = (position - a) / (b - a);
			//System.out.println("s = " + s);
			Color3f ca = (Color3f) map[i].object;
			Color3f cb = (Color3f) map[i + 1].object;
			return new Color3f(ca.x + s * (cb.x - ca.x), ca.y + s * (cb.y - ca.y), ca.z + s * (cb.z - ca.z));
		}
	}
}
