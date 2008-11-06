package com.jpatch.afw.testing;

import java.io.*;
import java.lang.reflect.*;

public class TestRunner {
	private static final String BIN = "bin";
	private static final String BASE = "com.jpatch";
	
	private static final Object[] VOID = new Object[0];
	
	private static final FileFilter PACKAGE_FILE_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	};
	private static final FileFilter CLASS_FILE_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			String name = pathname.getName();
			return !name.contains("$") && name.endsWith(".class");
		}
	};
	
	private int classes;
	private int testSuits;
	private int successes;
	private int warnings;
	private int errors;
	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) {
		TestRunner testRunner = new TestRunner();
		testRunner.testPackage(new File(BIN, BASE.replace('.', File.separatorChar)));
		testRunner.reportTotals();
	}

	private void testPackage(File path) {
		for (File classFile : path.listFiles(CLASS_FILE_FILTER)) {
			try {
				boolean classCounted = false;
				Class<?> mainClass = ClassLoader.getSystemClassLoader().loadClass(classNameFor(classFile));
				for (Class<?> subClass : mainClass.getDeclaredClasses()) {
					TestClass testClass = subClass.getAnnotation(TestClass.class);
					if (testClass != null) {
						if ((subClass.getModifiers() & Modifier.STATIC) != 0) {
							try {
								Object testObject = subClass.newInstance();
								if (!classCounted) {
									classes++;
									classCounted = true;
								}
								testSuits++;
								for (Method testMethod : subClass.getMethods()) {
									Test test = testMethod.getAnnotation(Test.class);
									if (test != null) {
										try {
											TestResult testResult = (TestResult) testMethod.invoke(testObject, VOID);
											report(subClass, testMethod, testResult);
										} catch (IllegalArgumentException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (InvocationTargetException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							} catch (InstantiationException e) {
								warn(e.getMessage());
							} catch (IllegalAccessException e) {
								warn(e.getMessage());
							}
						} else {
							warn("Test-Class is not static");
						}
					}
				}
			} catch (ClassNotFoundException e) {
				warn("Class not found:" + classFile);
			}
		}
		for (File subPackage : path.listFiles(PACKAGE_FILE_FILTER)) {
			testPackage(subPackage);
		}
	}
	
	private void reportTotals() {
		System.out.println("---");
		System.out.println(classes + " classes, " + testSuits + " testSuits, " + (successes + warnings + errors) + " tests");
		System.out.println(successes + " success, " + warnings + " warnings, " + errors + " errors");
	}
	
	private String classNameFor(File classFile) {
		String path = classFile.getPath();
		String className = classFile.getPath().replace(File.separatorChar, '.').substring(BIN.length() + 1, path.length() - 6);
		return className;
	}
	
	private void warn(String warning) {
		System.out.println("WARNING: " + warning);
	}
	
	private void report(Class<?> testClass, Method testMethod, TestResult result) {
		if (result.isSuccess()) {
			successes++;
			return;
		} else if (result.isWarning()) {
			warnings++;
			System.out.print("WARNING " + warnings + ":\t ");
		} else if (result.isError()) {
			errors++;
			System.out.print("ERROR " + errors + ":\t ");
		}
		System.out.print(testClass.getName() + "." + testMethod.getName() + "() ");
		System.out.println(result);
	}
}
