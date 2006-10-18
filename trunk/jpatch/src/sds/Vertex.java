package sds;

import java.util.Iterator;

import javax.vecmath.*;

/**
 * 
 */

/**
 * @author sascha
 *
 */
public class Vertex {
	static int count;
	final int num = count++;
	HalfEdge edge;
	final Point3d position;
	final Vector3d normal = new Vector3d();
	final Iterable<Face> faceIterable = new Iterable<Face>() {
		public Iterator<Face> iterator() {
			return new Iterator<Face>() {
				private HalfEdge e = edge.pair;
				boolean hasNext = true;
				
				public boolean hasNext() {
					return hasNext;
				}
				
				public Face next() {
					HalfEdge tmp = e;
					e = e.next.pair;
					hasNext = (e.next != null && e != edge.pair);
					return tmp.face;
				}
				
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	};
	final Iterable<HalfEdge> edgeIterable = new Iterable<HalfEdge>() {
		public Iterator<HalfEdge> iterator() {
			return new Iterator<HalfEdge>() {
				private HalfEdge e = edge;
				boolean hasNext = true;
				
				public boolean hasNext() {
					return hasNext;
				}
				
				public HalfEdge next() {
					HalfEdge tmp = e;
					e = e.pair.next;
					hasNext = (e != null && e != edge);
					return tmp;
				}
				
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	};
	
	public Vertex() {
		position = new Point3d();
	}
	
	public Vertex(double x, double y, double z) {
		position = new Point3d(x, y, z);
	}
	
	public Point3d getPosition() {
		return position;
	}
	
	public Vector3d getNormal() {
		return normal;
	}
	
	public Iterable<HalfEdge> getAdjacentEdges() {
		return edgeIterable;
	}
	
	public Iterable<Face> getAdjacentFaces() {
		return faceIterable;
	}
	
	void smooth() {
		Vector3d va = new Vector3d();
		Vector3d v0 = new Vector3d();
		Vector3d v1 = new Vector3d();
		Vector3d n = new Vector3d();
		normal.set(0, 0, 0);
		int i = 0;
		for (HalfEdge edge : getAdjacentEdges()) {
			assert edge.vertex == this;
			v1.sub(edge.pair.vertex.position, position);
			if (i > 0) {
				n.cross(v1, v0);
				n.normalize();
				normal.add(n);
			} else {
				va.set(v1);
			}
			v0.set(v1);
			i++;
		}
		n.cross(va, v0);
		n.normalize();
		normal.add(n);
		normal.normalize();
	}
	
	void moveVertex() {
		double Fx = 0, Fy = 0, Fz = 0, Rx = 0, Ry = 0, Rz = 0;
		int f = 0;
		for (Face face : getAdjacentFaces()) {
			Point3d p = face.facePoint.position;
			Fx += p.x;
			Fy += p.y;
			Fz += p.z;
			f++;
		}
		double sc = 1.0 / f;
		Fx *= sc;
		Fy *= sc;
		Fz *= sc;
//		System.out.println("    F=" + F + " (avg of " + f + " facepoints)");
		int n = 0;
		for (HalfEdge edge : getAdjacentEdges()) {
			if (edge.isMaster()) {
				Point3d p = edge.midPoint2;
				Rx += p.x;
				Ry += p.y;
				Rz += p.z;
			} else {
				Point3d p = edge.pair.midPoint2;
				Rx += p.x;
				Ry += p.y;
				Rz += p.z;
			}
			n++;
		}
		sc = 1.0 / n;
		Rx *= sc;
		Ry *= sc;
		Rz *= sc;
//		System.out.println("    2R=" + R + " (2x avg of " + n + " edge-midpoints)");
//		if (!interpolateBoundary || n == f) {
			position.scale(n - 3);
			position.x += Rx;
			position.y += Ry;
			position.z += Rz;
			position.x += Fx;
			position.y += Fy;
			position.z += Fz;
			position.scale(sc);
//		}
	}
	
	void validate() {
		HalfEdge e = edge;
//		System.out.println("validating " + num + " edge=" + edge);
		while (edge.prev != null && edge.prev.pair != e) {
			edge = edge.prev.pair;
//			System.out.println("    edge=" + edge);
		}
//		
//		boolean l = false;
//		while (edge.prev.neighbor != null && edge.prev.neighbor != e) {
//			edge = edge.prev.neighbor;
//			l = true;
//		}
//		if (l) {
//			edge = edge.prev;
//		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("v").append(num).append(" e=").append(edge).append(" p=").append(position).append(" el:");
		for (HalfEdge e : edgeIterable) {
			sb.append(e).append(" ");
		}
		sb.append("fl:");
		for (Face f : faceIterable) {
			sb.append(f.num).append(" ");
		}
		return sb.toString();
	}
}
