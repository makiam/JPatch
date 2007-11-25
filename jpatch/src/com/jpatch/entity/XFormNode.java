package com.jpatch.entity;

import com.jpatch.afw.vecmath.AbstractTransformUtil;

public class XFormNode extends SceneGraphNode {
//	public static final int TRANSLATION = 0;
//	public static final int SCALE = 1;
//	public static final int SHEAR = 2;
//	public static final int AXIS_ROTATION = 3;
//	public static final int ROTATION = 4;
//	private final int[] transformOrder = new int[] { SCALE, SHEAR, AXIS_ROTATION, ROTATION, TRANSLATION };
//	private final int[] transformPrev = new int[5];
//	private final int[] transformNext = new int[5];
	
	public static final int TRANSLATION = 0;
//	public static final int SCALE = 1;
	private final AbstractTransformUtil transformUtil = new AbstractTransformUtil("axisRotation", "local") {
		
	};
	
	XFormNode() {
		for (int i = 0; i < transformOrder.length; i++) {
			transformPrev[transformOrder[i]] = (i > 0) ? transformOrder[i - 1] : -1;
			transformNext[transformOrder[i]] = (i < transformOrder.length - 1) ? transformOrder[i + 1] : -1;
		}
		for (int i = 0; i < transformOrder.length; i++) {
			int index = transformOrder[i];
			System.out.println(index + "\t prev=" + transformPrev[index] + "\t this=" + index + "\t next=" + transformNext[index]);
		}
	}
	
	public static void main(String[] args) {
		new XFormNode();
	}
}
