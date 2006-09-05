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

/*
 * @author sascha
 *
 */
public class WorkspaceManager {
	private File workspaceDir;
	private File lock;
	
	public WorkspaceManager(File workspaceDir) throws IOException {
		this.workspaceDir = workspaceDir;
		lock = new File(workspaceDir, "jpatch.lock");
		if (!workspaceDir.exists()) {
			try {
				if (!workspaceDir.mkdirs()) {
					throw new IOException("Can't reate workspace directory \"" + workspaceDir.getCanonicalPath() + "\".");
				}
			} catch (SecurityException e) {
				throw new IOException("Can't create workspace directory \"" + workspaceDir.getCanonicalPath() + "\": " + e.getMessage());
			}
		}
		
		if (!lock.exists()) {
			try {
				lock.createNewFile();
			} catch (IOException e) {
				throw new IOException("Can't create lock in workspace directory \"" + workspaceDir.getCanonicalPath() + "\": " + e.getMessage());
			}
		}

		if (new FileOutputStream(lock).getChannel().tryLock() == null) {
			throw new IOException("Can't acquire exclusive lock on workspace \"" + workspaceDir.getCanonicalPath() + "\".");
		}
	}
	
	public File getDirectory() {
		return workspaceDir;
	}
}
