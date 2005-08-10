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
		return JOptionPane.showConfirmDialog(MainFrame.getInstance(),"The model has been changed, and all changes will be lost. Do you like to save?", "Save changes?", JOptionPane.YES_NO_CANCEL_OPTION);
	}
}
