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
import javax.vecmath.*;
import jpatch.auxilary.*;

public class TransformNode extends AbstractTransform {
	
	public Attribute.KeyedBoolean visibility = new Attribute.KeyedBoolean("Visibility", true);
	public Attribute.Enum rotationOrder = new Attribute.Enum("Order", Rotation3d.Order.XYZ);
	public Attribute.Point3d position = new Attribute.Point3d("Position", new Point3d(0, 0, 0), false);
	public Attribute.Vector3d translation = new Attribute.Vector3d("Translation", new Vector3d(0, 0, 0), true);
	public Attribute.Rotation3d orientation = new Attribute.Rotation3d("Orientation", new Rotation3d(0, 0, 0), false);
	public Attribute.Rotation3d rotation = new Attribute.Rotation3d("Rotation", new Rotation3d(0, 0, 0), true);
	public Attribute.Scale3d scale = new Attribute.Scale3d("Scale", new Scale3d(1, 1, 1), true);
	public Attribute.Point3d scalePivotPosition = new Attribute.Point3d("Pivot (world)", new Point3d(0, 0, 0), false);
	public Attribute.Vector3d scalePivotTranslation = new Attribute.Vector3d("Pivot (local)", new Vector3d(0, 0, 0), false);
	public Attribute.Point3d rotatePivotPosition = new Attribute.Point3d("Pivot (world)", new Point3d(0, 0, 0), false);
	public Attribute.Vector3d rotatePivotTranslation = new Attribute.Vector3d("Pivot (local)", new Vector3d(0, 0, 0), false);
	public Attribute.Scale3d shear = new Attribute.Scale3d("Shear", new Scale3d(0, 0, 0), true);
	
	private TransformNode parent;
	private List<AnimObject> animObjects = new ArrayList<AnimObject>(1);
	private List<AnimObject> unmodifiableAnimObjects = Collections.unmodifiableList(animObjects);
	
	private Matrix3d rotationMatrix = new Matrix3d();
	private Matrix3d scaleMatrix = new Matrix3d();
	private Vector3d translationTuple = new Vector3d();
	private Rotation3d rotationTuple = new Rotation3d();
	private Scale3d scaleTuple = new Scale3d();
	private Point3d pivot = new Point3d();
	
	
	
	public TransformNode() {
		addAttributeChangeListeners();
	}
	
	public List<AnimObject> getAnimObjects() {
		return unmodifiableAnimObjects;
	}
	
	@Override
	protected void computeMatrix() {
		scale.get(scaleTuple);
		scaleTuple.setMatrixScale(scaleMatrix);
		rotation.get(rotationTuple);
		rotationTuple.setMatrixRotation(rotationMatrix);
		scaleMatrix.mul(rotationMatrix);
		matrix.setRotationScale(scaleMatrix);
		translation.get(translationTuple);
		matrix.setTranslation(translationTuple);
		if (parent != null)
			parent.multiply(matrix);
		inverseInvalid = true;
	}
	
	@Override
	protected void computeDerivedAttributes() {
		translationChanged(translation, position);
		translationChanged(scalePivotTranslation, scalePivotPosition);
		translationChanged(rotatePivotTranslation, rotatePivotPosition);
	}
	
	@SuppressWarnings("unchecked")
	private void addAttributeChangeListeners() {
//		rotation.order.addAttributeListener(new AttributeListener() {
//			public void attributeChanged(Attribute attribute) {
//				orientation.order.set(rotation.order.get());
//			}
//		});
		
		position.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				positionChanged(position, translation);
				computeBranch();
			}
		});
		translation.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				translationChanged(translation, position);
				computeBranch();
			}
		});
		scalePivotPosition.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				positionChanged(scalePivotPosition, scalePivotTranslation);
			}
		});
		scalePivotTranslation.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				translationChanged(scalePivotTranslation, scalePivotPosition);
			}
		});
		rotatePivotPosition.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				positionChanged(rotatePivotPosition, rotatePivotTranslation);
			}
		});
		rotatePivotTranslation.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				translationChanged(rotatePivotTranslation, rotatePivotPosition);
			}
		});
	}
	
	
}
