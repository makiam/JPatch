// Copyright (c) 2004 David Cuny
// Inyo.java
// Released under the MIT license
// Much of this class was derived from Sunflow (http://sunflow.sourceforge.net)
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal 
// in the Software without restriction, including without limitation the rights 
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
// copies of the Software, and to permit persons to whom the Software is furnished 
// to do so,subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
// IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

/**
 * @author David Cuny
 *
 * The irradiance cache is used to hold point samples of irradiance - light falling
 * on that given point. These are saved so that values can be estimated based on
 * prior samples, instead of resampling irradiance at a point, which is an expensive
 * operation.
 * 
 * The irradiance cache uses an octree to store data.
 * 
 * Much of this particular implementation was derived from the Sunflow renderer
 * (http://sunflow.sf.net).
 * 
 */

package inyo;
import javax.vecmath.*;

class RtIrradianceCache {
	
	double minDistance;
	double maxDistance;
	double tolerance;
	double invTolerance;
	int maxTreeDepth;
	RtIrrOctTree root;

	/**
	 * Builds the initial irradiance cache. The <b>world</b> is searched to 
	 * determine the maximum size of the octree.
	 * 
	 * @param world		Holds the scene geometry
	 */
	public RtIrradianceCache(RtWorld world) {

		// set the values
		this.tolerance = world.irradianceCacheTolerance;
		this.invTolerance = 1.0 / this.tolerance;
		this.minDistance = world.irradianceCacheMinDistance;
		// this.maxDistance = world.irradianceCacheMaxDistance;
		this.maxDistance = 100.0 * this.minDistance;

		// create a bounding box that holds the entire world
		RtBoundingBox boundingBox = new RtBoundingBox();
		for (int i = 1; i < world.modelList.size(); i++) {
			// add size of bounding box
			RtBoundingBox child = (RtBoundingBox)world.modelList.get(i); 
			boundingBox.add(child);
		}

		// calculate longest size
		double size = boundingBox.maxX - boundingBox.minX;
		size = Math.max(size, boundingBox.maxY - boundingBox.minY);
		size = Math.max(size, boundingBox.maxZ - boundingBox.minZ);

		// create a root octree that will hold the entire world
		root = new RtIrrOctTree(boundingBox.getCenter(), 1.0001 * size);

		// set maximum tree depth
		this.maxTreeDepth = world.irradianceMaxTreeDepth;
		
	}

	/**
	 * Insert an irradiance sample into the octree.
	 * 
	 * @param position	The point where the sample was taken
	 * @param normal	The normal (direction) of the surface
	 * @param r0		The average distance of objects from the point
	 * @param red		Contribution of red irradiance
	 * @param blue		Contribution of blue irradiance
	 * @param green		Contribution of green irradiance
	 */
	void add( Point3d position, Vector3d normal, double r0, Color3f irradiance) {
		
		//System.out.println("-------------ADD---------------");
		// start at the root node
		RtIrrOctTree node = root;

		// clamp the mean harmonic distance using the tolerance and
		// minimum/maximum spacing
		// r0 *= tolerance;
		// r0 = Math.min(r0, maxDistance);
		// r0 = Math.max(r0, minDistance);
		// r0 *= invTolerance;

		int octTreeDepth = 0;
		// inside the leaf?
		if (root.isInside(position)) {
			// stop when depth exceeded or size reaches tolerance 
			while (octTreeDepth < this.maxTreeDepth && node.size >= (4.0 * tolerance)) {
			// while (node.size >= (4.0 * r0 * tolerance)) {
			// while (node.size >= (4.0 * tolerance)) {
			// while (node.size >= (4.0 * tolerance) && octTreeDepth < 8) {
			// while (octTreeDepth < this.maxTreeDepth) {
				// octTreeDepth++;
				// figure out which child the point lies in; result is bit
				// encoded 0..7
				int k = 0;
				k |= (position.x > node.center.x) ? 1 : 0;
				k |= (position.y > node.center.y) ? 2 : 0;
				k |= (position.z > node.center.z) ? 4 : 0;

				// child not created yet?
				if (node.child[k] == null) {
					// create an empty node
					Point3d newChild = new Point3d(node.center);

					// use bits to determine how much to offset from the center
					newChild.x += ((k & 1) == 0) ? -node.quarterSize : node.quarterSize;
					newChild.y += ((k & 2) == 0) ? -node.quarterSize : node.quarterSize;
					newChild.z += ((k & 4) == 0) ? -node.quarterSize : node.quarterSize;

					// assign to the octree
					node.child[k] = new RtIrrOctTree(newChild, node.halfSize);
				}

				// select the child
				node = node.child[k];
			}
		}

		// create a new sample
		RtSample sample = new RtSample(position, normal, r0, irradiance );
		
		// add it to the linked list for that node
		sample.next = node.first;
		node.first = sample;
	}

	
	/**
	 * Uses existing samples in the cache to return an estimate of the irradiance
	 * at the given position. If no good estimate can be made, it returns <b>null</b>.
	 * 
	 * The distance of the sample from the current point, surface normals, and 
	 * number of close objects to the sample are all taken into account.
	 * 
	 * @param position	Where the sample should be estimated from
	 * @param normal	Surface normal at <b>position</b>
	 * @return			Returns a vector containing the irradiance, or <b>null</b> if no estimate can be made
	 */
	Color3f estimateIrradiance(Point3d position, Vector3d normal) {

		// this holds the irradiance, if created
		Color3f irradiance = new Color3f();

		// search the octree and get an estimate
		float weight = root.getEstimate(position, normal, irradiance);

		// return the irradiance (or null, if error is too high)
		if (weight == 0.0) {
			// zap the irradiance
			irradiance = null;
		} else {			
			// divide the irradiance by the weight
			irradiance.scale((float)(1.0 / weight));
		}

		return irradiance;
	}

	/**
	 * 
	 * @author David Cuny
	 *
	 * Octree containing irradiance estimates. 
	 */
	class RtIrrOctTree {
		RtIrrOctTree[] child; // up to 8 children

		RtSample first; // linked list of samples
		Point3d center; // center point of node

		double size; // length of one side
		double halfSize; // half the length of one side
		double quarterSize; // hrm... seems like a bit much
		
		/**
		 * Create a new node in the octree to hold a sample.

		 * @param center	Center of the node
		 * @param size		Size of the node
		 */

		RtIrrOctTree(Point3d center, double size) {

			// create an array to hold the children
			child = new RtIrrOctTree[8];

			// set the children to null
			for (int i = 0; i < 8; i++) {
				child[i] = null;
			}

			// create the center
			this.center = new Point3d(center);

			// save the side length
			this.size = size;
			this.halfSize = size / 2.0;
			this.quarterSize = size / 4.0;

			// the linked list is empty
			first = null;
		}

		/**
		 * Return <b>true</b> if the point is contained in the node.
		 * 
		 * @param point		Point under consideration
		 * @return			If point is in node, returns <b>true</b>
		 */
		final boolean isInside(Point3d point) {
			// true if distance from center is less than half the side length
			// for each axis
			return (Math.abs(point.x - this.center.x) < this.halfSize)
					&& (Math.abs(point.y - this.center.y) < this.halfSize)
					&& (Math.abs(point.z - this.center.z) < this.halfSize);
		}


		/**
		 * Returns an estimate of the irradiance at the given position and
		 * orientation by searching through the node and all its children.
		 * 
		 * @param targetPosition	Point on surface
		 * @param targetNormal		Orientation of surface
		 * @param estimated			Value estimated so far
		 * @return
		 */
		final float getEstimate(Point3d targetPosition, Vector3d targetNormal, Color3f estimated) {

			// sum of all weights
			float totalWeight = (float)0.0;
			
			// walk through the linked list of all the samples in this octree			
			for (RtSample thisSample = first; thisSample != null; thisSample = thisSample.next) {
				// calculate the weight for this sample
				float thisWeight = (float)Math.min(1e10, thisSample.weight(targetPosition, targetNormal));

				// within tolerance?
				if (thisWeight > invTolerance) {
					// scale the amount and accumulate
					thisSample.irradiance.scale(thisWeight); 
					estimated.add(thisSample.irradiance);
					
					// add to the total weight  
					totalWeight += thisWeight;
				}
			}
			// iterate through each child
			for (int i = 0; i < 8; i++) {
				// if child is not null and point falls in child
				if ((this.child[i] != null)) {
					// is the point inside the child?
					if ((Math.abs(this.child[i].center.x - targetPosition.x) <= this.halfSize)
					&& (Math.abs(this.child[i].center.y - targetPosition.y) <= this.halfSize)
					&& (Math.abs(this.child[i].center.z - targetPosition.z) <= this.halfSize)) {
						// accumulate the weight for all samples in that child
						totalWeight += child[i].getEstimate(targetPosition,	targetNormal, estimated);
					}
				}
			}

			// return the weight
			return totalWeight;
		}
	}

	
	/**
	 * 
	 * @author David Cuny
	 * 
	 * A single irradiance sample.
	 */
	class RtSample {
		Point3d position; // position of the sample
		Vector3d normal; // orientation of the sample
		Color3f irradiance; // irradiance at the sample
		double R0; // harmonic mean distance of samples
		RtSample next;

		/**
		 * Creates an irradiance sample to store values into. The sample itself will
		 * be stored in the octree of the irradiance cache.
		 * 
		 * @param position	Position of the sample
		 * @param normal	Orientation of the surface
		 * @param R0		Average distance of objects from the sample
		 * @param red		Red contribution
		 * @param blue		Blue contribution
		 * @param green		Green contribution
		 */
		public RtSample(Point3d position, Vector3d normal, double R0, Color3f rgb )
		{
			// clip to limits
			// R0 = Math.min(R0, maxDistance);
			// R0 = Math.max(R0, minDistance);

			this.position = new Point3d(position);
			this.normal = new Vector3d(normal);
			this.R0 = R0;
			this.irradiance = new Color3f(rgb);
			this.next = null;
		}


		/**
		 * Returns the weight (estimated error amount) of this sample relative to 
		 * the target, or zero if the error estimate is too high. This is used to 
		 * determine how much weight to give the sample relative to other samples.
		 * 
		 * @param targetPosition	Position of the sample being estimated
		 * @param targetNormal		Surface orientation of the sample being estimated
		 * @return					Weight to give this sample
		 */
		double weight(Point3d targetPosition, Vector3d targetNormal) {
			double distanceError = this.position.distance( targetPosition );
			// test for zero, or will have infinite radius
			if (this.R0 > 0.0) {
				// divide by mean harmonic distance
				distanceError *= this.R0;
			}
			// distanceError = 0.0;

			// error based on normals
			double orientationError = Math.sqrt(1.0 - Math.min(1.0, this.normal.dot(targetNormal)));
			// orientationError = 0.0;
			
			// return inverse of error
			return 1.0 / (distanceError + orientationError);

		}

	}

}