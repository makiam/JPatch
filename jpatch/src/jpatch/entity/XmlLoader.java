package jpatch.entity;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * This class is used to parse JPatch XML files
 * @author sascha
 */
public class XmlLoader {
	/** Debugging flag, if true, parser input will be dumped to System.out */
	private static final boolean DEBUG = true;
	
	/** stringbuilder to store character input */
	private StringBuilder characters = new StringBuilder();
	
	/** list used as a stack for ContentHandlers */
	private List<ContentHandler> stack = new ArrayList<ContentHandler>();
	
	/** the currently active ContentHandler (the last on the stack) */
	private ContentHandler activeHandler;
	
	/** position of the activeHandler on the stack */
	private int stackPosition = -1;
	
	/** used to map IDs (integers) to ControlPoints */
	private Map<Integer, ControlPoint> cpIdMap = new HashMap<Integer, ControlPoint>();
	
	/** used to temporarily remember which ControlPoint is attached - can be resolved at the end once the cpIdMap is complete */
	private Map<ControlPoint, Integer> cpAttachMap = new HashMap<ControlPoint, Integer>();
	
	/** XmlReader to parse documents */
	private XMLReader xmlReader = XMLReaderFactory.createXMLReader();
	
	private Object parsedObject;
	
	/* * * * * * * *
	 * Constructors
	 * * * * * * * */
	
	public XmlLoader() throws SAXException { }
	
	/* * * * * * * * *
	 * public methods
	 * * * * * * * * */
	
	public Object parse(Reader reader) throws IOException, SAXException {
		parsedObject = null;
		push(new RootParser());
		xmlReader.setContentHandler(new DefaultHandler() {
			@Override
			public void characters(char[] ch, int start, int length) throws SAXException {
				if (DEBUG) {
					System.out.print(new String(ch, start, length));
				}
				characters.append(ch, start, length);
			}
			
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				if (DEBUG) {
					System.out.print("<" + localName);
					for (int i = 0, n = attributes.getLength(); i < n; i++) {
						System.out.print(" " + attributes.getLocalName(i) + "=\"" + attributes.getValue(i) + "\"");
					}
					System.out.print(">");
				}
				activeHandler.startElement(uri, localName, qName, attributes);
			}
			
			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				if (DEBUG) {
					System.out.print("</" + localName + ">");
				}
				activeHandler.endElement(uri, localName, qName);
			}
		});
		xmlReader.parse(new InputSource(reader));
		reader.close();
		return parsedObject;
	}
	
	/* * * * * * * * *
	 * private methods
	 * * * * * * * * */
	
	/**
	 * pushes the specified ContentHandler on the stack (making it the activeHandler)
	 * @param handler the ContentHandler to push on the stack
	 */
	private void push(ContentHandler handler) {
		stack.add(handler);
		stackPosition++;
		activeHandler = handler;
	}
	
	/**
	 * pops the activeHandler from the stack (making the previous handler on the stack
	 * the activeHandler)
	 */
	private void pop() {
		stack.remove(stackPosition);
		activeHandler = stack.get(--stackPosition);
	}

	/* * * * * * * * *
	 * inner classes
	 * * * * * * * * */
	
	/**
	 * This ContentHandler is used to parse the root level of the document
	 */
	private class RootParser extends DefaultHandler {
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (localName.equals("model")) {
				push(new ModelParser(attributes));
			}
		}
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			pop();
		}
	}
	
	/**
	 * This ContentHandler is used to parse models
	 */
	private class ModelParser extends DefaultHandler {
		private Model model;
		ModelParser(Attributes attributes) {
			model = new Model();
			model.name.set(attributes.getValue("name"));
		}
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (localName.equals("curve")) {
				push(new CurveParser(model, attributes));
			}
		}
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("model")) {
				/*
				 * connect attached ControlPoints
				 */
				for (ControlPoint cp : cpAttachMap.keySet()) {
					ControlPoint attach = cpIdMap.get(cpAttachMap.get(cp));
					attach.setNextAttached(cp);
					cp.setPrevAttached(attach);
				}
				parsedObject = model;
				pop();
			}
		}
	}
	
	/**
	 * This ContentHandler is used to parse curves (ControlPoints)
	 */
	private class CurveParser extends DefaultHandler {
		private Model model;
		private boolean loop;
		private ControlPoint start;
		private ControlPoint cp;
		CurveParser(Model model, Attributes attributes) {
			this.model = model;
			loop = ("true".equals(attributes.getValue("loop")));
		}
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (localName.equals("cp")) {
				ControlPoint tmp = new ControlPoint(model);
				if (cp == null) {
					start = tmp;
				} else {
					cp.setNext(tmp);
					tmp.setPrev(cp);
				}
				cp = tmp;
				cp.setId(Integer.parseInt(attributes.getValue("id")));
				cpIdMap.put(cp.getId(), cp);
				String attach = attributes.getValue("attach");
				String hook = attributes.getValue("hook");
				String mag = attributes.getValue("mag");
				if (hook == null && attach == null) {
					cp.position.set(
							Double.parseDouble(attributes.getValue("x")),
							Double.parseDouble(attributes.getValue("y")),
							Double.parseDouble(attributes.getValue("z"))
					);
				} else {
					if (attach != null) {
						cpAttachMap.put(cp, Integer.parseInt(attach));
					}
					if (hook != null) {
						cp.hookPos.set(Double.parseDouble(hook));
					}
				}
				if (mag != null) {
					cp.magnitude.set(Double.parseDouble(mag));
				}
			}
		}
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("curve")) {
				if (loop) {
					start.setLoop(true);
					start.setPrev(cp);
					cp.setNext(start);
				}
				model.addCurve(start);
				pop();
			}
		}
	}
}
