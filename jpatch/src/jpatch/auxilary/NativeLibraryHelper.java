package jpatch.auxilary;

import java.awt.Frame;
import java.io.*;
import java.util.*;
import java.security.*;

import javax.swing.JComboBox;
import javax.swing.JFrame;

import jpatch.boundary.ui.JPatchDialog;

import sun.security.action.GetLongAction;

public class NativeLibraryHelper {
	public static String NATIVE_LIBS_DIR = "nativelibs/";
	
	public static enum Os {
		UNKNOWN_OTHER,
		WINDOWS,
		LINUX,
		MAC_OS_X,
		SOLARIS
	}
	
	public static enum Arch {
		UNKNOWN_OTHER,
		X86,
		AMD64,
		PPC,
		SPARC
	}
	
	private static String[] nativeLibs = new String[0];
	private static String dir;
	
//	public static final Os detectedOs = detectOs();
//	public static final Arch detectedArch = detectArch();
	
//	public static void detectPlatform() {
//		String osDir = null, archDir = null, prefix = null, suffix = null;
//		switch(detectedOs) {
//		case WINDOWS:
//			osDir = "windows/";
//			prefix = "";
//			suffix = ".dll";
//			break;
//		case LINUX:
//			osDir = "linux/";
//			prefix = "lib";
//			suffix = ".so";
//			break;
//		case MAC_OS_X:
//			osDir = "osx/";
//			prefix = "lib";
//			suffix = ".jnilib";
//			break;
//		}
//		switch (detectedArch) {
//		case X86:
//			archDir = "x86/";
//			break;
//		case AMD64:
//			archDir = "amd64/";
//			break;
//		case PPC:
//			archDir = "ppc/";
//			break;
//		}
//		
//		/* windows hack */
//		if (detectedOs == Os.WINDOWS) {
//			archDir = "x86/";
//		}
//		
//		if (osDir != null && archDir != null && suffix != null) {
//			dir = NATIVE_LIBS_DIR + osDir + archDir;
//			List<String> libList = new ArrayList<String>();
//			libList.add(prefix + "jogl" + suffix);
//			libList.add(prefix + "jogl_awt" + suffix);
//			libList.add(prefix + "jogl_cg" + suffix);
//			if (detectedOs == Os.LINUX) {
//				libList.add(prefix + "jogl_drihack" + suffix);
//			}
//			nativeLibs = libList.toArray(new String[libList.size()]);
//		}
//	}
	
	private static String getNativeLibsDir(Os os, Arch arch) {
		String osDir = null, archDir = null;
		switch(os) {
		case WINDOWS:
			osDir = "windows/";
			break;
		case LINUX:
			osDir = "linux/";
			break;
		case MAC_OS_X:
			osDir = "osx/";
			break;
		}
		switch (arch) {
		case X86:
			archDir = "x86/";
			break;
		case AMD64:
			archDir = "amd64/";
			break;
		case PPC:
			archDir = "ppc/";
			break;
		}
		
		/* windows hack */
		if (os == Os.WINDOWS) {
			archDir = "x86/";
		}
		
		if (osDir != null && archDir != null) {
			return NATIVE_LIBS_DIR + osDir + archDir;
		}
		
		return null;
	}
	
	
//	String javaLibraryPath = properties.getProperty("java.library.path");
//	String[] folders = javaLibraryPath.split(properties.getProperty("path.separator"));
//	String lib = System.mapLibraryName("jogl");
//	for (int i = 0; i < folders.length; i++) {
//		File file = new File(folders[i], lib);
//		if (file.exists()) {
//			loaded = true;
//			logger.log("found in " + folders[i] + "\n");
//		}
//	}
	
	private static String[] getLibraryNames(Os os) {
		List<String> libList = new ArrayList<String>();
		libList.add(System.mapLibraryName("jogl"));
		libList.add(System.mapLibraryName("jogl_cg"));
		libList.add(System.mapLibraryName("jogl_awt"));
		if (os == Os.LINUX) {
			libList.add(System.mapLibraryName("jogl_drihack"));
		}
		return libList.toArray(new String[libList.size()]);
	}
	
	private static byte[] digest(InputStream in, String algorithm) throws NoSuchAlgorithmException, IOException {
		byte[] buffer = new byte[4096];
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		int bytes = 0;
		while ((bytes = in.read(buffer)) > -1) {
			digest.update(buffer, 0, bytes);
		}
		in.close();
		return digest.digest();
	}
	
	private static void extractLib(String dir, String prefix, String name, String suffix) throws IOException {
		InputStream i = ClassLoader.getSystemResourceAsStream(dir + prefix + name + suffix);
		if (i == null) {
			throw new FileNotFoundException();
		}
		InputStream in = new BufferedInputStream(i);
		File tmp = new File(System.getProperties().getProperty("java.io.tmpdir"), prefix + name + suffix);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp));
		int data;
		while ((data = in.read()) != -1) {
			out.write(data);
		}
		in.close();
		out.close();
		tmp.deleteOnExit();
	}
	
	private static Os detectOs() {
		String osName = System.getProperties().getProperty("os.name");
		if (osName.startsWith("Windows")) {
			return Os.WINDOWS;
		} else if (osName.equals("Linux")) {
			return Os.LINUX;
		} else if (osName.equals("Mac OS X")) {
			return Os.MAC_OS_X;
		} else if (osName.equals("Solaris")) {
			return Os.SOLARIS;
		} else {
			return Os.UNKNOWN_OTHER;
		}
	}
	
	private static Arch detectArch() {
		String osArch = System.getProperties().getProperty("os.arch");
		if (osArch.equals("x86") || osArch.equals("i386") || osArch.equals("i586") || osArch.equals("i686")) {
			return Arch.X86;
		} else if (osArch.equals("ppc") || osArch.equals("PowerPC")) {
			return Arch.PPC;
		} else if (osArch.equals("sparc")) {
			return Arch.SPARC;
		} else if (osArch.equals("amd64")) {
			return Arch.AMD64;
		} else {
			return Arch.UNKNOWN_OTHER;
		}
	}
	
	public boolean checkLibraries(Frame owner) throws NoSuchAlgorithmException, IOException {
		String algorithm = "SHA-1";
		Os os = detectOs();
		Arch arch = detectArch();
		
		String dir = getNativeLibsDir(os, arch);
		String[] libs = getLibraryNames(os);
		String javaLibraryPath = System.getProperty("java.library.path");
		String[] folders = javaLibraryPath.split(System.getProperty("path.separator"));

		/* create digests for bundled libraries */
		byte[][] digests = new byte[libs.length][];
		for (int i = 0; i < libs.length; i++) {
			System.out.println(dir + ":" + libs[i]);
			digests[i] = digest(ClassLoader.getSystemResourceAsStream(dir + libs[i]), algorithm);
			System.out.println(libs[i] + " " + digestToString(digests[i]));
		}
		
		int libraryFolder = -1;
		/* scan folders in library path */
		scanFolders:
		for (int i = 0; i < folders.length; i++) {
			String folder = folders[i];
			for (int j = 0; j < libs.length; j++) {
				String lib = libs[j];
				File libFile = new File(folder, lib);
				if (libFile.exists()) {
					/* set the libraryFolder to the path index where the first library was found */
					System.out.println(lib + " exists in " + folder);
					libraryFolder = i;
					break scanFolders;
				}
			}
		}
		
		if (libraryFolder == -1) {
			// no libraries were found, prompt user to install in any library path folder
			String message = "<b>The required JOGL native libraries have not been found. Please specify the folder " +
					"of the library path where JPatch should try to install the required libraries.</b>" +
					"<p>If the libraies have already been installed or you wish to install them to a different " +
					"folder, please add this folder to the library path and restart JPatch with the " +
					"<code>-Djava.library.path=<i>&lt;path&gt;</i></code> commandline switch.";
			JComboBox folderCombo = new JComboBox(folders);
			int selection = JPatchDialog.showDialog(owner, "JOGL native libraries installation", JPatchDialog.WARNING, message, folderCombo, new String[] { "Install", null, "Quit" }, 1, "400");
			if (selection == 1) {
				/* Quit */
				System.exit(0);
			}
			/* install to specified folder */
			try {
				for (int i = 0; i < libs.length; i++) {
					String lib = libs[i];
					InputStream source = ClassLoader.getSystemResourceAsStream(dir + lib);
					File destination = new File(folders[folderCombo.getSelectedIndex()], lib);
					install(source, destination);
				}
			} catch (IOException e) {
				message = "<b>An error occured during the installation of the JOGL native libraries:</b><p>" +
						"<font color='red'>" + e.getMessage() + "</font>" + 
						"<p>You possibly need administrator (root) privileges to install files in the specified folder.";
				JPatchDialog.showDialog(owner, "JPatch error", JPatchDialog.ERROR, message, null, new String[] { "OK" }, 0, "300");
			}
			return false;
		} else {
			List<Integer> reinstall = new ArrayList<Integer>();
			List<Integer> install = new ArrayList<Integer>();
			String folder = folders[libraryFolder];
			/* scan library files in library folder */
			for (int i = 0; i < libs.length; i++) {
				String lib = libs[i];
				File libFile = new File(folder, lib);
				if (libFile.exists()) {
					if (!compareDigest(digests[i], digest(new FileInputStream(libFile), algorithm))) {
						reinstall.add(i);
					}
				} else {
					install.add(i);
				}
			}
			if (reinstall.size() == 0 && install.size() == 0) {
				// all files found, all hashes ok, we can continue...
				return true;
			} else {
				// some files need to be re-installed (and maybe some need to be installed)...
				String message, buttonText;
				if (install.size() == 0) {
					// all files need to be re-installed
					message = "<b>The required JOGL native libraries have been found in " +
							folder + ", but they appear to be corrupt or of an unsupported version " +
							"and need to be re-installed.</b>" +
							"<p>Overwriting these files with the libraries from JOGL JSR-231 version 1.0.0 " +
							"may break other applications that depend on the installed version. If you do not " +
							"want to overwrite these files, select QUIT and restart JPatch with another " +
							"library-path by setting the " +
							"<code>-Djava.library.path=<i>&lt;path&gt;</i></code> commandline switch." +
							"<p>Choosing RE-INSTALL will overwrite the following files in " + folder + ":<ul>";
					for (int i = 0; i < libs.length; i++) {
						if (reinstall.contains(i)) {
							message += "<li>" + libs[i] + "</li>";
						}
					}
					message += "</ul>";
					buttonText = "Re-Install";
				} else if (reinstall.size() == 0) {
					message = "<b>The required JOGL native libraries have been found in " +
							folder + ", but some files appear to be missing and need to be installed.</b>" +
							"<p>If you do not " +
							"want to install the missing files in this folder, select QUIT and restart JPatch with another " +
							"library-path by setting the " +
							"<code>-Djava.library.path=<i>&lt;path&gt;</i></code> commandline switch.";
					buttonText = "Install";
				} else {
					message = "<b>Some of the required JOGL native libraries have been found in " +
							folder + ", but they appear to be corrupt or of an unsupported version " +
							"and need to be re-installed. Some files are missing and need to be installed.</b>" +
							"<p>Overwriting the already installed files with the libraries from JOGL JSR-231 version 1.0.0 " +
							"may break other applications that depend on the installed files. If you do not " +
							"want to overwrite these files, select QUIT and restart JPatch with another " +
							"library-path by setting the " +
							"<code>-Djava.library.path=<i>&lt;path&gt;</i></code> commandline switch." +
							"<p>Choosing RE-INSTALL will overwrite the following files in " + folder + ":";
					for (int i = 0; i < libs.length; i++) {
						if (reinstall.contains(i)) {
							message += "<li>" + libs[i] + "</li>";
						}
					}
					message += "</ul>";
					buttonText = "Re-Install";
				}
				int selection = JPatchDialog.showDialog(owner, "JOGL native libraries installation", JPatchDialog.WARNING, message, null, new String[] { buttonText, null, "Quit" }, 1, "400");
				if (selection == 1) {
					/* Quit */
					System.exit(0);
				}
				/* re-install */
				try {
					for (int i = 0; i < libs.length; i++) {
						String lib = libs[i];
						InputStream source = ClassLoader.getSystemResourceAsStream(dir + lib);
						File destination = new File(folder, lib);
						install(source, destination);
					}
				} catch (IOException e) {
					message = "<b>An error occured during the installation of the JOGL native libraries:</b><p>" +
							"<font color='red'>" + e.getMessage() + "</font>" + 
							"<p>You possibly need administrator (root) privileges to install files in the specified folder.";
					JPatchDialog.showDialog(owner, "JPatch error", JPatchDialog.ERROR, message, null, new String[] { "OK" }, 0, "300");
				}
				return false;
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setVisible(true);
		
		while (!new NativeLibraryHelper().checkLibraries(frame));
		System.out.println("OK");
		System.exit(0);
	}
	
	private static boolean compareDigest(byte[] digest1, byte[] digest2) {
//		System.out.println("comparing : ");
//		System.out.println(digestToString(digest1));
//		System.out.println(digestToString(digest2));
//		System.out.println();
		
		if (digest1.length != digest2.length) {
			return false;
		}
		for (int i = 0; i < digest1.length; i++) {
			if (digest1[i] != digest2[i]) {
				return false;
			}
		}
		return true;
	}
	
	private void install(InputStream source, File destination) throws IOException {
		OutputStream out = new FileOutputStream(destination);
		byte[] buffer = new byte[4096];
		int bytes = 0;
		while ((bytes = source.read(buffer)) > -1) {
			out.write(buffer, 0, bytes);
		}
		source.close();
		out.close();
	}
	
	private static String digestToString(byte[] digest) {
//		StringBuilder sb = new StringBuilder("{");
//		for (int i = 0; i < digest.length - 1; i++) {
//			sb.append(digest[i]);
//			sb.append(", ");
//		}
//		sb.append(digest[digest.length - 1]);
//		sb.append("}");
//		return sb.toString();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digest.length - 1; i++) {
			sb.append(Integer.toHexString(digest[i] & 0xff));
		}
		return sb.toString();
	}
}
