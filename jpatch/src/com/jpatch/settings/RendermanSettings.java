/*
 * $Id: RendermanSettings.java 420 2007-03-06 17:42:55Z sascha_l $
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
package com.jpatch.settings;

import com.jpatch.afw.settings.*;
import java.io.*;
import javax.swing.*;

public class RendermanSettings extends AbstractSettings {
	Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/renderman.png"));
	public Icon getIcon() {
		return icon;
	}
	public static enum Mode { TRIANGLES, QUADRILATERALS, CATMULL_CLARK_SUBDIVISION_SURFACE, BICUBIC_PATCHES };
	public static enum Interpolation { CONSTANT, SMOOTH };
	public File executable = new File("");
	public String environmentVariables = "";
	public RendermanSettings.Mode outputMode = Mode.TRIANGLES;
	public int subdivisionLevel = 3;
	public int pixelSamplesX = 2;
	public int pixelSamplesY = 2;
	public String pixelFilter = "gaussian";
	public int pixelFilterX = 2;
	public int pixelFilterY = 2;
	public float shadingRate = 1.0f;
	public RendermanSettings.Interpolation shadingInterpolation = Interpolation.SMOOTH;
	public float exposure = 1.0f;
}