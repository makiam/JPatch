package com.jpatch.entity;

import com.jpatch.entity.sds2.Sds;

public class SdsModel extends XFormNode {
	private final Sds sds;
	
	public SdsModel(Sds sds) {
		this.sds = sds;
	}
	
	public Sds getSds() {
		return sds;
	}
}
