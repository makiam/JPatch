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
public class MemoryMonitor {
	private static final int DELAY = 1000;
	private static final Insets INSETS = new Insets(0, 1, 0, 1);
	private static final Border BORDER = new Border() {
		public Insets getBorderInsets(Component c) {
			return INSETS;
		}
		public boolean isBorderOpaque() {
			return true;
		}
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			;
		}
	};
	private JLabel maxLabel = new JLabel();
	private JLabel totalLabel = new JLabel();
	private JLabel useLabel = new JLabel();

	private MemoryMonitor() {
		Color bg = maxLabel.getBackground();
		Color label = new Color(bg.getRed() - 24, bg.getGreen() - 24, bg.getBlue() - 24);
		maxLabel.setBorder(BORDER);
		maxLabel.setBackground(label);
		maxLabel.setOpaque(true);
		maxLabel.setToolTipText("Maximum memory the VM may use in MB");
		totalLabel.setBorder(BORDER);
		totalLabel.setBackground(label);
		totalLabel.setOpaque(true);
		totalLabel.setToolTipText("Total memory reserved for the VM in MB");
		useLabel.setBorder(BORDER);
		useLabel.setBackground(label);
		useLabel.setOpaque(true);
		useLabel.setToolTipText("Memory used by the VM in MB");
	}
	
	public static JComponent createMemoryMonitor() {
		final MemoryMonitor memMon = new MemoryMonitor();
		final Box box = Box.createHorizontalBox();
		box.add(memMon.maxLabel);
		box.add(Box.createHorizontalStrut(2));
		box.add(memMon.totalLabel);
		box.add(Box.createHorizontalStrut(2));
		box.add(memMon.useLabel);
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				memMon.update();
			}
		};
		timer.scheduleAtFixedRate(task, 0, DELAY);
		return box;
	}
	
	private void update() {
		Runtime r = Runtime.getRuntime();
		long maximum = r.maxMemory();
		long total = r.totalMemory();
		long free = r.freeMemory();
		long used = total - free;
		maxLabel.setText(Float.toString((float) ((maximum * 10) >> 20) / 10));
		totalLabel.setText(Float.toString((float) ((total * 10) >> 20) / 10));
		useLabel.setText(Float.toString((float) ((used * 10) >> 20) / 10));
	}
}
