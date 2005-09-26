/*
 * $Id: AtomicRemoveControlPointFromSelections.java,v 1.4 2005/09/26 15:07:42 sascha_l Exp $
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
public final class AtomicRemoveControlPointFromSelections extends JPatchAtomicEdit {
	private final ControlPoint cp;
	private final HashMap mapSelections = new HashMap();
	
	public AtomicRemoveControlPointFromSelections(ControlPoint cp) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + cp + ")");
		this.cp = cp;
		for (Iterator it = MainFrame.getInstance().getModel().getSelections().iterator(); it.hasNext(); ) {
			Selection selection = (Selection) it.next();
			if (selection.contains(cp))
				mapSelections.put(selection, selection.getMap().get(cp));
		}
		redo();
	}
	
	public void undo() {
		for (Iterator it = mapSelections.keySet().iterator(); it.hasNext(); ) {
			Selection selection = (Selection) it.next();
			selection.getMap().put(cp, mapSelections.get(selection)); 
		}
	}
	
	public void redo() {
		for (Iterator it = mapSelections.keySet().iterator(); it.hasNext(); ) {
			Selection selection = (Selection) it.next();
			selection.getMap().remove(cp); 
		}
	}
	
	public int sizeOf() {
		return mapSelections == null ? 8 + 4 + 4 : 8 + 4 + 4 + (8 + 4 + 4 + 4 + 4 + 8 * mapSelections.size() * 2);
	}
}
