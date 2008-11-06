package com.jpatch.afw.testing;

import java.util.*;

public final class TestResult {
	public static enum Type { SUCCESS, WARNING, ERROR }
	private final Type type;
	private final String message;
	private final StackTraceElement stackTraceElement;
	
	public static TestResult success() {
		return new TestResult(Type.SUCCESS, "");
	}
	
	public static TestResult warning(String message) {
		return new TestResult(Type.WARNING, message);
	}
	
	public static TestResult error(String message) {
		return new TestResult(Type.ERROR, message);
	}
	
	private TestResult(Type type, String message) {
		this.type = type;
		this.message = message;
		this.stackTraceElement = Thread.currentThread().getStackTrace()[3];
	}
	
	public String toString() {
		return message + " (" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")";
	}
	
	public boolean isSuccess() {
		return type == Type.SUCCESS;
	}
	
	public boolean isWarning() {
		return type == Type.WARNING;
	}
	
	public boolean isError() {
		return type == Type.ERROR;
	}
}
