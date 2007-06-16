package com.jpatch.afw.attributes;

public class IdentityMapping implements Mapping {
	private static final IdentityMapping INSTANCE = new IdentityMapping();
	
	private IdentityMapping() { }
	
	public static IdentityMapping getInstance() {
		return INSTANCE;
	}
	
	public double getMappedValue(double value) {
		return value;
	}

	public double getValue(double mappedValue) {
		return mappedValue;
	}

}