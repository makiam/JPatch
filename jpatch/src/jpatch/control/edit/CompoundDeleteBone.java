package jpatch.control.edit;

import java.util.ArrayList;
import java.util.Iterator;

import jpatch.boundary.MainFrame;
import jpatch.entity.Bone;
import jpatch.entity.ControlPoint;

public class CompoundDeleteBone extends JPatchCompoundEdit{
	public CompoundDeleteBone(Bone bone) {
		for (Iterator jt = (new ArrayList(bone.getChildBones())).iterator(); jt.hasNext(); ) {
			Bone child = (Bone) jt.next();
			addEdit(new AtomicDetachBone(child));
		}
		addEdit(new AtomicDropBone(bone));
		addEdit(new AtomicRemoveBoneFromSelections(bone));
		for (Iterator jt = MainFrame.getInstance().getModel().getCurveSet().iterator(); jt.hasNext(); ) {
			ControlPoint start = (ControlPoint) jt.next();
			for (ControlPoint cp = start; cp != null; cp = cp.getNextCheckNextLoop()) {
				if (cp.getBone() == bone)
					addEdit(new AtomicChangeControlPoint.Bone(cp, null));
			}
		}
	}
}
