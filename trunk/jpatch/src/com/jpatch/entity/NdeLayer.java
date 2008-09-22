package com.jpatch.entity;

import com.jpatch.afw.attributes.*;

public class NdeLayer extends MorphTarget {
	private BooleanAttr enabledAttr = new BooleanAttr(true);
	
	
	public NdeLayer(Morph morph) {
		super(morph);
	}
	
	public BooleanAttr getEnabledAttribute() {
		return enabledAttr;
	}
	
	
	
	@Override
	public void apply() {
		if (enabledAttr.getBoolean()) {
			super.apply();
		}
	}
}
