/*
 * $Id$
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.auxilary;

import java.util.SortedSet;
import java.util.TreeSet;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;;

/**
 * This class provides some static FileFilters.
 * @author sascha
 */
public class FileFilters {
	private static final SortedSet<String> IMAGE_EXTENSIONS = new TreeSet<String>();
	
	static {
		for (String formatName : ImageIO.getReaderFormatNames())
			IMAGE_EXTENSIONS.add(formatName.toLowerCase());
	}
	
	/**
	 * FileFilter for JPatch (*.jpt) files
	 */
	public static final FileFilter JPATCH = new FileFilter() {
		@Override
		public String getDescription() {
			return "JPatch files (*.jpt)";
		}
		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			return getExtension(f).equals("jpt");
		}		
	};
	
	/**
	 * FileFilter for Animation:Master (*.mdl) files
	 */
	public static final FileFilter AM_MODELS = new FileFilter() {
		@Override
		public String getDescription() {
			return "Animation:Master models (*.mdl)";
		}
		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			return getExtension(f).equals("mdl");
		}		
	};
	
	/**
	 * FileFilter for RenderMan (*.rib) files
	 */
	public static final FileFilter RIB = new FileFilter() {
		@Override
		public String getDescription() {
			return "RenderMan files (*.rib)";
		}
		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			return getExtension(f).equals("rib");
		}		
	};
	
	/**
	 * FileFilter for sPatch (*.spt) files
	 */
	public static final FileFilter SPATCH = new FileFilter() {
		@Override
		public String getDescription() {
			return "sPatch (*.spt)";
		}
		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			return getExtension(f).equals("spt");
		}		
	};
	
	/**
	 * FileFilter for POV-Ray (*.pov, *.inc) files
	 */
	public static final FileFilter POVRAY = new FileFilter() {
		@Override
		public String getDescription() {
			return "POV-Ray files (*.pov, *.inc)";
		}
		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			String ext = getExtension(f);
			return ext.equals("pov") || ext.equals("inc");
		}		
	};
	
	/**
	 * FileFilter for Alias|Wavefront (*.obj) files
	 */
	public static final FileFilter OBJ = new FileFilter() {
		@Override
		public String getDescription() {
			return "Alias|Wavefront (*.obj)";
		}
		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			String ext = getExtension(f);
			return ext.equals("obj");
		}		
	};
	
	/**
	 * FileFilter for image files (all formats supported by javax.imageIO.ImageIO)
	 */
	public static final FileFilter IMAGES = new FileFilter() {
		@Override
		public String getDescription() {
			StringBuilder sb = new StringBuilder("Image files (");
			for (String ext : IMAGE_EXTENSIONS)
				sb.append("*.").append(ext).append(", ");
			sb.setLength(sb.length() - 2);
			sb.append(")");
			return sb.toString();
		}
		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			return IMAGE_EXTENSIONS.contains(getExtension(f));
		}		
	};
	
	/**
	 * returns the extension (suffix) of a given file
	 */
	public static String getExtension(File f) {
		String filename = f.getPath();
		int i = filename.lastIndexOf('.');
		if (i > 0 &&  i < filename.length() - 1) {
			return filename.substring(i+1).toLowerCase();
		}
		return "";
	}
}
