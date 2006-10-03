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
	public int sides;
	public Vertex facePoint = new Vertex(0, 0, 0);
	Iterable<Edge> edgeIterable = new Iterable<Edge>() {
		public Iterator<Edge> iterator() {
			return new Iterator<Edge>() {
				private Edge e = edge;
				private boolean unused = true;
				public boolean hasNext() {
					return unused || e != edge;
				}

				public Edge next() {
					unused = false;
					return e = e.next;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
				
			};
		}
	};
	
	Edge edge;
	
	public Iterable<Edge> getEdges() {
		return edgeIterable;
	}
}
