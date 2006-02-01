/*
 * $Id: PovraySettings.java,v 1.2 2006/02/01 21:11:28 sascha_l Exp $
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

public class PovraySettings extends AbstractSettings {
	Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/povray.png"));
	public Icon getIcon() {
		return icon;
	}
	public static enum Mode { TRIANGLES, BICUBIC_PATCHES };
	public static enum Antialias { OFF, METHOD_1, METHOD_2 };
	public static enum Version { UNIX, WINDOWS };
	public File executable = new File("");
	public String environmentVariables = "";
	public PovraySettings.Version version = Version.UNIX;
	public PovraySettings.Mode outputMode = Mode.TRIANGLES;
	public int subdivisionLevel = 3;
	public PovraySettings.Antialias antialiasingMethod = Antialias.METHOD_1;
	public int antialiasingLevel = 2;
	public float antialiasingThreshold = 0.3f;
	public float antialiasingJitter = 1.0f;
	public File includeFile = new File("");
}