package jpatch.boundary.filefilters;

import java.io.*;
import jpatch.auxilary.*;

public class PovFilter extends javax.swing.filechooser.FileFilter {
	
	public String getDescription() {
		return "POV-Ray files (*.inc, *.pov)";
	}
	
	public boolean accept (File file) {
		if (file.isDirectory()) return true;
		String extension = JPatchUtils.getFileExtension(file.getPath());
		return (extension.equals("pov") || extension.equals("inc"));
	}
}
