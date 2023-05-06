package patterns;

/**
* This interface allows to change the slope of most patterns
**/

public interface Slope {
	/**
	* Returns an output float value (form 0 to 1) for each input float value (form 0 to 1)
	* @param pos the input position
	* @return the output position after applying the slope function
	**/
	float valueAt(float pos);
}
