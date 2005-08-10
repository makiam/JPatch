package jpatch.control.edit;

import java.util.*;
import javax.vecmath.*;
import jpatch.auxilary.*;
import jpatch.entity.*;
import jpatch.boundary.selection.*;

/**
* Undoable Clone Edit
**/
public class NewLatheEdit extends CloneCommonEdit {

	static private int iNum = 1;
	//private int iSegments = 8;
	/**
	* Creates and performes the edit
	* @param  A  ControlPoint A
	* @param  B  ControlPoint B
	*/
	public NewLatheEdit(ControlPoint[] acp, int iSegments, float epsilon) {
		super(acp);
		/*
		 * create set of points to clone
		 */
		
		
		
		float mag = Functions.optimumCurvature(iSegments);
		
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
		PointSelection ps = new PointSelection();
		
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
				ControlPoint cpClone = new ControlPoint(cpToClone);
				mapCPs.put(cpToClone, cpClone);
			}
			
			/* connect cloned controlPoints */
			for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
				ControlPoint cpToClone = (ControlPoint) it.next();
				ControlPoint cpClone = (ControlPoint) mapCPs.get(cpToClone);
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
					Curve curve = new Curve(cpClone);
					addEdit(new CreateCurveEdit(curve));	// create a new curve
					addEdit(new ValidateCurveEdit(curve)); // validate new curve
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
			/* create lathe curves */
		int n = setCPs.size();
		ControlPoint[] newCp = new ControlPoint[iSegments];
		for (int p = 0; p < n; p++) {
			ControlPoint cp = cpLathe[p][1];
			//if (cp != null) {
			if (cp != null && cp.isHead()) {
				boolean addCircle = true;
				if (cp.isStart() || cp.isEnd()) {
					float x = cp.getPosition().x;
					float z = cp.getPosition().z;
					//System.out.println(x + " " + z + " " + (x*x+z*z));
					if (x * x + z * z < epsilon * epsilon) {
						//System.out.println("*");
						addCircle = false;
					}
				}
				
				if (addCircle) {
					for (int s = 0; s < iSegments; s++) {
						newCp[s] = new ControlPoint();
						//newCp[s].setMode(ControlPoint.JPATCH_G3);
						addEdit(new AttachControlPointsEdit(newCp[s],cpLathe[p][s].getTail()));
						ps.addControlPoint(newCp[s].getHead());
					}
					for (int s = 0; s < iSegments; s++) {
						int prev = (s - 1 + iSegments) % iSegments;
						int next = (s + 1) % iSegments;
						newCp[s].setNext(newCp[next]);
						newCp[s].setPrev(newCp[prev]);
						newCp[s].setMagnitude(mag);
					}
					newCp[0].setLoop(true);
					Curve curve = new Curve(newCp[0]);
					curve.validate();
					addEdit(new CreateCurveEdit(curve));
				} else {
					if (iSegments % 2 == 0) {
						for (int s = 0; s < iSegments / 2; s++) {
							//addEdit(new 
							addEdit(new WeldControlPointsEdit(cpLathe[p][s],cpLathe[p][s + iSegments / 2]));
						}
						for (int s = iSegments / 2 + 1; s < iSegments; s++) {
							addEdit(new AttachControlPointsEdit(cpLathe[p][s],cpLathe[p][s - 1]));
						}
						addEdit(new NewMoveControlPointsEdit(new ControlPoint[] { cpLathe[p][iSegments / 2] } ));
						cpLathe[p][iSegments / 2].setPosition(0,cpLathe[p][iSegments / 2].getPosition().y,0);
						//addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,new ControlPoint[] { cpLathe[p][iSegments / 2] } ));
						ps.addControlPoint(cpLathe[p][iSegments / 2]);
					} else {
						for (int s = 1; s < iSegments; s++) {
							addEdit(new AttachControlPointsEdit(cpLathe[p][s],cpLathe[p][s - 1]));
						}
						addEdit(new NewMoveControlPointsEdit(new ControlPoint[] { cpLathe[p][0] } ));
						cpLathe[p][0].setPosition(0,cpLathe[p][0].getPosition().y,0);
						//addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,new ControlPoint[] { cpLathe[p][0] } ));
						ps.addControlPoint(cpLathe[p][0]);
					}
				}
			}
		}
		/* add selection */
		ps.setName("*lathe #" + iNum++);
		if (ps.getSize() > 0) {
			addEdit(new ChangeSelectionEdit(ps));
			addEdit(new AddSelectionEdit(ps));
		}
	}
}
