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
	final int num = count++;
	public final int sides;
	private final double recSides;
	private final Slate[] slates;
	Vertex facePoint = new Vertex();
	HalfEdge edge;
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
	
	Face(int sides) {
		this.sides = sides;
		recSides = 1.0 / sides;
		slates = new Slate[sides];
	}
	
	void bindFacePoint() {
		final Vertex[] stencil = new Vertex[sides];
		final double[] weight = new double[sides];
		int i = 0;
		for (HalfEdge edge : getEdges()) {
			stencil[i] = edge.vertex;
			weight[i] = recSides;
			i++;
		}
		facePoint.setStencil(stencil, weight);
	}
	
	void setupSlates() {
		Point3d[][] p = new Point3d[4][];
		int s = 0;
		for (HalfEdge edge : getEdges()) {
			p[0] = new Point3d[sides * 2 - 4];
			p[0][0] = facePoint.pos;
			HalfEdge e = edge.next;
			p[0][1] = e.edgePoint.pos;
			for (int i = 2; i < p[0].length; ) {
				e = e.next;
				p[0][i++] = e.vertex.vertexPoint.pos;
				p[0][i++] = e.edgePoint.pos;
			}
			
			p[1] = new Point3d[4];
			e = edge.prev.pair;
			p[1][0] = e.edgePoint.pos;
			p[1][1] = e.next.vertex.vertexPoint.pos;
			p[1][2] = e.next.edgePoint.pos;
			p[1][3] = e.face.facePoint.pos;
			
			p[2] = new Point3d[edge.vertex.valence() * 2 - 4];
			p[2][0] = edge.vertex.vertexPoint.pos;
			e = edge.prev.pair.prev.pair;
			p[2][1] = e.edgePoint.pos;
			for (int i = 2; i < p[2].length; ) {
				p[2][i++] = e.face.facePoint.pos;
				e = e.prev.pair;
				p[2][i++] = e.edgePoint.pos;
			}
			
			p[3] = new Point3d[4];
			e = edge.pair;
			p[3][0] = e.edgePoint.pos;
			p[3][1] = e.face.facePoint.pos;
			p[3][2] = e.prev.edgePoint.pos;
			p[3][3] = e.vertex.vertexPoint.pos;
			
			slates[s++] = new Slate(p);
		}
	}
	
	void setupSlateNeighbors() {
		int s = 0;
		for (HalfEdge edge : getEdges()) {
			int splus = (s + 1) % sides;
			int sminus = (s + sides - 1) % sides;
			slates[s].adjacentSlates[0] = slates[sminus];
			slates[s].adjacentSlates[3] = slates[splus];
			Face f1 = edge.prev.pair.face;
			int e = 0;
			for (HalfEdge ee : f1.getEdges()) {
				if (ee.pair == edge.prev) {
					break;
				}
				e++;
			}
			slates[s].adjacentSlates[1] = f1.slates[e];
			Face f2 = edge.pair.face;
			e = 0;
			for (HalfEdge ee : f2.getEdges()) {
				if (ee.next.pair == edge) {
					break;
				}
				e++;
			}
			slates[s].adjacentSlates[2] = f2.slates[e];
			s++;
		}
	}
//	public void subdivide(int maxLevel, SlateTesselator slate) {
//		Vertex[] va = new Vertex[10];
//		float[][][] boundary = new float[4][][];
//		int i = 0;
//		for (HalfEdge edge : getEdges()) {
////			System.out.println("Edge " + edge + " " + edge.vertex);
//			int valence = edge.vertex.valence();
////			int size = valence - 2;
////			float[][] corner = new float[size][3];
////			boundary[i] = corner;
////			Point3d p = edge.vertex.position;
////			corner[0][0] = (float) p.x;
////			corner[0][1] = (float) p.y;
////			corner[0][2] = (float) p.z;
////			int j = 0;
////			int start = -1;
////			for (HalfEdge e : edge.vertex.getAdjacentEdges()) {
////				if (e.face == this) {
////					start = j;
////				}
////				va[j++] = e.getSecondVertex();
////			}
////			for (int k = 0; k < valence - 2; k++) {
////				int index = (start + k + 2) % valence;
////				p = va[index].position;
////				corner[k + 1][0] = (float) p.x;
////				corner[k + 1][1] = (float) p.y;
////				corner[k + 1][2] = (float) p.z;
////			}
////			i++;
//			
//			int size = valence * 2 - 4;
////			System.out.println("in face: corner=" + i + " valence=" + valence + " size=" + size);
//			float[][] corner = new float[size][3];
//			boundary[i++] = corner;
//			Point3d p = edge.vertex.pos;
////			System.out.println(edge.vertex);
//			corner[0][0] = (float) p.x;
//			corner[0][1] = (float) p.y;
//			corner[0][2] = (float) p.z;
//			HalfEdge e = edge.prev.pair.prev.pair.next;
//			p = e.vertex.pos;
////			System.out.println(e.vertex);
//			corner[1][0] = (float) p.x;
//			corner[1][1] = (float) p.y;
//			corner[1][2] = (float) p.z;
//			for (int j = 2; j < size;) {
////				System.out.println(j);
//				e = e.next;
//				p = e.vertex.pos;
////				System.out.println(j + " " + p);
//				corner[j][0] = (float) p.x;
//				corner[j][1] = (float) p.y;
//				corner[j++][2] = (float) p.z;
////				System.out.println(j);
//				e = e.next.pair.next;
//				p = e.vertex.pos;
////				System.out.println(j + " " + p);
//				corner[j][0] = (float) p.x;
//				corner[j][1] = (float) p.y;
//				corner[j++][2] = (float) p.z;
//			}
//		}
//		slate.subdivide(maxLevel, boundary);
//	}
	
//	int getScreenSize() {
//		assert sides == 4;
//		HalfEdge e = edge;
//		Point3d p0 = e.vertex.position;
//		e = e.next;
//		Point3d p1 = e.vertex.position;
//		e = e.next;
//		Point3d p2 = e.vertex.position;
//		Point3d p3 = e.next.vertex.position;
//		
//		// TODO z-devide for perspective viewports
//		
//		int x0 = (int) p0.x;
//		int x1 = (int) p1.x;
//		int x2 = (int) p2.x;
//		int x3 = (int) p3.x;
//		int y0 = (int) p0.y;
//		int y1 = (int) p1.y;
//		int y2 = (int) p2.y;
//		int y3 = (int) p3.y;
//		int xmin = x0;
//		int xmax = x0;
//		int ymin = y0;
//		int ymax = y0;
//		if (x1 < xmin) xmin = x1;
//		if (x2 < xmin) xmin = x2;
//		if (x3 < xmin) xmin = x3;
//		if (x1 > xmax) xmax = x1;
//		if (x2 > xmax) xmax = x2;
//		if (x3 > xmax) xmax = x3;
//		if (y1 < ymin) ymin = y1;
//		if (y2 < ymin) ymin = y2;
//		if (y3 < ymin) ymin = y3;
//		if (y1 > ymax) ymax = y1;
//		if (y2 > ymax) ymax = y2;
//		if (y3 > ymax) ymax = y3;
//		int dx = xmax - xmin;
//		int dy = ymax - ymin;
//		return (dx > dy) ? dx : dy; 
//	}
	
	public Slate[] getSlates() {
		return slates;
	}
	
	public Iterable<HalfEdge> getEdges() {
		return edgeIterable;
	}
	
	public String toString() {
		return "f" + num;
	}
}
