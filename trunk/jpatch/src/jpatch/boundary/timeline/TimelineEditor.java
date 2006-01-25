package jpatch.boundary.timeline;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.metal.MetalScrollBarUI;

import jpatch.entity.*;
import jpatch.boundary.*;

public class TimelineEditor extends JScrollPane {

//	private final ImageIcon ZOOM_IN = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/zoom_in.png"));
//	private final ImageIcon ZOOM_OUT = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/zoom_out.png"));
//	private JButton buttonZoomInH = new JButton(ZOOM_IN);
//	private JButton buttonZoomOutH = new JButton(ZOOM_OUT);
//	private JButton buttonZoomInV = new JButton(ZOOM_IN);
//	private JButton buttonZoomOutV = new JButton(ZOOM_OUT);
	
	public static Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	public static Cursor horizontalResizeCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
	public static Cursor verticalResizeCursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
	public static Cursor cornerResizeCursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
	private Cursor currentCursor = defaultCursor;
	
	public static Color BACKGROUND = UIManager.getColor("ScrollPane.background");
	public static Color SHADOW = derivedColor(BACKGROUND, -96, -96, -96);
	public static Color LIGHT_SHADOW = derivedColor(BACKGROUND, -48, -48, -48);
	public static Color HIGHLIGHT = Color.WHITE;
	public static Color TICK = derivedColor(BACKGROUND, -32, -32, -32);
	public static Color DARK_TICK = derivedColor(BACKGROUND, -48, -48, -48);
	public static Color TRACK = derivedColor(BACKGROUND, -16, -16, -16);
	
	private List<Track> listTracks = new ArrayList<Track>();
	private int iCurrentFrame = 49;
	private int iFrameWidth = 8;
	private int mouseX, mouseY;
	private boolean bMove;
	
	private Header header = new Header(this);
	
	private static Color derivedColor(Color c, int r, int g, int b) {
		r += c.getRed();
		g += c.getGreen();
		b += c.getBlue();
		if (r < 0) r = 0;
		if (g < 0) g = 0;
		if (b < 0) b = 0;
		if (r > 255) r = 255;
		if (g > 255) g = 255;
		if (b > 255) b = 255;
		return new Color(r, g, b);
	}
	
	public TimelineEditor() {
		setWheelScrollingEnabled(false);
		TrackView trackView = new TrackView(this);
//		trackView.setOpaque(true);
		setViewportView(trackView);
		setColumnHeaderView(new Ruler(this));
		setRowHeaderView(header);
//		getColumnHeader().setBackground(Color.GRAY);
		getViewport().setBackground(SHADOW);
		setCorner(UPPER_LEFT_CORNER, new Corner(this));
		setCorner(LOWER_LEFT_CORNER, new JComponent() {
			public void paint(Graphics g) {
				super.paint(g);
				g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
				g.drawLine(0, 0, getWidth() - 1, 0);
				g.setColor(UIManager.getColor("ScrollBar.shadow"));
				g.drawLine(0, 1, getWidth() - 1, 1);
			}
		});
		setCorner(UPPER_RIGHT_CORNER, new JComponent() {
			public void paint(Graphics g) {
				super.paint(g);
				g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
				g.drawLine(0, 0, 0, getHeight() - 1);
				g.setColor(UIManager.getColor("ScrollBar.shadow"));
				g.drawLine(1, 0, 1, getHeight() - 1);
			}
		});
		setCorner(LOWER_RIGHT_CORNER, new JComponent() {
			public void paint(Graphics g) {
				super.paint(g);
				g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
				g.drawLine(0, 0, getWidth() - 1, 0);
				g.drawLine(0, 0, 0, getHeight() - 1);
				g.setColor(UIManager.getColor("ScrollBar.shadow"));
				g.drawLine(1, 1, getWidth() - 1, 1);
				g.drawLine(1, 1, 1, getHeight() - 1);
			}
		});
	}
	
	public void setCursor(Cursor cursor) {
		if (currentCursor != cursor) {
			currentCursor = cursor;
			super.setCursor(cursor);
		}
	}
	
	void expandTrack(Track track, boolean expand) {
		if (track.isExpanded() == expand)
			return;
		track.expand(expand);
		revalidate();
		((JComponent) getViewport().getView()).revalidate();
		((JComponent) getRowHeader().getView()).revalidate();
		repaint();
	}
	
	void expandAll(boolean expand) {
		for (Track track : listTracks)
			track.expand(expand);
		revalidate();
		((JComponent) getViewport().getView()).revalidate();
		((JComponent) getRowHeader().getView()).revalidate();
		repaint();
	}
	
	void resetTracks() {
		for (Track track : listTracks)
			track.setExpandedHeight(AvarTrack.EXPANDED_HEIGHT);
		revalidate();
		((JComponent) getViewport().getView()).revalidate();
		((JComponent) getRowHeader().getView()).revalidate();
		repaint();
	}
	
	public void setAnimObject(AnimObject animObject) {
		listTracks.clear();
		listTracks.add(new HeaderTrack(this, animObject.getName(), -12));
		MotionCurveSet mcs = MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
		for (MotionCurve curve : mcs.motionCurveList) {
			if (curve instanceof MotionCurve.Float)
				listTracks.add(new AvarTrack(this, (MotionCurve.Float) curve));
			else if (curve instanceof MotionCurve.Color3f)
				listTracks.add(new ColorTrack(this, (MotionCurve.Color3f) curve));
			else 
				listTracks.add(new Track(this, curve));
		}
		if (animObject instanceof AnimModel) {
			if (((AnimModel) animObject).getModel().getMorphList().size() > 0)
				listTracks.add(new HeaderTrack(this, "Morphs", -4));
			for (Iterator it = ((AnimModel) animObject).getModel().getMorphList().iterator(); it.hasNext(); ) {
				listTracks.add(new AvarTrack(this, ((MotionCurveSet.Model) mcs).morph((Morph) it.next())));
			}
			if (((AnimModel) animObject).getModel().getBoneSet().size() > 0)
				listTracks.add(new HeaderTrack(this, "Bones", -4));
			for (Iterator it = ((AnimModel) animObject).getModel().getBoneSet().iterator(); it.hasNext(); ) {
				Bone bone = (Bone) it.next();
				if (bone.getParentBone() == null)
					recursiveAddBoneDofs(bone, 0, (MotionCurveSet.Model) mcs);
			}
		}
		header.createButtons();
		revalidate();
		((JComponent) getViewport().getView()).revalidate();
		((JComponent) getRowHeader().getView()).revalidate();
		repaint();
	}
	
	private void recursiveAddBoneDofs(Bone bone, int level, MotionCurveSet.Model mcs) {
		int n = bone.getDofs().size();
		MotionCurve.Float[] curves = new MotionCurve.Float[n];
		for (int i = 0; i < n; i++)
			curves[i] = mcs.morph((Morph) bone.getDofs().get(i));
		listTracks.add(new BoneTrack(this, curves, bone, level));
		for (Iterator itBones = bone.getChildBones().iterator(); itBones.hasNext(); ) {
			recursiveAddBoneDofs((Bone) itBones.next(), level + 1, mcs);
		}
	}
	
	public int getFrameWidth() {
		return iFrameWidth;
	}
	
	public void setFrameWidth(int frameWidth) {
		iFrameWidth = frameWidth;
	}
	
	public void setViewportView(Component c) {
		super.setViewportView(c);
	}
	
//	public static void main(String[] args) {
//		TimelineEditor tle = new TimelineEditor();
//		tle.test();
//		JFrame frame = new JFrame("Timeline Editor");
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.add(tle);
//		frame.setSize(640, 480);
//		frame.setVisible(true);
//	}
	
//	public void test() {
//		
////		frame.setLayout(new BorderLayout());
//////		table.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//////		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//////		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
////		table.setColumnSelectionAllowed(true);
////		table.setRowSelectionAllowed(true);
////		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
////		table.getTableHeader().setReorderingAllowed(false);
////		table.setDragEnabled(true);
////		
////		for (int i = 0; i < table.getColumnCount(); i++) {
////			table.getColumnModel().getColumn(i).setPreferredWidth(5);
////			table.getColumnModel().getColumn(i).setMinWidth(5);
////			table.getColumnModel().getColumn(i).setMaxWidth(5);
////			table.getColumnModel().getColumn(i).setResizable(false);
////		
//		
//		Model model = new Model();
////		final MotionCurve2.Float curve = MotionCurve2.createMorphCurve(morph);
//		
//		Random rnd = new Random();
//		for (int i = 0; i < 200; i++) {
//			Morph morph = new Morph("test" + i, model);
//			morph.setMin(-1);
//			morph.setMax(1);
//			curves[i] = MotionCurve.createMorphCurve(morph);
//			for (int j = 3; j < 24 * 60; j++) {
//				if (rnd.nextInt(20) == 0)
//					curves[i].addKey(new MotionKey.Float(j, curves[i].getMin() + rnd.nextFloat() * (curves[i].getMax() - curves[i].getMin())));
//			}
//			curves[i].addKey(new MotionKey.Float(0, 0));
//			curves[i].addKey(new MotionKey.Float(1, -1));
//			curves[i].addKey(new MotionKey.Float(2, 1));
//			listTracks.add(new AvarTrack(this, morph, curves[i]));
//		}
//			
//		final TimelineEditor tle = this;
//		
//		
//		//tle.getHorizontalScrollBar().get  setBackground(getBackground().darker());
//		
//	}

	public List<Track> getTracks() {
		return listTracks;
	}
	
	public int getCurrentFrame() {
		return iCurrentFrame;
	}
	
	public int getTracksHeight() {
		int h = 0;
		for (Track track: listTracks)
			h += track.getHeight();
		return h;
	}	
}
