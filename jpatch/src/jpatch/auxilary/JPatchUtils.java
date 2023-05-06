package jpatch.auxilary;

import java.io.File;
import jpatch.boundary.*;
import jpatch.boundary.ui.JPatchDialog;

public final class JPatchUtils {
	
	public static boolean arrayContains(Object[] array, Object element) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == element) return true;
		}
		return false;
	}
	
//	public static String getFileExtension(String filename) {
//		int i = filename.lastIndexOf('.');
//		if (i > 0 &&  i < filename.length() - 1) {
//			return filename.substring(i+1).toLowerCase();
//		}
//		return "";
//	}
	
	public static int showSaveDialog() {
		//return JOptionPane.showConfirmDialog(MainFrame.getInstance(),"The model has been changed, and all changes will be lost. Do you like to save?", "Save changes?", JOptionPane.YES_NO_CANCEL_OPTION, );
//		return JOptionPane.showOptionDialog(MainFrame.getInstance(), "Do you want to save changes?", "Save changes?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] { "Save", "Don't save", "Cancel" }, "Save");
		File file = null;
		if (MainFrame.getInstance().getModel() != null) {
			file = MainFrame.getInstance().getModel().getFile();
		} else {
			file = MainFrame.getInstance().getAnimation().getFile();
		}
		String filename = "New File";
		if (file != null)
			filename = file.getName();
		
		return JPatchDialog.showDialog(MainFrame.getInstance(), filename, JPatchDialog.WARNING, "<b>Do you want to save changes to this file before closing it?</b><p>If you don't save, your changes will be lost.", new String[] { "Don't Save", null, "Cancel", "Save" }, 2);
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
