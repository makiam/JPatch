/*
 * $Id: CompoundReplaceControlPointInEntities.java,v 1.3 2005/09/29 15:12:12 sascha_l Exp $
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
public final class CompoundReplaceControlPointInEntities extends JPatchCompoundEdit {
	
	public CompoundReplaceControlPointInEntities(ControlPoint cpOld, ControlPoint cpNew) {
		System.out.println("*");
		addEdit(new AtomicReplaceControlPointInSelections(cpOld, cpNew));
		addEdit(new AtomicReplaceControlPointInPatches(cpOld, cpNew));
	}
}
