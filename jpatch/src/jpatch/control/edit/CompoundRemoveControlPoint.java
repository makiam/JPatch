package jpatch.control.edit;
/*
 * $Id: CompoundRemoveControlPoint.java,v 1.1 2005/09/07 16:19:02 sascha_l Exp $
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

import jpatch.entity.*;

/**
 * @author sascha
 *
 */
public class CompoundRemoveControlPoint extends JPatchCompoundEdit {

	public CompoundRemoveControlPoint(ControlPoint cp) {
		Curve curve = cp.getCurve();
		//is curve closed and length == 3?
		if (curve.isClosed() && curve.getLength() == 3) {
			// YES
			// delete cp
			addEdit(new CompoundDeleteControlPoint(cp));
			// return
			return;
		}
		// is curve length < 3?
		if (curve.getLength() < 3) {
			// YES
			// remove entire curve
			addEdit(new CompoundDropCurve(curve));
			// return
			return;
		}
		// drop the cp
		addEdit(new CompoundDropControlPoint(cp));
		// remove cp from curve
		addEdit(new AtomicRemoveControlPointFromCurve(cp));
	}
}
