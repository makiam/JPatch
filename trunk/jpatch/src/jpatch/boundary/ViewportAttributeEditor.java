/*
 * $Id:$
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
package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jpatch.boundary.ui.*;
import jpatch.entity.*;
/**
 * @author sascha
 *
 */
public class ViewportAttributeEditor extends AbstractAttributeEditor {
	
	public ViewportAttributeEditor(Viewport viewport) {
		
		ExpandableForm defaultForm = new ExpandableForm(true);
		ExpandableForm showForm = new ExpandableForm(true);
		ExpandableForm viewForm = new ExpandableForm(true);
		
		addScalar(defaultForm, "View direction", viewport.viewType);
		
		addScalar(showForm, "Show points", viewport.showPoints);
		addScalar(showForm, "Show curves", viewport.showCurves);
		addScalar(showForm, "Show surfaces", viewport.showSurfaces);
		
		addTuple(viewForm, "Translation", viewport.viewTranslation);
		addTuple(viewForm, "Rotation", viewport.viewRotation);
		addScalar(viewForm, "Scale", viewport.viewScale);
		
		add(defaultForm);
		add(showForm);
		add(viewForm);
	}
}
