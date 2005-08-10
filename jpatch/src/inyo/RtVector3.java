// Copyright (c) 2004 David Cuny
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
// associated documentation files (the "Software"), to deal in the Software without restriction, including 
// without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
// sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
// subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in all copies or substantial 
// portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT 
// NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
// IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
// SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


/**
 * 
 * @author David Cuny
 *
 * Supports 3D points and color tuples.
 * 
 */

package inyo;

class RtVector3 {
	
    double x, y, z;
    
    /**
     * Create a new vector <b>{0, 0, 0}</b>.
     *
     */
    public RtVector3() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    /**
     * Create a new vector <b>{x, y, z}</b>.
     * 
     * @param x
     * @param y
     * @param z
     */
    public RtVector3( double x, double y, double z ) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Clones vector <b>p</b>
     * 
     * @param p
     */
    RtVector3 ( RtVector3 p ) {
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
    }
    
    /**
     * Returns cross product with vector <b>p</b>.
     * 
     * @param p
     * @return
     */
    final RtVector3 cross( RtVector3 p ) {            
        return new RtVector3( 
            (this.y * p.z) - (this.z * p.y),
            (this.z * p.x) - (this.x * p.z),
            (this.x * p.y) - (this.y * p.x) );            
    }

    /**
     * Returns cross product with <b>{x, y, z}</b>.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    final RtVector3 cross( double x, double y, double z ) {            
        return new RtVector3( 
            (this.y * z) - (this.z * y),
            (this.z * x) - (this.x * z),
            (this.x * y) - (this.y * x) );            
    }

    /**
     * Negates the vector.
     *
     */
    final void negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
    }
    
    /**
     * Adds vector <b>p</b> to the vector.
     * 
     * @param p
     */
    final void add( RtVector3 p ) {
        this.x += p.x;
        this.y += p.y;
        this.z += p.z;
    }
    
    /**
     * Adds vector <b>{x, y, z}</b> to the vector.
     * 
     * @param x
     * @param y
     * @param z
     */
    final void add( double x, double y, double z ) {
    	this.x += x;
    	this.y += y;
    	this.z += z;
    }

    /**
     * Adds vector <b>p1</b> to <b>p2</b> and stores the result in this vector.
     * @param p1
     * @param p2
     */
    final void add( RtVector3 p1, RtVector3 p2 ) {
        this.x = p1.x + p2.x;
        this.y = p1.y + p2.y;
        this.z = p1.z + p2.z;
    }

    
    /**
     * Subtracts vector <b>p</b> from the vector.
     * 
     * @param p
     */
    final void sub( RtVector3 p ) {
        this.x -= p.x;
        this.y -= p.y;
        this.z -= p.z;
    }

    /**
     * Subtracts vector <b>p1</b> from <b>p2</b> and stores the result in this vector.
     * 
     * @param p1
     * @param p2
     */
    final void sub( RtVector3 p1, RtVector3 p2 ) {
        this.x = p1.x - p2.x;
        this.y = p1.y - p2.y;
        this.z = p1.z - p2.z;
    }
    
    /**
     * Multiples vector by the vector <b>p</b>.
     * 
     * @param p
     */
    final void mul( RtVector3 p ) {
        this.x *= p.x;
        this.y *= p.y;
        this.z *= p.z;
    }

    /**
     * Multiplies vector <b>p1</b> by <b>p2</b> and stores the result in this vector.
     * 
     * @param p1
     * @param p2
     */
    final void mul( RtVector3 p1, RtVector3 p2 ) {
        this.x = p1.x * p2.x;
        this.y = p1.y * p2.y;
        this.z = p1.z * p2.z;
    }
    
    /**
     * Divides the vector by the vector <b>p1</b>.
     * 
     * @param p
     */
    final void div( RtVector3 p ) {
        this.x /= p.x;
        this.y /= p.y;
        this.z /= p.z;
    }

    /**
     * Divides vector <b>p1</b> by <b>p2</b> and stores the result in this vector.
     * 
     * @param p1
     * @param p2
     */
    final void div( RtVector3 p1, RtVector3 p2 ) {
        this.x = p1.x / p2.x;
        this.y = p1.y / p2.y;
        this.z = p1.z / p2.z;
    }

    /**
     * Scales (multiples) the vector by <b>factor</b>.
     * 
     * @param factor
     */
    final void scale( double factor ) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
    }

    /**
     * Scales vector <b>p</b> by <b>factor</b> and leaves the result in this vector.
     * 
     * @param factor
     * @param p
     */
    final void scale( double factor, RtVector3 p ) {
        this.x = p.x * factor;
        this.y = p.y * factor;
        this.z = p.z * factor;
    }

    /**
     * Returns the distance between this vector and the vector <b>p</b>.
     * 
     * @param p
     * @return
     */
    final double length( RtVector3 p ) {
        double dx = this.x - p.x;
        double dy = this.y - p.y;
        double dz = this.z - p.z;
        return Math.sqrt( (dx*dx) + (dy*dy) + (dz*dz) );
    }

    /**
     * Returns the distance between this vector and <b>{x, y, z}</b>.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    final double length( double x, double y, double z ) {
        x -= this.x;
        y -= this.y;
        z -= this.z;
        return Math.sqrt( (x*x) + (y*y) + (z*z) );
    }

    
    /**
     * Set this vector to <b>{x, y, z}</b>.
     * 
     * @param x
     * @param y
     * @param z
     */
    final void set( double x, double y, double z ) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Set this vector to the vector <b>p</b>.
     * 
     * @param p
     */
    final void set( RtVector3 p ) {
    	if (p != null) {
    		this.x = p.x;
    		this.y = p.y;
    		this.z = p.z;
    	}
    }

    
    /**
     * Clamp the values in this vector between <b>min</b> and <b>max</b>.
     * 
     * @param min
     * @param max
     */
    final void clamp( double min, double max ) {
    	// clamp x
        if (this.x < min) this.x = min;
        if (this.x > max) this.x = max;

        // clamp y
        if (this.y < min) this.y = min;
        if (this.y > max) this.y = max;

        // clamp z
        if (this.z < min) this.z = min;
        if (this.z > max) this.z = max;
    }        
        
    /**
     * Linearly interpolate this vector's values between vector <b>p1</b> and
     * <b>p2</b>, based on the avlue <b>alpha</b>. When <b>alpha == 0</b>, the
     * value is equal to <b>p1<b>p1</b>. <b>alpha</b> ranges between 0 and 1.
     * When <b>alpha == 1</b>, the value is equal to <b>p2</b>.
     * 
     * @param p1
     * @param p2
     * @param alpha
     */
    final void interpolate( RtVector3 p1, RtVector3 p2, double alpha ) {
        this.x = (1-alpha)*p1.x + alpha*p2.x;
        this.y = (1-alpha)*p1.y + alpha*p2.y;
        this.z = (1-alpha)*p1.z + alpha*p2.z;
    }
    
    /**
     * Normalize the vector (make the length equal to 1).
     *
     */
    final void normalize() {
        // get the length
        double len = Math.sqrt( (this.x*this.x) + (this.y*this.y) + (this.z*this.z) );
        
        if ( len > 0.0 ) {
            // divide vector by length
            this.x /= len;
            this.y /= len;
            this.z /= len;
        }
    }
    
    /**
     * Return the dot product of this vector and vector <b>p</b> (a cosine relationship
     * with the angle between the vectors).
     * 
     * @param p
     * @return
     */
    final double dot( RtVector3 p ) {
        return (this.x * p.x) + (this.y * p.y) + (this.z * p.z);
    }
    
    /**
     * Return the dot product between this vector and <b>{a, b, c}</b>.
     * 
     * @param a
     * @param b
     * @param c
     * @return
     */
    final double dot( double a, double b, double c ) {
        return (this.x * a) + (this.y * b) + (this.z * c);
    }
    
    /**
     * Return this point rotates about <b>{rotX, rotY, rotZ}</b>.
     * 
     * @param angleX
     * @param angleY
     * @param angleZ
     * @return
     */
    final RtVector3 rotate( double angleX, double angleY, double angleZ ) {
        
        // x, y, z are the coordinates of the point to be rotated,
        // ax, ay, az are the angles the object is to be rotated by around each axis,
        // rx, ry, rz are the rotated coordinates

         // rotation around the x axis
        double ry1 = this.y*Math.cos(angleX) - this.z*Math.sin(angleX);
        double rz1 = this.z*Math.cos(angleX) + this.y*Math.sin(angleX);

        // rotate around the y axis
        double rz2 = rz1*Math.cos(angleY) - this.x*Math.sin(angleY);
        double rx2 = this.x*Math.cos(angleY) + rz1*Math.sin(angleY);

        // rotate around the z axis
        double rx3 = rx2*Math.cos(angleZ) - ry1*Math.sin(angleZ);
        double ry3 = ry1*Math.cos(angleZ) + rx2*Math.sin(angleZ);
        
        return new RtVector3( rx3, ry3, rz2 );
    }

    /**
     * Return a vector that points somewhere in the unit hemisphere of the source vector
     * @return
     */
    final RtVector3 sampleRandomly() {
        
		RtVector3 sample;

		// two random angles
		double theta = Math.random() * Math.PI * 2;
		double phi = Math.random() * Math.PI;

		// calculate direction vector    		
		double x = Math.cos(phi) * Math.sin(theta);
		double y = Math.sin(phi) * Math.sin(theta);
		double z = Math.cos(theta);
	
		// create a new ray
		sample = new RtVector3( x, y, z );

		// add the vector, and normalize
		sample.add( this );
		sample.normalize();
    
		// dot product negative?
		if (this.dot( sample ) <= 0.0) {
			// reverse the direction of the vector
			sample.negate();			
		}
			
        return sample;
    }

    

    /**
     * Assuming that this vector is a surface normal, return a new vector that
     * is pointing into the hemisphere around this normal. 
     * 
     * @param randomTheta
     * @param randomPhi
     * @return
     */
    final RtVector3 sampleHemisphere_( double randomTheta, double randomPhi ) { 
    	
		RtVector3 u, v, n;

		// create unit vector perpendicular to normal
		// ensure normal is not colinear with (1, 0, 0)
		if (Math.abs(this.x) < 0.5) {
			u = new RtVector3( 1.0, 0.0, 0.0 );
		} else {
			u = this.cross( 0.0, 1.0, 0.0);
		}
		u.normalize();
		
		// v should already be unit length
		v = u.cross( this );

		// create sample by rejection sampling
		double a, b, c;
		while (true) {
			// create random values between -1 and 1		
			a = (Math.random() * 2.0) - 1.0;
			b = (Math.random() * 2.0) - 1.0;
			if (a*a + b*b < 1) break;
		}
		c = Math.sqrt(1 - (a*a + b*b));
		
		// return the vector a*u + b*v + c*n
		return new RtVector3( a*u.x + b*v.x + c*this.x, a*u.y + b*v.y + c*this.y, a*u.z + b*v.z + c*this.z);
		
    }
    
    /**
     * Return a jittered normal that points somewhere in the
     * hemisphere around this surface normal.
     * 
     * @param randomTheta
     * @param randomPhi
     * @return
     */
    final RtVector3 sampleHemisphere( double theta, double phi ) {
        
		RtVector3 sample;

		// calculate direction vector    		
		double x = Math.cos(phi) * Math.sin(theta);
		double y = Math.sin(phi) * Math.sin(theta);
		double z = Math.cos(theta);
	
		// create a new ray
		sample = new RtVector3( x, y, z );

		// add the average normal, and normalize
		sample.add( this );
		sample.normalize();
    
		// dot product negative?
		if (this.dot( sample ) < 0.0) {
			// reverse the direction of the vector
			sample.negate();			
		}
			
        return sample;
    }

    
    /**
     * Return a jittered normal that points somewhere in the
     * hemisphere around this surface normal, using a cosine
     * distribution.
     * 
     * @param randomTheta
     * @param randomPhi
     * @return
     */
    final RtVector3 sampleUsingCosine() {
        
    	// FIXME: this is likely to be all wrong...
    	
        // convert normal from cartesian coordinates to spherical coordinates
        double thetaNormal = Math.atan2(this.y, this.x);
        double phiNormal = Math.acos( this.z );
                        
        // pick angle based on the cosine distribution 
        double theta = Math.PI * Math.acos( Math.sqrt( Math.random() ) );
        double phi = 2 * Math.PI * Math.random();
        
        // add in the normal
        theta += thetaNormal;
        phi += phiNormal;
        
        // calculate direction vector    
		double z = Math.cos(phi);
		double x = Math.sin(phi) * Math.cos(theta);
		double y = Math.sin(phi) * Math.sin(theta);
		
        // create a new ray
        RtVector3 sample = new RtVector3( x, y, z );
        
        // dot product negative?
        if (this.dot( sample ) < 0) {
            // reverse the direction of the vector
            sample.negate();
        }
                        
        return sample;
    }
    
    /**
     * Return a vector based on the vector that will return the most light. This
     * is assumed to be the "perfect" reflection of the direction vector. The
     * reflection is calculated, and then that value is jittered randomly.
     * 
     * @param direction
     * @param normal
     * @return
     */
    final RtVector3 importanceSample( RtVector3 normal ) {

    	// NOT TESTED YET...
    	
		// calculate the reflection angle: 2*(N.D)* N - D
		RtVector3 D = new RtVector3(this);		
		D.negate();
		
		RtVector3 reflection = new RtVector3(normal);		
		reflection.scale(2 * normal.dot(D));
		reflection.sub(D);
				
		// generate vectors until one satisfies the criteria
		while (true) {
			// generate a vector using cosine sampling
			RtVector3 jittered = this.sampleUsingCosine();
			
			// test the direction against the normal
	        if (this.dot( jittered ) >= 0) {
	            // exit loop
	        	return jittered;
	        }
		}		
    }
}
