package sds;

import java.awt.Rectangle;
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
	final int num = count++;
	public final int sides;
	private final double recSides;
	
	public Vertex facePoint;
	final int level;
	final Iterable<HalfEdge> edgeIterable = new Iterable<HalfEdge>() {
		public Iterator<HalfEdge> iterator() {
			return new Iterator<HalfEdge>() {
				private HalfEdge e = edge;
				private int i;
				public boolean hasNext() {
					return i < sides;
				}

				public HalfEdge next() {
					i++;
					return e = e.next;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	};
	HalfEdge edge;
	boolean needsSubdivision;
	
	Face(int sides, int level) {
		this.sides = sides;
		this.level = level;
		recSides = 1.0 / sides;
	}
	
	void computeFacePoint() {
		facePoint = new Vertex();
		for (HalfEdge edge : getEdges()) {
			facePoint.position.add(edge.vertex.position);
		}
		facePoint.position.scale(recSides);
	}
	
	public void subdivide(int maxLevel, Slate slate) {
		Vertex[] va = new Vertex[10];
		float[][][] boundary = new float[4][][];
		int i = 0;
		for (HalfEdge edge : getEdges()) {
			int valence = edge.vertex.valence();
			float[][] corner = new float[valence - 2][3];
			boundary[i] = corner;
			Point3d p = edge.vertex.position;
			corner[0][0] = (float) p.x;
			corner[0][1] = (float) p.y;
			corner[0][2] = (float) p.z;
			int j = 0;
			int start = -1;
			for (HalfEdge e : edge.vertex.getAdjacentEdges()) {
				if (e.face == this) {
					start = i;
				}
				va[j++] = e.getSecondVertex();
			}
			for (int k = 0; k < valence - 2; k++) {
				int index = (start + k) % valence;
				p = va[k].position;
				corner[k][0] = (float) p.x;
				corner[k][1] = (float) p.y;
				corner[k][2] = (float) p.z;
//				System.out.println(i + " " + k + " " + p);
			}
			i++;
		}
		slate.subdivide(maxLevel, boundary);
	}
	
	int getScreenSize() {
		assert sides == 4;
		HalfEdge e = edge;
		Point3d p0 = e.vertex.position;
		e = e.next;
		Point3d p1 = e.vertex.position;
		e = e.next;
		Point3d p2 = e.vertex.position;
		Point3d p3 = e.next.vertex.position;
		
		// TODO z-devide for perspective viewports
		
		int x0 = (int) p0.x;
		int x1 = (int) p1.x;
		int x2 = (int) p2.x;
		int x3 = (int) p3.x;
		int y0 = (int) p0.y;
		int y1 = (int) p1.y;
		int y2 = (int) p2.y;
		int y3 = (int) p3.y;
		int xmin = x0;
		int xmax = x0;
		int ymin = y0;
		int ymax = y0;
		if (x1 < xmin) xmin = x1;
		if (x2 < xmin) xmin = x2;
		if (x3 < xmin) xmin = x3;
		if (x1 > xmax) xmax = x1;
		if (x2 > xmax) xmax = x2;
		if (x3 > xmax) xmax = x3;
		if (y1 < ymin) ymin = y1;
		if (y2 < ymin) ymin = y2;
		if (y3 < ymin) ymin = y3;
		if (y1 > ymax) ymax = y1;
		if (y2 > ymax) ymax = y2;
		if (y3 > ymax) ymax = y3;
		int dx = xmax - xmin;
		int dy = ymax - ymin;
		return (dx > dy) ? dx : dy; 
	}
	
	public Iterable<HalfEdge> getEdges() {
		return edgeIterable;
	}
	
	public String toString() {
		return "f" + num;
	}
}
