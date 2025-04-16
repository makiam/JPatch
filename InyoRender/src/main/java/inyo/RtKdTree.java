// Copyright (c) 2005 David Cuny
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
 * A KD recursively divides space up into two parts. This particular implementation
 * uses the surface area heuristic (SAH), which attempts to create as much "empty space"
 * as possible. This is done by estimating the cost of a particular configuration, and
 * selecting the lowest cost. Cost is defined as the cost of splitting at a particular point,
 * which is just the cost of both sides:
 * 
 *     totalCost = leftSideCost + rightSideCost
 * 
 * The cost of each side is an estimate based on the volume of the space multiplied by
 * the number of objects in each space:
 * 
 *     cost = volume * number of objects
 * 
 * Recursion stops when the maximum depth or minimum number of objects in an region has
 * been reached.
 * 
 * If geometry overlaps regions, a copy of that geometry is placed into each region.
 */

package inyo;

import java.util.Enumeration;
import java.util.Vector;

class RtKdTree extends RtBoundingBox {

	public static final int X_AXIS 		= 0;	// split on x axis
	public static final int Y_AXIS		= 1;	// split on y axis
	public static final int Z_AXIS  	= 2;	// split on z axis
	
    // FIXME: these should be world attributes
    int maxDepth = 30;
    int minItems = 16;
	
	RtKdTree left = null;		// left side
	RtKdTree right = null;		// right side
	RtBoundingBox child[] = null;		// objects held in kd tree leaf
	
	/**
	 * Place all triangles between <b>firstTriangle</b> and <b>lastTriangle</b> into
	 * the octree.
	 * 
	 * @param world
	 * @param firstTriangle
	 * @param lastTriangle
	 */
	public RtKdTree( RtWorld world, int firstTriangle, int lastTriangle ) {
                
		// gather all contained children
		Vector childList = new Vector();
		  
        // place all triangles of latest model into the list		
        for ( int i = firstTriangle; i <= lastTriangle; i++ ) {
        	// FIXME!!! broken
            // get a triangle
            //RtTriangle triangle = (RtTriangle)world.triangleList.get(i);          
            
            // add to the octtree's bounding box            
			//this.add( triangle );
          
			// add to child list
			//childList.addElement( (Object)triangle );
        }
        
        // fill it in
        this.split( 0, childList );
    }

	public RtKdTree( double minX, double minY, double minZ, double maxX, double maxY, double maxZ ) {
		this.setBounds( minX, minY, minZ, maxX, maxY, maxZ );
	}

	
	/**
	 * Return the number of child triangles that lie inside a boundary
	 * 
	 * @param axis Axis the split is on
	 * @param min Minimum value of split
	 * @param max Maximum value of split
	 * @param childList Children to compare
	 * @return Number of children in boundaries
	 */
	final boolean inRange( int axis, double min, double max, RtBoundingBox box ) {
		
		switch (axis) {
		case RtKdTree.X_AXIS:
			return (box.maxX >= min && box.minX <= max );
		
		case RtKdTree.Y_AXIS:
			return (box.maxY >= min && box.minY <= max );			

		default:		
			return (box.maxZ >= min && box.minZ <= max );			
		}
	}
	
	/**
	 * Similar to <b>countChildrenInside</b>, but returns a list of all children in <b>childList</b>
	 * that fall within the boundaries.
	 * @param axis Axis box lies on 
	 * @param min Minimum axis value
	 * @param max Maximum axis value
	 * @param childList List of triangles
	 * @return Objects falling within boundaries
	 */
	Vector getChildrenInside( int axis, double min, double max, Vector childList ) {
		
		// holds resulting children
		Vector newList = new Vector();
		
		// iterate through children
		for (Enumeration e = childList.elements() ; e.hasMoreElements() ;) {
			// get the next triangle			
            RtBoundingBox box = (RtBoundingBox)e.nextElement();
            
            // in bounds?
            if (inRange( axis, min, max, box )) { 
    			// add to new list
    			newList.addElement( (Object)box );
            }
		}

		return newList;
	}

	
	void split( int depth, Vector childList ) {
						
		if (depth > maxDepth || childList.size() <= minItems ) {
			
			// create a permanant array for the elements
			this.child = new RtBoundingBox[childList.size()];
            
            // copy the elements into the array
            int i = 0;
            for (Enumeration e = childList.elements() ; e.hasMoreElements() ;) {
                // get the next element
                child[i++] = (RtBoundingBox)e.nextElement();
			}
			return;
		}
		
		
		// set initial cost impossibly high		
		double bestEstimate = Double.MAX_VALUE;		
    	int bestAxis = RtKdTree.X_AXIS; // keep compiler happy
		double splitPoint = this.minX - 100;
    	double bestSplitPoint = splitPoint;
    	int bestLeftCount = 0;
    	int bestRightCount = 0;

		double min = minX;
		double max = maxX;
		double leftSize = 0;
		double rightSize = 0;
		
		// calculate sizes
		double xLength = this.maxX - this.minX;
		double yLength = this.maxY - this.minY;
		double zLength = this.maxZ - this.minZ;
		double yzSize = zLength * yLength;
		double xzSize = xLength * zLength;
		double xySize = xLength * yLength;
		double totalSize = xySize * zLength;
		
		// check each axis
		for (int axis = RtKdTree.X_AXIS; axis <= RtKdTree.Z_AXIS; axis++ ) {
			
			// get range of axis
            switch (axis) {
            case RtKdTree.X_AXIS:
            	min = this.minX;
            	max = this.maxX;
            	break;
            case RtKdTree.Y_AXIS:
            	min = this.minY;
        		max = this.maxY;
        		break;
            case RtKdTree.Z_AXIS:
            	min = this.minZ;
        		max = this.maxZ;
        		break;
            }
                        
            // iterate through each triangle
			for (Enumeration e = childList.elements() ; e.hasMoreElements() ;) {
				// get the next object	
	            RtBoundingBox box = (RtBoundingBox)e.nextElement();        

	           	// get the split point
	            switch (axis) {
	            case RtKdTree.X_AXIS:
	            	splitPoint = box.maxX;
	            	if (splitPoint == min || splitPoint == max) {
	            		continue;
	            	}
	            	leftSize = yzSize * (splitPoint - min);
	            	break;
	            	
	            case RtKdTree.Y_AXIS:
	            	splitPoint = box.maxY;
	            	if (splitPoint == min || splitPoint == max) {
	            		continue;
	            	}
	            	leftSize = xzSize * (splitPoint - min);
	            	break;
	            	
	            case RtKdTree.Z_AXIS:
	            	splitPoint = box.maxX;
	            	if (splitPoint == min || splitPoint == max) {
	            		continue;
	            	}
	            	leftSize = xySize * (splitPoint - min);
	            	break;
	            }		
            
	            // skip this one?
	            if (bestAxis == axis && bestSplitPoint == splitPoint) {
	            	continue;
	            }	            
	            
	            // count children. A child can be on both sides
	            int leftCount = 0;
	            int rightCount = 0;
				for (Enumeration e2 = childList.elements() ; e.hasMoreElements() ;) {
					// get the next child in list	
		            RtBoundingBox child = (RtBoundingBox)e.nextElement();
		            
		            // on left side?
		            if (inRange( axis, min, splitPoint, child )) {
		            	leftCount++;
		            }

		            // on right side?
		            if (inRange( axis, splitPoint, max, child )) {
		            	rightCount++;
		            }
				}

	            // estimate cost
	            rightSize = totalSize - leftSize;
	            double estimate = ((double)leftCount * leftSize) + ((double)rightCount * rightSize);

	            // cheapest estimate so far?
	            if (estimate < bestEstimate) {
	            	bestEstimate = estimate;
	            	bestAxis = axis;
	            	bestSplitPoint = splitPoint;
	            	bestLeftCount = leftCount;
	            	bestRightCount = rightCount;
	            }
            }
		}

		// figure out the range
		switch(bestAxis) {
		case (RtKdTree.X_AXIS):
			// get range
			min = this.minX;
			max = this.maxX;

			// build left and right sides
			if (bestLeftCount > 0) {
				this.left = new RtKdTree( this.minX, this.minY, this.minZ, bestSplitPoint, this.maxY, this.maxZ );
			}
			if (bestRightCount > 0) {
				this.right = new RtKdTree( bestSplitPoint, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ );
			}
			break;
		
		case (RtKdTree.Y_AXIS):
			// get range
			min = this.minY;
			max = this.maxY;

			// build left and right sides
			if (bestLeftCount > 0) {
				this.left = new RtKdTree( this.minX, this.minY, this.minZ, this.maxX, bestSplitPoint, this.maxZ );
			}
			
			if (bestRightCount > 0) {
				this.right = new RtKdTree( this.minX, bestSplitPoint, this.minZ, this.maxX, this.maxY, this.maxZ );
			}
			break;

		case (RtKdTree.Z_AXIS):
			// get range
			min = this.minZ;
			max = this.maxZ;
			
			// build left and right sides
			if (bestLeftCount > 0) {				
				this.left = new RtKdTree( this.minX, this.minY, this.minZ, this.maxX, this.maxY, bestSplitPoint );
			}
			if (bestRightCount > 0) {
				this.right = new RtKdTree( this.minX, this.minY, bestSplitPoint, this.maxX, this.maxY, this.maxZ );
			}
			break;
		}

		// populate the sides
		if (this.left != null ) {
			this.left.split( depth+1, getChildrenInside( bestAxis, min, bestSplitPoint, childList ) );
		}
		
		if (this.right != null) {
			this.right.split( depth+1, getChildrenInside( bestAxis, bestSplitPoint, max, childList ) );	
		}
		
	}
		
		
	/**
	 * Returns true if ray described by <b>pathNode</b> hits the octree.
	 * 
	 * @param world
	 * @param pathNode
	 */
	final void hitTest( RtWorld world, RtPathNode pathNode ) {

		// test to see if ray intersects
		if (!(this.hitsBox( pathNode ))) {
			return;
		}

		// test children?
		if (child != null) {
			// test children for hits
			for ( int i = 0; i < child.length; i++ ) {	// SL: fixed
				// get child
				RtTriangle triangle = (RtTriangle)child[i];
				
				// hit test it
				// FIXME!!!!!
				//triangle.hitTest( world, pathNode );

				// early out for shadow test?
				if (pathNode.hit && pathNode.stopAtFirstHit) {
					return;
				}
			}
			return;
		}
			

		// left node?
		if (this.left != null) {
			// hit test
			this.left.hitTest( world, pathNode );

			// early out for shadow test?
			if (pathNode.hit && pathNode.stopAtFirstHit) {
				return;
			}
		}

		// right node?
		if (this.right != null) {
			this.right.hitTest( world, pathNode );
		}
	}

}
