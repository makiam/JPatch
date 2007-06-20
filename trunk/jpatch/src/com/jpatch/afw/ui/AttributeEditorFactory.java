package com.jpatch.afw.ui;

import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.*;

import jpatch.boundary.AbstractAttributeEditor.Item;
import jpatch.boundary.AbstractAttributeEditor.Type;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class AttributeEditorFactory {
	private static final AttributeEditorFactory INSTANCE = new AttributeEditorFactory();
	
	private Map <Class, AttributeEditor> editors = new HashMap<Class, AttributeEditor>();
	
	private AttributeEditorFactory() { }
	
	public static AttributeEditorFactory getInstance() {
		return INSTANCE;
	}
	
	public AttributeEditor getEditorFor(Object object, Color borderColor) {
		AttributeEditor editor = editors.get(object.getClass());
		if (editor == null) {
			URL url = null;
			Class objectClass = object.getClass();
			while (url == null) {
				url = ClassLoader.getSystemResource(objectClass.getName().replace('.', '/') + ".xml");
				System.out.println("URL=" + url);
				editor = editors.get(objectClass);
				if (editor != null) {
					editors.put(object.getClass(), editor);
					return editor;
				}
				if (url == null) {
					objectClass = objectClass.getSuperclass();
				}
			}
			try {
				System.out.println("reading...");
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				AttributeContentHandler handler = new AttributeContentHandler(objectClass, object, borderColor);
				xmlReader.setContentHandler(handler);
				xmlReader.parse(url.toString());
				editor = handler.getEditor();
				editors.put(object.getClass(), editor);
				return editor;
			} catch (SAXException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		editor.setEntity(object);
		return editor;
	}
	
	private static class AttributeContentHandler extends DefaultHandler {
		private final Class objectClass;
		private final AttributeEditor editor;
		
		private AttributeContentHandler(Class objectClass, Object entity, Color borderColor) {
			this.objectClass = objectClass;
			this.editor = new AttributeEditor(objectClass, objectClass.getSimpleName(), entity, borderColor);
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			System.out.println(localName);
			if (localName.equals("attributeEditor")) {
				if (!attributes.getValue("class").equals(objectClass.getName())) {
					throw new SAXException("wrong class: " + attributes.getValue("class") + ", excpected " + objectClass.getSimpleName());
				}
			}
			if (localName.equals("group")) {
				editor.startContainer(attributes.getValue("name"));
			} else if (localName.equals("field")) {
				editor.addField(attributes.getValue("name"), attributes.getValue("attribute"));
			} else if (localName.equals("limits")) {
				editor.addLimits(attributes.getValue("attribute"));
			} else if (localName.equals("slider")) {
				editor.addSlider(attributes.getValue("name"), attributes.getValue("attribute"));
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("group")) {
				editor.endContainer();
			}
		}
		
		public AttributeEditor getEditor() {
			return editor;
		}
	}
}
