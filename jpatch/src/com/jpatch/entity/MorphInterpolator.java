package com.jpatch.entity;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.ui.*;
import com.jpatch.afw.vecmath.*;
import java.util.*;
import javax.vecmath.*;


public class MorphInterpolator extends Morph<MorphTarget> {
	private static final String[] DIMENSION_NAMES = new String[] { "x", "y", "z", "w", "u", "v" };
	
	/** degrees of freedom (center dimensions)*/
	private final int degreesOfFreedom;
	
	private final double[] position;
	
	private final GenericAttr<String> nameAttr;
	private final DoubleArrayAttr positionAttr;
	private final DoubleArrayAttr minimumsAttr;
	private final DoubleArrayAttr maximumsAttr;
	private final GenericArrayAttr<String> dofNamesAttr;
	private final IntAttr kAttr = AttributeManager.getInstance().createBoundedIntAttr(1, 1, 3);
	/** locations of the centers */
	private double[][] centers = new double[0][];
	/** values at centers */
	private double[][] values = new double[0][];
	
	private final Map<Tuple3Accumulator, Integer> index = new HashMap<Tuple3Accumulator, Integer>();
	private Tuple3Accumulator[] accumulators = new Tuple3Accumulator[0];
	private int[] references = new int[0];
	private Tuple3d[] accumulatorValues = new Tuple3d[0];
	
	private double[] results = new double[0];
	
	/** weights */
	private double[] weights;
	
	private final Map<Tuple3d, Integer> valueIndex = new IdentityHashMap<Tuple3d, Integer>();
	
	private final Map<MorphTarget, Integer> morphTargetIndex = new HashMap<MorphTarget, Integer>();
	
	/** value dimensions */
	private int dimensions;
	
	/** flag for lazy evaluation */
	private boolean weightsValid = false;
	
	private boolean valuesValid = false;
	
	public MorphInterpolator(int degreesOfFreedom, MorphController morphController, String name) {
		super(MorphTarget.class, morphController);
		this.degreesOfFreedom = degreesOfFreedom;
		position = new double[degreesOfFreedom];
		nameAttr = new GenericAttr<String>(name);
		positionAttr = new DoubleArrayAttr(degreesOfFreedom);
		positionAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				positionAttr.getDoubles(position);
				valuesValid = false;
				System.out.println(this + " position=" + Arrays.toString(position));
			}
		});
		minimumsAttr = new DoubleArrayAttr(degreesOfFreedom);
		maximumsAttr = new DoubleArrayAttr(degreesOfFreedom);
		dofNamesAttr = new GenericArrayAttr<String>(degreesOfFreedom);
		for (int i = 0; i < degreesOfFreedom; i++) {
			minimumsAttr.setDouble(i, 0.0);
			maximumsAttr.setDouble(i, 1.0);
			AttributeManager.getInstance().setLowerLimit(positionAttr.getAttr(i), new DoubleMinimum(minimumsAttr.getAttr(i)));
			AttributeManager.getInstance().setUpperLimit(positionAttr.getAttr(i), new DoubleMaximum(maximumsAttr.getAttr(i)));
			dofNamesAttr.setValue(i, DIMENSION_NAMES[i]);
		}
		kAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				weightsValid = false;
				valuesValid = false;
			}
		});
//		MorphTarget target = createMorphTarget();
//		target.getNameAttribute().setValue("idle target");
		
		for (int i = 0; i <= degreesOfFreedom; i++) {
			MorphTarget target = createMorphTarget();
			if (i == 0) {
				target.getNameAttribute().setValue("idle target");
			} else {
				target.getNameAttribute().setValue(DIMENSION_NAMES[i - 1] + " = 1");
				centers[i][i - 1] = 1;
			}
		}
	}
	
	public GenericAttr<String> getNameAttribute() {
		return nameAttr;
	}
	
	public GenericArrayAttr<String> getDofNamesAttribute() {
		return dofNamesAttr;
	}
	
	public DoubleArrayAttr getLowerLimitsAttribute() {
		return minimumsAttr;
	}
	
	public DoubleArrayAttr getPositionAttribute() {
		return positionAttr;
	}
	
	public DoubleArrayAttr getUpperLimitsAttribute() {
		return maximumsAttr;
	}
	
	public IntAttr getKAttribute() {
		return kAttr;
	}
	
	public int getDegreesOfFreedom() {
		return degreesOfFreedom;
	}
	
	@Override
	public MorphTarget createMorphTarget() {
		final MorphTarget morphTarget = super.createMorphTarget();
		final int index = addCenter(new double[degreesOfFreedom]);
		morphTargetIndex.put(morphTarget, index);
		return morphTarget;
	}




	@Override
	public void removeMorphTarget(MorphTarget morphTarget) {
		super.removeMorphTarget(morphTarget);
		removeCenter(morphTargetIndex.get(morphTarget));
		morphTargetIndex.remove(morphTarget);
	}




	@Override
	public void addAccumulator(Tuple3Accumulator accumulator, Object object, Tuple3d value) {
		super.addAccumulator(accumulator, object, value);
		
		final Integer position = index.get(accumulator);
		if (position == null) {
			final int n = accumulators.length;
			
			final Tuple3Accumulator[] tmpAccumulators = new Tuple3Accumulator[n + 1];
			System.arraycopy(accumulators, 0, tmpAccumulators, 0, n);
			tmpAccumulators[n] = accumulator;
			accumulators = tmpAccumulators;
			
			final Tuple3d[] tmpAccumulatorValues = new Tuple3d[n + 1];
			System.arraycopy(accumulatorValues, 0, tmpAccumulatorValues, 0, n);
			Tuple3d newValue = new Point3d();
			tmpAccumulatorValues[n] = newValue;
			accumulatorValues = tmpAccumulatorValues;
			
//			if (accumulator instanceof Tuple3Accumulator) {
				valueIndex.put(value, dimensions);
				addDimensions(3);
				System.out.println("*** adding valueIndex for value " + System.identityHashCode(value));
//			} else {
//				throw new IllegalArgumentException();
//			}
		} else {
			references[position]++;
		}
	}

	@Override
	public void removeAccumulator(Tuple3Accumulator accumulator) {
		super.removeAccumulator(accumulator);
		
		final int pos = index.get(accumulator);
		references[pos]--;
		if (references[pos] == 0) {
			final int n = accumulators.length;
			
			final Tuple3Accumulator[] tmpAccumulators = new Tuple3Accumulator[n - 1];
			System.arraycopy(accumulators, 0, tmpAccumulators, 0, pos);
		    System.arraycopy(accumulators, pos + 1, tmpAccumulators, pos, n - pos - 1);
		    accumulators = tmpAccumulators;
		    
		    final Tuple3d[] tmpAccumulatorValues = new Tuple3d[n - 1];
			System.arraycopy(accumulatorValues, 0, tmpAccumulatorValues, 0, pos);
		    System.arraycopy(accumulatorValues, pos + 1, tmpAccumulatorValues, pos, n - pos - 1);
		    accumulatorValues = tmpAccumulatorValues;
		    
			if (accumulator instanceof Tuple3Accumulator) {
				removeDimension(valueIndex.get(accumulator), 3);
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	private int addCenter(double[] coordinates) {
		assert coordinates.length == degreesOfFreedom : "wrong dimension";
		final int n = centers.length;
		
		final double[][] tmpC = new double[n + 1][];
		System.arraycopy(centers, 0, tmpC, 0, n);
		tmpC[n] = coordinates.clone();
		centers = tmpC;
		
		final double[][] tmpV = new double[n + 1][];
		System.arraycopy(values, 0, tmpV, 0, n);
		tmpV[n] = new double[dimensions];
		values = tmpV;
		
		weightsValid = false;
		
		return n;
	}
	
	private void removeCenter(int index) {
		final int n = centers.length;
		
		final double[][] tmpC = new double[n - 1][];
		System.arraycopy(centers, 0, tmpC, 0, index);
	    System.arraycopy(centers, index + 1, tmpC, index, n - index - 1);
	    centers = tmpC;
	    
	    final double[][] tmpY = new double[n - 1][];
		System.arraycopy(values, 0, tmpY, 0, index);
	    System.arraycopy(values, index + 1, tmpY, index, n - index - 1);
	    values = tmpY;
	    
	    weightsValid = false;
	}
	
	private int addDimensions(int count) {
		final int n = dimensions;
		dimensions += count;
		for (int i = 0; i < values.length; i++) {
			final double[] tmp = new double[dimensions];
			System.arraycopy(values[i], 0, tmp, 0, n);
			values[i] = tmp;
		}
		results = new double[dimensions];
		
		weightsValid = false;
		
		return n;
	}
	
	private void removeDimension(int index, int count) {
		final int n = dimensions;
		dimensions -= count;
		for (int i = 0; i < values.length; i++) {
			final double[] tmp = new double[dimensions];
			System.arraycopy(values[i], 0, tmp, 0, index);
		    System.arraycopy(values[i], index + count, tmp, index, n - index - count);
		    values[i] = tmp;
		}
		results = new double[dimensions];
		
		weightsValid = false;
	}
	
	private void check() {
		System.out.println("check");
		for (int centerIndex = 0; centerIndex < centers.length; centerIndex++) {
			MorphTarget morphTarget = morphTargets.get(centerIndex);
			final double[] values = this.values[centerIndex];
			
			System.out.println("morphTarget:" + morphTarget);
			/* check if accumulator values have changed and, if yes, change the value and clear the weightsValid flag */
			for (Tuple3d value : morphTarget.getValues()) {
//				if (accumulator instanceof Tuple3Accumulator) {
//					System.out.println(accumulator);
					System.out.println("*** getting valueIndex for " + System.identityHashCode(value));
					int index = valueIndex.get(value);
//					Tuple3d tuple = ((Tuple3Accumulator) accumulator).asTuple();
					weightsValid &= checkValue(values, index, value.x);
					weightsValid &= checkValue(values, index + 1, value.y);
					weightsValid &= checkValue(values, index + 2, value.z);
//				} else {
//					throw new RuntimeException();
//				}
			}
			System.out.println("values=" + Arrays.toString(values));
		}
	}
	
	private boolean checkValue(double[] values, int index, double value) {
		if (values[index] != value) {
			values[index] = value;
			return false;
		} else {
			return true;
		}
	}
	
//	public DoubleArrayAttr createCenterPositionAttribute(MorphTarget target) {
//		final int centerIndex = morphTargetIndex.get(target);
//		DoubleArrayAttr arrayAttr = new DoubleArrayAttr(degreesOfFreedom);
//		for (int i = 0; i < degreesOfFreedom; i++) {
//			final int dofIndex = i;
//			DoubleAttr doubleAttr = arrayAttr.getAttr(dofIndex);
//			doubleAttr.setDouble(centers[centerIndex][dofIndex]);
//			doubleAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
//				public void attributeHasChanged(Attribute source) {
//					centers[centerIndex][dofIndex] = ((DoubleAttr) source).getDouble();
//					weightsValid = false;
//				}		
//			});
//		}
//		System.out.println("created new ArrayAttr for index " + centerIndex + ": " + arrayAttr);
//		return arrayAttr;
//	}
	
	public double getCenterPosition(int centerIndex, int dimIndex) {
		return centers[centerIndex][dimIndex];
	}
	
	public void setCenterPosition(int centerIndex, int dimIndex, double value) {
		centers[centerIndex][dimIndex] = value;
		weightsValid = false;
	}
	
	private void computeWeights() {
		System.out.println("*compute weights*");
		if (dimensions == 0) {
			weightsValid = true;
			return;
		}
		
		int dim = centers.length + 1 + degreesOfFreedom;
		
		double[] y = new double[values.length * dimensions];
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < dimensions; j++) {
				y[i * dimensions + j] = values[i][j];
			}
		}
		
		System.out.println("values:" + Arrays.deepToString(values));
		System.out.println("y:" + Arrays.toString(y));
		System.out.println("centers:" + Arrays.deepToString(centers));
		
		/* fill matrix */
		double[] matrix = new double[dim * dim];
		for (int i = 0; i < centers.length; i++) {		// row
			for (int j = 0; j < centers.length; j++) {	// column
				matrix[i * dim + j] = phi(centers[i], centers[j]);
			}
			matrix[centers.length + i * dim] = 1;
			matrix[centers.length * dim + i] = 1;
			for (int j = 0; j < centers[0].length; j++) {
				matrix[i * dim + centers.length + 1 + j] = centers[i][j];
				matrix[(centers.length + 1 + j) * dim + i] = centers[i][j];
			}
		}
		System.out.println("matrix:");
		Utils3d.printMatrix(matrix);
		
		double[] b = new double[dim * dimensions];
		weights = new double[dim * dimensions];
		
		System.out.println("y.length=" + y.length);
		System.out.println("b.length=" + b.length);
		System.out.println("values.length=" + values.length);
		
		System.arraycopy(y, 0, b, 0, y.length);
		
		System.out.println("b:" + Arrays.toString(b));
		
		/* solve the system */
		MatrixUtils.solve(matrix, b, weights);
		
		weightsValid = true;
		
		System.out.println("weights:" + Arrays.toString(weights));
	}
	
	private void evaluate() {
//		if (true) return; // TODO
		System.out.println("evaluate");
		check();
		if (!weightsValid) {
			computeWeights();
		}
		for (int i = 0; i < results.length; i++) {
			results[i] = weights[centers.length * dimensions + i];
		}
		for (int i = 0; i < centers.length; i++) {
			double phi = phi(position, centers[i]);
			int row = i * dimensions;
			for (int j = 0; j < results.length; j++) {
				results[j] += weights[row + j] * phi;
			}
			
		}
		for (int i = 0; i < centers[0].length; i++) {
			int row = (centers.length + 1 + i) * dimensions;
			for (int j = 0; j < results.length; j++) {
				results[j] += weights[row + j] * position[i];
			}
		}
		
		/* copy results to accumulatorValues */
		int index = 0;
		for (Tuple3d value : accumulatorValues) {
//			if (accumulator instanceof Tuple3Accumulator) {
//				Tuple3d tuple = ((Tuple3Accumulator) accumulator).asTuple();
			value.x = results[index++];
			value.y = results[index++];
			value.z = results[index++];
//			} else {
//				throw new RuntimeException();
//			}
//			System.out.println(accumulator);
		}
		
		valuesValid = true;
	}
	
	@Override
	public void apply(MorphTarget activeMorphTarget) {
		System.out.println("apply");
		if (morphTargetIndex.containsKey(activeMorphTarget)) {
			activeMorphTarget.apply(true);
			return;
		}
		if (!valuesValid || !weightsValid) {
			evaluate();
		}
		
		/* apply targets */
		for (int i = 0; i < accumulators.length; i++) {
			accumulators[i].accumulatePassive(accumulatorValues[i]);
		}
	}
	
	/**
	 * The radial basis function of the spline.
	 * phi(r) = r^k for k = 1,3,5,...
	 * phi(r) = r^k*ln(r) with k = 2,4,6...
	 * where r = ||a - b||
	 * 
	 * @param a point a (usually a point to evaluate)
	 * @param b point b (usually a center of the spline)
	 * @return r^k for k = 1,3,5,..., r^k*ln(r) with k = 2,4,6... where r = ||a - b||
	 */
	private double phi(double[] a, double[] b) {
		double r = distance(a, b);
		final int k = kAttr.getInt();
		switch (k) {
		case 1:
			return r;
		case 2:
			if (r < 1) {
				return r * Math.log(Math.pow(r, r));	// since ln(0) = -infinity
			} else {
				return r * r * Math.log(r);
			}
		case 3:
			return r * r * r;
		default:
			if (k % 2 == 0) {
				if (r < 1) {
					return Math.pow(r, k - 1) * Math.log(Math.pow(r, r));	// since ln(0) = -infinity
				} else {
					return Math.pow(r, k) * Math.log(r);
				}
			} else {
				return Math.pow(r, k);
			}
		}
	}
	
	/**
	 * Compute the distance between points a and b
	 * @param a array representing point a
	 * @param b array representing point b
	 * @return the distance between a and b
	 * @throws IllegalArgumentException if a.length ≠ b.length
	 * @throws NullPointerException if one of the parameters is null
	 */
	private double distance(double[] a, double[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("a and b must have same length (" + a.length + " ≠ " + b.length + ")");
		}
		double dsq = 0;
		for (int i = 0; i < a.length; i++) {
			double d = a[i] - b[i];
			dsq += d * d;
		}
		return Math.sqrt(dsq);
	}

}
