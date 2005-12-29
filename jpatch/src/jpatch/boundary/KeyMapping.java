package jpatch.boundary;

import java.awt.event.*;

import jpatch.boundary.settings.Settings;

public class KeyMapping {

	private static Settings settings = Settings.getInstance();
	
	public static JPatchKey getKey(String function) {
//		for (int i = 0; i < settings.astrKeyMap.length; i++) {
//			String[] m = settings.astrKeyMap[i].split("\t+");
//			if (m[0].toLowerCase().equals(function.toLowerCase())) {
//				return getJPatchKey(m[2]);
//			}
//		}
		return null;
	}

	public static String getDescription(String function) {
//		for (int i = 0; i < settings.astrKeyMap.length; i++) {
//			String[] m = settings.astrKeyMap[i].split("\t+");
//			if (m[0].toLowerCase().equals(function.toLowerCase())) {
//				return (m[1] + "  [" + m[2] + "]");
//			}
//		}
		return null;
	}

	public static String getKeyString(String function) {
//		for (int i = 0; i < settings.astrKeyMap.length; i++) {
//			String[] m = settings.astrKeyMap[i].split("\t+");
//			if (m[0].toLowerCase().equals(function.toLowerCase())) {
//				return ("  [" + m[2] + "]");
//			}
//		}
		return null;
	}
	
	private static JPatchKey getJPatchKey(String key) {
		String[] s = key.split("-");
		String k;
		int vk = 0;
		int mask = 0;
		if (s.length == 2) {
			k = s[1].toUpperCase();
			if (s[0].equals("SHIFT")) mask = InputEvent.SHIFT_DOWN_MASK;
			else if (s[0].equals("CTRL")) mask = InputEvent.CTRL_DOWN_MASK;
			else if (s[0].equals("ALT")) mask = InputEvent.ALT_DOWN_MASK;
		} else {
			k = key.toUpperCase();
			mask = 0;
		}
		if (k.equals("A")) vk = KeyEvent.VK_A;
		if (k.equals("B")) vk = KeyEvent.VK_B;
		if (k.equals("C")) vk = KeyEvent.VK_C;
		if (k.equals("D")) vk = KeyEvent.VK_D;
		if (k.equals("E")) vk = KeyEvent.VK_E;
		if (k.equals("F")) vk = KeyEvent.VK_F;
		if (k.equals("G")) vk = KeyEvent.VK_G;
		if (k.equals("H")) vk = KeyEvent.VK_H;
		if (k.equals("I")) vk = KeyEvent.VK_I;
		if (k.equals("J")) vk = KeyEvent.VK_J;
		if (k.equals("K")) vk = KeyEvent.VK_K;
		if (k.equals("L")) vk = KeyEvent.VK_L;
		if (k.equals("M")) vk = KeyEvent.VK_M;
		if (k.equals("N")) vk = KeyEvent.VK_N;
		if (k.equals("O")) vk = KeyEvent.VK_O;
		if (k.equals("P")) vk = KeyEvent.VK_P;
		if (k.equals("Q")) vk = KeyEvent.VK_Q;
		if (k.equals("R")) vk = KeyEvent.VK_R;
		if (k.equals("S")) vk = KeyEvent.VK_S;
		if (k.equals("T")) vk = KeyEvent.VK_T;
		if (k.equals("U")) vk = KeyEvent.VK_U;
		if (k.equals("V")) vk = KeyEvent.VK_V;
		if (k.equals("W")) vk = KeyEvent.VK_W;
		if (k.equals("X")) vk = KeyEvent.VK_X;
		if (k.equals("Y")) vk = KeyEvent.VK_Y;
		if (k.equals("Z")) vk = KeyEvent.VK_Z;
		if (k.equals("0")) vk = KeyEvent.VK_0;
		if (k.equals("1")) vk = KeyEvent.VK_1;
		if (k.equals("2")) vk = KeyEvent.VK_2;
		if (k.equals("3")) vk = KeyEvent.VK_3;
		if (k.equals("4")) vk = KeyEvent.VK_4;
		if (k.equals("5")) vk = KeyEvent.VK_5;
		if (k.equals("6")) vk = KeyEvent.VK_6;
		if (k.equals("7")) vk = KeyEvent.VK_7;
		if (k.equals("8")) vk = KeyEvent.VK_8;
		if (k.equals("F1")) vk = KeyEvent.VK_F1;
		if (k.equals("F2")) vk = KeyEvent.VK_F2;
		if (k.equals("F3")) vk = KeyEvent.VK_F3;
		if (k.equals("F4")) vk = KeyEvent.VK_F4;
		if (k.equals("F5")) vk = KeyEvent.VK_F5;
		if (k.equals("F6")) vk = KeyEvent.VK_F6;
		if (k.equals("F7")) vk = KeyEvent.VK_F7;
		if (k.equals("F8")) vk = KeyEvent.VK_F8;
		if (k.equals("F9")) vk = KeyEvent.VK_F9;
		if (k.equals("F10")) vk = KeyEvent.VK_F10;
		if (k.equals("F11")) vk = KeyEvent.VK_F11;
		if (k.equals("F12")) vk = KeyEvent.VK_F12;
		if (k.equals("ESC")) vk = KeyEvent.VK_ESCAPE;
		if (k.equals("DEL")) vk = KeyEvent.VK_DELETE;
		if (k.equals("ENTER")) vk = KeyEvent.VK_ENTER;
		if (k.equals("BACKSPACE")) vk = KeyEvent.VK_BACK_SPACE;
		if (k.equals("TAB")) vk = KeyEvent.VK_TAB;
		if (k.equals("PLUS")) vk = KeyEvent.VK_PLUS;
		if (k.equals("NUM+")) vk = KeyEvent.VK_ADD;
		if (k.equals("INS")) vk = KeyEvent.VK_INSERT;
		if (k.equals("NUM0")) vk = KeyEvent.VK_NUMPAD0;
		if (k.equals("NUM1")) vk = KeyEvent.VK_NUMPAD1;
		if (k.equals("NUM2")) vk = KeyEvent.VK_NUMPAD2;
		if (k.equals("NUM3")) vk = KeyEvent.VK_NUMPAD3;
		if (k.equals("NUM4")) vk = KeyEvent.VK_NUMPAD4;
		if (k.equals("NUM5")) vk = KeyEvent.VK_NUMPAD5;
		if (k.equals("NUM6")) vk = KeyEvent.VK_NUMPAD6;
		if (k.equals("NUM7")) vk = KeyEvent.VK_NUMPAD7;
		if (k.equals("NUM8")) vk = KeyEvent.VK_NUMPAD8;
		if (k.equals("NUM9")) vk = KeyEvent.VK_NUMPAD9;
		return new JPatchKey(vk,mask);
	}
}
