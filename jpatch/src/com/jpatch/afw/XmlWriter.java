package com.jpatch.afw;

import com.jpatch.afw.attributes.*;
import jpatch.entity.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

import javax.vecmath.*;

public class XmlWriter {
	private static final String indent = "  ";
	private static final String newLine = "\n";
	private static final boolean ASCII_ONLY = false;
	
	private final Stack<String> stack = new Stack<String>();
	private final Writer writer;
	private boolean inElement;
	private boolean inDocument;
	private boolean isNewLine;
	
	public XmlWriter(Writer writer) {
		this.writer = writer;
	}
	
	public void startDocument() throws IOException {
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.write(newLine);
		isNewLine = true;
		inDocument = true;
	}
	
	public void startDocument(String docType) throws IOException {
		startDocument();
		writer.write(docType);
		writer.write(newLine);
		isNewLine = true;
	}
	
	public void startElement(String elementName) throws IOException {
		if (!inDocument) {
			throw new IllegalStateException();
		}
		if (inElement) {
			writer.write(">");
			writer.write(newLine);
		}
		inElement = true;
		for (int i = 0; i < stack.size(); i++) {
			writer.write(indent);
		}
		writer.write("<");
		writer.write(elementName);
		stack.push(elementName);
		isNewLine = false;
	}
	
	public void characters(CharSequence c) throws IOException {
		if (!inDocument) {
			throw new IllegalStateException();
		}
		if (inElement) {
			writer.write(">");
			inElement = false;
		}
		writeEscaped(c);
		isNewLine = false;
	}
	
	public void endElement() throws IOException {
		if (!inDocument) {
			throw new IllegalStateException();
		}
		String elementName = stack.pop();
		if (inElement) {
			writer.write(" />");
			writer.write(newLine);
			isNewLine = true;
			inElement = false;
		} else {
			if (isNewLine) {
				for (int i = 0; i < stack.size(); i++) {
					writer.write(indent);
				}
			}
			writer.write("</");
			writer.write(elementName);
			writer.write(">");
			writer.write(newLine);
			isNewLine = true;
		}
	}
	
	public void endDocument() throws IOException {
		if (!inDocument && !stack.isEmpty()) {
			throw new IllegalStateException();
		}
		inDocument = false;
	}
	
	public void intArray(int[] array) throws IOException {
		if (!inDocument) {
			throw new IllegalStateException();
		}
		if (inElement) {
			writer.write(">");
			inElement = false;
		}
		writer.write(Integer.toString(array[0]));
		for (int i = 1; i < array.length; i++) {
			writer.write(' ');
			writer.write(Integer.toString(array[i]));
		}
		isNewLine = false;
	}
	
	public void writeTuple(Tuple3d tuple) throws IOException {
		if (!inDocument) {
			throw new IllegalStateException();
		}
		if (inElement) {
			writer.write('>');
			inElement = false;
		}
		writer.write(Double.toString(tuple.x));
		writer.write(' ');
		writer.write(Double.toString(tuple.y));
		writer.write(' ');
		writer.write(Double.toString(tuple.z));
	}
	
	public void attribute(String name, byte b) throws IOException {
		saveAttribute(name, Byte.toString(b));
	}
	
	public void attribute(String name, short s) throws IOException {
		saveAttribute(name, Short.toString(s));
	}
	
	public void attribute(String name, char c) throws IOException {
		attribute(name, Character.toString(c));
	}
	
	public void attribute(String name, int i) throws IOException {
		saveAttribute(name, Integer.toString(i));
	}
	
	public void attribute(String name, long l) throws IOException {
		saveAttribute(name, Long.toString(l));
	}
	
	public void attribute(String name, float f) throws IOException {
		saveAttribute(name, Float.toString(f));
	}
	
	public void attribute(String name, double d) throws IOException {
		saveAttribute(name, Double.toString(d));
	}
	
	public void attribute(String name, boolean b) throws IOException {
		saveAttribute(name, Boolean.toString(b));
	}
	
	public void attribute(String name, BooleanAttr attr) throws IOException {
		attribute(name, attr.getBoolean());
	}
	
	public void attribute(String name, IntAttr attr) throws IOException {
		attribute(name, attr.getInt());
	}
	
	public void attribute(String name, DoubleAttr attr) throws IOException {
		attribute(name, attr.getDouble());
	}
	
	public void attribute(String name, GenericAttr<String> attr) throws IOException {
		attribute(name, attr.getValue());
	}
	
	public void attribute(String name, String value) throws IOException {
		if (!inElement) {
			throw new IllegalStateException();
		}
		writer.write(" ");
		writer.write(name);
		writer.write("=\"");
		writeEscaped(value);
		writer.write("\"");
		isNewLine = false;
	}
	
	private void saveAttribute(String name, String value) throws IOException {
		if (!inElement) {
			throw new IllegalStateException();
		}
		writer.write(" ");
		writer.write(name);
		writer.write("=\"");
		writer.write(value);
		writer.write("\"");
		isNewLine = false;
	}
	
	private void writeEscaped(CharSequence c) throws IOException {
		for (int i = 0; i < c.length(); i++) {
			char ch = c.charAt(i);
			switch(ch) {
			case '&':
				writer.write("&amp;");
				break;
			case '<':
				writer.write("&lt;");
				break;
			case '>':
				writer.write("&gt;");
				break;
			case '\"':
				writer.write("&quot;");
				break;
			default:
				if (ASCII_ONLY && ch > '\u007f') {
					writer.write("&#");
					writer.write(Integer.toString(ch));
					writer.write(';');
				} else {
					writer.write(ch);
				}
			}
		}
		isNewLine = false;
	}
}
