package com.jpatch.entity;

import com.jpatch.entity.sds2.*;

import java.util.*;

public abstract class Morph<T extends MorphTarget> {
//	protected final Sds sds;
	protected final MorphController morphController;
//	private final Map<Accumulator, Integer> index = new HashMap<Accumulator, Integer>();
//	protected Accumulator[] accumulators = new Accumulator[0];
//	private int[] references = new int[0];
	
	protected final List<T> morphTargets = new ArrayList<T>();
	private final List<T> morphTargetsView = Collections.unmodifiableList(morphTargets);
	private final Class<? extends T> morphTargetClass;
	
	protected Morph(Class<T> morphTargetClass, MorphController morphController) {
		this.morphController = morphController;
		this.morphTargetClass = morphTargetClass;
	}
	
	public void addAccumulator(Accumulator accumulator, Object object, Accumulator value) {
//		System.out.println(this + " addAccumulator(" + accumulator + ", " + object);
//		final Integer position = index.get(accumulator);
//		if (position == null) {
//			addNewAccumulator(accumulator, object);
//		} else {
//			references[position]++;
//		}
		morphController.addAccumulator(accumulator, object);
	}
	
//	protected void addNewAccumulator(Accumulator accumulator, Object object) {
//		final int n = accumulators.length;
//		
//		final Accumulator[] tmpAccumulators = new Accumulator[n + 1];
//		System.arraycopy(accumulators, 0, tmpAccumulators, 0, n);
//		tmpAccumulators[n] = accumulator;
//		accumulators = tmpAccumulators;
//		
//		final int[] tmpReferences = new int[n + 1];
//		System.arraycopy(references, 0, tmpReferences, 0, n);
//		tmpReferences[n] = 1;
//		references = tmpReferences;
//		
//		morphController.addAccumulator(accumulator, object);
//		
//		index.put(accumulator, n);
//	}
	
	public void removeAccumulator(Accumulator accumulator) {
//		final int pos = index.get(accumulator);
//		references[pos]--;
//		if (references[pos] == 0) {
//			removeAccumulator(accumulator, pos);
//		}
		morphController.removeAccumulator(accumulator);
	}
	
//	protected void removeAccumulator(Accumulator accumulator, final int pos) {
//		final int n = accumulators.length;
//		
//		final Accumulator[] tmpAccumulators = new Accumulator[n - 1];
//		System.arraycopy(accumulators, 0, tmpAccumulators, 0, pos);
//	    System.arraycopy(accumulators, pos + 1, tmpAccumulators, pos, n - pos - 1);
//	    accumulators = tmpAccumulators;
//	    
//	    final int[] tmpReferences = new int[n - 1];
//		System.arraycopy(references, 0, tmpReferences, 0, pos);
//	    System.arraycopy(references, pos + 1, tmpReferences, pos, n - pos - 1);
//	    references = tmpReferences;
//	    
//	    morphController.removeAccumulator(accumulator);
//	    
//	    index.remove(accumulator);
//	}
	
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
	
	public List<T> getMorphTargets() {
		return morphTargetsView;
	}
	
	public void removeMorphTarget(MorphTarget morphTarget) {
		morphTargets.remove(morphTarget);
	}
	
	public void apply(MorphTarget activeMorptarget) {
		/* apply targets */
		for (T morphTarget : morphTargets) {
			if (morphTarget instanceof NdeLayer) {
				if (morphTarget == activeMorptarget) {
					continue;
				} else if (((NdeLayer) morphTarget).getEnabledAttribute().getBoolean()) {
					morphTarget.apply();
				}
			}
		}
	}
}
