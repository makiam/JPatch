package com.jpatch.entity.sds2;

import com.jpatch.afw.*;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;
import com.jpatch.afw.ui.*;
import com.jpatch.entity.*;

import java.util.*;

public class Sds {
	public static final class Type {
		public static final int VERTEX = 1 << 0;
		public static final int LIMIT = 1 << 1;
		public static final int EDGE = 1 << 2;
		public static final int FACE = 1 << 3;
		public static final int STRAY_VERTEX = 1 << 4;
		public static final int STRAY_EDGE = 1 << 5;
		public static final int BOUNDARY_EDGE = 1 << 6;
		
		private Type() {
			assert false;	// not instanciable
		}
	}
	
	
	private int currentMaxLevel = 1;
	private IntAttr maxLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(currentMaxLevel, 0, SdsConstants.MAX_LEVEL);
	private IntAttr renderLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(currentMaxLevel, 0, SdsConstants.MAX_LEVEL);
	private IntAttr editLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(0, 0, SdsConstants.MAX_LEVEL);
	
	private final static Comparator<Face> faceMaterialComparator = new Comparator<Face>() {
		public int compare(Face f1, Face f2) {
			int m1 = System.identityHashCode(f1.getMaterial());
			int m2 = System.identityHashCode(f2.getMaterial());
			return (m1 < m2) ? -1 : (m1 > m2) ? 1 : 0;
		}		
	};
	
	private final Map<Integer, Face> faceIdMap = new HashMap<Integer, Face>();
	private final Set<Face>[] faceSets = new Set[SdsConstants.MAX_LEVEL + 1];
	private final List<Face>[] faceLists = new List[SdsConstants.MAX_LEVEL + 1];
	private final Set<HalfEdge> strayEdges = new HashSet<HalfEdge>();
	private final Set<BaseVertex> strayVertices = new HashSet<BaseVertex>();
	private final Set<BaseVertex[]> strayFaces = new HashSet<BaseVertex[]>();
	private final Map<EdgeKey, HalfEdge>[] edgeMaps = new Map[SdsConstants.MAX_LEVEL + 1];
	
	private boolean facesSorted;
	
	/** used as key to find entries in edgeMaps without creating a new key object every time */
	private final EdgeKey edgeKey = new EdgeKey();
	
	public Sds(final JPatchUndoManager undoManager) {
		for (int i = 0; i < faceSets.length; i++) {
			faceSets[i] = new HashSet<Face>();
			faceLists[i] = new ArrayList<Face>();
			edgeMaps[i] = new HashMap<EdgeKey, HalfEdge>();
		}
		
		/* add or remove faces on new (old) levels when maxLevel changes */
		maxLevelAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				// add or remove faces on new (old) levels
				List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>();
				setMaxLevel(editList);
				if (undoManager != null) {
					undoManager.addEdit("Change subdivision level", editList);
				}
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
		edgeKey.set(vertex0, vertex1);
		assert (!edgeMaps[0].containsKey(edgeKey));
		HalfEdge edge = new HalfEdge(vertex0, vertex1);
		JPatchUndoableEdit addEdgeEdit = new AddEdgeEdit(edge, 0);
		JPatchUndoableEdit addStrayEdgeEdit = new AddStrayEdgeEdit(edge);
		if (editList != null) {
			editList.add(addEdgeEdit);
			editList.add(addStrayEdgeEdit);
		}
	}
	
	public void removeSegment(List<JPatchUndoableEdit> editList, HalfEdge strayEdge) {
		assert (strayEdges.contains(strayEdge));
		assert (strayVertices.contains(strayEdge.getVertex()));
		assert (strayVertices.contains(strayEdge.getPairVertex()));
		JPatchUndoableEdit removeStrayEdgeEdge = new RemoveStrayEdgeEdit(strayEdge);
		JPatchUndoableEdit removeEdgeEdit = new RemoveEdgeEdit(strayEdge, 0);
		if (editList != null) {
			editList.add(removeStrayEdgeEdge);
			editList.add(removeEdgeEdit);
		}
	}
	
	public void addStrayFace(List<JPatchUndoableEdit> editList, BaseVertex[] vertices) {
		AddStrayFaceEdit edit = new AddStrayFaceEdit(vertices);
		if (editList != null) {
			editList.add(edit);
		}
	}
	
	public void removeStrayEdge(List<JPatchUndoableEdit> editList, HalfEdge strayEdge) {
		RemoveStrayEdgeEdit edit = new RemoveStrayEdgeEdit(strayEdge);
		if (editList != null) {
			editList.add(edit);
		}
	}
	
	public void removeStrayFace(List<JPatchUndoableEdit> editList, BaseVertex[] vertices) {
		RemoveStrayFaceEdit edit = new RemoveStrayFaceEdit(vertices);
		if (editList != null) {
			editList.add(edit);
		}
	}
	
	public Face addFace(List<JPatchUndoableEdit> editList, int level, Material material, AbstractVertex... vertices) {
		return addFace(editList, level, null, -1, material, vertices);
	}
	
	private Face addFace(List<JPatchUndoableEdit> editList, int level, Face parent, int edgeIndex, Material material, AbstractVertex... invertices) {
		AbstractVertex[] vertices = invertices.clone();
		HalfEdge[] edges = new HalfEdge[vertices.length];

		System.out.print("add face called: ");
		for (AbstractVertex vertex : vertices) {
			System.out.print(vertex + " ");
		}
		System.out.println();
		
		for (int i = 0; i < vertices.length; i++) {
			int j = i + 1;
			if (j == vertices.length) {
				j = 0;
			}
			edges[i] = getHalfEdge(editList, vertices[i], vertices[j], level);
		}
		for (AbstractVertex vertex : vertices) {
//			System.out.println("  saving edges for " + vertex);
			vertex.saveEdges(editList);
		}
		
		Face face;
		if (parent == null) {
			face = new Face(material, edges);
		} else {
			face = new Face(material, edges, parent, edgeIndex);
		}
//		if (level == 0) System.out.println(this + " adding face " + face);
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
			for (Face face : faceSets[newLevel]) {
				face.disposeFacePoint();
			}
			/* remove all faces at levels higher than the new level */
			for (int level = newLevel + 1; level <= currentMaxLevel; level++) {
				for (Face face : new ArrayList<Face>(faceSets[level])) {
					removeFace(editList, level, face);
				}
			}
			currentMaxLevel = newLevel;
		} else if (newLevel > currentMaxLevel) {
			/* create subdivided faces for each level higher than currentlevel (up to newlevel) */
			for (Face face : faceSets[currentMaxLevel]) {
				subdivideFace(editList, currentMaxLevel, face);
			}
			currentMaxLevel = newLevel;
			if (newLevel > currentMaxLevel) {
				setMaxLevel(editList);
			}
		}
	}
	
	public void flipFaces(List<JPatchUndoableEdit> editList, Collection<Face> faces) {
		FlipFacesEdit edit = new FlipFacesEdit(faces);
		if (editList != null) {
			editList.add(edit);
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
			addFace(editList, nextLevel, face, i, face.getMaterial(), v0, v1, v2, v3);
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
//		if (level == 0) System.out.println("removing face " + face);
		assert faceSets[level].contains(face) : "unknown face";
		
		RemoveFaceEdit removeFaceEdit = new RemoveFaceEdit(level, face);
		if (editList != null) {
			editList.add(removeFaceEdit);
		}
		
		for (HalfEdge edge : face.getEdges()) {
			assert edge.getFace() == face : "edge " + edge + " doesn't belong to face " + face;
			if (editList != null) {
				edge.saveState(editList);
				edge.getVertex().saveEdges(editList);
			}
			edge.setFace(null);
		}
		
		for (HalfEdge edge : face.getEdges()) {
			if (edge.getPairFace() == null) {
//				System.out.println("removing edge " + edge);
				RemoveEdgeEdit removeEdgeEdit = new RemoveEdgeEdit(edge, level);
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
		for (int i = 0; i < faceSets.length; i++) {
			faceLists[i].clear();
			faceLists[i].addAll(faceSets[i]);
			Collections.sort(faceLists[i], faceMaterialComparator);
		}
		facesSorted = true;
	}
	
	public DerivedVertex locateVertex(int[] hierarchyId) {
//		System.out.println("hierarchyId = " + Arrays.toString(hierarchyId));
		Face face = faceIdMap.get(hierarchyId[hierarchyId.length - 1]);
		if (face == null) {
			throw new IllegalArgumentException("level-0 face with id " + hierarchyId[0] + " does not exist");
		}
//		System.out.println("base face found: " + face);
		HalfEdge[] subEdges = new HalfEdge[2];
		for (int i = hierarchyId.length - 2; i > 0; i--) {
			face.getSubEdges(hierarchyId[i], subEdges);
			face = subEdges[0].getFace();
//			System.out.println("descending to " + face);
		}
		return (DerivedVertex) face.getEdges()[hierarchyId[0]].getVertex();
	}
	
	public List<Face> getFaces(int level) {
		sortFaces();
		return faceLists[level];
	}
	
	@SuppressWarnings("unchecked")
	public Iterable<? extends AbstractVertex> getVertices(final int level, final boolean includeStrayVertices) {
		if (includeStrayVertices) {
			return new CombinedIterable(getFaceVertices(level), strayVertices);
		}
		return getFaceVertices(level);
	}
	
	public Iterable<? extends AbstractVertex> getFaceVertices(final int level) {
		return new Iterable<AbstractVertex>() {
			public Iterator<AbstractVertex> iterator() {
				if (faceLists[level].size() == 0) {
					return new ArrayList<AbstractVertex>(0).iterator();
				}
				return new Iterator<AbstractVertex>() {
					Iterator<Face> faces = faceLists[level].iterator();
					Face face = faces.next();
					HalfEdge[] faceEdges = face.getEdges();
					int edgeIndex;
					AbstractVertex nextVertex = getNextVertex();
					
					private final AbstractVertex getNextVertex() {
						if (edgeIndex == faceEdges.length) {
							if (faces.hasNext()) {
								face = faces.next();
								faceEdges = face.getEdges();
								edgeIndex = 0;
							} else {
								return null;
							}
						}
						HalfEdge edge = faceEdges[edgeIndex++];
						AbstractVertex vertex = edge.getVertex();
						if (vertex.getPrimaryFace() != face) {
							return getNextVertex();
						}
						return vertex;
					}
					
					
					public boolean hasNext() {
						return nextVertex != null;
					}

					public AbstractVertex next() {
						AbstractVertex tmp = nextVertex;
						nextVertex = getNextVertex();
						return tmp;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
//	public Iterable<? extends AbstractVertex> getVerticesOld(final int level, final boolean includeStrayVertices) {
//		sortFaces();
//		return new Iterable<AbstractVertex>() {
//			public Iterator<AbstractVertex> iterator() {
//				return new Iterator<AbstractVertex>() {
//					Iterator<Face> faces = faceLists[level].iterator();
//					HalfEdge[] faceEdges;
//					int edgeIndex;
//					Iterator<BaseVertex> vertices = strayVertices.iterator();
//					
//					public boolean hasNext() {
//						if (faceEdges != null && edgeIndex < faceEdges.length) {
//							return true;
//						}
//						if (faces.hasNext()) {
//							faceEdges = faces.next().getEdges();
//							edgeIndex = 0;
//							return hasNext();
//						}
//						if (includeStrayVertices) {
//							return vertices.hasNext();
//						}
//						return false;
//					}
//
//					public AbstractVertex next() {
//						if (faceEdges != null && edgeIndex < faceEdges.length) {
//							return faceEdges[edgeIndex++].getVertex();
//						}
//						if (faces.hasNext()) {
//							faceEdges = faces.next().getEdges();
//							edgeIndex = 0;
//							return next();
//						}
//						if (includeStrayVertices) {
//							return vertices.next();
//						}
//						throw new NoSuchElementException();
//					}
//
//					public void remove() {
//						throw new UnsupportedOperationException();
//					}
//				};
//			}
//		};
//	}
	
	public Collection<BaseVertex> getStrayVertices() {
		return strayVertices;
	}
	
	public Iterable<HalfEdge> getStrayEdges() {
		return strayEdges;
	}
	
	public Collection<BaseVertex[]> getStrayFaces() {
		return strayFaces;
	}
	
	public Iterable<HalfEdge> getEdges(final int level, final boolean includeStrayEdges) {
		return new Iterable<HalfEdge>() {
			public Iterator<HalfEdge> iterator() {
				return new Iterator<HalfEdge>() {
					Iterator<Face> faces = faceLists[level].iterator();
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
						if (includeStrayEdges) {
							return edges.hasNext();
						}
						return false;
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
						if (includeStrayEdges) {
							return edges.next();
						}
						throw new NoSuchElementException();
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
	public void dumpFaces(int level) {
		for (Face face : faceSets[level]) {
			System.out.print(face + " ");
			for (HalfEdge edge : face.getEdges()) {
				System.out.print(edge + " ");
			}
			System.out.println();
		}
		for (int l = level; l <= 1; l++) {
			for (AbstractVertex vertex : getVertices(l, true)) {
				System.out.print(vertex + " ");
				for (HalfEdge edge : vertex.getEdges()) {
					System.out.print(edge + " ");
				}
				System.out.println();
			}
		}
		for (HalfEdge edge : getEdges(0, true)) {
			System.out.print(edge + " ");
			System.out.print("next=" + edge.getNext() + " ");
			System.out.print("prev=" + edge.getPrev() + " ");
			System.out.print("face=" + edge.getFace() + " ");
			System.out.print("pairFace=" + edge.getPairFace() + " ");
			System.out.println();
		}
		
		System.out.print("stray edges: ");
		for (HalfEdge edge : getStrayEdges()) {
			System.out.print(edge + " ");
		}
		System.out.println();
		
		System.out.print("stray vertices: ");
		for (AbstractVertex vertex : getStrayVertices()) {
			System.out.print(vertex + " ");
		}
		System.out.println();
		
		System.out.print("stray faces: ");
		for (AbstractVertex[] face : getStrayFaces()) {
			System.out.print("{ ");
			for (AbstractVertex vertex : face) {
				System.out.print(vertex + " ");
			}
			System.out.print("} ");
		}
		System.out.println();
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
	private HalfEdge getHalfEdge(List<JPatchUndoableEdit> editList, AbstractVertex vertex0, AbstractVertex vertex1, int level) {
		assert vertex0 != vertex1 : "Vertices are identical: " + vertex0;
		/* check if the HalfEdge (v0->v1) already exists */
		edgeKey.set(vertex0, vertex1);
		HalfEdge edge = edgeMaps[level].get(edgeKey);
		if (edge == null) {
//			System.out.println("create new edge " + vertex0 + "-" + vertex1);
			/* if no edge is found, create a new one and store it in the maps */
			edge = new HalfEdge(vertex0, vertex1);
			JPatchUndoableEdit addEdgeEdit = new AddEdgeEdit(edge, level);
			if (editList != null) {
				editList.add(addEdgeEdit);
			}
		} else {
//			System.out.println("found edge " + edge);
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
	
	private static class FlipFacesEdit extends AbstractSwapEdit {
		final Face[] faces;
		
		FlipFacesEdit(Collection<Face> faces) {
			this.faces = faces.toArray(new Face[faces.size()]);
			swap();
			applied = true;
		}
		
		@Override
		protected void swap() {
			Set<HalfEdge> edgesToFlip = new HashSet<HalfEdge>();
			Set<HalfEdge> uniqueEdges = new HashSet<HalfEdge>();
			Set<AbstractVertex> verticesToFlip = new HashSet<AbstractVertex>();
			for (Face face : faces) {
				face.flip(edgesToFlip, verticesToFlip);
			}
			for (HalfEdge edge : edgesToFlip) {
				if (!uniqueEdges.contains(edge.getPair())) {
					uniqueEdges.add(edge);
				}
			}
			for (HalfEdge edge : uniqueEdges) {
				edge.flip();
			}
			for (AbstractVertex vertex : verticesToFlip) {
				vertex.flip();
			}
		}
		
	}
	
	private abstract class EdgeEdit extends AbstractUndoableEdit {
		final HalfEdge halfEdge;
		final Map<EdgeKey, HalfEdge> edgeMap;
		
		EdgeEdit(HalfEdge halfEdge, int level) {
			this.halfEdge = halfEdge;
			edgeMap = edgeMaps[level];
			apply(true);
		}
		
		void add() {
			AbstractVertex v0 = halfEdge.getVertex();
			AbstractVertex v1 = halfEdge.getPairVertex();
			edgeKey.set(v0, v1);
			assert !edgeMap.containsKey(edgeKey) : "HalfEdge " + halfEdge + " already in SDS";
			edgeMap.put(edgeKey.clone(), halfEdge);
			edgeKey.set(v1, v0);
			assert !edgeMap.containsKey(edgeKey) : "HalfEdge " + halfEdge + " already in SDS";
			edgeMap.put(edgeKey.clone(), halfEdge.getPair());
			v0.addEdge(halfEdge);
			v1.addEdge(halfEdge.getPair());
		}
		
		void remove() {
			AbstractVertex v0 = halfEdge.getVertex();
			AbstractVertex v1 = halfEdge.getPairVertex();
			edgeKey.set(v0, v1);
			assert edgeMap.containsKey(edgeKey) : "HalfEdge " + halfEdge + " noz in SDS";
			edgeMap.remove(edgeKey);
			edgeKey.set(v1, v0);
			assert edgeMap.containsKey(edgeKey) : "HalfEdge " + halfEdge + " noz in SDS";
			edgeMap.remove(edgeKey);
			v0.removeEdge(halfEdge);
			v1.removeEdge(halfEdge.getPair());
		}
	}
	
	private class AddEdgeEdit extends EdgeEdit {
		private AddEdgeEdit(HalfEdge halfEdge, int level) {
			super(halfEdge, level);
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
		private RemoveEdgeEdit(HalfEdge halfEdge, int level) {
			super(halfEdge, level);
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
//			halfEdge.getVertex().removeEdge(halfEdge);
			for (AbstractVertex v : halfEdge.getVertices(new AbstractVertex[2])) {
				boolean removeStrayVertex = true;
				for (HalfEdge e : v.getEdges()) {
					if (!e.isStray()) {
						removeStrayVertex = false;
						continue;
					}
				}
				if (removeStrayVertex) {
					strayVertices.remove(v);
				}
			}
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
	
	private abstract class StrayFaceEdit extends AbstractUndoableEdit {
		BaseVertex[] vertices;
		StrayFaceEdit(BaseVertex[] vertices) {
			this.vertices = vertices;
			apply(true);
		}
		
		void add() {
			strayFaces.add(vertices);
		}
		
		void remove() {
			strayFaces.remove(vertices);
		}
	}
	
	private class AddStrayFaceEdit extends StrayFaceEdit {
		private AddStrayFaceEdit(BaseVertex[] vertices) {
			super(vertices);
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
	
	private class RemoveStrayFaceEdit extends StrayFaceEdit {
		private RemoveStrayFaceEdit(BaseVertex[] vertices) {
			super(vertices);
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
			assert !faceSets[level].contains(face) : "Face " + face + " has already been added to " + Sds.this + " at level " + level;
			faceSets[level].add(face);
			if (level == 0) {
				faceIdMap.put(face.id, face);
			}
			facesSorted = false;
		}
		
		void remove() {
			assert faceSets[level].contains(face) : "Face " + face + " is unknown to " + Sds.this + " at level " + level;
			faceSets[level].remove(face);
			if (level == 0) {
				faceIdMap.remove(face.id);
			}
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
	
	private static final class EdgeKey implements Cloneable {
		private AbstractVertex v0;
		private AbstractVertex v1;
		private int hashCode;
		
		private EdgeKey() {
			;
		}
		
		private void set(AbstractVertex v0, AbstractVertex v1) {
			this.v0 = v0;
			this.v1 = v1;
			hashCode = (System.identityHashCode(v0) << 1) ^ System.identityHashCode(v1);
		}
		
		@Override
		public EdgeKey clone() {
			try {
				return (EdgeKey) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
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
	
	public HalfEdge getStart(HalfEdge strayEdge) {
		assert strayEdges.contains(strayEdge);
		HalfEdge startEdge = strayEdge;
		HalfEdge prevEdge = getPrevStrayEdge(strayEdge);
		while (prevEdge != null && prevEdge != strayEdge) {
			HalfEdge tmp = startEdge;
			startEdge = prevEdge;
			prevEdge = getPrevStrayEdge(tmp);
		}
		return startEdge;
	}
	
	public BaseVertex[] getChain(BaseVertex strayVertex) {
		assert strayVertices.contains(strayVertex);
//		assert strayVertex.getEdges().length == 1;
		HalfEdge edge = strayVertex.getEdges()[0];
		List<BaseVertex> vertices = new ArrayList<BaseVertex>();
		BaseVertex startVertex = (BaseVertex) edge.getVertex();
		vertices.add(startVertex);
		while (edge != null) {
			BaseVertex v = (BaseVertex) edge.getPairVertex();
			vertices.add(v);
			if (v == startVertex) {
				break;
			}
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
