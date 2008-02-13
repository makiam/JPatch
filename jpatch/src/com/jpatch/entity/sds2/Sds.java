package com.jpatch.entity.sds2;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;
import com.jpatch.afw.ui.*;
import com.jpatch.entity.*;

import java.util.*;

public class Sds {
	private int currentMaxLevel = 1;
	private IntAttr maxLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(currentMaxLevel, 1, 4);
	private IntAttr renderLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(currentMaxLevel, 1, 4);
	private IntAttr editLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(0, 0, 4);
	
	private final static Comparator<Face> faceMaterialComparator = new Comparator<Face>() {
		public int compare(Face f1, Face f2) {
			int m1 = System.identityHashCode(f1.getMaterial());
			int m2 = System.identityHashCode(f2.getMaterial());
			return (m1 < m2) ? -1 : (m1 > m2) ? 1 : 0;
		}		
	};
	
	private Set<Face>[] levelFaceSets = new Set[SdsConstants.MAX_LEVEL + 1];
	private List<Face>[] levelFaceLists = new List[SdsConstants.MAX_LEVEL + 1];
	private Set<HalfEdge> strayEdges = new HashSet<HalfEdge>();
	private Set<BaseVertex> strayVertices = new HashSet<BaseVertex>();
	
	private boolean facesSorted;
	
	private Map<EdgeKey, HalfEdge> edgeMap = new HashMap<EdgeKey, HalfEdge>();
	
	public Sds(final JPatchUndoManager undoManager) {
		for (int i = 0; i < levelFaceSets.length; i++) {
			levelFaceSets[i] = new HashSet<Face>();
			levelFaceLists[i] = new ArrayList<Face>();
		}
		
		/* add or remove faces on new (old) levels when maxLevel changes */
		maxLevelAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				// add or remove faces on new (old) levels
				List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>();
				setMaxLevel(editList);
				undoManager.addEdit("Change subdivision level", editList);
			}
		});
		
		/* ensures that maxLevel can't be lower than renderLevel */
		maxLevelAttr.addAttributePreChangeListener(new AttributePreChangeAdapter() {
			@Override
			public int attributeWillChange(ScalarAttribute source, int value) {
				return Math.max(renderLevelAttr.getInt(), value);
			}
		});
		
		/* ensures that render-level is lower than max level */
		renderLevelAttr.addAttributePreChangeListener(new AttributePreChangeAdapter() {
			@Override
			public int attributeWillChange(ScalarAttribute source, int value) {
				int level = Math.min(maxLevelAttr.getInt(), value);
				level = Math.max(editLevelAttr.getInt(), level);
				return level;
			}
		});
		
		/* ensures that edit-level is lower than render level */
		editLevelAttr.addAttributePreChangeListener(new AttributePreChangeAdapter() {
			@Override
			public int attributeWillChange(ScalarAttribute source, int value) {
				return Math.min(renderLevelAttr.getInt(), value);
			}
		});
	}
	
	public IntAttr getMaxLevelAttribute() {
		return maxLevelAttr;
	}
	
	public IntAttr getRenderLevelAttribute() {
		return renderLevelAttr;
	}
	
	public IntAttr getEditLevelAttribute() {
		return editLevelAttr;
	}
	
	public void addSegment(List<JPatchUndoableEdit> editList, AbstractVertex vertex0, AbstractVertex vertex1) {
		assert (!edgeMap.containsKey(new EdgeKey(vertex0, vertex1)));
		HalfEdge edge = new HalfEdge(vertex0, vertex1);
		JPatchUndoableEdit addEdgeEdit = new AddEdgeEdit(edge);
		JPatchUndoableEdit addStrayEdgeEdit = new AddStrayEdgeEdit(edge);
		if (editList != null) {
			editList.add(addEdgeEdit);
			editList.add(addStrayEdgeEdit);
		}
	}
	
	public Face addFace(List<JPatchUndoableEdit> editList, int level, Material material, AbstractVertex... vertices) {
		HalfEdge[] edges = new HalfEdge[vertices.length];
		for (AbstractVertex vertex : vertices) {
			vertex.saveEdges(editList);
		}
		for (int i = 0; i < vertices.length; i++) {
			int j = i + 1;
			if (j == vertices.length) {
				j = 0;
			}
			edges[i] = getHalfEdge(editList, vertices[i], vertices[j]);
		}
		Face face = new Face(material, edges);
		if (level == 0) System.out.println("adding face " + face);
		AddFaceEdit addFaceEdit = new AddFaceEdit(level, face);
		if (editList != null) {
			editList.add(addFaceEdit);
		}
		if (level < maxLevelAttr.getInt()) {
			subdivideFace(editList, level, face);
		}
		return face;
	}
	
	public void setMaxLevel(List<JPatchUndoableEdit> editList) { 
		int newLevel = maxLevelAttr.getInt();
		if (newLevel < currentMaxLevel) {
			/* clear references to facepoints on all faces on the new level */
			for (Face face : levelFaceSets[newLevel]) {
				face.disposeFacePoint();
			}
			/* remove all faces at levels higher than the new level */
			for (int level = newLevel + 1; level <= currentMaxLevel; level++) {
				for (Face face : new ArrayList<Face>(levelFaceSets[level])) {
					removeFace(editList, level, face);
				}
			}
			currentMaxLevel = newLevel;
		} else if (newLevel > currentMaxLevel) {
			/* create subdivided faces for each level higher than currentlevel (up to newlevel) */
			for (Face face : levelFaceSets[currentMaxLevel]) {
				subdivideFace(editList, currentMaxLevel, face);
			}
			currentMaxLevel = newLevel;
			if (newLevel > currentMaxLevel) {
				setMaxLevel(editList);
			}
		}
	}
	
	private void subdivideFace(List<JPatchUndoableEdit> editList, int level, Face face) {
		int nextLevel = level + 1;
		HalfEdge[] edges = face.getEdges();
		for (int i = 0; i < edges.length; i++) {
			AbstractVertex v0 = face.getFacePoint() == null ? face.createFacePoint() : face.getFacePoint();
			AbstractVertex v1 = edges[i].getPrev().getEdgePoint() == null ? edges[i].getPrev().createEdgePoint() : edges[i].getPrev().getEdgePoint();
			AbstractVertex v2 = edges[i].getVertex().getVertexPoint() == null ? edges[i].getVertex().createVertexPoint() : edges[i].getVertex().getVertexPoint();
			AbstractVertex v3 = edges[i].getEdgePoint() == null ? edges[i].createEdgePoint() : edges[i].getEdgePoint();
			addFace(editList, nextLevel, face.getMaterial(), v0, v1, v2, v3);
		}
	}
	
	public void setFaceMaterial(Face face, Material material) {
		face.setMaterial(material);
		DerivedVertex facePoint = face.getFacePoint();
		for (HalfEdge edge : facePoint.getEdges()) {
			setFaceMaterial(edge.getFace(), material);
		}
	}
	
	public void removeFace(List<JPatchUndoableEdit> editList, int level, Face face) {
		if (level == 0) System.out.println("removing face " + face);
		assert levelFaceSets[level].contains(face) : "unknown face";
		
		RemoveFaceEdit removeFaceEdit = new RemoveFaceEdit(level, face);
		if (editList != null) {
			editList.add(removeFaceEdit);
		}
		
		for (HalfEdge edge : face.getEdges()) {
			assert edge.getFace() == face : "edge " + edge + " doesn't belong to face " + face;
			edge.saveState(editList);
			edge.getVertex().saveEdges(editList);
			edge.setFace(null);
		}
		
		for (HalfEdge edge : face.getEdges()) {
			if (edge.getPairFace() == null) {
//				System.out.println("removing edge " + edge);
				RemoveEdgeEdit removeEdgeEdit = new RemoveEdgeEdit(edge);
				if (editList != null) {
					editList.add(removeEdgeEdit);
				}
			}
		}
		
		for (HalfEdge edge : face.getEdges()) {
			edge.getVertex().organizeEdges();
		}
		
		DerivedVertex facePoint = face.getFacePoint();
		if (facePoint != null) {
			for (HalfEdge subEdge : facePoint.getEdges().clone()) {
				Face subFace = subEdge.getFace();
				if (subFace != null) {
					removeFace(editList, level + 1, subFace);
				}
			}
		}
		facesSorted = false;
	}
	
	private void sortFaces() {
		if (facesSorted) {
			return;
		}
		for (int i = 0; i < levelFaceSets.length; i++) {
			levelFaceLists[i].clear();
			levelFaceLists[i].addAll(levelFaceSets[i]);
			Collections.sort(levelFaceLists[i], faceMaterialComparator);
		}
		facesSorted = true;
	}
	
	public List<Face> getFaces(int level) {
		sortFaces();
		return levelFaceLists[level];
	}
	
	public Iterable<AbstractVertex> getVertices(final int level) {
		return new Iterable<AbstractVertex>() {
			public Iterator<AbstractVertex> iterator() {
				return new Iterator<AbstractVertex>() {
					Iterator<Face> faces = levelFaceLists[level].iterator();
					HalfEdge[] faceEdges;
					int edgeIndex;
					Iterator<BaseVertex> vertices = strayVertices.iterator();
					
					public boolean hasNext() {
						if (faceEdges != null && edgeIndex < faceEdges.length) {
							return true;
						}
						if (faces.hasNext()) {
							faceEdges = faces.next().getEdges();
							edgeIndex = 0;
							return hasNext();
						}
						return vertices.hasNext();
					}

					public AbstractVertex next() {
						if (faceEdges != null && edgeIndex < faceEdges.length) {
							return faceEdges[edgeIndex++].getVertex();
						}
						if (faces.hasNext()) {
							faceEdges = faces.next().getEdges();
							edgeIndex = 0;
							return next();
						}
						return vertices.next();
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
	public Collection<BaseVertex> getStrayVertices() {
		return strayVertices;
	}
	
	public Iterable<HalfEdge> getStrayEdges() {
		return strayEdges;
	}
	
	public Iterable<HalfEdge> getEdges(final int level) {
		return new Iterable<HalfEdge>() {
			public Iterator<HalfEdge> iterator() {
				return new Iterator<HalfEdge>() {
					Iterator<Face> faces = levelFaceLists[level].iterator();
					HalfEdge[] faceEdges;
					int edgeIndex;
					Iterator<HalfEdge> edges = strayEdges.iterator();
					
					public boolean hasNext() {
						if (faceEdges != null && edgeIndex < faceEdges.length) {
							return true;
						}
						if (faces.hasNext()) {
							faceEdges = faces.next().getEdges();
							edgeIndex = 0;
							return hasNext();
						}
						return edges.hasNext();
					}


					public HalfEdge next() {
						if (faceEdges != null && edgeIndex < faceEdges.length) {
							return faceEdges[edgeIndex++];
						}
						if (faces.hasNext()) {
							faceEdges = faces.next().getEdges();
							edgeIndex = 0;
							return next();
						}
						return edges.next();
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
	public void dumpFaces(int level) {
		for (Face face : levelFaceSets[level]) {
			System.out.println(face);
		}
	}
	
	/**
	 * Checks if the halfEdge vertex0->vertex1 already exists. If it exists and it's face is null,
	 * it is returned, if the face is not null, an IllegalStateException is thrown (non-manifold surface,
	 * each HalfEdge can only belong to one face). If it doesn't exist, a new edge is created, added to the SDS
	 * and returned
	 * @param vertex0
	 * @param vertex1
	 * @return
	 */
	private HalfEdge getHalfEdge(List<JPatchUndoableEdit> editList, AbstractVertex vertex0, AbstractVertex vertex1) {
		/* check if the HalfEdge (v0->v1) already exists */
		HalfEdge edge = edgeMap.get(new EdgeKey(vertex0, vertex1));
		if (edge == null) {
			/* if no edge is found, create a new one and store it in the maps */
			edge = new HalfEdge(vertex0, vertex1);
			JPatchUndoableEdit addEdgeEdit = new AddEdgeEdit(edge);
			if (editList != null) {
				editList.add(addEdgeEdit);
			}
		} else {
			assert edge.getFace() == null : "Surface is non-manifold, edge=" + edge + " face=" + edge.getFace();
			if (editList != null) {
				edge.saveState(editList);
			}
			if (strayEdges.contains(edge)) {
				JPatchUndoableEdit removeStrayEdgeEdit = new RemoveStrayEdgeEdit(edge);
				if (editList != null) {
					editList.add(removeStrayEdgeEdit);
				}
			}
		}
		return edge;
	}
	
	private abstract class EdgeEdit extends AbstractUndoableEdit {
		HalfEdge halfEdge;
		EdgeEdit(HalfEdge halfEdge) {
			this.halfEdge = halfEdge;
			apply(true);
		}
		
		void add() {
			AbstractVertex v0 = halfEdge.getVertex();
			AbstractVertex v1 = halfEdge.getPairVertex();
			EdgeKey key = new EdgeKey(v0, v1);
			EdgeKey pairKey = new EdgeKey(v1, v0);
			if (edgeMap.containsKey(key) || edgeMap.containsKey(pairKey)) {
				throw new IllegalStateException("HalfEdge " + halfEdge + " already in SDS");
			}
			edgeMap.put(key, halfEdge);
			edgeMap.put(pairKey, halfEdge.getPair());
			v0.addEdge(halfEdge);
			v1.addEdge(halfEdge.getPair());
		}
		
		void remove() {
//			if (halfEdge.getFace() != null || halfEdge.getPairFace() != null) {
//				throw new IllegalStateException("Edge " + halfEdge + " still has faces");
//			}
			AbstractVertex v0 = halfEdge.getVertex();
			AbstractVertex v1 = halfEdge.getPairVertex();
			EdgeKey key = new EdgeKey(v0, v1);
			EdgeKey pairKey = new EdgeKey(v1, v0);
			if (!edgeMap.containsKey(key) || !edgeMap.containsKey(pairKey)) {
				throw new IllegalStateException("HalfEdge " + halfEdge + " not in SDS");
			}
			edgeMap.remove(key);
			edgeMap.remove(pairKey);
			v0.removeEdge(halfEdge);
			v1.removeEdge(halfEdge.getPair());
		}
	}
	
	private class AddEdgeEdit extends EdgeEdit {
		private AddEdgeEdit(HalfEdge halfEdge) {
			super(halfEdge);
		}
		
		public void undo() {
			super.undo();
			remove();
		}
		
		public void redo() {
			super.redo();
			add();
		}
	}
	
	private class RemoveEdgeEdit extends EdgeEdit {
		private RemoveEdgeEdit(HalfEdge halfEdge) {
			super(halfEdge);
		}
		
		public void undo() {
			super.undo();
			add();
		}
		
		public void redo() {
			super.redo();
			remove();
		}
	}
	
	private abstract class StrayEdgeEdit extends AbstractUndoableEdit {
		HalfEdge halfEdge;
		StrayEdgeEdit(HalfEdge halfEdge) {
			this.halfEdge = halfEdge;
			apply(true);
		}
		
		void add() {
			strayEdges.add(halfEdge);
			strayEdges.add(halfEdge.getPair());
			strayVertices.add((BaseVertex) halfEdge.getVertex());
			strayVertices.add((BaseVertex) halfEdge.getPairVertex());
		}
		
		void remove() {
			strayEdges.remove(halfEdge);
			strayEdges.remove(halfEdge.getPair());
			strayVertices.remove(halfEdge.getVertex());
			strayVertices.remove(halfEdge.getPairVertex());
		}
	}
	
	private class AddStrayEdgeEdit extends StrayEdgeEdit {
		private AddStrayEdgeEdit(HalfEdge halfEdge) {
			super(halfEdge);
		}
		
		public void undo() {
			super.undo();
			remove();
		}
		
		public void redo() {
			super.redo();
			add();
		}
	}
	
	private class RemoveStrayEdgeEdit extends StrayEdgeEdit {
		private RemoveStrayEdgeEdit(HalfEdge halfEdge) {
			super(halfEdge);
		}
		
		public void undo() {
			super.undo();
			add();
		}
		
		public void redo() {
			super.redo();
			remove();
		}
	}
	
	private abstract class FaceEdit extends AbstractUndoableEdit {
		final int level;
		final Face face;
		FaceEdit(int level, Face face) {
			this.level = level;
			this.face = face;
			apply(true);
		}
		
		void add() {
			assert !levelFaceSets[level].contains(face) : "Face " + face + " has already been added to " + Sds.this + " at level " + level;
			levelFaceSets[level].add(face);
			facesSorted = false;
		}
		
		void remove() {
			assert levelFaceSets[level].contains(face) : "Face " + face + " is unknown to " + Sds.this + " at level " + level;
			levelFaceSets[level].remove(face);
			facesSorted = false;
		}
	}
	
	private class AddFaceEdit extends FaceEdit {
		private AddFaceEdit(int level, Face face) {
			super(level, face);
		}
		
		public void undo() {
			super.undo();
			remove();
		}
		
		public void redo() {
			super.redo();
			add();
		}
	}
	
	private class RemoveFaceEdit extends FaceEdit {
		private RemoveFaceEdit(int level, Face face) {
			super(level, face);
		}
		
		public void undo() {
			super.undo();
			add();
		}
		
		public void redo() {
			super.redo();
			remove();
		}
	}
	
	private static final class EdgeKey {
		private final AbstractVertex v0;
		private final AbstractVertex v1;
		private final int hashCode;
		
		private EdgeKey(AbstractVertex v0, AbstractVertex v1) {
			this.v0 = v0;
			this.v1 = v1;
			hashCode = (System.identityHashCode(v0) << 1) ^ System.identityHashCode(v1);
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object o) {
			EdgeKey ek = (EdgeKey) o;
			return v0 == ek.v0 && v1 == ek.v1;
		}
	}
	
	public HalfEdge getNextStrayEdge(HalfEdge strayEdge) {
		assert strayEdges.contains(strayEdge) : "edge " + strayEdge + " not in " + strayEdges;
		HalfEdge[] vertexEdges = strayEdge.getPairVertex().getEdges();
		if (vertexEdges.length == 2) {
			assert (vertexEdges[0] == strayEdge.getPair() || vertexEdges[1] == strayEdge.getPair());
			return (vertexEdges[0] == strayEdge.getPair()) ? vertexEdges[1] : vertexEdges[0];
		} else {
			assert vertexEdges.length == 1;
			return null;
		}
	}
	
	public HalfEdge getPrevStrayEdge(HalfEdge strayEdge) {
		assert strayEdges.contains(strayEdge);
		HalfEdge[] vertexEdges = strayEdge.getVertex().getEdges();
		if (vertexEdges.length == 2) {
			assert (vertexEdges[0] == strayEdge || vertexEdges[1] == strayEdge);
			return (vertexEdges[0] == strayEdge) ? vertexEdges[1].getPair() : vertexEdges[0].getPair();
		} else {
			assert vertexEdges.length == 1;
			return null;
		}
	}
	
	public BaseVertex[] getLoop(BaseVertex strayVertex) {
		assert strayVertices.contains(strayVertex);
		assert strayVertex.getEdges().length == 1;
		HalfEdge edge = strayVertex.getEdges()[0];
		List<BaseVertex> vertices = new ArrayList<BaseVertex>();
		vertices.add((BaseVertex) edge.getVertex());
		while (edge != null) {
			vertices.add((BaseVertex) edge.getPairVertex());
			edge = getNextStrayEdge(edge);
		}
		return vertices.toArray(new BaseVertex[vertices.size()]);
	}
	
	public boolean isConnected(BaseVertex a, BaseVertex b) {
		assert strayVertices.contains(a);
		assert strayVertices.contains(b);
		assert a.getEdges().length == 1;
		assert b.getEdges().length == 1;
		HalfEdge strayEdge = a.getEdges()[0];
		while (strayEdge != null && strayEdge.getPairVertex() != b) {
			assert strayEdge.getPairVertex() != a;
			strayEdge = getNextStrayEdge(strayEdge);
		}
		if (strayEdge == null) {
			return false;
		} else {
			assert strayEdge.getPairVertex() == b;
			return true;
		}
	}
	
	public boolean isStartOfChain(BaseVertex strayVertex) {
		assert strayVertices.contains(strayVertex);
		assert strayVertex.getEdges().length == 1 || strayVertex.getEdges().length == 2;
		return strayVertex.getEdges().length == 1;
	}
}
