package jpatch.boundary.filefilters;

import java.io.*;
import jpatch.auxilary.*;

public class JPatchFilter extends javax.swing.filechooser.FileFilter {
	
	public String getDescription() {
		return "JPatch Files (*.jpt)";
	}
	
	public boolean accept (File file) {
		if (file.isDirectory()) return true;
		String extension = JPatchUtils.getFileExtension(file.getPath());
		return (extension.equals("jpt"));
	}
}
