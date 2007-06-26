package com.jpatch.afw.attributes;

public class Lock implements AttributePreChangeListener {
	private static final Lock INSTANCE = new Lock();
	
	private Lock() { }
	
	public static Lock getInstance() {
		return INSTANCE;
	}
	
	public boolean attributeWillChange(ScalarAttribute source, boolean value) {
		return ((BooleanAttr) source).getBoolean();
	}

	public double attributeWillChange(ScalarAttribute source, double value) {
		return ((DoubleAttr) source).getDouble();
	}

	public int attributeWillChange(ScalarAttribute source, int value) {
		return ((IntAttr) source).getInt();
	}

	public Object attributeWillChange(ScalarAttribute source, Object value) {
		return ((GenericAttr) source).getValue();
	}
}
