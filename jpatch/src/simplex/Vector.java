package simplex;

import java.util.*;

public class Vector {
	private double[] elements;
	
	public Vector(int dimensions) {
		elements = new double[dimensions];
	}
	
	public Vector(double ... coordinates) {
		set(coordinates);
	}
	
	public Vector(Vector vector) {
		set(vector);
	}
	
	public int getDimensions() {
		return elements.length;
	}
	
	public double[] getElements() {
		return elements.clone();
	}
	
	public double getElement(int index) {
		return elements[index];
	}
	
	public void set(double ... coordinates) {
		elements = coordinates.clone();
	}
	
	public void set(Vector vector) {
		set(vector.elements);
	}
	
	public void setElement(int index, double value) {
		elements[index] = value;
	}
	
	public void add(Vector a, Vector b) {
		int dim = checkDimensions(a, b);
		for (int i = 0; i < dim; i++) {
			elements[i] = a.elements[i] + b.elements[i];
		}
	}
	
	public void add(Vector vector) {
		add(this, vector);
	}
	
	public void sub(Vector a, Vector b) {
		int dim = checkDimensions(a, b);
		for (int i = 0; i < dim; i++) {
			elements[i] = a.elements[i] - b.elements[i];
		}
	}
	
	public void sub(Vector vector) {
		sub(this, vector);
	}
	
	public double dot(Vector vector) {
		return dot(this, vector);
	}
	
	public static double dot(Vector a, Vector b) {
		int dim = checkDimensions(a, b);
		double sum = 0;
		for (int i = 0; i < dim; i++) {
			sum += a.elements[i] * b.elements[i];
		}
		return sum;
	}
	
	public void extend(Vector a, Vector b) {
		set(extend(a.elements, b.elements));
	}
	
	public void extend(Vector a, double ... elements) {
		set(extend(a.elements, elements));
	}
	
	public void extend(double ... elements) {
		extend(this, elements);
	}
	
	public double getNormSq() {
		double sum = 0;
		for (double element : elements) {
			sum += element * element;
		}
		return sum;
	}
	
	public double getNorm() {
		return Math.sqrt(getNormSq());
	}
	
	public void mul(Vector vector, double factor) {
		set(vector);
		mul(factor);
	}
	
	public void mul(double factor) {
		for (int i = 0; i < elements.length; i++) {
			elements[i] *= factor;
		}
	}
	
	public void normalize(Vector vector) {
		set(vector);
		normalize();
	}
	
	public void normalize() {
		mul(1.0 / getNorm());
	}
	
	public void bisector(Vector a, Vector b) {
		checkDimensions(a, b);
		Vector diff = new Vector();
		diff.sub(a, b);
		Vector sum = new Vector();
		sum.add(a, b);
		double dot = diff.dot(sum);
		set(extend(diff.elements, -dot / 2));
	}
	
	private static double[] extend(double[] a, double ... b) {
		double[] result = new double[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			Vector v = (Vector) o;
			int dim = checkDimensions(this, v);
			for (int i = 0; i < dim; i++) {
				if (elements[i] != v.elements[i]) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public void cross(Vector v, Matrix m) {
		int dim = m.getRowCount() + 1;
		if (dim != m.getColumnCount()) {
			throw new IllegalArgumentException("Dimension mismatch");
		}
		this.elements = new double[dim];
		boolean[] activeColumns = new boolean[dim];
		for (int column = 0; column < dim; column++) {
			activeColumns[column] = true;
		}
		int sign = 1;
		for (int column = 0; column < dim; column++) {
			activeColumns[column] = false;
			this.elements[column] = sign * m.determinant(0, activeColumns);
			activeColumns[column] = true;
			sign = -sign;
		}
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(elements);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("(");
		for (int i = 0; i < elements.length; i++) {
			sb.append(Double.toString(elements[i]));
			if (i < elements.length - 1) {
				sb.append(',').append(' ');
			}
		}
		sb.append(')');
		return sb.toString();
	}
	
	private static int checkDimensions(Vector a, Vector b) {
		int dim = a.elements.length;
		if (dim != b.elements.length) {
			throw new IllegalArgumentException("Dimension mismatch: " + dim + " != " + b.elements.length);
		}
		return dim;
	}
	
	public static void main(String[] args) {
		Vector p = new Vector(1, 2, 3);
        System.out.println("Pnt created: " + p);
        
	}
}
