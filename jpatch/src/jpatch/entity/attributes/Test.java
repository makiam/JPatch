package jpatch.entity.attributes;

import java.util.*;

public class Test<X> {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		DoubleAttr doubleAttr = new DoubleAttr(90);
//		DoubleAttr min = new DoubleAttr(0.5);
//		DoubleAttr max = new DoubleAttr(11.3);
//		
//		Constraint minConstr = new LimitConstraint.Minimum(10, min);
//		Constraint maxConstr = new LimitConstraint.Minimum(10, max);
//		
//		minConstr.enforceOn(doubleAttr);
//		maxConstr.enforceOn(doubleAttr);
//		System.out.println(doubleAttr);
		Test<double> test = new Test<double>();
		Collection<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		
		String x = test.doTest2("aaa");
	}
	
	<T> T doTest(Collection<T> c) {
		return c.iterator().next();
	}
	
	X doTest2(Object o) {
		return (X) o;
	}
}
