package jpatch.auxilary;

import java.io.*;
import java.util.*;
import java.security.*;

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
	
	public static final Os detectedOs = detectOs();
	public static final Arch detectedArch = detectArch();
	
	public static void detectPlatform() {
		String osDir = null, archDir = null, prefix = null, suffix = null;
		switch(detectedOs) {
		case WINDOWS:
			osDir = "windows/";
			prefix = "";
			suffix = ".dll";
			break;
		case LINUX:
			osDir = "linux/";
			prefix = "lib";
			suffix = ".so";
			break;
		case MAC_OS_X:
			osDir = "osx/";
			prefix = "lib";
			suffix = ".jnilib";
			break;
		}
		switch (detectedArch) {
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
		if (detectedOs == Os.WINDOWS) {
			archDir = "x86/";
		}
		
		if (osDir != null && archDir != null && suffix != null) {
			dir = NATIVE_LIBS_DIR + osDir + archDir;
			List<String> libList = new ArrayList<String>();
			libList.add(prefix + "jogl" + suffix);
			libList.add(prefix + "jogl_awt" + suffix);
			libList.add(prefix + "jogl_cg" + suffix);
			if (detectedOs == Os.LINUX) {
				libList.add(prefix + "jogl_drihack" + suffix);
			}
			nativeLibs = libList.toArray(new String[libList.size()]);
		}
	}
	
	private static String getNativeLibsDir(Os os, Arch arch) {
		String osDir = null, archDir = null, prefix = null, suffix = null;
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
		if (detectedOs == Os.WINDOWS) {
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
		if (osName.startsWith("")) {
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
		} if (osArch.equals("sparc")) {
			return Arch.SPARC;
		} else {
			return Arch.UNKNOWN_OTHER;
		}
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		String algorithm = "SHA-256";
		Os os = detectOs();
		Arch arch = detectArch();
		
		String dir = getNativeLibsDir(os, arch);
		String[] libs = getLibraryNames(os);
		String javaLibraryPath = System.getProperty("java.library.path");
		String[] folders = javaLibraryPath.split(System.getProperty("path.separator"));

		for (String lib : libs) {
			System.out.println(dir + ":" + lib);
			byte[] digest = digest(ClassLoader.getSystemResourceAsStream(dir + lib), algorithm);
			for (String folder : folders) {
				System.out.println(folder);
				File libFile = new File(folder, lib);
				if (libFile.exists()) {
					byte[] fileDigest = digest(new FileInputStream(libFile), algorithm);
					System.out.println(compareDigest(digest, fileDigest));
				}
			}
		}
	}
	
	private static boolean compareDigest(byte[] digest1, byte[] digest2) {
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
	private static String digestToString(byte[] digest) {
		StringBuilder sb = new StringBuilder("{");
		for (int i = 0; i < digest.length - 1; i++) {
			sb.append(digest[i]);
			sb.append(", ");
		}
		sb.append(digest[digest.length - 1]);
		sb.append("}");
		return sb.toString();
	}
}
