package com.jpatch.entity; 

import java.util.*;

public class MorphTarget {
	private final Set<Object> objects = new HashSet<Object>();
	private final Set<Object> objectsRO = Collections.unmodifiableSet(objects);
	private Accumulator[] accumulators = new Accumulator[0];
	private double[] vectors = new double[0];
	
	public void setVector(Accumulator accumulator, double vector) {
		int index = Arrays.binarySearch(accumulators, accumulator);
		if (index < 0) {
			if (vector == 0) {
				return;	// no need to add new accumulator if vector is 0
			}
			/* add new accumulator */
			index = -index - 1;
			addAccumulator(accumulator, index);
			vectors[index] = vector;
		} else {
			vectors[index] = vector;
			if (vectors[index] == 0) {
				removeAccumulator(index);
			}
		}
	}
	
	public void addObject(Object object) {
		objects.add(object);
	}
	
	public Set<Object> getObjects() {
		return objectsRO;
	}
	
	public void addVector(Accumulator accumulator, double vector) {
		int index = Arrays.binarySearch(accumulators, accumulator);
		if (index < 0) {
			if (vector == 0) {
				return;	// no need to add new accumulator if vector is 0
			}
			/* add new accumulator */
			index = -index - 1;
			addAccumulator(accumulator, index);
			vectors[index] = vector;
		} else {
			vectors[index] += vector;
			if (vectors[index] == 0) {
				removeAccumulator(index);
			}
		}
	}
	
	private void addAccumulator(Accumulator accumulator, int index) {
		int insertPosition = -Arrays.binarySearch(accumulators, accumulator) -1;
		accumulators = arrayInsert(accumulators, insertPosition, accumulator);
		vectors = arrayInsert(vectors, insertPosition, 0.0);
	}
	
	void apply() {
		for (int i = 0; i < accumulators.length; i++) {
			accumulators[i].add(vectors[i]);
		}
	}
	
	public void reset() {
		for (int i = 0; i < accumulators.length; i++) {
			accumulators[i].reset();
		}
	}
	
	void apply(final double weight) {
		for (int i = 0; i < accumulators.length; i++) {
			accumulators[i].add(vectors[i] * weight);
		}
	}
	
	
//	private void removeAccumulator(Accumulator accumulator) {
//		final int index = Arrays.binarySearch(accumulators, accumulator);
//		removeAccumulator(index);
//	}
	
	private void removeAccumulator(int index) {
		accumulators = arrayRemove(accumulators, index);
		vectors = arrayRemove(vectors, index);
	}
	
	private static final double[] arrayInsert(double[] array, int position, double value) {
		final double[] tmp = new double[array.length + 1];
		System.arraycopy(array, 0, tmp, 0, position);
		tmp[position] = value;
		System.arraycopy(array, position, tmp, position + 1, array.length - position);
		return tmp;
	}
	
	private static final double[] arrayRemove(double[] array, int position) {
		final double[] tmp = new double[array.length - 1];
		System.arraycopy(array, 0, tmp, 0, position);
	    System.arraycopy(array, position + 1, tmp, position, tmp.length - position);
	   return tmp;
	}
	
	private static final Accumulator[] arrayInsert(Accumulator[] array, int position, Accumulator value) {
		final Accumulator[] tmp = new Accumulator[array.length + 1];
		System.arraycopy(array, 0, tmp, 0, position);
		tmp[position] = value;
		System.arraycopy(array, position, tmp, position + 1, array.length - position);
		return tmp;
	}
	
	private static final Accumulator[] arrayRemove(Accumulator[] array, int position) {
		final Accumulator[] tmp = new Accumulator[array.length - 1];
		System.arraycopy(array, 0, tmp, 0, position);
	    System.arraycopy(array, position + 1, tmp, position, tmp.length - position);
	   return tmp;
	}
	
	private void dump() {
		for (int i = 0; i < accumulators.length; i++) {
			System.out.println(accumulators[i] + "\t" + vectors[i]);
		}
	}
	
//	public static void main(String[] args) {
//		MorphTarget mt = new MorphTarget();
//		Accumulator a = new Accumulator();
//		Accumulator b = new Accumulator();
//		Accumulator c = new Accumulator();
//		
//		System.out.println("a = " + a);
//		System.out.println("b = " + b);
//		System.out.println("c = " + c);
//		mt.addAccumulator(a);
//		mt.addAccumulator(b);
//		mt.addAccumulator(c);
//		
//		mt.setVector(c, 5);
//		mt.dump();
//		System.out.println();
//		mt.removeAccumulator(c);
//		mt.dump();
//	}
}
