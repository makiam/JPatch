package test;

import java.util.*;

import javax.vecmath.*;

public class StackVmTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StackTest stackTest = new ArrayStackTest();	// change this to ObjectStackTest for the other result
		stackTest.push(1, 2, 3);
		stackTest.push(-2, -2, -2);
		for (int run = 0; run < 2; run++) {	// one dry run, the next round counts
			long t = System.currentTimeMillis();
			for (int j = 0; j < 100000; j++) {
				for (int i = 1; i < 100; i++) {
					stackTest.add();
				}
//				stackTest.print();	// check if results are equal
				for (int i = 1; i < 100; i++) {
					stackTest.pop();
				}
//				stackTest.print();  // check if results are equal
			}
			
			System.out.println(System.currentTimeMillis() - t);
		}
	}

	interface StackTest {
		void push(double x, double y, double z);	// push tuple x,y,z onto the stack
		void add();									// add the last two tuples on the stack and push the result on top of the stack
		void print();								// print the tuple on top of the stack;
		void pop();									// pop last element
	}
	
	static class ObjectStackTest implements StackTest {
		List<Object> stack = new ArrayList<Object>();
		
		public void add() {
			Point3d sum = new Point3d();
			int index = stack.size();
			sum.add((Point3d) stack.get(index - 1), (Point3d) stack.get(index - 2));
			stack.add(sum);
		}

		public void print() {
			System.out.println(stack.get(stack.size() - 1));
		}

		public void push(double x, double y, double z) {
			stack.add(new Point3d(x, y, z));
		}

		public void pop() {
			stack.remove(stack.size() - 1);
		}
	}
	
	static class ArrayStackTest implements StackTest {
		double[] stack = new double[1000];
		int pointer = -1;
		
		public void add() {
			stack[pointer + 1] = stack[pointer - 5] + stack[pointer - 2];
			stack[pointer + 2] = stack[pointer - 4] + stack[pointer - 1];
			stack[pointer + 3] = stack[pointer - 3] + stack[pointer];
			pointer += 3;
		}

		public void print() {
			System.out.println(stack[pointer - 2] + "," + stack[pointer - 1] + "," + stack[pointer]);
		}

		public void push(double x, double y, double z) {
			stack[pointer + 1] = x;
			stack[pointer + 2] = y;
			stack[pointer + 3] = z;
			pointer += 3;
		}

		public void pop() {
			pointer -= 3;
		}
	}
}
