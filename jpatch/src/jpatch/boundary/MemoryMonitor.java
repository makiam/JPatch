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
package jpatch.boundary;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;
import javax.swing.border.*;


/**
 * A simple JLable that displays the maximum amount of memory the VM will attempt to use,
 * the total amount of memory in the VM and the amount of memory currently used by JPatch.
 * The values are updated every 500 milliseconds.
 * @author sascha
 */
@SuppressWarnings("serial")
public class MemoryMonitor extends JLabel {
	private static final int DELAY = 500;
	
	public MemoryMonitor() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				updateText();
			}
		};
		setOpaque(true);
		setToolTipText("Max/total/used VM memory in MB");
		setBorder(new LineBorder(Color.GRAY));
		setBackground(new Color(0xcccccc));
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(task, DELAY, DELAY);
		updateText();
	}
	
	private void updateText() {
		Runtime r = Runtime.getRuntime();
		long maximum = r.maxMemory();
		long total = r.totalMemory();
		long free = r.freeMemory();
		long used = total - free;
		float max = (float) ((maximum * 10) >> 20) / 10;
		float tot = (float) ((total * 10) >> 20) / 10;
		float use = (float) ((used * 10) >> 20) / 10;
		String text = " " + max + "/" + tot + "/" + use + " ";
		if (!text.equals(getText()))
			setText(" " + max + "/" + tot + "/" + use + " ");
	}
}
