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
package jpatch.auxilary;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.swing.KeyStroke;


/**
 * This class is used to create String representations of KeyStrokes
 * KeyStroke.toString() works pretty well in Java 1.5, but the same
 * method in Java 1.4 returns garbage, so this uses some methods taken
 * from the 1.5 sources to generate "valid" String representations
 * (i.e. these strings can be used in KeyStroke.getKeyStroke(String s)
 * to recreate the KeyStroke).
 * 
 * @author sascha
 */
public class KeyStrokeUtils {

	private static VKCollection vks;
	
	private KeyStrokeUtils() { }
	
	public static String keyStrokeToString(KeyStroke ks) {
		if (ks == null)
			return null;
		int keyCode = ks.getKeyCode();
		int modifiers = ks.getModifiers();
		char keyChar = ks.getKeyChar();
		boolean onKeyRelease = ks.isOnKeyRelease();
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            return getModifiersText(modifiers) + "typed " + keyChar;
        } else {
            return getModifiersText(modifiers) +
                (onKeyRelease ? "released" : "pressed") + " " +
                getVKText(keyCode);
        }
    }
	
	private static String getModifiersText(int modifiers) {
        StringBuffer buf = new StringBuffer();

        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0 ) {
            buf.append("shift ");
        }
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0 ) {
            buf.append("ctrl ");
        }
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0 ) {
            buf.append("meta ");
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0 ) {
            buf.append("alt ");
        }
        if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0 ) {
            buf.append("altGraph ");
        }
        if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0 ) {
            buf.append("button1 ");
        }
        if ((modifiers & InputEvent.BUTTON2_DOWN_MASK) != 0 ) {
            buf.append("button2 ");
        }
        if ((modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0 ) {
            buf.append("button3 ");
        }

        return buf.toString();
    }
	
	private static String getVKText(int keyCode) { 
        VKCollection vkCollect = getVKCollection();
        Integer key = new Integer(keyCode);
        String name = vkCollect.findName(key);
        if (name != null) {
            return name.substring(3);
        }
        int expected_modifiers = 
            (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                if (fields[i].getModifiers() == expected_modifiers
                    && fields[i].getType() == Integer.TYPE
                    && fields[i].getName().startsWith("VK_")
                    && fields[i].getInt(KeyEvent.class) == keyCode) 
                {
                    name = fields[i].getName();
                    vkCollect.put(name, key);
                    return name.substring(3);
                }
            } catch (IllegalAccessException e) {
                assert(false);
            }
        }
        return "UNKNOWN";
    }
	
	private static VKCollection getVKCollection() {
        if (vks == null) {
            vks = new VKCollection();
        }
        return vks;
    }
	
	private static class VKCollection {
	    Map code2name;
	    Map name2code;

	    public VKCollection() {
	        code2name = new HashMap();
	        name2code = new HashMap();
	    }

	    public synchronized void put(String name, Integer code) {
	        assert((name != null) && (code != null));
	        assert(findName(code) == null);
	        assert(findCode(name) == null);
	        code2name.put(code, name);
	        name2code.put(name, code);
	    }

	    public synchronized Integer findCode(String name) {
	        assert(name != null);
	        return (Integer)name2code.get(name);
	    }

	    public synchronized String findName(Integer code) {
	        assert(code != null);
	        return (String)code2name.get(code);
	    }
	}
}
