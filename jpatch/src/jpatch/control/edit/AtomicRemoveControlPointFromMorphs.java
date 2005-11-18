/*
 * $Id: AtomicRemoveControlPointFromMorphs.java,v 1.4 2005/11/18 16:00:33 sascha_l Exp $
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
	private final ControlPoint cp;
	private final HashMap mapMorphTargets = new HashMap();;
	
	public AtomicRemoveControlPointFromMorphs(ControlPoint cp) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + cp + ")");
		this.cp = cp;
		for (Iterator itMorph = MainFrame.getInstance().getModel().getMorphList().iterator(); itMorph.hasNext(); ) {
			Morph morph = (Morph) itMorph.next();
			for (Iterator itTarget = morph.getTargets().iterator(); itTarget.hasNext(); ) {
				MorphTarget target = (MorphTarget) itTarget.next();
				Object vector = target.getVectorFor(cp);
				if (vector != null) {
					mapMorphTargets.put(target, target.getVectorFor(cp));
					target.removePoint(cp);
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
