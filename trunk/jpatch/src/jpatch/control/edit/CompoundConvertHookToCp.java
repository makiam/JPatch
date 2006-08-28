package jpatch.control.edit;


import javax.vecmath.*;
import jpatch.entity.*;


public class CompoundConvertHookToCp extends JPatchCompoundEdit {

	public CompoundConvertHookToCp(OLDControlPoint hook) {
//		ControlPoint targetHook = hook.getPrevAttached();
		OLDControlPoint startHook = hook.getStart();
		OLDControlPoint endHook = hook.getEnd();
		OLDControlPoint parentHook = startHook.getParentHook();
		OLDControlPoint prevHook = hook.getPrev();
		OLDControlPoint nextHook = hook.getNext();
		Point3f position = hook.getPosition();
		float hookPos = hook.getHookPos();
		
		/* remove hook from hook curve */
		addEdit(new AtomicRemoveControlPointFromCurve(hook));
		
		/* and insert it on the parent curve */
		addEdit(new AtomicInsertControlPoint(hook, parentHook));
		
		/* and convert it to a regular controlpoint by setting hookpos to -1 */
		addEdit(new AtomicChangeControlPoint.HookPos(hook,-1));
		
		/* set position */
		hook.setPosition(position);
		
		if (prevHook == startHook && nextHook == endHook) {
			
			/* remove hook curve */
			//addEdit(new RemoveCurveFromModelEdit(startHook.getCurve()));
			addEdit(new AtomicChangeControlPoint.ChildHook(parentHook, null));
			addEdit(new CompoundDeleteControlPoint(startHook));
		} else if (prevHook == startHook) {
			
			/* modify hook curve */
			addEdit(new AtomicChangeControlPoint.ParentHook(startHook, hook));
			addEdit(new AtomicChangeControlPoint.ChildHook(parentHook, null));
			addEdit(new AtomicChangeControlPoint.ChildHook(hook, startHook));
			for (OLDControlPoint cp = startHook; cp != null; cp = cp.getNext()) {
				float hp = cp.getHookPos();
				if (hp > 0 && hp < 1)
					addEdit(new AtomicChangeControlPoint.HookPos(cp, (hp - hookPos) / ( 1 - hookPos)));
			}
//			System.out.println("b");
		} else if (nextHook == endHook ){
			
			/* modify hook curve */
			addEdit(new AtomicChangeControlPoint.ParentHook(endHook, hook));
			for (OLDControlPoint cp = startHook; cp != null; cp = cp.getNext()) {
				float hp = cp.getHookPos();
				if (hp > 0 && hp < 1)
					addEdit(new AtomicChangeControlPoint.HookPos(cp, hp / hookPos));
			}
//			System.out.println("a");
		} else {
			
			/* add a new hook curve and modify curves */
			OLDControlPoint newStartHook = new OLDControlPoint();
			OLDControlPoint newEndHook = new OLDControlPoint();
			newEndHook.appendTo(newStartHook);
			newStartHook.setParentHook(hook);
			newStartHook.setHookPos(0);
			newEndHook.setParentHook(endHook.getParentHook());
			newEndHook.setHookPos(1);
			addEdit(new AtomicChangeControlPoint.ParentHook(endHook, hook));
			addEdit(new AtomicChangeControlPoint.ChildHook(hook, newStartHook));
			OLDControlPoint cpAppend = newStartHook;
			for (OLDControlPoint cp = startHook; cp != null; cp = cp.getNext()) {
				float hp = cp.getHookPos();
				if (hp > 0 && hp < 1) {
					if (hp < hookPos) {
						addEdit(new AtomicChangeControlPoint.HookPos(cp, hp / hookPos));
					} else {
						addEdit(new AtomicChangeControlPoint.HookPos(cp, (hp - hookPos) / ( 1 - hookPos)));
						addEdit(new AtomicRemoveControlPointFromCurve(cp));
						addEdit(new AtomicInsertControlPoint(cp, cpAppend));
						cpAppend = cp;
					}
				}
			}
//			Curve curve = new Curve(newStartHook);
//			curve.validate();
			addEdit(new AtomicAddCurve(newStartHook));
//			System.out.println("c");
		}
	}
	
// TODO: correct patches and morphs!	

}
