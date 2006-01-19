package jpatch.boundary.timeline;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jpatch.entity.*;

public class TimelineEditor extends JScrollPane {

//	private final ImageIcon ZOOM_IN = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/zoom_in.png"));
//	private final ImageIcon ZOOM_OUT = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/zoom_out.png"));
//	private JButton buttonZoomInH = new JButton(ZOOM_IN);
//	private JButton buttonZoomOutH = new JButton(ZOOM_OUT);
//	private JButton buttonZoomInV = new JButton(ZOOM_IN);
//	private JButton buttonZoomOutV = new JButton(ZOOM_OUT);
	
	private final MotionCurve.Float[] curves = new MotionCurve.Float[20];
	private List<Track> listTracks = new ArrayList<Track>();
	private int iCurrentFrame = 49;
	private int iFrameWidth = 8;
	
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
//		setAutoscrolls(true);
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
		for (int i = 0; i < 20; i++) {
			Morph morph = new Morph("test" + i, model);
			morph.setMin(-1);
			morph.setMax(1);
			curves[i] = MotionCurve.createMorphCurve(morph);
			for (int j = 3; j < 24 * 60; j++) {
				if (rnd.nextInt(20) == 0)
					curves[i].addKey(new MotionKey.Float(j, curves[i].getMin() + rnd.nextFloat() * (curves[i].getMax() - curves[i].getMin())));
			}
			curves[i].addKey(new MotionKey.Float(0, 0));
			curves[i].addKey(new MotionKey.Float(1, -1));
			curves[i].addKey(new MotionKey.Float(2, 1));
			listTracks.add(new AvarTrack(this, morph, curves[i]));
		}
			
		final TimelineEditor tle = this;
		
		TrackView display = new TrackView(tle);
		display.setOpaque(false);
		tle.setViewportView(display);
		
		JComponent columnHeader = new Ruler(tle);
		tle.setColumnHeaderView(columnHeader);
		System.out.println(tle.getColumnHeader());
		JComponent rowHeader = new Header(tle);
		tle.setRowHeaderView(rowHeader);
		frame.add(tle);
		frame.setSize(640, 480);
		frame.setVisible(true);
	}

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
