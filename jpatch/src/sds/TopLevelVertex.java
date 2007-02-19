package sds;

import java.util.Iterator;
import java.util.Random;

import javax.vecmath.*;

import jpatch.entity.Attribute;
import jpatch.entity.AttributeListener;
import jpatch.entity.Constants;

/**
 * 
 */

/**
 * @author sascha
 *
 */
public class TopLevelVertex extends AbstractVertex {
	HalfEdge edge;
	
	public Level2Vertex vertexPoint;
	
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
	
	public TopLevelVertex() {
		super();
	}
	
	public TopLevelVertex(double x, double y, double z) {
		this();
		referencePosition.set(x, y, z);
	}
	
	public TopLevelVertex(Point3d p) {
		this();
		referencePosition.set(p);
	}
	
	public Iterable<HalfEdge> getAdjacentEdges() {
		return edgeIterable;
	}
	
	public Iterable<Face> getAdjacentFaces() {
		return faceIterable;
	}
	
	public int valence() {
		int i = 1; 
		HalfEdge e = edge.pair;
		while (e.next != null && e.next != edge) {
			e = e.next.pair;
			i++;
		}
		return i;
	}
	
	public boolean isBoundary() {
		return edge.pair.face == null;
	}
	
//	public void initFragment(int valence, float[][] buffer) {
////		System.out.println("initfragment " + edge.vertex.num + "\t" + edge.vertex.position);
//		Point3d p = edge.vertex.position;
//		buffer[0][0] = (float) p.x;
//		buffer[0][1] = (float) p.y;
//		buffer[0][2] = (float) p.z;
//		HalfEdge e = edge.next.next;
//		int j = 1;
//		for (int i = 0; i < valence; i++) {
////			System.out.println("\t1:" + e.vertex.num + "\t" + e.vertex.position);
//			p = e.vertex.position;
//			buffer[j][0] = (float) p.x;
//			buffer[j][1] = (float) p.y;
//			buffer[j++][2] = (float) p.z;
//			e = e.next.pair.next;
////			System.out.println("\t1:" + e.vertex.num + "\t" + e.vertex.position);
//			p = e.vertex.position;
//			buffer[j][0] = (float) p.x;
//			buffer[j][1] = (float) p.y;
//			buffer[j++][2] = (float) p.z;
//			e = e.next;
//		}
//		e = edge.next.pair.next.next.next.pair.next.next;
//		for (int i = 0; i < valence; i++) {
////			System.out.println("\t2:" + e.vertex.num + "\t" + e.vertex.position);
//			p = e.vertex.position;
//			buffer[j][0] = (float) p.x;
//			buffer[j][1] = (float) p.y;
//			buffer[j++][2] = (float) p.z;
//			e = e.next.pair.next;
////			System.out.println("\t2:" + e.vertex.num + "\t" + e.vertex.position);
//			p = e.vertex.position;
//			buffer[j][0] = (float) p.x;
//			buffer[j][1] = (float) p.y;
//			buffer[j++][2] = (float) p.z;
//			e = e.next.pair.next;
////			System.out.println("\t2:" + e.vertex.num + "\t" + e.vertex.position);
//			p = e.vertex.position;
//			buffer[j][0] = (float) p.x;
//			buffer[j][1] = (float) p.y;
//			buffer[j++][2] = (float) p.z;
//			e = e.next.pair.next;
////			System.out.println("\t2:" + e.vertex.num + "\t" + e.vertex.position);
//			p = e.vertex.position;
//			buffer[j][0] = (float) p.x;
//			buffer[j][1] = (float) p.y;
//			buffer[j++][2] = (float) p.z;
//			e = e.next;
//		}
//	}
	
//	void smooth() {
//		Vector3d va = new Vector3d();
//		Vector3d v0 = new Vector3d();
//		Vector3d v1 = new Vector3d();
//		Vector3d n = new Vector3d();
//		normal.set(0, 0, 0);
//		int i = 0;
//		for (HalfEdge edge : getAdjacentEdges()) {
//			assert edge.vertex == this;
//			v1.sub(edge.pair.vertex.position, position);
//			if (i > 0) {
//				n.cross(v1, v0);
//				n.normalize();
//				normal.add(n);
//			} else {
//				va.set(v1);
//			}
//			v0.set(v1);
//			i++;
//		}
//		n.cross(va, v0);
//		n.normalize();
//		normal.add(n);
//		normal.normalize();
//	}
	
	void bindVertexPoint() {
		final int valence = valence();
//		final int stencilSize = valence * 2;
//		final AbstractVertex[] stencil = new AbstractVertex[stencilSize];
//		int i = 0;
//		for (HalfEdge edge : getAdjacentEdges()) {
//			stencil[i++] = edge.face.facePoint;
//			stencil[i++] = edge.pair.vertex;
//		}
		vertexPoint = new Level2Vertex() {
			@Override
			public void computeDerivedPosition() {
				if (!overridePosition.get()) {
					if (TopLevelVertex.this.sharpness.get() > 0) {
						position.set(TopLevelVertex.this.pos);
					} else {
						final double k = 1.0 / (valence * valence);
						double w = (valence - 2.0) / valence;
						double x = 0, y = 0, z = 0;
						for (HalfEdge edge : getAdjacentEdges()) {
							Point3d p = edge.face.facePoint.pos;
							x += p.x;
							y += p.y;
							z += p.z;
							p = edge.pair.vertex.pos;
							x += p.x;
							y += p.y;
							z += p.z;
						}
//						for (int i = 1; i < stencil.length; i++) {
//							x += stencil[i].pos.x;
//							y += stencil[i].pos.y;
//							z += stencil[i].pos.z;
//						}
						position.set(x * k + TopLevelVertex.this.pos.x * w, y * k + TopLevelVertex.this.pos.y * w, z * k + TopLevelVertex.this.pos.z * w);
					}
				}
				if (!overrideSharpness.get()) {
					sharpness.set(Math.max(0, TopLevelVertex.this.sharpness.get() - 1));
				}
			}
		};
	}
	
//	void bindLimitPoint() {
//		limitPoint = new Vertex();
//		final int n = valence();
//		final int stencilSize = n * 2 + 1;
//		final Vertex[] stencil = new Vertex[stencilSize];
//		final double[] weight = new double[stencilSize];
//		final double k = 1.0 / (n * (n + 5));
//		stencil[0] = vertexPoint;
//		weight[0] = (n * n) * k;
//		int i = 1;
//		for (HalfEdge edge : getAdjacentEdges()) {
//			stencil[i] = edge.face.facePoint;
//			weight[i] = 1 * k;
//			i++;
//			stencil[i] = edge.edgePoint;
//			weight[i] = 4 * k;
//			i++;
//		}
//		double test = 0.0;
//		for (double w : weight) {
//			test += w;
//		}
//		System.out.println("valence=" + n + " weight sum=" + test);
//		limitPoint.setStencil(stencil, weight);
//	}
	
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
	
//	public String toString() {
//		if (true) return "Vertex";
//		StringBuilder sb = new StringBuilder();
//		sb.append("v").append(num).append(" e=").append(edge).append(" p=").append(position).append(" el:");
//		for (HalfEdge e : edgeIterable) {
//			sb.append(e).append(" ");
//		}
//		sb.append("fl:");
//		for (Face f : faceIterable) {
//			sb.append(f.num).append(" ");
//		}
//		return sb.toString();
//	}
	
	abstract class DerivedVertex extends TopLevelVertex {
		abstract void computeDerivedPosition();
	}
}
