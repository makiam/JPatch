/*
 * $Id: CompoundDropControlPoint.java,v 1.1 2005/09/06 13:44:52 sascha_l Exp $
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

import jpatch.entity.*;

/**
 * @author sascha
 *
 */
public class CompoundDropControlPoint extends JPatchCompoundEdit {
	public CompoundDropControlPoint(ControlPoint cp) {
		// has cp got a valid parent-hook?
		if (cp.getParentHook() != null && cp.getParentHook().getChildHook() == cp)
			// YES
			// change parent-hook's child-hook to null
			addEdit(new AtomicChangeControlPoint.ChildHook(cp.getParentHook(), null));
		// has cp got a child-hook?
		if (cp.getChildHook() != null)
			// YES
			// drop child-hook's curve
			addEdit(new CompoundDropCurve(cp.getChildHook().getCurve()));
		// has previous cp got a child-hook?
		if (cp.getPrev() != null && cp.getPrev().getChildHook() != null)
			// YES
			// drop previous cp's child-hook's curve
			addEdit(new CompoundDropCurve(cp.getPrev().getChildHook().getCurve()));
		// remove cp from all entities
		addEdit(new CompoundRemoveControlPointFromEntities(cp));
		// detauch the cp
		addEdit(new AtomicDetatchControlPoint(cp));
	}
}
