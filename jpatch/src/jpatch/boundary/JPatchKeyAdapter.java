package jpatch.boundary;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;

public class JPatchKeyAdapter extends KeyAdapter {
	private ArrayList listKeys = new ArrayList();
	//private Map mapKeys = new HashMap();
	
	public void addKey(JPatchKey key, Object object) {
		if (listKeys.indexOf(key) != -1) throw new IllegalArgumentException(key + " already assigned");
		listKeys.add(key);
		listKeys.add(object);
		//System.out.println("add key " + key + " " + listKeys.size());
		//mapKeys.put(key, object);
		//System.out.println(key);
	}
	
	public void removeAllKeys() {
		//System.out.println("remove all keys");
		listKeys.clear();
		//System.out.println(listKeys.size());
	}
	
	public void removeKey(JPatchKey key) {
		//System.out.print("remove key " + key);
		for (int i = 0, n = listKeys.size(); i < n; i += 2) {
			if (key.equals(listKeys.get(i))) {
				listKeys.remove(i);
				listKeys.remove(i);
				//System.out.println("...ok " + listKeys.size());
				return;
			}
		}
		//System.out.println("...?");
		//int index = listKeys.indexOf(key);
		//if (index >= 0) {
		//	listKeys.remove(index);
		//	listKeys.remove(index);
		//}
		//mapKeys.remove(key);
	}
	
	public void keyPressed(KeyEvent keyEvent) {
		for (int i = 0; i < listKeys.size(); i += 2) {
			JPatchKey key = (JPatchKey)listKeys.get(i);
			if (key.test(keyEvent)) {
				Object object = listKeys.get(i + 1);
				
				//try {
				//	if (Class.forName("java.awt.event.ActionListener").isInstance(object)) {
				//		((ActionListener) object).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,"Key pressed"));
				//	} else if (Class.forName("javax.swing.AbstractButton").isInstance(object)) {
				//		((AbstractButton) object).doClick();
				//	}
				//} catch(ClassNotFoundException exception) {
				//	System.err.println(exception);
				//}
				if (object instanceof ActionListener) {
					((ActionListener) object).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,"Key pressed"));
				} else if (object instanceof AbstractButton) {
					((AbstractButton) object).doClick();
				}
			}
		}
	}
}
