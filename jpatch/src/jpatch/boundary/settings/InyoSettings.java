/*
 * $Id: InyoSettings.java,v 1.1 2005/12/29 16:13:48 sascha_l Exp $
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

public class InyoSettings extends AbstractSettings {
	Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/inyo.png"));
	public Icon getIcon() {
		return icon;
	}
	public static enum Supersampling { ADAPTIVE, EVERYTHING };
	
	public File textureDirectory = new File(System.getProperty("user.dir"));
	public int subdivisionLevel = 3;
	public InyoSettings.Supersampling supersamplingMode = Supersampling.ADAPTIVE;
	public int supersamplingLevel = 3;
	public int recursionDepth = 12;
	public int shadowSamples = 8;
	public boolean transparentShadows = false;
	public boolean caustics = false;
	public boolean oversampleCaustics = false;
	public boolean ambientOcclusion = false;
	public float ambientOcclusionDistance = 1000.0f;
	public int ambientOcclusionSamples = 3;
	public float ambientOcclusionColorbleed = 0.25f;	
}