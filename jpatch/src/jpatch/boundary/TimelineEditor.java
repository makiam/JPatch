package jpatch.boundary;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;

import javax.swing.*;

import jpatch.entity.*;

public class TimelineEditor extends JScrollPane {

//	private final ImageIcon ZOOM_IN = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/zoom_in.png"));
//	private final ImageIcon ZOOM_OUT = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/zoom_out.png"));
//	private JButton buttonZoomInH = new JButton(ZOOM_IN);
//	private JButton buttonZoomOutH = new JButton(ZOOM_OUT);
//	private JButton buttonZoomInV = new JButton(ZOOM_IN);
//	private JButton buttonZoomOutV = new JButton(ZOOM_OUT);
	
	private int iFrameWidth = 6;
	private final MotionCurve.Float[] curves = new MotionCurve.Float[20];
	private final Track[] tracks = new Track[20];
	private int currentFrame = 49;
	
	public TimelineEditor() {
//		super(VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);
//		Dimension dim = new Dimension(ZOOM_IN.getIconWidth() + 1, ZOOM_IN.getIconHeight() + 1);
//		buttonZoomInH.setPreferredSize(dim);
//		buttonZoomOutH.setPreferredSize(dim);
//		buttonZoomInV.setPreferredSize(dim);
//		buttonZoomOutV.setPreferredSize(dim);
//		buttonZoomInH.setFocusable(false);
//		buttonZoomOutH.setFocusable(false);
//		buttonZoomInV.setFocusable(false);
//		buttonZoomOutV.setFocusable(false);
//		Box corner = Box.createVerticalBox();
//		corner.add(buttonZoomInV);
//		corner.add(buttonZoomOutV);
//		corner.setPreferredSize(new Dimension(15, 30));
//		setCorner(UPPER_RIGHT_CORNER, corner);
//		corner = Box.createHorizontalBox();
//		corner.add(buttonZoomInH);
//		corner.add(buttonZoomOutH);
//		corner.setPreferredSize(new Dimension(30, 15));
//		setCorner(LOWER_LEFT_CORNER, corner);
//		setCorner(UPPER_RIGHT_CORNER, new Corner());
//		setCorner(UPPER_LEFT_CORNER, new Corner());
//		setCorner(LOWER_LEFT_CORNER, new Corner());
	}
	
	public void setViewportView(Component c) {
		super.setViewportView(c);
	}
	
	public static void main(String[] args) {
		new TimelineEditor().test();
	}
	
	public void test() {
		JFrame frame = new JFrame("Timeline Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setLayout(new BorderLayout());
////		table.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
////		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
////		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		table.setColumnSelectionAllowed(true);
//		table.setRowSelectionAllowed(true);
//		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		table.getTableHeader().setReorderingAllowed(false);
//		table.setDragEnabled(true);
//		
//		for (int i = 0; i < table.getColumnCount(); i++) {
//			table.getColumnModel().getColumn(i).setPreferredWidth(5);
//			table.getColumnModel().getColumn(i).setMinWidth(5);
//			table.getColumnModel().getColumn(i).setMaxWidth(5);
//			table.getColumnModel().getColumn(i).setResizable(false);
//		
		
		Model model = new Model();
//		final MotionCurve2.Float curve = MotionCurve2.createMorphCurve(morph);
		
		Random rnd = new Random();
		for (int i = 0; i < tracks.length; i++) {
			Morph morph = new Morph("test" + i, model);
			morph.setMin(-1);
			morph.setMax(1);
			curves[i] = MotionCurve.createMorphCurve(morph);
			for (int j = 0; j < 200; j++) {
				if (rnd.nextInt(5) == 0)
					curves[i].addKey(new MotionKey.Float(j, curves[i].getMin() + rnd.nextFloat() * (curves[i].getMax() - curves[i].getMin())));
			}
			tracks[i] = new AvarTrack(morph, curves[i]);
		}
			
		final TimelineEditor tle = this;
		
		TimelineDisplay display = tle.new TimelineDisplay();
		display.setOpaque(false);
		tle.setViewportView(display);
		
		JComponent columnHeader = tle.new Ruler();
		tle.setColumnHeaderView(columnHeader);
		System.out.println(tle.getColumnHeader());
		JComponent rowHeader = tle.new Header();
		tle.setRowHeaderView(rowHeader);
		frame.add(tle);
		frame.setSize(640, 480);
		frame.setVisible(true);
	}

	public int getTracksHeight() {
		int h = 0;
		for (Track track: tracks)
			h += track.getHeight();
		return h;
	}
	
	public class Header extends JComponent {
		int width = 128;
		private Dimension dim = new Dimension(width, 16 * 20);
		private Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		private Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
		private boolean bResizeCursor = false;
		private boolean bResizing = false;
		
		public Header() {
			addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseMoved(MouseEvent e) {
					setResizeCursor(e.getX() > width - 5);
				}
				public void mouseDragged(MouseEvent e) {
					if (bResizing) {
						width = e.getX() + 5;
						if (width < 16 + 5)
							width = 16 + 5;
						if (width > TimelineEditor.this.getWidth() - 32)
							width = TimelineEditor.this.getWidth() - 32;
						setSize(getPreferredSize());
						TimelineEditor.this.doLayout();
					}
				}
			});
			
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (e.getClickCount() == 2 && e.getX() > width - 5) {
						width = 128;
						setSize(getPreferredSize());
						TimelineEditor.this.doLayout();
						return;
					}
					bResizing = e.getX() > width - 5;
					setResizeCursor(bResizing);
					if (e.getClickCount() == 2) {
						int y = 0;
						for (Track track : tracks) {
							if (e.getY() > y && e.getY() < y + track.getHeight()) {
								track.expand(!track.isExpanded());
								revalidate();
								((JComponent) TimelineEditor.this.getRowHeader().getView()).revalidate();
								TimelineEditor.this.repaint();
								return;
							}
							y += track.getHeight();
						}
					}
//					repaint();
				}
				public void mouseReleased(MouseEvent e) {
					if (bResizing) {
						bResizing = false;
//						repaint();
					}
				}
				public void mouseExited(MouseEvent e) {
					if (!bResizing)
						setResizeCursor(false);
				}
			});
		}
		
		private void setResizeCursor(boolean enable) {
			if (enable == bResizeCursor)
				return;
			bResizeCursor = enable;
			if (bResizeCursor)
				setCursor(resizeCursor);
			else
				setCursor(defaultCursor);
//			repaint();
		}
		public Dimension getPreferredSize() {
			dim.setSize(width, getTracksHeight());
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
			//int frame = start / TimelineEditor.this.iFrameWidth - 1;
//			g.setColor(Color.WHITE);
//			for (int x = -TimelineEditor.this.iFrameWidth ; x <= clip.width + TimelineEditor.this.iFrameWidth; x += TimelineEditor.this.iFrameWidth) {
//				g.drawLine(x + start, clip.y, x + start, clip.y + clip.height);
//			}
			
			int y = 0;
			for (Track track : tracks) {
				
				g.setColor(Color.WHITE);
				g.drawLine(clip.x, y + track.getHeight() - 1, clip.x + clip.width, y + track.getHeight() - 1);
				g.setColor(Color.BLACK);
				g.drawString(((AvarTrack) track).motionCurve.getName(), 8, y + 12);
				y += track.getHeight();
			}
//			if (bResizing)
//				g.setColor(getBackground().darker());
//			if (bResizeCursor)
//				g.setColor(getBackground().brighter());
//			else
				g.setColor(getBackground());
			g.fill3DRect(width - 6, 0, 6, dim.height, true);
//			g.setColor(Color.BLACK);
//			g.drawLine(x + 1, clip.y, x + 1, clip.height);
		}
	}

	private class TimelineDisplay extends JComponent implements Scrollable {
		private Dimension dim = new Dimension();
		
		public TimelineDisplay() {
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent event) {
					if (event.getClickCount() == 2) {
						int y = 0;
						for (Track track : tracks) {
							if (event.getY() > y && event.getY() < y + track.getHeight()) {
								track.expand(!track.isExpanded());
								revalidate();
								((JComponent) TimelineEditor.this.getRowHeader().getView()).revalidate();
								TimelineEditor.this.repaint();
								return;
							}
							y += track.getHeight();
						}
					}
				}
			});
		}
		
		public Dimension getPreferredSize() {
			dim.setSize(iFrameWidth * 200, getTracksHeight()); // FIXME: use animation length
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
			int start = clip.x - clip.x % iFrameWidth;
			//int frame = start / TimelineEditor.this.iFrameWidth - 1;
//			g.setColor(Color.WHITE);
//			for (int x = -TimelineEditor.this.iFrameWidth ; x <= clip.width + TimelineEditor.this.iFrameWidth; x += TimelineEditor.this.iFrameWidth) {
//				g.drawLine(x + start, clip.y, x + start, clip.y + clip.height);
//			}
			int y = 0;
			for (Track track : tracks) {
				track.paint(g, y);
				y += track.getHeight();
			}
			int x = currentFrame * iFrameWidth + iFrameWidth / 2;
			g.setColor(Color.BLACK);
			g.drawLine(x, clip.y, x, clip.y + clip.height);
//			g.setColor(Color.BLACK);
//			g.drawLine(x + 1, clip.y, x + 1, clip.height);
		}
		
		public Dimension getPreferredScrollableViewportSize() {
			return new Dimension(600, 200);
		}

		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			switch (orientation) {
			case SwingConstants.HORIZONTAL:
				int x = visibleRect.x % iFrameWidth;
				if (direction > 0)
					return iFrameWidth - x;
				else
					return x == 0 ? iFrameWidth : x;
			case SwingConstants.VERTICAL:
				int y= visibleRect.y % 16;
				if (direction > 0)
					return 16 - y;
				else
					return y == 0 ? 16 : y;
			}
			throw new IllegalArgumentException();
		}

		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			switch (orientation) {
			case SwingConstants.HORIZONTAL:
				return getScrollableUnitIncrement(visibleRect, orientation, direction) + iFrameWidth * 9;
			case SwingConstants.VERTICAL:
				return getScrollableUnitIncrement(visibleRect, orientation, direction) + 16 * 4;
			}
			throw new IllegalArgumentException();
		}

		public boolean getScrollableTracksViewportWidth() {
			return false;
		}

		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}
	
	private class Ruler extends JComponent {
		private Dimension dim = new Dimension();
		private Font font = new Font("Monospaced", Font.PLAIN, 10);
		
		public Dimension getPreferredSize() {
			dim.setSize(iFrameWidth * 200, 16); // FIXME: use animation length
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
			g.setFont(font);
			Rectangle clip = g.getClipBounds();
//			int frameWidth = TimelineEditor.this.iFrameWidth;
			int start = clip.x - clip.x % iFrameWidth + TimelineEditor.this.iFrameWidth / 2;
			int frame = start / iFrameWidth - 1;
//			((Graphics2D) g).setPaint(new GradientPaint(0, 0, getBackground(), 0, 20, getBackground().brighter()));
//			((Graphics2D) g).fill(clip);
//			g.fillRect(clip.x, clip.y, clip.width, clip.height);
			g.setColor(Color.BLACK);
			g.drawLine(clip.x, 15, clip.x + clip.width, 15);
			for (int x = -iFrameWidth ; x <= clip.width + iFrameWidth; x += iFrameWidth) {
				if (frame % 6 == 0)
					g.drawLine(x + start, getHeight() - 6, x + start, getHeight() - 1);
				else
					g.drawLine(x + start, getHeight() - 4, x + start, getHeight() - 3);
				if (frame % 12 == 0) {
					if (iFrameWidth > 2 || frame % 24 == 0) {
						String num = String.valueOf(frame);
						g.drawString(num, x + start - num.length() * 3 + 1, 9);
					}
				}
				if (frame == currentFrame) {
//					g.setColor(Color.BLACK);
//					g.fillPolygon(new int[] { x + start - 4, x + start + 7, x + start + 1}, new int[] { clip.y + clip.height - 6, clip.y + clip.height - 6, clip.y + clip.height - 0}, 3);
//					g.setColor(Color.RED);
					g.fillPolygon(new int[] { x + start - 5, x + start + 6, x + start }, new int[] { getHeight() - 6, getHeight() - 6, getHeight() - 0}, 3);
//					g.setColor(Color.BLACK);
				}
				frame++;
			}
			
//			g.setColor(Color.WHITE);
//			g.draw3DRect(clip.x, clip.y, clip.width - 1, clip.height - 1, false);
//			g.draw3DRect(clip.x + 1, clip.y + 1, clip.width - 3, clip.height - 3, true);
		}
	}
	
	public abstract class Track {
		public final static int TRACK_HEIGHT = 16;
		boolean bExpanded = false;
		
		public int getHeight() {
			return TRACK_HEIGHT;
		}
		
		public abstract void paint(Graphics g, int y);
		
		public void expand(boolean expand) {
			bExpanded = expand;
		}
		
		public boolean isExpanded() {
			return bExpanded;
		}
	}
	
	
	
	public class AvarTrack extends Track {
		private static final int EXPANDED_HEIGHT = 64;
		private final Color SEPARATOR = new Color(255, 255, 255);
		private final Color TRACK = new Color(208, 216, 200);
		private final Color KEY = new Color(136, 128, 144);
		
		private Morph morph;
		private MotionCurve.Float motionCurve;
		
		
		public int getHeight() {
			return bExpanded ? EXPANDED_HEIGHT : TRACK_HEIGHT;
		}
		
		public AvarTrack(Morph morph, MotionCurve.Float motionCurve) {
			this.morph = morph;
			this.motionCurve = motionCurve;
		}
		
		public void paint(Graphics g, int y) {
			
			Rectangle clip = g.getClipBounds();
			
			int start = clip.x - clip.x % iFrameWidth + iFrameWidth / 2;
			int frame = start / iFrameWidth - 1;
			g.setColor(SEPARATOR);
			g.drawLine(clip.x, y + getHeight() - 1, clip.x + clip.width, y + getHeight() - 1);
			if (bExpanded) {
				float scale = motionCurve.getMax() - motionCurve.getMin();
				int size = 50;
				int off = 64 - 8 + (int) (size * motionCurve.getMin() / scale);
				g.setColor(TRACK);
				g.fillRect(clip.x, y + 6, clip.width, size);
//				g.setColor(TRACK.darker());
//				g.drawLine(clip.x, y + 1, clip.x + clip.width, y + 1);
//				g.setColor(TRACK.brighter());
//				g.drawLine(clip.x, y + 61, clip.x + clip.width, y + 61);
				g.setColor(getBackground());
				for (int x = -iFrameWidth ; x <= clip.width + iFrameWidth; x += iFrameWidth) {
					g.drawLine(x + start, y + 2, x + start, y + 60);
				}
				g.setColor(Color.BLACK);
				g.drawLine(clip.x, y + off, clip.x + clip.width, y + off);
				g.setClip(clip.intersection(new Rectangle(clip.x, y + 4, clip.width, 56)));
				int vPrev = off - (int) (size / scale * motionCurve.getFloatAt(frame));
				g.setColor(KEY);
				for (int x = -iFrameWidth ; x <= clip.width + iFrameWidth; x ++) {
					float f = (float) (start + x - iFrameWidth / 2) / iFrameWidth;
					int vThis = off - (int) (size / scale * motionCurve.getFloatAt(f));
//					g.setColor(Color.BLACK);
					g.drawLine(x + start - 1, y + vPrev, x + start, y + vThis);
					frame++;
					vPrev = vThis;
				}
				g.setColor(KEY);
				frame = start / iFrameWidth - 1;
				for (int x = -iFrameWidth ; x <= clip.width + iFrameWidth; x += iFrameWidth) {
					int vThis = off - (int) (size / scale * motionCurve.getFloatAt(frame));
					if (motionCurve.getKeyAt(frame) != null) {
						g.fillOval(x + start - 3, y + vThis - 3, 6, 6);
						g.setColor(Color.BLACK);
						g.drawOval(x + start - 3, y + vThis - 3, 6, 6);
						g.setColor(KEY);
					} else {
//						g.fillRect(x + start - 1, y + vThis - 1, 3, 3);
					}
					frame++;
				}
				g.setClip(clip);
				return;
			}
//			g.setColor(TRACK.darker());
//			g.drawLine(clip.x, y + 5, clip.x + clip.width, y + 5);
//			g.setColor(TRACK.brighter());
//			g.drawLine(clip.x, y + 9, clip.x + clip.width, y + 9);
			g.setColor(TRACK);
			g.fillRect(clip.x, y + 5, clip.width, 5);
			
			g.setColor(KEY);
			for (int x = -iFrameWidth ; x <= clip.width + iFrameWidth; x += iFrameWidth) {
				if (motionCurve.getKeyAt(frame) != null) {
					//g.fill3DRect(x + start - iFrameWidth / 2, y + 2, iFrameWidth, 11, true);
					g.setColor(KEY);
					g.fillOval(x + start - 3, y + 4, 6, 6);
					g.setColor(Color.BLACK);
					g.drawOval(x + start - 3, y + 4, 6, 6);
					
				} else {
					g.setColor(getBackground());
					g.drawLine(x + start, y + 5, x + start, y + 9);
				}
				frame++;
			}
		}
	}
}
