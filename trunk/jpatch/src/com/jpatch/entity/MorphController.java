package com.jpatch.entity;

import com.jpatch.afw.attributes.*;
import com.jpatch.boundary.*;
import com.jpatch.entity.sds2.*;

import java.util.*;

public class MorphController {
	protected final Sds sds;
	private final Map<Accumulator, Integer> index = new HashMap<Accumulator, Integer>();
	protected Accumulator[] accumulators = new Accumulator[0];
	protected Object[] objects = new Object[0];
	private int[] references = new int[0];
	private NdeLayerManager ndeLayerManager;
	private List<MorphInterpolator> morphs = new ArrayList<MorphInterpolator>();
	private GenericAttr<MorphTarget> activeMorphTargetAttr = new GenericAttr<MorphTarget>();
	
	public MorphController(Sds sds) {
		this.sds = sds;
	}
	
	public int getNumberOfMorphs() {
		return morphs.size();
	}
	
	public final void addAccumulator(Accumulator accumulator, Object object) {
		System.out.println(this + " addAccumulator(" + accumulator + ", " + object);
		final Integer position = index.get(accumulator);
		if (position == null) {
			addNewAccumulator(accumulator, object);
		} else {
			references[position]++;
		}
	}
	
	protected void addNewAccumulator(Accumulator accumulator, Object object) {
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
	}
	
	public final void removeAccumulator(Accumulator accumulator) {
		final int pos = index.get(accumulator);
		references[pos]--;
		if (references[pos] == 0) {
			removeAccumulator(accumulator, pos);
		}
	}
	
	protected void removeAccumulator(Accumulator accumulator, final int pos) {
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
	
	public void addMorph(MorphInterpolator morph) {
		assert !morphs.contains(morph) : morph + " has already been added";
		morphs.add(morph);
	}
	
	public void removeMorph(Morph<?extends MorphTarget> morph) {
		assert morphs.contains(morph) : morph + " has not been been added";
		morphs.remove(morph);
	}
	
	public MorphInterpolator getMorph(int index) {
		return morphs.get(index);
	}
	
	public void apply() {
		/* reset all accumulators */
		for (Accumulator accumulator : accumulators) {
			accumulator.reset();
		}
		
		final MorphTarget activeMorphTarget = activeMorphTargetAttr.getValue();
		
		ndeLayerManager.apply(activeMorphTarget);
		for (Morph<? extends MorphTarget> morph : morphs) {
			morph.apply(activeMorphTargetAttr.getValue());
		}
		
		activeMorphTarget.set();
		
		/* validate objects */
		for (Object object : objects) {
			if (object instanceof AbstractVertex) {
				((AbstractVertex) object).invalidateAll();
			}
		}
	}
	
	public void setNdeLayerManager(NdeLayerManager ndeLayerManager) {
		this.ndeLayerManager = ndeLayerManager;
	}
	
	public MorphTarget getActiveMorphTarget() {
		return activeMorphTargetAttr.getValue();
	}
	
	public void setActiveMorphTarget(MorphTarget morphTarget) {
		this.activeMorphTargetAttr.setValue(morphTarget);
	}
	
	public GenericAttr<MorphTarget> getActiveMorphTargetAttribute() {
		return activeMorphTargetAttr;
	}
}
