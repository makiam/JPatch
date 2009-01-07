package com.jpatch.entity.sds2;

import com.jpatch.afw.*;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;
import com.jpatch.afw.testing.*;
import com.jpatch.afw.ui.*;
import com.jpatch.boundary.*;
import com.jpatch.entity.*;

import java.util.*;

import javax.vecmath.*;

import org.junit.*;
import static org.junit.Assert.*;

public class Sds {
	public static enum Type { VERTEX, LIMIT, EDGE, FACE, STRAY_VERTEX, STRAY_EDGE, BOUNDARY_EDGE };

	//	public static final class Type {
	//		public static final int VERTEX = 1 << 0;
	//		public static final int LIMIT = 1 << 1;
	//		public static final int EDGE = 1 << 2;
	//		public static final int FACE = 1 << 3;
	//		public static final int STRAY_VERTEX = 1 << 4;
	//		public static final int STRAY_EDGE = 1 << 5;
	//		public static final int BOUNDARY_EDGE = 1 << 6;
	//		
	//		private Type() {
	//			assert false;	// not instanciable
	//		}
	//	}


	private int currentMinLevel = 0;
	private IntAttr minLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(currentMinLevel, 2, SdsConstants.MAX_LEVEL);
	//	private IntAttr renderLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(currentMaxLevel, 0, SdsConstants.MAX_LEVEL);
	private IntAttr editLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(0, 0, SdsConstants.MAX_LEVEL);

	private final static Comparator<Face> faceMaterialComparator = new Comparator<Face>() {
		public int compare(Face f1, Face f2) {
			int m1 = System.identityHashCode(f1.getMaterial());
			int m2 = System.identityHashCode(f2.getMaterial());
			return (m1 < m2) ? -1 : (m1 > m2) ? 1 : 0;
		}		
	};


	private final FaceSet[] faceSets = new FaceSet[SdsConstants.MAX_LEVEL + 1];
	@SuppressWarnings("unchecked")
	private final Iterable<HalfEdge>[] faceEdges = (Iterable<HalfEdge>[]) new Iterable[SdsConstants.MAX_LEVEL + 1];
	private final Iterable<? extends AbstractVertex>[] faceVertices = (Iterable<? extends AbstractVertex>[]) new Iterable[SdsConstants.MAX_LEVEL + 1];

	private final Set<HalfEdge> strayEdges = new HashSet<HalfEdge>();
	private final Collection<BaseVertex> strayVertices = createStrayVertexCollection();

	private final Set<BaseVertex[]> strayFaces = new HashSet<BaseVertex[]>();
	@SuppressWarnings("unchecked")
	private final Set<Displacement>[] hierarchy = new Set[SdsConstants.MAX_LEVEL + 1];



	private final MorphController morphController = new MorphController(this);
	private final NdeLayerManager ndeLayerManager = new NdeLayerManager(morphController);

	private Material newFaceMaterial = new BasicMaterial(new Color3f(1, 1, 1));



	public Sds(final JPatchUndoManager undoManager) {

		for (int i = 0; i < faceSets.length; i++) {
			faceSets[i] = new FaceSet();
			faceEdges[i] = createFaceEdgesIterable(i);
			faceVertices[i] = createFaceVerticesIterable(i);
			hierarchy[i] = new HashSet<Displacement>();
		}

		/* add or remove faces on new (old) levels when maxLevel changes */
		minLevelAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				// add or remove faces on new (old) levels
				setMinLevel();
			}
		});

		//		/* ensures that maxLevel can't be lower than renderLevel */
		//		minLevelAttr.addAttributePreChangeListener(new AttributePreChangeAdapter<Object>() {
		//			@Override
		//			public int attributeWillChange(ScalarAttribute source, int value) {
		//				return Math.max(renderLevelAttr.getInt(), value);
		//			}
		//		});

		//		/* ensures that render-level is lower than max level */
		//		renderLevelAttr.addAttributePreChangeListener(new AttributePreChangeAdapter<Object>() {
		//			@Override
		//			public int attributeWillChange(ScalarAttribute source, int value) {
		//				int level = Math.min(minLevelAttr.getInt(), value);
		//				level = Math.max(editLevelAttr.getInt(), level);
		//				return level;
		//			}
		//		});

		//		/* ensures that edit-level is lower than render level */
		//		editLevelAttr.addAttributePreChangeListener(new AttributePreChangeAdapter<Object>() {
		//			@Override
		//			public int attributeWillChange(ScalarAttribute source, int value) {
		//				return Math.min(renderLevelAttr.getInt(), value);
		//			}
		//		});
	}

	public IntAttr getMinLevelAttribute() {
		return minLevelAttr;
	}

	//	public IntAttr getRenderLevelAttribute() {
	//		return renderLevelAttr;
	//	}

	public IntAttr getEditLevelAttribute() {
		return editLevelAttr;
	}

	public NdeLayerManager getNdeLayerManager() {
		return ndeLayerManager;
	}
	//	
	public MorphTarget getActiveMorphTarget() {
		return morphController.getActiveMorphTarget();
	}

	public MorphController getMorphController() {
		return morphController;
	}

	public Iterable<BaseVertex[]> getStrayFaces() {
		return strayFaces;
	}

	public Collection<BaseVertex> getStrayVertices() {
		return strayVertices;
	}
	//	
	//	
	//	public void setActiveMorphTarget(MorphTarget morphTarget) {
	//		this.activeMorphTarget = morphTarget;
	//	}

	public void addSegment(List<JPatchUndoableEdit> editList, AbstractVertex vertex0, AbstractVertex vertex1) {
		//		edgeKey.set(vertex0, vertex1);
		//		assert (!edgeMap.containsKey(edgeKey));
		//		HalfEdge edge = new HalfEdge(vertex0, vertex1);
		//		JPatchUndoableEdit addEdgeEdit = new AddEdgeEdit(edge, 0);
		JPatchUndoableEdit addStrayEdgeEdit = new Edits.StrayEdgeEdit.Add(this, vertex0, vertex1);
		if (editList != null) {
			//			editList.add(addEdgeEdit);
			editList.add(addStrayEdgeEdit);
		}
	}

	public void removeSegment(List<JPatchUndoableEdit> editList, HalfEdge strayEdge) {
		//assert (strayEdges.contains(strayEdge));
		JPatchUndoableEdit removeStrayEdgeEdge = new Edits.StrayEdgeEdit.Remove(this, strayEdge);
		//		JPatchUndoableEdit removeEdgeEdit = new RemoveEdgeEdit(strayEdge, 0);
		if (editList != null) {
			editList.add(removeStrayEdgeEdge);
			//			editList.add(removeEdgeEdit);
		}
	}

	public Iterable<HalfEdge> getStrayEdges() {
		return strayEdges;
	}

	public Displacement createHierarchyModification(int[] path) {
		int level = path.length - 2;
		Displacement hierarchicalVertexModification = new Displacement(path);
		hierarchy[level].add(hierarchicalVertexModification);
		return hierarchicalVertexModification;
	}

	public void discardHierarchicalVertexModification(Displacement hierarchicalVertexModification) {
		int level = hierarchicalVertexModification.hierarchyPath.length - 2;
		assert hierarchy[level].contains(hierarchicalVertexModification);
		hierarchy[level].remove(hierarchicalVertexModification);
	}

	public void addStrayFace(List<JPatchUndoableEdit> editList, BaseVertex[] vertices) {
		JPatchUndoableEdit edit = new Edits.StrayFaceEdit.Add(this, vertices);
		if (editList != null) {
			editList.add(edit);
		}
	}

	public void removeStrayFace(List<JPatchUndoableEdit> editList, BaseVertex[] vertices) {
		JPatchUndoableEdit edit = new Edits.StrayFaceEdit.Remove(this, vertices);
		if (editList != null) {
			editList.add(edit);
		}
	}


	//	private Face createFace(int level, Face parent, int edgeIndex, Material material, HalfEdge... edges) {
	//		assert level > 0;
	//		assert parent != null;
	////		System.out.print("add face called: ");
	////		for (AbstractVertex vertex : vertices) {
	////			System.out.print(vertex + " ");
	////		}
	////		System.out.println();
	//		
	////		for (int i = 0; i < vertices.length; i++) {
	////			int j = i + 1;
	////			if (j == vertices.length) {
	////				j = 0;
	////			}
	////			edges[i] = getHalfEdge(vertices[i], vertices[j]);
	////		}
	//		
	//		Face face = new Face(material, edges, parent, edgeIndex);
	//		
	////		if (level == 0) System.out.println(this + " adding face " + face);
	//		
	//		assert !faceSets[level].contains(face) : "Face " + face + " has already been added to " + Sds.this + " at level " + level;
	//		faceSets[level].add(face);
	//		
	//		
	//		if (level < minLevelAttr.getInt()) {
	//			subdivideFace(level, face);

	//	private Face createSubFace(int level, int edgeIndex, Material material, HalfEdge... edges) {
	//		assert level > 0;
	////		assert parent != null;
	////		System.out.print("add face called: ");
	////		for (AbstractVertex vertex : vertices) {
	////			System.out.print(vertex + " ");
	//
	////		}
	//
	////		return face;
	////	}
	//
	////		System.out.println();
	//		
	////		for (int i = 0; i < vertices.length; i++) {
	////			int j = i + 1;
	////			if (j == vertices.length) {
	////				j = 0;
	////			}
	////			edges[i] = getHalfEdge(vertices[i], vertices[j]);
	////		}
	//		
	//		Face face = new Face(material, edges, edgeIndex);
	//		
	////		if (level == 0) System.out.println(this + " adding face " + face);
	//		
	//		assert !faceSets[level].contains(face) : "Face " + face + " has already been added to " + Sds.this + " at level " + level;
	//		faceSets[level].add(face);
	//		
	//		
	//		if (level < maxLevelAttr.getInt()) {
	//			subdivideFace(level, face, false);
	//		}
	//		return face;
	//	}


	//	public Face addFace(List<JPatchUndoableEdit> editList, Material material, AbstractVertex... invertices) {
	//		AbstractVertex[] vertices = invertices.clone();
	//		HalfEdge[] edges = new HalfEdge[vertices.length];
	//
	////		System.out.print("add face called: ");
	////		for (AbstractVertex vertex : vertices) {
	////			System.out.print(vertex + " ");
	////		}
	////		System.out.println();
	//		
	//		for (int i = 0; i < vertices.length; i++) {
	//			int j = i + 1;
	//			if (j == vertices.length) {
	//				j = 0;
	//			}
	//			edges[i] = getHalfEdge(editList, vertices[i], vertices[j]);
	//		}
	//		for (AbstractVertex vertex : vertices) {
	////			System.out.println("  saving edges for " + vertex);
	//			vertex.saveEdges(editList);
	//		}
	//		
	//		Face face;
	//		
	//		face = new Face(material, edges);
	//		
	////		if (level == 0) System.out.println(this + " adding face " + face);
	//		AddFaceEdit addFaceEdit = new AddFaceEdit(0, face);
	//		if (editList != null) {
	//			editList.add(addFaceEdit);
	//		}
	//		if (0 < maxLevelAttr.getInt()) {
	//			subdivideFace(0, face);
	//		}
	//		return face;
	//	}

	public void setMinLevel() {
		System.gc();
		int newLevel = minLevelAttr.getInt();
		System.out.println("currentMinLevel = " + currentMinLevel + ", newLevel = " + newLevel);
		//		if (newLevel < currentMaxLevel) {
		//			/* clear references to facepoints on all faces on the new level */
		//			for (Face face : faceSets[newLevel]) {
		//				face.disposeFacePoint();
		//				for (HalfEdge edge : face.getEdges()) {
		//					edge.disposeEdgePoint();
		//					edge.getVertex().disposeVertexPoint();
		//				}
		//			}
		//			/* remove all faces at levels higher than the new level */
		//			for (int level = newLevel + 1; level <= currentMaxLevel; level++) {
		//				for (Face face : new ArrayList<Face>(faceSets[level])) {
		//					discardFace(level, face);
		//				}
		//			}
		//		} else 
		if (newLevel > currentMinLevel) {
			/* create subdivided faces for each level higher than currentlevel (up to newlevel) */
			//			int n = faceSets[currentMaxLevel].size();
			//			int i = 0, p = 0;

			for (Face face : faceSets[currentMinLevel]) {
				subdivideFace(currentMinLevel, face, Face.SubdivStatus.AUTO_SUBDIVIDED);

				//				int np = (i++) * 100 / n;
				//				if (np > p) {
				//					p = np;
				//					System.out.println(i + " (" + p + "%) " + Runtime.getRuntime().maxMemory() + "," + Runtime.getRuntime().totalMemory() + "," + Runtime.getRuntime().freeMemory());
				//				}
			}
		}
		currentMinLevel = newLevel;
		System.gc();
	}

	public void flipFaces(List<JPatchUndoableEdit> editList, Collection<Face> faces) {
		JPatchUndoableEdit edit = new Edits.FlipFacesEdit(faces);
		if (editList != null) {
			editList.add(edit);
		}
	}

	public void subdivideFace(final int level, final Face face, final Face.SubdivStatus subdivStatus) {
		System.out.println("subdivideFace(" + level + ", " + face + ", " + subdivStatus + ") called");

		assert subdivStatus != Face.SubdivStatus.NOT_SUBDIVIDED;

		if (face.getSubdivStatus() == subdivStatus) {
			return;
		}

		final Material material = face.getMaterial();

		if (face.getSubdivStatus() == Face.SubdivStatus.NOT_SUBDIVIDED) {

			final HalfEdge[] edges = face.getEdges();
			final HalfEdge[] hubEdges = new HalfEdge[face.getSides()];
			final HalfEdge[] newEdges = new HalfEdge[4];
			final DerivedVertex[] edgePoints = new DerivedVertex[face.getSides()];
			final DerivedVertex[] vertexPoints = new DerivedVertex[face.getSides()];

			final AbstractVertex facePoint = face.getOrCreateFacePoint();
			for (int i = 0; i < edges.length; i++) {
				vertexPoints[i] = edges[i].getVertex().getOrCreateVertexPoint();
				edgePoints[i] = edges[i].getOrCreateEdgePoint();
				hubEdges[i] = HalfEdge.getOrCreate(facePoint, edgePoints[i]);
			}

			for (int i = 0; i < edges.length; i++) {
				int j = (i == edges.length - 1) ? 0 : i + 1;
				newEdges[0] = hubEdges[i];
				//				newEdges[1] = edges[i].getPair().getSubEdge().getPair();
				newEdges[1] = HalfEdge.getOrCreate(edgePoints[i], vertexPoints[j]);
				//				newEdges[2] = edges[j].getSubEdge();
				newEdges[2] = HalfEdge.getOrCreate(vertexPoints[j], edgePoints[j]);
				newEdges[3] = hubEdges[j].getPair();

				final Face subFace = faceSets[level + 1].getFace(newEdges);


				//				createFace(level + 1, face, i, face.getMaterial(), newEdges);

				//				AbstractVertex v1 = edges[i].getPrev().getEdgePoint();
				//				AbstractVertex v2 = edges[i].getVertex().getVertexPoint();
				//				AbstractVertex v3 = edges[i].getEdgePoint();
				//				createFace(level + 1, face, i, face.getMaterial(), v0, v1, v2, v3);
				if (subdivStatus == Face.SubdivStatus.BOUNDARY) {
					faceSets[level + 1].setMaterial(subFace, null);
				} else {
					faceSets[level + 1].setMaterial(subFace, material);
					if (level + 1 < minLevelAttr.getInt()) {
						subdivideFace(level + 1, subFace, subdivStatus);
					}
				}
			}
			face.setSubdivStatus(subdivStatus);
		} else {
			/* promote subdivSatus if higher */
			if (face.getSubdivStatus().compareTo(subdivStatus) < 0) {
				face.setSubdivStatus(subdivStatus);
				//				if (subdivStatus != Face.SubdivStatus.BOUNDARY) {
				for (HalfEdge edge : face.getFacePoint().getEdges()) {
					final Face subFace = edge.getFace();
					if (subFace.getMaterial() == null) {
						faceSets[level + 1].setMaterial(subFace, material);
					}
				}
				//				}
			}
		}


		/* subdivide surrounding faces */
		if (subdivStatus != Face.SubdivStatus.BOUNDARY) {
			for (HalfEdge edge : face.getEdges()) {
				for (HalfEdge corner : edge.getVertex().getEdges()) {
					Face f = corner.getFace();
					if (f != null && f != face) {
						subdivideFace(level, f, Face.SubdivStatus.BOUNDARY);
					}
				}
			}
		}
	}

	//	public void setFaceMaterial(Face face, Material material) {
	//		face.setMaterial(material);
	//		DerivedVertex facePoint = face.getFacePoint();
	//		for (HalfEdge edge : facePoint.getEdges()) {
	//			setFaceMaterial(edge.getFace(), material);
	//		}
	//	}

	public Face addFace(List<JPatchUndoableEdit> editList, Material material, AbstractVertex... vertices) {
		Edits.FaceEdit.Add edit = new Edits.FaceEdit.Add(this, material, vertices);
		if (editList != null) {
			editList.add(edit);
		}
		return edit.face;
	}

	public void removeFace(List<JPatchUndoableEdit> editList, Face face) {
		JPatchUndoableEdit edit = new Edits.FaceEdit.Remove(this, face);
		if (editList != null) {
			editList.add(edit);
		}
	}


	/**
	 * Creates a face (and, if necessary, its edges)
	 */
	public Face getOrCreateFace(AbstractVertex... vertices) {
		System.out.println("createFace(" + Arrays.toString(vertices) + ") called");
		HalfEdge[] edges = new HalfEdge[vertices.length];

		final int last = vertices.length - 1;
		for (int i = 0; i < last; ) {
			edges[i] = HalfEdge.getOrCreate(vertices[i], vertices[++i]);
		}
		edges[last] = HalfEdge.getOrCreate(vertices[last], vertices[0]);

		Face face = faceSets[0].getFace(edges);

		if (0 < minLevelAttr.getInt()) {
			subdivideFace(0, face, Face.SubdivStatus.AUTO_SUBDIVIDED);
		}
		return face;
	}

	/**
	 * Discards a face. Sets face-fields of all edges to null and removes
	 * orphaned edges. Also calls discardFace on all sub-faces of the
	 * specified face.
	 * This method is intended to be called from FaceSet.removeFace()
	 * @param discardEdges must be true when called from FaceSet, false when called recursively
	 */
	private void discardFace(Face face, boolean discardEdges) {
		/* clear face-field on all edges, dispose edges if necessary */
		for (HalfEdge edge: face.getEdges()) {
			assert edge.getFace() == face;
			edge.clearFace();
			//			if (discardEdges && edge.getPairFace() == null) {
			//				edgeSet.removeHalfEdge(edge); // TODO: need to discard edge?
			//			}
		}

		/* discard sub-faces of specified face */
		DerivedVertex facePoint = face.getFacePoint();
		if (facePoint != null) {
			for (HalfEdge edge : facePoint.getEdges().clone()) {
				discardFace(edge.getFace(), false);
			}
		}
	}

	//	public void removeFace(List<JPatchUndoableEdit> editList, int level, Face face) {
	////		if (level == 0) System.out.println("removing face " + face);
	//		assert faceSets[level].contains(face) : "unknown face";
	//		
	//		RemoveFaceEdit removeFaceEdit = new RemoveFaceEdit(level, face);
	//		if (editList != null) {
	//			editList.add(removeFaceEdit);
	//		}
	//		
	//		for (HalfEdge edge : face.getEdges()) {
	//			assert edge.getFace() == face : "edge " + edge + " doesn't belong to face " + face;
	//			if (editList != null) {
	//				edge.saveState(editList);
	//				edge.getVertex().saveEdges(editList);
	//			}
	//			edge.setFace(null);
	//		}
	//		
	//		for (HalfEdge edge : face.getEdges()) {
	//			if (edge.getPairFace() == null) {
	////				System.out.println("removing edge " + edge);
	//				RemoveEdgeEdit removeEdgeEdit = new RemoveEdgeEdit(edge, level);
	//				if (editList != null) {
	//					editList.add(removeEdgeEdit);
	//				}
	//			}
	//		}
	//		
	//		for (HalfEdge edge : face.getEdges()) {
	//			edge.getVertex().organizeEdges();
	//		}
	//		
	//		DerivedVertex facePoint = face.getFacePoint();
	//		if (facePoint != null) {
	//			for (HalfEdge subEdge : facePoint.getEdges().clone()) {
	//				Face subFace = subEdge.getFace();
	//				if (subFace != null) {
	//					removeFace(editList, level + 1, subFace);
	//				}
	//			}
	//		}
	//		facesSorted = false;
	//	}


	//	public DerivedVertex locateVertex(int[] hierarchyId) {
	////		System.out.println("hierarchyId = " + Arrays.toString(hierarchyId));
	//		Face face = faceIdMap.get(hierarchyId[hierarchyId.length - 1]);
	//		if (face == null) {
	//			throw new IllegalArgumentException("level-0 face with id " + hierarchyId[0] + " does not exist");
	//		}
	////		System.out.println("base face found: " + face);
	//		HalfEdge[] subEdges = new HalfEdge[2];
	//		for (int i = hierarchyId.length - 2; i > 0; i--) {
	//			face.getSubEdges(hierarchyId[i], subEdges);
	//			face = subEdges[0].getFace();
	////			System.out.println("descending to " + face);
	//		}
	//		return (DerivedVertex) face.getEdges()[hierarchyId[0]].getVertex();
	//	}

	public Iterable<Face> getFaces(int level) {
		return faceSets[level];
	}

	@SuppressWarnings("unchecked")
	public Iterable<? extends AbstractVertex> getVertices(final int level) {
		return faceVertices[level];
	}

	public Iterable<? extends AbstractVertex> getAllVertices() {
		return new CompositIterable(faceVertices[0], strayVertices);
	}


	@SuppressWarnings("unchecked")
	public Iterable<HalfEdge> getEdges(final int level) {
		return faceEdges[level];
	}

	public Iterable<HalfEdge> getAllEdges() {
		return new CompositIterable<HalfEdge>(faceEdges[0], strayEdges);
	}

	private Iterable<? extends AbstractVertex> createFaceVerticesIterable(final int level) {
		return new Iterable<AbstractVertex>() {
			public Iterator<AbstractVertex> iterator() {
				if (faceSets[level].size() == 0) {
					return new Iterator<AbstractVertex>() {
						public boolean hasNext() {
							return false;
						}
						public AbstractVertex next() {
							throw new NoSuchElementException();
						}
						public void remove() {
							throw new UnsupportedOperationException();
						}		
					};
				}
				return new Iterator<AbstractVertex>() {
					Iterator<Face> faces = faceSets[level].iterator();
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
						if (nextVertex == null) {
							throw new NoSuchElementException();
						}
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

	private Collection<BaseVertex> createStrayVertexCollection() {
		return new AbstractCollection<BaseVertex>() {
			public Iterator<BaseVertex> iterator() {
				return new Iterator<BaseVertex>() {
					private Iterator<HalfEdge> strayEdgeIterator = strayEdges.iterator();
					private HalfEdge strayEdge;
					private boolean first = false;
					private AbstractVertex vertex = getNext();

					public boolean hasNext() {
						return vertex != null;
					}

					public BaseVertex next() {
						AbstractVertex tmp = vertex;
						vertex = getNext();
						return (BaseVertex) tmp;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}

					private AbstractVertex getNext() {
						if (!first) {
							if (strayEdgeIterator.hasNext()) {
								strayEdge = strayEdgeIterator.next();
								if (isStartOfChain(strayEdge.getVertex())) {
									first = true;
									return strayEdge.getVertex();
								}
							} else {
								return null;
							}
						}
						first = false;
						return strayEdge.getPairVertex();
					}
				};
			}

			@Override
			public int size() {
				int i = 0;
				for (Iterator it = iterator(); it.hasNext(); ) {
					i++;
				}
				return i;
			}
		};
	}
	//	
	//	
	//	private Iterable<HalfEdge> strayEdges = new Iterable<HalfEdge>() {
	//		public Iterator<HalfEdge> iterator() {
	//			return new Iterator<HalfEdge>() {
	//				Iterator<HalfEdge> edges = edgeSet.iterator();
	//				HalfEdge edge = getNext();
	//				
	//				public boolean hasNext() {
	//					return edge != null;
	//				}
	//
	//				public HalfEdge next() {
	//					if (edge == null) {
	//						throw new NoSuchElementException();
	//					}
	//					HalfEdge tmp = edge;
	//					edge = getNext();
	//					return tmp;
	//				}
	//
	//				public void remove() {
	//					throw new UnsupportedOperationException();
	//				}
	//				
	//				private HalfEdge getNext() {
	//					if (edges.hasNext()) {
	//						edge = edges.next();
	//						if (edge.isStray()) {
	//							return edge;
	//						}
	//						return getNext();
	//					}
	//					return null;
	//				}
	//			};
	//		}	
	//	};


	private Iterable<HalfEdge> createFaceEdgesIterable(final int level) {
		return new Iterable<HalfEdge>() {
			public Iterator<HalfEdge> iterator() {
				return new Iterator<HalfEdge>() {
					Iterator<Face> faces = faceSets[level].iterator();
					HalfEdge[] faceEdges = new HalfEdge[0];
					int edgeIndex;
					HalfEdge next = getNext();

					public boolean hasNext() {
						return next != null;
					}


					public HalfEdge next() {
						if (next == null) {
							throw new NoSuchElementException();
						}
						HalfEdge tmp = next;
						next = getNext();
						return tmp;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}

					private HalfEdge getNext() {
						if (edgeIndex < faceEdges.length) {
							next = faceEdges[edgeIndex++];
							if (next.isPrimary()) {
								return next;
							} else {
								if (next.getPairFace() == null || !next.getPairFace().isDrawable()) {
									return next.getPair();
								} else {
									return getNext();
								}
							}
						} else {
							if (faces.hasNext()) {
								faceEdges = faces.next().getEdges();
								edgeIndex = 0;
								return getNext();
							} else {
								return null;
							}
						}
					}
				};
			}
		};
	}

	public void dumpFaces(int level) {
		System.out.println("FACES");
		for (Face face : faceSets[level]) {
			System.out.print(face + " ");
			for (HalfEdge edge : face.getEdges()) {
				System.out.print(edge + " ");
			}
			System.out.println();
		}
		System.out.println("VERTICES");
		for (int l = level; l <= 0; l++) {
			for (AbstractVertex vertex : getVertices(l)) {
				System.out.print(vertex + " ");
				for (HalfEdge edge : vertex.getEdges()) {
					System.out.print(edge + " ");
				}
				System.out.println();
			}
		}
		System.out.println("EDGES");
		for (HalfEdge edge : getEdges(0)) {
			System.out.print(edge + " ");
			System.out.print("next=" + edge.getNext() + " ");
			System.out.print("prev=" + edge.getPrev() + " ");
			System.out.print("face=" + edge.getFace() + " ");
			System.out.print("pairFace=" + edge.getPairFace() + " ");
			System.out.println();
		}

		//		System.out.print("stray edges: ");
		//		for (HalfEdge edge : getStrayEdges()) {
		//			System.out.print(edge + " ");
		//		}
		//		System.out.println();
		//		
		//		System.out.print("stray vertices: ");
		//		for (AbstractVertex vertex : getStrayVertices()) {
		//			System.out.print(vertex + " ");
		//		}
		//		System.out.println();
		//		
		//		System.out.print("stray faces: ");
		//		for (AbstractVertex[] face : getStrayFaces()) {
		//			System.out.print("{ ");
		//			for (AbstractVertex vertex : face) {
		//				System.out.print(vertex + " ");
		//			}
		//			System.out.print("} ");
		//		}
		//		System.out.println();

		//		System.out.println("running compare-test");
		//		int p = 0;
		//		List<VertexId> idList = new ArrayList<VertexId>();
		//		for (AbstractVertex a : getVertices(currentMinLevel, false)) {
		//			System.out.print(".");
		//			if (p++ == 100) {
		//				System.out.println();
		//				p = 0;
		//			}
		//			idList.add(a.vertexId);
		//			for (AbstractVertex b : getVertices(currentMinLevel, false)) {
		//				if (a.vertexId.compareTo(b.vertexId) == 0 && a != b) {
		//					System.out.println("error");
		//				}
		//			}
		//		}
		//		
		//		System.out.println("sorting...");
		//		Collections.sort(idList);
		//		VertexId last = idList.get(idList.size() - 1);
		//		for (VertexId id : idList) {
		//			System.out.println(id + " " + id.compareTo(last));
		//			last = id;
		//			if (id.getVertex().vertexId != id) {
		//				System.out.println("****ERROR****");
		//			}
		//		}
		//		System.out.println();
		//		System.out.println("edgeset:");
		//		edgeSet.dump();
	}

	//	/**
	//	 * Checks if the halfEdge vertex0->vertex1 already exists. If it exists and it's face is null,
	//	 * it is returned, if the face is not null, an IllegalStateException is thrown (non-manifold surface,
	//	 * each HalfEdge can only belong to one face). If it doesn't exist, a new edge is created, added to the SDS
	//	 * and returned
	//	 * @param vertex0
	//	 * @param vertex1
	//	 * @return
	//	 */
	//	private HalfEdge getHalfEdge(List<JPatchUndoableEdit> editList, AbstractVertex vertex0, AbstractVertex vertex1) {
	//		assert vertex0 != vertex1 : "Vertices are identical: " + vertex0;
	//		/* check if the HalfEdge (v0->v1) already exists */
	//		edgeKey.set(vertex0, vertex1);
	//		HalfEdge edge = edgeMaps[0].get(edgeKey);
	//		if (edge == null) {
	////			System.out.println("create new edge " + vertex0 + "-" + vertex1);
	//			/* if no edge is found, create a new one and store it in the maps */
	//			edge = new HalfEdge(vertex0, vertex1);
	//			JPatchUndoableEdit addEdgeEdit = new AddEdgeEdit(edge, 0);
	//			if (editList != null) {
	//				editList.add(addEdgeEdit);
	//			}
	//		} else {
	////			System.out.println("found edge " + edge);
	//			assert edge.getFace() == null : "Surface is non-manifold, edge=" + edge + " face=" + edge.getFace();
	//			if (editList != null) {
	//				edge.saveState(editList);
	//			}
	//			if (strayEdges.contains(edge)) {
	//				JPatchUndoableEdit removeStrayEdgeEdit = new RemoveStrayEdgeEdit(edge);
	//				if (editList != null) {
	//					editList.add(removeStrayEdgeEdit);
	//				}
	//			}
	//		}
	//		return edge;
	//	}

	//	/**
	//	 * Checks if the halfEdge vertex0->vertex1 already exists. If it exists and it's face is null,
	//	 * it is returned, if the face is not null, an IllegalStateException is thrown (non-manifold surface,
	//	 * each HalfEdge can only belong to one face). If it doesn't exist, a new edge is created, added to the SDS
	//	 * and returned
	//	 * @param vertex0
	//	 * @param vertex1
	//	 * @return
	//	 */
	//	private HalfEdge getHalfEdge(AbstractVertex vertex0, AbstractVertex vertex1) {
	//		assert vertex0 != vertex1 : "Vertices are identical: " + vertex0;
	//		/* check if the HalfEdge (v0->v1) already exists */
	////		edgeKey.set(vertex0, vertex1);
	////		HalfEdge edge = edgeMap.get(edgeKey);
	////		if (edge == null) {
	//////			System.out.println("create new edge " + vertex0 + "-" + vertex1);
	////			/* if no edge is found, create a new one and store it in the maps */
	////			edge = new HalfEdge(vertex0, vertex1);
	////			
	////			assert !edgeMap.containsKey(edgeKey) : "HalfEdge " + edge + " already in SDS";
	////			edgeMap.put(edgeKey.clone(), edge);
	////			edgeKey.swap();
	////			edgeMap.put(edgeKey.clone(), edge.getPair());
	////		}
	////		return edge;
	//		return edgeSet.getHalfEdge(vertex0, vertex1);
	//	}

	private static abstract class Edits {

		private static abstract class FaceEdit extends AbstractUndoableEdit {
			final Sds sds;
			Material material;
			AbstractVertex[] vertices;
			Face face;

			FaceEdit(Sds sds) {
				this.sds = sds;
			}

			void add() {
				face = sds.getOrCreateFace(vertices);
				sds.faceSets[0].setMaterial(face, material);
			}

			void remove() {
				assert sds.faceSets[0].contains(face) : "Face " + face + " is unknown to " + sds;
				sds.discardFace(face, true);
			}

			private static class Add extends FaceEdit {
				private Add(Sds sds, Material material, AbstractVertex[] vertices) {
					super(sds);
					this.vertices = vertices.clone();
					this.material = material;
					apply(true);
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

			private static class Remove extends FaceEdit {
				private Remove(Sds sds, Face face) {
					super(sds);
					this.face = face;
					vertices = new AbstractVertex[face.getSides()];
					for (int i = 0; i < vertices.length; i++) {
						vertices[i] = face.getEdges()[i].getVertex();
					}
					material = face.getMaterial();
					apply(true);
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

		private static abstract class StrayEdgeEdit extends AbstractUndoableEdit {
			final Sds sds;
			final AbstractVertex v0;
			final AbstractVertex v1;
			StrayEdgeEdit(Sds sds, AbstractVertex v0, AbstractVertex v1) {
				this.sds = sds;
				this.v0 = v0;
				this.v1 = v1;
				apply(true);
			}

			void add() {
				final HalfEdge halfEdge = HalfEdge.getOrCreate(v0, v1).getPrimary();
				sds.strayEdges.add(halfEdge);
			}

			void remove() {
				final HalfEdge halfEdge = HalfEdge.getOrCreate(v0, v1);
				sds.strayEdges.remove(halfEdge);
				((BaseVertex) halfEdge.getVertex()).removeEdge(halfEdge);
				((BaseVertex) halfEdge.getPairVertex()).removeEdge(halfEdge.getPair());
			}


			private static class Add extends StrayEdgeEdit {
				private Add(Sds sds, AbstractVertex v0, AbstractVertex v1) {
					super(sds, v0, v1);
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

			private static class Remove extends StrayEdgeEdit {
				private Remove(Sds sds, HalfEdge halfEdge) {
					super(sds, halfEdge.getVertex(), halfEdge.getPairVertex());
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
		}

		private static abstract class StrayFaceEdit extends AbstractUndoableEdit {
			final Sds sds;
			BaseVertex[] vertices;
			StrayFaceEdit(Sds sds, BaseVertex[] vertices) {
				this.sds = sds;
				this.vertices = vertices.clone();
				apply(true);
			}

			void add() {
				sds.strayFaces.add(vertices);
			}

			void remove() {
				sds.strayFaces.remove(vertices);
			}

			private static class Add extends StrayFaceEdit {
				private Add(Sds sds, BaseVertex[] vertices) {
					super(sds, vertices);
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

			private static class Remove extends StrayFaceEdit {
				private Remove(Sds sds, BaseVertex[] vertices) {
					super(sds, vertices);
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
		}

	}

	public HalfEdge getNextStrayEdge(HalfEdge strayEdge) {
		assert strayEdges.contains(strayEdge.getPrimary()) : "edge " + strayEdge + " not in " + strayEdges;
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
		assert strayEdges.contains(strayEdge.getPrimary());
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
		assert strayEdges.contains(strayEdge.getPrimary());
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
		//assert strayVertices.contains(strayVertex);
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

	public boolean isStartOfChain(AbstractVertex strayVertex) {
		assert strayVertex.getEdges().length == 1 || strayVertex.getEdges().length == 2;
		return strayVertex.getEdges().length == 1;
	}



	//	/**
	//	 * A special purpose hashtable that associates vertex-pairs with edges.
	//	 * It is used to find existing edges for given vertex-pairs.
	//	 * @author sascha
	//	 */
	//	public static class EdgeSet implements Iterable<HalfEdge> {
	//		final static double growFactor = 0.75;
	//		final static double shrinkFactor = 0.25;
	//		final static int MIN = 256;
	//		int size;
	//		int capacity;
	//		int mask;
	//		int count;
	//		int max;
	//		int min;
	//		int version = 0;
	//		private HalfEdge[][] buckets;
	//		
	//		private EdgeSet() {
	//			setSize(8);	//initial capacity = 256
	//			buckets = new HalfEdge[capacity][];
	//		}
	//		
	//		/**
	//		 * compute hashcode for vertex pairs.
	//		 * Hashcode for v0,v1 is identical to hashcode of v1,v0
	//		 */
	//		private int hash(final AbstractVertex v0, final AbstractVertex v1) {
	//			return (v0.hashCode() ^ v1.hashCode()) & mask;
	//		}
	//		
	//		/**
	//		 * Returns a HalfEdge that connects the specified vertices v0 and v1.
	//		 * It that HalfEdge already exists, it is returned (this is also true if only
	//		 * it's pair has been created yet). If not, a new HalfEdge is stored in the
	//		 * hashTable and returned.
	//		 * @param v0 the first vertex
	//		 * @param v1 the second vertex
	//		 * @return the HalfEdge connecting the specified vertices
	//		 */
	//		public HalfEdge getHalfEdge(final AbstractVertex v0, final AbstractVertex v1) {
	//			final int index = hash(v0, v1);
	//			HalfEdge[] bucket = buckets[index];
	//			/* check if the bucket is non-empty */
	//			if (bucket != null) {
	//				/* scan bucket sequentially for the halfEdge or its pair*/
	//				for (HalfEdge halfEdge : bucket) {
	//					if (halfEdge.getVertex() == v0 && halfEdge.getPairVertex() == v1) {
	//						return halfEdge;
	//					} else if (halfEdge.getVertex() == v1 && halfEdge.getPairVertex() == v0) {
	//						return halfEdge.getPair();
	//					}
	//				}
	//			}
	//			/* create a new halfedge and add it to the bucket */
	//			HalfEdge halfEdge = new HalfEdge(v0, v1);
	//			add(index, halfEdge);
	//			return halfEdge;
	//		}
	//		
	//		/**
	//		 * Removes the specified HalfEdge (or its pair) from the hashTable.
	//		 * @param halfEdge the HalfEdge to remove
	//		 */
	//		public void removeHalfEdge(final HalfEdge halfEdge) {
	//			remove(hash(halfEdge.getVertex(), halfEdge.getPairVertex()), halfEdge);
	//		}
	//		
	//		public Iterator<HalfEdge> iterator() {
	//			return new Iterator<HalfEdge>() {
	//				private final int snapshot = version;
	//				private int bucketIndex = 0;
	//				private HalfEdge[] bucket = null;
	//				private int index = 0;
	//				private HalfEdge next = getNext();
	//				
	//				public boolean hasNext() {
	//					if (snapshot != version) {
	//						throw new ConcurrentModificationException();
	//					}
	//					return next != null;
	//				}
	//
	//				public HalfEdge next() {
	//					if (snapshot != version) {
	//						throw new ConcurrentModificationException();
	//					}
	//					if (next == null) {
	//						throw new NoSuchElementException();
	//					}
	//					HalfEdge tmp = next;
	//					next = getNext();
	//					return tmp;
	//				}
	//
	//				public void remove() {
	//					throw new UnsupportedOperationException();
	//				}
	//				
	//				private HalfEdge getNext() {
	//					while(bucket == null) {
	//						bucketIndex++;
	//						if (bucketIndex == buckets.length) {
	//							return null;
	//						}
	//						bucket = buckets[bucketIndex];
	//						index = 0;
	//					}
	//					if (index < bucket.length) {
	//						return bucket[index++].getPrimary();
	//					} else {
	//						bucket = null;
	//						return getNext();
	//					}
	//				}
	//			};
	//		}
	//		
	//		/**
	//		 * Adds a halfEdge to a bucket
	//		 */
	//		private void add(final int bucketIndex, final HalfEdge halfEdge) {
	//			if (buckets[bucketIndex] == null) {
	//				/* empty bucket, create new one */
	//				buckets[bucketIndex] = new HalfEdge[] { halfEdge };
	//			} else {
	//				/* non empty bucket, grow by one element and copy all elements */
	//				HalfEdge[] tmp = buckets[bucketIndex];
	//				buckets[bucketIndex] = new HalfEdge[tmp.length + 1];
	//				System.arraycopy(tmp, 0, buckets[bucketIndex], 0, tmp.length);
	//				buckets[bucketIndex][tmp.length] = halfEdge;
	//			}
	//			/* increase count, rehash if necessary */
	//			count++;
	//			if (count > max) {
	//				setSize(size + 1);
	//				rehash();
	//			}
	//			version++;
	//		}
	//		
	//		/**
	//		 * Removes a halfEdge from a bucket
	//		 */
	//		private void remove(final int bucketIndex, final HalfEdge halfEdge) {
	//			HalfEdge[] tmp = buckets[bucketIndex];
	//			if (tmp.length == 1) {
	//				/* length was 1, bucket is empty now */
	//				buckets[bucketIndex] = null;
	//			} else {
	//				/* shrink bucket by 1*/
	//				buckets[bucketIndex] = new HalfEdge[tmp.length - 1];
	//				/* scan for element to discard */
	//				int i = 0;
	//				final HalfEdge pair = halfEdge.getPair();System.out.println("running compare-test");
	//	int p = 0;
	//	List<VertexId> idList = new ArrayList<VertexId>();
	//	for (AbstractVertex a : getVertices(currentMinLevel, false)) {
	//		System.out.print(".");
	//		if (p++ == 100) {
	//			System.out.println();
	//			p = 0;
	//		}
	//		idList.add(a.vertexId);
	//		for (AbstractVertex b : getVertices(currentMinLevel, false)) {
	//			if (a.vertexId.compareTo(b.vertexId) == 0 && a != b) {
	//				System.out.println("error");
	//			}
	//		}
	//	}
	//	
	//	System.out.println("sorting...");
	//	Collections.sort(idList);
	//	VertexId last = idList.get(idList.size() - 1);
	//	for (VertexId id : idList) {
	//		System.out.println(id + " " + id.compareTo(last));
	//		last = id;
	//		if (id.getVertex().vertexId != id) {
	//			System.out.println("****ERROR****");
	//		}
	//	}
	//		    	while (i < tmp.length && tmp[i] != halfEdge && tmp[i] != pair) {
	//		    		i++;
	//		    	}
	//		    	assert(i < tmp.length) : "element not found in hash bucket";
	//		    	/* copy the other elements */
	//		    	System.arraycopy(tmp, 0, buckets[bucketIndex], 0, i);
	//		    	if (i < buckets[bucketIndex].length) {
	//	    	    	System.arraycopy(tmp, i + 1, buckets[bucketIndex], i, buckets[bucketIndex].length - i);
	//		    	}
	//			}
	//			/* decrease count, rehash if necessary */
	//			count--;
	//			if (count < min) {
	//				setSize(size - 1);
	//				rehash();
	//			}
	//			version++;
	////			halfEdge.dispose();//FIXME
	//		}
	//		
	//		/**
	//		 * Modify number of buckets and reshash
	//		 */
	//		private void rehash() {
	//			HalfEdge[][] tmp = buckets;
	//			/* new number of buckets */
	//			buckets = new HalfEdge[capacity][];
	//			count = 0; // reset count!
	//			/* add all old elements to new buckets */
	//			for (HalfEdge[] bucket : tmp) {
	//				if (bucket != null) {
	//					for (int i = 0; i < bucket.length; i ++) {
	//						HalfEdge halfEdge = bucket[i];
	//						final int index = hash(halfEdge.getVertex(), halfEdge.getPairVertex());
	//						add(index, halfEdge);
	//					}
	//				}
	//			}
	//		}
	//		
	//		/**
	//		 * compute capacity, bitmask and min/max load values for a specified size
	//		 * @param size capacity = 2^size
	//		 */
	//		private void setSize(int size) {
	//			this.size = size;
	//			capacity = 1 << size;
	//			max = (int) (capacity * growFactor);
	//			min = (int) (capacity * shrinkFactor);
	//			if (min < MIN) {
	//				min = 0;
	//			}
	//			mask = capacity - 1;
	//		}
	//		
	//		@SuppressWarnings("unused")
	//		private void dump() {
	//			for (int i = 0; i < buckets.length; i++) {
	//				System.out.print("Bucket " + i + ":\t");
	//				if (buckets[i] != null) {
	//					for (HalfEdge edge : buckets[i]) {
	//						System.out.print(" " + edge);
	//					}
	//					System.out.println();
	//				} else {
	//					System.out.println(" *empty*");
	//				}
	//			}
	//		}
	//	}


	/**
	 * A special purpose hashtable that associates vertex-arrays with faces.
	 * It is used to find existing faces for given vertex-arrays.
	 * @author sascha
	 */
	private final class FaceSet implements Iterable<Face> {
		private final static double growFactor = 0.75;
		private final static double shrinkFactor = 0.25;
		private final static int MIN = 256;
		private int size;
		private int capacity;
		private int mask;
		private int count;
		private int max;
		private int min;
		private Face[][] buckets;
		/** additionally store each face in per-material sets */
		private final Map<Material, Set<Face>> perMaterialFaceSets = new HashMap<Material, Set<Face>>();

		FaceSet() {
			setSize(8);	//initial capacity = 256
			buckets = new Face[capacity][];
		}

		/**
		 * compute hashcode for edge array
		 * Hashcode for e0,e1,e2... is identical to hashcode of e2,e2,...e0 etc.
		 */
		private int hash(HalfEdge... edges) {
			int hash = 0;
			for (HalfEdge edge : edges) {
				hash ^= edge.getVertex().hashCode();
			}
			return hash & mask;
		}

		/**
		 * Returns a Face that spans the specified vertices.
		 * It that Face already exists, it is returned.
		 * If not, a new Face is stored in the
		 * hashTable and returned.
		 * @param vertices the vertices
		 * @return the Face connecting the specified vertices
		 */
		private Face findFace(final int index, final HalfEdge... edges) {

			//			System.out.println("getFace(" + Arrays.toString(vertices) + " called");
			//			System.out.println("bucketIndex = " + index);
			Face[] bucket = buckets[index];
			/* check if the bucket is non-empty */
			if (bucket != null) {
				/* scan bucket sequentially for the face*/
				faceLoop:
					for (Face face : bucket) {
						HalfEdge[] faceEdges = face.getEdges();
						for (int start = 0; start < faceEdges.length; start++) {
							if (faceEdges[start] == edges[0]) {
								//							System.out.println("  start=" + start);
								for (int i = 1; i < faceEdges.length; i++) {
									int j = start + i;
									if (j >= faceEdges.length) {
										j -= faceEdges.length;
									}
									//								System.out.println("vertices[" + i + "]=" + vertices[i] + " faceEdge[" + j + "].getVertex()=" + faceEdges[j].getVertex());
									if (edges[i] != faceEdges[j]) {
										continue faceLoop;
									}
								}
								return face;
							}
						}
					}
			}
			return null;
		}

		Face getFace(final HalfEdge... edges) {
			final int index = hash(edges);
			Face face = findFace(index, edges);
			if (face == null) {
				face = new Face(newFaceMaterial, edges);
				add(index, face);
			}
			return face;
		}

		boolean contains(final HalfEdge... edges) {
			return findFace(hash(edges), edges) != null;
		}

		boolean contains(final Face face) {
			final HalfEdge[] edges = face.getEdges();
			Face f = findFace(hash(edges), edges);
			if (f == null) {
				return false;
			}
			assert f == face;
			return true;
		}

		/**
		 * Removes the specified Face from the hashTable.
		 * @param face the Face to remove
		 */
		void removeFace(final Face face) {
			remove(hash(face.getEdges()), face);
		}

		/**
		 * Iterates over all faces, sorted by material, for all faces where face.getMaterial() != null
		 * A null material indicates a face that's not meant to be rendered (i.e. faces only needed to
		 * compute the position of neighbor-vertices at higher subdiv levels)
		 */
		public Iterator<Face> iterator() {
			@SuppressWarnings("unchecked")
			Iterator<Face>[] iterators = (Iterator<Face>[]) new Iterator[perMaterialFaceSets.size()];
			int i = 0;
			for (Map.Entry<Material, Set<Face>> entry : perMaterialFaceSets.entrySet()) {
				iterators[i++] = entry.getValue().iterator();
			}
			return new CompositeIterator<Face>(iterators);
		}

		public int size() {
			return count;
		}

		void setMaterial(final Face face, final Material newMaterial) {
			final Material oldMaterial = face.getMaterial();
			if (oldMaterial == newMaterial) {
				return;
			}
			if (oldMaterial != null) {
				perMaterialFaceSets.get(oldMaterial).remove(face);
			}
			if (newMaterial != null) {
				Set<Face> faceSet = perMaterialFaceSets.get(newMaterial);
				if (faceSet == null) {
					faceSet = new HashSet<Face>();
					perMaterialFaceSets.put(newMaterial, faceSet);
				}
				faceSet.add(face);
			}
			face.setMaterial(newMaterial);
		}

		/**
		 * Adds a face to a bucket
		 */
		private void add(final int bucketIndex, final Face face) {
			if (buckets[bucketIndex] == null) {
				/* empty bucket, create new one */
				buckets[bucketIndex] = new Face[] { face };
			} else {
				/* non empty bucket, grow by one element and copy all elements */
				Face[] tmp = buckets[bucketIndex];
				buckets[bucketIndex] = new Face[tmp.length + 1];
				System.arraycopy(tmp, 0, buckets[bucketIndex], 0, tmp.length);
				buckets[bucketIndex][tmp.length] = face;
			}
			/* increase count, rehash if necessary */
			count++;
			if (count > max) {
				setSize(size + 1);
				rehash();
			}

			final Material faceMaterial = face.getMaterial();
			if (faceMaterial != null) {
				Set<Face> faceSet = perMaterialFaceSets.get(faceMaterial);
				if (faceSet == null) {
					faceSet = new HashSet<Face>();
					perMaterialFaceSets.put(faceMaterial, faceSet);
				}
				faceSet.add(face);
			}
		}

		/**
		 * Removes a face from a bucket
		 */
		private void remove(final int bucketIndex, final Face face) {
			Face[] tmp = buckets[bucketIndex];
			if (tmp.length == 1) {
				/* length was 1, bucket is empty now */
				buckets[bucketIndex] = null;
			} else {
				/* shrink bucket by 1*/
				buckets[bucketIndex] = new Face[tmp.length - 1];
				/* scan for element to discard */
				int i = 0;
				while (i < tmp.length && tmp[i] != face) {
					i++;
				}
				assert(i < tmp.length) : "element not found in hash bucket";
				/* copy the other elements */
				System.arraycopy(tmp, 0, buckets[bucketIndex], 0, i);
				if (i < buckets[bucketIndex].length) {
					System.arraycopy(tmp, i + 1, buckets[bucketIndex], i, buckets[bucketIndex].length - i);
				}
			}
			/* decrease count, rehash if necessary */
			count--;
			if (count < min) {
				setSize(size - 1);
				rehash();
			}

			final Material faceMaterial = face.getMaterial();
			if (faceMaterial != null) {
				perMaterialFaceSets.get(faceMaterial).remove(face);
			}
			discardFace(face, true);
		}

		/**
		 * Modify number of buckets and reshash
		 */
		private void rehash() {
			Face[][] tmp = buckets;
			/* new number of buckets */
			buckets = new Face[capacity][];
			count = 0; // reset count!
			/* add all old elements to new buckets */
			for (Face[] bucket : tmp) {
				if (bucket != null) {
					for (int i = 0; i < bucket.length; i ++) {
						Face face = bucket[i];
						final int index = hash(face.getEdges());
						add(index, face);
					}
				}
			}
		}

		/**
		 * compute capacity, bitmask and min/max load values for a specified size
		 * @param size capacity = 2^size
		 */
		private void setSize(int size) {
			this.size = size;
			capacity = 1 << size;
			max = (int) (capacity * growFactor);
			min = (int) (capacity * shrinkFactor);
			if (min < MIN) {
				min = 0;
			}
			mask = capacity - 1;
		}

		@SuppressWarnings("unused")
		private void dump() {
			for (int i = 0; i < buckets.length; i++) {
				System.out.print("Bucket " + i + ":\t");
				if (buckets[i] != null) {
					for (Face face : buckets[i]) {
						System.out.print(" " + face);
					}
					System.out.println();
				} else {
					System.out.println(" *empty*");
				}
			}
		}
	}





	@TestSuit
	public static class SetTests {
		private EdgeSet edgeSet = new EdgeSet();
		private AbstractVertex[] vertices = new AbstractVertex[100];
		private HalfEdge[][] edges = new HalfEdge[100][100];
		private final Sds sds = new Sds(null);
		private final SdsModel sdsModel = new SdsModel(sds);

		public SetTests() {
			/* create vertices */
			for (int i = 0; i < vertices.length; i++) {
				vertices[i] = new BaseVertex(sdsModel);
			}
		}

		private int vertexNumber(AbstractVertex vertex) {
			for (int i = 0; i < vertices.length; i++) {
				if (vertices[i] == vertex) {
					return i;
				}
			}
			return -1;
		}

		@TestCase
		public TestResult edgeSetTest() {
			/* create edges */
			for (int k = 0; k < 2; k++) { // running 2 times to ensure that existing edges will be found and not created twice
				for (int i = 0; i < vertices.length; i++) {
					for (int j = 0; j < vertices.length; j++) {
						try {
							edges[i][j] = edgeSet.getHalfEdge(vertices[i], vertices[j]);
							if (i == j) {
								return TestResult.error("edge with v0=v1 cereated without assertion-error");
							}
						} catch (AssertionError e) {
							if (i != j) {
								return TestResult.error(e.getMessage());
							}
						}
					}
				}
			}

			/* count created edges in hashTable, check number */
			int count = 0;
			for (HalfEdge[] bucket : edgeSet.buckets) {
				if (bucket != null) {
					count += bucket.length;
				}
			}
			int expect = vertices.length * (vertices.length - 1) / 2;
			if (count != expect) {
				return TestResult.error("should have " + expect + " edges in hashTable, found " + count);
			}

			/* test that edges a-b actually have pair-edges b-a */
			for (int i = 0; i < vertices.length; i++) {
				for (int j = 0; j < vertices.length; j++) {
					if (i == j) {
						if (edges[i][j] != null) {
							return TestResult.error(edges[i][j] + " should be null");
						}
						continue;
					}
					if (edges[i][j].getPair() != edges[j][i]) {
						return TestResult.error(edges[i][j] + " pair is " + edges[i][j].getPair() + ", should be " + edges[j][i]);
					}
				}
			}

			/* remove all edges between vertices > 10 */
			for (int i = 0; i < vertices.length; i++) {
				for (int j = 0; j < i; j++) {
					if (i >= 10 || j >= 10) {
						edgeSet.removeHalfEdge(edges[i][j]);
					}
				}
			}

			/* check that the remaining edges are still there, but the rest has been removed */
			count = 0;
			final boolean[][] edgePresent = new boolean[vertices.length][vertices.length];
			for (HalfEdge[] bucket : edgeSet.buckets) {
				if (bucket != null) {
					for (HalfEdge edge : bucket) {
						count++;
						final int i = vertexNumber(edge.getVertex());
						final int j = vertexNumber(edge.getPairVertex());
						if (edgePresent[i][j]) {
							return TestResult.error(edge + " found twice");
						} else {
							edgePresent[i][j] = true;
						}
						if (edgePresent[j][i]) {
							return TestResult.error(edge.getPair() + " found twice");
						} else {
							edgePresent[j][i] = true;
						}
					}
				}
			}

			if (count != 45) {
				return TestResult.error("should have 45 edges in hashTable, found " + count);
			}

			for (int i = 0; i < vertices.length; i++) {
				for (int j = 0; j < vertices.length; j++) {
					boolean expecting = (i < 10 && j < 10 && i != j);
					if (edgePresent[i][j] != expecting) {
						return TestResult.error("edgePresent[" + i + "][" + j + "] should be " + expecting);
					}
				}
			}
			return TestResult.success();
		}

		@TestCase
		public TestResult faceSetTest() {

			FaceSet faceSet = sds.new FaceSet();

			/* create faces with valence 3..6 */
			AbstractVertex[][] faceVertices = new AbstractVertex[7][];
			HalfEdge[][] faceEdges = new HalfEdge[7][];
			Face[] faces = new Face[7];
			int vertexIndex = 0;
			for (int valence = 3; valence <= 6; valence++) {
				faceVertices[valence] = new AbstractVertex[valence];
				faceEdges[valence] = new HalfEdge[valence];
				for (int j = 0; j < valence; j++) {
					faceVertices[valence][j] = vertices[vertexIndex++];
				}
				for (int j = 0; j < valence; j++) {
					faceEdges[valence][j] = HalfEdge.getOrCreate(faceVertices[valence][j], faceVertices[valence][(j + 1) % valence]); 
				}
				faces[valence] = faceSet.getFace(faceEdges[valence]);
			}

			/* check that arrays with cycled vertices return the same faces */
			for (int valence = 3; valence <= 6; valence++) {
				HalfEdge[] e = new HalfEdge[valence];
				for (int i = 0; i < valence; i++) {
					for (int j = 0; j < valence; j++) {
						e[j] = faceEdges[valence][(i + j) % valence];	
					}
					Face test = faceSet.getFace(e);
					if (test != faces[valence]) {
						return TestResult.error("face " + Arrays.toString(faceEdges[valence]) + " != face " + Arrays.toString(e));
					}
				}
			}

			//			/* add face 2,x,0 (adjacent to 0,1,2) */
			//			faceSet.getFace(vertices[2], vertices[vertexIndex++], vertices[0]);
			//			/* creation of 2,1,0 (0,1,2 reversed) should throw asserionError */
			//			try {
			//				faceSet.getFace(vertices[2], vertices[1], vertices[0]);
			//				return TestResult.error("creation of flipped existing face did not trow an AssertionError");
			//			} catch (AssertionError e) {
			//				// OK;
			//			}

			/* should have 5 faces in set */
			if (faceSet.size() != 4) {
				return TestResult.error("faces in set: " + faceSet.size() + ", should be 4");
			}

			/* remove two faces */
			List<Face> faceList = new ArrayList<Face>();
			for (Face face : faceSet) {
				faceList.add(face);
			}
			faceSet.removeFace(faceList.get(0));
			faceSet.removeFace(faceList.get(2));
			faceSet.removeFace(faceList.get(3));
			/* should have 1 faces in set */
			if (faceSet.size() != 1) {
				return TestResult.error("faces in set: " + faceSet.size() + ", should be 2");
			}

			return TestResult.success();
		}
	}

	@TestSuit
	public static class SubdivTests {
		private final AbstractVertex[] v;
		private final Sds sds = new Sds(null);
		private final SdsModel sdsModel = new SdsModel(sds);
		private final Face[] faces;

		public SubdivTests() {
			/* create a cube */
			v = new BaseVertex[] {
					new BaseVertex(sdsModel, -1, -1, -1),
					new BaseVertex(sdsModel, -1, -1, +1),
					new BaseVertex(sdsModel, +1, -1, +1),
					new BaseVertex(sdsModel, +1, -1, -1),
					new BaseVertex(sdsModel, -1, +1, -1),
					new BaseVertex(sdsModel, -1, +1, +1),
					new BaseVertex(sdsModel, +1, +1, +1),
					new BaseVertex(sdsModel, +1, +1, -1),
			};
			faces = new Face[] {
					sds.addFace(null, sds.newFaceMaterial, v[0], v[1], v[2], v[3]),
					sds.addFace(null, sds.newFaceMaterial, v[1], v[0], v[4], v[5]),
					sds.addFace(null, sds.newFaceMaterial, v[2], v[1], v[5], v[6]),
					sds.addFace(null, sds.newFaceMaterial, v[3], v[2], v[6], v[7]),
					sds.addFace(null, sds.newFaceMaterial, v[0], v[3], v[7], v[4]),
					sds.addFace(null, sds.newFaceMaterial, v[7], v[6], v[5], v[4])
			};
		}


		@TestCase
		public TestResult subdivTest() {
			/* should have 6 faces at level 0 and none at level 1*/
			if (sds.faceSets[0].size() != 6) {
				return TestResult.error("expected 6 faces at level 0, found " + sds.faceSets[0].size());
			}
			if (sds.faceSets[1].size() != 0) {
				return TestResult.error("expected 0 faces at level 1, found " + sds.faceSets[1].size());
			}

			/* subdivide face 0 */
			sds.subdivideFace(0, faces[0], true);

			/* should have 6 faces at level 20 and none at level 1*/
			if (sds.faceSets[0].size() != 6) {
				return TestResult.error("expected 6 faces at level 0, found " + sds.faceSets[0].size());
			}
			if (sds.faceSets[1].size() != 20) {
				return TestResult.error("expected 20 faces at level 1, found " + sds.faceSets[1].size());
			}

			/* check sub-faces of face 0 */
			for (HalfEdge edge : faces[0].getFacePoint().getEdges()) {
				Face subFace = edge.getFace();
				if (subFace.getMaterial() == null) {
					return TestResult.error("face 0 subface " + subFace + " material == null");
				}
			}

			/* check sub-faces of face 1..4 */
			for (int i = 1; i <= 4; i++) {
				for (HalfEdge edge : faces[i].getFacePoint().getEdges()) {
					Face subFace = edge.getFace();
					if (subFace.getMaterial() != null) {
						return TestResult.error("face " + i + " subface " + subFace + " material != null");
					}
				}
			}

			/* check sub-faces of face 5 */
			if (faces[5].isSubdivided()) {
				return TestResult.error("face 5 is subdivided!");
			}

			/* check that we only receive 4 faces (with materials) from level 1 */
			int count = 0;
			for (Face f : sds.getFaces(1)) {
				count++;
			}
			if (count != 4) {
				return TestResult.error("expected 4 material-faces on level 1, got " + count);
			}

			/* compute positions of level-1 vertices */
			Point3d p = new Point3d();
			try {
				for (Face f : sds.getFaces(1)) {
					for (HalfEdge e : f.getEdges()) {
						e.getVertex().getPosition(p);
						e.getVertex().getLimit(p);
					}
					f.getControlSurface();
					f.getLimitSurface();
				}
			} catch (Exception e) {
				return TestResult.error(e.getMessage());
			}

			/* subdivide face 1 */
			sds.subdivideFace(0, faces[1], true);

			/* should have 6 faces at level 24 and none at level 1*/
			if (sds.faceSets[0].size() != 6) {
				return TestResult.error("expected 6 faces at level 0, found " + sds.faceSets[0].size());
			}
			if (sds.faceSets[1].size() != 24) {
				return TestResult.error("expected 24 faces at level 1, found " + sds.faceSets[1].size());
			}

			/* check sub-faces of face 0..1 */
			for (int i = 0; i <= 1; i++) {
				for (HalfEdge edge : faces[i].getFacePoint().getEdges()) {
					Face subFace = edge.getFace();
					if (subFace.getMaterial() == null) {
						return TestResult.error("face " + i + " subface " + subFace + " material == null");
					}
				}
			}

			/* check sub-faces of face 2..5 */
			for (int i = 2; i <= 5; i++) {
				for (HalfEdge edge : faces[i].getFacePoint().getEdges()) {
					Face subFace = edge.getFace();
					if (subFace.getMaterial() != null) {
						return TestResult.error("face " + i + " subface " + subFace + " material != null");
					}
				}
			}

			/* check that we only receive 8 faces (with materials) from level 1 */
			count = 0;
			for (Face f : sds.getFaces(1)) {
				count++;
			}
			if (count != 8) {
				return TestResult.error("expected 8 material-faces on level 1, got " + count);
			}

			/* compute positions of level-1 vertices */
			try {
				for (Face f : sds.getFaces(1)) {
					for (HalfEdge e : f.getEdges()) {
						e.getVertex().getPosition(p);
						e.getVertex().getLimit(p);
					}
					f.getControlSurface();
					f.getLimitSurface();
				}
			} catch (Exception e) {
				return TestResult.error(e.getMessage());
			}

			return TestResult.success();
		}
	}
}
