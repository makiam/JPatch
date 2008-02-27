package com.jpatch.test;

public class InnerClassTest {
	String name;
	InnerClassTest inner;
	
	private void createInner() {
		inner = new InnerClassTest() {
			public String toString() {
				return InnerClassTest.this.name;
			}
		};
	}
	
	private void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
	public static void main(String[] args) {
		InnerClassTest outer = new InnerClassTest();
		outer.setName("outer");
		outer.createInner();
		outer.inner.setName("inner");
		outer.inner.createInner();
		outer.inner.inner.setName("inner inner");
		System.out.println(outer);
		System.out.println(outer.inner);
		System.out.println(outer.inner.inner);
	}
}
