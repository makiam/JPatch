/*
 * $Id: CompoundDelete.java,v 1.6 2005/11/18 16:00:33 sascha_l Exp $
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.control.edit;

import java.util.*;

import jpatch.boundary.*;
import jpatch.boundary.action.DetachControlPointsAction;
import jpatch.entity.*;

/**
 * @author sascha
 *
 */
public class CompoundDelete extends JPatchCompoundEdit {
	
	public CompoundDelete(Collection objects) {
		/*
		 * Delete ControlPoints
		 */
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + objects + ")");
		HashSet controlPointSet = new HashSet();
		HashSet btSet = new HashSet();
		for (Iterator it = objects.iterator(); it.hasNext(); ) {
			Object object = it.next();
			if (object instanceof ControlPoint) {
				ControlPoint head = (ControlPoint) object;
				for (ControlPoint cp = head; cp != null; cp = cp.getPrevAttached()) {
					controlPointSet.add(cp);
				}
			} else if (object instanceof Bone.BoneTransformable) {
				btSet.add(object);
			}
		}
		if (DEBUG)
			System.out.println("\t" + controlPointSet);
		for (Iterator it = (new HashSet(MainFrame.getInstance().getModel().getCurveSet())).iterator(); it.hasNext(); ) {
			ControlPoint start = (ControlPoint) it.next();
			if (!start.isDeleted() && dropCurve(start, objects)) {
				for (ControlPoint cp = start; cp != null; cp = cp.getNextCheckNextLoop())
					controlPointSet.remove(cp);
				addEdit(new CompoundDropCurve(start));
			}
		}
		for (Iterator it = controlPointSet.iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			if (!cp.isDeleted())
				addEdit(new CompoundDeleteControlPoint(cp));
		}
		
		/*
		 * Delete Bones
		 */
		Set boneSet = new HashSet();
		// create set of bones to be deleted
		for (Iterator it = MainFrame.getInstance().getModel().getBoneSet().iterator(); it.hasNext(); ) {
			Bone bone = (Bone) it.next();
			Bone.BoneTransformable start = (bone.getParentBone() == null) ? bone.getBoneStart() : bone.getParentBone().getBoneEnd();
			if (btSet.contains(bone.getBoneEnd()) && btSet.contains(start))
				boneSet.add(bone);
		}
		// drop bones
		for (Iterator it = boneSet.iterator(); it.hasNext(); ) {
			Bone bone = (Bone) it.next();
			for (Iterator jt = (new ArrayList(bone.getChildBones())).iterator(); jt.hasNext(); ) {
				Bone child = (Bone) jt.next();
				addEdit(new AtomicDetachBone(child));
			}
			addEdit(new AtomicDropBone(bone));
			addEdit(new AtomicRemoveBoneFromSelections(bone));
		}
		
		
//		Remove empty selections
		for (Iterator it = (new HashSet(MainFrame.getInstance().getModel().getSelections())).iterator(); it.hasNext(); ) {
			Selection selection = (Selection) it.next();
			if (selection.getMap().size() == 0)
				addEdit(new AtomicRemoveSelection(selection));
		}
//		 Remove empty morphs
		for (Iterator itMorph = (new ArrayList(MainFrame.getInstance().getModel().getMorphList())).iterator(); itMorph.hasNext(); ) {
			Morph morph = (Morph) itMorph.next();
			for (Iterator itTarget = new ArrayList(morph.getTargets()).iterator(); itTarget.hasNext(); ) {
				MorphTarget morphTarget = (MorphTarget) itTarget.next();
//				if (morphTarget.getNumberOfPoints() == 0)
//					addEdit(new AtomicRemoveMorphTarget(morphTarget));
			}
			if (morph.getChildCount() == 0)
				addEdit(new AtomicRemoveMorph(morph));
					
		}		
		
		// set parents of orphanized bones to null
//		for (Iterator it = MainFrame.getInstance().getModel().getBoneSet().iterator(); it.hasNext(); ) {
//			Bone bone = (Bone) it.next();
//			if (boneSet.contains(bone.getParentBone())) {
//				System.out.println("*");
//				addEdit(new AtomicDropBone(bone) new AtomicChangeBone.Parent(bone, null));
//			}
//		}
	}
////		for (Curve curve = model.getFirstCurve(); curve != null; curve = curve.getNext()) {
////			if (dropCurve(curve, objects)) {
////				for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop())
////					objects.remove(cp.getHead());
////				addEdit(new CompoundDropCurve(curve));
////			}
////		}
//		for (Iterator it = objects.iterator(); it.hasNext(); ) {
//			Object object = it.next();
//			if (object instanceof ControlPoint) {
////				addEdit(new CompoundDropControlPointStack(((ControlPoint) object).getHead()));
//				ControlPoint[] acp = ((ControlPoint) object).getHead().getStack();
//				for (int i = 0; i < acp.length; i++) {
//					if (acp[i].getCurve() != null)
//						addEdit(new CompoundDeleteControlPoint(acp[i]));
//				}
//			}
//		}
//	}
	
	private boolean dropCurve(ControlPoint start, Collection objects) {
		boolean consecutive = false;
		for (ControlPoint cp = start; cp != null; cp = cp.getNextCheckNextLoop()) {
			if (!objects.contains(cp.getHead())) {
				if (consecutive)
					return false;
				consecutive = true;
			} else {
				consecutive = false;
			}
		}
		return true;
	}
}
