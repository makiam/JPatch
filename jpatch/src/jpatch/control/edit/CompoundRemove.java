/*
 * $Id$
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

import jpatch.boundary.MainFrame;
import jpatch.entity.*;

/**
 * @author sascha
 *
 */
public class CompoundRemove extends JPatchCompoundEdit {
	
	public CompoundRemove(Collection objects) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + objects + ")");
		/*
		 * Remove ControlPoints
		 */
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
//		for (Iterator it = (new HashSet(MainFrame.getInstance().getModel().getCurveSet())).iterator(); it.hasNext(); ) {
//			ControlPoint start = (ControlPoint) it.next();
//			if (dropCurve(start, objects)) {
//				for (ControlPoint cp = start; cp != null; cp = cp.getNextCheckNextLoop())
//					controlPointSet.remove(cp);
//				addEdit(new CompoundDropCurve(start, start.getHookPos() == 0));
//			}
//		}
		for (Iterator it = controlPointSet.iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			if (!cp.isDeleted())
				addEdit(new CompoundRemoveControlPoint(cp));
		}
		
		/*
		 * Remove Bones
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
			addEdit(new AtomicDropBone(bone));
		}
		// set parents of orphanized bones to null
//		for (Iterator it = MainFrame.getInstance().getModel().getBoneSet().iterator(); it.hasNext(); ) {
//			Bone bone = (Bone) it.next();
//			if (boneSet.contains(bone.getParentBone())) {
//				Bone parent;
//				for (parent = bone.getParentBone(); parent != null && boneSet.contains(parent); parent = parent.getParentBone());
//				addEdit(new AtomicChangeBone.Parent(bone, parent));
//			}
//		}
	}
	
//	public CompoundRemove(Model model, Collection objects) {
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
//				ControlPoint[] acp = ((ControlPoint) object).getHead().getStack();
//				for (int i = 0; i < acp.length; i++) {
//					addEdit(new CompoundRemoveControlPoint(acp[i]));
//				}
//			}
//		}
//	}
	
//	private boolean dropCurve(Curve curve, Collection objects) {
//		boolean consecutive = false;
//		for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
//			if (!objects.contains(cp.getHead())) {
//				if (consecutive)
//					return false;
//				consecutive = true;
//			} else {
//				consecutive = false;
//			}
//		}
//		return true;
//	}
}