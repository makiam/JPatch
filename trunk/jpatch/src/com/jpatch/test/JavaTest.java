package com.jpatch.test;

public class JavaTest {

	public static void main(String[] args) {
		Parent p = new Parent();
		Child c = new Child();
		p.publicMethod();
		c.publicMethod();
	}
		static class Parent {
			public void publicMethod() {
				System.out.println("Parent: public");
				protectedMethod();
			}
			protected void protectedMethod() {
				System.out.println("Parent: protected");
			}
		}
		
		static class Child extends Parent {
			protected void protectedMethod() {
				System.out.println("Child: protected");
			}
		}
		
}
