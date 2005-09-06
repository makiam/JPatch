package jpatch.control.edit;

import jpatch.entity.*;

/**
 * This edit will reverse a curve - i.e. it will reverse the order of ControlPoints in a curve.
 * It takes care for loops and automatically reverses child hook-curves.
 * @author sascha
 *
 */
public final class AtomicReverseCurve extends JPatchAtomicEdit {

	private Curve curve;
	
	public AtomicReverseCurve(Curve curve) {
		this.curve = curve;
		if (curve.getStart().getLoop())
			throw new IllegalArgumentException(this.getClass().getName() + " can't be applied to closed (looped) curve " + curve);
		reverseCurve(curve);
	}
	
	public void undo() {
		reverseCurve(curve);
	}

	public void redo() {
		reverseCurve(curve);
	}

	private void reverseCurve(Curve curve) {
		for (ControlPoint cp = curve.getStart().getEnd(); cp != null; cp = cp.getNext()) {
			/*
			 * if it's on a hook curve, reverse the hookPos
			 */
			if (cp.getHookPos() != -1) {
				cp.setHookPos(1 - cp.getHookPos());
			}
			/*
			 * if we've got a child hook, reverse child hook curve and move childhook
			 */
			if (cp.getChildHook() != null) {
				//System.out.println("cp   : " + cp);
				//System.out.println("next : " + cp.getNext());
				//System.out.println("child: " + cp.getChildHook());
				cp.getNext().setChildHook(cp.getChildHook().getEnd());
				reverseCurve(cp.getChildHook().getCurve());
				cp.setChildHook(null);
			}
			/*
			 * swap cpNext <-> cpPrev
			 */
			ControlPoint cpDummy = cp.getNext();
			cp.setNext(cp.getPrev());
			cp.setPrev(cpDummy);
			cp.setTangentsValid(false);
		}
	}
	
	public int sizeOf() {
		return 8 + 4;
	}
}
