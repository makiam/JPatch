/*
 * $Id: Pose.java,v 1.2 2006/02/01 21:11:28 sascha_l Exp $
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
package jpatch.entity;

import java.util.*;
import jpatch.boundary.*;

/**
 * @author sascha
 *
 */
public class Pose {
	private Model model;
	private Map<Morph, Float> poseMap = new HashMap<Morph, Float>();
	
	public Pose(Model model) {
		this.model = model;
		setPose();
	}
	
	public void setPose() {
		for (Iterator it = model.getMorphList().iterator(); it.hasNext(); ) {
			Morph morph = (Morph) it.next();
			poseMap.put(morph, morph.getValue());
		}
		for (Iterator itBone = model.getBoneSet().iterator(); itBone.hasNext(); ) {
			for (Iterator itDof = ((Bone) itBone.next()).getDofs().iterator(); itDof.hasNext(); ) {
				RotationDof dof = (RotationDof) itDof.next();
				poseMap.put(dof, dof.getValue());
			}
		}
	}
	
	public void applyPose() {
		for (Morph morph:poseMap.keySet()) {
			morph.setValue(poseMap.get(morph));
			if (MainFrame.getInstance().getAnimation() != null)
				morph.updateCurve();
		}
	}
}
