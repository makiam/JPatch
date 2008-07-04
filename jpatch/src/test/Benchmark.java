package test;

public class Benchmark {
	static final int IT = 100000000;
	static float value;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (int i = 2; i < 4; i++) {
			test(i);
			long t = System.nanoTime();
			test(i);
			System.out.println("Test " + i + ":" + (System.nanoTime() - t) + "ns");
		}
	}

	private static void test(int id) {
		switch (id) {
			case 0: mulRandom();
			break;
			case 1: divRandom();
			break;
			case 2: mulConstant();
			break;
			case 3: divConstant();
			break;
		}
	}
	private static void mulRandom() {
		for (int i = 0; i < IT; i++) {
			value = i * Math.random();
		}
	}
	
	private static void divRandom() {
		for (int i = 0; i < IT; i++) {
			value = i / Math.random();
		}
	}
	
	private static void mulConstant() {
		final float a = (float) Math.random();
		for (int i = 0; i < IT; i++) {
			value = i * a;
		}
	}
	
	private static void divConstant() {
		final float a = (float) Math.random();
		for (int i = 0; i < IT; i++) {
			value = i / a;
		}
	}
}
