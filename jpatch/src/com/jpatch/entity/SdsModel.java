package com.jpatch.entity;

import com.jpatch.afw.attributes.GenericAttr;
import com.jpatch.entity.sds.Sds;

public class SdsModel extends TransformNode {
	private final GenericAttr<String> nameAttr = new GenericAttr<String>();
	private final Sds sds;
	
	public SdsModel(Sds sds) {
		this.sds = sds;
	}
	
	public Sds getSds() {
		return sds;
	}
	
	public GenericAttr<String> getNameAttribute() {
		return nameAttr;
	}
}
