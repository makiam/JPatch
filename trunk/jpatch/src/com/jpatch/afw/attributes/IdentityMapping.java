package com.jpatch.afw.attributes;

public class IdentityMapping implements Mapping {

	public double getMappedValue(double value) {
		return value;
	}

	public double getValue(double mappedValue) {
		return mappedValue;
	}

}