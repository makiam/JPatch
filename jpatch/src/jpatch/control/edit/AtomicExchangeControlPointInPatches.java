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
public final class AtomicExchangeControlPointInPatches extends JPatchAtomicEdit {
	private final ControlPoint cpOld, cpNew;
	private final HashMap mapSelections = new HashMap();
	
	public AtomicExchangeControlPointInPatches(ControlPoint cpOld, ControlPoint cpNew) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + cpOld + ", " + cpNew + ")");
		this.cpOld = cpOld;
		this.cpNew = cpNew;
		for (Iterator it = MainFrame.getInstance().getModel().getPatchSet().iterator(); it.hasNext(); ) {
			Patch patch = (Patch) it.next();
			if (patch.contains(cpOld))
				mapSelections.put(selection, selection.getMap().get(cpOld));
		}
		redo();
	}
	
	public void undo() {
		for (Iterator it = mapSelections.keySet().iterator(); it.hasNext(); ) {
			NewSelection selection = (NewSelection) it.next();
			selection.getMap().remove(cpNew);
			selection.getMap().put(cpOld, mapSelections.get(selection)); 
		}
	}
	
	public void redo() {
		for (Iterator it = mapSelections.keySet().iterator(); it.hasNext(); ) {
			NewSelection selection = (NewSelection) it.next();
			selection.getMap().remove(cpOld);
			selection.getMap().put(cpNew, mapSelections.get(selection));
		}
	}
	
	public int sizeOf() {
		return 8 + 4 + 4 + 4 + 4 + (8 + 4 + 4 + 4 + 4 + 8 * mapSelections.size() * 2);
	}
}
