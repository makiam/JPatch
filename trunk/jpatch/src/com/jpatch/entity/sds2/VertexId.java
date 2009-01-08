package com.jpatch.entity.sds2;

public abstract class VertexId implements Comparable<VertexId>{
	AbstractVertex vertex;
	
	abstract void createVertex();
	
	
	final AbstractVertex getOrCreateVertex() {
		if (vertex == null) {
			createVertex();
		}
		return vertex;
	}
	
	static class BaseVertexId extends VertexId {
		BaseVertexId(BaseVertex vertex) {
			this.vertex = vertex;
		}
		
		@Override
		public String toString() {
			return Integer.toString(((BaseVertex) vertex).num);
		}
		
		@Override
		void createVertex() {
			throw new UnsupportedOperationException();
		}
		
		public int compareTo(VertexId other) {
			int n = ((BaseVertex) vertex).num;
			int on = ((BaseVertex) other.vertex).num;
			return n < on ? -1 : n > on ? 1 : 0;
		}
	}
	
	static class VertexPointId extends VertexId {
		
		private final VertexId parentVertexId;
		
		VertexPointId(VertexId parentVertexId) {
			this.parentVertexId = parentVertexId;
		}
		
		@Override
		public String toString() {
			return "v(" + parentVertexId + ")";
		}
		
		@Override
		void createVertex() {
			vertex = parentVertexId.getOrCreateVertex().getOrCreateVertexPoint();
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
		
		private final VertexId parentVertexId0;
		private final VertexId parentVertexId1;
		
		EdgePointId(VertexId parentVertexId0, VertexId parentVertexId1) {
			if (parentVertexId0.compareTo(parentVertexId1) < 0) {
				this.parentVertexId0 = parentVertexId0;
				this.parentVertexId1 = parentVertexId1;
			} else {
				this.parentVertexId0 = parentVertexId1;
				this.parentVertexId1 = parentVertexId0;
			}
		}
		
		@Override
		public String toString() {
			return "e(" + parentVertexId0 + "," + parentVertexId1 + ")";
		}
		
		@Override
		void createVertex() {
			final AbstractVertex v0 = parentVertexId0.getOrCreateVertex();
			final AbstractVertex v1 = parentVertexId1.getOrCreateVertex();
			final HalfEdge edge = HalfEdge.getOrCreate(v0, v1);
			vertex = edge.getEdgePoint();
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
		
		private final VertexId[] parentVertexIds;
		
		FacePointId(VertexId ... parentVertexIds) {
			this.parentVertexIds = parentVertexIds.clone();
		}
		
		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("f(");
			for (int i = 0 ; i < parentVertexIds.length; i++) {
				sb.append(parentVertexIds[i]);
				if (i < parentVertexIds.length - 1) {
					sb.append(',');
				}
			}
			sb.append(')');
			return sb.toString();
		}
		
		@Override
		void createVertex() {
			final AbstractVertex[] vertices = new AbstractVertex[parentVertexIds.length];
			for (int i = 0; i < parentVertexIds.length; i++) {
				vertices[i] = parentVertexIds[i].getOrCreateVertex();
			}
			final Sds sds = vertices[0].sds;
			final Face face = sds.getOrCreateFace(vertices);
			vertex = face.getOrCreateFacePoint();
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
