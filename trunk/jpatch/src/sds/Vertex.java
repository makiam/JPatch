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
	Edge edge;
	Point3d position;
	Vector3d normal = new Vector3d();
	Iterable<Edge> edgeIterable = new Iterable<Edge>() {
		public Iterator<Edge> iterator() {
			return new Iterator<Edge>() {
				private Edge e = edge;
				private boolean unused = true;
				public boolean hasNext() {
					return e.neighbor != null && (unused || e.neighbor.next != edge);
				}

				public Edge next() {
					if (unused) {
						unused = false;
						return e;
					}
					return e = e.neighbor.next;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
				
			};
		}
	};
	
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
	
	void smooth() {
		Vector3d va = new Vector3d();
		Vector3d v0 = new Vector3d();
		Vector3d v1 = new Vector3d();
		Vector3d n = new Vector3d();
		normal.set(0, 0, 0);
		int i = 0;
		for (Edge edge : getAdjacentEdges()) {
			v1.sub(edge.vertex1.position, position);
			if (i != 0) {
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
		while (edge.prev.neighbor != null && edge.prev.neighbor != e) {
			edge = edge.prev.neighbor;
		}
	}
}
