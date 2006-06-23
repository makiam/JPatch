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
	private Attribute[] attributes = new Attribute[] {
			new Attribute.String("Name"),
			new Attribute.KeyedBoolean("Visibility", true),
			new Attribute.BoundedDouble("Translation X", 0.0),
			new Attribute.BoundedDouble("Translation Y", 0.0),
			new Attribute.BoundedDouble("Translation Z", 0.0),
			new Attribute.BoundedDouble("Rotation X", 0.0),
			new Attribute.BoundedDouble("Rotation Y", 0.0),
			new Attribute.BoundedDouble("Rotation Z", 0.0),
			new Attribute.BoundedDouble("Scale X", 0.0),
			new Attribute.BoundedDouble("Scale Y", 0.0),
			new Attribute.BoundedDouble("Scale Z", 0.0),
			new Attribute.BoundedDouble("Pivot X", 0.0),
			new Attribute.BoundedDouble("Pivot Y", 0.0),
			new Attribute.BoundedDouble("Pivot Z", 0.0),
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
	public Attribute getAttribute(String name) {
		for (Attribute attribute : attributes)
			if (name.equals(attribute.getName()))
				return attribute;
		return null;
	}
	
	public Attribute[] getAttributes() {
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
		final Attribute.Double translationX = (Attribute.Double) getAttribute("Translation X");
		final Attribute.Double translationY = (Attribute.Double) getAttribute("Translation Y");
		final Attribute.Double translationZ = (Attribute.Double) getAttribute("Translation Z");
		final Attribute.Double rotationX = (Attribute.Double) getAttribute("Rotation X");
		final Attribute.Double rotationY = (Attribute.Double) getAttribute("Rotation X");
		final Attribute.Double rotationZ = (Attribute.Double) getAttribute("Rotation X");
		final Attribute.Double scaleX = (Attribute.Double) getAttribute("Scale X");
		final Attribute.Double scaleY = (Attribute.Double) getAttribute("Scale X");
		final Attribute.Double scaleZ = (Attribute.Double) getAttribute("Scale X");
		final Attribute.Double pivotX = (Attribute.Double) getAttribute("Pivot X");
		final Attribute.Double pivotY = (Attribute.Double) getAttribute("Pivot X");
		final Attribute.Double pivotZ = (Attribute.Double) getAttribute("Pivot X");
		translationX.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				translation.x = translationX.get();
			}
		});
		translationY.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				translation.y = translationY.get();
			}
		});
		translationZ.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				translation.z = translationZ.get();
			}
		});
		rotationX.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				rotation.x = rotationX.get();
			}
		});
		rotationY.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				rotation.y = rotationY.get();
			}
		});
		rotationZ.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				rotation.z = rotationZ.get();
			}
		});
		scaleX.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				scale.x = scaleX.get();
			}
		});
		scaleY.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				scale.y = scaleY.get();
			}
		});
		scaleZ.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				scale.z = scaleZ.get();
			}
		});
		pivotX.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				pivot.x = pivotX.get();
			}
		});
		pivotY.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				pivot.y = pivotY.get();
			}
		});
		pivotZ.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				pivot.z = pivotZ.get();
			}
		});
	}
}
