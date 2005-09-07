package jpatch.control.edit;


import javax.vecmath.*;
import jpatch.entity.*;


public class ConvertHookToCpEdit extends JPatchCompoundEdit {

	public ConvertHookToCpEdit(ControlPoint hook) {
//		ControlPoint targetHook = hook.getPrevAttached();
		ControlPoint startHook = hook.getStart();
		ControlPoint endHook = hook.getEnd();
		ControlPoint parentHook = startHook.getParentHook();
		ControlPoint prevHook = hook.getPrev();
		ControlPoint nextHook = hook.getNext();
		Point3f position = hook.getPosition();
		float hookPos = hook.getHookPos();
		
		/* remove hook from hook curve */
		addEdit(new AtomicRemoveControlPointFromCurve(hook));
		
		/* and insert it on the parent curve */
		addEdit(new AtomicInsertControlPoint(hook, parentHook));
		
		/* and convert it to a regular controlpoint by setting hookpos to -1 */
		addEdit(new ChangeCPHookPosEdit(hook,-1));
		
		/* set position */
		hook.setPosition(position);
		
		if (prevHook == startHook && nextHook == endHook) {
			
			/* remove hook curve */
			//addEdit(new RemoveCurveFromModelEdit(startHook.getCurve()));
			addEdit(new ChangeCPChildHookEdit(parentHook, null));
			addEdit(new CompoundDeleteControlPoint(startHook));
		} else if (prevHook == startHook) {
			
			/* modify hook curve */
			addEdit(new ChangeCPParentHookEdit(startHook, hook));
			addEdit(new ChangeCPChildHookEdit(parentHook, null));
			addEdit(new ChangeCPChildHookEdit(hook, startHook));
			for (ControlPoint cp = startHook; cp != null; cp = cp.getNext()) {
				float hp = cp.getHookPos();
				if (hp > 0 && hp < 1)
					addEdit(new ChangeCPHookPosEdit(cp, (hp - hookPos) / ( 1 - hookPos)));
			}
//			System.out.println("b");
		} else if (nextHook == endHook ){
			
			/* modify hook curve */
			addEdit(new ChangeCPParentHookEdit(endHook, hook));
			for (ControlPoint cp = startHook; cp != null; cp = cp.getNext()) {
				float hp = cp.getHookPos();
				if (hp > 0 && hp < 1)
					addEdit(new ChangeCPHookPosEdit(cp, hp / hookPos));
			}
//			System.out.println("a");
		} else {
			
			/* add a new hook curve and modify curves */
			ControlPoint newStartHook = new ControlPoint();
			ControlPoint newEndHook = new ControlPoint();
			newEndHook.appendTo(newStartHook);
			newStartHook.setParentHook(hook);
			newStartHook.setHookPos(0);
			newEndHook.setParentHook(endHook.getParentHook());
			newEndHook.setHookPos(1);
			addEdit(new ChangeCPParentHookEdit(endHook, hook));
			addEdit(new ChangeCPChildHookEdit(hook, newStartHook));
			ControlPoint cpAppend = newStartHook;
			for (ControlPoint cp = startHook; cp != null; cp = cp.getNext()) {
				float hp = cp.getHookPos();
				if (hp > 0 && hp < 1) {
					if (hp < hookPos) {
						addEdit(new ChangeCPHookPosEdit(cp, hp / hookPos));
					} else {
						addEdit(new ChangeCPHookPosEdit(cp, (hp - hookPos) / ( 1 - hookPos)));
						addEdit(new AtomicRemoveControlPointFromCurve(cp));
						addEdit(new AtomicInsertControlPoint(cp, cpAppend));
						cpAppend = cp;
					}
				}
			}
			Curve curve = new Curve(newStartHook);
			curve.validate();
			addEdit(new AtomicAddCurve(curve));
//			System.out.println("c");
		}
	}
	
// TODO: correct patches and morphs!	

}
