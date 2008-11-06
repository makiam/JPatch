package com.jpatch.test;

import org.junit.Test;
import static org.junit.Assert.*;
import junit.framework.JUnit4TestAdapter;

public class JUnitTest {
	@Test
	public void testHelloWorld() {
		String s = "HelloxWorld";
		assertEquals("Just a test to see if everything works...", "HelloWorld", s);
	}
}
