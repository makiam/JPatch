/*
 * $Id: AddPointsToMorphEdit.java,v 1.1 2005/08/10 12:57:20 sascha_l Exp $
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
public class AddPointsToMorphEdit extends JPatchAbstractUndoableEdit {
	
	private Morph morph;
	private List listPoints;
	private List listVectors;

	public AddPointsToMorphEdit(Morph morph, List points, List vectors) {
		this.morph = morph;
		this.listPoints = points;
		this.listVectors = vectors;
		redo();
	}
	
	public void redo() {
		morph.getPointList().addAll(listPoints);
		morph.getVectorList().addAll(listVectors);
	}
	
	public void undo() {
		List morphPoints = morph.getPointList();
		List morphVectors = morph.getVectorList();
		for (Iterator it = listPoints.iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			morphVectors.remove(morphPoints.indexOf(cp));
			morphPoints.remove(cp);
		}
	}
}
