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
	final Vertex firstVertex;
	final Vertex secondVertex;
	final int level;
	Edge neighbor;
	Face face;
	Edge prev;
	Edge next;
	Vertex edgePoint;
	Point3d midPoint2;
	
	public Edge(Vertex vertex0, Vertex vertex1, int level) {
		this.firstVertex = vertex0;
		this.secondVertex = vertex1;
		this.level = level;
	}
	
	public Vertex getVertex0() {
		return firstVertex;
	}
	
	public Vertex getVertex1() {
		return secondVertex;
	}
	
	public Face getRightFace() {
		return face;
	}
	
	public Face getLeftFace() {
		return neighbor.face;
	}
	
	public boolean isMaster() {
		return face != null && (neighbor.face == null || firstVertex.hashCode() < secondVertex.hashCode());
	}
	
	@Override
	public int hashCode() {
		return (firstVertex.hashCode() << 1) ^ secondVertex.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Edge) {
			Edge e = (Edge) o;
			return firstVertex == e.firstVertex && secondVertex == e.secondVertex;
		}
		return false;
	}
	
	public String toString() {
		return isMaster() ? firstVertex.num + "+" + secondVertex.num : firstVertex.num + "-" + secondVertex.num;
	}
}
