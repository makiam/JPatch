package jpatch.entity;


import java.util.*;
import javax.vecmath.*;
import jpatch.auxilary.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

/**
 *  Description of the Class
 *
 * @author     aledinsk
 * @created    02. Mai 2003
 */
public class Model extends JPatchTreeNode {

	/**
	 *  Description of the Field
	 */
	private Curve firstCurve;
	private Patch firstPatch;
	private Curve lastCurve;
	private Patch lastPatch;

	private Bone firstBone;
	
	private JPatchTreeNode treenodeSelections;
	private JPatchTreeNode treenodeMaterials;
	private JPatchTreeNode treenodeExpressions; 
	private JPatchTreeNode treenodeBones;
	
	private ArrayList lstCandidateFivePointPatch = new ArrayList();
	
	//private JPatchMaterial[] aJPMaterial = new JPatchMaterial[32];
	
	private List lstMaterials = new ArrayList();
	private List lstSelections = new ArrayList();
	private List lstMorphs = new ArrayList();
	private List lstBoneShapes = new ArrayList();
	private HashMap mapPhonemes = new HashMap();
	
	private Rotoscope[] aRotoscope = new Rotoscope[6];
	//private ArrayList listeners = new ArrayList();
	
	public Model() {
		iNodeType = JPatchTreeNode.MODEL;
		strName = "New Model";
		treenodeSelections= new JPatchTreeNode(JPatchTreeNode.SELECTIONS,this,"Selections");
		treenodeMaterials = new JPatchTreeNode(JPatchTreeNode.MATERIALS,this,"Materials");
		treenodeExpressions = new JPatchTreeNode(JPatchTreeNode.MORPHS,this,"Expressions");
		treenodeBones = new JPatchTreeNode(JPatchTreeNode.BONES,this,"Bones");
		JPatchMaterial material = new JPatchMaterial(new Color3f(1,1,1));
		material.setName("Default Material");
		addMaterial(material);
	}
	
	public void setRotoscope(int view, Rotoscope rotoscope) {
		if (view >= 1 && view <= 6) {
			aRotoscope[view - 1] = rotoscope;
		}
	}
	
	public Rotoscope getRotoscope(int view) {
		if (view >= 1 && view <= 6) {
			return aRotoscope[view - 1];
		} else {
			return null;
		}
	}
	
	//public StringBuffer xmlRotoscopes(int tabs) {
	//	StringBuffer sb = new StringBuffer();
	//	String[] viewName = new String[] { "front","rear","top","bottom","left","right" };
	//	for (int i = 0; i < 6; i++) {
	//		if (aRotoscope[i] != null) {
	//			sb.append(aRotoscope[i].xml(tabs,viewName[i]));
	//		}
	//	}
	//	return sb;
	//}
	
	public StringBuffer xml(int tab) {
		StringBuffer sbIndent = XMLutils.indent(tab);
		StringBuffer sbIndent1 = XMLutils.indent(tab + 1);
		StringBuffer sbLineBreak = XMLutils.lineBreak();
		StringBuffer sb = new StringBuffer();
		sb.append(sbIndent).append("<model>").append(sbLineBreak);
		sb.append(sbIndent1).append("<name>").append(strName).append("</name>").append(sbLineBreak);
		/*
		for (int m = 0; m < 32; m++) {
			if (aJPMaterial[m] != null) {
				sb.append(aJPMaterial[m].xml(tab + 1,m));
			}
		}
		*/
		
		//sb.append(MainFrame.getInstance().getJPatchScreen().xmlRotoscopes(1));
		
		String[] viewName = new String[] { "front","rear","top","bottom","left","right" };
		for (int i = 0; i < 6; i++) {
			if (aRotoscope[i] != null) {
				sb.append(aRotoscope[i].xml(tab + 1,viewName[i]));
			}
		}
		
		int n = 0;
		for (Iterator it = lstMaterials.iterator(); it.hasNext();) {
			JPatchMaterial mat = (JPatchMaterial) it.next();
			mat.setXmlNumber(n++);
			sb.append(mat.xml(tab + 1));
		}
		sb.append(sbIndent1).append("<mesh>").append(sbLineBreak);
		setCpMap();
		for (Curve curve = getFirstCurve(); curve != null; curve = curve.getNext()) {
			//if (!curve.getStart().isStartHook()) {
				sb.append(curve.xml(tab + 2));
			//}
		}
		for (Patch patch = getFirstPatch(); patch != null; patch = patch.getNext()) {
			sb.append(patch.xml(tab + 2));
		}
		for (Iterator it = lstMorphs.iterator(); it.hasNext(); ) {
			Morph morph = (Morph) it.next();
			sb.append(morph.xml(tab + 2));
		}
		StringBuffer lipSyncMap = new StringBuffer();
		for (Iterator it = mapPhonemes.keySet().iterator(); it.hasNext(); ) {
			String phoneme = (String) it.next();
			Morph morph = (Morph) mapPhonemes.get(phoneme);
			if (morph != null) lipSyncMap.append(sbIndent1).append("\t\t<map phoneme=\"" + phoneme + "\" morph=\"" + lstMorphs.indexOf(morph) + "\"/>").append(sbLineBreak);
		}
		if (lipSyncMap.length() > 0) {
			sb.append(sbIndent1).append("\t<lipsync>").append(sbLineBreak);
			sb.append(lipSyncMap);
			sb.append(sbIndent1).append("\t</lipsync>").append(sbLineBreak);
		}
		sb.append(sbIndent1).append("</mesh>").append(sbLineBreak);
		for (Iterator it = lstSelections.iterator(); it.hasNext();) {
			PointSelection selection = (PointSelection) it.next();
			sb.append(selection.xml(tab + 1));
		}
		sb.append(sbIndent).append("</model>").append(sbLineBreak);
		return sb;
	}
	
	/*
	public void addMaterial(JPatchMaterial material, int n) {
		if (aJPMaterial[n] == null) {
			treenodeMaterials.add(material);
		}
		aJPMaterial[n] = material;
		material.setNumber(n);
	}
	*/
	
	public void setMorphFor(String phoneme, Morph morph) {
		mapPhonemes.put(phoneme, morph);
	}
	
	public Morph getMorphFor(String phoneme) {
		return (Morph) mapPhonemes.get(phoneme);
	}
	
	public Set getPhonemeMorphSet() {
		HashSet set = new HashSet();
		for (Iterator it = mapPhonemes.keySet().iterator(); it.hasNext(); ) {
			set.add(mapPhonemes.get(it.next()));
		}
		return set;
	}
	
	public boolean addMaterial(JPatchMaterial material) {
		/*
		for (int m = 0; m < 32; m++) {
			if (aJPMaterial[m] == null) {
				aJPMaterial[m] = material;
				material.setNumber(m);
				treenodeMaterials.add(material);
				return true;
			}
		}
		return false;
		*/
		treenodeMaterials.add(material);
		lstMaterials.add(material);
		return true;
	}
	
	public boolean checkSelection(NewSelection selection) {
		return (!lstSelections.contains(selection));
	}
	
	public NewSelection getSelection(NewSelection selection) {
		return (NewSelection) lstSelections.get(lstSelections.indexOf(selection));
	}
	
	public void addSelection(NewSelection selection) {
		/*
		for (int m = 0; m < 32; m++) {
			if (aJPMaterial[m] == null) {
				aJPMaterial[m] = material;
				material.setNumber(m);
				treenodeMaterials.add(material);
				return true;
			}
		}
		return false;
		*/
		//if (!lstSelections.contains(selection)) {
			treenodeSelections.add(selection);
			lstSelections.add(selection);
		//	return true;
		//} else {
		//	return false;
		//}
	}

	public void addSelection(int index, NewSelection selection) {
			treenodeSelections.add(index, selection);
			lstSelections.add(index, selection);
	}
	
	public void getBounds(Point3f min, Point3f max) {
		min.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		max.set(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		for (Patch patch = firstPatch; patch != null; patch = patch.getNext()) {
			ControlPoint[] acp = patch.getControlPoints();
			for (int i = 0; i < acp.length; i++) {
				Point3f p = acp[i].getPosition();
				if (p.x < min.x) min.x = p.x;
				if (p.y < min.y) min.y = p.y;
				if (p.z < min.z) min.z = p.z;
				if (p.x > max.x) max.x = p.x;
				if (p.y > max.y) max.y = p.y;
				if (p.z > max.z) max.z = p.z;
			}
		}
	}
	
	public void addExpression(Morph morph) {
		treenodeExpressions.add(morph);
		lstMorphs.add(morph);
	}
	
	public void setReferenceGeometry() {
		unapplyMorphs();
		for (Curve curve = firstCurve; curve != null; curve = curve.getNext()) {
			for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
				cp.setReference();
			}
		}
		applyMorphs();
	}
	
	public void unapplyMorphs() {
		for (Iterator it = lstMorphs.iterator(); it.hasNext(); ) {
			((Morph) it.next()).unapply();
		}
	}
	
	public void applyMorphs() {
		for (Iterator it = lstMorphs.iterator(); it.hasNext(); ) {
			((Morph) it.next()).apply();
		}
	}
	
//	public Iterator getSelectionIterator() {
//		return lstSelections.iterator();
//	}
	
	public List getSelections() {
		return lstSelections;
	}
	
	public Iterator getMorphIterator() {
		return lstMorphs.iterator();
	}
	
	public List getMorphList() {
		return lstMorphs;
	}
	
	public List getBoneShapeList() {
		return lstBoneShapes;
	}
	
	public void removeSelection(NewSelection selection) {
		treenodeSelections.remove(selection);
		lstSelections.remove(selection);
	}
	
	public JPatchTreeNode getTreenodeSelections() {
		return treenodeSelections;
	}
	
	public void removeMaterial(JPatchMaterial material) {
		treenodeMaterials.remove(material);
		lstMaterials.remove(material);
	}
	
	public void removeExpression(Morph morph) {
		treenodeExpressions.remove(morph);
		lstMorphs.remove(morph);
	}
	
	// accessor methods
	public void addCandidateFivePointPatchList(ArrayList list) {
		lstCandidateFivePointPatch.addAll(list);
	}
	
	public JPatchMaterial getMaterial(int m) {
		//if (m > 31) m = 0;
		return (JPatchMaterial)lstMaterials.get(m);
	}
	
	public List getMaterialList() {
		return lstMaterials;
	}
	
	/**
	 *  Sets the firstCurve attribute of the Model object
	 *
	 * @param  firstCurve  The new firstCurve value
	 */
	public void setFirstCurve(Curve firstCurve) {
		this.firstCurve = firstCurve;
	}


	/**
	 *  Sets the lastCurve attribute of the Model object
	 *
	 * @param  lastCurve  The new lastCurve value
	 */
	public void setLastCurve(Curve lastCurve) {
		this.lastCurve = lastCurve;
	}


	/**
	 *  Gets the firstCurve attribute of the Model object
	 *
	 * @return    The firstCurve value
	 */
	public Curve getFirstCurve() {
		return firstCurve;
	}

	public JPatchTreeNode getRootBone() {
		return treenodeBones;
	}
	
	public Bone getFirstBone() {
		return firstBone;
	}
	
	public void setFirstBone(Bone bone) {
		firstBone = bone;
	}
	
	public void addBone(Bone bone) {
		bone.insertBefore(firstBone);
		firstBone = bone;
	}
	
	/**
	 *  Gets the firstPatch attribute of the Model object
	 *
	 * @return    The firstPatch value
	 */
	public Patch getFirstPatch() {
		return firstPatch;
	}

	public Patch getLastPatch() {
		return lastPatch;
	}

	public void setFirstPatch(Patch p) {
		firstPatch = p;
	}

	public void setLastPatch(Patch p) {
		lastPatch = p;
	}
	/**
	 *  Gets the lastCurve attribute of the Model object
	 *
	 * @return    The lastCurve value
	 */
	public Curve getLastCurve() {
		return lastCurve;
	}
	/*
	public void reset() {
		for (Curve curve = getFirstCurve(); curve != null; curve = curve.getNext()) {
			curve.reset();
		}
		
		for (Patch patch = getFirstPatch(); patch != null; patch = patch.getNext()) {
			patch.reset();
		}
		
	}
	*/
	// public methods
	/**
	 *  Adds a feature to the Curve attribute of the Model object
	 *
	 * @param  curve  The feature to be added to the Curve attribute
	 */
	public void addCurve(Curve curve) {
		if (lastCurve != null) {
			lastCurve.setNext(curve);
		}
		curve.setPrev(lastCurve);
		curve.setNext(null);
		lastCurve = curve;
		if (firstCurve == null) {
			firstCurve = curve;
		}
		curve.setModel(this);
		/*
		for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
			ViewportManager.getInstance().addControlPoint(cp);
		}
		for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
			if (cp.hasNext()) {
				ViewportManager.getInstance().addCurveSegment(cp);
			}
		}
		*/
	}


	/**
	 *  Adds a feature to the Curve attribute of the Model object
	 *
	 * @param  start  The feature to be added to the Curve attribute
	 */
	public void addCurve(ControlPoint start) {
		Curve curve = new Curve(start, this);
		curve.validate();
		addCurve(curve);
	}


	/**
	 *  Adds a feature to the Patch attribute of the Model object
	 *
	 * @param  patch  The feature to be added to the Patch attribute
	 */
	public void addPatch(ControlPoint[] acp, JPatchCompoundEdit compoundEdit) {
		boolean old = false;
		patch_loop:
		for (Patch p = firstPatch; p != null; p = p.getNext()) {
			if (p.isEqual(acp)) {
				old = true;
				p.setValid(true);
				break patch_loop;
			}
		}
		if (!old) addPatch(new Patch(acp),compoundEdit);
	}

	public void addPatch(Patch patch, JPatchCompoundEdit compoundEdit) {
		if (compoundEdit == null) {
			if (lastPatch != null) {
				lastPatch.setNext(patch);
			}
			patch.setPrev(lastPatch);
			patch.setNext(null);
			lastPatch = patch;
			if (firstPatch == null) {
				firstPatch = patch;
			}
			patch.setModel(this);
		} else {
			compoundEdit.addEdit(new AddPatchEdit(patch));
		}
	}
	
	public void removePatch(Patch patch, JPatchCompoundEdit compoundEdit) {
		if (compoundEdit == null) {
			patch.remove();
		} else {
			compoundEdit.addEdit(new RemovePatchFromModelEdit(patch));
		}
	}
	
	public void removeMorph(Morph morph) {
		lstMorphs.remove(morph);
	}
	
	/**
	 *  Removes all patches which contain cp
	 */
	public ArrayList getPatchesContaining(ControlPoint cp) {
		ArrayList list = new ArrayList();
		for (Patch p = firstPatch; p != null; p = p.getNext()) {
			if (p.contains(cp)) list.add(p);
		}
		return list;
	}
	
	/**
	 *  Description of the Method
	 */
	public void clearPatches() {
		//firstPatch = null;
		//lastPatch = null;
		for (Patch p = firstPatch; p != null; p = p.getNext()) {
			p.setValid(false);
		}
	}
	
	public ArrayList allHeads() {
		ArrayList lstHead = new ArrayList();
		Curve curve = getFirstCurve();
		while (curve != null) {
			ControlPoint cp = curve.getStart();
			while (cp != null) {
				if (cp.isHead() && !cp.isChildHook()) {
					lstHead.add(cp);
				}
				cp = cp.getNextCheckNextLoop();
			}
			curve = curve.getNext();
		}
		return lstHead;
	}
	
	public int numberOfCurves() {
		int iNum = 0;
		Curve curve = getFirstCurve();
		while (curve != null) {
			if (!curve.getStart().isHook()) {
				iNum++;
			}
			curve = curve.getNext();
		}
		return iNum;
	}
	
	public int numberOfPatches() {
		int iNum = 0;
		Patch patch = getFirstPatch();
		while (patch != null) {
			iNum++;
			patch = patch.getNext();
		}
		return iNum;
	}
	
	///**
	// * clone heads
	// */
	//public PointSelection clone(ControlPoint[] acp) {
	//	Set setCPs = new HashSet();
	//	for (int i = 0; i < acp.length; i++) {
	//		ControlPoint[] stack = acp[i].getStack();
	//		for (int s = 0; s < stack.length; s++) {
	//			if (stack[s].isHook()) {
	//				boolean add = true;
	//				loop1:
	//				for (ControlPoint cp = stack[s].getStart(); cp != null; cp = cp.getNext()) {
	//					boolean part = false;
	//					loop2:
	//					for (int p = 0; p < acp.length; p++) {
	//						if (acp[p] == cp) {
	//							part = true;
	//							break loop2;
	//						}
	//					}
	//					if (!part) {
	//						add = false;
	//						break loop1;
	//					}
	//				}
	//				if (add) {
	//					setCPs.add(stack[s]);
	//				}
	//			} else { 
	//				setCPs.add(stack[s]);
	//			}
	//		}
	//	}
	//	return clone(setCPs);
	//}
	//
	///**
	// * clone cps
	// */
	//public PointSelection clone(Set setCPs) {
	//	ArrayList listCPs = new ArrayList();
	//	ArrayList listNewCurves = new ArrayList();
	//	HashMap mapCPs = new HashMap();
	//	PointSelection ps = new PointSelection();
	//	
	//	/* create cloned controlPoints */
	//	for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
	//		ControlPoint cpToClone = (ControlPoint) it.next();
	//		ControlPoint cpClone = new ControlPoint();
	//		mapCPs.put(cpToClone, cpClone);
	//	}
	//	
	//	/* connect cloned controlPoints */
	//	for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
	//		ControlPoint cpToClone = (ControlPoint) it.next();
	//		ControlPoint cpClone = (ControlPoint) mapCPs.get(cpToClone);
	//		cpClone.setNext((ControlPoint) mapCPs.get(cpToClone.getNext()));
	//		cpClone.setPrev((ControlPoint) mapCPs.get(cpToClone.getPrev()));
	//		cpClone.setNextAttached((ControlPoint) mapCPs.get(cpToClone.getNextAttached()));
	//		cpClone.setPrevAttached((ControlPoint) mapCPs.get(cpToClone.getPrevAttached()));
	//		cpClone.setParentHook((ControlPoint) mapCPs.get(cpToClone.getParentHook()));
	//		cpClone.setChildHook((ControlPoint) mapCPs.get(cpToClone.getChildHook()));
	//		cpClone.setHookPos(cpToClone.getHookPos());
	//	}
	//	
	//	/* check for loops, add curves to model*/
	//	for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
	//		ControlPoint cpToClone = (ControlPoint) it.next();
	//		ControlPoint cpClone = (ControlPoint) mapCPs.get(cpToClone);
	//		
	//		/* check if the cloned curve is closed */
	//		if (cpToClone.getLoop()) {
	//			boolean bLoop = false;
	//			loop:
	//			for (ControlPoint cp = cpClone.getNext(); cp != null; cp = cp.getNext()) {
	//				if (cp == cpClone) {
	//					bLoop = true;
	//					break loop;
	//				}
	//			}
	//			cpClone.setLoop(bLoop);
	//		}
	//		
	//		/* add a curve if we are a start point AND have a next point */
	//		if ((cpClone.getLoop() || cpClone.getPrev() == null) && cpClone.getNext() != null) {
	//			addCurve(cpClone);
	//		}
	//		
	//		/* detach empty points */
	//		if (cpClone.getNext() == null && cpClone.getPrev() == null) {
	//			if (cpClone.getNextAttached() != null) {
	//				cpClone.getNextAttached().setPrevAttached(cpClone.getPrevAttached());
	//			}
	//			if (cpClone.getPrevAttached() != null) {
	//				cpClone.getPrevAttached().setNextAttached(cpClone.getNextAttached());
	//			}
	//		}
	//	}
	//	
	//	for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
	//		ControlPoint cpToClone = (ControlPoint) it.next();
	//		ControlPoint cpClone = (ControlPoint) mapCPs.get(cpToClone);
	//		if (cpClone.isHead()) {
	//			/* if we are a head, add us to the new selection */
	//			ps.addControlPoint(cpClone);
	//			cpClone.setPosition(cpToClone.getPosition());
	//		}
	//	}
	//	
	//	/* clone patches */
	//	ArrayList newPatches = new ArrayList();
	//	ArrayList newMaterials = new ArrayList();
	//	for (Patch p = getFirstPatch(); p != null; p = p.getNext()) {
	//		ControlPoint[] acp = p.getControlPoints();
	//		ControlPoint[] acpNew = new ControlPoint[acp.length];
	//		boolean addPatch = true;
	//		loop:
	//		for (int n = 0; n < acp.length; n++) {
	//			if (mapCPs.get(acp[n]) == null) {
	//				addPatch = false;
	//				break loop;
	//			} else {
	//				acpNew[n] = (ControlPoint) mapCPs.get(acp[n]);
	//			}
	//		}
	//		if (addPatch) {
	//			newPatches.add(acpNew);
	//			newMaterials.add(p.getMaterial());
	//		}
	//	}
	//	Iterator itMat = newMaterials.iterator();
	//	for (Iterator it = newPatches.iterator(); it.hasNext(); ) {
	//		addPatch((ControlPoint[]) it.next(), null);
	//		getLastPatch().setMaterial((JPatchMaterial) itMat.next());
	//	}
	//	return ps;
	//}
	
	public ArrayList getCandidateFivePointPatchList() {
		return lstCandidateFivePointPatch;
	}
	
	/**
	 *  Description of the Method
	 */
	public void computePatches() {
		computePatches(null);
	}
	
	public void computePatches(JPatchCompoundEdit compoundEdit) {
		//System.out.println("computePatches() started... " + lstCandidateFivePointPatch.size() + " candidate 5-point-patches");
		clearPatches();
		//for (Curve curve = getFirstCurve(); curve != null; curve = curve.getNext()) {
		//	for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
		//		cp.clearPatches();
		//	}
		//}
		ArrayList lstHead = allHeads();							// stores all heads
		ArrayList lstlstNeighbor = new ArrayList();					// a list with lists of neighbors (per head)
		HashMap mapHeadIndex = new HashMap();						// key = cp, value = index
		for (int h = 0; h < lstHead.size(); h++) {
			ControlPoint cp = (ControlPoint)lstHead.get(h);
			lstlstNeighbor.add(cp.allNeighbors());
			mapHeadIndex.put(cp,new Integer(h));
	//		System.out.println(h + "\t" + cp + "\t" + cp.getPosition());
		}
	
		// ------
		/*
		Curve curve;
		ControlPoint cp;
		for(curve = getFirstCurve(); curve != null; curve = curve.getNext()) {
			for (cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
				System.out.println(cp + " " + cp.getPosition() + " " + cp.getHead());
			}
		}
			for (int h = 0; h < lstHead.size(); h++) {
				ArrayList test = (ArrayList)lstlstNeighbor.get(h);
				System.out.print(h + ":");
				cp = (ControlPoint)lstHead.get(h);
				System.out.print(((Integer)mapHeadIndex.get(cp)).intValue() + " ");
				for (int t = 0; t < test.size(); t++) {
					cp = ((ControlPoint[])test.get(t))[0];
					int index = ((Integer)mapHeadIndex.get(cp)).intValue();
					System.out.print(index + " ");
				}
				System.out.println();
			}
			System.out.println();
		// ------
		*/
		int num3 = 0;
		int num4 = 0;
		int num5 = 0;
		
		/**
		* check candidate 5-point patches
		**/
		ArrayList candidateFivePointPatchesToRemove = new ArrayList();
		for (int f = 0; f < lstCandidateFivePointPatch.size(); f++) {
			ControlPoint[] fpp = new ControlPoint[10];
			ControlPoint[] acp = (ControlPoint[])lstCandidateFivePointPatch.get(f);
			boolean ok = false;
			Integer iIndex = (Integer) mapHeadIndex.get(trueHead(acp[0]));
			int index;
			ArrayList lstNeighbor;
			if (iIndex != null) {
				index = iIndex.intValue();
				lstNeighbor = (ArrayList)lstlstNeighbor.get(index);
				for (int n = 0; n <lstNeighbor.size(); n++) {
					ControlPoint[] acpNeighbor = (ControlPoint[])lstNeighbor.get(n);
					if (acpNeighbor[0] == trueHead(acp[1])) {
						ok = true;
						fpp[0] = acpNeighbor[1];
						fpp[1] = acpNeighbor[2];
					}
				}
			}
			if (ok) {
				ok = false;
				index = ((Integer)mapHeadIndex.get(trueHead(acp[1]))).intValue();
				lstNeighbor = (ArrayList)lstlstNeighbor.get(index);
				for (int n = 0; n <lstNeighbor.size(); n++) {
					ControlPoint[] acpNeighbor = (ControlPoint[])lstNeighbor.get(n);
					if (acpNeighbor[0] == trueHead(acp[2])) {
						ok = true;
						fpp[2] = acpNeighbor[1];
						fpp[3] = acpNeighbor[2];
					}
				}
			}
			if (ok) {
				ok = false;
				index = ((Integer)mapHeadIndex.get(trueHead(acp[2]))).intValue();
				lstNeighbor = (ArrayList)lstlstNeighbor.get(index);
				for (int n = 0; n <lstNeighbor.size(); n++) {
					ControlPoint[] acpNeighbor = (ControlPoint[])lstNeighbor.get(n);
					if (acpNeighbor[0] == trueHead(acp[3])) {
						ok = true;
						fpp[4] = acpNeighbor[1];
						fpp[5] = acpNeighbor[2];
					}
				}
			}
			if (ok) {
				ok = false;
				index = ((Integer)mapHeadIndex.get(trueHead(acp[3]))).intValue();
				lstNeighbor = (ArrayList)lstlstNeighbor.get(index);
				for (int n = 0; n <lstNeighbor.size(); n++) {
					ControlPoint[] acpNeighbor = (ControlPoint[])lstNeighbor.get(n);
					if (acpNeighbor[0] == trueHead(acp[4])) {
						ok = true;
						fpp[6] = acpNeighbor[1];
						fpp[7] = acpNeighbor[2];
					}
				}
			}
			if (ok) {
				ok = false;
				index = ((Integer)mapHeadIndex.get(trueHead(acp[4]))).intValue();
				lstNeighbor = (ArrayList)lstlstNeighbor.get(index);
				for (int n = 0; n <lstNeighbor.size(); n++) {
					ControlPoint[] acpNeighbor = (ControlPoint[])lstNeighbor.get(n);
					if (acpNeighbor[0] == trueHead(acp[0])) {
						ok = true;
						fpp[8] = acpNeighbor[1];
						fpp[9] = acpNeighbor[2];
					}
				}
			}
			if (
				ok &&
				fpp[0].trueCp() != fpp[9].trueCp() &&
				fpp[1].trueCp() != fpp[2].trueCp() &&
				fpp[3].trueCp() != fpp[4].trueCp() &&
				fpp[5].trueCp() != fpp[6].trueCp() &&
				fpp[7].trueCp() != fpp[8].trueCp()
			) {
				addPatch(fpp,compoundEdit);
				num5++;
			} else {
				//lstCandidateFivePointPatch.remove(acp);
				candidateFivePointPatchesToRemove.add(acp);
			}
		}					
		lstCandidateFivePointPatch.removeAll(candidateFivePointPatchesToRemove);
		
		/**
		* search for 3- and 4-point patches
		**/
		for (int y = 0; y < lstHead.size(); y++) {
			ArrayList lstNeighbor = (ArrayList)lstlstNeighbor.get(y);
			for (int x = 0; x < lstNeighbor.size() - 1; x++) {
				ControlPoint[] acpNeighborX = (ControlPoint[])lstNeighbor.get(x);
				ControlPoint headX = acpNeighborX[0];
				int indexX = ((Integer)mapHeadIndex.get(headX)).intValue();
				if (indexX > y) {
//					System.out.println("\tX=" + indexX);
					for (int z = x + 1; z < lstNeighbor.size(); z++) {
						ControlPoint[] acpNeighborZ = (ControlPoint[])lstNeighbor.get(z);
						ControlPoint headZ = acpNeighborZ[0];
						int indexZ = ((Integer)mapHeadIndex.get(headZ)).intValue();
						if (indexZ > y) {
//							System.out.println("\t\tZ=" + indexZ);
							ArrayList lstX = (ArrayList)lstlstNeighbor.get(indexX);
							ArrayList lstZ = (ArrayList)lstlstNeighbor.get(indexZ);
							//
							// search for 3-point patch...
							//
							for (int xx = 0; xx < lstX.size(); xx++) {
								ControlPoint[] acpNeighborXX = (ControlPoint[])lstX.get(xx);
								ControlPoint headXX = acpNeighborXX[0];
								if (headXX == headZ) {
									//Curve c = acpNeighborXX[1].getHookCurve();
									//if (c != acpNeighborXX[2].getHookCurve() || c != acpNeighborZ[1].getHookCurve() || c != acpNeighborZ[2].getHookCurve() || c != acpNeighborX[1].getHookCurve() || c != acpNeighborX[2].getHookCurve()) {
									//System.out.println("<<<");
									//System.out.println(acpNeighborX[1]);
									//System.out.println(acpNeighborX[2]);
									//System.out.println(acpNeighborXX[1]);
									//System.out.println(acpNeighborXX[2]);
									//System.out.println(acpNeighborZ[1]);
									//System.out.println(acpNeighborZ[2]);
									//System.out.println(">>>");
									if (
										acpNeighborX[1].trueCp() != acpNeighborZ[1].trueCp() &&
										acpNeighborXX[1].trueCp() != acpNeighborX[2].trueCp() &&
										acpNeighborZ[2].trueCp() != acpNeighborXX[2].trueCp()
									) {
									//	(acpNeighborX[1].getHookCurve() != acpNeighborZ[2].getHookCurve()) &&
									//	(acpNeighborZ[1].getHookCurve() != acpNeighborXX[2].getHookCurve()) &&
									//	(acpNeighborXX[1].getHookCurve() != acpNeighborX[2].getHookCurve())
									//) {
										ControlPoint[] acpPatch = new ControlPoint[] {
											acpNeighborX[1],
											acpNeighborX[2],
											acpNeighborXX[1],
											acpNeighborXX[2],
											acpNeighborZ[2],
											acpNeighborZ[1],
										};
										//System.out.println(acpPatch[0] + " " + acpPatch[1] + " " + acpPatch[2] + " " + acpPatch[3] + " " + acpPatch[4] + " " + acpPatch[5]);
										addPatch(acpPatch,compoundEdit);
										num3++;
										//System.out.println("+");
									}
								}
								/*		
								int indexXX = ((Integer)mapHeadIndex.get(headXX)).intValue();
								if (indexXX == indexZ) {
									System.out.print("3-point-patch " + num3++ + "found:");
									System.out.println(y + " " + indexX + " " + indexZ);
								}
								*/
							}
							//
							// search for 4-point patch...
							//
							boolean ok = true;
							for (int xx = 0; xx < lstX.size(); xx++) {
								ControlPoint[] acpNeighborXX = (ControlPoint[])lstX.get(xx);
								ControlPoint headXX = acpNeighborXX[0];
								if (headXX == headZ) {
									ok = false;
								}
							}
							for (int zz = 0; zz < lstZ.size(); zz++) {
								ControlPoint[] acpNeighborZZ = (ControlPoint[])lstZ.get(zz);
								ControlPoint headZZ = acpNeighborZZ[0];
								if (headZZ == headX) {
									ok = false;
								}
							}
							for (int xx = 0; xx < lstX.size(); xx++) {
								ControlPoint[] acpNeighborXX = (ControlPoint[])lstX.get(xx);
								ControlPoint headXX = acpNeighborXX[0];
								int indexXX = ((Integer)mapHeadIndex.get(headXX)).intValue();
								if (indexXX != y) {
									for (int zz = 0; zz < lstZ.size(); zz++) {
										ControlPoint[] acpNeighborZZ = (ControlPoint[])lstZ.get(zz);
										ControlPoint headZZ = acpNeighborZZ[0];
										if (headZZ == headXX) {
											
											// eliminate if 3pp
											
											for (int w = 0; w < lstNeighbor.size(); w++) {
												ControlPoint[] acpNeighborW = (ControlPoint[])lstNeighbor.get(w);
												ControlPoint headW = acpNeighborW[0];
												if (headXX == headW) {
													ok = false;
												}
											}
											
											
											//
											if (ok) {
											//Curve c = acpNeighborXX[1].getHookCurve();
											//if (c != acpNeighborXX[2].getHookCurve() || c != acpNeighborZ[1].getHookCurve() || c != acpNeighborZ[2].getHookCurve() || c != acpNeighborX[1].getHookCurve() || c != acpNeighborX[2].getHookCurve() || c != acpNeighborZZ[1].getHookCurve() || c != acpNeighborZZ[2].getHookCurve()) {
											if (
												acpNeighborX[1].trueCp() != acpNeighborZ[1].trueCp() &&
												acpNeighborXX[1].trueCp() != acpNeighborX[2].trueCp() &&
												acpNeighborZZ[2].trueCp() != acpNeighborXX[2].trueCp() &&
												acpNeighborZZ[1].trueCp() != acpNeighborZ[2].trueCp()
											) {
												ControlPoint[] acpPatch = new ControlPoint[] {
													acpNeighborX[1],
													acpNeighborX[2],
													acpNeighborXX[1],
													acpNeighborXX[2],
													acpNeighborZZ[2],
													acpNeighborZZ[1],
													acpNeighborZ[2],
													acpNeighborZ[1],
												};
												addPatch(acpPatch,compoundEdit);
												num4++;
											}
											}
										}		
										/*
										int indexZZ = ((Integer)mapHeadIndex.get(headZZ)).intValue();
										if (indexXX == indexZZ) {
											System.out.print("4-point-patch " + num++ + " found:");
											System.out.println(y + " " + indexX + " " + indexZ + " " + indexXX);
										}
										*/
									}
								}
							}
						}
					}
				}
			}
		}
		/*
		 * remove invalid patches
		 */
		for (Patch p = firstPatch; p != null; p = p.getNext()) {
			if (!p.isValid()) {
				//p.remove();
				removePatch(p,compoundEdit);
			}
		}
		//System.out.print(num3 + " 3-point-, " + num4 + " 4-point- and " + num5 + " 5-point-patches found ");
		//System.out.println("(" + lstCandidateFivePointPatch.size() + " candidate 5-point-patches)");
	//	System.out.println("...stop");
	}

	public void dump() {
		System.out.println("------------- curves -------------");
		System.out.println("\tcp\tnext\tprev\tloop\tna\tpa\tphook\tchook\thpos\n");
		Curve curve = getFirstCurve();
		while (curve != null) {
			System.out.println("Curve: " + curve.hashCode() + " closed: " + curve.isClosed());
			//System.out.println(curve);
			
			ControlPoint cp = curve.getStart();
			while (cp != null) {
				System.out.println("\t" + cp + "\t" + cp.getNext() + "\t" + cp.getPrev() + "\t" + cp.getLoop() + "\t" + cp.getNextAttached() + "\t" + cp.getPrevAttached() + "\t" + cp.getParentHook() + "\t" + cp.getChildHook() + "\t" + cp.getHookPos());
				
				//System.out.println("\t" + cp + "\t" + cp.getPosition() + "\t" + cp.getNext() + "\t" + cp.getPrev() + "\t" + cp.getNextAttached() + "\t" + cp.getPrevAttached() + "\t" + cp.getLoop());
				//System.out.println("\t" + cp);
				//System.out.println("\t" + cp.getHead());
				//System.out.println("\t" + cp.getInTangent() + "\t" + cp.getPosition() + "\t" + cp.getOutTangent());
				//System.out.println(cp + "\t" + cp.isHook() + "\t" + cp.isTargetHook() + "\t" + cp.getHead());
				cp = cp.getNextCheckNextLoop();
			}
			
			curve = curve.getNext();
		}
		System.out.println("\n\n------------- patches -------------");
		for (Patch p = firstPatch; p != null; p = p.getNext()) {
			System.out.println(p);
		}
		
		System.out.println("\n\n--------active selection -------");
		NewSelection selection = MainFrame.getInstance().getSelection();
		if (selection != null) {
			System.out.println(selection);
			System.out.println(selection.getMap());
			System.out.println();
		}
		
		System.out.println("\n\n----------- selections -------------");
		for (Iterator it = lstSelections.iterator(); it.hasNext(); ) {
			selection = (NewSelection) it.next();
			System.out.println(selection);
			System.out.println(selection.getMap());
			System.out.println();
		}
		
		System.out.println("\n\n----------- morphs -------------");
		for (Iterator it = lstMorphs.iterator(); it.hasNext(); ) {
			((Morph) it.next()).dump();
		}
		
		System.out.println("\n\n----------- end -------------");
	}
	
	private void setCpMap() {
		HashMap map = new HashMap();
		int i = 0;
		for (Curve curve = getFirstCurve(); curve != null; curve = curve.getNext()) {
			for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
				map.put(cp,new Integer(i++));
			}
		}
		ControlPoint.setMap(map);
	}
	
	private ControlPoint trueHead(ControlPoint cp) {
		return (cp.getParentHook() == null) ? cp.getHead() : cp.getParentHook().getHead();
	}
}

