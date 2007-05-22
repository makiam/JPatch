package com.jpatch.entity;

import com.jpatch.afw.attributes.TransformedPoint3;
import com.jpatch.afw.attributes.TransformedVector3;

public class TransformNode {
	protected TransformedPoint3 position = new TransformedPoint3(0, 0, 0);
	protected TransformedVector3 rotation = new TransformedVector3(0, 0, 0);
}
