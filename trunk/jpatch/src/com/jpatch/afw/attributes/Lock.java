package com.jpatch.afw.attributes;

public class Lock implements AttributePreChangeListener {
	private static final Lock INSTANCE = new Lock();
	
	private Lock() { }
	
	public static Lock getInstance() {
		return INSTANCE;
	}
	
	public boolean attributeWillChange(Attribute source, boolean value) {
		if (source instanceof Toggle) {
			return ((Toggle) source).getBoolean();
		} else {
			return ((BooleanAttr) source).getBoolean();
		}
	}

	public double attributeWillChange(Attribute source, double value) {
		return ((DoubleAttr) source).getDouble();
	}

	public int attributeWillChange(Attribute source, int value) {
		return ((IntAttr) source).getInt();
	}

	public Object attributeWillChange(Attribute source, Object value) {
		if (source instanceof StateMachine) {
			return ((StateMachine) source).getState();
		} else {
			return ((GenericAttr) source).getValue();
		}
	}
}
