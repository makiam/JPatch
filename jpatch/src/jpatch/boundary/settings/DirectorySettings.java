/*
 * $Id: DirectorySettings.java,v 1.3 2006/05/22 10:46:20 sascha_l Exp $
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
package jpatch.boundary.settings;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class DirectorySettings extends AbstractSettings {
	Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/directories.png"));
	public Icon getIcon() {
		return icon;
	}
	public boolean rememberLastDirectories = true;
	public File jpatchFiles = new File(System.getProperty("user.dir"));
	public File spatchFiles = new File(System.getProperty("user.dir"));
	public File animationmasterFiles = new File(System.getProperty("user.dir"));
	public File povrayFiles = new File(System.getProperty("user.dir"));
	public File rendermanFiles = new File(System.getProperty("user.dir"));
	public File objFiles = new File(System.getProperty("user.dir"));
	public File rotoscopeFiles = new File(System.getProperty("user.dir"));
}