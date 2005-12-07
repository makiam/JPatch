package jpatch.control.edit;

import java.util.*;
import javax.vecmath.*;
import jpatch.auxilary.*;
import jpatch.entity.*;
import jpatch.boundary.*;

/**
* Undoable Clone Edit
**/
public class CompoundLathe extends AbstractClone implements JPatchRootEdit {

	static private int iNum = 1;
	//private int iSegments = 8;
	/**
	* Creates and performes the edit
	* @param  A  ControlPoint A
	* @param  B  ControlPoint B
	*/
	public CompoundLathe(ControlPoint[] acp, int iSegments, float epsilon) {
		super(acp);
		/*
		 * create set of points to clone
		 */
		
		
		
		float mag = Functions.optimumCurvature(iSegments);
		float mag4 = Functions.optimumCurvature(4);
		
		Set setCPs = new HashSet();
		for (int i = 0; i < acp.length; i++) {
			ControlPoint[] stack = acp[i].getStack();
			for (int s = 0; s < stack.length; s++) {
				if (stack[s].isHook()) {
					//boolean add = true;
					//loop1:
					//for (ControlPoint cp = stack[s].getStart(); cp != null; cp = cp.getNext()) {
					//	boolean part = false;
					//	loop2:
					//	for (int p = 0; p < acp.length; p++) {
					//		if (acp[p] == cp) {
					//			part = true;
					//			break loop2;
					//		}
					//	}
					//	if (!part) {
					//		add = false;
					//		break loop1;
					//	}
					//}
					//if (add) {
					//	setCPs.add(stack[s]);
					//}
					System.out.println("lathe: hook selected!!!");
				} else if (checkForNeighbor(stack[s])) { 
					setCPs.add(stack[s]);
				}
			}
		}
		
		/*
		 * clone controlpoints
		 */
		HashMap mapCPs = new HashMap();
//		NewSelection selection = new NewSelection();
		ArrayList pointList = new ArrayList();
		
		/* store all the points in an array */
				
		ControlPoint[][] cpLathe = new ControlPoint[setCPs.size()][iSegments];
		int i = 0;
		for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
				cpLathe[i++][0] = (ControlPoint) it.next();
		}
		
		/* create cloned controlPoints */
		/* for each segment */
		
		for (int s = 1; s < iSegments; s++) {
			mapCPs.clear();
			for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
				ControlPoint cpToClone = (ControlPoint) it.next();
//				if (cpToClone.isStart() || cpToClone.isEnd()) {
//					float x = cpToClone.getReferencePosition().x;
//					float z = cpToClone.getReferencePosition().z;
//					if (x * x + z * z == 0)
//						continue;
//				}
				ControlPoint cpClone = new ControlPoint(cpToClone);
				mapCPs.put(cpToClone, cpClone);
			}
			
			/* connect cloned controlPoints */
			for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
				ControlPoint cpToClone = (ControlPoint) it.next();
				ControlPoint cpClone = (ControlPoint) mapCPs.get(cpToClone);
				if (cpClone == null)
					continue;
				cpClone.setNext((ControlPoint) mapCPs.get(cpToClone.getNext()));
				cpClone.setPrev((ControlPoint) mapCPs.get(cpToClone.getPrev()));
				cpClone.setNextAttached((ControlPoint) mapCPs.get(cpToClone.getNextAttached()));
				cpClone.setPrevAttached((ControlPoint) mapCPs.get(cpToClone.getPrevAttached()));
				cpClone.setParentHook((ControlPoint) mapCPs.get(cpToClone.getParentHook()));
				cpClone.setChildHook((ControlPoint) mapCPs.get(cpToClone.getChildHook()));
				cpClone.setHookPos(cpToClone.getHookPos());
			}
			
			/* check for loops, add curves to model*/
			for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
				ControlPoint cpToClone = (ControlPoint) it.next();
				ControlPoint cpClone = (ControlPoint) mapCPs.get(cpToClone);
				
				if (cpClone == null)
					continue;
				
				/* check if the cloned curve is closed */
				if (cpToClone.getLoop()) {
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
				
				/* add a curve if we are a start point AND have a next point */
				if ((cpClone.getLoop() || cpClone.getPrev() == null) && cpClone.getNext() != null) {
					//model.addCurve(cpClone);
//					Curve curve = new Curve(cpClone);
					addEdit(new AtomicAddCurve(cpClone));	// create a new curve
//					addEdit(new ValidateCurveEdit(curve)); // validate new curve
				}
				
				/* detach empty points */
				if (cpClone.getNext() == null && cpClone.getPrev() == null) {
					if (cpClone.getNextAttached() != null) {
						cpClone.getNextAttached().setPrevAttached(cpClone.getPrevAttached());
					}
					if (cpClone.getPrevAttached() != null) {
						cpClone.getPrevAttached().setNextAttached(cpClone.getNextAttached());
					}
				}
			}
			
			//Point3f box = new Point3f(ps.getCornerB());
			//box.sub(ps.getCornerA());
			//box.scale(0.1f);
			
			Matrix3f m3Rotation = new Matrix3f();
			m3Rotation.rotY(2f * (float) Math.PI * (float) s / iSegments);
			Point3f pos = new Point3f();
			for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
				ControlPoint cpToClone = (ControlPoint) it.next();
				ControlPoint cpClone = (ControlPoint) mapCPs.get(cpToClone);
				
				if (cpClone == null)
					continue;
				
				if (cpClone.isHead()) {
				//if (cpClone.isHead() && !cpClone.isHook()) {
					/* if we are a head, add us to the new selection */
					//ps.addControlPoint(cpClone);
					pos.set(cpToClone.getPosition());
					//pos.x += 5;
					//pos.y -= 5;
					m3Rotation.transform(pos);
					cpClone.setPosition(pos);
					//cpClone.setPosition(pos);
				}
			}
			
			i = 0;
			for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
				ControlPoint cpToClone = (ControlPoint) it.next();
				ControlPoint cpClone = (ControlPoint) mapCPs.get(cpToClone);
				
				if (cpClone == null)
					continue;
				
				if (cpClone.getPrev() != null || cpClone.getNext() != null) {
					cpLathe[i][s] = cpClone;
				}
				i++;
			}
			
			///* clone patches */
			//ArrayList newPatches = new ArrayList();
			//ArrayList newMaterials = new ArrayList();
			//for (Patch p = model.getFirstPatch(); p != null; p = p.getNext()) {
			//	ControlPoint[] acpOld = p.getControlPoints();
			//	ControlPoint[] acpNew = new ControlPoint[acpOld.length];
			//	boolean addPatch = true;
			//	loop:
			//	for (int n = 0; n < acpOld.length; n++) {
			//		if (mapCPs.get(acpOld[n]) == null) {
			//			addPatch = false;
			//			break loop;
			//		} else {
			//			acpNew[n] = (ControlPoint) mapCPs.get(acpOld[n]);
			//		}
			//	}
			//	if (addPatch) {
			//		newPatches.add(acpNew);
			//		newMaterials.add(p.getMaterial());
			//		if (acpNew.length == 10) {
			//			ControlPoint[] acp5 = new ControlPoint[] {
			//				acpNew[0].trueHead(),
			//				acpNew[2].trueHead(),
			//				acpNew[4].trueHead(),
			//				acpNew[6].trueHead(),
			//				acpNew[8].trueHead()
			//				};
			//			model.getCandidateFivePointPatchList().add(acp5);
			//		}
			//	}
			//}
			//Iterator itMat = newMaterials.iterator();
			//for (Iterator it = newPatches.iterator(); it.hasNext(); ) {
			//	Patch patch = new Patch((ControlPoint[]) it.next());
			//	patch.setMaterial((JPatchMaterial) itMat.next());
			//	addEdit(new AddPatchEdit(patch));
			//	//model.addPatch((ControlPoint[]) it.next());
			//	//model.getLastPatch().setMaterial((JPatchMaterial) itMat.next());
			//}
			
			///* add selection */
			//ps.setName("extruded points #" + iNum++);
			//addEdit(new ChangeSelectionEdit(ps));
			//addEdit(new AddSelectionEdit(ps));
			
			///* attach new point to extruded points */
			//for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
			//	ControlPoint cpToClone = (ControlPoint) it.next();
			//	ControlPoint cpClone = (ControlPoint) mapCPs.get(cpToClone);
			//	if (cpClone.isHead() && !cpClone.isHook()) {
			//		ControlPoint cpNew = new ControlPoint();
			//		//ControlPoint cpDummy = new ControlPoint();
			//		//ControlPoint cpEnd = cpToClone.getLooseEnd();
			//		cpNew.attachTo(cpClone);
			//		
			//		/* find a good loose end */
			//		ControlPoint cpEnd = null;
			//		if (!cpToClone.isSingle()) {
			//			ControlPoint[] acpStack = cpToClone.getStack();
			//			loop:
			//			for (int i = 0; i < acpStack.length; i++) {
			//				if (acpStack[i].getPrev() == null && acpStack[i].getNext() != null && !setCPs.contains(acpStack[i].getNext())) {
			//					cpEnd = acpStack[i];
			//					break loop;
			//				} else if (acpStack[i].getNext() == null && acpStack[i].getPrev() != null && !setCPs.contains(acpStack[i].getPrev())) {
			//					cpEnd = acpStack[i];
			//					break loop;
			//				}
			//			}
			//		}
			//		
			//		//ControlPoint cpNext = null;
			//		//if (cpEnd != null && cpEnd.isStart()) {
			//		//	cpNext = cpEnd.getNext();
			//		//} else if (cpEnd != null && cpEnd.isEnd()) {
			//		//	cpNext = cpEnd.getPrev();
			//		//}
			//		///* check if we have a valid loose end on the cloned cp */
			//		//System.out.println(cpNext + " " + setCPs.contains(cpNext));
			//		if (cpEnd == null) {
			//			//System.out.println("extrude new end");
			//			/* if not create a new cp, */
			//			cpEnd = new ControlPoint();
			//			
			//			/* attach it to the cpToClone */
			//			addEdit(new AttachControlPointsEdit(cpEnd,cpToClone.getTail()));
			//			
			//			/* and add it as a new curve */
			//			cpNew.appendTo(cpEnd);
			//			Curve curve = new Curve(cpEnd);
			//			curve.validate();
			//			addEdit(new CreateCurveEdit(curve));
			//		} else {
			//			//System.out.println("extrude old end");
			//			if (cpEnd.isEnd()) {
			//				addEdit(new ChangeControlPointNextEdit(cpEnd,cpNew));
			//				cpNew.setPrev(cpEnd);
			//				cpNew.setCurve(cpEnd.getCurve());
			//			} else if (cpEnd.isStart()) {
			//				addEdit(new ChangeControlPointPrevEdit(cpEnd,cpNew));
			//				cpNew.setNext(cpEnd);
			//				cpNew.setCurve(cpEnd.getCurve());
			//				addEdit(new ChangeCurveStartEdit(cpEnd.getCurve(),cpNew));
			//			} else {
			//				System.out.println("error in extrudeEdit");
			//			}
			//		}
			//	}
			//}
		}
		
		if (false)
			return;
		
			/* create lathe curves */
		int n = setCPs.size();
		ControlPoint[] newCp = new ControlPoint[iSegments];
		boolean[] addCircle = new boolean[n];
		boolean[] hookCurve = new boolean[n];
		for (int p = 0; p < n; p++) {
			ControlPoint cp = cpLathe[p][0]; // was ...=cpLath[p][1] !?
			//if (cp != null) {
			if (cp != null && cp.isHead()) {
				addCircle[p] = true;
				hookCurve[p] = false;
				System.out.println("cp=" + cp + " next=" + cp.getNext() + " prev=" + cp.getPrev());
				if (cp.isStart() || cp.isEnd()) {
					System.out.println("is start or end");
					float x = cp.getReferencePosition().x;
					float z = cp.getReferencePosition().z;
					if (x * x + z * z <= epsilon * epsilon) {
						addCircle[p] = false;
					}
				}
				if (cp.getNext() != null && cp.getNext().isEnd()) {
					System.out.println("next is end");
					float x = cp.getNext().getReferencePosition().x;
					float z = cp.getNext().getReferencePosition().z;
					if (x * x + z * z <= epsilon * epsilon) {
						addCircle[p] = false;
						hookCurve[p] = true;
					}
				}
				if (cp.getPrev() != null && cp.getPrev().isStart()) {
					System.out.println("prev is start");
					float x = cp.getPrev().getReferencePosition().x;
					float z = cp.getPrev().getReferencePosition().z;
					if (x * x + z * z <= epsilon * epsilon) {
						addCircle[p] = false;
						hookCurve[p] = true;
					}
				}
			}
		}
		for (int p = 0; p < n; p++) {
			ControlPoint cp = cpLathe[p][0]; // was ...=cpLath[p][1] !?
			//if (cp != null) {
			if (cp != null && cp.isHead()) {
				if (addCircle[p]) {
					for (int s = 0; s < iSegments; s++) {
						newCp[s] = new ControlPoint();
						//newCp[s].setMode(ControlPoint.JPATCH_G3);
						addEdit(new AtomicAttachControlPoints(newCp[s],cpLathe[p][s].getTail()));
						pointList.add(newCp[s].getHead());
					}
					for (int s = 0; s < iSegments; s++) {
						int prev = (s - 1 + iSegments) % iSegments;
						int next = (s + 1) % iSegments;
						newCp[s].setNext(newCp[next]);
						newCp[s].setPrev(newCp[prev]);
						newCp[s].setMagnitude(mag);
					}
					newCp[0].setLoop(true);
//					Curve curve = new Curve(newCp[0]);
//					curve.validate();
					addEdit(new AtomicAddCurve(newCp[0]));
				} else if (!hookCurve[p]) {
					if (iSegments % 2 == 0) {
						for (int s = 0; s < iSegments / 2; s += iSegments / 4) {
							//addEdit(new 
							addEdit(new CompoundWeldControlPoints(cpLathe[p][s],cpLathe[p][s + iSegments / 2]));
						}
						for (int s = iSegments / 2 + iSegments / 4; s < iSegments; s += iSegments / 4) {
							addEdit(new AtomicAttachControlPoints(cpLathe[p][s],cpLathe[p][s - iSegments / 4]));
						}
						for (int s = 0; s < iSegments; s++)
							if (s % (iSegments / 4) != 0)
								addEdit(new CompoundDeleteControlPoint(cpLathe[p][s]));
					
//						addEdit(new AtomicMoveControlPoints(new ControlPoint[] { cpLathe[p][iSegments / 2] } ));
//						cpLathe[p][iSegments / 2].setPosition(0,cpLathe[p][iSegments / 2].getPosition().y,0);
						//addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,new ControlPoint[] { cpLathe[p][iSegments / 2] } ));
						pointList.add(cpLathe[p][iSegments / 2]);
					} else {
//						for (int s = 1; s < iSegments; s++) {
//							addEdit(new AtomicAttachControlPoints(cpLathe[p][s],cpLathe[p][s - 1]));
//						}
//						addEdit(new AtomicMoveControlPoints(new ControlPoint[] { cpLathe[p][0] } ));
//						cpLathe[p][0].setPosition(0,cpLathe[p][0].getPosition().y,0);
//						//addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,new ControlPoint[] { cpLathe[p][0] } ));
//						pointList.add(cpLathe[p][0]);
					}
				} else {
					for (int s = 0; s < iSegments; s += iSegments / 4) {
						newCp[s] = new ControlPoint();
						//newCp[s].setMode(ControlPoint.JPATCH_G3);
						addEdit(new AtomicAttachControlPoints(newCp[s],cpLathe[p][s].getTail()));
						pointList.add(newCp[s].getHead());
					}
					ControlPoint startHook = null;
					for (int s = 0; s < iSegments; s ++) {
						int sm = s % (iSegments / 4);
						System.out.println("#####" + sm);
						if (sm == 0) {
							int prev = (s - iSegments / 4 + iSegments) % iSegments;
							int next = (s + iSegments / 4) % iSegments;
							newCp[s].setNext(newCp[next]);
							newCp[s].setPrev(newCp[prev]);
							newCp[s].setMagnitude(mag4);
							System.out.println(newCp[s]);
							startHook = newCp[s];
//							newCp[s].setChildHook(startHook);
						} else {
							System.out.println(cpLathe[p][s].getTail() + " hookto " + startHook + " " + (float) (s % (iSegments / 4)) / (iSegments / 4));
							
							addEdit(new CompoundHook(cpLathe[p][s].getTail(), startHook, (float) (s % (iSegments / 4)) / (iSegments / 4)));
							
//							cpLathe[p][s].getTail().hookTo(startHook, (float) (s % (iSegments / 4)) / (iSegments / 4));
						}
					}
					newCp[0].setLoop(true);
//					Curve curve = new Curve(newCp[0]);
//					curve.validate();
					addEdit(new AtomicAddCurve(newCp[0]));
				}
			}
		}
		/* add selection */
		Selection selection = new Selection(pointList);
		selection.setName("*lathe #" + iNum++);
		if (selection.getMap().size() > 0) {
			addEdit(new AtomicChangeSelection(selection.cloneSelection()));
			addEdit(new AtomicAddSelection(selection));
		}
	}
	
	public String getName() {
		return "lathe";
	}
}
