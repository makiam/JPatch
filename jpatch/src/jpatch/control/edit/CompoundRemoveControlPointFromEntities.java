/*
 * $Id: CompoundRemoveControlPointFromEntities.java,v 1.3 2005/09/26 10:36:28 sascha_l Exp $
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
public final class CompoundRemoveControlPointFromEntities extends JPatchCompoundEdit {
	
	public CompoundRemoveControlPointFromEntities(ControlPoint cp) {
		// Remove cp from selections
		addEdit(new AtomicRemoveControlPointFromSelections(cp));
		// Remove empty selections
		for (Iterator it = (new HashSet(MainFrame.getInstance().getModel().getSelections())).iterator(); it.hasNext(); ) {
			NewSelection selection = (NewSelection) it.next();
			if (selection.getMap().size() == 0)
				addEdit(new AtomicRemoveSelection(selection));
		}
		// Remove patches containing cp
		for (Iterator it = (new HashSet(MainFrame.getInstance().getModel().getPatchSet())).iterator(); it.hasNext(); ) {
			Patch patch = (Patch) it.next();
			if (patch.contains(cp))
				addEdit(new AtomicRemovePatch(patch));
		}
		// Remove cp from morphs
		addEdit(new AtomicRemoveControlPointFromMorphs(cp));
		// Remove empty morphs
		for (Iterator it = (new ArrayList(MainFrame.getInstance().getModel().getMorphList())).iterator(); it.hasNext(); ) {
			Morph morph = (Morph) it.next();
			if (morph.getNumberOfPoints() == 0)
				addEdit(new AtomicRemoveMorph(morph));
		}
	}
}
