package test;

import java.io.*;
import java.util.*;

public class CheckUnusedClasses {

	/**
	 * @param args
	 */
	
	FileFilter dirFileFilter = new FileFilter() {
		public boolean accept(File pathName) {
			return pathName.isDirectory();
		}
	};
	
	FileFilter javaFileFilter = new FileFilter() {
		public boolean accept(File pathName) {
			return pathName.getPath().endsWith(".java");
		}
	};
	
	ArrayList listJavaFiles = new ArrayList();
	HashMap mapMainClasses = new HashMap();
	
	public static void main(String[] args) throws IOException{
		new CheckUnusedClasses(args[0]);
	}
	
	public CheckUnusedClasses(String start) throws IOException {
		descend(new File(start));
		scanClass("Launcher", (File) mapMainClasses.get("Launcher"));
//		for (Iterator it = listJavaFiles.iterator(); it.hasNext(); ) {
//			File file = (File) it.next();
//			scanFile(file);
//		}
		for (Iterator it = mapMainClasses.keySet().iterator(); it.hasNext(); ) {
			String name = (String) it.next();
			System.out.println(name + "\t" + mapMainClasses.get(name));
		}
	}
	
	void descend(File dir) {
		File[] subDirs = dir.listFiles(dirFileFilter);
		File[] javaFiles = dir.listFiles(javaFileFilter);
		for (int i = 0; i < javaFiles.length; i++) {
			listJavaFiles.add(javaFiles[i]);
			mapMainClasses.put(javaFiles[i].getName().replaceAll("\\.java$", ""), javaFiles[i]);
		}
		for (int i = 0; i < subDirs.length; descend(subDirs[i++]));
	}
	
	void scanClass(String className, File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] comment = line.split("//");
			if (comment.length > 0) line = comment[0];
			String[] token = line.split("[\\s|\\(|\\)|\\.]");
			for (int i = 0; i < token.length; i++) {
				if (token[i].matches("^[A-Z].+") && !token[i].equals(token[i].toUpperCase())) {
					//System.out.println(token[i]);
					File f = (File) mapMainClasses.get(token[i]);
					if (f != null) {
						mapMainClasses.remove(token[i]);
						scanClass(token[i], f);
					}
				}
			}
		}
	}
	
}
