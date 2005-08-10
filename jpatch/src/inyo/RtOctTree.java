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
 * An octree recursively divides space up into 8 quadrants. At the point of
 * maximum depth (determined by a hard-coded value), the octree stops recursing, 
 * and holds triangles.
 * 
 * By dividing space into octants, it's much easier to discard geometry - rays that 
 * fail to hit an octree node also miss all the objects held inside that node.
 * 
 * If a triangle overlaps nodes, a copy of that triangle is placed into each node it
 * overlaps. 
 */

package inyo;

import java.util.ArrayList;

class RtOctTree extends RtBoundingBox {
	    
	boolean isLeaf = false;
	RtOctTree octant[] = null;
	RtTriangle child[] = null;
	
	/**
	 * Place all triangles between <b>firstTriangle</b> and <b>lastTriangle</b> into
	 * the octree.
	 * 
	 * @param world
	 * @param firstTriangle
	 * @param lastTriangle
	 */
	public RtOctTree( int maxDepth, int maxItems, ArrayList triangleList ) {
                		  
        // scan through children in the list and build the bounding box
		for (int i = 0; i < triangleList.size(); i++ ) {        
            // get the next triangle
            RtTriangle triangle = (RtTriangle)triangleList.get(i);            
            
            // add to the octtree's bounding box
			this.add( triangle );
        }
                  
        // fill it in
        this.buildOctants( maxDepth, maxItems, triangleList, 1, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ );
    }
    
	/**
	 * Creates a new node, and builds a list of all the triangles that fit into the node.
	 * It then calls <b>buildOctants</b> to recurse to the bottom of the nodes, and 
	 * add the triangles to the nodes.
	 * 
	 * @param world
	 * @param minX
	 * @param minY
	 * @param minZ
	 * @param maxX
	 * @param maxY
	 * @param maxZ
	 * @param depth
	 * @param childList
	 */
    public RtOctTree( int maxDepth, int maxItems, ArrayList triangleList, int depth, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        ArrayList newList = new ArrayList();
        
        // create bounding box
        this.add( minX, minY, minZ );
        this.add( maxX, maxY, maxZ );

        // scan through triangles in the list
        for (int i = 0; i < triangleList.size(); i++ ) {
            // get the next triangle
        	RtTriangle triangle = (RtTriangle)triangleList.get(i);            
            			
            // in bounding box?
            if (this.intersects( triangle )) {
                // add to the list
                newList.add( (Object)triangle );
            }
        }

        this.buildOctants( maxDepth, maxItems, newList, depth, minX, minY, minZ, maxX, maxY, maxZ );
    }
       
    /**
     * Builds octants into an octree's node, and adds children from <b>childList</b>
     * into the appropriate octants of the octree.
     * 
     * @param world
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param depth
     * @param childList
     */
    final void buildOctants( int maxDepth, int maxItems, ArrayList triangleList, int depth, double minX, double minY, double minZ, double maxX, double maxY, double maxZ ) {

        // no children?
        if (triangleList.size() == 0) {
			// make it a leaf, but leave children null so it can be deleted
			this.isLeaf = true;
		
        // past limits?
		} else if ((depth > maxDepth) || (triangleList.size() < maxItems)) {        
			// make this a leaf
			this.isLeaf = true;

			// create a permanant array for the elements
			child = new RtTriangle[triangleList.size()];
            
            // copy the elements into the array
            for (int i = 0; i < triangleList.size(); i++ ) {
                // get the next triangle and place in the array
            	child[i] = (RtTriangle)triangleList.get(i);            	
			}
			
		} else {
			// get midpoint of box
			double midX = (minX+maxX)/2;
			double midY = (minY+maxY)/2;
			double midZ = (minZ+maxZ)/2;

			// make this an octant
			isLeaf = false;
			this.octant = new RtOctTree[8];
			
			// create the octants            
			octant[0] = new RtOctTree( maxDepth, maxItems, triangleList, depth+1, minX, minY, minZ, midX, midY, midZ );            
			octant[1] = new RtOctTree( maxDepth, maxItems, triangleList, depth+1, midX, minY, minZ, maxX, midY, midZ );            
			octant[2] = new RtOctTree( maxDepth, maxItems, triangleList, depth+1, minX, minY, midZ, midX, midY, maxZ );            
			octant[3] = new RtOctTree( maxDepth, maxItems, triangleList, depth+1, midX, minY, midZ, maxX, midY, maxZ );
			octant[4] = new RtOctTree( maxDepth, maxItems, triangleList, depth+1, minX, midY, minZ, midX, maxY, midZ );
			octant[5] = new RtOctTree( maxDepth, maxItems, triangleList, depth+1, midX, midY, minZ, maxX, maxY, midZ );
			octant[6] = new RtOctTree( maxDepth, maxItems, triangleList, depth+1, minX, midY, midZ, midX, maxY, maxZ );
			octant[7] = new RtOctTree( maxDepth, maxItems, triangleList, depth+1, midX, midY, midZ, maxX, maxY, maxZ );

            // remove empty octants
            for ( int i = 0; i < 8; i++ ) {
                if ( (octant[i].isLeaf) && (octant[i].child == null)) {
                    // remove it
                    octant[i] = null;
                }
            }
		}
	}


	/**
	 * Returns true if ray described by <b>pathNode</b> hits the octree.
	 * 
	 * @param world
	 * @param pathNode
	 */
	final void hitTest( RtPathNode pathNode ) {

        // test to see if ray intersects
        if (!(this.hitsBox( pathNode ))) {
            return;
        }

        // hit a leaf?
        if (isLeaf) {
			// test children for hits
			for ( int i = 0; i < child.length; i++ ) {
				// get child
                RtTriangle t = child[i];
				// hit test it
				t.hitTest( pathNode );                
			}
        
        // hit an octant
        } else {
			// test all 8 octants
            for ( int i = 0; i < 8; i++ ) {
                // not null?
                if (octant[i] != null) {
                	// make sure it doesn't fail the minimum distance test
                	if (pathNode.maxDistance == 0.0 || octant[i].withinDistance( pathNode.origin, pathNode.maxDistance )) {                	
                        // test
                        octant[i].hitTest( pathNode );
                        
                        // early out for shadow test?
                        if (pathNode.hit && pathNode.stopAtFirstHit) {
                        		return;
                        }
                        
                	}                	
                }
            }
		}
	}
}
