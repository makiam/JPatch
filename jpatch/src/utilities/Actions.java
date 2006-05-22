/*
 * $Id$
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package utilities;

import java.net.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * @author sascha
 *
 */
public class Actions extends DefaultHandler {

	private static final String resource = "jpatch/boundary/action/actions.xml";
	private static final String prefix = "\t\t";
	private static final String mapName = "actionMap";
	
	private StringBuilder chars;
	private String actionName;
	
	public static void main(String args[]) {
		new Actions(ClassLoader.getSystemResource(resource));
	}
	
	private Actions(URL url) {
		System.out.println(prefix + "/*");
		System.out.println(prefix + " * BEGIN OF AUTO-GENERATED CODE - DO NOT MODIFY");
		System.out.println(prefix + " * The following lines have been generated with " + getClass().getName());
		System.out.println(prefix + " * from the file \"" + resource + "\"");
		System.out.println(prefix + " */");
		System.out.println();
		XMLReader xmlReader = null;
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			try {
				xmlReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			} catch (SAXException ee) {
				ee.printStackTrace();
			}
		}
		try {
			xmlReader.setContentHandler(this);
			xmlReader.parse(new InputSource(url.toString()));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		System.out.println();
		System.out.println(prefix + "/*");
		System.out.println(prefix + " * END OF AUTO-GENERATED CODE");
		System.out.println(prefix + " */");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		chars = new StringBuilder();
		if (localName.equals("action")) {
			actionName = attributes.getValue("name");
		}
	}

	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("constructor")) {
			System.out.println(prefix + mapName + ".put(\"" + actionName + "\", new ActionDescriptor(" + chars + "));");
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		chars.append(ch, start, length);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
//		characters(ch, start, length);
	}
}
