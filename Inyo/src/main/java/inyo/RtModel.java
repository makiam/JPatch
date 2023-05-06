
package inyo;

import java.util.ArrayList;
import java.util.List;

/*
 * Created on Feb 24, 2005
 *
 */

/**
 * @author dcuny
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RtModel {
	
	RtOctTree octree;
	List<RtTriangle> triangleList = new ArrayList<>();
	
	public void addTriangle(RtTriangle triangle) {
		// add to the list
		this.triangleList.add(triangle);
		
		// add to material's samples?
		if (triangle.material.specular == 0) {
			triangle.material.samples.addTriangle(triangle);
		}
	}
	
	public void buildOctree( int maxDepth, int maxItems ) {
		this.octree = new RtOctTree( maxDepth, maxItems, this.triangleList );
	}
	
	public void hitTest( RtPathNode pathNode ) {
		this.octree.hitTest(pathNode);
		
		// FIXME: memory hack
		this.triangleList = null;
	}
	
}
