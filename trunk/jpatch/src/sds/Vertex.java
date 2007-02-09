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
public class Vertex {
	public static final int POINT = 0;
	public static final int EDGE = 1;
	public static final int FACE = 2;
	
	static int count;
	final int num = count++;
	HalfEdge edge;
	
	private Vertex[] stencil;
	private int stencilType;
	private int valence;
	
	public final Attribute.Tuple3 referencePosition = new Attribute.Tuple3(null, 0, 0, 0, false);
	public final Attribute.Tuple3 position = new Attribute.Tuple3(null, 0, 0, 0, false);
	public final Attribute.Integer sharpness = new Attribute.Integer(0);
	
	private final Matrix4d transform = new Matrix4d(Constants.IDENTITY_MATRIX);
	private final Matrix4d invTransform = new Matrix4d(Constants.IDENTITY_MATRIX);
	private boolean inverseInvalid = false;
	
	final Point3d pos = new Point3d();
	final Point3d refPos = new Point3d();
	
	public Vertex vertexPoint;
//	public Vertex limitPoint;
	
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
		position.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
//				System.out.println(ControlPoint.this + " position changed");
				position.get(pos);
				refPos.set(pos);
				if (inverseInvalid) {
					computeInverseTransform();
				}
				invTransform.transform(refPos);
				referencePosition.setValueAdjusting(true);
				referencePosition.set(refPos);
				referencePosition.setValueAdjusting(false);
			}
		});
		referencePosition.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				referencePosition.get(refPos);
				pos.set(refPos);
				transform.transform(pos);
				position.setValueAdjusting(true);
				position.set(pos);
				position.setValueAdjusting(false);
			}
		});
	}
	
	void computeDerivedPosition() {
		switch (stencilType) {
		case POINT:
			if (stencil[0].sharpness.get() > 0) {
				position.set(stencil[0].pos);
			} else {
				final double k = 1.0 / (valence * valence);
				double w = (valence - 2.0) / valence;
				double x = stencil[1].pos.x;
				double y = stencil[1].pos.y;
				double z = stencil[1].pos.z;
				for (int i = 2; i < stencil.length; i++) {
					x += stencil[i].pos.x;
					y += stencil[i].pos.y;
					z += stencil[i].pos.z;
				}
				position.set(x * k + stencil[0].pos.x * w, y * k + stencil[0].pos.y * w, z * k + stencil[0].pos.z * w);
			}
			break;
		case EDGE:
			if (valence > 0) {
				position.set(
						(stencil[0].pos.x + stencil[1].pos.x) * 0.5,
						(stencil[0].pos.y + stencil[1].pos.y) * 0.5,
						(stencil[0].pos.z + stencil[1].pos.z) * 0.5
				);
			} else {
				position.set(
						(stencil[0].pos.x + stencil[1].pos.x + stencil[2].pos.x + stencil[3].pos.x) * 0.25,
						(stencil[0].pos.y + stencil[1].pos.y + stencil[2].pos.y + stencil[3].pos.y) * 0.25,
						(stencil[0].pos.z + stencil[1].pos.z + stencil[2].pos.z + stencil[3].pos.z) * 0.25
				);
			}
			break;
		case FACE:
			double k = 1.0 / valence;
			double x = stencil[0].pos.x;
			double y = stencil[0].pos.y;
			double z = stencil[0].pos.z;
			for (int i = 1; i < stencil.length; i++) {
				x += stencil[i].pos.x;
				y += stencil[i].pos.y;
				z += stencil[i].pos.z;
			}
			position.set(x * k, y * k, z * k);
			break;	
		}
	}
	
	public void setStencil(int type, int valence, Vertex[] stencil) {
		this.stencilType = type;
		this.valence = valence;
		this.stencil = stencil.clone();
	}
	
	public Vertex(double x, double y, double z) {
		this();
		referencePosition.set(x, y, z);
	}
	
	public Vertex(Point3d p) {
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
	
	/**
	 * Computes the inverse transformation matrix and clears
	 * the inverseInvalid flag.
	 */
	private void computeInverseTransform() {
		invTransform.invert(transform);
		inverseInvalid = false;
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
		vertexPoint = new Vertex();
		final int n = valence();
		final int stencilSize = n * 2 + 1;
		final Vertex[] stencil = new Vertex[stencilSize];
		stencil[0] = this;
		int i = 1;
		for (HalfEdge edge : getAdjacentEdges()) {
			stencil[i++] = edge.face.facePoint;
			stencil[i++] = edge.pair.vertex;
		}
		vertexPoint.setStencil(POINT, n, stencil);
		sharpness.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				vertexPoint.sharpness.set(Math.max(0, sharpness.get() - 1));
			}
		});
//		vertexPoint.sharpness.set(Integer.MAX_VALUE);
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
	
	public String toString() {
		if (true) return "Vertex";
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
