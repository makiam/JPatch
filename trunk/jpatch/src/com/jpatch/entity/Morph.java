package com.jpatch.entity;

import java.util.*;

public class Morph {
	private double[] weights;
	private MorphTarget[] morphTargets = new MorphTarget[0];
	private double[][] preWeights;
	private double[] fairWeights;
	private int valueSize;
	
	public Morph(int dimensions, int valueSize) {
//		morphTargets = new MorphTarget[] { new MorphTarget(new double[dimensions], value) };
		this.valueSize = valueSize;
	}
	
	public MorphTarget[] getMorphTargets() {
		return morphTargets.clone();
	}
	
	public void computePreWeights() {
		if (morphTargets.length == 0) {
			return;
		}
		preWeights = new double[morphTargets.length][morphTargets[0].value.length];
		for (int i = 0; i < morphTargets.length; i++) {
			interpolate(i, preWeights[i]);
		}
	}
	
	public MorphTarget addMorphTarget(double[] position) {
//		if (morphTarget == null) {
//    		throw new NullPointerException();
//    	}
//    	for (MorphTarget target : morphTargets) {
//    		if (target == morphTarget) {
//    			throw new IllegalArgumentException(morphTarget + " has already been added to " + this);
//    		}
//		}
		computePreWeights();
		MorphTarget morphTarget = new MorphTarget(position, new double[valueSize]);
    	interpolate(position, true, morphTarget.value);
    	System.out.println(Arrays.toString(morphTarget.value));
    	int i = morphTargets.length;
    	MorphTarget[] tmp = new MorphTarget[i + 1];
    	System.arraycopy(morphTargets, 0, tmp, 0, i);
 	    tmp[i] = morphTarget;
 	    morphTargets = tmp;
 	    weights = new double[morphTargets.length];
 	    fairWeights = new double[morphTargets.length];
 	    return morphTarget;
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
	public void interpolate(int targetIndex, double[] result) {
		double weightSum = 0;
		double[] position = morphTargets[targetIndex].position;
		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
		}
		for (int i = 0; i < morphTargets.length; i++) {
			if (i == targetIndex) {
				weights[i] = 0;
				continue;
			}
			double distanceSq = 0;
			for (int j = 0; j < position.length; j++) {
				double d = position[j] - morphTargets[i].position[j];
				distanceSq += d * d;
			}
			double d = Math.sqrt(distanceSq);
			weights[i] = Math.pow(d, -3);
			weightSum += weights[i];
		}
		final double invWeightSum = 1.0 / weightSum;
		for (int i = 0; i < morphTargets.length; i++) {
			for (int j = 0; j < result.length; j++) {
				result[j] += morphTargets[i].value[j] * weights[i] * invWeightSum;
			}
		}
		for (int i = 0; i < result.length; i++) {
			double d = result[i] - morphTargets[targetIndex].value[i];
			result[i] = Math.abs(d);
		}
	}
	
	/*
	 * Performs inverse distance interpolation. Each weight is proportional to the inverse cube of the distance.
	 */
	public void interpolate(double[] position, boolean preWeight, double[] result) {
		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
		}
		double totalWeight = 0;
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
			double d = Math.sqrt(distanceSq);
			weights[i] = Math.pow(d, -3);
			totalWeight += weights[i];
		}
		if (!preWeight) {
			double factor = 1.0 / totalWeight;
			for (int i = 0; i < morphTargets.length; i++) {
				for (int j = 0; j < result.length; j++) {
					result[j] += morphTargets[i].value[j] * weights[i] * factor;
				}
			}
		} else {
			for (int i = 0; i < result.length; i++) {
				totalWeight = 0;
				for (int j = 0; j < morphTargets.length; j++) {
					fairWeights[j] = weights[j] * preWeights[j][i];
					totalWeight += fairWeights[j];
				}
				double factor = 1.0 / totalWeight;
				for (int j = 0; j < morphTargets.length; j++) {
					result[i] += morphTargets[j].value[i] * fairWeights[j] * factor;
				}
			}
		}
		
		
		
//		for (int i = 0; i < morphTargets.length; i++) {
//			double totalWeight = 0;
//			for (int j = 0; j < result.length; j++) {
//				fairWeights[i] = weights[i] * 1;//preWeights[i][j];
//				totalWeight += fairWeights[i];
//			}
//			double factor = 1.0 / totalWeight;
//			for (int j = 0; j < result.length; j++) {
//				result[j] += morphTargets[i].value[j] * fairWeights[i] * factor;
//			}
//		}
	}
}
