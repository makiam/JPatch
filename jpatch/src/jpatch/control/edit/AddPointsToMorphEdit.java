/*
 * $Id: AddPointsToMorphEdit.java,v 1.6 2006/02/01 21:11:28 sascha_l Exp $
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

/**
 * @author sascha
 *
 */
public class AddPointsToMorphEdit extends JPatchAtomicEdit {
	
	private MorphTarget morph;
	private Map map;

	public AddPointsToMorphEdit(MorphTarget morph, Map map) {
		this.morph = morph;
		this.map = map;
		redo();
	}
	
	public void redo() {
		morph.getMorphMap().putAll(map);
		morph.getMorph().setupMorphMap();
//		morph.getPointList().addAll(listPoints);
//		morph.getVectorList().addAll(listVectors);
	}
	
	public void undo() {
//		List morphPoints = morph.getPointList();
//		List morphVectors = morph.getVectorList();
		Map morphMap = morph.getMorphMap();
		for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
//			ControlPoint cp = (ControlPoint) it.next();
//			morphVectors.remove(morphPoints.indexOf(cp));
//			morphPoints.remove(cp);
			morphMap.remove(it.next());
		}
		morph.getMorph().setupMorphMap();
	}
	
	public int sizeOf() {
		return 8 + 4 + (8 + 4 + 4 + 4 + 4 + 8 * map.size() * 2);
	}
}
