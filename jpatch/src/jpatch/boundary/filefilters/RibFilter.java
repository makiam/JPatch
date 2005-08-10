package jpatch.boundary.filefilters;

import java.io.*;
import jpatch.auxilary.*;

public class RibFilter extends javax.swing.filechooser.FileFilter {
	
	public String getDescription() {
		return "RenderMan files (*.rib)";
	}
	
	public boolean accept (File file) {
		if (file.isDirectory()) return true;
		String extension = JPatchUtils.getFileExtension(file.getPath());
		return (extension.equals("rib"));
	}
}
