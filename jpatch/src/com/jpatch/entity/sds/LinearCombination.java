package com.jpatch.entity.sds;

import java.util.*;

public class LinearCombination<T> {
	private List<T> entities = new ArrayList<T>();
	private double[] weights = new double[0];
	
	public int size() {
		return weights.length;
	}
	
	public double summaryWeight() {
		double sum = 0;
		for (int i = 0; i < weights.length; i++) {
			sum += weights[i];
		}
		return sum;
	}
	
	public List<T> getEntities() {
		return entities;
	}
	
	public double[] getWeights() {
		return weights;
	}
	
	public void add(T entity, double weight) {
		if (weight == 0) {
			return;
		}
		int index = this.entities.indexOf(entity);
		if (index != -1) {
			weights[index] += weight;
			if (weights[index] == 0) {
				int size = weights.length;
				entities.remove(index);
				double[] tmp = new double[size - 1];
				System.arraycopy(weights, 0, tmp, 0, index);
				System.arraycopy(weights, index + 1, tmp, index, size - index - 1);
				weights = tmp;
			}
		} else {
			int size = weights.length;
			entities.add(entity);
			double[] tmp = new double[size + 1];
			System.arraycopy(weights, 0, tmp, 0, size);
			tmp[size] = weight;
			weights = tmp;
		}
	}
	
	public void add(List<T> entities, double[] weights) {
		int i = 0;
		for (T entity : entities) {
			add(entity, weights[i]);
			i++;
		}
	}
	
	public void addScaled(List<T> entities, double[] weights, double factor) {
		double[] newWeights = weights.clone();
		for (int i = 0; i < newWeights.length; i++) {
			newWeights[i] *= factor;
		}
		add(entities, newWeights);
	}
	
	public void add(LinearCombination<T> linearCombination) {
		add(linearCombination.entities, linearCombination.weights);
	}
	
	public void addScaled(LinearCombination<T> linearCombination, double factor) {
		addScaled(linearCombination.entities, linearCombination.weights, factor);
	}
	
	public void scale(double factor) {
		for (int i = 0; i < weights.length; i++) {
			weights[i] *= factor;
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < weights.length; i++) {
			if (i > 0) {
				if (weights[i] > 0) {
					sb.append(" + ");
				} else {
					sb.append(" - ");
				}
				if (Math.abs(weights[i]) != 1) {
					sb.append(Double.toString(Math.abs(weights[i])));
					sb.append(" * ");
				}
			} else {
				if (weights[0] == -1) {
					sb.append("-");
				} else if (weights[0] != 1) {
					sb.append(weights[0]);
					sb.append(" * ");
				}
			}
			sb.append(entities.get(i));
		}
		
		sb.append(" (weight sum = ");
		sb.append(Double.toString(summaryWeight()));
		sb.append(")");
		return sb.toString();
	}
	
	public static void main(String[] args) {
		LinearCombination<String> lc1 = new LinearCombination();
		lc1.add("a", 1);
		lc1.add("b", 2);
		lc1.add("c", 3);
		lc1.add("d", 1);
		System.out.println("lc1 = " + lc1);
		LinearCombination<String> lc2 = new LinearCombination();
		lc2.add("a", 0.5);
		lc2.add("b", 1);
		lc2.add("c", 1.5);
		System.out.println("lc2 = " + lc2);
		lc1.addScaled(lc2, -2);
		System.out.println("lc1 = " + lc1);
	}
}
