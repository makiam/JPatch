package com.jpatch.entity;

import com.jpatch.afw.attributes.*;

public class NdeLayer extends MorphTarget {
	private BooleanAttr enabledAttr = new BooleanAttr(true);
	private GenericAttr<String> nameAttr = new GenericAttr<String>();
	
	public NdeLayer(String name) {
		nameAttr.setValue(name);
	}
	
	public BooleanAttr getEnabledAttribute() {
		return enabledAttr;
	}
	
	public GenericAttr<String> getNameAttribute() {
		return nameAttr;
	}
	
	@Override
	public void apply() {
		if (enabledAttr.getBoolean()) {
			super.apply();
		}
	}
}
