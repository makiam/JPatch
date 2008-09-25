package com.jpatch.entity;

import javax.vecmath.*;

public class Tuple3Accumulator {
	/** accumulates only passive targets */
	private final Tuple3d delta = new Point3d();
	
	/** a pointer to the target tuple */
	private final Tuple3d targetTuple;
	
	public Tuple3Accumulator(Tuple3d targetTuple) {
		this.targetTuple = targetTuple;
	}
	
	/**
	 * musts be called from all inactive morph-targets
	 * @param tuple
	 */
	public void accumulatePassive(Tuple3d tuple) {
		targetTuple.add(tuple);
		delta.add(tuple);
	}
	
	/**
	 * must be called from the active morph-target after all
	 * inactive morph-targets have been accumulated
	 * @param tuple
	 */
	public void accumulateActive(Tuple3d tuple) {
		targetTuple.add(tuple);
	}
	
	/**
	 * call to set the value of the active morph-target
	 * @param value
	 */
	public void applyTo(Tuple3d value) {
		value.sub(targetTuple, delta);
	}
	
	/**
	 * resets this accumulator
	 * must be called before accumulation starts
	 */
	public void reset() {
		targetTuple.set(0, 0, 0);
		delta.set(0, 0, 0);
	}
}
