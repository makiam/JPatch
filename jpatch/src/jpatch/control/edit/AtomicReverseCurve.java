package jpatch.control.edit;

import jpatch.boundary.*;
import jpatch.entity.*;

/**
 * This edit will reverse a curve - i.e. it will reverse the order of ControlPoints in a curve.
 * It takes care for loops and automatically reverses child hook-curves.
 * @author sascha
 *
 */
public final class AtomicReverseCurve extends JPatchAtomicEdit {

	private ControlPoint cpStart;
	
	public AtomicReverseCurve(ControlPoint start) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + start + ")");
		cpStart = start;
		if (cpStart.getLoop())
			throw new IllegalArgumentException(this.getClass().getName() + " can't be applied to closed (looped) curve starting at " + cpStart);
		reverseCurve(cpStart);
	}
	
	public void undo() {
		reverseCurve(cpStart);
	}

	public void redo() {
		reverseCurve(cpStart);
	}

	private void reverseCurve(ControlPoint start) {
		for (ControlPoint cp = start.getEnd(); cp != null; cp = cp.getNext()) {
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
				reverseCurve(cp.getChildHook());
				cp.setChildHook(null);
			}
			/*
			 * swap cpNext <-> cpPrev
			 */
			ControlPoint cpDummy = cp.getNext();
			cp.setNext(cp.getPrev());
			cp.setPrev(cpDummy);
//			cp.setTangentsValid(false);
		}
		MainFrame.getInstance().getModel().getCurveSet().remove(cpStart);
		cpStart = cpStart.getStart();
		MainFrame.getInstance().getModel().getCurveSet().add(cpStart);
	}
	
	public int sizeOf() {
		return 8 + 4;
	}
}
