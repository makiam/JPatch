/*
 * $Id: RendererSettings.java,v 1.4 2006/05/22 10:46:20 sascha_l Exp $
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

import javax.swing.*;
import javax.vecmath.*;

public class RendererSettings extends AbstractSettings {
	Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/export.png"));
	public Icon getIcon() {
		return icon;
	}
	public static enum Renderer { POVRAY, RENDERMAN, INYO };
	
	public RendererSettings.Renderer rendererToUse = Renderer.INYO;
	public int imageWidth = 640;
	public int imageHeight = 480;
	public float aspectWidth = 4;
	public float aspectHeight = 3;
	public Color3f backgroundColor = new Color3f(0.5f, 0.5f, 0.5f);
	public File workingDirectory = new File(System.getProperty("user.dir"));
	public File modelDirectory = new File(System.getProperty("user.dir"));
	public boolean deletePerFrameFilesAfterRendering = true;
	public final PovraySettings povray = new PovraySettings();
	public final RendermanSettings renderman = new RendermanSettings();
	public final InyoSettings inyo = new InyoSettings();
	public final AliasWavefrontSettings aliaswavefront = new AliasWavefrontSettings();
}