/*
 * $Id: Header.java,v 1.21 2006/05/06 09:52:15 sascha_l Exp $
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
package jpatch.boundary.timeline;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

import java.util.*;
import java.util.List;
import javax.swing.*;

import jpatch.entity.*;

public class Header extends JComponent implements MouseListener, MouseMotionListener {
	/**
	 * 
	 */
	private static enum SelectionMode { OFF, ADD, REMOVE, RANGE }

	private final TimelineEditor timelineEditor;
	int width = 128;
	private Dimension dim = new Dimension(width, 16 * 20);
	
	private boolean bHorizontalResize = false;
	private int iVerticalResize = -1;
	private static final Icon[] iconDownArrow = new Icon[] {createIcon(0, Color.BLACK), createIcon(0, Color.WHITE), createIcon(0, UIManager.getColor("Button.focus")) };
	private static final Icon[] iconUpArrow = new Icon[] {createIcon(1, Color.BLACK), createIcon(1, Color.WHITE), createIcon(1, UIManager.getColor("Button.focus")) };
	private JToggleButton[] expandButton = new JToggleButton[0];
	
	private Font plain = new Font("Sans-Serif", Font.PLAIN, 12);
	private Font bold = new Font("Sans-Serif", Font.BOLD, 12);
	
	private Set<Track> setSelectedTracks = new HashSet<Track>();
	private Set<Track> backupTracks = new HashSet<Track>();
	private int iSelectedTrack;
	private SelectionMode selectionMode = SelectionMode.OFF;
	
	private int my = -1;
	
	public Header(TimelineEditor tle) {
		timelineEditor = tle;
		addMouseListener(this);
		addMouseMotionListener(this);
		setLayout(null);
		createButtons();
	}
	
	public Set<Track> getSelectedTracks() {
		return setSelectedTracks;
	}
	
	public void createButtons() {
		for (AbstractButton button : expandButton)
			if (button != null)
				remove(button);
		expandButton = new JToggleButton[timelineEditor.getTracks().size()];
		for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
			final Track track = timelineEditor.getTracks().get(i);
			expandButton[i] = new JToggleButton(iconDownArrow[0]);
			expandButton[i].setRolloverIcon(iconDownArrow[2]);
			expandButton[i].setPressedIcon(iconDownArrow[1]);
			expandButton[i].setSelectedIcon(iconUpArrow[0]);
			//expandButton[i].set(iconDownArrow[1]);
			expandButton[i].setRolloverSelectedIcon(iconUpArrow[2]);
			expandButton[i].setBorderPainted(false);
			expandButton[i].setContentAreaFilled(false);
			expandButton[i].setFocusable(false);
			expandButton[i].setOpaque(false);
			expandButton[i].setToolTipText("expand track");
			expandButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean expanded = !track.isExpanded();
					timelineEditor.expandTrack(track, expanded);
					((JToggleButton) e.getSource()).setToolTipText(expanded ? "collapse track" : "expand track");
					((JToggleButton) e.getSource()).setPressedIcon(expanded ? iconUpArrow[1] : iconDownArrow[1]);
				}
			});
			add(expandButton[i]);
		}
	}
	
	public void setAllExpanded(boolean expanded) {
		for (JToggleButton button : expandButton) {
			button.setToolTipText(expanded ? "collapse track" : "expand track");
			button.setPressedIcon(expanded ? iconUpArrow[1] : iconDownArrow[1]);
		}
	}
	
	public void doLayout() {
		super.doLayout();
		layoutButtons();
	}
	
	private void layoutButtons() {
		int y = 0;
		
		for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
			Track track = timelineEditor.getTracks().get(i);
			expandButton[i].setVisible(!track.isHidden());
			if (track.isHidden())
				continue;
			if (track.isExpandable()) {
				expandButton[i].setBounds(3, y + 3, 11, 6);
				expandButton[i].setSelected(track.isExpanded());
			}
			y += track.getHeight();
		}
	}
	
	public Dimension getPreferredSize() {
		dim.setSize(width, timelineEditor.getTracksHeight() + 7);
		return dim;
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Rectangle clip = g.getClipBounds();
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		//int frame = start / TimelineEditor.this.iFrameWidth - 1;
//		g.setColor(Color.WHITE);
//		for (int x = -TimelineEditor.this.iFrameWidth ; x <= clip.width + TimelineEditor.this.iFrameWidth; x += TimelineEditor.this.iFrameWidth) {
//		g.drawLine(x + start, clip.y, x + start, clip.y + clip.height);
//		}
		
		
//		g.setColor(TimelineEditor.BACKGROUND);
//		g.fillRect(width - 4, clip.y, 3, clip.height);
//		clip.width -= 5;
//		g.setClip(clip);
		int y = 0;
		Track prev = null;
		for (Track track : timelineEditor.getTracks()) {
			if (track.isHidden())
				continue;
			if (y + track.getHeight() > clip.y && y < clip.y + clip.height) {
				int height = track.getHeight();
				int bottom = track.getHeight() - 4;
				int clipTop = clip.y > y ? clip.y : y;
				int clipBottom = clip.y + clip.height < y + height ? clip.y + clip.height : y + height;
				g.setClip(clip.x, clipTop, clip.width, clipBottom - clipTop);
				if (setSelectedTracks.contains(track)) {
					g.setColor(TimelineEditor.SELECTED_BACKGROUND);
					g.fillRect(0, y, width, track.isExpanded() ? bottom : height);
				}
				g.setColor(Color.BLACK);
				if (track instanceof HeaderTrack) {
//					g.setColor(UIManager.getColor("ScrollBar.shadow"));
//					g.fillRect(clip.x, y, clip.width, track.getHeight() - 1);
					//g.setFont(bold);
					g.setColor(TimelineEditor.HIGHLIGHT);
					g.drawString(track.getName(), 16 + track.getIndent(), y + 11);
					g.setColor(TimelineEditor.SHADOW);
					//g.drawString(track.getName(), 16 + track.getIndent(), y + 10);
					g.drawString(track.getName(), 15 + track.getIndent(), y + 10);
					//g.drawString(track.getName(), 15 + track.getIndent(), y + 11);
					//g.setColor(TimelineEditor.HIGHLIGHT);
					//g.drawString(track.getName(), 16 + track.getIndent(), y + 12);
					//g.drawString(track.getName(), 17 + track.getIndent(), y + 12);
					//g.drawString(track.getName(), 17 + track.getIndent(), y + 11);
					
				} else {
					g.setFont(plain);
					g.drawString(track.getName(), 16 + track.getIndent(), y + 11);
					if (track instanceof BoneTrack) {
						Bone bone = ((BoneTrack) track).getBone();
						Color[] col = new Color[] {
								new Color(255, 0, 0),
								new Color(0, 128, 0),
								new Color(0, 0, 255)
						};
						for (int i = 0; i < bone.getDofs().size(); i++) {
							g.setColor(col[i]);
							g.drawString(bone.getDof(i).getName(), 16 + track.getIndent() + 8, y + 25 + 14 * i);
						}
					}
				}
				
				g.setColor(Color.GRAY);
				for (int i = 0; i < track.getIndent(); i += 4) {
					g.fillRect(16 + i, y + 6, 2, 2);
				}
				
				g.setColor(Color.BLACK);
				if (track.isExpanded()) {
					g.setColor(TimelineEditor.SHADOW);
					g.drawLine(width - 6, y, width - 6, y + height - 1);
					g.drawLine(width - 2, y, width - 2, y + height - 1);
					g.setColor(TimelineEditor.LIGHT_SHADOW);
					g.drawLine(width - 1, y, width - 1, y + height - 1);
//					g.setColor(TimelineEditor.BACKGROUND);
//					g.fillRect(width - 4, y, 2, y + bottom);
//					g.setColor(UIManager.getColor("ScrollBar.shadow"));
//					g.drawLine(width - 1, y, width - 1, y + track.getHeight() - 5);
//					g.drawLine(0, y + track.getHeight() - 1, width - 1, y + track.getHeight() - 1);
//					g.setColor(Track.SEPARATOR);
//					g.drawLine(width - 6, y, width - 6, y + track.getHeight() - 1);
//					g.drawLine(width - 2, y, width - 2, y + track.getHeight() - 1);
//					g.drawLine(0, y + track.getHeight() - 6, width - 1, y + track.getHeight() - 6);
//					g.drawLine(0, y + track.getHeight() - 2, width - 1, y + track.getHeight() - 2);
					
//					g.setColor(getBackground());
//					g.fillRect(width - 3, y, 3, track.getHeight());
//					g.fillRect(0, y + track.getHeight() - 3, width, 3);
					if (prev != null && prev.isExpanded()) {
						g.setColor(TimelineEditor.SHADOW);
						g.drawLine(0, y, width - 1, y);
						g.setColor(TimelineEditor.LIGHT_SHADOW);
						g.drawLine(0, y + 1, width - 7, y + 1);
						g.setColor(TimelineEditor.BACKGROUND);
						g.fillRect(0, y - 3, width, 3);
					} else {
						g.setColor(TimelineEditor.SHADOW);
						g.drawLine(width - 2, y, width - 1, y);
					}
//					g.drawLine(width - 1, y, width - 1, y + bottom - 1);
					g.setColor(TimelineEditor.SHADOW);
					g.drawLine(0, y + bottom, width - 1, y + bottom);
//					g.drawLine(width - 5, y + 1, width - 5, y + bottom);
					
//					g.fillRect(width - 5, y, 4, bottom + );
//					g.fillRect(width - 4, y, 3, track.getHeight());
				} else {
					g.setColor(TimelineEditor.SHADOW);
					g.drawLine(width - 6, y, width - 6, y + height - 1);
					g.drawLine(width - 2, y, width - 2, y + height - 1);
					g.setColor(TimelineEditor.LIGHT_SHADOW);
					g.drawLine(width - 1, y, width - 1, y + height - 1);
//					g.setColor(TimelineEditor.BACKGROUND);
//					g.fillRect(width - 4, y, 2, y + height);
//					g.setColor(UIManager.getColor("ScrollBar.shadow"));
//					g.drawLine(width - 1, y, width - 1, y + track.getHeight() - 1);
//					g.setColor(TimelineEditor.SHADOW);
//					g.drawLine(width - 1, y - 3, width - 1, y + bottom);
//					g.setColor(TimelineEditor.HIGHLIGHT);
//					g.drawLine(width - 5, y - 3, width - 5, y + bottom);
//					g.setColor(getBackground());
//					g.fillRect(width - 4, y, 3, track.getHeight());
//					g.drawLine(width - 6, y, width - 6, y + track.getHeight() - 1);
//					g.drawLine(width - 2, y, width - 2, y + track.getHeight() - 1);
//					g.drawLine(0, y + track.getHeight() - 1, width - 1, y + track.getHeight() - 1);
//					g.setColor(getBackground());
//					g.fillRect(width - 3, y, 3, track.getHeight());
				}
			}
			y += track.getHeight();
			prev = track;
		}
		g.setClip(clip);
		g.setColor(TimelineEditor.BACKGROUND);
		g.fillRect(width - 5, clip.y, 3, clip.height);
		
		g.setColor(TimelineEditor.SHADOW);
		g.drawLine(width - 6, y, width - 6, y + 5);
		g.drawLine(width - 2, y, width - 2, y + 5);
		g.setColor(TimelineEditor.LIGHT_SHADOW);
		g.drawLine(width - 1, y, width - 1, y + 5);
		g.setColor(Color.BLACK);
		g.drawLine(0, y + 6, width - 1, y + 6);
		g.setColor(TimelineEditor.SHADOW);
		Rectangle r = timelineEditor.getRowHeader().getViewRect();
		g.fillRect(0, y + 7, width, r.height);
		
//		if (timelineEditor.getTracks().size() > 0 && timelineEditor.getTracks().get(timelineEditor.getTracks().size() - 1).isExpanded())
//		y -= 1;
//		g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
//		g.drawLine(0, y - 1, width - 1, y - 1);
//		g.setColor(UIManager.getColor("ScrollBar.shadow"));
//		g.drawLine(0, y, width - 1, y);
//		g.setColor(getBackground().darker());
//		g.drawLine(width - 5, 0, width - 5, y - 1);
//		g.drawLine(width - 1, 0, width - 1, y - 1);
//		g.drawLine(width - 5, 0, width - 1, 0);
//		g.drawLine(width - 5, y - 1, width - 1, y - 1);
//		g.setColor(getBackground().darker().darker());
//		g.fillRect(0, y, width, clip.height - y);
//		if (bResizing)
//		g.setColor(getBackground().darker());
//		if (bResizeCursor)
//		g.setColor(getBackground().brighter());
//		else
		
//		g.setColor(Color.BLACK);
//		g.drawLine(x + 1, clip.y, x + 1, clip.height);
	}
	
	private static Icon createIcon(final int type, final Color color) {
		return new Icon() {
			public void paintIcon(Component c, Graphics g, int x, int y) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				g.setColor(color);
				switch (type) {
				case 0:
					g.fillPolygon(new int[] { 0, 9, 4 }, new int[] { 1, 1, 6}, 3);
					break;
				case 1:
					g.fillPolygon(new int[] { -1, 9, 4 }, new int[] { 5, 5, -1}, 3);
					break;
				}
			}
			
			public int getIconWidth() {
				return 6;
			}
			
			public int getIconHeight() {
				return 4;
			}
		};
	}
	
	public void mouseMoved(MouseEvent e) {
		boolean hResize = e.getX() > width - 6 && e.getY() < timelineEditor.getTracksHeight();
		int y = 0;
		boolean vResize = false;
		for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
			Track track = timelineEditor.getTracks().get(i);
			if (!bHorizontalResize && track.isExpanded() && e.getY() > y + track.getHeight() - 6 && e.getY() <= y + track.getHeight()) {
				vResize = true;
				break;
			}
			y += track.getHeight();
		}
		if (hResize && vResize)
			timelineEditor.setCursor(TimelineEditor.cornerResizeCursor);
		else if (hResize)
			timelineEditor.setCursor(TimelineEditor.horizontalResizeCursor);
		else if (vResize)
			timelineEditor.setCursor(TimelineEditor.verticalResizeCursor);
		else 
			timelineEditor.setCursor(TimelineEditor.defaultCursor);
	}
	
	public void mouseDragged(MouseEvent e) {
		if (bHorizontalResize) {
			width = e.getX() + 3;
			if (width < 48 + 6)
				width = 48 + 6;
			if (width > timelineEditor.getWidth() - 48)
				width = timelineEditor.getWidth() - 48;
			setSize(getPreferredSize());
			timelineEditor.doLayout();
			return;
		}
		if (iVerticalResize > -1) {
			int y = 0;
			for (int i = 0; i < iVerticalResize; i++)
				y += timelineEditor.getTracks().get(i).getHeight();
			int h = e.getY() - y + 3;
			if (h < 32)
				h = 32;
			if (h > 256)
				h = 256;
			timelineEditor.setTrackHeight(iVerticalResize, h);
			timelineEditor.revalidate();
			revalidate();
			((JComponent) timelineEditor.getViewport().getView()).revalidate();
			timelineEditor.repaint();
			return;
		}
		
		if (my != -1) {
			JScrollBar vsb = timelineEditor.getVerticalScrollBar();
			int sby = vsb.getValue();
			int dy = e.getY() - my;
			my = (sby == 0 || sby == vsb.getMaximum() - vsb.getVisibleAmount()) ? e.getY() : e.getY() - dy;
			timelineEditor.getVerticalScrollBar().setValue(sby - dy);
			return;
		}
		int y = 0, i;
		for (i = 0; i < timelineEditor.getTracks().size(); i++) {
			Track track = timelineEditor.getTracks().get(i);
			if (e.getY() > y && e.getY() < y + track.getHeight())
				break;
			y += track.getHeight();
		}
		if (i < timelineEditor.getTracks().size()) {
			switch (selectionMode) {
			case ADD:
				//setSelectedTracks.add(timelineEditor.getTracks().get(i));
				setSelectedTracks.clear();
				setSelectedTracks.addAll(backupTracks);
				selectRange(i, false);
				break;
			case REMOVE:
				setSelectedTracks.clear();
				setSelectedTracks.addAll(backupTracks);
				unselectRange(i);
				break;
			case RANGE:
				setSelectedTracks.clear();
				selectRange(i, false);
			}
			timelineEditor.repaint();
		}
	}
	
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (e.getClickCount() == 2 && e.getX() > width - 6) {
				width = 128;
				setSize(getPreferredSize());
				timelineEditor.doLayout();
//				setHorizontalResizeCursor(e.getX() > width - 5);
				return;
			}
			bHorizontalResize = e.getX() > width - 6;
			iVerticalResize = -1;
			//setHorizontalResizeCursor(bHorizontalResize);
			
			int y = 0;
			for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
				Track track = timelineEditor.getTracks().get(i);
				int h2 = track.isExpanded() ? track.getHeight() - 6 : track.getHeight();
				if (e.getClickCount() == 1 && e.getY() > y && e.getY() < y + h2) {
//					timelineEditor.expandTrack(track, !track.isExpanded());
					
					if (e.isShiftDown()) {
						setSelectedTracks.clear();
						selectRange(i, true);
						selectionMode = SelectionMode.RANGE;
					} else {
						if (!e.isControlDown()) {
							setSelectedTracks.clear();
							if (i == 0) {
								selectVisibleTracks();
								timelineEditor.repaint();
								return;
							}
							selectTrack(i, false);
							selectionMode = SelectionMode.ADD;
							backupTracks.clear();
							backupTracks.addAll(setSelectedTracks);
						} else {
							if (setSelectedTracks.contains(track))
								selectionMode = SelectionMode.REMOVE;
							else
								selectionMode = SelectionMode.ADD;
							selectTrack(i, true);
							backupTracks.clear();
							backupTracks.addAll(setSelectedTracks);
						}
						iSelectedTrack = i;
					}
					timelineEditor.repaint();
					return;
				} if (track.isExpanded() && e.getY() > y + track.getHeight() - 6 && e.getY() <= y + track.getHeight()) {
					if (e.getClickCount() == 2) {
						timelineEditor.setTrackHeight(i, track.EXPANDED_HEIGHT);
						timelineEditor.revalidate();
						revalidate();
						((JComponent) timelineEditor.getViewport().getView()).revalidate();
						timelineEditor.repaint();
//						setVerticalResizeCursor(e.getY() > y + track.getHeight() - 5 && e.getY() <= y + track.getHeight());
					} else {
						iVerticalResize = i;
					}
					return;
				}
				y += track.getHeight();
			}
			setSelectedTracks.clear();
			timelineEditor.repaint();
			return;
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			showPopup(e);
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			my = e.getY();
		}
//		repaint();
	}
	
	private void selectRange(int track, boolean selectGroups) {
		if (iSelectedTrack < track) {
			for (int i = iSelectedTrack; i <= track; i++) {
				if (timelineEditor.getTracks().get(i).isHidden())
					continue;
				if (!selectGroups || (i != iSelectedTrack && i != track)) {
					if (!(timelineEditor.getTracks().get(i) instanceof HeaderTrack))
						selectTrack(i, false);
				} else {
					selectTrack(i, false);
				}
			}
		} else {
			for (int i = track; i <= iSelectedTrack; i++) {
				if (timelineEditor.getTracks().get(i).isHidden())
					continue;
				if (!selectGroups || (i != iSelectedTrack && i != track)) {
					if (!(timelineEditor.getTracks().get(i) instanceof HeaderTrack))
						selectTrack(i, false);
				} else {
					selectTrack(i, false);
				}
			}
		}
	}
	
	private void unselectRange(int track) {
		if (iSelectedTrack < track) {
			for (int i = iSelectedTrack; i <= track; i++)
				if (!(timelineEditor.getTracks().get(i) instanceof HeaderTrack))
					setSelectedTracks.remove(timelineEditor.getTracks().get(i));
		} else {
			for (int i = track; i <= iSelectedTrack; i++)
				if (!(timelineEditor.getTracks().get(i) instanceof HeaderTrack))
					setSelectedTracks.remove(timelineEditor.getTracks().get(i));
		}	
	}
	
	private void selectTrack(int trackNo,  boolean toggle) {
		Track track = timelineEditor.getTracks().get(trackNo);
		if (track instanceof HeaderTrack) {
			trackNo++;
			while (trackNo < timelineEditor.getTracks().size()) {
				track = timelineEditor.getTracks().get(trackNo);
				if (track instanceof HeaderTrack)
					break;
				if (toggle && setSelectedTracks.contains(track))
					setSelectedTracks.remove(track);
				else
					setSelectedTracks.add(track);
				trackNo++;
			}
		} else {
			if (toggle && setSelectedTracks.contains(track))
				setSelectedTracks.remove(track);
			else
				setSelectedTracks.add(track);
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if (bHorizontalResize)
			bHorizontalResize = false;
		if (iVerticalResize > -1)
			iVerticalResize = -1;
		selectionMode = SelectionMode.OFF;
		my = -1;
//		setHorizontalResizeCursor(e.getX() > width - 5);
//		int y = 0;
//		boolean vResize = false;
//		for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
//		Track track = timelineEditor.getTracks().get(i);
//		if (!bHorizontalResize && track.isExpanded() && e.getY() > y + track.getHeight() - 5 && e.getY() <= y + track.getHeight()) {
//		vResize = true;
//		break;
//		}
//		y += track.getHeight();
//		}
//		setVerticalResizeCursor(vResize);
	}
	
	public void mouseExited(MouseEvent e) {
		timelineEditor.setCursor(TimelineEditor.defaultCursor);
	}
	
	public void mouseClicked(MouseEvent e) { }
	
	public void mouseEntered(MouseEvent e) { }
	
	private void showPopup(MouseEvent e) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem mi;
		
		/*
		 * select all (not hidden) tracks
		 */
		mi = new JMenuItem("select all (not hidden) tracks");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				selectVisibleTracks();
				timelineEditor.repaint();
			}
		});
		popup.add(mi);
		
		/*
		 * unselect all tracks
		 */
		mi = new JMenuItem("unselect all tracks");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setSelectedTracks.clear();
				timelineEditor.repaint();
			}
		});
		if (setSelectedTracks.size() == 0)
			mi.setEnabled(false);
		popup.add(mi);
		
		/*
		 * invert selection
		 */
		mi = new JMenuItem("invert selection");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				for (Track track : timelineEditor.getTracks())
					if (!(track instanceof HeaderTrack)) {
						if (setSelectedTracks.contains(track))
							setSelectedTracks.remove(track);
						else if (!track.isHidden())
							setSelectedTracks.add(track);
					}
				timelineEditor.repaint();
			}
		});
		popup.add(mi);
		
		popup.add(new JSeparator());
		
		/*
		 * show only selected tracks
		 */
		mi = new JMenuItem("show only selected tracks");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				for (Track track : timelineEditor.getTracks())
					timelineEditor.hideTrack(track, !setSelectedTracks.contains(track));
				showHideHeaderTracks();
				timelineEditor.revalidate();
				revalidate();
				((JComponent) timelineEditor.getViewport().getView()).revalidate();
				timelineEditor.repaint();
			}
		});
		if (setSelectedTracks.size() == 0)
			mi.setEnabled(false);
		popup.add(mi);
		
		/*
		 * hide selected tracks
		 */
		mi = new JMenuItem("hide selected tracks");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				for (Track track : setSelectedTracks)
					timelineEditor.hideTrack(track, true);
				setSelectedTracks.clear();
				showHideHeaderTracks();
				timelineEditor.revalidate();
				revalidate();
				((JComponent) timelineEditor.getViewport().getView()).revalidate();
				timelineEditor.repaint();
			}
		});
		if (setSelectedTracks.size() == 0)
			mi.setEnabled(false);
		popup.add(mi);
		
		/*
		 * show all tracks
		 */
		mi = new JMenuItem("show all tracks");
		mi.setEnabled(false);
		for (Track track : timelineEditor.getTracks()) {
			if (track.isHidden()) {
				mi.setEnabled(true);
				break;
			}
		}
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				for (Track track : timelineEditor.getTracks())
					timelineEditor.hideTrack(track, false);
				timelineEditor.revalidate();
				revalidate();
				((JComponent) timelineEditor.getViewport().getView()).revalidate();
				timelineEditor.repaint();
			}
		});
		popup.add(mi);
//		mi = new JMenuItem("dump selected tracks");
//		mi.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				for (Track track : setSelectedTracks)
//					System.out.println(track.getName());
//			}
//		});
//		popup.add(mi);
		popup.show((Component) e.getSource(), e.getX(), e.getY());
	}
	
	private void showHideHeaderTracks() {
		Track header = null;
		for (Track track : timelineEditor.getTracks()) {
			if (track instanceof HeaderTrack) {
				header = track;
				timelineEditor.hideTrack(header, true);
			} else {
				if (!track.isHidden() && header != null)
					timelineEditor.hideTrack(header, false);
			}
		}
	}
	
	private void selectVisibleTracks() {
		setSelectedTracks.clear();
		for (Track track : timelineEditor.getTracks())
			if (!track.isHidden() && !(track instanceof HeaderTrack))
				setSelectedTracks.add(track);
	}
}