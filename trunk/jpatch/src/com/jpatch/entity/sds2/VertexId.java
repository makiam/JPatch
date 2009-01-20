package com.jpatch.entity.sds2;

public abstract class VertexId implements Comparable<VertexId>{
	
	static class BaseVertexId extends VertexId {
		public final BaseVertex vertex;
		
		BaseVertexId(BaseVertex vertex) {
			this.vertex = vertex;
		}
		
		@Override
		public String toString() {
			return "v" + Integer.toString(((BaseVertex) vertex).num);
		}
		
		public int compareTo(VertexId other) {
			int n = ((BaseVertex) vertex).num;
			int on = ((BaseVertex) ((BaseVertexId) other).vertex).num;
			return n < on ? -1 : n > on ? 1 : 0;
		}
	}
	
	static class VertexPointId extends VertexId {
		public final AbstractVertex parentVertex;
		public final VertexId parentVertexId;
		
		VertexPointId(AbstractVertex parentVertex) {
			this.parentVertex = parentVertex;
			this.parentVertexId = parentVertex.vertexId;
		}
		
		@Override
		public String toString() {
			return "v(" + parentVertexId + ")";
		}
		
		public int compareTo(VertexId other) {
			if (other instanceof VertexPointId) {
				return parentVertexId.compareTo(((VertexPointId) other).parentVertexId);
			} else {
				return -1;
			}
		}
	}
	
	static class EdgePointId extends VertexId {
		public final HalfEdge halfEdge;
		public final VertexId parentVertexId0;
		public final VertexId parentVertexId1;
		
		EdgePointId(HalfEdge halfEdge) {
			final VertexId id0 = halfEdge.getVertex().vertexId;
			final VertexId id1 = halfEdge.getPairVertex().vertexId;
			if (id0.compareTo(id1) < 0) {
				this.parentVertexId0 = id0;
				this.parentVertexId1 = id1;
				this.halfEdge = halfEdge;
			} else {
				this.parentVertexId0 = id1;
				this.parentVertexId1 = id0;
				this.halfEdge = halfEdge.getPair();
			}	
		}
		
		@Override
		public String toString() {
			return "e(" + parentVertexId0 + "," + parentVertexId1 + ")";
		}
		
		
		public int compareTo(VertexId other) {
			if (other instanceof EdgePointId) {
				int c = parentVertexId0.compareTo(((EdgePointId) other).parentVertexId0);
				if (c == 0) {
					return parentVertexId1.compareTo(((EdgePointId) other).parentVertexId1);
				} else {
					return c;
				}
			} else if (other instanceof VertexPointId) {
				return 1;
			} else if (other instanceof FacePointId) {
				return -1;
			}
			throw new AssertionError("should never get here");
		}
	}
	
	static class FacePointId extends VertexId {
		public final Face face;
		public final VertexId[] parentVertexIds;
		
		FacePointId(Face face) {
			this.face = face;
			this.parentVertexIds = new VertexId[face.getSides()];
			for (int i = 0; i < face.getSides(); i++) {
				parentVertexIds[i] = face.getEdges()[i].getVertex().vertexId;
			}
		}
		
		@Override
		public String toString() {
			return "f(" + face.toString() + ")";
//			final StringBuilder sb = new StringBuilder();
//			sb.append("f(");
//			for (int i = 0 ; i < parentVertexIds.length; i++) {
//				sb.append(parentVertexIds[i]);
//				if (i < parentVertexIds.length - 1) {
//					sb.append(',');
//				}
//			}
//			sb.append(')');
//			return sb.toString();
		}
		
		public int compareTo(VertexId other) {
			if (other instanceof FacePointId) {
				int c = parentVertexIds[0].compareTo(((FacePointId) other).parentVertexIds[1]);
				if (c == 0) {
					return parentVertexIds[0].compareTo(((FacePointId) other).parentVertexIds[1]);
				} else {
					return c;
				}
			} else {
				return 1;
			}
		}
	}
}
