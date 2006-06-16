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

public class TransformNode implements Animatable {
	private final static String[] CHANNELS = new String[] {
		"VISIBILITY",
		"TRANSLATE_X",
		"TRANSLATE_Y",
		"TRANSLATE_Z",
		"ROTATE_X",
		"ROTATE_Y",
		"ROTATE_Z",
		"SCALE_X",
		"SCALE_Y",
		"SCALE_Z"
	};
	private MotionCurveNew[] motionCurves = new MotionCurveNew[CHANNELS.length];
	private TransformNode parent;
	private List<TransformNode> children = new ArrayList<TransformNode>();
	private Matrix4d matrix = new Matrix4d();
	private Matrix3d rotationMatrix = new Matrix3d();
	private Matrix3d scaleMatrix = new Matrix3d();
	private boolean visible;
	private Vector3d translation = new Vector3d();
	private Rotation3d rotation = new Rotation3d();
	private Scale3d scale = new Scale3d();
	private String name;
	
	public TransformNode() {
		matrix.setIdentity();
	}
	
	public String getId() {
		return name;
	}
	
	public String[] getChannels() {
		return CHANNELS;
	}
	
	public double getChannel(int n) {
		switch (n) {
		case 0:
			return visible ? 1 : 0;
		case 1:
			return translation.x;
		case 2:
			return translation.y;
		case 3:
			return translation.z;
		case 4:
			return rotation.x;
		case 5:
			return rotation.y;
		case 6:
			return rotation.z;
		case 7:
			return scale.x;
		case 8:
			return scale.y;
		case 9:
			return scale.z;
		}
		throw new ArrayIndexOutOfBoundsException(n);
	}
	
	public void setChannel(int n, double value) {
		switch (n) {
		case 0:
			visible = value > 0.5;
		case 1:
			translation.x = value;
			break;
		case 2:
			translation.y = value;
			break;
		case 3:
			translation.z = value;
			break;
		case 4:
			rotation.x = value;
			break;
		case 5:
			rotation.y = value;
			break;
		case 6:
			rotation.z = value;
			break;
		case 7:
			scale.x = value;
			break;
		case 8:
			scale.y = value;
			break;
		case 9:
			scale.z = value;
			break;
		}
	}
	
	public void setMotionCurveForChannel(int channelNumber, MotionCurveNew motionCurve) {
		motionCurves[channelNumber] = motionCurve;
	}
	
	public MotionCurveNew getMotionCurveForChannel(int channelNumber) {
		return motionCurves[channelNumber];
	}
	
	public void update(double position) {
		for (int i = 0; i < CHANNELS.length; i++)
			setChannel(i, motionCurves[i].getValueAt(position));
	}
	
	public int getChannelNumberByName(String name) {
		for (int i = 0; i < CHANNELS.length; i++) {
			if (name.equals(CHANNELS[i]))
				return i;
		}
		return -1;
	}
	
	public Matrix4d getMatrix() {
		return matrix;
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
}
