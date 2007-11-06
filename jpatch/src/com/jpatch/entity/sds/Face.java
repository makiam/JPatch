package com.jpatch.entity.sds;

import static com.jpatch.entity.sds.SdsWeights.*;

import java.util.Iterator;

import com.jpatch.afw.attributes.*;
import com.jpatch.entity.*;
import javax.vecmath.*;

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
	final Slate2[] slates;
	
	final SlateEdge[] slateEdges;
	private Material material = new BasicMaterial(new Color3f(1.0f, 1.0f, 1.0f));
	
	public final Level2Vertex facePoint;
	public final HalfEdge[] edges;
//	final Iterable<HalfEdge> edgeIterable = new Iterable<HalfEdge>() {
//		public Iterator<HalfEdge> iterator() {
//			return new Iterator<HalfEdge>() {
//				private HalfEdge e = edge;
//				private int i;
//				public boolean hasNext() {
//					return i < sides;
//				}
//
//				public HalfEdge next() {
//					i++;
//					HalfEdge tmp = e;
//					e = e.next;
//					return tmp;
//				}
//
//				public void remove() {
//					throw new UnsupportedOperationException();
//				}
//			};
//		}
//	};
	
	Face(int sides, HalfEdge start) {
		this.sides = sides;
		edges = new HalfEdge[sides];
		HalfEdge edge = start;
		for (int i = 0; i < sides; i++) {
			edges[i] = edge;
			edge = edge.next;
		}
		
		recSides = 1.0 / sides;
		
//		facePoint = new Level2Vertex(getFacePointLc(), getLimitLc(), getTangentLc(0), getTangentLc(1));
		
		facePoint = new Level2Vertex() {
			@Override
			public void computeDerivedPosition() {
				if (positionValid) {
					return;
				}
				double x = 0, y = 0, z = 0;
				for (HalfEdge edge : getEdges()) {
					Tuple3Attr t = edge.vertex.position;
					x += t.getX();
					y += t.getY();
					z += t.getZ();
				}
				position.setTuple(x * recSides, y * recSides, z * recSides);
				positionValid = true;
			}
			
			@Override
			public void computeLimit() {
				if (limitValid) {
					return;
				}
				double fx = 0, fy = 0, fz = 0;
				double ex = 0, ey = 0, ez = 0;
				for (HalfEdge edge : edges) {
					Tuple3Attr t = edge.vertex.vertexPoint.position;
					fx += t.getX();
					fy += t.getY();
					fz += t.getZ();
					t = edge.edgePoint.position;
					ex += t.getX();
					ey += t.getY();
					ez += t.getZ();
				}
				limit.set(
						fx * VERTEX_FACE_LIMIT[Face.this.sides] + ex * VERTEX_EDGE_LIMIT[Face.this.sides] + position.getX() * VERTEX_POINT_LIMIT[Face.this.sides],
						fy * VERTEX_FACE_LIMIT[Face.this.sides] + ey * VERTEX_EDGE_LIMIT[Face.this.sides] + position.getY() * VERTEX_POINT_LIMIT[Face.this.sides],
						fz * VERTEX_FACE_LIMIT[Face.this.sides] + ez * VERTEX_EDGE_LIMIT[Face.this.sides] + position.getZ() * VERTEX_POINT_LIMIT[Face.this.sides]
				);
				
				float ax = 0;
				float ay = 0;
				float az = 0;
				float bx = 0;
				float by = 0;
				float bz = 0;
				int i = 0;
				for (HalfEdge edge : edges) {
					HalfEdge nextEdge = edge.next;
					Tuple3Attr t0f = edge.face.facePoint.position;
					Tuple3Attr t0e = edge.edgePoint.position;
					Tuple3Attr t1f = nextEdge.face.facePoint.position;
					Tuple3Attr t1e = nextEdge.edgePoint.position;
					float ew = TANGENT_EDGE_WEIGHT[Face.this.sides][i];
					float fw = TANGENT_FACE_WEIGHT[Face.this.sides][i];
					ax += t1f.getX() * fw;
					ay += t1f.getY() * fw;
					az += t1f.getZ() * fw;
					ax += t1e.getX() * ew;
					ay += t1e.getY() * ew;
					az += t1e.getZ() * ew;
					bx += t0f.getX() * fw;
					by += t0f.getY() * fw;
					bz += t0f.getZ() * fw;
					bx += t0e.getX() * ew;
					by += t0e.getY() * ew;
					bz += t0e.getZ() * ew;
					i++;
				}
				uTangent.set(bx, by, bz);
				vTangent.set(ax, ay, az);
				normal.cross(uTangent, vTangent);
				normal.normalize();
				limitValid = true;
			}
		};
		
		slates = new Slate2[sides];
		slateEdges = new SlateEdge[sides];
		HalfEdge e = edge;
		for (int i = 0; i < sides; i++) {
			slates[i] = new Slate2();
		}
		for (int i = 0; i < sides; i++) {
			e.face = this;
			slateEdges[i] = new SlateEdge(slates[(i + 1) % sides], slates[i], facePoint, e.edgePoint, this);
			e = e.next;
		}
		
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public void setMaterial(Material material) {
		this.material = material;
	}
	
	public LinearCombination<TopLevelVertex> getFacePointLc() {
		LinearCombination<TopLevelVertex> lc = new LinearCombination<TopLevelVertex>();
		double weight = 1.0 / sides;
		for (HalfEdge edge : getEdges()) {
			lc.add(edge.vertex, weight);
		}
		return lc;
	}
	
	void prepareSlates() {
		HalfEdge e = edges[0];
		for (int n = 0; n < sides; n++) {
			Face f = e.face;
			e.slateEdge0.slate = f.slates[f.getEdgeIndex(e)];
			e.slateEdge1.slate = f.slates[f.getEdgeIndex(e.next)];
			e = e.next;
		}
	}
	
	void setupSlates() {
		HalfEdge edge = edges[0];
		
		for (int n = 0; n < sides; n++) {
			SlateEdge[][] corners = new SlateEdge[4][];
			
			/* create SlateEdges for corner 0 (outer corner) */
			final int valence = edge.vertex.valence;
			
//			System.out.println("face=" + this + " side=" + n + " valence=" + valence);
			corners[0] = new SlateEdge[valence];
			
			HalfEdge e = edge.vertex.edges[0];
			int offset = valence - edge.vertex.getEdgeIndex(edge);
			
			for (int i = 0; i < valence; i++) {
				corners[0][(i + offset) % valence] = edge.vertex.edges[i].slateEdge0;
//				if (e.prev != null) {
//					e = e.prev.pair;
//				}
			}
//			for (HalfEdge e : edge.vertex.getAdjacentEdges()) {
//				corners[0][(i + offset) % valence] = e.slateEdge0;
//				i++;
//			}
			
//			for (int i = 0; i < edge.vertex.valence; i++) {
//				corners[0][i] = e.slateEdge0;
////				if (e.prev.pair == null) {
////					e = e.pair.next;
////				} else {
//					e = e.prev.pair;
////				}
//			}
			
			/* create SlateEdges for corner 1 (upper right) */
			corners[1] = new SlateEdge[4];
			corners[1][0] = slateEdges[n].pair;
			corners[1][1] = edge.slateEdge0.pair;
			corners[1][2] = edge.pair.face == null ? null : edge.pair.face.slateEdges[edge.pair.face.getEdgeIndex(edge.pair)].pair;
			corners[1][3] = edge.slateEdge1;
			
			/* create SlateEdges for corner 2 (inner corner) */
			corners[2] = new SlateEdge[sides];
			for (int i = 0; i < sides; i++) {
				corners[2][i] = slateEdges[(sides - 1 + n + i) % sides];
			}
			
			/* create SlateEdges for corner 3 (lower left) */
			corners[3] = new SlateEdge[4];
			corners[3][0] = edge.prev.slateEdge1;
			corners[3][1] = slateEdges[(sides - 1 + n) % sides].pair;
			corners[3][2] = edge.prev.slateEdge0.pair;
			corners[3][3] = edge.prev.pair.face == null ? null : edge.prev.pair.face.slateEdges[edge.prev.pair.face.getEdgeIndex(edge.prev.pair)].pair;
			
			slates[n].setCorners(corners);
			
			edge = edge.next;
		}
		
	}
	
	public void initFans() {
		for (Slate2 slate : slates) {
			slate.initFans();
		}
	}
	
//	void setupSlates() {
//		Point3d[][] p = new Point3d[4][];
//		BaseVertex[][] v = new BaseVertex[4][];
//		HalfEdge[][] edges = new HalfEdge[4][];
//		
//		int s = 0;
//		try {
//			for (HalfEdge edge : getEdges()) {
//				v[0] = new BaseVertex[sides * 2 - 4];
//				v[0][0] = facePoint;
//				p[0] = new Point3d[sides * 2 - 4];
//				p[0][0] = v[0][0].pos;
//				HalfEdge e = edge.next;
//				v[0][1] = e.edgePoint;
//				p[0][1] = v[0][1].pos;
//				for (int i = 2; i < p[0].length; ) {
//					e = e.next;
//					v[0][i] = e.vertex.vertexPoint;
//					p[0][i] = v[0][i++].pos;
//					v[0][i] = e.edgePoint;
//					p[0][i] = v[0][i++].pos;
//				}
//				
//				v[1] = new BaseVertex[4];
//				p[1] = new Point3d[4];
//				e = edge.prev.pair;
//				v[1][0] = e.edgePoint;
//				v[1][1] = e.next.vertex.vertexPoint;
//				v[1][2] = e.next.edgePoint;
//				v[1][3] = e.face.facePoint;
//				p[1][0] = v[1][0].pos;
//				p[1][1] = v[1][1].pos;
//				p[1][2] = v[1][2].pos;
//				p[1][3] = v[1][3].pos;
//				
//				v[2] = new BaseVertex[edge.vertex.valence() * 2 - 4];
//				p[2] = new Point3d[edge.vertex.valence() * 2 - 4];
//				v[2][0] = edge.vertex.vertexPoint;
//				p[2][0] = v[2][0].pos;
//				e = edge.prev.pair.prev.pair;
//				v[2][1] = e.edgePoint;
//				p[2][1] = v[2][1].pos;
//				for (int i = 2; i < p[2].length; ) {
//					v[2][i] = e.face.facePoint;
//					p[2][i] = v[2][i++].pos;
//					e = e.prev.pair;
//					v[2][i] = e.edgePoint;
//					p[2][i] = v[2][i++].pos;
//				}
//				
//				v[3] = new BaseVertex[4];
//				p[3] = new Point3d[4];
//				e = edge.pair;
//				v[3][0] = e.edgePoint;
//				v[3][1] = e.face.facePoint;
//				v[3][2] = e.prev.edgePoint;
//				v[3][3] = e.vertex.vertexPoint;
//				p[3][0] = v[3][0].pos;
//				p[3][1] = v[3][1].pos;
//				p[3][2] = v[3][2].pos;
//				p[3][3] = v[3][3].pos;
//				
//				slates[s++] = new Slate(p, v);
//			}
//		} catch (Exception e) {
//			for (int i = 0; i < sides; i++) {
//				slates[i] = null;
//			}
//		}
//	}
	
	int getEdgeIndex(HalfEdge edge) {
		for (int i = 0; i < sides; i++) {
			if (edges[i] == edge) {
				return i;
			}
		}
		throw new IllegalArgumentException(edge.toString());
	}
	
//	void setupSlateNeighbors() {
//		int s = 0;
//		for (HalfEdge edge : getEdges()) {
//			if (slates[s] == null) {
//				continue;
//			}
//			int splus = (s + 1) % sides;
//			int sminus = (s + sides - 1) % sides;
//			slates[s].adjacentSlates[0] = slates[sminus];
//			slates[s].adjacentSlates[3] = slates[splus];
//			Face f1 = edge.prev.pair.face;
//			int e = 0;
//			for (HalfEdge ee : f1.getEdges()) {
//				if (ee.pair == edge.prev) {
//					break;
//				}
//				e++;
//			}
//			slates[s].adjacentSlates[1] = f1.slates[e];
//			Face f2 = edge.pair.face;
//			e = 0;
//			for (HalfEdge ee : f2.getEdges()) {
//				if (ee.prev.pair == edge) {
//					break;
//				}
//				e++;
//			}
//			slates[s].adjacentSlates[2] = f2.slates[e];
//			
//			
////			// check
////			for (int side = 0; side < 4; side++) {
////				if (slates[s].adjacentSlates[side] != null) {
////					Slate shouldBeThisOne = slates[s].adjacentSlates[side].adjacentSlates[3 - side];
////					System.out.println("Slate=" + slates[s] + " side=" + side + " adjacentSlate=" + slates[s].adjacentSlates[side] + " back=" + shouldBeThisOne);
////					if (shouldBeThisOne != null && shouldBeThisOne != slates[s]) {
////						System.err.println("bad slate adjacency");
////					}
////				}
////			}
//			
//			s++;
//		}
//	}
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
	
	public Slate2[] getSlates() {
		return slates;
	}
	
	public HalfEdge[] getEdges() {
		return edges;
	}
	
	public String toString() {
		return "f" + num;
	}
}
