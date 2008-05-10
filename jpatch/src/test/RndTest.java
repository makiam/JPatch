package test;

public class RndTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (int i = 0; i < 1000000; i++) {
			if (Math.random() < 0.0) {
				throw new RuntimeException("Math.random() is broken!");
			}
		}
	}

}
