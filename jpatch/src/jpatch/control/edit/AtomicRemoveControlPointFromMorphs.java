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
import javax.vecmath.*;

import jpatch.entity.*;
import jpatch.boundary.*;

/**
 * @author sascha
 *
 */
public final class AtomicRemoveControlPointFromMorphs extends JPatchAtomicEdit {
	private final OLDControlPoint cp;
	private final HashMap mapMorphTargets = new HashMap();;
	
	public AtomicRemoveControlPointFromMorphs(OLDControlPoint cp) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + cp + ")");
		this.cp = cp;
		for (Iterator itMorph = MainFrame.getInstance().getModel().getMorphList().iterator(); itMorph.hasNext(); ) {
			OLDMorph morph = (OLDMorph) itMorph.next();
			for (MorphTarget target : morph.getTargets()) {
				Object vector = target.getVectorFor(cp);
				if (vector != null) {
					mapMorphTargets.put(target, target.getVectorFor(cp));
					target.removePoint(cp);
				}
			}
		}
		for (OLDBone bone : MainFrame.getInstance().getModel().getBoneSet()) {
			for (OLDMorph morph : bone.getDofs()) {
				for (MorphTarget target : morph.getTargets()) {
					Object vector = target.getVectorFor(cp);
					if (vector != null) {
						mapMorphTargets.put(target, target.getVectorFor(cp));
						target.removePoint(cp);
					}
				}
			}
		}
	}
	
	public void undo() {
		for (Iterator it = mapMorphTargets.keySet().iterator(); it.hasNext(); ) {
			MorphTarget morph = (MorphTarget) it.next();
			morph.addPoint(cp, (Vector3f) mapMorphTargets.get(morph)); 
		}
	}
	
	public void redo() {
		for (Iterator it = mapMorphTargets.keySet().iterator(); it.hasNext(); ) {
			MorphTarget morph = (MorphTarget) it.next();
			morph.removePoint(cp); 
		}
	}
	
	public int sizeOf() {
		return mapMorphTargets == null ? 8 + 4 + 4 : 8 + 4 + 4 + (8 + 4 + 4 + 4 + 4 + 8 * mapMorphTargets.size() * 2);
	}
}
