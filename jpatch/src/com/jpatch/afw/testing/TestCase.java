package com.jpatch.afw.testing;

public class TestCase {
	@TestClass
	public static class Tests {
		
		@Test
		public TestResult ok() {
			return TestResult.success();
		}
		
		@Test
		public TestResult warn() {
			return TestResult.warning("a warning");
		}
		
		@Test
		public TestResult fail() {
			return TestResult.error("an error");
		}
	}
}
