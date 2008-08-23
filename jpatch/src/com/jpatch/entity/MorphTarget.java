package com.jpatch.entity;

import java.util.*;

public class MorphTarget {
	private final Morph morph;
	private final Map<Accumulator, Integer> index = new HashMap<Accumulator, Integer>();
	private Accumulator[] accumulators = new Accumulator[0];
	private Accumulator[] values = new Accumulator[0];
	
	public MorphTarget(Morph morph) {
		this.morph = morph;
	}
	
	public final void addAccumulator(Accumulator accumulator, Object object) {
		final Integer position = index.get(accumulator);
		if (position == null) {
			final int n = accumulators.length;
			final Accumulator[] tmpAccumulators = new Accumulator[n + 1];
			System.arraycopy(accumulators, 0, tmpAccumulators, 0, n);
			tmpAccumulators[n] = accumulator;
			accumulators = tmpAccumulators;
			final Accumulator[] tmpValues = new Accumulator[n + 1];
			System.arraycopy(values, 0, tmpValues, 0, n);
			tmpValues[n] = accumulator.getValue();
			values = tmpValues;
			index.put(accumulator, n);
			morph.addAccumulator(accumulator, object);
		} else {
			values[position].accumulate(accumulator);
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
}
