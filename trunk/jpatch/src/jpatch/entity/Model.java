package jpatch.entity;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import jpatch.auxilary.XmlWriter;

public class Model extends AbstractNamedObject {

	private Collection<Selection> selections = new ArrayList<Selection>();
	private Collection<Selection> unmodifiableSelections = Collections.unmodifiableCollection(selections);
	private Collection<OLDMaterial> materials = new ArrayList<OLDMaterial>();
	private Collection<OLDMaterial> unmodifiableMaterials = Collections.unmodifiableCollection(materials);
	private Collection<OLDMorph> morphs = new ArrayList<OLDMorph>();
	private Collection<OLDMorph> unmodifiableMorphs = Collections.unmodifiableCollection(morphs);
	private Collection<AbstractTransform> transforms = new ArrayList<AbstractTransform>();
	private Collection<AbstractTransform> unmodifiableTransforms = Collections.unmodifiableCollection(transforms);
	private Collection<ControlPoint> curves = new ArrayList<ControlPoint>();
	private Collection<ControlPoint> unmodifiableCurves = Collections.unmodifiableCollection(curves);
	private Collection<Patch> patches = new ArrayList<Patch>();
	private Collection<Patch> unmodifiablePatches = Collections.unmodifiableCollection(patches);
	private ObjectRegistry objectRegistry = new ObjectRegistry();
	private int nextCpId;
	
	public Model() {
		super();
		name.set("Model");
	}

	public void setParent(JPatchObject parent) {
		
	}

	public Iterable<AbstractTransform> getTransforms() {
		return unmodifiableTransforms;
	}

	public Iterable<ControlPoint> getCurves() {
		return unmodifiableCurves;
	}

	public Iterable<Patch> getPatches() {
		return unmodifiablePatches;
	}
	
	public Iterable<OLDMaterial> getMaterials() {
		return unmodifiableMaterials;
	}

	public Iterable<OLDMorph> getMorphs() {
		return unmodifiableMorphs;
	}

	public Iterable<Selection> getSelections() {
		return unmodifiableSelections;
	}
	
	public ObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}
	
	public Selection getSelection(String name) {
		return (Selection) objectRegistry.getObject(Selection.class, name);
	}
	
	public OLDMaterial getMaterial(String name) {
		return (OLDMaterial) objectRegistry.getObject(OLDMaterial.class, name);
	}
	
	public OLDMorph getMorph(String name) {
		return (OLDMorph) objectRegistry.getObject(OLDMorph.class, name);
	}
	
	public Bone getBone(String name) {
		return (Bone) objectRegistry.getObject(Bone.class, name);
	}
	
	public void addSelection(Selection selection) {
		selection.setParent(this);
		selections.add(selection);
	}
	
	public void removeSelection(Selection selection) {
		selections.remove(selection);
		selection.setParent(null);
	}
	
	public void addMaterial(OLDMaterial material) {
		materials.add(material);
//		objectRegistry.add(material);
	}
	
	public void removeMaterial(OLDMaterial material) {
		materials.remove(material);
//		objectRegistry.remove(material);
	}
	
	public void addMorph(OLDMorph morph) {
		morphs.add(morph);
//		objectRegistry.add(morph);
	}
	
	public void removeMorph(OLDMorph morph) {
		morphs.remove(morph);
//		objectRegistry.remove(morph);
	}
	
	public void addTransform(AbstractTransform transform) {
		transforms.add(transform);
		objectRegistry.add(transform);
	}
	
	public void removeTransform(AbstractTransform transform) {
		transforms.remove(transform);
		objectRegistry.remove(transform);
	}
	
	public void addCurve(ControlPoint cp) {
		assert cp.isStart() : "ControlPoint " + cp + " is not the start of a curve";
		assert !curves.contains(cp) : "Curve " + cp + " already exists in model " + cp.getModel() + ".";
		curves.add(cp);
	}
	
	public void removeCurve(ControlPoint cp) {
//		assert cp.isStart() : "ControlPoint " + cp + " is not the start of a curve";
		assert curves.contains(cp) : "Curve " + cp + " does not exist in model " + cp.getModel() + ".";
		curves.remove(cp);
	}
	
	public void addPatch(Patch patch) {
		patches.add(patch);
	}
	
	public void removePatch(Patch patch) {
		patches.remove(patch);
	}
	
	public int getNextCpId() {
		return nextCpId++;
	}
	
	public void setNextCpId(int nextCpId) {
		this.nextCpId = nextCpId;
	}
	
	public void initControlPoints() {
		for (ControlPoint start : curves) {
			computeCurveTangents(start);
		}
	}
	
	public void computeCurveTangents(ControlPoint cp) {
		do {
			cp.computeTangents(false);
			cp = cp.getNextNonHook();
		} while (cp != null && ! cp.isLoop());
	}
	
	public void writeXml(XmlWriter xmlWriter) throws IOException {
		renumberControlPoints();
		xmlWriter.startElement("model");
		xmlWriter.attribute("name", name);
		xmlWriter.attribute("type", "patch");
		xmlWriter.startElement("curves");
		for (ControlPoint cp : curves) {
			cp.writeXml(xmlWriter);
		}
		xmlWriter.endElement();
		xmlWriter.endElement();
	}
	
	private void renumberControlPoints() {
		int n = 0;
		for (ControlPoint cp : curves) {
			do {
				cp.setId(n++);
				cp = cp.getNext();
			} while (cp != null && !cp.isLoop());
		}
	}
}
