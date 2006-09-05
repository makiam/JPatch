package jpatch.entity;

import java.io.File;

public class JPatchDirectory extends AbstractJPatchObject {
	private File file;
	private Project project;
	
	public JPatchDirectory(File directory) {
		file = directory;
		if (!file.isDirectory()) {
			throw new IllegalArgumentException(file + " is not a directory");
		}
	}
	
	public String getName() {
		return file.getName();
	}

	public void setParent(JPatchObject parent) {
		project = (Project) parent;
	}
	
	
}
