package com.jpatch.entity;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.ui.*;
import com.jpatch.entity.sds2.Sds;

public class SdsModel extends XFormNode {
	private final Sds sds;
	
	public SdsModel(Sds sds) {
		this.sds = sds;
		
	}
	
	public Sds getSds() {
		return sds;
	}
	
	public IntAttr getMaxLevelAttribute() {
		return sds.getMaxLevelAttribute();
	}
	
	public IntAttr getRenderLevelAttribute() {
		return sds.getRenderLevelAttribute();
	}
	
	public IntAttr getEditLevelAttribute() {
		return sds.getEditLevelAttribute();
	}
}
