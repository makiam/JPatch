package sds;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.vecmath.*;

public class JptLoader {
	private List<Cp> cpList = new ArrayList<Cp>();
	private Map<Integer, Cp> childHookIndex = new HashMap<Integer, Cp>();
	private int cpIndex;
	private Sds sds;
	
	public ContentHandler handler = new DefaultHandler() {
		StringBuilder chars;
		Cp prevCp;
		boolean patch;
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (chars != null) {
				chars.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (patch) {
				if (localName.equals("patch")) {
					patch = false;
				} else if (localName.equals("points")) {
					String[] cpIndexes = chars.toString().split("[,\\ ]");
					List<TopLevelVertex> vertexList = new ArrayList<TopLevelVertex>();
					for (int i = 0; i < cpIndexes.length; i += 2) {
						Cp cp = cpList.get(Integer.parseInt(cpIndexes[i]));
						Cp cpNext = cpList.get(Integer.parseInt(cpIndexes[i + 1]));
						Cp childHook0 = childHookIndex.get(cp.index);
						Cp childHook1 = childHookIndex.get(cpNext.index);
						vertexList.add(cp.getVertex());
						if (childHook0 != null) {
							Cp end = null;
							for (end = childHook0.next; end.next != null; end = end.next);
							if (end.getHead() == cpNext.getHead()) {
								for (Cp hook = childHook0.next; hook.next != null; hook = hook.next) {
									vertexList.add(hook.getVertex());
								}
							}
						}
						if (childHook1 != null) {
							Cp end = null;
							for (end = childHook1.next; end.next != null; end = end.next);
							if (end.getHead() == cp.getHead()) {
								for (Cp hook = end.prev; hook.prev != null; hook = hook.prev) {
									vertexList.add(hook.getVertex());
								}
							}
						}
						if (cp.hookPos != -1) {
							Cp start, end;
							for (start = cp; start.prev != null; start = start.prev);
							for (end = cp; end.next != null; end = end.next);
							cp.vertex.pos.interpolate(start.getVertex().pos, end.getVertex().pos, cp.hookPos);
						}
					}
					System.out.println(vertexList);
					sds.addFace(vertexList.toArray(new TopLevelVertex[vertexList.size()]));
					chars = null;
				}
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			System.out.print("<" + localName + " ");
			for (int i = 0; i < attributes.getLength(); i++) {
				System.out.print(attributes.getLocalName(i) + "=\"" + attributes.getValue(i) + "\" ");
			}
			System.out.println(">");
			if (localName.equals("curve")) {
				prevCp = null;
			} else if (localName.equals("cp")) {
				String attach = attributes.getValue("attach");
				String hook = attributes.getValue("hook");
				String hookPos = attributes.getValue("hookpos");
				Cp cp;
				if (attach != null) {
					cp = new Cp(Integer.parseInt(attach));
				} else if (hook != null) {
					int parentHookIndex = Integer.parseInt(hook);
					cp = new Cp(parentHookIndex);
					if (prevCp == null) {
						childHookIndex.put(parentHookIndex, cp);
					}
				} else if (hookPos != null) {
					cp = new Cp(Double.parseDouble(hookPos));
				} else {
					cp = new Cp(
							Double.parseDouble(attributes.getValue("x")),
							Double.parseDouble(attributes.getValue("y")),
							Double.parseDouble(attributes.getValue("z"))
					);
				}
				cpList.add(cp);
				if (prevCp != null) {
					prevCp.next = cp;
					cp.prev = prevCp;
				}
				prevCp = cp;
			} else if (localName.equals("points") && patch == true) {
				chars = new StringBuilder();
			} else if (localName.equals("patch")) {
				patch = true;
			}
		}
		
	};
	
	public Sds importModel(InputStream inputStream) throws IOException {
		XMLReader xmlReader;
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(handler);
			sds = new Sds();
			cpList.clear();
			childHookIndex.clear();
			cpIndex = 0;
			xmlReader.parse(new InputSource(inputStream));
			inputStream.close();
			sds.validateVertices();
			sds.dump();
			sds.makeSlates();
			sds.rethinkSlates();
			return sds;
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private class Cp {
		final int index = cpIndex++, attach;
		final TopLevelVertex vertex;
		final double hookPos;
		Cp prev, next;
		
		Cp(double x, double y, double z) {
			vertex = new TopLevelVertex(x, y, z);
			attach = -1;
			hookPos = -1;
			sds.vertexList.add(vertex);
		}
		
		Cp(int attach) {
			this.attach = attach;
			vertex = null;
			hookPos = -1;
		}
		
		Cp(double hookPos) {
			vertex = new TopLevelVertex();
			this.hookPos = hookPos;
			attach = -1;
			sds.vertexList.add(vertex);
		}
		
		Cp getHead() {
			return (attach == -1) ? this : cpList.get(attach).getHead();
		}
		
		TopLevelVertex getVertex() {
			return getHead().vertex;
		}
	}
}
