package sds;

import javax.vecmath.Point3d;

/**
 * 
 */

/**
 * @author sascha
 *
 */
public class Edge {
	Vertex vertex0;
	Vertex vertex1;
	int level;
	Edge neighbor;
	Face face;
	Edge prev;
	Edge next;
	public Vertex edgePoint;
	public Point3d midPoint2;
	public Edge(Vertex vertex0, Vertex vertex1) {
		this.vertex0 = vertex0;
		this.vertex1 = vertex1;
	}
	
	public Edge(Vertex vertex0, Vertex vertex1, int level) {
		this.vertex0 = vertex0;
		this.vertex1 = vertex1;
		this.level = level;
	}
	
	public Vertex getVertex0() {
		return vertex0;
	}
	
	public Vertex getVertex1() {
		return vertex1;
	}
	
	public Face getFace0() {
		return face;
	}
	
	public Face getFace1() {
		return neighbor.face;
	}
	
	public boolean isMaster() {
		return neighbor == null || vertex0.hashCode() < vertex1.hashCode();
	}
	
	@Override
	public int hashCode() {
		return (vertex0.hashCode() << 1) ^ vertex1.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Edge) {
			Edge e = (Edge) o;
			return vertex0 == e.vertex0 && vertex1 == e.vertex1;
		}
		return false;
	}
	
	public String toString() {
		return isMaster() ? vertex0.num + "+" + vertex1.num : vertex0.num + "-" + vertex1.num;
	}
}
