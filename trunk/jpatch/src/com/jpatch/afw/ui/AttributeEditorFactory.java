package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.*;

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
	private Map <String, BooleanAttr> expansionControls = new HashMap<String, BooleanAttr>();
	
	private AttributeEditorFactory() { }
	
	public static AttributeEditorFactory getInstance() {
		return INSTANCE;
	}
	
	public AttributeEditor getEditorFor(Object object, BooleanAttr expansionControl, Color borderColor) {
		System.out.println("getEditorFor(" + object + ")");
		AttributeEditor editor = editors.get(object.getClass());
		if (editor == null) {
			URL url = null;
			Class objectClass = object.getClass();
			while (url == null) {
				System.out.println(objectClass + " " + objectClass.getName());
				url = ClassLoader.getSystemResource(objectClass.getName().replace('.', '/') + ".xml");
				System.out.println("URL=" + url);
				editor = editors.get(objectClass);
				if (editor != null) {
					editors.put(object.getClass(), editor);
					return editor;
				}
				if (url == null) {
					objectClass = objectClass.getSuperclass();
					if (objectClass == null) {
						return null;
					}
				}
			}
			try {
				System.out.println("reading...");
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				AttributeContentHandler handler = new AttributeContentHandler(objectClass, object, expansionControl, borderColor);
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
	
	private class AttributeContentHandler extends DefaultHandler {
		private final Class objectClass;
		private final AttributeEditor editor;
		
		private AttributeContentHandler(Class objectClass, Object entity, BooleanAttr expansionControl, Color borderColor) {
			this.objectClass = objectClass;
			this.editor = new AttributeEditor(objectClass, objectClass.getSimpleName(), expansionControl, entity, borderColor);
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (localName.equals("attributeEditor")) {
				if (!attributes.getValue("class").equals(objectClass.getName())) {
					throw new SAXException("wrong class: " + attributes.getValue("class") + ", excpected " + objectClass.getSimpleName());
				}
			} else if (localName.equals("boolean")) {
				String display = attributes.getValue("display");
				if (display.equals("checkbox")) {
					editor.setBooleanValues(null, null);
				} else if (display.equals("switch")) {
					editor.setBooleanValues(attributes.getValue("false"), attributes.getValue("true"));
				}
			} else if (localName.equals("group")) {
				String expansion = attributes.getValue("expansion");
				BooleanAttr expansionControl = null;
				if (expansion != null) {
					expansionControl = expansionControls.get(expansion);
					if (expansionControl == null) {
						expansionControl = new BooleanAttr();
						expansionControls.put(expansion, expansionControl);
					}
				}
				editor.startContainer(attributes.getValue("name"), expansionControl);
			} else if (localName.equals("field")) {
				editor.addField(attributes.getValue("name"), attributes.getValue("attribute"));
			} else if (localName.equals("limits")) {
				editor.addLimits(attributes.getValue("attribute"));
			} else if (localName.equals("slider")) {
				String mapping = attributes.getValue("mapping");
				if (mapping != null && mapping.equals("exponential")) {
					editor.setMapping(ExponentialMapping.getInstance());
				} else {
					editor.setMapping(IdentityMapping.getInstance());
				}
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
