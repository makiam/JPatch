/*
 * Created on Feb 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package inyo;
import javax.vecmath.*;

/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RtHalton3d {	
		
	double invBase[] = new double[3];
	double prev[] = new double[3];

	public RtHalton3d() {
		// initialize the inverse bases
		this.invBase[0] = 1./2;
		this.invBase[1] = 1./3;
		this.invBase[2] = 1./4;
		
		// reset the initial values
		this.prev[0] = 0;
		this.prev[1] = 0;
		this.prev[2] = 0;
	}

		
	// return a single value from a Halton sequence
	final double halton( int index ) {
	
		double r = 1 - this.prev[index] - 1e-10;
		if (invBase[index] < r) {
			// store result as previous value
			this.prev[index] = this.prev[index] + this.invBase[index];
			return this.prev[index];
		}
			
		double h = this.invBase[index];
		double hh;
		do {
			hh = h;
			h *= invBase[index];
		} while (h >= r);
		
		// store value as previous value
		prev[index] = this.prev[index] + hh + h - 1;
		
		return prev[index];
	}
		
	/**
	 * Reset the Halton sequence.
	 *
	 */
	void reset() {
		this.prev[0] = 0;
		this.prev[1] = 0;
		this.prev[2] = 0;
	}
		
	/**
	 * Return the next Point3D in the Halton sequence
	 * @return Next point in the Halton sequence
	 */
	Point3d GetNext() {
		// call the halton sequence generator for each dimension
		return new Point3d( 
				halton(0), 
				halton(1), 
				halton(2));
	}
		
}
