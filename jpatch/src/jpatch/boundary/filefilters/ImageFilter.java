package jpatch.boundary.filefilters;

import java.util.*;
import java.io.*;
import javax.imageio.*;
import jpatch.auxilary.*;

public class ImageFilter extends javax.swing.filechooser.FileFilter {
	
	public String getDescription() {
		String[] format = ImageIO.getReaderFormatNames();
		String description = "Images files (";
		Set setFormats = new TreeSet();
		for (int i = 0; i < format.length;i++) {
			setFormats.add(format[i].toLowerCase());
		}
		for (Iterator it = setFormats.iterator(); it.hasNext(); ) {
			String f = (String) it.next();
			description += "*." + f;
			if (it.hasNext()) description += ", ";
		}
		
		description += ")";
		return description;
	}
	
	public boolean accept (File file) {
		if (file.isDirectory()) return true;
		String extension = JPatchUtils.getFileExtension(file.getPath());
		String[] format = ImageIO.getReaderFormatNames();
		for (int i = 0; i < format.length;i++) {
			if (extension.equals(format[i].toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
