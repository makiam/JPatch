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
 * This is used to quickly determine if an enclosed piece of geometry is visible
 * to the raytracer. It's axis aligned, so the tests are fairly cheap.
 */

package inyo;
import javax.vecmath.*;


class RtBoundingBox {

    boolean isEmpty = true;
    double minX, minY, minZ, maxX, maxY, maxZ;
    
    public RtBoundingBox() {    
        // does nothing
    }

    final void setBounds( double minX, double minY, double minZ, double maxX, double maxY, double maxZ ) {
    	this.minX = minX;
    	this.minY = minY;
    	this.minZ = minZ;
    	this.maxX = maxX;
    	this.maxY = maxY;
    	this.maxZ = maxZ;
    }
    
    /**
     * Increase the size of the bounding box to enclose the point
     * @param x		<b>x</b> position of the point
     * @param y		<b>y</b> position of the point
     * @param z		<b>z</b> position of the point
     */
    final void add( double x, double y, double z ) {
        if (this.isEmpty) {
            // clear flag
            this.isEmpty = false;
            
            // add first point
            this.minX = x;
            this.minY = y;
            this.minZ = z;

            this.maxX = x;
            this.maxY = y;
            this.maxZ = z;
            
        } else {
            // test to see if it increases the bounds size
            if (x < this.minX) this.minX = x;
            if (y < this.minY) this.minY = y;
            if (z < this.minZ) this.minZ = z;
                
            if (x > this.maxX) this.maxX = x;
            if (y > this.maxY) this.maxY = y;
            if (z > this.maxZ) this.maxZ = z;
        }
    }
    
    /**
     * Increase the bounds of the bounding box to enclose the vector <b>p</b>
     * @param p	Vector containing <b>x, y, z</b> values of the point.
     */
    final void add( Point3d p ) {
        this.add( p.x, p.y, p.z );
    }
    
    
    /**
     * Increase the bounds of the bounding box to enclose the given bounding box.
     * @param bb	Bounding box to enclose
     */
    final void add( RtBoundingBox bb ) {
        this.add( bb.minX, bb.minY, bb.minZ );
        this.add( bb.maxX, bb.maxY, bb.maxZ );
    }

    /**
     * Return the center of the bounding box.
     * @return	A vector containing the center of the bounding box. 
     */
    
    final Point3d getCenter() {
    	double midX = this.minX + ((this.maxX - this.minX) / 2.0 );
    	double midY = this.minY + ((this.maxY - this.minY) / 2.0 );
    	double midZ = this.minZ + ((this.maxZ - this.minZ) / 2.0 );
    	return new Point3d( midX, midY, midZ );
    }

    /**
     * Returns true if the bounding box is within the minimum distance
     * 
     * @param d
     * @return
     */
    final boolean withinDistance( Point3d p, double limit ) {
    	
    	// calculate the center of the bounding box
    	double midX = this.minX + ((this.maxX - this.minX) / 2.0 );
    	double midY = this.minY + ((this.maxY - this.minY) / 2.0 );
    	double midZ = this.minZ + ((this.maxZ - this.minZ) / 2.0 );

    	// calculate the distance from the eyepoint to the center
        double dx = midX - p.x;
        double dy = midY - p.y;
        double dz = midZ - p.z;
        double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        // close enough?
        if (distance < limit) {
        	return true;
        }
    	
        // calculate distance across the bounding box
        dx = minX - maxX;
        dy = minY - maxY;
        dz = minZ - maxZ;
        double distance2 = Math.sqrt(dx*dx + dy*dy + dz*dz)/2.0;
        
        // fail if distance is greater than limit 
        return (distance - distance2 > limit);
    }

    
    
    /**
     * Returns true if <b>child</b> intersects the bounding box.
     * @param child	The bounding box to test for intersection
     * @return		<b>true</b> if <b>child</b> intersects this bounding box
     */
    final boolean intersects( RtBoundingBox child ) {
        if ( (child.minX > this.maxX) || (child.maxX < this.minX) ||
             (child.minY > this.maxY) || (child.maxY < this.minY) ||
             (child.minZ > this.maxZ) || (child.maxZ < this.minZ) ) {
                return false;
             } else {
             return true;
         }
     }
     
    /**
     * Returns true if the ray described by <b>pathNode</b> intersects this
     * bounding box.
     * 
     * @param pathNode	Describes a ray
     * @return			True if <b>pathNode</b> intesects this bounding box
     */
    final boolean hitsBox( RtPathNode pathNode ) {
        
        // initial endpoints
        double in = -Double.MAX_VALUE;
        double out = Double.MAX_VALUE;
        
        // new t values
        double newIn, newOut;
        
        // X slabs (perpendicular to X axis)
        if (pathNode.direction.x == 0) {
            // ray is parallel to slab planes
            if ((pathNode.origin.x < this.minX) || (pathNode.origin.x > this.maxX)) {
                return false;
            }
        } else {
            // tval entering min plane
            newIn = (this.minX-pathNode.origin.x)/pathNode.direction.x;
            newOut = (this.maxX-pathNode.origin.x)/pathNode.direction.x;
            
            if (newOut > newIn) {
                if (newIn > in) in = newIn;
                if (newOut < out) out = newOut;
            } else {
                if (newOut > in) in = newOut;
                if (newIn < out) out = newIn;
            }
            
            if (in > out ) return false;
        }
            
        // Y slabs (perpendicular to Y axis)
        if (pathNode.direction.y == 0) {
            // ray is parallel to slab planes
            if ((pathNode.origin.y < this.minY) || (pathNode.origin.y > this.maxY)) {
                return false;
            }
        } else {
            // tval entering min plane
            newIn = (this.minY-pathNode.origin.y)/pathNode.direction.y;
            newOut = (this.maxY-pathNode.origin.y)/pathNode.direction.y;
            
            if (newOut > newIn) {
                if (newIn > in) in = newIn;
                if (newOut < out) out = newOut;
            } else {
                if (newOut > in) in = newOut;
                if (newIn < out) out = newIn;
            }
            
            if (in > out) return false;
        }

        // Z slabs (perpendicular to Z axis)
        if (pathNode.direction.z == 0) {
            // ray is parallel to slab planes
            if ((pathNode.origin.z < this.minZ) || (pathNode.origin.z > this.maxZ)) {
                return false;
            }
        } else {
            // tval entering min plane
            newIn = (this.minZ-pathNode.origin.z)/pathNode.direction.z;
            newOut = (this.maxZ-pathNode.origin.z)/pathNode.direction.z;
            
            if (newOut > newIn) {
                if (newIn > in) in = newIn;
                if (newOut < out) out = newOut;
            } else {
                if (newOut > in) in = newOut;
                if (newIn < out) out = newIn;
            }
            
            if (in > out) return false;
        }
        
        // check if intersections are at or beyond the start of the ray
        if (in >= 0 || out >= 0 ) {
            return true;
        } else {
            return false;
        }
    }
                    
}