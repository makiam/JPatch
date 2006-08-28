package jpatch.control.edit;

import java.util.ArrayList;
import java.util.Iterator;

import jpatch.boundary.MainFrame;
import jpatch.entity.OLDBone;
import jpatch.entity.OLDControlPoint;

public class CompoundDeleteBone extends JPatchCompoundEdit{
	public CompoundDeleteBone(OLDBone bone) {
		for (Iterator jt = (new ArrayList(bone.getChildBones())).iterator(); jt.hasNext(); ) {
			OLDBone child = (OLDBone) jt.next();
			addEdit(new AtomicDetachBone(child));
		}
		addEdit(new AtomicDropBone(bone));
		addEdit(new AtomicRemoveBoneFromSelections(bone));
		for (Iterator jt = MainFrame.getInstance().getModel().getCurveSet().iterator(); jt.hasNext(); ) {
			OLDControlPoint start = (OLDControlPoint) jt.next();
			for (OLDControlPoint cp = start; cp != null; cp = cp.getNextCheckNextLoop()) {
				if (cp.getBone() == bone)
					addEdit(new AtomicChangeControlPoint.Bone(cp, null));
			}
		}
	}
}
