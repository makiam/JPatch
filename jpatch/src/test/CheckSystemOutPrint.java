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
package test;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class CheckSystemOutPrint {

	private File dir = new File("src/jpatch");
	private List<File> files = new ArrayList<File>();
	private FileFilter dirFileFilter = new FileFilter() {
		public boolean accept(File pathName) {
			return pathName.isDirectory();
		}
	};
	private FileFilter javaFileFilter = new FileFilter() {
		public boolean accept(File pathName) {
			return pathName.getPath().endsWith(".java");
		}
	};
	
	public static void main(String[] args) throws Exception {
		new CheckSystemOutPrint();
	}
	
	public CheckSystemOutPrint() throws Exception {
		descend(dir);
		Pattern pattern = Pattern.compile("(System\\.(out|err)\\.print.*)");
		boolean lineComment = false;
		boolean blockComment = false;
		boolean block = false;
		StringBuilder sb;
		for (File file:files) {
			String className = file.getName().substring(0, file.getName().length() - 5);
			HashSet<String> uses = new HashSet<String>();
			FileReader r = new FileReader(file);
			sb = new StringBuilder();
			char c0;
			char c1;
			int i = r.read();
			if (i == -1)
				continue;
			c0 = (char) i;
			
			while (i != -1) {
				i = r.read();
				if (i == -1) {
					break;
				}
				c1 = (char) i;
				if (lineComment) {
					if (c1 == '\n') {
						lineComment = false;
						c0 = c1;
						block = true;
						continue;
					}
				} else if (blockComment) {
					if (c0 == '*' && c1 == '/') {
						blockComment = false;
						c0 = c1;
						block = true;
						continue;
					}
				} else {
					if (c0 == '/') {
						if (c1 == '/') {
							lineComment = true;
						} else if (c1 == '*') {
							blockComment = true;
						} else {
							if (block)
								block = false;
							else
								sb.append(c0);
						}
					} else {
						if (block)
							block = false;
						else
							sb.append(c0);
					}
				}
				c0 = c1;
			}
			if (!block)
				sb.append(c0);
			Matcher matcher = pattern.matcher(sb);
			while (matcher.find()) {
				System.out.println(file + ": " + matcher.group(1));
			}
		}
	}
	
	
	void descend(File dir) {
		File[] subDirs = dir.listFiles(dirFileFilter);
		File[] javaFiles = dir.listFiles(javaFileFilter);
		for (int i = 0; i < javaFiles.length; i++) {
			files.add(javaFiles[i]);
		}
		for (int i = 0; i < subDirs.length; descend(subDirs[i++]));
	}
}
