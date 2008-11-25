package test;

public class MethodTest {
	
	private void test() {
		final int b;
		class X {
			int incr(int a) {
				return a + b;
			}
		}
		int a = new X().incr(3);
	}
}
