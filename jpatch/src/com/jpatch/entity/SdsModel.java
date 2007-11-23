package com.jpatch.entity;

import com.jpatch.afw.attributes.GenericAttr;
import com.jpatch.entity.sds.Sds;

public class SdsModel extends TransformNode {
	private final Sds sds;
	
	public SdsModel(Sds sds) {
		this.sds = sds;
	}
	
	public Sds getSds() {
		return sds;
	}
}
