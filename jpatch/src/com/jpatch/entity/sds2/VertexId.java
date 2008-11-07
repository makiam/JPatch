package com.jpatch.entity.sds2;

public abstract class VertexId implements Comparable<VertexId>{
	abstract AbstractVertex getVertex();
	
	static class BaseVertexId extends VertexId {
		private final BaseVertex baseVertex;
		BaseVertexId(BaseVertex vertex) {
			this.baseVertex = vertex;
		}
		@Override
		public String toString() {
			return Integer.toString(baseVertex.num);
		}
		@Override
		BaseVertex getVertex() {
			return baseVertex;
		}
		public int compareTo(VertexId other) {
			BaseVertexId o = (BaseVertexId) other;
			return baseVertex.num < o.baseVertex.num ? -1 : baseVertex.num > o.baseVertex.num ? 1 : 0;
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
		AbstractVertex getVertex() {
			return parentVertexId.getVertex().getVertexPoint();
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
		AbstractVertex getVertex() {
			AbstractVertex v0 = parentVertexId0.getVertex();
			AbstractVertex v1 = parentVertexId1.getVertex();
			for (HalfEdge edge : v0.getEdges()) {
				if (edge.getPairVertex() == v1) {
					return edge.getEdgePoint();
				}
			}
			throw new AssertionError("should never get here");
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
		private final VertexId parentVertexId0;
		private final VertexId parentVertexId1;
		FacePointId(VertexId parentVertexId0, VertexId parentVertexId1) {
			this.parentVertexId0 = parentVertexId0;
			this.parentVertexId1 = parentVertexId1;
		}
		@Override
		public String toString() {
			return "f(" + parentVertexId0 + "," + parentVertexId1 + ")";
		}
		@Override
		AbstractVertex getVertex() {
			AbstractVertex v0 = parentVertexId0.getVertex();
			AbstractVertex v1 = parentVertexId1.getVertex();
			for (HalfEdge edge : v0.getEdges()) {
				if (edge.getPairVertex() == v1) {
					return edge.getFace().getFacePoint();
				}
			}
			throw new AssertionError("should never get here");
		}
		public int compareTo(VertexId other) {
			if (other instanceof FacePointId) {
				int c = parentVertexId0.compareTo(((FacePointId) other).parentVertexId0);
				if (c == 0) {
					return parentVertexId1.compareTo(((FacePointId) other).parentVertexId1);
				} else {
					return c;
				}
			} else {
				return 1;
			}
		}
	}
}
