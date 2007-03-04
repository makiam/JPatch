package jpatch.auxilary;

import java.io.*;
import java.lang.reflect.Field;

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
	
	public static final Os detectedOs = detectOs();
	public static final Arch detectedArch = detectArch();
	
	public static void extractNativeLibraries() throws NoSuchFieldException, IllegalAccessException, IOException {
		
		/*
		 * Extremely ugly hack to add the temp-dir to the library path.
		 */
		Class classloaderClass = ClassLoader.class;
		Field field = classloaderClass.getDeclaredField("sys_paths");
		field.setAccessible(true);
		field.set(classloaderClass, null);
		String libraryPath = System.getProperty("java.library.path");
		libraryPath = libraryPath + File.pathSeparator + System.getProperty("java.io.tmpdir");
		System.setProperty("java.library.path", libraryPath);
		
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
			String dir = NATIVE_LIBS_DIR + osDir + archDir;
			extractLib(dir, prefix, "jogl", suffix);
			extractLib(dir, prefix, "jogl_awt", suffix);
			extractLib(dir, prefix, "jogl_cg", suffix);
			if (detectedOs == Os.LINUX) {
				extractLib(dir, prefix, "jogl_drihack", suffix);
			}
			
		}
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
}
