package jpatch.auxilary;

import javax.swing.*;

import jpatch.boundary.*;

public final class JPatchUtils {
	
	public static boolean arrayContains(Object[] array, Object element) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == element) return true;
		}
		return false;
	}
	
	public static String getFileExtension(String filename) {
		int i = filename.lastIndexOf('.');
		if (i > 0 &&  i < filename.length() - 1) {
			return filename.substring(i+1).toLowerCase();
		}
		return "";
	}
	
	public static int showSaveDialog() {
		//return JOptionPane.showConfirmDialog(MainFrame.getInstance(),"The model has been changed, and all changes will be lost. Do you like to save?", "Save changes?", JOptionPane.YES_NO_CANCEL_OPTION, );
		return JOptionPane.showOptionDialog(MainFrame.getInstance(), "Do you want to save changes?", "Save changes?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] { "Save", "Don't save", "Cancel" }, "Save");
	}
	
	public static int[] getJvmVersion() {
		String version = (String) System.getProperty("java.version");
		String[] s = version.split("\\.");
		int[] V = new int[] { Integer.parseInt(s[0]), Integer.parseInt(s[1]) };
		return V;
	}
	
	public static boolean isJvmVersionGreaterOrEqual(int major, int minor) {
		int[] jvm = getJvmVersion();
		if (major > jvm[0])
			return true;
		if (major == jvm[0] && minor >= jvm[1])
			return true;
		else
			return false;
	}
}
