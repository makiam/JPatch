package jpatch.boundary;

import java.io.*;
import java.net.*;
import java.util.*;

import jpatch.boundary.AbstractAttributeEditor.Item;
import jpatch.boundary.AbstractAttributeEditor.Type;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class AttributeEditorFactory {
	public static final AttributeEditorFactory INSTANCE = new AttributeEditorFactory();
	
	private Map <Class, Item[][]> schemaCache = new HashMap<Class, Item[][]>();
	
	private AttributeEditorFactory() { }
	
	public AbstractAttributeEditor createEditorFor(Object object) {
		Item[][] schema = schemaCache.get(object.getClass());
		if (schema == null) {
			URL url = null;
			Class objectClass = object.getClass();
			while (url == null) {
				url = ClassLoader.getSystemResource(objectClass.getName().replace('.', '/') + ".xml");
				System.out.println("URL=" + url);
				schema = schemaCache.get(objectClass);
				if (schema != null) {
					schemaCache.put(object.getClass(), schema);
					return new AbstractAttributeEditor(object, schema);
				}
				if (url == null) {
					objectClass = objectClass.getSuperclass();
				}
			}
			try {
				System.out.println("reading...");
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				AttributeContentHandler handler = new AttributeContentHandler(objectClass);
				xmlReader.setContentHandler(handler);
				xmlReader.parse(url.toString());
				schema = handler.getSchema();
				schemaCache.put(object.getClass(), schema);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new AbstractAttributeEditor(object, schema);
	}
	
	private static class AttributeContentHandler extends DefaultHandler {
		private Class objectClass;
		private final List<Item[]> sections = new ArrayList<Item[]>();
		private List<Item> section = null;
		
		private AttributeContentHandler(Class objectClass) {
			this.objectClass = objectClass;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			try {
				if (localName.equals("attributeEditor")) {
					if (!attributes.getValue("class").equals(objectClass.getSimpleName())) {
						throw new SAXException("wrong class: " + attributes.getValue("class") + ", excpected " + objectClass.getSimpleName());
					}
				}
				if (localName.equals("section")) {
					section = new ArrayList<Item>();
				} else if (localName.equals("attribute")) {
					Item item = new Item(
							Type.ATTRIBUTE,
							attributes.getValue("name"),
							objectClass.getField(attributes.getValue("field")),
							attributes.getValue("widget"),
							attributes.getValue("min"),
							attributes.getValue("max")
					);
					addItem(item);
					System.out.println(item);
				} else if (localName.equals("limits")) {
					Item item = new Item(Type.LIMIT, null, objectClass.getField(attributes.getValue("field")), null, null, null);
					addItem(item);
					System.out.println(item);
				}
			} catch (SecurityException e) {
				throw new SAXException(e);
			} catch (NoSuchFieldException e) {
				throw new SAXException(e);
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("section")) {
				Item[] items = section.toArray(new Item[section.size()]);
				sections.add(items);
				section = null;
			}
		}
		
		private void addItem(Item item) {
			if (section == null) {
				sections.add(new Item[] { item });
			} else {
				section.add(item);
			}
		}
		
		public Item[][] getSchema() {
			return sections.toArray(new Item[sections.size()][]);
		}
	}
}
