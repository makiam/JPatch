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

import jpatch.entity.*;
import jpatch.boundary.*;

/**
 * @author sascha
 *
 */
public final class AtomicRemoveBoneFromSelections extends JPatchAtomicEdit {
	private final OLDBone.BoneTransformable boneStart;
	private final OLDBone.BoneTransformable boneEnd;
	private final HashMap mapSelectionsStart = new HashMap();
	private final HashMap mapSelectionsEnd = new HashMap();
	
	public AtomicRemoveBoneFromSelections(OLDBone bone) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + bone + ")");
		boneStart = bone.getParentBone() == null ? bone.getBoneStart() : bone.getParentBone().getBoneEnd();
		boneEnd = bone.getBoneEnd();
//		System.out.println("remove bone from selections " + boneStart + " " + boneEnd);
		for (Iterator it = MainFrame.getInstance().getModel().getSelections().iterator(); it.hasNext(); ) {
			OLDSelection selection = (OLDSelection) it.next();
			Object weight = selection.getMap().get(boneStart);
			if ((weight) != null) {
				mapSelectionsStart.put(selection, weight);
				selection.getMap().remove(boneStart);
			}
			weight = selection.getMap().get(boneEnd);
			if ((weight) != null) {
				mapSelectionsEnd.put(selection, weight);
				selection.getMap().remove(boneEnd);
			}
		}
		OLDSelection selection = MainFrame.getInstance().getSelection();
		if (selection != null) {
			Object weight = selection.getMap().get(boneStart);
			if ((weight) != null) {
				mapSelectionsStart.put(selection, weight);
				selection.getMap().remove(boneStart);
			}
			weight = selection.getMap().get(boneEnd);
			if ((weight) != null) {
				mapSelectionsEnd.put(selection, weight);
				selection.getMap().remove(boneEnd);
			}
		}
	}
	
	public void undo() {
		for (Iterator it = mapSelectionsStart.keySet().iterator(); it.hasNext(); ) {
			OLDSelection selection = (OLDSelection) it.next();
			selection.getMap().put(boneStart, mapSelectionsStart.get(selection)); 
		}
		for (Iterator it = mapSelectionsEnd.keySet().iterator(); it.hasNext(); ) {
			OLDSelection selection = (OLDSelection) it.next();
			selection.getMap().put(boneEnd, mapSelectionsEnd.get(selection)); 
		}
	}
	
	public void redo() {
		for (Iterator it = mapSelectionsStart.keySet().iterator(); it.hasNext(); ) {
			OLDSelection selection = (OLDSelection) it.next();
			selection.getMap().remove(boneStart); 
		}
		for (Iterator it = mapSelectionsEnd.keySet().iterator(); it.hasNext(); ) {
			OLDSelection selection = (OLDSelection) it.next();
			selection.getMap().remove(boneEnd); 
		}
	}
	
	public int sizeOf() {
		return mapSelectionsStart == null ? 8 + 4 + 4 + 4 + 4 : 8 + 4 + 4 + 4 + 4 + (8 + 4 + 4 + 4 + 4 + 8 * mapSelectionsStart.size() * 2) * 2;
	}
}
