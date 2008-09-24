package com.jpatch.entity;

import java.util.*;

import javax.vecmath.*;

@SuppressWarnings("serial")
public class Tuple3Accumulator implements Accumulator {
	private final Tuple3d t = new Point3d();
	private final Tuple3d tuple;
	
	public Tuple3Accumulator(Tuple3d tuple) {
		this.tuple = tuple;
	}
	
	public void accumulate(Accumulator acc) {
		t.add(((Tuple3Accumulator) acc).t);
		tuple.set(t);
//		System.out.println(this + " accumulate, tuple=" + tuple);
	}
	
	public void set(Accumulator acc) {
		tuple.add(t, ((Tuple3Accumulator) acc).t);
//		System.out.println(this + " set " + acc + ", tuple=" + tuple);
	}
	
	public void reset() {
		t.set(0, 0, 0);
		tuple.set(0, 0, 0);
//		System.out.println(this + " reset");
	}

	public boolean isZero() {
		return t.x == 0 && t.y == 0 && t.z == 0;
	}
	
	public Tuple3Accumulator getValue() {
		Tuple3Accumulator a = new Tuple3Accumulator(null);
		a.t.set(t);
		return a;
	}
	
	public Tuple3d asTuple() {
		return t;
	}
	
	public void setTuple(double x, double y, double z) {
		t.set(x, y, z);
	}
	
	public void setTuple(Tuple3d tuple) {
		setTuple(tuple.x, tuple.y, tuple.z);
	}
	
	public Tuple3d getTuple(Tuple3d tuple) {
		tuple.set(t);
		return tuple;
	}
	
	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}
	
	@Override
	public boolean equals(Object o) {
		return o == this;
	}
	
	private static String toString(Tuple3d tuple) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		sb.append("[");
		if (tuple != null) {
			formatter.format("%3.3f", tuple.x);
			sb.append(",");
			formatter.format("%3.3f", tuple.y);
			sb.append(",");
			formatter.format("%3.3f", tuple.z);
			sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}
	
	public String toString() {
		return "Tuple3Accumulator@" + System.identityHashCode(this) + toString(this.t) + toString(tuple);
	}
}
