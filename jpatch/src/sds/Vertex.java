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
	int num = count++;
	Edge edge;
	Point3d position;
	Vector3d normal = new Vector3d();
	Iterable<Face> faceIterable = new Iterable<Face>() {
		public Iterator<Face> iterator() {
			return new Iterator<Face>() {
				private Edge e = edge;
				private boolean end = false;
				public boolean hasNext() {
					return !end;
				}
				
				public Face next() {
					Edge tmp = e;
					e = e.next.neighbor;
					end = (e == null || e == edge);
					return tmp.face;
				}
				
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	};
	Iterable<Edge> edgeIterable = new Iterable<Edge>() {
		public Iterator<Edge> iterator() {
			return new Iterator<Edge>() {
				private Edge e = edge;
//				private boolean unused = true;
				boolean end = false;
				boolean end2 = false;
				public boolean hasNext() {
					return !end;
				}

				private Edge computeNext() {
					Edge next = e.next;
					Edge nextNeighbor = next.neighbor;
					if (nextNeighbor == null) {
						end = end2;
						end2 = true;
						return next;
					}
					if (nextNeighbor == edge) {
						end = true;
					} else {
						end = end2;
					}
					return nextNeighbor;
				}
				
				public Edge next() {
					Edge tmp = e;
					e = computeNext();
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
	
	public Iterable<Edge> getAdjacentEdges() {
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
		for (Edge edge : getAdjacentEdges()) {
			if (edge.vertex0 == this) {
				v1.sub(edge.vertex1.position, position);
			} else {
				v1.sub(edge.vertex0.position, position);
			}
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
	
	void validate() {
		Edge e = edge;
//		System.out.println("validating " + num + " edge=" + edge);
		while (edge.neighbor != null && edge.neighbor.prev != e) {
			edge = edge.neighbor.prev;
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
		for (Edge e : edgeIterable) {
			sb.append(e).append(" ");
		}
		sb.append("fl:");
		for (Face f : faceIterable) {
			sb.append(f.num).append(" ");
		}
		return sb.toString();
	}
}
