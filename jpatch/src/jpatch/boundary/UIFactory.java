/*
 * $Id: UIFactory.java,v 1.16 2006/04/18 22:32:14 sascha_l Exp $
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
package jpatch.boundary;

import java.awt.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import jpatch.boundary.action.*;
import jpatch.boundary.settings.*;
import jpatch.boundary.ui.*;

/**
 * @author sascha
 */

public class UIFactory extends DefaultHandler {
//	private StringBuffer sbChars = new StringBuffer();
	private ArrayList listMenu = new ArrayList();
	private JToolBar toolBar;
	private JMenuBar menuBar = new JMenuBar();
	private JPopupMenu popupMenu = new JPopupMenu();
	private JMenu viewMenu;
	private Map mapObjects = new HashMap();
	private Map mapLayout = new HashMap();
	private Icon emptyIcon = new Icon() {
		public void paintIcon(Component c, Graphics g, int x, int y) { }

		public int getIconWidth() {
			return 15;
		}

		public int getIconHeight() {
			return 15;
		}
	};
	public void parseLayout(URL url) {
		
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
			System.out.println("Building menues and toolbars...");
			xmlReader.parse(new InputSource(url.toString()));
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		
		mapObjects.put("menubar", menuBar);
		mapObjects.put("viewport popup", popupMenu);
		mapObjects.put("view menu", viewMenu);
		
		/*
		 * Bind unbound actions to JPatchScreen
		 */
		Set<Action> unboundActions = Actions.getInstance().getUnboundActions();
		if (unboundActions.size() > 0) {
			InputMap inputMap = MainFrame.getInstance().getJPatchScreen().getInputMap(JComponent.WHEN_FOCUSED);
			ActionMap actionMap = MainFrame.getInstance().getJPatchScreen().getActionMap();
			for (Action action : unboundActions) {
				String accelerator = (String) action.getValue(JPatchAction.ACCELERATOR);
				if (accelerator == null)
					throw new IllegalArgumentException("Action " + action + " is not bound to a menu or button, yet has no keyboard accelerator");
				KeyStroke ks = KeyStroke.getKeyStroke(accelerator);
				inputMap.put(ks, action);
				actionMap.put(action, action);
			}
		}
	}
	
	public JComponent getComponent(String key) {
		/*
		 * if the main toolbar was requested, set the
		 * viewport button's selection state
		 */
		if (key.equals("main toolbar")) {
			switch (Settings.getInstance().viewports.viewportMode) {
			case SINGLE:
				Actions.getInstance().getButton("single view").setSelected(true);
				break;
			case HORIZONTAL_SPLIT:
				Actions.getInstance().getButton("horizontally split view").setSelected(true);
				break;
			case VERTICAL_SPLIT:
				Actions.getInstance().getButton("vertically split view").setSelected(true);
				break;
			case QUAD:
				Actions.getInstance().getButton("quad view").setSelected(true);
				break;
			}
		}
		/*
		 * return the requested component
		 */
		return (JComponent) mapObjects.get(key);
	}
	
	public Object getLayout(String key) {
		return mapLayout.get(key);
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) {	
//		System.out.print("<" + localName);
//		for (int i = 0; i < attributes.getLength(); i++)
//			System.out.print(" " + attributes.getLocalName(i) + "=\"" + attributes.getValue(i) + "\"");
//		System.out.println(">");
		if (localName.equals("toolbar")) {
			toolBar = new JToolBar();
			toolBar.setFloatable(false);
			String position = "";
			for (int i = 0; i < attributes.getLength(); i++) {
				if (attributes.getLocalName(i).equals("name"))
					toolBar.setName(attributes.getValue(i));
				else if (attributes.getLocalName(i).equals("position")) {
					if (attributes.getValue(i).equals("north"))
							position = BorderLayout.NORTH;
					if (attributes.getValue(i).equals("east"))
							position = BorderLayout.EAST;
					if (attributes.getValue(i).equals("south"))
							position = BorderLayout.SOUTH;
					if (attributes.getValue(i).equals("west"))
							position = BorderLayout.WEST;
				}
			}
			if (position == BorderLayout.NORTH || position == BorderLayout.SOUTH)
				toolBar.setOrientation(JToolBar.HORIZONTAL);
			else
				toolBar.setOrientation(JToolBar.VERTICAL);
			mapObjects.put(toolBar.getName(), toolBar);
			mapLayout.put(toolBar.getName(), position);
		} else if (localName.equals("separator")) {
			if (toolBar != null) {
				if (toolBar.getOrientation() == JToolBar.HORIZONTAL)
					toolBar.add(JPatchSeparator.createHorizontalSeparator());
				else
					toolBar.add(JPatchSeparator.createVerticalSeparator());
			} else if (getMenu() != null) {
				JComponent menu = getMenu();
				if (menu instanceof JMenu)
					((JMenu) menu).addSeparator();
				else if (menu instanceof JPopupMenu)
					((JPopupMenu) menu).addSeparator();
			}
		} else if (localName.equals("button")) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if (attributes.getLocalName(i).equals("command")) {
					AbstractButton button = Actions.getInstance().getButton(attributes.getValue(i));
					toolBar.add(button);
					if (button instanceof JPatchMenuButton) {
						JPopupMenu popupMenu = new JPopupMenu();
						listMenu.add(popupMenu);
						((JPatchMenuButton) button).setPopupMenu(popupMenu);
					}
				}
			}
		} else if (localName.equals("menu")) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if (attributes.getLocalName(i).equals("name")) {
					if (getMenu() != null) {
						JMenu menu = new JPatchMenu(attributes.getValue(i));
//						System.out.println(((JMenu) getMenu()).getText() + " " + menu.getText());
						if (!(getMenu() instanceof JMenuBar))
							menu.setIcon(emptyIcon);
						getMenu().add(menu);
						listMenu.add(menu);
						if (attributes.getValue(i).toLowerCase().equals("view"))
							viewMenu = menu;
					} else {
						if (attributes.getValue(i).equals("menubar"))
							listMenu.add(menuBar);
						else if (attributes.getValue(i).equals("viewport popup"))
							listMenu.add(popupMenu);
						else
							throw new IllegalArgumentException("invalid root menu");
					}
				}
				if (attributes.getLocalName(i).equals("mnemonic")) {
					((JMenu) getMenu()).setMnemonic(attributes.getValue(i).charAt(0));
				}
				if (attributes.getLocalName(i).equals("icon")) {
					((JMenu) getMenu()).setIcon(new ImageIcon(ClassLoader.getSystemResource(attributes.getValue(i))));
				}
			}							
		} else if (localName.equals("item")) {
//			System.out.println(getMenu() + " item " + attributes.getValue("command"));
			for (int i = 0; i < attributes.getLength(); i++) {
				if (attributes.getLocalName(i).equals("command")) {
					getMenu().add(Actions.getInstance().getMenuItem(attributes.getValue(i)));
				}
			}
		} else if (localName.equals("shortcut")) {
			String key = null;
			String command = null;
			for (int i = 0; i < attributes.getLength(); i++) {
				if (attributes.getLocalName(i).equals("key"))
					key = attributes.getValue(i);
				else if (attributes.getLocalName(i).equals("command"))
					command = attributes.getValue(i);
			}
//			Actions.getInstance().getInstance().setKeyBinding(key, command);
		}
	}
	
	public void endElement(String namespaceURI, String localName, String qName) {
//		System.out.println("</" + localName + ">");
		if (localName.equals("menu")) {
			listMenu.remove((listMenu.size() - 1));
		} else if (localName.equals("button")) {
			if (listMenu.size() > 0)
				listMenu.remove((listMenu.size() - 1));
		}else if (localName.equals("toolbar")) {
			toolBar = null;
		}
	}
	
	public void characters(char[] ch, int start, int length) {
//		sbChars.append(ch, start, length);
	}
	
	private JComponent getMenu() {
		if (listMenu.size() > 0)
			return (JComponent) listMenu.get(listMenu.size() - 1);
		return null;
	}
}
