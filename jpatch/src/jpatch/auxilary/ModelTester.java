package jpatch.auxilary;

import java.util.*;

import jpatch.entity.*;
import jpatch.boundary.*;

public class ModelTester {
	
	private ArrayList listCurves;
	private ArrayList listControlPoints;
	private boolean bSuccess;
	
	public boolean test(Model model) {
		
		bSuccess = true;
		/*
		* populate lists of curves and points
		*/
		listCurves  = new ArrayList(model.getCurveSet());
		listControlPoints = new ArrayList();
		
		for (Iterator it = listCurves.iterator(); it.hasNext(); ) {
			for(ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
				listControlPoints.add(cp);
			}
		}
		
		System.out.println(listCurves.size() + " curves");
		System.out.println(listControlPoints.size() + " control points");
		
		/*
		* perform tests on curves and points
		*/
		curveLoop:
		for (Iterator it = listCurves.iterator(); it.hasNext(); ) {
			
//			// check if model is set
//			if (curve.getModel() != model) {
//				error("curve " + curve + " model is " + curve.getModel());
//			}
			
			ControlPoint cp = (ControlPoint) it.next();
			ControlPoint start = cp;
			
			// check if we have a start
			if (cp == null) {
				error("Curve start is null");
				break curveLoop;
			}
			
			// check if start ist first point or has loop flag set
			if (cp.getPrev() != null && !cp.getLoop()) {
				error("Start of curve " + cp + " is not the first cp, but loop is false");
				break curveLoop;
			}
			
			// check if there is at least one more controlpoint on this curve
			if (cp.getNext() == null) {
				error("Curve starting with " + cp + " has just one controlPoint");
				break curveLoop;
			}
			
			ControlPoint cpLast = cp.getPrev();
			
			// loop over controlPoints
			while (cp != null) {
				
//				// check if the cp belongs to the curve
//				if (cp.getCurve() != curve) {
//					error("Cp " + cp + " curve pointer points to wrong curve");
//				}
				
				// check if cp is deleted
				if (cp.isDeleted()) {
					error("Cp " + cp + " has deleted flag set");
				}
				
				// check if prev cp is ok
				if (cp.getPrev() != cpLast) {
					error("Cp " + cp + " cpLast pointer is wrong");
				}
				
				//check if loop is set
				if (cp.getLoop() == true && cp != start) {
					error("Cp " + cp + " has loop flag set but is not start of curve!");
				}
				
				//check if attached an hooks are known
				if (cp.getNextAttached() != null && !listControlPoints.contains(cp.getNextAttached())) {
					error("Cp " + cp + " cpNextAttached is unknown");
				}
				if (cp.getPrevAttached() != null && !listControlPoints.contains(cp.getPrevAttached())) {
					error("Cp " + cp + " cpPrevAttached is unknown");
				}
				if (cp.getParentHook() != null && !listControlPoints.contains(cp.getParentHook())) {
					error("Cp " + cp + " cpParentHook is unknown");
				}
				if (cp.getChildHook() != null && !listControlPoints.contains(cp.getChildHook())) {
					error("Cp " + cp + " cpChildHook is unknown");
				}
				
				//check if we are attached to a hook
				if (cp.getNextAttached() != null && cp.getNextAttached().getHookPos() != -1) {
					
					//check if we have no prevAttached
					if (cp.getPrevAttached() != null) {
						error("Target-Hook " + cp + " has cpPrev!");
					}
					
					//check if we are start or end of curve
					if (cp.getNext() != null && cp.getPrev() != null) {
						error("Target-Hook " + cp + " is not start or end of curve");
					}
				}
				
				//do we have a child hook
				if (cp.getChildHook() != null) {
					
					//does it point back to us
					if (cp.getChildHook().getParentHook() != cp) {
						error("Child hook of " + cp + " does not point back!");
					}
				}
				
				//check if we are a hook
				if (cp.getHookPos() != -1) {
					
					//check if we are the start hook
					if (cp.getHookPos() == 0) {
						
						ControlPoint parentHook = cp.getParentHook();
						
						// are we the start of the curve?
						if (cp.getPrev() != null) {
							error ("Start-Hook " + cp + " is not start of curve!");
						}
						
						//do we have a parent hook
						if (parentHook == null) {
							error("Start-Hook " + cp + " has no parent hook!");
						} else {
							
							//check if parent hook has child hook pointer back to us
							if (parentHook.getChildHook() != cp) {
								error("Start-Hook " + cp + ": parent hook does not point back to us");
							}
							
							// check if end hook points to the right cp
							if (cp.getEnd().getParentHook() != parentHook.getNext()) {
								error("Hook curve " + cp + ": parent hook of curve end is wrong");
							}
						}
						
						// we must have no attached points [
						if (cp.getNextAttached() != null) {
							error("Start-Hook " + cp + " has next attached point!");
						}
						if (cp.getPrevAttached() != null) {
							error("Start-Hook " + cp + " has prev attached point!");
						}
					}
					
					// check if we are the end hook
					else if (cp.getHookPos() == 1) {
						
						//do we have a parent hook
						if (cp.getParentHook() == null) {
							error("End-Hook " + cp + " has no parent hook!");
						}
						
						//we must have no attached points [
						if (cp.getNextAttached() != null) {
							error("End-Hook " + cp + " has next attached point!");
						}
						if (cp.getPrevAttached() != null) {
							error("End-Hook " + cp + " has prev attached point!");
						}
					}
					
					else {
						
						// normal hook, must not have next-attached, must have prev-attached
						if (cp.getNextAttached() != null) {
							error("Hook " + cp + " has next attached point!");
						}
						if (cp.getPrevAttached() == null) {
							error("Hook " + cp + " has no prev attached point!");
						}
					}
				} else {
					
					//we're no hook, check if parent hook == null
					if (cp.getParentHook() != null) {
						error ("cp " + cp + " is not a hook, but parentHook is not null");
					}
				}
				cpLast = cp;
				cp = cp.getNextCheckNextLoop();
			}
				
					
		}
		
		/*
		* perform tests on selections
		*/
		int selections = 0;
		for (Iterator it = model.getSelections().iterator(); it.hasNext(); ) {
			Selection selection = (Selection) it.next();
			for (Iterator it2 = selection.getObjects().iterator(); it2.hasNext(); ) {
				Object object = it2.next();
				if (object instanceof ControlPoint) {
					ControlPoint cp = (ControlPoint) object;
					if (!listControlPoints.contains(cp)) {
						error ("cp " + cp + " of selection " + selection.getName() + " is invalid");
					}
					if (!cp.isHead()) {
						error ("cp " + cp + " of selection " + selection.getName() + " is not a head");
					}
				}
			}
			selections++;
		}
		System.out.println(selections + " selections");
		
		/*
		* perform tests on patches
		*/
		int patches = 0;
		for (Iterator it = model.getPatchSet().iterator(); it.hasNext(); ) {
			Patch patch = (Patch) it.next();
			ControlPoint[] acp = patch.getControlPoints();
			for (int c = 0; c < acp.length; c++) {
				if (!listControlPoints.contains(acp[c])) {
					error ("cp " + acp[c] + " in patch " + patch + " is invalid");
				}
			}
			patches++;
		}
		System.out.println(patches + " patches");
		
		/*
		* perform tests on morphs
		*/
		
		int morphs = 0;
		
		for (Iterator itMorph = MainFrame.getInstance().getModel().getMorphList().iterator(); itMorph.hasNext(); ) {
			Morph morph = (Morph) itMorph.next();
			for (Iterator itTarget = morph.getTargets().iterator(); itTarget.hasNext(); ) {
				MorphTarget target = (MorphTarget) itTarget.next();
				for (Iterator itCp = target.getMorphMap().keySet().iterator(); itCp.hasNext(); ) {
					ControlPoint cp = (ControlPoint) itCp.next();
					if (!listControlPoints.contains(cp)) {
						error ("cp " + cp + " of morph " + morph.getName() + " is invalid");
					}
					if (!cp.isHead()) {
						error ("cp " + cp + " of morph " + morph.getName() + " is not a head");
					}
					if (target.getVectorFor(cp) == null) {
						error ("cp " + cp + " of morph " + morph.getName() + " has null vector");
					}
				}
			}
			morphs++;
		}
		System.out.println(morphs + " morphs");
		
		return bSuccess;
	}
	
	private void error(String errorString) {
		System.err.println(errorString);
		bSuccess = false;
	}
}

