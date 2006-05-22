/*
 * $Id$
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
package jpatch.boundary.action;

import javax.swing.*;

/**
 * Defines the keys for additional action properties, which are used by
 * the classes defined in jpatch.boundary.ui.*;
 * @see jpatch.boundary.ui.JPatchButton
 * @see jpatch.boundary.ui.JPatchTogglePatchButton
 * @see jpatch.boundary.ui.JPatchLockingTogglePatchButton
 * @see jpatch.boundary.ui.JPatchMenuButton
 * @see jpatch.boundary.ui.JPatchMenuItem
 * @see jpatch.boundary.ui.JPatchRadioButtonMenuItem
 * @see jpatch.boundary.ui.JPatchCheckBoxMenuItem
 * 		
 * @author sascha
 */
public interface JPatchAction extends Action {
	/** Key for the text displayed in MenuItems */
	public static final String MENU_TEXT = "MenuText";
	
	/** Key for the text displayed on Buttons */
	public static final String BUTTON_TEXT = "ButtonText";
	
	/** Key for the text displayed in MenuItem tooltips */
	public static final String MENU_TOOLTIP = "MenuToolTip";
	
	/** Key for the text displayed in Button tooltips */
	public static final String BUTTON_TOOLTIP = "ButtonToolTip";
	
	/** Key for the string-representation of the KeyStroke that triggers the action */
	public static final String ACCELERATOR = "Accelerator";
	
	/** Key for the string which is used as mnemonic in Menus */
	public static final String MNEMONIC = "Mnemonic";
	
	/** Key for the Icon Resource */
	public static final String ICON = "Icon";
	
	/** Key for the SelectedIcon Resource */
	public static final String SELECTED_ICON = "SelectedIcon";
	
	/** Key for the RolloverIcon Resource */
	public static final String ROLLOVER_ICON = "RolloverIcon";
	
	/** Key for the DisabledIcon Resource */
	public static final String DISABLED_ICON = "DisabledIcon";
	
	/** Key for the RolloverSelectedIcon Resource */
	public static final String ROLLOVER_SELECTED_ICON = "RolloverSelectedIcon";
	
	/** Key for the DisabledSelectedIcon Resource */
	public static final String DISABLED_SELECTED_ICON = "DisabledSelectedIcon";
	
	/** Key for the LockedIcon Resource */
	public static final String LOCKED_ICON = "LockedIcon";
	
	/** Key for the RolloverLockedIcon Resource */
	public static final String ROLLOVER_LOCKED_ICON = "RolloverLockedIcon";
	
	/** Key for the DisabledLockedIcon Resource */
	public static final String DISABLED_LOCKED_ICON = "DisabledLockedIcon";
}
