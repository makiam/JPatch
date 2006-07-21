package jpatch.control.edit;

import java.util.ArrayList;
import java.util.Iterator;

import jpatch.entity.OLDBone;

public class CompoundRemoveBone extends JPatchCompoundEdit{
	public CompoundRemoveBone(OLDBone bone) {
		for (Iterator jt = (new ArrayList(bone.getChildBones())).iterator(); jt.hasNext(); ) {
			OLDBone child = (OLDBone) jt.next();
			addEdit(new AtomicDetachBone(child));
			if (bone.getParentBone() != null)
				addEdit(new AtomicAttachBone(child, bone.getParentBone()));
		}
		addEdit(new AtomicDropBone(bone));
		addEdit(new AtomicRemoveBoneFromSelections(bone));
	}
}
