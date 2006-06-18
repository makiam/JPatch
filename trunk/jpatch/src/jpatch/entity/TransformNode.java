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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.MutableTreeNode;
import javax.vecmath.*;
import jpatch.auxilary.*;

public class TransformNode implements JPatchObject {
	private Attribute<Boolean> visibility = new Attribute<Boolean>("Visibility", true);
	private Attribute<Double> translationX = new Attribute<Double>("Position X", 0.0);
	private Attribute<Double> translationY = new Attribute<Double>("Position Y", 0.0);
	private Attribute<Double> translationZ = new Attribute<Double>("Position Z", 0.0);
	private Attribute<Double> rotationX = new Attribute<Double>("Rotation X", 0.0);
	private Attribute<Double> rotationY = new Attribute<Double>("Rotation Y", 0.0);
	private Attribute<Double> rotationZ = new Attribute<Double>("Rotation Z", 0.0);
	private Attribute<Double> scaleX = new Attribute<Double>("Scale X", 1.0);
	private Attribute<Double> scaleY = new Attribute<Double>("Scale Y", 1.0);
	private Attribute<Double> scaleZ = new Attribute<Double>("Scale Z", 1.0);
	private Attribute<Double> pivotX = new Attribute<Double>("Pivot X", 0.0);
	private Attribute<Double> pivotY = new Attribute<Double>("Pivot Y", 0.0);
	private Attribute<Double> pivotZ = new Attribute<Double>("Pivot Z", 0.0);
	private Attribute[] attributes = new Attribute[] {
			pivotX,
			pivotY,
			pivotZ
	};
	private Attribute[] channels = new Attribute[] {
			visibility,
			translationX,
			translationY,
			translationZ,
			rotationX,
			rotationY,
			rotationZ,
			scaleX,
			scaleY,
			scaleZ
	};
	private TransformNodeTreeNode treeNode = new TransformNodeTreeNode(this);
	private JPatchObject object;
	private TransformNode parent;
	private List<TransformNode> children = new ArrayList<TransformNode>(1);
	private Matrix4d matrix = new Matrix4d();
	private Matrix3d rotationMatrix = new Matrix3d();
	private Matrix3d scaleMatrix = new Matrix3d();
	private Vector3d translation = new Vector3d();
	private Rotation3d rotation = new Rotation3d();
	private Scale3d scale = new Scale3d();
	private Point3d pivot = new Point3d();
	private String name;
	
	public TransformNode(JPatchObject object) {
		this.object = object;
		matrix.setIdentity();
		addAttributeChangeListeners();
	}
	
	public String getId() {
		return name;
	}
	
	public boolean isVisible() {
		return visibility.getValue();
	}
	
	public Matrix4d getMatrix() {
		return matrix;
	}
	
	public List<TransformNode> getChildren() {
		return children;
	}
	
	public JPatchObject getObject() {
		return object;
	}
	
	public void setObject(JPatchObject object) {
		this.object = object;
	}
	
	public void setParent(TransformNode parent) {
		this.parent = parent;
	}
	
	public TransformNode getParent() {
		return parent;
	}
	
	public TransformNodeTreeNode getTreeNode() {
		return treeNode;
	}
	
	public void computeBranch() {
		computeMatrix();
		for (TransformNode child : children)
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
	
	private void addAttributeChangeListeners() {
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
