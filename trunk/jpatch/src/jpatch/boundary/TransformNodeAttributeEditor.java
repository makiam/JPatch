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
public class TransformNodeAttributeEditor extends AbstractAttributeEditor {
	private TransformNode transformNode;
	
	public TransformNodeAttributeEditor(TransformNode transformNode) {
		this.transformNode = transformNode;
		ExpandableForm defaultForm = new ExpandableForm(true);
		ExpandableForm translationForm = new ExpandableForm();
		ExpandableForm positionForm = new ExpandableForm();
		ExpandableForm rotationForm = new ExpandableForm();
		ExpandableForm orientationForm = new ExpandableForm();
		ExpandableForm scaleForm = new ExpandableForm();
		ExpandableForm shearForm = new ExpandableForm();
		
		addScalar(defaultForm, "Name", transformNode.name);
		addScalar(defaultForm, "Visibility", transformNode.visibility);
		addTuple(translationForm, "Translation", transformNode.translation);
		addLimit(translationForm, transformNode.translation);
		addTuple(positionForm, "Position", transformNode.position);
		addTuple(rotationForm, "Rotation", transformNode.rotation);
		addScalar(rotationForm, "Order", transformNode.rotationOrder);
		addLimit(rotationForm, transformNode.rotation);
		addTuple(rotationForm, "Pivot Transl.", transformNode.rotatePivotTranslation);
		addTuple(rotationForm, "Pivot Pos.",transformNode.rotatePivotPosition);
		
		addTuple(orientationForm, "Orientation", transformNode.orientation);
		
//		addScalar(transformNode.rotation.order);
		addTuple(scaleForm, "Scale", transformNode.scale);
//		addLimit(scaleForm, transformNode.scale);
		addTuple(scaleForm, "Pivot Transl.", transformNode.scalePivotTranslation);
		addTuple(scaleForm, "Pivot Pos.", transformNode.scalePivotPosition);
		addTuple(shearForm, "Shear", transformNode.shear);
		
//		addTuple(shear, transformNode.scale);
//		addTuple(transformNode.rotatePivotPosition);
//		addTuple(transformNode.rotatePivotTranslation);
//		addTuple(transformNode.scalePivotPosition);
//		addTuple(transformNode.scalePivotTranslation);
//		addLimit(transformNode.translation.x);
//		addLimit(transformNode.translation.y);
//		addLimit(transformNode.translation.z);
//		addLimit(transformNode.rotation.x);
//		addLimit(transformNode.rotation.y);
//		addLimit(transformNode.rotation.z);
//		addLimit(transformNode.scale.x);
//		addLimit(transformNode.scale.y);
//		addLimit(transformNode.scale.z);
		
		add(defaultForm);
		add(translationForm);
		add(positionForm);
		add(rotationForm);
		add(orientationForm);
		add(scaleForm);
		add(shearForm);
	}
	

	
	
}
