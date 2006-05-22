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
	public static Cursor eastResizeCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
	public static Cursor westResizeCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
	public static Cursor verticalResizeCursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
	public static Cursor cornerResizeCursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
	private Cursor currentCursor = defaultCursor;
	
	public static Color SELECTED_BACKGROUND = new Color(192, 192, 255);
	public static Color BACKGROUND = new JPanel().getBackground();
	public static Color SHADOW = derivedColor(BACKGROUND, -96, -96, -96);
	public static Color LIGHT_SHADOW = derivedColor(BACKGROUND, -48, -48, -48);
	public static Color HIGHLIGHT = Color.WHITE;
	public static Color TICK = derivedColor(BACKGROUND, -64, -64, -32);
	public static Color DARK_TICK = derivedColor(BACKGROUND, -96, -96, -64);
	public static Color TRACK = derivedColor(BACKGROUND, -16, -16, -16);
	public static Color SELECTED_KEY = new Color(0, 255, 255);
	public static Color HIT_KEY = new Color(255, 255, 0);
	
	private List<Track> listTracks = new ArrayList<Track>();
	private List<SortedSet<MotionKey>> listSelections = new ArrayList<SortedSet<MotionKey>>();
	private int[] aiTrackBottom;
	private boolean bTrackHeightsValid;
	private int iTracksHeight;
	
	private int iCurrentFrame = (int) MainFrame.getInstance().getAnimation().getPosition();
	private int iFrameWidth = 8;
	private int mouseX, mouseY;
	private boolean bMove;
	private String strText = "";
	private Header header = new Header(this);
	private AnimObject animObject;
	
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
			private Font font = new Font("Sans-Serif", Font.PLAIN, 10);
			public void paint(Graphics g) {
				super.paint(g);
				g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
				g.drawLine(0, 0, getWidth() - 1, 0);
				g.setColor(UIManager.getColor("ScrollBar.shadow"));
				g.drawLine(0, 1, getWidth() - 1, 1);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g.setFont(font);
				g.setColor(Color.BLACK);
				g.drawString(strText, 4, 12);
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
	
	public void setCornerText(String text) {
		strText = text;
		getCorner(LOWER_LEFT_CORNER).repaint();
	}
	
	public Header getHeader() {
		return header;
	}
	
	void expandTrack(Track track, boolean expand) {
		if (track.isExpanded() == expand)
			return;
		track.expand(expand);
		bTrackHeightsValid = false;
		revalidate();
		((JComponent) getViewport().getView()).revalidate();
		((JComponent) getRowHeader().getView()).revalidate();
		repaint();
	}
	
	void expandAll(boolean expand) {
		for (Track track : listTracks)
			track.expand(expand);
		bTrackHeightsValid = false;
		revalidate();
		((Header) getRowHeader().getView()).setAllExpanded(expand);
		((JComponent) getViewport().getView()).revalidate();
		((JComponent) getRowHeader().getView()).revalidate();
		repaint();
	}
	
	void resetTracks() {
		for (Track track : listTracks)
			track.setExpandedHeight(AvarTrack.EXPANDED_HEIGHT);
		bTrackHeightsValid = false;
		revalidate();
		((JComponent) getViewport().getView()).revalidate();
		((JComponent) getRowHeader().getView()).revalidate();
		repaint();
	}
	
	public void setTrackHeight(int trackNo, int height) {
		listTracks.get(trackNo).setExpandedHeight(height);
		bTrackHeightsValid = false;
	}
	
	public void hideTrack(Track track, boolean hide) {
		track.setHidden(hide);
		bTrackHeightsValid = false;
	}
	
	public AnimObject getAnimObject() {
		return animObject;
	}
	
	public void setAnimObject(AnimObject animObject) {
		this.animObject = animObject;
		listTracks.clear();
		if (animObject != null) {
			listTracks.add(new HeaderTrack(this, animObject.getName(), -12, true));
			listTracks.add(new HeaderTrack(this, "Common tracks", -4, false));
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
					listTracks.add(new HeaderTrack(this, "Morphs", -4, false));
				for (Iterator it = ((AnimModel) animObject).getModel().getMorphList().iterator(); it.hasNext(); ) {
					listTracks.add(new AvarTrack(this, ((MotionCurveSet.Model) mcs).morph((Morph) it.next())));
				}
				if (((AnimModel) animObject).getModel().getBoneSet().size() > 0)
					listTracks.add(new HeaderTrack(this, "Bones", -4, false));
				for (Iterator it = ((AnimModel) animObject).getModel().getBoneSet().iterator(); it.hasNext(); ) {
					Bone bone = (Bone) it.next();
					if (bone.getParentBone() == null)
						recursiveAddBoneDofs(bone, 0, (MotionCurveSet.Model) mcs);
				}
			}
		}
		listSelections.clear();
		for (Track track : listTracks)
			listSelections.add(new TreeSet<MotionKey>());
		header.createButtons();
		aiTrackBottom = new int[listTracks.size()];
		computeHeights();
		revalidate();
		((TrackView) getViewport().getView()).reset();
		((JComponent) getViewport().getView()).revalidate();
		((JComponent) getRowHeader().getView()).revalidate();
		repaint();
	}
	
	private void recursiveAddBoneDofs(Bone bone, int level, MotionCurveSet.Model mcs) {
		int n = bone.getDofs().size();
		MotionCurve.Float[] curves = new MotionCurve.Float[n];
		RotationDof[] dofs = new RotationDof[n];
		for (int i = 0; i < n; i++) {
			dofs[i] = bone.getDof(i);
			curves[i] = mcs.morph(dofs[i]);
		}
		listTracks.add(new BoneTrack(this, dofs, curves, bone, level));
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
	
//	public void setViewportView(Component c) {
//		super.setViewportView(c);
//	}
	
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
	
	public void showCurrentFrame() {
		int x = iCurrentFrame * iFrameWidth + iFrameWidth / 2;
		Rectangle r = getViewport().getViewRect();
		if (x > r.x + r.width - 1 * iFrameWidth) {
			getHorizontalScrollBar().setValue(x - 2 * iFrameWidth);
			iCurrentFrame = iCurrentFrame;
			return;
		}
		if (x < r.x + 0 * iFrameWidth) {
			getHorizontalScrollBar().setValue(x - r.width + 2 * iFrameWidth);
			iCurrentFrame = iCurrentFrame;
			return;
		}
	}
	
	public void setCurrentFrame(int frame) {
//		MainFrame.getInstance().getAnimation().setPosition(frame);
//		MainFrame.getInstance().getJPatchScreen().update_all();
//		int start = (int) MainFrame.getInstance().getAnimation().getStart();
		int x = (iCurrentFrame - (int) MainFrame.getInstance().getAnimation().getStart()) * iFrameWidth + iFrameWidth / 2;
		iCurrentFrame = frame;
		getViewport().getView().repaint(x - 5, 0, 11, getViewport().getView().getHeight());
		getColumnHeader().getView().repaint(x - 5, 0, 11, getColumnHeader().getView().getHeight());
		x = (iCurrentFrame - (int) MainFrame.getInstance().getAnimation().getStart()) * iFrameWidth + iFrameWidth / 2;
		getViewport().getView().repaint(x - 5, 0, 11, getViewport().getView().getHeight());
		getColumnHeader().getView().repaint(x - 5, 0, 11, getColumnHeader().getView().getHeight());
	}
	
	private void computeHeights() {
		iTracksHeight = 0;
		for (int i = 0; i < listTracks.size(); i++) {
			Track track = listTracks.get(i);
			iTracksHeight += track.getHeight();
			aiTrackBottom[i] = iTracksHeight;
		}
		bTrackHeightsValid = true;
	}
	
	public int getTracksHeight() {
		if (!bTrackHeightsValid)
			computeHeights();
		return iTracksHeight;
	}	
	
	public int getTrackBottom(int i) {
		if (!bTrackHeightsValid)
			computeHeights();
		return aiTrackBottom[i];
	}
	
	public int getTrackTop(int i) {
		if (i == 0)
			return 0;
		if (!bTrackHeightsValid)
			computeHeights();
		return aiTrackBottom[i - 1];
	}
}
