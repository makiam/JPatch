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
package jpatch.entity;

/**
 * @author sascha
 *
 */

import java.util.*;

import javax.swing.event.*;
import javax.vecmath.*;
import jpatch.auxilary.*;

public class TransformNode implements JPatchObject {
	private AttributeOld[] attributes = new AttributeOld[] {
			new AttributeOld<String>("Name", ""),
			new AttributeOld<Boolean>("Visibility", true),
			new AttributeOld<Double>("Translation X", 0.0),
			new AttributeOld<Double>("Translation Y", 0.0),
			new AttributeOld<Double>("Translation Z", 0.0),
			new AttributeOld<Double>("Rotation X", 0.0),
			new AttributeOld<Double>("Rotation Y", 0.0),
			new AttributeOld<Double>("Rotation Z", 0.0),
			new AttributeOld<Double>("Scale X", 0.0),
			new AttributeOld<Double>("Scale Y", 0.0),
			new AttributeOld<Double>("Scale Z", 0.0),
			new AttributeOld<Double>("Pivot X", 0.0),
			new AttributeOld<Double>("Pivot Y", 0.0),
			new AttributeOld<Double>("Pivot Z", 0.0),
	};
	private TransformNode parent;
	private List<AnimObject> animObjects = new ArrayList<AnimObject>(1);
	private List<AnimObject> unmodifiableAnimObjects = Collections.unmodifiableList(animObjects);
	private List<TransformNode> childTransformNodes = new ArrayList<TransformNode>(1);
	private List<TransformNode> unmodifiableChildTransformNodes = Collections.unmodifiableList(childTransformNodes);
	private Matrix4d matrix = new Matrix4d();
	private Matrix3d rotationMatrix = new Matrix3d();
	private Matrix3d scaleMatrix = new Matrix3d();
	private Vector3d translation = new Vector3d();
	private Rotation3d rotation = new Rotation3d();
	private Scale3d scale = new Scale3d();
	private Point3d pivot = new Point3d();
	
	public TransformNode() {
		matrix.setIdentity();
		addAttributeChangeListeners();
	}
	
	@SuppressWarnings("unchecked")
	public AttributeOld getAttribute(String name) {
		for (AttributeOld attribute : attributes)
			if (name.equals(attribute.getName()))
				return attribute;
		return null;
	}
	
	public AttributeOld[] getAttributes() {
		return attributes;
	}
	
	public Matrix4d getMatrix() {
		return matrix;
	}
	
	public List<TransformNode> getChildTransformNodes() {
		return unmodifiableChildTransformNodes;
	}
	
	public List<AnimObject> getAnimObjects() {
		return unmodifiableAnimObjects;
	}
	
	public void setParent(TransformNode parent) {
		this.parent = parent;
	}
	
	public TransformNode getParent() {
		return parent;
	}
	
	public void computeBranch() {
		computeMatrix();
		for (TransformNode child : childTransformNodes)
			child.computeBranch();
	}
	
	private void computeMatrix() {
		scale.setMatrixScale(scaleMatrix);
		rotation.setMatrixRotation(rotationMatrix);
		scaleMatrix.mul(rotationMatrix);
		matrix.setRotationScale(scaleMatrix);
		matrix.setTranslation(translation);
		if (parent != null)
			matrix.mul(parent.getMatrix());
	}
	
	@SuppressWarnings("unchecked")
	private void addAttributeChangeListeners() {
		final AttributeOld<Double> translationX = (AttributeOld<Double>) getAttribute("Translation X");
		final AttributeOld<Double> translationY = (AttributeOld<Double>) getAttribute("Translation Y");
		final AttributeOld<Double> translationZ = (AttributeOld<Double>) getAttribute("Translation Z");
		final AttributeOld<Double> rotationX = (AttributeOld<Double>) getAttribute("Rotation X");
		final AttributeOld<Double> rotationY = (AttributeOld<Double>) getAttribute("Rotation X");
		final AttributeOld<Double> rotationZ = (AttributeOld<Double>) getAttribute("Rotation X");
		final AttributeOld<Double> scaleX = (AttributeOld<Double>) getAttribute("Scale X");
		final AttributeOld<Double> scaleY = (AttributeOld<Double>) getAttribute("Scale X");
		final AttributeOld<Double> scaleZ = (AttributeOld<Double>) getAttribute("Scale X");
		final AttributeOld<Double> pivotX = (AttributeOld<Double>) getAttribute("Pivot X");
		final AttributeOld<Double> pivotY = (AttributeOld<Double>) getAttribute("Pivot X");
		final AttributeOld<Double> pivotZ = (AttributeOld<Double>) getAttribute("Pivot X");
		translationX.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				translation.x = translationX.getValue();
			}
		});
		translationY.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				translation.y = translationY.getValue();
			}
		});
		translationZ.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				translation.z = translationZ.getValue();
			}
		});
		rotationX.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				rotation.x = rotationX.getValue();
			}
		});
		rotationY.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				rotation.y = rotationY.getValue();
			}
		});
		rotationZ.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				rotation.z = rotationZ.getValue();
			}
		});
		scaleX.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				scale.x = scaleX.getValue();
			}
		});
		scaleY.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				scale.y = scaleY.getValue();
			}
		});
		scaleZ.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				scale.z = scaleZ.getValue();
			}
		});
		pivotX.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				pivot.x = pivotX.getValue();
			}
		});
		pivotY.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				pivot.y = pivotY.getValue();
			}
		});
		pivotZ.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				pivot.z = pivotZ.getValue();
			}
		});
	}
}
