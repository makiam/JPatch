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
public class BoneAttributeEditor extends AbstractAttributeEditor {
	private Bone bone;
	
	public BoneAttributeEditor(Bone bone) {
		this.bone = bone;
		ExpandableForm defaultForm = new ExpandableForm(true);
		ExpandableForm translationForm = new ExpandableForm();
		ExpandableForm positionForm = new ExpandableForm();
		ExpandableForm axisRotationForm = new ExpandableForm();
		ExpandableForm rotationForm = new ExpandableForm();
		ExpandableForm orientationForm = new ExpandableForm();
		ExpandableForm extentForm = new ExpandableForm();
		ExpandableForm scaleForm = new ExpandableForm();
		
		addScalar(defaultForm, bone.name);
		addTuple(axisRotationForm, bone.axisRotation);
		addScalar(axisRotationForm, bone.axisRotationOrder);
		addTuple(translationForm, bone.translation);
		addTuple(positionForm, bone.position);
		addTuple(extentForm, bone.extent);
		addTuple(extentForm, bone.up);
		addTuple(rotationForm, bone.rotation);
		addScalar(rotationForm, bone.rotationOrder);
		
		rotationForm.add(new JLabel("Weighting"));
		JComponent box = new ExpandableFormRow();
		box.setOpaque(false);
		box.add(AttributeUiHelper.createComboBoxFor(bone.weightingX));
		box.add(AttributeUiHelper.createComboBoxFor(bone.weightingX));
		box.add(AttributeUiHelper.createComboBoxFor(bone.weightingX));
		rotationForm.add(box);
		addLimit(rotationForm, bone.rotation);
		
		addTuple(orientationForm, bone.orientation);
		
		
		addTuple(scaleForm, bone.scale);
		addLimit(scaleForm, bone.scale);
//		addLimit(rotationForm, transformNode.rotation);
//		addTuple(rotationForm, transformNode.rotatePivotTranslation);
//		addTuple(rotationForm, transformNode.rotatePivotPosition);
		
		
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
		add(extentForm);
		add(axisRotationForm);
		add(rotationForm);
		add(orientationForm);
		add(scaleForm);
	}
}
