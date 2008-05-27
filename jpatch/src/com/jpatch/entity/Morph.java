package com.jpatch.entity;

public class Morph {
	private double[] weights;
	private MorphTarget[] morphTargets;
	
	public Morph(int dimensions, double[] value) {
		morphTargets = new MorphTarget[] { new MorphTarget(new double[dimensions], value) };
	}
	
	public MorphTarget[] getMorphTargets() {
		return morphTargets.clone();
	}
	
	public void addMorphTarget(MorphTarget morphTarget) {
		if (morphTarget == null) {
    		throw new NullPointerException();
    	}
    	for (MorphTarget target : morphTargets) {
    		if (target == morphTarget) {
    			throw new IllegalArgumentException(morphTarget + " has already been added to " + this);
    		}
		}
    	int i = morphTargets.length;
    	MorphTarget[] tmp = new MorphTarget[i + 1];
    	System.arraycopy(morphTargets, 0, tmp, 0, i);
 	    tmp[i] = morphTarget;
 	    morphTargets = tmp;
 	    weights = new double[morphTargets.length];
	}
	
	public void removeMorphTarget(MorphTarget morphTarget) {
    	int i = 0;
    	while (i < morphTargets.length && morphTargets[i] != morphTarget) {
    		i++;
    	}
    	if (i < morphTargets.length) {
    		MorphTarget[] tmp = new MorphTarget[morphTargets.length - 1];
    	    // Copy the list up to i
    	    System.arraycopy(morphTargets, 0, tmp, 0, i);
    	    // Copy from one past the index, up to
    	    // the end of tmp (which is one element
    	    // shorter than the old list)
    	    if (i < tmp.length)
    	    	System.arraycopy(morphTargets, i + 1, tmp, i, tmp.length - i);
    	    // set the listener array to the new array
    	    morphTargets = tmp;
    	    weights = new double[morphTargets.length];
    	} else {
    		throw new IllegalArgumentException(morphTarget + " is not part of " + this);
    	}
    }
	
	/*
	 * Performs inverse distance interpolation. Each weight is proportional to the inverse cube of the distance.
	 */
	public void interpolate(double[] position, double[] result) {
		double weightSum = 0;
		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
		}
		for (int i = 0; i < morphTargets.length; i++) {
			double distanceSq = 0;
			for (int j = 0; j < position.length; j++) {
				double d = position[j] - morphTargets[i].position[j];
				distanceSq += d * d;
			}
			/* if distance = 0, return target value */
			if (distanceSq == 0) {
				for (int j = 0; j < result.length; j++) {
					result[j] = morphTargets[i].value[j];
				}
				return;
			}
			weights[i] = 1.0 / distanceSq;
			weightSum += weights[i];
		}
		final double invWeightSum = 1.0 / weightSum;
		for (int i = 0; i < morphTargets.length; i++) {
			for (int j = 0; j < result.length; j++) {
				result[j] += morphTargets[i].value[j] * weights[i] * invWeightSum;
			}
		}
	}
}
