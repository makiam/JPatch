package com.jpatch.entity;

import java.util.*;

public class MorphTarget {
	private final Morph<? extends MorphTarget> morph;
	private final Map<Accumulator, Integer> index = new HashMap<Accumulator, Integer>();
	private Accumulator[] accumulators = new Accumulator[0];
	private Accumulator[] values = new Accumulator[0];
	
	public MorphTarget(Morph<? extends MorphTarget> morph) {
		this.morph = morph;
	}
	
	public final Accumulator getAccumulatorValueFor(Accumulator accumulator, Object object) {
		System.out.println("gettint accumulator for " + object);
		final Integer position = index.get(accumulator);
		if (position == null) {
			System.out.println("adding accumulator for " + object);
			final int n = accumulators.length;
			final Accumulator[] tmpAccumulators = new Accumulator[n + 1];
			System.arraycopy(accumulators, 0, tmpAccumulators, 0, n);
			tmpAccumulators[n] = accumulator;
			accumulators = tmpAccumulators;
			final Accumulator[] tmpValues = new Accumulator[n + 1];
			System.arraycopy(values, 0, tmpValues, 0, n);
			final Accumulator value = accumulator.getValue();
			tmpValues[n] = value;
			values = tmpValues;
			index.put(accumulator, n);
			morph.addAccumulator(accumulator, object);
			return value;
		} else {
			return values[position];
		}
	}
	
	private final void removeAccumulator(Accumulator accumulator) {
		final int pos = index.get(accumulator);
		final int n = accumulators.length;
		final Accumulator[] tmpAccumulators = new Accumulator[n - 1];
		System.arraycopy(accumulators, 0, tmpAccumulators, 0, pos);
	    System.arraycopy(accumulators, pos + 1, tmpAccumulators, pos, n - pos - 1);
	    accumulators = tmpAccumulators;
	    final Accumulator[] tmpValues = new Accumulator[n - 1];
		System.arraycopy(values, 0, tmpValues, 0, pos);
	    System.arraycopy(accumulators, pos + 1, tmpValues, pos, n - pos - 1);
	    values = tmpValues;
	    index.remove(accumulator);
	    morph.removeAccumulator(accumulator);
	}
	
	void apply() {
		for (int i = 0; i < accumulators.length; i++) {
			accumulators[i].accumulate(values[i]);
		}
	}
	
	void set() {
		for (int i = 0; i < accumulators.length; i++) {
			accumulators[i].set(values[i]);
		}
	}
	
	Accumulator[] getValues() {
		return values;
	}
}
