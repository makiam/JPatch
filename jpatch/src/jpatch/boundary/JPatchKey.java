package jpatch.boundary;

import java.awt.event.*;

public class JPatchKey {
	private final int iKeyCode;
	private final int iOnMask;
	private final int iOffMask;
	
	public JPatchKey(int keyCode) {
		this(keyCode,0,0xFFFFFFFF);
	}
	
	public JPatchKey(int keyCode, int mask) {
		this(keyCode,mask,0xFFFFFFFF ^ mask);
	}
	
	public JPatchKey(int keyCode, int onMask, int offMask) {
		iKeyCode = keyCode;
		iOnMask = onMask;
		iOffMask = offMask;
	}
	
	public boolean test(KeyEvent keyEvent) {
		return (keyEvent.getKeyCode() == iKeyCode && (keyEvent.getModifiersEx() & (iOnMask | iOffMask)) == iOnMask);
	}
	
	public String toString() {
		return "KEY " + iKeyCode + " " + iOnMask + " " + iOffMask + " " + (char) iKeyCode;
	}
	
	public int hashCode() {
		return iKeyCode * 17 + iOnMask * 39 + iOffMask * 59;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof JPatchKey)) return false;
		JPatchKey key = (JPatchKey) o;
		return iKeyCode == key.iKeyCode && iOnMask == key.iOnMask && iOffMask == key.iOffMask;
	}
}

