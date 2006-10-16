package sds;

import java.util.Iterator;

import javax.vecmath.Point3d;

/**
 * 
 */

/**
 * @author sascha
 *
 */
public class Face {
	static int count;
	int num = count++;
	public final int sides;
	public Vertex facePoint;
	final int level;
	final Iterable<Edge> edgeIterable = new Iterable<Edge>() {
		public Iterator<Edge> iterator() {
			return new Iterator<Edge>() {
				private Edge e = edge;
				private int i;
				public boolean hasNext() {
					return i < sides;
				}

				public Edge next() {
					i++;
					return e = e.next;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
				
			};
		}
	};
	Edge edge;
	boolean needsSubdivision;
	
	Face(int sides, int level) {
		this.sides = sides;
		this.level = level;
	}
	
	void checkError() {
		if (sides != 4) {
			needsSubdivision = true;
			return;
		}
		Edge e = edge;
		Point3d p0 = e.vertex0.position;
		e = e.next;
		Point3d p1 = e.vertex0.position;
		e = e.next;
		Point3d p2 = e.vertex0.position;
		Point3d p3 = e.vertex1.position;
		/*
		 * error = |p0-p1+p2-p3| + |p2-p0| + |p3-p1|
		 */
		double x = p0.x - p1.x + p2.x -p3.x;
		double y = p0.y - p1.y + p2.y -p3.y;
		double z = p0.z - p1.z + p2.z -p3.z;
		double error = Math.sqrt(x * x + y * y + z * z);
		x = p2.x - p0.x;
		y = p2.y - p0.y;
		z = p2.z - p0.z;
		error += Math.sqrt(x * x + y * y + z * z);
		x = p3.x - p1.x;
		y = p3.y - p1.y;
		z = p3.z - p1.z;
		error += Math.sqrt(x * x + y * y + z * z);
		needsSubdivision = (error > 16);
	}
	
	public Iterable<Edge> getEdges() {
		return edgeIterable;
	}
	
	public String toString() {
		return "f" + num;
	}
}
