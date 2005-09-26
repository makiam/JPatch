package jpatch.control.edit;

import java.util.*;
import jpatch.auxilary.*;
import jpatch.entity.*;
import jpatch.boundary.*;


/**
* This abstract class provides common methods to clone ControlPoints which are
* needed by CopyEdit, LatheEdit and ExtrudeEdit
**/

public abstract class AbstractClone extends JPatchCompoundEdit {

	/** Array containing all ControlPoints to be cloned (in most cases the selection) **/
	protected ControlPoint[] acp;
	/** maps cloned ControlPoints to their originals **/
	protected Map mapClones = new HashMap();
	/** maps originals to their clones **/
	protected Map mapOriginals = new HashMap();

	public AbstractClone(ControlPoint[] controlPointsToClone) {
		acp = controlPointsToClone;
	}

	public AbstractClone() {
	}
	
	public static boolean checkForHooks(ControlPoint[] acp) {
		for (int i = 0; i < acp.length; i++) {
			ControlPoint[] acpStack = acp[i].getStack();
			for (int s = 0; s < acpStack.length; s++) {
				ControlPoint cp = acpStack[s];
				if (cp.isHook()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	* checks all selected ControlPoints (those in acp), and if they are really to be
	* cloned, adds them to mapCPs
	**/
	protected void buildCloneMap(boolean hooks) {
		ArrayList targetHooks = new ArrayList();
		for (int i = 0; i < acp.length; i++) {
			ControlPoint[] acpStack = acp[i].getStack();
			for (int s = 0; s < acpStack.length; s++) {
				ControlPoint cp = acpStack[s];

				/*
				* check if a hook-curve should be added
				*/
				if (cp.getNextAttached() == null || !cp.getNextAttached().isHook()) {
					if (cp.isStartHook()) {
						if (hooks && checkStartHook(cp)) {
							addToCloneMap(cp);
							addToCloneMap(cp.getEnd());
						}
					} else if (cp.isHook()) {
						if (hooks && cp.getPrevAttached() != null && checkForNeighbor(cp.getPrevAttached())) {
							if (checkStartHook(cp.getStart())) {
								addToCloneMap(cp);
							}
						}
					} else if (checkForNeighbor(cp)) {
						addToCloneMap(cp);
					}
				} else {
					targetHooks.add(cp);
				}
			}
		}
		for (Iterator it = targetHooks.iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			ControlPoint hook = cp.getNextAttached();
			ControlPoint startHook = hook.getStart();
			ControlPoint endHook = hook.getEnd();
			if (mapClones.containsKey(hook) && mapClones.containsKey(startHook) && mapClones.containsKey(endHook)) {
				addToCloneMap(cp);
			} else {
				if (cp.getPrev() != null && (cp.getPrev().getPrev() == null || !mapClones.containsKey(cp.getPrev().getPrev()))) {
					mapOriginals.remove(mapClones.get(cp.getPrev()));
					mapClones.remove(cp.getPrev());
					//System.out.println(cp.getPrev() + " removed");
				}
				if (cp.getNext() != null && (cp.getNext().getNext() == null || !mapClones.containsKey(cp.getNext().getNext()))) {
					mapOriginals.remove(mapClones.get(cp.getNext()));
					mapClones.remove(cp.getNext());
					//System.out.println(cp.getNext() + " removed");
				}
				mapOriginals.remove(mapClones.get(hook));
				mapClones.remove(hook);
				//System.out.println(hook + " removed");
			}
		}
	}

	/**
	* configures the clones after their originals
	**/
	protected void cloneControlPoints() {
		for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cpClone = (ControlPoint) it.next();
			ControlPoint cpOriginal = getOriginal(cpClone);
			if (cpClone != cpOriginal) {
				/* set next and prev controlPoints */
				if (!cpOriginal.isHook()) {
					cpClone.setNext(getClone(cpOriginal.getPrev()));
					cpClone.setPrev(getClone(cpOriginal.getNext()));
				} else {
					ControlPoint cpNext;
					for (cpNext = cpOriginal.getNext(); cpNext != null && getClone(cpNext) == null; cpNext = cpNext.getNext());
					cpClone.setPrev(getClone(cpNext));
					ControlPoint cpPrev;
					for (cpPrev = cpOriginal.getPrev(); cpPrev != null && getClone(cpPrev) == null; cpPrev = cpPrev.getPrev());
					cpClone.setNext(getClone(cpPrev));
				}
				
				/* set next attached and prev attached controlPoints */
				ControlPoint cpNextAttached;
				for (cpNextAttached = cpOriginal.getNextAttached(); cpNextAttached != null && getClone(cpNextAttached) == null; cpNextAttached = cpNextAttached.getNextAttached());
				cpClone.setNextAttached(getClone(cpNextAttached));
				ControlPoint cpPrevAttached;
				for (cpPrevAttached = cpOriginal.getPrevAttached(); cpPrevAttached != null && getClone(cpPrevAttached) == null; cpPrevAttached = cpPrevAttached.getPrevAttached());
				cpClone.setPrevAttached(getClone(cpPrevAttached));
			}
		}
	}

	/**
	* cloneHooks
	**/
	protected void cloneHooks() {
		ArrayList childHooks = new ArrayList();
		for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cpClone = (ControlPoint) it.next();
			ControlPoint cpOriginal = getOriginal(cpClone);
			//if (cpClone != cpOriginal) {
				/* set parent and child hooks */
				//System.out.println(cpClone + " " + cpOriginal + " " + cpOriginal.getChildHook());
				cpClone.setParentHook(getClone(cpOriginal.getParentHook()));
				if (cpOriginal.getChildHook() != null) {
					if (cpClone.getPrev() != null) {
					//cpClone.getPrev().setChildHook(getClone(cpOriginal.getChildHook()));
						childHooks.add(cpClone.getPrev());
						childHooks.add(getClone(cpOriginal.getChildHook()));
					}
				}
				
				/* set hook pos */
				if (cpOriginal.getHookPos() != -1) {
					addEdit(new AtomicChangeControlPoint.HookPos(cpClone, 1 - cpOriginal.getHookPos()));
				}
			//}
		}
		for (Iterator it = childHooks.iterator(); it.hasNext(); ) {
			addEdit(new AtomicChangeControlPoint.ChildHook((ControlPoint) it.next(), (ControlPoint) it.next()));
		}
		
		for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cpClone = (ControlPoint) it.next();
			//ControlPoint cpOriginal = getOriginal(cpClone);
			//if (cpClone == cpOriginal) {
				/* correct child hooks (as hook curves have been reversed */
				ControlPoint cp = cpClone;
				if (cp.getChildHook() != null) {
					addEdit(new AtomicChangeControlPoint.ChildHook(cp,cp.getChildHook().getStart()));
				}
			//}
		}
			
	}
	
	/**
	* add cloned curves to model
	**/
	protected void cloneCurves() {
		//ArrayList curvesToReverse = new ArrayList();
		for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cpClone = (ControlPoint) it.next();
			ControlPoint cpOriginal = getOriginal(cpClone);
			if (cpClone != cpOriginal) {
				/* check for a loop */
				if (cpOriginal.getLoop()) {
					boolean bLoop = false;
					loop:
					for (ControlPoint cp = cpClone.getNext(); cp != null; cp = cp.getNext()) {
						if (cp == cpClone) {
							bLoop = true;
							break loop;
						}
					}
					cpClone.setLoop(bLoop);
				}
                        	
				/* if we are the start, add the curve */
				if (cpClone.getLoop() || cpClone.getPrev() == null) {
					if (cpClone.getNext() != null) {
						addEdit(new AtomicAddCurve(cpClone));
					} else {
						System.err.println("Error in CloneCommonEdit - attempted to add invalid curve");
					}
				}
			}
		}
		//for (Iterator it = curvesToReverse.iterator(); it.hasNext(); ) {
		//	((Curve) it.next()).reverse();
		//}
	}

	/**
	* create new selection
	**/
	protected Selection createNewSelection() {
		ArrayList list = new ArrayList(); 
		for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cpClone = (ControlPoint) it.next();
			if (cpClone.isHead()) {
				list.add(cpClone);
			}
		}
		return new Selection(list);
	}

	/**
	* clone patches
	**/
	protected void clonePatches() {
		clonePatches(null);
	}
	
	protected void clonePatches(List mirror) {
		List list = new ArrayList();
		Model model = MainFrame.getInstance().getModel();
		for (Iterator it = model.getPatchSet().iterator(); it.hasNext(); ) {
			Patch patch = (Patch) it.next();
			ControlPoint[] acpOriginalPatch = patch.getControlPoints();
			ControlPoint[] acpClonePatch = new ControlPoint[acpOriginalPatch.length];
			boolean addPatch = true;
			loop:
			for (int n = 0; n < acpOriginalPatch.length; n++) {
				//System.out.println(acpOriginalPatch[n] + " " + getClone(acpOriginalPatch[n]));
				if (getClone(acpOriginalPatch[n]) == null) {
					if (mirror != null && mirror.contains(acpOriginalPatch[n])) {
						acpClonePatch[n] = acpOriginalPatch[n];
					} else {
						//System.out.println("xxx");
						addPatch = false;
						break loop;
					}
				} else {
					acpClonePatch[n] = getClone(acpOriginalPatch[n]);
				}
			}
			if (addPatch) {
				list.add(acpClonePatch);
				list.add(patch.getMaterial());
				if (acpClonePatch.length == 10) {
					ControlPoint[] acp5 = new ControlPoint[] {
						acpClonePatch[0].trueHead(),
						acpClonePatch[2].trueHead(),
						acpClonePatch[4].trueHead(),
						acpClonePatch[6].trueHead(),
						acpClonePatch[8].trueHead()
						};
					model.getCandidateFivePointPatchList().add(acp5);
				}
			}
		}
		for (Iterator it = list.iterator(); it.hasNext(); ) {
			Patch patch = new Patch((ControlPoint[]) it.next());
			patch.setMaterial((JPatchMaterial) it.next());
			addEdit(new AtomicAddPatch(patch));
			if(mirror != null) patch.flip();
		}
	}
	
	/**
	* creates a new cloned ControlPoint and adds it to the maps
	**/
	protected void addToCloneMap(ControlPoint cp) {
		//System.out.println(cp + " added to clone map");
		ControlPoint clone = new ControlPoint(cp);
		mapClones.put(cp,clone);
		mapOriginals.put(clone,cp);
	}

	/**
	* returns the original controlPoint for a clone
	**/
	protected ControlPoint getOriginal(ControlPoint clone) {
		return (ControlPoint) mapOriginals.get(clone);
	}
	
	/**
	* returns the clone for a original controlPoint
	**/
	protected ControlPoint getClone(ControlPoint original) {
		return (ControlPoint) mapClones.get(original);
	}
	
	/**
	* checks if a direct neighbot is part of the selection
	**/
	protected boolean checkForNeighbor(ControlPoint cp) {
		return ((cp.getNext() != null && JPatchUtils.arrayContains(acp,cp.getNext().getHead())) || (cp.getPrev() != null && JPatchUtils.arrayContains(acp,cp.getPrev().getHead())));
	}

	/**
	* checks if start cp of a hook curve should be added
	**/
	protected boolean checkStartHook(ControlPoint cp) {

		/* check if end of hook-curve is part of the selection */
		if (JPatchUtils.arrayContains(acp, cp.getEnd().getHead())) {

			/* we need at least one more selected point on the hook curve */
			/* so let's search for one... */
			for (ControlPoint cpHookCurve = cp; cpHookCurve.getNext() != null; cpHookCurve = cpHookCurve.getNext()) {

				/* check if the point and its direct neighbor are part of the selection too */
				if (JPatchUtils.arrayContains(acp,cpHookCurve) && cpHookCurve.getPrevAttached() != null && checkForNeighbor(cpHookCurve.getPrevAttached())) {
					return true;
				}
			}
		}
		return false;
	}
}

