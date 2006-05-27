package jpatch.control.edit;

import java.util.ArrayList;
import java.util.Iterator;

import jpatch.entity.Bone;

public class CompoundRemoveBone extends JPatchCompoundEdit{
	public CompoundRemoveBone(Bone bone) {
		for (Iterator jt = (new ArrayList(bone.getChildBones())).iterator(); jt.hasNext(); ) {
			Bone child = (Bone) jt.next();
			addEdit(new AtomicDetachBone(child));
			if (bone.getParentBone() != null)
				addEdit(new AtomicAttachBone(child, bone.getParentBone()));
		}
		addEdit(new AtomicDropBone(bone));
		addEdit(new AtomicRemoveBoneFromSelections(bone));
	}
}
