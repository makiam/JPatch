package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*;
import buoy.widget.*;
import buoy.event.*;
import jpatch.entity.*;

public class MotionCurveEditor extends BorderContainer {
	
	private static final int STOP = 0;
	private static final int PLAY = 1;
	private static final int FAST_FWD = 2;
	private static final int SLOW_FWD = 3;
	private static final int FAST_BWD = 4;
	private static final int SLOW_BWD = 5;
	private static final int PLAY_BWD = 6;
	//private static final int STOP = 0;
	//private static final int STOP = 0;
	
	private int iState = STOP;
	
	private RowContainer statusbar = new RowContainer();
	
	private ButtonGroup vcrGroup = new ButtonGroup();
	private JToggleButton buttonToStart = createToggleButton(new ToStartAction(), vcrGroup);
	private JToggleButton buttonFastRev = createToggleButton(new FastBackwardAction(), vcrGroup);
	private JToggleButton buttonPlayRev = createToggleButton(new PlayBackwardAction(), vcrGroup);
	private JToggleButton buttonSlowBack = createToggleButton(new SlowBackwardAction(), vcrGroup);
	private JToggleButton buttonPause = createToggleButton(new StopAction(), vcrGroup);
	private JToggleButton buttonSlowForward = createToggleButton(new SlowForwardAction(), vcrGroup);
	private JToggleButton buttonPlay = createToggleButton(new PlayAction(), vcrGroup);
	private JToggleButton buttonFastForward = createToggleButton(new FastForwardAction(), vcrGroup);
	private JToggleButton buttonToEnd = createToggleButton(new ToEndAction(), vcrGroup);
	
	private JButton buttonAdd = createButton(new AddKeyAction());
	private JButton buttonDelete = createButton(new DeleteKeyAction());
	private JButton buttonPrevKey = createButton(new PrevKeyAction());
	private JButton buttonNextKey = createButton(new NextKeyAction());
	private JToggleButton buttonPrevFrame = createToggleButton(new PrevFrameAction(), vcrGroup);
	private JToggleButton buttonNextFrame = createToggleButton(new NextFrameAction(), vcrGroup);
	
	//private TimeCodeDisplay tcd = new TimeCodeDisplay();
	//private ColumnContainer curvesPanel = new ColumnContainer();
	//private BScrollPane scrollPane = new BScrollPane(curvesPanel);
	private static BTextField textTime = new BTextField(" 00:00:00.00", 13);
	private static BTextField textFrame = new BTextField("      1", 8);
	
	private MotionCurveDisplay mcd;
	SmartScrollPane smartScrollPane = new SmartScrollPane();
	
	private PlayThread playThread;

	private static JToggleButton createToggleButton(Action action, ButtonGroup bg) {
		JToggleButton button = new JToggleButton(action);
		button.setPreferredSize(new Dimension(27, 19));
//		button.setFocusable(false);
		bg.add(button);
		return button;
	}
	
	private static JButton createButton(Action action) {
		JButton button = new JButton(action);
		button.setPreferredSize(new Dimension(27, 19));
//		button.setFocusable(false);
		return button;
	}
	
//	private static JButton createImageButton(String filename) {
//		JButton button = new JButton(new ImageIcon(ClassLoader.getSystemResource(filename)));
//		button.setPreferredSize(new Dimension(27, 19));
//		button.setFocusable(false);
//		return button;
//	}
	
	public MotionCurveEditor() {
		statusbar.add(new AWTWidget(buttonToStart));
		statusbar.add(new AWTWidget(buttonFastRev));
		statusbar.add(new AWTWidget(buttonPlayRev));
		statusbar.add(new AWTWidget(buttonSlowBack));
		statusbar.add(new AWTWidget(buttonPause));
		statusbar.add(new AWTWidget(buttonSlowForward));
		statusbar.add(new AWTWidget(buttonPlay));
		statusbar.add(new AWTWidget(buttonFastForward));
		statusbar.add(new AWTWidget(buttonToEnd));
		//statusbar.add(new AWTWidget(tcd));
		statusbar.add(new AWTWidget(new JSeparator()));
		statusbar.add(textTime);
		statusbar.add(textFrame);
		statusbar.add(new AWTWidget(new JSeparator()));
		statusbar.add(new AWTWidget(buttonPrevKey));
		statusbar.add(new AWTWidget(buttonPrevFrame));
		statusbar.add(new AWTWidget(buttonNextFrame));
		statusbar.add(new AWTWidget(buttonNextKey));
		statusbar.add(new AWTWidget(new JSeparator()));
		statusbar.add(new AWTWidget(buttonAdd));
		statusbar.add(new AWTWidget(buttonDelete));
		
		textTime.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textFrame.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textTime.setEditable(false);
		textFrame.setEditable(false);
		textFrame.addEventLink(MouseClickedEvent.class, this, "enterFrameNumber");
		textTime.setBackground(Color.WHITE);
		textFrame.setBackground(Color.WHITE);
		//buttonDelete.addEventLink(CommandEvent.class, new Object() {
		//	void processEvent() {
		//		MotionCurve.MotionKey mk = mcd.getSelectedKey();
		//		if (mk != null) {
		//			if (mcd.selectedCamera == null) mk.remove();
		//			else {
		//				MotionCurve.PointCurve mpc = Animator.getInstance().getPositionCurveFor(mcd.selectedCamera);
		//				mpc.removePoint(mk.position);
		//				MotionCurve.RotationCurve mrc = Animator.getInstance().getRotationCurveFor(mcd.selectedCamera);
		//				mrc.removeRotation(mk.position);
		//			}
		//			repaint();
		//		}
		//	}
		//});
		//buttonFrameForward.addEventLink(CommandEvent.class, new Object() {
		//	void processEvent() {
		//		Clip clip = Animator.getInstance().getClip();
		//		if (clip != null && clip.isRunning()) clip.stop();
		//		if (playThread == null || playThread.stopped) {
		//			double fr = Animator.getInstance().getFrameRate();
		//			double t = ((double) Animator.getInstance().getPosition()) / 10000.0;
		//			double s = ((double) Animator.getInstance().getStart()) / 10000.0;
		//			double df = (t - s) * fr;
		//			double frame = Math.floor(df + 0.1 / fr) + 1;
		//			int tt = Animator.getInstance().getStart() + (int) (frame / fr * 10000.0);
		//			Animator.getInstance().setPosition(tt);
		//			repaint();
		//		}
		//	}
		//});
		//buttonFrameBack.addEventLink(CommandEvent.class, new Object() {
		//	void processEvent() {
		//		Clip clip = Animator.getInstance().getClip();
		//		if (clip != null && clip.isRunning()) clip.stop();
		//		if (playThread == null || playThread.stopped) { 
		//			double fr = Animator.getInstance().getFrameRate();
		//			double t = ((double) Animator.getInstance().getPosition()) / 10000.0;
		//			double s = ((double) Animator.getInstance().getStart()) / 10000.0;
		//			double df = (t - s) * fr;
		//			double frame = Math.floor(df - 0.1 / fr);
		//			int tt = Animator.getInstance().getStart() + (int) (frame / fr * 10000.0);
		//			Animator.getInstance().setPosition(tt);
		//			repaint();
		//		}
		//	}
		//});
		//buttonToStart.addEventLink(CommandEvent.class, new Object() {
		//	void processEvent() {
		//		Clip clip = Animator.getInstance().getClip();
		//		if (clip != null && clip.isRunning()) clip.stop();
		//		if (playThread == null || playThread.stopped) {
		//			Animator.getInstance().setPosition(Animator.getInstance().getStart());
		//			repaint();
		//		}
		//	}
		//});
		//buttonToEnd.addEventLink(CommandEvent.class, new Object() {
		//	void processEvent() {
		//		Clip clip = Animator.getInstance().getClip();
		//		if (clip != null && clip.isRunning()) clip.stop();
		//		if (playThread == null || playThread.stopped) {
		//			Animator.getInstance().setPosition(Animator.getInstance().getEnd());
		//			repaint();
		//		}
		//	}
		//});
		//buttonPlay.setAction(new AbstractAction() {
		//	//super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/add.png")));
		//	
		//		//putValue(Action.SHORT_DESCRIPTION,"Play");
		//	
		//	public void actionPerformed(ActionEvent e) {
		//		Clip clip = Animator.getInstance().getClip();
		//		if (clip != null && !clip.isRunning()) clip.start();
		//		if (playThread != null && !playThread.stop) playThread.stop = true;
		//		playThread = new PlayThread();
		//		playThread.start();
		//	}
		//});
		//buttonFastForward.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent e) {
		//		Clip clip = Animator.getInstance().getClip();
		//		if (clip != null && clip.isRunning()) clip.stop();
		//		if (playThread != null && !playThread.stop) playThread.stop = true;
		//		playThread = new PlayThread();
		//		playThread.rate = 300;
		//		playThread.start();
		//	}
		//});
		//buttonSlowForward.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent e) {
		//		Clip clip = Animator.getInstance().getClip();
		//		if (clip != null && clip.isRunning()) clip.stop();
		//		if (playThread != null && !playThread.stop) playThread.stop = true;
		//		playThread = new PlayThread();
		//		playThread.rate = 3;
		//		playThread.start();
		//	}
		//});
		//buttonFastForward.addEventLink(CommandEvent.class, new Object() {
		//	void processEvent() {
		//		Clip clip = Animator.getInstance().getClip();
		//		if (clip != null && clip.isRunning()) clip.stop();
		//		if (playThread == null || playThread.stopped) {
		//			playThread = new PlayThread();
		//			playThread.factor = 10;
		//			playThread.start();
		//		} else playThread.factor = 10;
		//	}
		//});
		//buttonPlayRev.addEventLink(CommandEvent.class, new Object() {
		//	void processEvent() {
		//		Clip clip = Animator.getInstance().getClip();
		//		if (clip != null && clip.isRunning()) clip.stop();
		//		if (playThread == null || playThread.stopped) {
		//			playThread = new PlayThread();
		//			playThread.factor = -1;
		//			playThread.start();
		//		} else playThread.factor = -1;
		//	}
		//});
		//buttonFastRev.addEventLink(CommandEvent.class, new Object() {
		//	void processEvent() {
		//		Clip clip = Animator.getInstance().getClip();
		//		if (clip != null && clip.isRunning()) clip.stop();
		//		if (playThread == null || playThread.stopped) {
		//			playThread = new PlayThread();
		//			playThread.factor = -10;
		//			playThread.start();
		//		} else playThread.factor = -10;
		//	}
		//});
		//buttonPause.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent e) {
		//		Clip clip = Animator.getInstance().getClip();
		//		if (clip != null && clip.isRunning()) clip.stop();
		//		if (playThread != null) playThread.stop = true;
		//	}
		//});
		
		//tcd.setTimecode(0);
		//curvesPanel.setPreferredSize(new Dimension(800,600));
		add(statusbar, SOUTH);
		mcd = new MotionCurveDisplay(smartScrollPane);
		smartScrollPane.setVirtualCanvas(mcd);
		add(new AWTWidget(smartScrollPane), CENTER);
		//vScrollBar.setOrientation(BScrollBar.VERTICAL);
		//vScrollPanel.add(vScrollBar, CENTER);
		//vZoomPanel.add(buttonZoomInV);
		//vZoomPanel.add(buttonZoomOutV);
		//vScrollPanel.add(vZoomPanel, NORTH);
		//curvesPanel.add(vScrollPanel, new Rectangle(0,0));
		//
		////placeholder.setPreferredSize(new Dimension(15,15));
		//hScrollBar.setOrientation(BScrollBar.HORIZONTAL);
		//hScrollPanel.add(hScrollBar, 2, 0);
		//hScrollPanel.add(buttonZoomInH, 0, 0);
		//hScrollPanel.add(buttonZoomOutH, 1, 0);
		////hScrollPanel.add(hZoomPanel, WEST);
		////hScrollPanel.add(placeholder, EAST);
		//curvesPanel.add(hScrollPanel, new Rectangle(0,0));
		//
		//motionCurveDisplay.setBackground(Color.BLUE);
		//motionCurveDisplay.setPreferredSize(new Dimension(800,600));
		//curvesPanel.add(motionCurveDisplay, new Rectangle(0,0));
		//
		//layoutScrollPanel();
		//add(curvesPanel, CENTER);
		//layoutChildren();
	}
	
	public MotionCurveDisplay getMotionCurveDisplay() {
		return mcd;
	}
	
	public void stop() {
		if (playThread != null) playThread.stop = true;
		buttonPause.doClick();
	}
	
	private void enterFrameNumber() {
		try {
			buttonPause.doClick();
			String s = JOptionPane.showInputDialog(textFrame.getComponent(), "Go to frame number:");
			if (s == null) return;
			float frame = (float) Integer.parseInt(s) - 1;
			Animator animator = Animator.getInstance();
			if (frame < animator.getStart()) frame = animator.getStart();
			if (frame > animator.getEnd()) frame = animator.getEnd();
			animator.setPosition((float) frame);
			smartScrollPane.repaint();
		} catch (NumberFormatException e) {
		}
	}
	
	public void setPosition(float position) {
		//tcd.setTimecode(position);
		//tcd.paint(tcd.getGraphics());
		int rate = (int) Animator.getInstance().getFramerate();
		boolean bMinus = (position < 0);
		int t = (int) position;
		if (t < 0) t = -t;
		int h = t / 3600 / rate;
		t -= h * 3600 * rate;
		int m = t / 60 / rate;
		t -= m * 60 * rate;
		int s = t / rate;
		t -= s * rate;
		StringBuffer timecode = new StringBuffer();
		if (bMinus) timecode.append('-');
		else timecode.append(' ');
		timecode.append(pad("" + h, '0', 2)).append(':').append(pad("" + m, '0', 2)).append(':').append(pad("" + s, '0', 2)).append('.').append(pad("" + t, '0', 2));
		textTime.setText(timecode.toString());
		textFrame.setText(pad("" + (int) (position - Animator.getInstance().getStart() + 1), ' ', 7));
		
	}
	
	private class PlayThread extends Thread {
		volatile boolean stop = false;
		volatile float rate = Animator.getInstance().getFramerate();
		Animator animator = Animator.getInstance();
		public void run() {
			long time = System.currentTimeMillis();
			float start = animator.getPosition();
			float oldFrame = -Float.MAX_VALUE;
			while (!stop) {
				float frame = Math.round(((float) (System.currentTimeMillis() - time)) / 1000 * rate + start);
				if (frame <= animator.getEnd() && frame >= animator.getStart()) {
					if (frame != oldFrame) {
						animator.setPosition(frame);
						oldFrame = frame;
					}
				}
				else {
					stop = true;
					buttonPause.doClick();
					if (frame > animator.getEnd()) animator.setPosition(animator.getEnd());
					if (frame < animator.getStart()) animator.setPosition(animator.getStart());
				}
			}
			smartScrollPane.repaint();
			//mcd.updateTimeline();
		}
	}
	
	//private void layoutScrollPanel() {
	//	Rectangle mainBounds = curvesPanel.getBounds();
	//	int width = mainBounds.width;
	//	int height = mainBounds.height;
	//	curvesPanel.setChildBounds(motionCurveDisplay, new Rectangle(width - 15, height - 15));
	//	curvesPanel.setChildBounds(vScrollPanel, new Rectangle(width - 15, 0, 15, height - 15));
	//	curvesPanel.setChildBounds(hScrollPanel, new Rectangle(0, height - 15, width - 15, 15));
	//}
	
	//public static void main(String[] args) {
	//	BFrame frame = new BFrame();
	//	frame.addEventLink(WindowClosingEvent.class, frame, "dispose");
	//	MotionCurveEditor mce = new MotionCurveEditor();
	//	//MotionCurve mc1 = new MotionCurve();
	//	//mc1.randomFill();
	//	//MotionCurve mc2 = new MotionCurve();
	//	//mc2.randomFill();
	//	//MotionCurve mc3 = new MotionCurve();
	//	//mc3.randomFill();
	//	//MotionCurveDisplay mcd1 = new MotionCurveDisplay(mc1);
	//	//MotionCurveDisplay mcd2 = new MotionCurveDisplay(mc2);
	//	//MotionCurveDisplay mcd3 = new MotionCurveDisplay(mc3);
	//	//mce.curvesPanel.add(new AWTWidget(mcd1));
	//	//mce.curvesPanel.add(new AWTWidget(mcd2));
	//	//mce.curvesPanel.add(new AWTWidget(mcd3));
	//	frame.setContent(mce);
	//	frame.setBounds(new Rectangle(800,600));
	//	frame.setVisible(true);
	//}
	private String pad(String string, char paddingCharacter, int length) {
		StringBuffer sb = new StringBuffer();
		for (int i = string.length(); i < length; i++) {
			sb.append(paddingCharacter);
		}
		sb.append(string);
		return sb.toString();
	}
	
	private class PlayAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		PlayAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/play.png")));
			putValue(Action.SHORT_DESCRIPTION,"Play");
		}
		public void actionPerformed(ActionEvent e) {
			if (playThread != null) playThread.stop = true;
			if (iState == PLAY) buttonPause.doClick();
			else {
				iState = PLAY;
				Clip clip = Animator.getInstance().getClip();
				if (clip != null && !clip.isRunning()) clip.start();
				System.out.println(clip);
				playThread = new PlayThread();
				playThread.start();
			}
		}
	}
	
	private class PlayBackwardAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		PlayBackwardAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/play_rev.png")));
			putValue(Action.SHORT_DESCRIPTION,"Play reverse");
		}
		public void actionPerformed(ActionEvent e) {
			if (playThread != null) playThread.stop = true;
			Clip clip = Animator.getInstance().getClip();
			if (clip != null && clip.isRunning()) clip.stop();
			if (iState == PLAY_BWD) buttonPause.doClick();
			else {
				iState = PLAY_BWD;
				playThread = new PlayThread();
				playThread.rate = -Animator.getInstance().getFramerate();
				playThread.start();
			}
		}
	}
	
	private class FastForwardAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		FastForwardAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/fast_forward.png")));
			putValue(Action.SHORT_DESCRIPTION,"Fast forward");
		}
		public void actionPerformed(ActionEvent e) {
			if (playThread != null) playThread.stop = true;
			Clip clip = Animator.getInstance().getClip();
			if (clip != null && clip.isRunning()) clip.stop();
			if (iState == FAST_FWD) buttonPause.doClick();
			else {
				iState = FAST_FWD;
				playThread = new PlayThread();
				playThread.rate = 300;
				playThread.start();
			}
		}
	}
	
	private class FastBackwardAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		FastBackwardAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/fast_rev.png")));
			putValue(Action.SHORT_DESCRIPTION,"Fast reverse");
		}
		public void actionPerformed(ActionEvent e) {
			if (playThread != null) playThread.stop = true;
			Clip clip = Animator.getInstance().getClip();
			if (clip != null && clip.isRunning()) clip.stop();
			if (iState == FAST_BWD) buttonPause.doClick();
			else {
				iState = FAST_BWD;
				playThread = new PlayThread();
				playThread.rate = -300;
				playThread.start();
			}
		}
	}
	
	private class SlowForwardAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		SlowForwardAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/slomo_fwd.png")));
			putValue(Action.SHORT_DESCRIPTION,"Slow motion");
		}
		public void actionPerformed(ActionEvent e) {
			if (playThread != null) playThread.stop = true;
			Clip clip = Animator.getInstance().getClip();
			if (clip != null && clip.isRunning()) clip.stop();
			if (iState == SLOW_FWD) buttonPause.doClick();
			else {
				iState = SLOW_FWD;
				playThread = new PlayThread();
				playThread.rate = 3;
				playThread.start();
			}
		}
	}
	
	private class SlowBackwardAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		SlowBackwardAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/slomo_back.png")));
			putValue(Action.SHORT_DESCRIPTION,"Slow reverse");
		}
		public void actionPerformed(ActionEvent e) {
			if (playThread != null) playThread.stop = true;
			Clip clip = Animator.getInstance().getClip();
			if (clip != null && clip.isRunning()) clip.stop();
			if (iState == SLOW_BWD) buttonPause.doClick();
			else {
				iState = SLOW_BWD;
				playThread = new PlayThread();
				playThread.rate = -3;
				playThread.start();
			}
		}
	}
	
	private class StopAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7458944508970534077L;
		StopAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/pause.png")));
			putValue(Action.SHORT_DESCRIPTION,"Stop");
		}
		public void actionPerformed(ActionEvent e) {
			iState = STOP;
			Clip clip = Animator.getInstance().getClip();
			if (clip != null && clip.isRunning()) clip.stop();
			if (playThread != null) playThread.stop = true;
		}
	}
	
	private class PrevFrameAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		PrevFrameAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/prev_frame.png")));
			putValue(Action.SHORT_DESCRIPTION,"Go to previous frame");
		}
		public void actionPerformed(ActionEvent e) {
			iState = STOP;
			Animator animator = Animator.getInstance();
			Clip clip = animator.getClip();
			if (clip != null && clip.isRunning()) clip.stop();
			if (playThread != null) playThread.stop = true;
			float frame = Math.round(animator.getPosition());
			if (frame >= animator.getStart() + 1) animator.setPosition(animator.getPosition() - 1);
			buttonPause.doClick();
			smartScrollPane.repaint();
		}
	}
	
	private class NextFrameAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		NextFrameAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/next_frame.png")));
			putValue(Action.SHORT_DESCRIPTION,"Go to next frame");
		}
		public void actionPerformed(ActionEvent e) {
			iState = STOP;
			Animator animator = Animator.getInstance();
			Clip clip = animator.getClip();
			if (clip != null && clip.isRunning()) clip.stop();
			if (playThread != null) playThread.stop = true;
			float frame = Math.round(animator.getPosition());
			if (frame <= animator.getEnd() - 1) animator.setPosition(animator.getPosition() + 1);
			buttonPause.doClick();
			smartScrollPane.repaint();
		}
	}
	
	private class PrevKeyAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		PrevKeyAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/prev_key.png")));
			putValue(Action.SHORT_DESCRIPTION,"Go to previous key");
		}
		public void actionPerformed(ActionEvent e) {
			if (iState != STOP) buttonPause.doClick();
			MotionCurve curve = Animator.getInstance().getActiveCurve();
			if (curve != null) {
				MotionKey key = curve.getPrevKey(Animator.getInstance().getPosition());
				Animator.getInstance().setActiveKey(key);
				Animator.getInstance().setPosition(key.getPosition());
				//curve.insertKeyAt(Animator.getInstance().getPosition());
				smartScrollPane.repaint();
				Animator.getInstance().rerenderViewports();
			}
		}
	}
	
	private class NextKeyAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		NextKeyAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/next_key.png")));
			putValue(Action.SHORT_DESCRIPTION,"Go to next key");
		}
		public void actionPerformed(ActionEvent e) {
			if (iState != STOP) buttonPause.doClick();
			MotionCurve curve = Animator.getInstance().getActiveCurve();
			if (curve != null) {
				MotionKey key = curve.getNextKey(Animator.getInstance().getPosition());
				Animator.getInstance().setActiveKey(key);
				Animator.getInstance().setPosition(key.getPosition());
				//curve.insertKeyAt(Animator.getInstance().getPosition());
				smartScrollPane.repaint();
				Animator.getInstance().rerenderViewports();
			}
		}
	}
	
	private class AddKeyAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		AddKeyAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/addkey.png")));
			putValue(Action.SHORT_DESCRIPTION,"Add key");
		}
		public void actionPerformed(ActionEvent e) {
			if (iState != STOP) buttonPause.doClick();
			MotionCurve curve = Animator.getInstance().getActiveCurve();
			if (curve != null) {
				MotionKey key = curve.insertKeyAt(Animator.getInstance().getPosition());
				Animator.getInstance().setActiveKey(key);
				smartScrollPane.repaint();
				Animator.getInstance().rerenderViewports();
			}
		}
	}
	
	private class DeleteKeyAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		DeleteKeyAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/delete.png")));
			putValue(Action.SHORT_DESCRIPTION,"Delete key");
		}
		public void actionPerformed(ActionEvent e) {
			if (iState != STOP) buttonPause.doClick();
			MotionKey key = Animator.getInstance().getActiveKey();
			if (key != null) {
				Animator.getInstance().getActiveCurve().removeKey(key);
				smartScrollPane.repaint();
				Animator.getInstance().rerenderViewports();
			}
		}
	}
	
	private class ToStartAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		ToStartAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/to_start.png")));
			putValue(Action.SHORT_DESCRIPTION,"Go to start");
		}
		public void actionPerformed(ActionEvent e) {
			if (playThread != null) playThread.stop = true;
			Clip clip = Animator.getInstance().getClip();
			if (clip != null && clip.isRunning()) clip.stop();
			buttonPause.doClick();
			Animator.getInstance().setPosition(Animator.getInstance().getStart());
			smartScrollPane.repaint();
			Animator.getInstance().rerenderViewports();
		}
	}
	
	private class ToEndAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		ToEndAction() {
			super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/to_end.png")));
			putValue(Action.SHORT_DESCRIPTION,"Go to end");
		}
		public void actionPerformed(ActionEvent e) {
			if (playThread != null) playThread.stop = true;
			Clip clip = Animator.getInstance().getClip();
			if (clip != null && clip.isRunning()) clip.stop();
			Animator.getInstance().setPosition(Animator.getInstance().getEnd());
			buttonPause.doClick();
			smartScrollPane.repaint();
			Animator.getInstance().rerenderViewports();
		}
	}
}
