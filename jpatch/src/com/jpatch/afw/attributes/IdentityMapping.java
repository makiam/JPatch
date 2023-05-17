package com.jpatch.afw.attributes;

public class IdentityMapping implements Mapping {
	private static final IdentityMapping INSTANCE = new IdentityMapping();
	
	private IdentityMapping() { }
	
	public static IdentityMapping getInstance() {
		return INSTANCE;
	}
	
        @Override
	public double getMappedValue(double value) {
		return value;
	}

        @Override
	public double getValue(double mappedValue) {
		return mappedValue;
	}

}
