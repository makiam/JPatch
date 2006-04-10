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
 * @author sascha
 *
 */
public interface JPatchAction extends Action {
	public static final String MENU_TEXT = "MenuText";
	public static final String BUTTON_TEXT = "ButtonText";
	public static final String MENU_TOOLTIP = "MenuToolTip";
	public static final String BUTTON_TOOLTIP = "ButtonToolTip";
	public static final String ACCELERATOR = "Accelerator";
	public static final String MNEMONIC = "Mnemonic";
	public static final String ICON = "Icon";
	public static final String SELECTED_ICON = "SelectedIcon";
	public static final String ROLLOVER_ICON = "RolloverIcon";
	public static final String DISABLED_ICON = "DisabledIcon";
	public static final String ROLLOVER_SELECTED_ICON = "RolloverSelectedIcon";
	public static final String DISABLED_SELECTED_ICON = "DisabledSelectedIcon";
	public static final String LOCKED_ICON = "LockedIcon";
	public static final String ROLLOVER_LOCKED_ICON = "RolloverLockedIcon";
	public static final String DISABLED_LOCKED_ICON = "DisabledLockedIcon";
}
