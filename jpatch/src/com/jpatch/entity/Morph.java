package com.jpatch.entity;

import com.jpatch.entity.sds2.*;

import java.util.*;

public class Morph<T extends MorphTarget> {
	protected final Sds sds;
	private final Map<Accumulator, Integer> index = new HashMap<Accumulator, Integer>();
	protected Accumulator[] accumulators = new Accumulator[0];
	protected Object[] objects = new Object[0];
	private int[] references = new int[0];
	
	protected final List<T> morphTargets = new ArrayList<T>();
	private final Class<? extends T> morphTargetClass;
	
	public Morph(Class<T> morphTargetClass, Sds sds) {
		this.sds = sds;
		this.morphTargetClass = morphTargetClass;
	}
	
	public final void addAccumulator(Accumulator accumulator, Object object) {
		System.out.println(this + " addAccumulator(" + accumulator + ", " + object);
		final Integer position = index.get(accumulator);
		if (position == null) {
			final int n = accumulators.length;
			
			final Accumulator[] tmpAccumulators = new Accumulator[n + 1];
			System.arraycopy(accumulators, 0, tmpAccumulators, 0, n);
			tmpAccumulators[n] = accumulator;
			accumulators = tmpAccumulators;
			
			final Object[] tmpObjects = new Object[n + 1];
			System.arraycopy(objects, 0, tmpObjects, 0, n);
			tmpObjects[n] = object;
			objects = tmpObjects;
			
			final int[] tmpReferences = new int[n + 1];
			System.arraycopy(references, 0, tmpReferences, 0, n);
			tmpReferences[n] = 1;
			references = tmpReferences;
			
			index.put(accumulator, n);
		} else {
			references[position]++;
		}
	}
	
	public final void removeAccumulator(Accumulator accumulator) {
		final int pos = index.get(accumulator);
		references[pos]--;
		if (references[pos] == 0) {
			final int n = accumulators.length;
			
			final Accumulator[] tmpAccumulators = new Accumulator[n - 1];
			System.arraycopy(accumulators, 0, tmpAccumulators, 0, pos);
		    System.arraycopy(accumulators, pos + 1, tmpAccumulators, pos, n - pos - 1);
		    accumulators = tmpAccumulators;
		    
		    final Object[] tmpObjects = new Object[n - 1];
			System.arraycopy(objects, 0, tmpObjects, 0, pos);
		    System.arraycopy(objects, pos + 1, tmpObjects, pos, n - pos - 1);
		    objects = tmpObjects;
		    
		    final int[] tmpReferences = new int[n - 1];
			System.arraycopy(references, 0, tmpReferences, 0, pos);
		    System.arraycopy(references, pos + 1, tmpReferences, pos, n - pos - 1);
		    references = tmpReferences;
		    
		    index.remove(accumulator);
		}
	}
	
	public T createMorphTarget() {
		T morphTarget;
		try {
			morphTarget = morphTargetClass.getConstructor(Morph.class).newInstance(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		morphTargets.add(morphTarget);
		return morphTarget;
	}
	
	public final void removeMorphTarget(final int index) {
		morphTargets.remove(index);
	}
	
	public void apply() {
		/* reset all accumulators */
		for (Accumulator accumulator : accumulators) {
			accumulator.reset();
		}
		
		/* apply targets */
		MorphTarget activeMorptarget = sds.getActiveMorphTarget();
		for (T morphTarget : morphTargets) {
			if (morphTarget instanceof NdeLayer) {
				if (morphTarget == activeMorptarget) {
					continue;
				} else if (((NdeLayer) morphTarget).getEnabledAttribute().getBoolean()) {
					morphTarget.apply();
				}
			}
		}
		
		activeMorptarget.set();
		
		/* validate objects */
		for (Object object : objects) {
			if (object instanceof AbstractVertex) {
				((AbstractVertex) object).invalidateAll();
			}
		}
	}
}
