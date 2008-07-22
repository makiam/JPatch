package test;

public class EnumTest {
	public enum E1 { A, B, C }
	public enum E2 implements E1 { D, E }
	
	public static void main(String[] args) {
		new EnumTest();
	}
}
