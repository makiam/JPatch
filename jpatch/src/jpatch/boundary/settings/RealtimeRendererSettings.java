/*
 * $Id: RealtimeRendererSettings.java,v 1.3 2006/02/01 21:11:28 sascha_l Exp $
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

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class RealtimeRendererSettings extends AbstractSettings {
	Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/renderer.png"));
	public Icon getIcon() {
		return icon;
	}
	public static enum RealtimeRenderer { JAVA_2D, SOFTWARE_ZBUFFER, OPEN_GL };
	public static enum Backface { RENDER, HIDE, HIGHLIGHT };
	public static enum LightingMode { OFF, SIMPLE, HEADLIGHT, THREE_POINT };
	
	public RealtimeRendererSettings.RealtimeRenderer realtimeRenderer = RealtimeRenderer.SOFTWARE_ZBUFFER;
	public int realtimeRenererQuality = 2;
	public RealtimeRendererSettings.LightingMode lightingMode = LightingMode.THREE_POINT;
	public boolean lightFollowsCamera = false;
	public RealtimeRendererSettings.Backface backfacingPatches = Backface.RENDER;
	public boolean wireframeFogEffect = true;
}