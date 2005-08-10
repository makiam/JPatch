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
 * The depth buffer. This isn't terribly accurate, which leads to lots of
 * errors if the depth buffer is used to accelerate raytracing.
 * 
 * In addition to holding the corresponding depth each point in the canvas, 
 * the material is tracked. This makes it possible to link to the raytracer's
 * rendering routine.
 */

package inyo;
import javax.vecmath.*;

class RtZBuffer {

    // FIXME: can spans be removed after building buffer?
	
	boolean edgeFlag[];
	double edgeStartX[], edgeStartZ[], edgeEndX[], edgeEndZ[];
	double depthBuffer[][];    
	RtTriangle triangleBuffer[][];
	
	int top;
	int bottom;
	int left;
	int right;
	
	int high;
	int wide;
	
	double dTop;
	double dBottom;
	double dLeft;
	double dRight;
	
	double fov;
    
    double dClipping = 1.0;
	
    /**
     * Create the depth buffer.
     * 
     * @param wide
     * @param high
     */
    public RtZBuffer(int x1, int y1, int x2, int y2, int fov ) {
		// store integer values
    	this.left = x1;
    	this.right = x2;
    	this.top = y1;
    	this.bottom = y2;
    	
    	// store as floats
    	this.dTop = (double)top;
    	this.dBottom = (double)bottom;
    	this.dLeft = (double)left;
    	this.dRight = (double)right;
    	this.fov = (double)fov;
    	
    	// calculate size
    	this.high = (int)Math.abs(top - bottom) + 1;
    	this.wide = (int)Math.abs(right - left) + 1;
    	
        // create the span buffers
        edgeFlag = new boolean[this.high];
        edgeStartX = new double[this.high];
        edgeStartZ = new double[this.high];
        edgeEndX = new double[this.high];
        edgeEndZ = new double[this.high];
        
        // set up the buffers
		depthBuffer = new double[this.wide][this.high];		                
		triangleBuffer = new RtTriangle[this.wide][this.high];
                
        // erase the buffer
        for (int y = 0; y < this.high; y++ ) {
            for ( int x = 0; x < this.wide; x++ ) {
                depthBuffer[x][y] = -1;
                triangleBuffer[x][y] = null;
            }
        }
	}
    
    
    /**
     * Free the spans to clean up memory. This probably doesn't really help
     * much, since the geometry is already loaded into memory and all the 
     * memory intensive operations have already taken place
     *
     */
    void freeSpans() {
        edgeFlag = null;
        edgeStartX = null;
        edgeStartZ = null;
        edgeEndX = null;
        edgeEndZ = null;
    }
    
    /**
     * Determine 3D point <b>p</b> that corresponds to point <b>{x, y}</b> in the depth buffer.
     * 
     * @param world
     * @param x
     * @param y
     * @param p
     */
	final public void setHitPoint( RtWorld world, int x, int y, Point3d p ) {
 
		// convert the point to coordinates in the array
		int a = -this.left + x;
		int b  = this.top - y;
		
		// convert x, y and z back to 3d space                
		double z1 = ((double)1.0)/depthBuffer[a][b];
		double x1 = ((double)(x) / this.fov) * z1;
		double y1 = ((double)(y) / this.fov) * z1;
		
		p.set( x1, y1, z1 );
	
	}
        
    
    /**
     * Return the triangle at <b>{x, y}</b> or <b>null</b> for sky. 
     * 
     * @param x
     * @param y
     * @return
     */
    final public RtTriangle getTriangle( int x, int y ) {
		// return the id of the triangle at the x,y point in the zBuffer
		
		// convert
		int a = -this.left + x;
		int b  = this.top - y;

        return triangleBuffer[a][b];
	}


    /**
     * Return the material at <b>{x, y}</b>, or <b>null</b> for sky.
     * 
     * @param x
     * @param y
     * @return
     */
	final public RtMaterial getMaterial( int x, int y ) {
		// convert
		int a = -this.left + x;
		int b  = this.top - y;

		// get the triangle
		RtTriangle t = triangleBuffer[a][b];
		if (t == null) {
			return null;
		}
		// get the material of the triangle
		return t.material;
	}

	
	/**
	 * Return the depth of the triangle at <b>{x, y}</b>.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	final public double getDepth( int x, int y ) {
		// convert
		int a = -this.left + x;
		int b  = this.top - y;

		// return the depth of the triangle at the x,y point in the zBuffer
        return ((double)1.0)/depthBuffer[a][b];
	}

	/**
	 * Calculates the point where p1-p2 intersect the Z plane at Z=dClipping, and returns 
	 * the value in p. Also calculates the vertex normal at p1-p2, and returns the value in n.
	 * Used to clip triangles that intersect the Z plane.
 
	 * @param p1
	 * @param p2
	 * @param p
	 */
    final public void calculateClip( Point3d p1, Point3d p2, Point3d p ) {
		
		// get the delta z
		double dz = p2.z - p1.z;

        // get the distance from p1 to the clip plane
		double scale = dClipping - p1.z;

		
		// get the slopes of x and y
		double mx = (p2.x - p1.x) / dz;
		double my = (p2.y - p1.y) / dz;
        double mz = (p2.z - p1.z) / dz;
						
		// calculate the point where z intercepts the plane
		p.x = p1.x + (scale * mx);
		p.y = p1.y + (scale * my);
		p.z = p1.z + (scale * mz);
		
	}

    /**
     * Clip the triangle against the Z plane before rendering. Discard portions of the triangle
     * that lie on the wrong side of the Z plane. In some cases, this may require construction
     * new triangles to replace the old triangle. For example, if the triangle (V1, V2, V3) has
     * the vertex V1 outside of the viewing plane, the points V12 and V13 (where the Z plane
     * intercepts V1 and V2, and V1 and V3), and two new triangles created: (V13, V12, V2)
     * and (V13, V2, V3), These are then passed onto renderTriangle() to render them into
     * the zBuffer.
     * 
     * @param world
     * @param triangle
     */
	final public void renderClippedTriangle( RtWorld world, RtTriangle triangle ) {
				
		// get the vertices
		Point3d p1 = triangle.v1.point;
		Point3d p2 = triangle.v2.point;
		Point3d p3 = triangle.v3.point;
	            
		if (p1.z >= dClipping && p2.z >= dClipping && p3.z >= dClipping ) {        
			// entirely in front of clip plane                  
			renderTriangle( world, triangle, p1, p2, p3 );
			
		} else if (p1.z < dClipping && p2.z < dClipping && p3.z < dClipping ) {
			// entirely behind clip plane, do nothing		        
			
		} else if (p1.z < dClipping && p2.z < dClipping) {
			// p1, p2 behind clip plane:            
			Point3d p13 = new Point3d();			
			Point3d p23 = new Point3d();			

			calculateClip( p1, p3, p13 );
			calculateClip( p2, p3, p23 );
			
			renderTriangle( world, triangle, p13, p23, p3 );
			
		} else if (p2.z < dClipping && p3.z < dClipping) {
			// p2, p3 behind clip plane                
			Point3d p12 = new Point3d();			
			Point3d p13 = new Point3d();			

			calculateClip( p1, p2, p12 );
			calculateClip( p1, p3, p13 );
			
			renderTriangle( world, triangle, p1, p12, p13 );

		} else if (p1.z < dClipping && p3.z < dClipping) {
			// p1, p3 behind clip plane            
			Point3d p12 = new Point3d();			
			Point3d p23 = new Point3d();			

			calculateClip( p1, p2, p12 );
			calculateClip( p2, p3, p23 );
			
			renderTriangle( world, triangle, p12, p2, p23 );
			
		} else if (p1.z < dClipping) {
			// p1 behind the clip plane  
            // System.out.println("p1 behind");            
			Point3d p12 = new Point3d();			
			Point3d p13 = new Point3d();			

			calculateClip( p1, p2, p12 );
			calculateClip( p1, p3, p13 );
			
			renderTriangle( world, triangle, p13, p12, p2 );
			renderTriangle( world, triangle, p13, p2, p3 );
			
		} else if (p2.z < dClipping) {
			// p2 behind the clip plane            
            // System.out.println("p2 behind");
			Point3d p12 = new Point3d();			
			Point3d p23 = new Point3d();			

			calculateClip( p1, p2, p12 );
			calculateClip( p2, p3, p23 );
			
			renderTriangle( world, triangle, p1, p12, p3 );
			renderTriangle( world, triangle, p12, p23, p3 );
			
		} else {
			// p3 behind the clip plane
            // System.out.println("p3 behind");
			Point3d p13 = new Point3d();
			Point3d p23 = new Point3d();			
            
			calculateClip( p1, p3, p13 );
			calculateClip( p2, p3, p23 );

			renderTriangle( world, triangle, p1, p13, p2 );
			renderTriangle( world, triangle, p13, p23, p2 );			
		}
		
	}
    
	
	/**
	 * Render each span of the triangle into the span buffer.
	 * 
	 * @param world
	 * @param triangle
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	final void renderTriangle( RtWorld world, RtTriangle triangle, Point3d p1, Point3d p2, Point3d p3 ) {		

		// convert coordinates into screen space
		double x1 = (p1.x / p1.z) * this.fov;
		double y1 = (p1.y / p1.z) * this.fov;
		double z1 = ((double)1.0 / p1.z);

		double x2 = (p2.x / p2.z) * this.fov;
		double y2 = (p2.y / p2.z) * this.fov;
		double z2 = ((double)1.0 / p2.z);

		double x3 = (p3.x / p3.z) * this.fov;
		double y3 = (p3.y / p3.z) * this.fov;
		double z3 = ((double)1.0 / p3.z);
		
		// is the triangle outside the window?
		if ((x1 < this.left && x2 < this.left && x3 < this.left)
		|| (x1 > this.right && x2 > this.right && x3 > this.right)
		|| (y1 > this.top && y2 > this.top && y3 > this.top)
		|| (y1 < this.bottom && y2 < this.bottom && y3 < this.bottom)) {
			// don't render it
			return;
		}
		
		// clear the segment buffer
		for ( int i = 0; i < this.high; i++ ) {
			edgeFlag[i] = false;
		}
		
		// render the segments into the segment buffer
		renderSegment( x1, y1, z1, x2, y2, z2 );
		renderSegment( x2, y2, z2, x3, y3, z3 );
		renderSegment( x1, y1, z1, x3, y3, z3 );
		
		// now render the spans in the segment
		for ( int i = 0; i < this.high; i++ ) {
			// y position
			int y = this.top - i;
			
			// is there a span in this part?
			if (edgeFlag[i]) {
				// render the span
				renderSpan( triangle, y, i );
			}
		}
		
	}

	/**
	 * Render a segement from <b>{x1, y1, z1}</b> to <b>{x2, y2, z2}</b>.
	 * 
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 */
	final void renderSegment( double x1, double y1, double z1, double x2, double y2, double z2 ) {
		
		// get deltas in each direction
//System.out.println( "segment:" + x1 + " " + y1 + " " + z1 + "::" + x2 + " " + y2 + " " + z2 );
		
        // render along y axis
        // need to swap?
        if (y1 > y2) {
            double tmp = x1;
            x1 = x2;
            x2 = tmp;
            
            tmp = y1;
            y1 = y2;
            y2 = tmp;

            tmp = z1;
            z1 = z2;
            z2 = tmp;
        }
        
        // calculate y slope
        double dy = (y2 - y1);
        
        // less than a pixel high, requires special handling
        if (dy <= (double)1.0) {
        // add the top and bottom into the edge buffers
        	addEdge(x1, y1, z1);
        	addEdge(x2, y2, z2);
        	return;
        }
        
        // calculate the remaining slopes
        double dx = (x2 - x1) / dy;
        double dz = (z2 - z1) / dy;
                
        // render from top to bottom
        for ( double y = y1; y <= y2; y++ ) {
        	// add the edge
        	addEdge( x1, y, z1 );
        	// move along slop
            x1 += dx;
            z1 += dz;            
        }
    }

	/**
	 * Adds a point of an edge into the edge buffer.
	 * 
	 * @param y
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param y2
	 */
	final void addEdge( double x, double y, double z){
        if (y >= this.bottom && y <= this.top) {
            // get index
        	// int i  = this.top - (int)Math.floor(y + (double)0.5);                
        	int i  = this.top - (int)Math.floor(y);
            
            // still in range?
            if (i > 0 && i < this.high) {
            	// first element in the span?
            	if (!this.edgeFlag[i]) {
            		// mark as non-empty
            		this.edgeFlag[i] = true;

            		// place as start and end
            		this.edgeStartX[i] = x;
            		this.edgeStartZ[i] = z;
            		this.edgeEndX[i] = x;
            		this.edgeEndZ[i] = z;
            
            	} else if ( x < this.edgeStartX[i] ) {
               		// add to left side
            		this.edgeStartX[i] = x;
            		this.edgeStartZ[i] = z;
                
            	} else {
            		// add to right side
            		this.edgeEndX[i] = x;
            		this.edgeEndZ[i] = z;
            	}
            }
        }
	}
	
	/**
	 * Render the span of a triangle
	 * 
	 * @param triangle
	 * @param y
	 * @param span
	 */
	final void renderSpan( RtTriangle triangle, int y, int span) {
		// note that z is actually 1/z, since it interpolates linearly
        
        double x1 = this.edgeStartX[span];
        double z1 = this.edgeStartZ[span];
        double x2 = this.edgeEndX[span];
        double z2 = this.edgeEndZ[span];
        
		// need to swap?
		if ( x1 > x2 ) {
			double tmp = x1;
			x1 = x2;
			x2 = tmp;
			
			tmp = z1;
			z1 = z2;
			z2 = tmp;
		}
		
		// delta z
		double dz = (z2-z1)/(x2-x1);
						
		// fill in the span
		for ( double x = x1; x <= x2; x++ ) {
            // is the point in bounds?
            if (x >= this.left && x <= this.right && y <= this.top && y >= this.bottom) {
                // convert to integer values                
        		// int xx = -this.left + (int)Math.floor(x+(double)0.5);
            	// int yy  = this.top - (int)Math.floor(y+(double)0.5);
            	int xx = -this.left + (int)Math.floor(x);
        		int yy  = this.top - (int)Math.floor(y);

                // is it still in range?
                if (xx > 0 && xx < this.wide && yy > 0 && yy < this.high) {
                
                	// closer than prior value?
                	if (z1 >= this.depthBuffer[xx][yy]) {
                		// replace prior value
                		this.depthBuffer[xx][yy] = z1;
                		this.triangleBuffer[xx][yy] = triangle;
                	}
                }
            }
            // increment z
			z1 += dz;
		}
	}
}