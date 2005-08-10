package jpatch.auxilary;

import java.io.*;

public class JarFileFilter implements FileFilter
{
	protected String getExtension(File f)
	{
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1)
		{
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
	
	public boolean accept(File file)
	{
		if (file.isDirectory()) return true;
		String extension = getExtension(file);
		if (extension != null)
		{
			if (extension.equals("jar")) return true;
		}
	 	return false;
	}
	
	public String getDescription()
	{
    		return "JAR archives";
	}
}
