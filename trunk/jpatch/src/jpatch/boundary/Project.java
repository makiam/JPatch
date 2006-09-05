/*
 * $Id:$
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
package jpatch.boundary;

import java.io.*;

import jpatch.entity.*;
import java.lang.reflect.*;

/**
 * @author sascha
 *
 */
public class Project extends AbstractJPatchObject {
	private File projectDir;
	private File modelDir;
	private File choreographyDir;
	private File rotoscopeDir;
	private File renderedImagesDir;
	private File shaderDir;
	private File resourceDir;
	
	private Attribute.Name name;
	
	public Project(WorkspaceManager workspaceManager, String projectName) throws IOException {
		name = new Attribute.Name(this);
		name.set(projectName);
		projectDir = new File(workspaceManager.getDirectory(), projectName);
		modelDir = new File(projectDir, "models");
		choreographyDir = new File(projectDir, "choreographies");
		rotoscopeDir = new File(projectDir, "rotoscope_images");
		renderedImagesDir = new File(projectDir, "rendered_images");
		shaderDir = new File(projectDir, "shaders");
		resourceDir = new File(projectDir, "external_resources");
		
		/**
		 * Loop over all File fields and create directories if necessary.
		 */
		for (Field field : this.getClass().getDeclaredFields()) {
			if (File.class.isAssignableFrom(field.getType())) {
				File file;
				try {
					file = (File) field.get(this);
					if (!file.exists()) {
						if (!file.mkdir()) {
							throw new IOException("Can't create directory \"" + file.getCanonicalPath() + "\".");
						}
					}
				} catch (Exception e) {
					throw new IOException("Error opening project \"" + projectName + "\":" + e.getMessage());
				}
			}
		}
	}

	public String getName() {
		return name.get();
	}

	/* (non-Javadoc)
	 * @see jpatch.entity.JPatchObject#setParent(jpatch.entity.JPatchObject)
	 */
	public void setParent(JPatchObject parent) {
		// TODO Auto-generated method stub
		
	}

	
}
