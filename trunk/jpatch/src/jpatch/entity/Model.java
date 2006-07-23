package jpatch.entity;

import java.util.*;

public class Model extends AbstractJPatchObject {

	private Collection<Selection> selections = new ArrayList<Selection>();
	private Collection<Selection> unmodifiableSelections = Collections.unmodifiableCollection(selections);
	private Collection<JPatchMaterial> materials = new ArrayList<JPatchMaterial>();
	private Collection<JPatchMaterial> unmodifiableMaterials = Collections.unmodifiableCollection(materials);
	private Collection<Morph> morphs = new ArrayList<Morph>();
	private Collection<Morph> unmodifiableMorphs = Collections.unmodifiableCollection(morphs);
	private Collection<AbstractTransform> transforms = new ArrayList<AbstractTransform>();
	private Collection<AbstractTransform> unmodifiableTransforms = Collections.unmodifiableCollection(transforms);
	private Collection<ControlPoint> curves = new ArrayList<ControlPoint>();
	private Collection<ControlPoint> unmodifiableCurves = Collections.unmodifiableCollection(curves);
	private Collection<Patch> patches = new ArrayList<Patch>();
	private Collection<Patch> unmodifiablePatches = Collections.unmodifiableCollection(patches);
	private ObjectRegistry objectRegistry = new ObjectRegistry();
	
	public Model() {
		super();
		name.set("Model");
	}

	public void setParent(JPatchObject parent) {
		
	}

	public Collection<AbstractTransform> getTransforms() {
		return unmodifiableTransforms;
	}

	public Collection<ControlPoint> getCurves() {
		return unmodifiableCurves;
	}

	public Collection<Patch> getPatches() {
		return unmodifiablePatches;
	}
	
	public Collection<JPatchMaterial> getMaterials() {
		return unmodifiableMaterials;
	}

	public Collection<Morph> getMorphs() {
		return unmodifiableMorphs;
	}

	public Collection<Selection> getSelections() {
		return unmodifiableSelections;
	}
	
	public ObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}
	
	public Selection getSelection(String name) {
		return (Selection) objectRegistry.getObject(Selection.class, name);
	}
	
	public JPatchMaterial getMaterial(String name) {
		return (JPatchMaterial) objectRegistry.getObject(JPatchMaterial.class, name);
	}
	
	public Morph getMorph(String name) {
		return (Morph) objectRegistry.getObject(Morph.class, name);
	}
	
	public Bone getBone(String name) {
		return (Bone) objectRegistry.getObject(Bone.class, name);
	}
	
	public void addSelection(Selection selection) {
		selections.add(selection);
	}
	
	public void removeSelection(Selection selection) {
		selections.remove(selection);
	}
	
	public void addMaterial(JPatchMaterial material) {
		materials.add(material);
	}
	
	public void removeMaterial(JPatchMaterial material) {
		materials.remove(material);
	}
	
	public void addMorph(Morph morph) {
		morphs.add(morph);
	}
	
	public void removeMorph(Morph morph) {
		morphs.remove(morph);
	}
	
	public void addTransform(AbstractTransform transform) {
		transforms.add(transform);
	}
	
	public void removeTransform(AbstractTransform transform) {
		transforms.remove(transform);
	}
	
	public void addCurve(ControlPoint cp) {
		curves.add(cp);
	}
	
	public void removeCurve(ControlPoint cp) {
		curves.remove(cp);
	}
	
	public void addPatch(Patch patch) {
		patches.add(patch);
	}
	
	public void removePatch(Patch patch) {
		patches.remove(patch);
	}
}
