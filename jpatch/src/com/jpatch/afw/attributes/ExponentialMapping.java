package com.jpatch.afw.attributes;

public class ExponentialMapping implements Mapping {
    private static final ExponentialMapping INSTANCE = new ExponentialMapping();
	
	private ExponentialMapping() { }
	
	public static ExponentialMapping getInstance() {
		return INSTANCE;
	}
	
    @Override
	public double getMappedValue(double value) {
		return Math.exp(value);
	}

    @Override
	public double getValue(double mappedValue) {
		return Math.log(mappedValue);
	}

}
