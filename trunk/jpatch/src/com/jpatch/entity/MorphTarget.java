package com.jpatch.entity;

import com.jpatch.afw.attributes.*;

import java.util.*;

import javax.vecmath.*;

public class MorphTarget {
	private final Morph<? extends MorphTarget> morph;
	private final Map<Tuple3Accumulator, Integer> accumulatorIndex = new HashMap<Tuple3Accumulator, Integer>();
	private Tuple3Accumulator[] accumulators = new Tuple3Accumulator[0];
	private Tuple3d[] values = new Tuple3d[0];
	private GenericAttr<String> nameAttr = new GenericAttr<String>("New NDE Layer");
	
	public MorphTarget(Morph<? extends MorphTarget> morph) {
		this.morph = morph;
	}
	
	public GenericAttr<String> getNameAttribute() {
		return nameAttr;
	}
	
	public final Tuple3d getValueFor(final Tuple3Accumulator accumulator, final Object object) {
		final Integer position = accumulatorIndex.get(accumulator);
		if (position == null) {
			System.out.println("adding accumulator for " + object);
			
			final int n = accumulators.length;
			
			final Tuple3Accumulator[] tpmAccumulators = new Tuple3Accumulator[n + 1];
			System.arraycopy(accumulators, 0, tpmAccumulators, 0, n);
			tpmAccumulators[n] = accumulator;
			accumulators = tpmAccumulators;
			
			final Tuple3d[] tmpValues = new Tuple3d[n + 1];
			System.arraycopy(values, 0, tmpValues, 0, n);
			final Tuple3d value = new Point3d();
			tmpValues[n] = value;
			values = tmpValues;
			
			accumulatorIndex.put(accumulator, n);
			morph.addAccumulator(accumulator, object, value);
			
			return value;
		} else {
			return values[position];
		}
	}
	
	private final void removeTarget(Tuple3Accumulator accumulator) {
		final int pos = accumulatorIndex.get(accumulator);
		final int n = accumulators.length;
		
		final Tuple3Accumulator[] tpmAccumulators = new Tuple3Accumulator[n - 1];
		System.arraycopy(accumulators, 0, tpmAccumulators, 0, pos);
	    System.arraycopy(accumulators, pos + 1, tpmAccumulators, pos, n - pos - 1);
	    accumulators = tpmAccumulators;
	    
	    final Tuple3d[] tmpValues = new Tuple3d[n - 1];
		System.arraycopy(values, 0, tmpValues, 0, pos);
	    System.arraycopy(values, pos + 1, tmpValues, pos, n - pos - 1);
	    values = tmpValues;
	    
	    accumulatorIndex.remove(accumulator);
	    morph.removeAccumulator(accumulator);
	}
	
	public void apply(boolean isActive) {
		if (isActive) {
			for (int i = 0; i < accumulators.length; i++) {
				accumulators[i].accumulateActive(values[i]);
			}
		} else {
			for (int i = 0; i < accumulators.length; i++) {
				accumulators[i].accumulatePassive(values[i]);
			}
		}
	}
	
	Tuple3d[] getValues() {
		return values;
	}
	
//	@Override
//	public String toString() {
//		return "MorphTarget@" + System.identityHashCode(this) + "(" + nameAttr.getValue() + ") " + Arrays.toString(targets);
//	}
}
