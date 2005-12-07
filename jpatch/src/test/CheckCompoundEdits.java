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

public class CheckCompoundEdits {

	private File dir = new File("src/jpatch/control/edit");
	
	private FileFilter filter = new FileFilter() {
		public boolean accept(File pathName) {
			String name = pathName.getName();
			return name.startsWith("Compound") && name.endsWith(".java");
		}
	};
	private HashMap<String, HashSet<String>> classMap = new HashMap<String, HashSet<String>>();
	private HashSet<String> visitedClasses = new HashSet<String>();
	private HashSet<String> errors = new HashSet<String>();
	
	public static void main(String[] args) throws Exception {
		new CheckCompoundEdits();
	}
	
	public CheckCompoundEdits() throws Exception {
		File[] files = dir.listFiles(filter);
		boolean lineComment = false;
		boolean blockComment = false;
		boolean block = false;
		StringBuilder sb;
		Pattern pattern = Pattern.compile("addEdit\\s*\\(\\s*new (\\w+)");
		for (File file:files) {
			String className = file.getName().substring(0, file.getName().length() - 5);
			HashSet<String> uses = new HashSet<String>();
			classMap.put(className, uses);
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
				uses.add(matcher.group(1));
			}
		}
		for (String className:classMap.keySet()) {
			visitedClasses.clear();
			dump(className, "");
			System.out.println();
		}
		for (String error:errors) {
			System.out.println("Recursion in " + error);
		}
	}
	
	public void dump(String className, String prefix) {
		System.out.println(prefix + className);
		if (visitedClasses.contains(className)) {
			System.out.println("RECURSION ERROR!!!");
			errors.add(className);
			return;
		}
		visitedClasses.add(className);
		HashSet<String> uses = classMap.get(className);
		for (String usedClass:uses) {
			if (classMap.containsKey(usedClass))
				dump(usedClass, prefix + "    ");
			else
				System.out.println(prefix + "    " + usedClass);
		}
		visitedClasses.remove(className);
	}
}
