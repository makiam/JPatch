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
public final class AtomicReplaceControlPointInSelections extends JPatchAtomicEdit {
	private OLDControlPoint cpOld, cpNew;
	
	public AtomicReplaceControlPointInSelections(OLDControlPoint cpOld, OLDControlPoint cpNew) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + cpOld + ", " + cpNew + ")");
		this.cpOld = cpOld;
		this.cpNew = cpNew;
		swap();
	}
	
	private void swap() {
		for (Iterator it = MainFrame.getInstance().getModel().getSelections().iterator(); it.hasNext(); ) {
			OLDSelection selection = (OLDSelection) it.next();
			if (selection.contains(cpOld)) {
				Object weight = selection.getMap().get(cpOld);
				selection.getMap().remove(cpOld);
				selection.getMap().put(cpNew, weight);
				if (selection.getHotObject() == cpOld)
					selection.setHotObject(cpNew);
			}
		}
		OLDSelection selection = MainFrame.getInstance().getSelection();
		if (selection != null && selection.contains(cpOld)) {
			Object weight = selection.getMap().get(cpOld);
			selection.getMap().remove(cpOld);
			selection.getMap().put(cpNew, weight);
			if (selection.getHotObject() == cpOld)
				selection.setHotObject(cpNew);
		}
		OLDControlPoint dummy = cpOld;
		cpOld = cpNew;
		cpNew = dummy;
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	public int sizeOf() {
		return 8 + 4 + 4;
	}
}
