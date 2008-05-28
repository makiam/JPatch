package simplex;

import javax.vecmath.*;

public class Matrix {
	private final double[][] elements;
	private final int rows;
	private final int columns;
	
	public Matrix(final int rows, final int columns) {
		this.rows = rows;
		this.columns = columns;
		elements = new double[rows][columns];
	}
	
	public Matrix(final double[][] elements) {
		rows = elements.length;
		columns = elements[0].length;
		this.elements = new double[rows][columns];
		set(elements);
	}
	
	public void set(final double[][] elements) {
		if (elements.length != rows) {
			throw new IllegalArgumentException("rows must be " + rows + ", but were " + elements.length);
		}
		for (int row = 0; row < rows; row++) {
			setRow(row, elements[row]);
		}
	}
	
	public void setRow(final int row, final double ... elements) {
		if (elements.length != columns) {
			throw new IllegalArgumentException("columns must be " + columns + ", but were " + elements.length);
		}
		System.arraycopy(elements, 0, this.elements[row], 0, columns);
	}
	
	public void setElement(final int row, final int column, final double value) {
		elements[row][column] = value;
	}
	
	public int getRowCount() {
		return rows;
	}
	
	public int getColumnCount() {
		return columns;
	}
	
	public double determinant() {
		if (rows != columns) {
			throw new IllegalStateException("Matrix is not square");
		}
		boolean[] activeColumns = new boolean[columns];
		for (int i = 0; i < columns; i++) {
			activeColumns[i] = true;
		}
		return determinant(0, activeColumns);
	}
	
	public double determinant(int row, boolean[] activeColumns) {
        if (row == rows) {
        	return 1;
        }
        double sum = 0;
        int sign = 1;
        for (int col = 0; col < activeColumns.length; col++) {
            if (activeColumns[col]) {
            	activeColumns[col] = false;
            	sum += sign * elements[row][col] * determinant(row + 1, activeColumns);
            	activeColumns[col] = true;
            	sign = -sign;
            }
        }
        return sum;
    }
	
	@Override
	public String toString() {
		String[][] s = new String[rows][columns];
		int[] maxLength = new int[columns];
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				s[row][column] = Float.toString((float) elements[row][column]);
				int l = s[row][column].length();
				if (elements[row][column] >= 0.0) {
					l += 1;
				}
				if (l > maxLength[column]) {
					maxLength[column] = l;
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				for (int i = 0; i < maxLength[column] - s[row][column].length(); i++) {
					sb.append(' ');
				}
				sb.append(s[row][column]).append(' ');
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		Matrix matrix1 = new Matrix(new double[][] {{1,2}, {3,4}});
        Matrix matrix2 = new Matrix(new double[][] {{7,0,5}, {2,4,6}, {3,8,1}});
        System.out.print("Results should be -2 and -288: ");
        System.out.println(matrix1.determinant() + " " + matrix2.determinant());
	}
}
