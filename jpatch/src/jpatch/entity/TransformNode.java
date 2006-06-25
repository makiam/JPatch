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

public class TransformNode implements JPatchObject {
	public static final int ATTRIBUTE_COUNT = 20;
	
	public static final int NAME = 0;
	public static final int VISIBILITY = 1;
	public static final int POSITION_X = 2;
	public static final int POSITION_Y = 3;
	public static final int POSITION_Z = 4;
	public static final int TRANSLATION_X = 5;
	public static final int TRANSLATION_Y = 6;
	public static final int TRANSLATION_Z = 7;
	public static final int ROTATION_X = 8;
	public static final int ROTATION_Y = 9;
	public static final int ROTATION_Z = 10;
	public static final int SCALE_X = 11;
	public static final int SCALE_Y = 12;
	public static final int SCALE_Z = 13;
	public static final int PIVOT_POSITION_X = 14;
	public static final int PIVOT_POSITION_Y = 15;
	public static final int PIVOT_POSITION_Z = 16;
	public static final int PIVOT_TRANSLATION_X = 17;
	public static final int PIVOT_TRANSLATION_Y = 18;
	public static final int PIVOT_TRANSLATION_Z = 19;
	
	private Attribute.String name = new Attribute.String("Name");
	private Attribute.KeyedBoolean visibility = new Attribute.KeyedBoolean("Visibility", true);
	private Attribute.BoundedDouble positionX = new Attribute.BoundedDouble("Position X", 0.0);
	private Attribute.BoundedDouble positionY = new Attribute.BoundedDouble("Position Y", 0.0);
	private Attribute.BoundedDouble positionZ = new Attribute.BoundedDouble("Position Z", 0.0);
	private Attribute.BoundedDouble translationX = new Attribute.BoundedDouble("Translation X", 0.0);
	private Attribute.BoundedDouble translationY = new Attribute.BoundedDouble("Translation Y", 0.0);
	private Attribute.BoundedDouble translationZ = new Attribute.BoundedDouble("Translation Z", 0.0);
	private Attribute.BoundedDouble rotationX = new Attribute.BoundedDouble("Rotation X", 0.0);
	private Attribute.BoundedDouble rotationY = new Attribute.BoundedDouble("Rotation Y", 0.0);
	private Attribute.BoundedDouble rotationZ = new Attribute.BoundedDouble("Rotation Z", 0.0);
	private Attribute.BoundedDouble scaleX = new Attribute.BoundedDouble("Scale X", 1.0);
	private Attribute.BoundedDouble scaleY = new Attribute.BoundedDouble("Scale Y", 1.0);
	private Attribute.BoundedDouble scaleZ = new Attribute.BoundedDouble("Scale Z", 1.0);
	private Attribute.BoundedDouble pivotPositionX = new Attribute.BoundedDouble("Pivot Position X", 0.0);
	private Attribute.BoundedDouble pivotPositionY = new Attribute.BoundedDouble("Pivot Position Y", 0.0);
	private Attribute.BoundedDouble pivotPositionZ = new Attribute.BoundedDouble("Pivot Position Z", 0.0);
	private Attribute.BoundedDouble pivotTranslationX = new Attribute.BoundedDouble("Pivot Translation X", 0.0);
	private Attribute.BoundedDouble pivotTranslationY = new Attribute.BoundedDouble("Pivot Translation Y", 0.0);
	private Attribute.BoundedDouble pivotTranslationZ = new Attribute.BoundedDouble("Pivot Translation Z", 0.0);
	
	private TransformNode parent;
	private List<AnimObject> animObjects = new ArrayList<AnimObject>(1);
	private List<AnimObject> unmodifiableAnimObjects = Collections.unmodifiableList(animObjects);
	private List<TransformNode> childTransformNodes = new ArrayList<TransformNode>(1);
	private List<TransformNode> unmodifiableChildTransformNodes = Collections.unmodifiableList(childTransformNodes);
	private Matrix4d matrix = new Matrix4d();
	private Matrix4d inverseMatrix = new Matrix4d();
	private Matrix3d rotationMatrix = new Matrix3d();
	private Matrix3d scaleMatrix = new Matrix3d();
	private Vector3d translation = new Vector3d();
	private Rotation3d rotation = new Rotation3d();
	private Scale3d scale = new Scale3d();
	private Point3d pivot = new Point3d();
	
	private Iterable<Attribute> attributes = new Iterable<Attribute>() {
		public Iterator<Attribute> iterator() {
			return createAttributeIterator();
		}
	};
	
	private Iterable<Attribute> channels = new Iterable<Attribute>() {
		public Iterator<Attribute> iterator() {
			return createChannelIterator();
		}
	};
	
	public TransformNode() {
		matrix.setIdentity();
		addAttributeChangeListeners();
	}
	
	public Iterable<Attribute> getAttributes() {
		return attributes;
	}
	
	public Iterable<Attribute> getChannels() {
		return channels;
	}
	
	public Attribute getAttribute(int index) {
		switch (index) {
		case NAME:
			return name;
		case VISIBILITY:
			return visibility;
		case POSITION_X:
			return positionX;
		case POSITION_Y:
			return positionY;
		case POSITION_Z:
			return positionZ;
		case TRANSLATION_X:
			return translationX;
		case TRANSLATION_Y:
			return translationY;
		case TRANSLATION_Z:
			return translationZ;
		case ROTATION_X:
			return rotationX;
		case ROTATION_Y:
			return rotationY;
		case ROTATION_Z:
			return rotationZ;
		case SCALE_X:
			return scaleX;
		case SCALE_Y:
			return scaleY;
		case SCALE_Z:
			return scaleZ;
		case PIVOT_POSITION_X:
			return pivotPositionX;
		case PIVOT_POSITION_Y:
			return pivotPositionY;
		case PIVOT_POSITION_Z:
			return pivotPositionZ;
		case PIVOT_TRANSLATION_X:
			return pivotTranslationX;
		case PIVOT_TRANSLATION_Y:
			return pivotTranslationY;
		case PIVOT_TRANSLATION_Z:
			return pivotTranslationZ;
		default:
			return null;
		}
	}
	
	private Iterator<Attribute> createAttributeIterator() {
		return new Iterator<Attribute>() {
			private int index = 0;
			
			public boolean hasNext() {
				return index < ATTRIBUTE_COUNT;
			}

			public Attribute next() {
				return getAttribute(index++);
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	private Iterator<Attribute> createChannelIterator() {
		return new Iterator<Attribute>() {
			private int index = searchNextChannel();
			
			public boolean hasNext() {
				return index < ATTRIBUTE_COUNT;
			}

			public Attribute next() {
				Attribute a = getAttribute(index++);
				searchNextChannel();
				return a;
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			private int searchNextChannel() {
				for (; index < ATTRIBUTE_COUNT; index++)
					if (getAttribute(index).isKeyed())
						break;
				if (index < ATTRIBUTE_COUNT)
					index--;
				return index;
			}
		};
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
		computeDerivedAttributes();
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
		inverseMatrix.invert(matrix);
	}
	
	private void computeDerivedAttributes() {
		translationChanged();
		pivotChanged();
	}
	
	@SuppressWarnings("unchecked")
	private void addAttributeChangeListeners() {
		positionX.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				positionChanged();
			}
		});
		positionY.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				positionChanged();
			}
		});
		positionZ.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				positionChanged();
			}
		});
		translationX.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				translation.x = translationX.get();
				translationChanged();
			}
		});
		translationY.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				translation.y = translationY.get();
				translationChanged();
			}
		});
		translationZ.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				translation.z = translationZ.get();
				translationChanged();
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
		pivotPositionX.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				worldPivotChanged();
			}
		});
		pivotPositionY.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				worldPivotChanged();
			}
		});
		pivotPositionZ.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				worldPivotChanged();
			}
		});
		pivotTranslationX.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				pivot.x = pivotTranslationX.get();
				pivotChanged();
			}
		});
		pivotTranslationY.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				pivot.y = pivotTranslationY.get();
				pivotChanged();
			}
		});
		pivotTranslationZ.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				pivot.z = pivotTranslationZ.get();
				pivotChanged();
			}
		});
	}
	
	private void positionChanged() {
		Point3d tmp = new Point3d(positionX.get(), positionY.get(), positionZ.get());
		if (parent != null)
			parent.inverseMatrix.transform(tmp);
		positionX.setValueAdjusting(true);
		positionY.setValueAdjusting(true);
		positionZ.setValueAdjusting(true);
		translationX.set(tmp.x);
		translationY.set(tmp.y);
		translationZ.set(tmp.z);
		positionX.setValueAdjusting(false);
		positionY.setValueAdjusting(false);
		positionZ.setValueAdjusting(false);
	}
	
	private void translationChanged() {
		Point3d tmp = new Point3d(translation);
		if (parent != null)
			parent.matrix.transform(tmp);
		translationX.setValueAdjusting(true);
		translationY.setValueAdjusting(true);
		translationZ.setValueAdjusting(true);
		positionX.set(tmp.x);
		positionY.set(tmp.y);
		positionZ.set(tmp.z);
		translationX.setValueAdjusting(false);
		translationY.setValueAdjusting(false);
		translationZ.setValueAdjusting(false);
	}
	
	private void worldPivotChanged() {
		Point3d tmp = new Point3d(pivotPositionX.get(), pivotPositionY.get(), pivotPositionZ.get());
		if (parent != null)
			parent.inverseMatrix.transform(tmp);
		pivotPositionX.setValueAdjusting(true);
		pivotPositionY.setValueAdjusting(true);
		pivotPositionZ.setValueAdjusting(true);
		pivotTranslationX.set(tmp.x);
		pivotTranslationY.set(tmp.y);
		pivotTranslationZ.set(tmp.z);
		pivotPositionX.setValueAdjusting(false);
		pivotPositionY.setValueAdjusting(false);
		pivotPositionZ.setValueAdjusting(false);
	}
	
	private void pivotChanged() {
		Point3d tmp = new Point3d(pivot);
		if (parent != null)
			parent.matrix.transform(tmp);
		pivotTranslationX.setValueAdjusting(true);
		pivotTranslationY.setValueAdjusting(true);
		pivotTranslationZ.setValueAdjusting(true);
		pivotPositionX.set(tmp.x);
		pivotPositionY.set(tmp.y);
		pivotPositionZ.set(tmp.z);
		pivotTranslationX.setValueAdjusting(false);
		pivotTranslationY.setValueAdjusting(false);
		pivotTranslationZ.setValueAdjusting(false);
	}
}
