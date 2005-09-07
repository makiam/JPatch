package jpatch.control.edit;

import java.util.*;
import javax.swing.*;
import javax.vecmath.*;

import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public class AutoMirrorEdit extends CloneCommonEdit {
	
	protected List mirrorList = new ArrayList();
	
	public AutoMirrorEdit(PointSelection ps) {
		super(ps.getControlPointArray());
		//System.out.println("automirror");
		int side = 0;
		boolean valid = true;
		loop:
		for (int i = 0; i < acp.length; i++) {
			if (!acp[i].isHook()) {
				Point3f pos = acp[i].getPosition();
				if (side == 0) {
					if (pos.x < 0) side = -1;
					if (pos.x > 0) side = 1;
				} else if (side == -1 && pos.x > 0) {
					valid = false;
					break loop;
				} else if (side == 1 && pos.x < 0) {
					valid = false;
					break loop;
				}
			}
		}
		if (!valid) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Found points on both sides, automirror operation canceled!", "Can't automirror", JOptionPane.ERROR_MESSAGE);
		} else {
			
			/* convert all hooks on boundary curves to regular controlpoints... */
			for (Curve curve = MainFrame.getInstance().getModel().getFirstCurve(); curve != null; curve = curve.getNext()) {
				
				/* check if this is a boundary curve */
				boolean boundary = true;
				loop:
				for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
					if (cp.getPosition().x != 0) {
						boundary = false;
						break loop;
					}
				}
				
				if (boundary) {
					for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
					
						/* check if we are start of a hook curve, and if both, start and end are on the boundary */
						if (ps.contains(cp.getHead()) && cp.getChildHook() != null && cp.getNext() != null && ps.contains(cp.getNext().getHead())) { //&& cp.getPosition().x == 0 && cp.getNext().getPosition().x == 0) {
							
							/* convert hooks... */
							ControlPoint startHook = cp.getChildHook();
							ControlPoint endHook = startHook.getEnd();
							ControlPoint cpToAppend = cp;
							ControlPoint cpEnd = cp.getNext();
							Curve hookCurve = cp.getChildHook().getCurve();
							for (ControlPoint hook = cp.getChildHook().getNext(); hook.getHookPos() < 1;) {
								Point3f position = hook.getPosition();
								ControlPoint next = hook.getNext();
								//System.out.println(hook);
								/* remove hook from hook curve */
								addEdit(new AtomicRemoveControlPointFromCurve(hook));
								
								/* and insert it on the parent curve */
								addEdit(new AtomicInsertControlPoint(hook, cpToAppend));
								
								/* and convert it to a regular controlpoint by setting hookpos to -1 */
								addEdit(new ChangeCPHookPosEdit(hook,-1));
								
								hook.setPosition(position);
								//hook.fixPosition();
								
								/* add "new" cp to all relevant selections */
								ArrayList listAdd = new ArrayList();
								ArrayList listRemove = new ArrayList();
								listAdd.add(hook);
								listRemove.add(startHook);
								listRemove.add(endHook);
								for (Iterator it = MainFrame.getInstance().getModel().getSelections().iterator(); it.hasNext(); ) {
									PointSelection psLoop = (PointSelection) it.next();
									if (psLoop.contains(cpEnd) && ps.contains(cpEnd.getPrev())) {
										addEdit(new AddControlPointsToSelectionEdit(psLoop, listAdd));
									}
									if (psLoop.contains(startHook) && psLoop.contains(endHook)) {
										addEdit(new RemoveControlPointsFromSelectionEdit(psLoop, listRemove));
									}
								}
								addEdit(new AddControlPointsToSelectionEdit(ps, listAdd));
								addEdit(new RemoveControlPointsFromSelectionEdit(ps, listRemove));
								cpToAppend = hook;
								hook = next;
							}
							
							/* remove the hook-curve */
							addEdit(new AtomicRemoveCurve(hookCurve));
							addEdit(new ChangeCPChildHookEdit(cp, null));
							
							/* correct patches */
							ArrayList patches;
							patches = MainFrame.getInstance().getModel().getPatchesContaining(startHook);
							for (Iterator it = patches.iterator(); it.hasNext(); ) {
								Patch patch = (Patch) it.next();
								ControlPoint[] patchCPs = patch.getControlPoints();
								for (int i = 0; i < patchCPs.length; i++) {
									if (patchCPs[i] == startHook) {
										patchCPs[i] = cp;
									}
								}
							}
							patches = MainFrame.getInstance().getModel().getPatchesContaining(endHook);
							for (Iterator it = patches.iterator(); it.hasNext(); ) {
								Patch patch = (Patch) it.next();
								ControlPoint[] patchCPs = patch.getControlPoints();
								for (int i = 0; i < patchCPs.length; i++) {
									if (patchCPs[i] == endHook) {
										patchCPs[i] = cpEnd;
									}
								}
							}
						}
					}
				}
			}
			
			
			
			/* clone points */
			buildCloneMap(true);
			cloneControlPoints();
			cloneCurves();
			
			for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
				ControlPoint cpClone = (ControlPoint) it.next();
				ControlPoint cpOriginal = getOriginal(cpClone);
				if (cpClone == cpOriginal) {
					ControlPoint cp = cpClone;
					if (cp.getNext() == null) {
						addEdit(new ChangeControlPointNextEdit(cp,getClone(cp.getPrev())));
					} else {
						addEdit(new ChangeControlPointPrevEdit(cp,getClone(cp.getNext())));
						//addEdit(new ChangeControlPointLoopEdit(cp.getCurve().getStart(),true));
					}
					
					
				}
			}
			
			
			
			for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
				ControlPoint cpClone = (ControlPoint) it.next();
				if (cpClone.getPosition().x == 0 && cpClone.getNext() != null && cpClone.getNext().getPosition().x == 0) {
					//System.out.println("something went wrong in automirror!");
				} else if (cpClone.isHead()) {
					Point3f p3 = cpClone.getPosition();
					p3.x = -p3.x;
					cpClone.setPosition(p3);
					//cpClone.fixPosition();
				}
			}
			
			/* remove dead curves */
			for (Curve curve = MainFrame.getInstance().getModel().getFirstCurve(); curve != null; curve = curve.getNext()) {
				if (curve.getStart().getCurve() != curve) {
					addEdit(new AtomicRemoveCurve(curve));
				}
			}
			
			/* add loop flags */
			for (Curve curve = MainFrame.getInstance().getModel().getFirstCurve(); curve != null; curve = curve.getNext()) {
				ControlPoint cp = curve.getStart();
				if (!cp.getLoop()) {
					ControlPoint cpStart = cp;
					cp = cp.getNext();
					while (cp != null && cp != cpStart) cp = cp.getNext();
					if (cp == cpStart) {
						addEdit(new ChangeControlPointLoopEdit(cp, true));
					}
				}
			}
			
			/* validate all curves */
			for (Curve curve = MainFrame.getInstance().getModel().getFirstCurve(); curve != null; curve = curve.getNext()) {
				curve.validate();
			}
			
			cloneHooks();
			
			
			
			clonePatches(mirrorList);
			
			/*
			 * mirror morphs
			 */
			ArrayList newMorphList = new ArrayList();
			for (Iterator it = MainFrame.getInstance().getModel().getMorphList().iterator(); it.hasNext(); ) {
				Morph morph = (Morph) it.next();
				
				boolean expand = false;
				boolean mirror = true;
				for (Iterator jt = morph.getPointList().iterator(); jt.hasNext(); ) {
					ControlPoint cp = (ControlPoint) jt.next();
					if (!mapClones.containsKey(cp) && !mirrorList.contains(cp))
						mirror = false;
					else if (mapClones.get(cp) == cp || mirrorList.contains(cp))
						expand = true;
				}
				if (mirror) {
					List pointList = morph.getPointList();
					List vectorList = morph.getVectorList();
					if (expand) {
						ArrayList newPointList = new ArrayList();
						ArrayList newVectorList = new ArrayList();
						for (int i = 0, n = pointList.size(); i < n; i++) {
							ControlPoint cp = (ControlPoint) pointList.get(i);
							ControlPoint clone = (ControlPoint) mapClones.get(cp);
							if (cp != clone && !mirrorList.contains(cp)) {
								Vector3f vector = new Vector3f((Vector3f) vectorList.get(i));
								vector.x = -vector.x;
								newPointList.add(clone);
								newVectorList.add(vector);
							}
						}
						if (newPointList.size() > 0)
							addEdit(new AddPointsToMorphEdit(morph, newPointList, newVectorList));
					} else {
						Morph newMorph = new Morph(morph.getNodeType(), "mirrored " + morph.getName());
						for (int i = 0, n = pointList.size(); i < n; i++) {
							ControlPoint cp = (ControlPoint) pointList.get(i);
							ControlPoint clone = (ControlPoint) mapClones.get(cp);
							Vector3f vector = new Vector3f((Vector3f) vectorList.get(i));
							vector.x = -vector.x;
							newMorph.add(clone, vector);
							newMorph.setMax(morph.getMax());
							newMorph.setMin(morph.getMin());
							newMorph.setSliderValue(morph.getSliderValue());
						}
						newMorphList.add(newMorph);
					}
				}
			}
			for (Iterator it = newMorphList.iterator(); it.hasNext(); addEdit(new AddMorphEdit((Morph) it.next())));
			
			/*
			* mirror selections
			*/
			//System.out.println(mirrorList);
			ArrayList newSelections = new ArrayList();
			for (Iterator it = MainFrame.getInstance().getModel().getSelections().iterator(); it.hasNext(); ) {
				ps = (PointSelection) it.next();
				ControlPoint[] acp = ps.getControlPointArray();
				boolean mirror = true;
				boolean expand = false;
				for (int c = 0; c < acp.length; c++) {
					if (!mapClones.containsKey(acp[c]) && !mirrorList.contains(acp[c])) {
						mirror = false;
					} //else if (mirrorList.contains(acp[c])) {
					//else if (acp[c].getPosition().x == 0) {
					else if (mapClones.get(acp[c]) == acp[c] || mirrorList.contains(acp[c])) {
						expand = true;
					}
				}
				//System.out.println(ps.getName() + "\t" + mirror + "\t" + expand);
				if (mirror) {
					if (expand) {
						//System.out.println("expand " + ps.getName());
						ArrayList pointsToAdd = new ArrayList();
						for (int c = 0; c < acp.length; c++) {
							//if (!mirrorList.contains(acp[c])) {
							//if (acp[c].getPosition().x != 0) {
							if (mapClones.get(acp[c]) != acp[c] && !mirrorList.contains(acp[c])) {
								//System.out.println(acp[c] + " " + mapClones.get(acp[c]));
								pointsToAdd.add(mapClones.get(acp[c]));
							}
						}
						if (pointsToAdd.size() > 0) {
							addEdit(new AddControlPointsToSelectionEdit(ps, pointsToAdd));
						}
					} else {
						PointSelection selection = new PointSelection();
						for (int c = 0; c < acp.length; c++) {
							selection.addControlPoint((ControlPoint) mapClones.get(acp[c]));
						}
						if (ps.getName().startsWith("*"))
							selection.setName("*mirrored " + ps.getName().substring(1));
						else
							selection.setName("mirrored " + ps.getName());
						newSelections.add(selection);
					}
				}
			}
			for (Iterator it = newSelections.iterator(); it.hasNext(); ) {
				addEdit(new AddSelectionEdit((Selection) it.next()));
			}
			
			//for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			//	ControlPoint cpClone = (ControlPoint) it.next();
			//	ControlPoint cpOriginal = getOriginal(cpClone);
			//	if (cpClone != cpOriginal) {
			//		//if (!cpClone.getCurve().getStart().getLoop()) {
			//			ControlPoint cp = cpClone.getNext();
			//			cpClone.setNext(cpClone.getPrev());
			//			cpClone.setPrev(cp);
			//		//}
			//	}
			//}
			
			
			//MainFrame.getInstance().getModel().dump();
			
			////clonePatches();
			//for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			//	ControlPoint cpClone = (ControlPoint) it.next();
			//	if (cpClone.getPosition().x == 0 && cpClone.getNext() != null && cpClone.getNext().getPosition().x == 0) {
			//		addEdit(new RemoveCurveSegmentEdit(cpClone));
			//	} else if (cpClone.isHead()) {
			//		Point3f p3 = cpClone.getPosition();
			//		p3.x = -p3.x;
			//		cpClone.setPosition(p3);
			//		cpClone.fixPosition();
			//	}
			//}
			//for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			//	ControlPoint cpClone = (ControlPoint) it.next();
			//	if (cpClone.getPosition().x == 0 && !cpClone.isHook() && cpClone.isSingle()) {
			//		System.out.println(cpClone.getNext() + "," + cpClone.getPrev());
			//		ControlPoint cpOriginal = getOriginal(cpClone);
			//		if ((cpOriginal.getNext() == null || cpOriginal.getNext().getPosition().x != 0) && (cpOriginal.getPrev() == null || cpOriginal.getPrev().getPosition().x != 0)) {
			//			
			//			//System.out.println(cpClone + " " + cpOriginal);
			//			addEdit(new ComplexAppendControlPointsEdit(cpClone,cpOriginal));
			//		} //else System.out.println("*");
			//	}
			//}
		}
	}
	/**
	* checks all selected ControlPoints (those in acp), and if they are really to be
	* cloned, adds them to mapCPs
	**/
	protected void buildCloneMap(boolean hooks) {
		for (int i = 0; i < acp.length; i++) {
			ControlPoint[] acpStack = acp[i].getStack();
			for (int s = 0; s < acpStack.length; s++) {
				ControlPoint cp = acpStack[s];
        
				/*
				* check if a hook-curve should be added
				*/
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
					if (cp.getPosition().x != 0) {
						addToCloneMap(cp);
					} else {
						//System.out.print(cp + " ");
						if ((cp.getNext() != null && cp.getPrev() == null && cp.getNext().getPosition().x != 0) || (cp.getPrev() != null && cp.getNext() == null && cp.getPrev().getPosition().x != 0)) {
							/* if the cp is not single, check if the attached curve is entrirely on the mirror plane */
							if (cp.isSingle()) {
								//System.put.println(cp + " single");
								mapClones.put(cp,cp);
								mapOriginals.put(cp,cp);
							} else if (cp.getNextAttached() == null && cp.getPrevAttached() != null && cp.getPrevAttached().getPrevAttached() == null) {
								//System.put.println(cp + " + prev attched curve");
								boolean onMirror = true;
								loop:
								for (ControlPoint c = cp.getPrevAttached().getCurve().getStart(); c != null; c = c.getNextCheckNextLoop()) {
									if (c.getPosition().x != 0) {
										onMirror = false;
										break loop;
									}
								}
								if (onMirror) {
									mapClones.put(cp,cp);
									mapOriginals.put(cp,cp);
								} else {
									addToCloneMap(cp);
								}
							} else if (cp.getPrevAttached() == null && cp.getNextAttached() != null && cp.getNextAttached().getNextAttached() == null) {
								//System.put.println(cp + " next attached curve");
								boolean onMirror = true;
								loop:
								for (ControlPoint c = cp.getNextAttached().getCurve().getStart(); c != null; c = c.getNextCheckNextLoop()) {
									if (c.getPosition().x != 0) {
										onMirror = false;
										break loop;
									}
								}
								if (onMirror) {
									mapClones.put(cp,cp);
									mapOriginals.put(cp,cp);
								} else {
									addToCloneMap(cp);
								}
							} else {
								addToCloneMap(cp);
							}
							
							//System.out.println("self");
						} else {
							boolean onMirror = true;
							loop:
							for (ControlPoint c = cp.getCurve().getStart(); c != null; c = c.getNextCheckNextLoop()) {
								if (c.getPosition().x != 0) {
									onMirror = false;
									break loop;
								}
							}
							if (onMirror) {
								mirrorList.add(cp);
								//System.out.println("mirror");
							} else {
								addToCloneMap(cp);
								//System.out.println("clone");
							}
						}
					}
				}
			}
		}
	}
}

