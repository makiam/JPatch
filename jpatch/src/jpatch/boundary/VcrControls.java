package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.sound.sampled.*;
import javax.swing.*;

import jpatch.entity.*;


public class VcrControls extends JToolBar {
	private static enum State { STOP, PLAY, FAST_FWD, SLOW_FWD, FAST_BWD, SLOW_BWD, PLAY_BWD };
	private State state = State.STOP;
	
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
	
	private static JTextField textTime = new JTextField(" 00:00:00.00", 13);
	private static JTextField textFrame = new JTextField("      0", 8);
	
	private PlayThread playThread;
	
	public VcrControls() {
		setFloatable(false);
		setOpaque(false);
		setBorder(null);
//		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(buttonToStart);
		add(buttonFastRev);
		add(buttonPlayRev);
		add(buttonSlowBack);
		add(buttonPause);
		add(buttonSlowForward);
		add(buttonPlay);
		add(buttonFastForward);
		add(buttonToEnd);
		add(new JSeparator());
		add(textTime);
		add(textFrame);
		add(new JSeparator());
//		add(buttonPrevKey);
		add(buttonPrevFrame);
		add(buttonNextFrame);
//		add(buttonNextKey);
//		add(new JSeparator());
//		add(buttonAdd);
//		add(buttonDelete);
		textTime.setEditable(false);
		textTime.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textFrame.setFont(new Font("Monospaced", Font.PLAIN, 12));
		
		textFrame.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				float p = MainFrame.getInstance().getAnimation().getPosition();
				try {
					int i = Integer.parseInt(textFrame.getText().trim());
					System.out.println(i);
					float f = i;
					if (f >= MainFrame.getInstance().getAnimation().getStart() && f <= MainFrame.getInstance().getAnimation().getEnd())
						p = f;
				} catch (NumberFormatException e) { }
				System.out.println(p);
				setPosition(p);
			}
		});
//		textTime.setEditable(true);
//		textFrame.setEditable(true);
//		textTime.setBackground(Color.WHITE);
//		textFrame.setBackground(Color.WHITE);
		
	}
	
	public void stop() {
		if (playThread != null) playThread.stop = true;
		buttonPause.doClick();
	}
	
	public void setPosition(float position) {
		//tcd.setTimecode(position);
		//tcd.paint(tcd.getGraphics());
		Animation animation = MainFrame.getInstance().getAnimation();
		int rate = (int) animation.getFramerate();
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
		textFrame.setText(pad("" + (int) (position - animation.getStart()), ' ', 7));
//		MainFrame.getInstance().getAnimation().setPosition(position);
//		MainFrame.getInstance().getJPatchScreen().update_all();
	}
	
	private String pad(String string, char paddingCharacter, int length) {
		StringBuffer sb = new StringBuffer();
		for (int i = string.length(); i < length; i++) {
			sb.append(paddingCharacter);
		}
		sb.append(string);
		return sb.toString();
	}
	
	private static JToggleButton createToggleButton(Action action, ButtonGroup bg) {
		JToggleButton button = new JToggleButton(action);
		button.setPreferredSize(new Dimension(23, 23));
//		button.setFocusable(false);
		bg.add(button);
		return button;
	}
	
	private static JButton createButton(Action action) {
		JButton button = new JButton(action);
		button.setPreferredSize(new Dimension(23, 23));
//		button.setFocusable(false);
		return button;
	}
	
	private class PlayThread extends Thread {
		volatile boolean stop = false;
		Animation animation = MainFrame.getInstance().getAnimation();
		volatile float rate = animation.getFramerate();
		
		public void run() {
			long time = System.currentTimeMillis();
			float start = animation.getPosition();
			float oldFrame = -Float.MAX_VALUE;
			while (!stop) {
				float frame = Math.round(((float) (System.currentTimeMillis() - time)) / 1000 * rate + start);
				if (frame <= animation.getEnd() && frame >= animation.getStart()) {
					if (frame != oldFrame) {
						animation.setPosition(frame);
						MainFrame.getInstance().getJPatchScreen().update_all();
						oldFrame = frame;
					}
				}
				else {
					stop = true;
					buttonPause.doClick();
					if (frame > animation.getEnd()) {
						animation.setPosition(animation.getEnd());
						MainFrame.getInstance().getJPatchScreen().update_all();
					}
					if (frame < animation.getStart()) {
						animation.setPosition(animation.getStart());
						MainFrame.getInstance().getJPatchScreen().update_all();
					}
				}
			}
//			smartScrollPane.repaint();
			//mcd.updateTimeline();
		}
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
			if (state == State.PLAY) buttonPause.doClick();
			else {
				state = State.PLAY;
//				Clip clip = Animator.getInstance().getClip();
//				if (clip != null && !clip.isRunning()) clip.start();
//				System.out.println(clip);
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
//			Clip clip = Animator.getInstance().getClip();
//			if (clip != null && clip.isRunning()) clip.stop();
			if (state == State.PLAY_BWD) buttonPause.doClick();
			else {
				state = State.PLAY_BWD;
				playThread = new PlayThread();
				playThread.rate = -MainFrame.getInstance().getAnimation().getFramerate();
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
//			Clip clip = Animator.getInstance().getClip();
//			if (clip != null && clip.isRunning()) clip.stop();
			if (state == State.FAST_FWD) buttonPause.doClick();
			else {
				state = State.FAST_FWD;
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
//			Clip clip = Animator.getInstance().getClip();
//			if (clip != null && clip.isRunning()) clip.stop();
			if (state == State.FAST_BWD) buttonPause.doClick();
			else {
				state = State.FAST_BWD;
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
//			Clip clip = Animator.getInstance().getClip();
//			if (clip != null && clip.isRunning()) clip.stop();
			if (state == State.SLOW_FWD) buttonPause.doClick();
			else {
				state = State.SLOW_FWD;
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
//			Clip clip = Animator.getInstance().getClip();
//			if (clip != null && clip.isRunning()) clip.stop();
			if (state == State.SLOW_BWD) buttonPause.doClick();
			else {
				state = State.SLOW_BWD;
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
			state = State.STOP;
//			Clip clip = Animator.getInstance().getClip();
//			if (clip != null && clip.isRunning()) clip.stop();
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
			state = State.STOP;
			Animation animation = MainFrame.getInstance().getAnimation();
//			Clip clip = animation.getClip();
//			if (clip != null && clip.isRunning()) clip.stop();
			if (playThread != null) playThread.stop = true;
			float frame = Math.round(animation.getPosition());
			if (frame >= animation.getStart() + 1) {
				animation.setPosition(animation.getPosition() - 1);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
			buttonPause.doClick();
//			smartScrollPane.repaint();
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
			state = State.STOP;
			Animation animation = MainFrame.getInstance().getAnimation();
//			Clip clip = animator.getClip();
//			if (clip != null && clip.isRunning()) clip.stop();
			if (playThread != null) playThread.stop = true;
			float frame = Math.round(animation.getPosition());
			if (frame <= animation.getEnd() - 1) {
				animation.setPosition(animation.getPosition() + 1);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
			buttonPause.doClick();
//			smartScrollPane.repaint();
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
//			if (state != State.STOP) buttonPause.doClick();
//			MotionCurve2 curve = Animator.getInstance().getActiveCurve();
//			if (curve != null) {
//				MotionKey2 key = curve.getPrevKey(Animator.getInstance().getPosition());
//				Animator.getInstance().setActiveKey(key);
//				Animator.getInstance().setPosition(key.getPosition());
//				//curve.insertKeyAt(Animator.getInstance().getPosition());
////				smartScrollPane.repaint();
//				Animator.getInstance().rerenderViewports();
//			}
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
//			if (state != State.STOP) buttonPause.doClick();
//			MotionCurve2 curve = Animator.getInstance().getActiveCurve();
//			if (curve != null) {
//				MotionKey2 key = curve.getNextKey(Animator.getInstance().getPosition());
//				Animator.getInstance().setActiveKey(key);
//				Animator.getInstance().setPosition(key.getPosition());
//				//curve.insertKeyAt(Animator.getInstance().getPosition());
////				smartScrollPane.repaint();
//				Animator.getInstance().rerenderViewports();
//			}
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
//			if (state != State.STOP) buttonPause.doClick();
//			MotionCurve2 curve = Animator.getInstance().getActiveCurve();
//			if (curve != null) {
//				MotionKey2 key = curve.insertKeyAt(Animator.getInstance().getPosition());
//				Animator.getInstance().setActiveKey(key);
////				smartScrollPane.repaint();
//				Animator.getInstance().rerenderViewports();
//			}
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
//			if (state != State.STOP) buttonPause.doClick();
//			MotionKey2 key = Animator.getInstance().getActiveKey();
//			if (key != null) {
//				Animator.getInstance().getActiveCurve().removeKey(key);
////				smartScrollPane.repaint();
//				Animator.getInstance().rerenderViewports();
//			}
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
//			Clip clip = Animator.getInstance().getClip();
//			if (clip != null && clip.isRunning()) clip.stop();
			buttonPause.doClick();
			MainFrame.getInstance().getAnimation().setPosition(MainFrame.getInstance().getAnimation().getStart());
			MainFrame.getInstance().getJPatchScreen().update_all();
//			smartScrollPane.repaint();
//			Animator.getInstance().rerenderViewports();
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
//			Clip clip = Animator.getInstance().getClip();
//			if (clip != null && clip.isRunning()) clip.stop();
//			Animator.getInstance().setPosition(Animator.getInstance().getEnd());
			buttonPause.doClick();
			MainFrame.getInstance().getAnimation().setPosition(MainFrame.getInstance().getAnimation().getEnd());
			MainFrame.getInstance().getJPatchScreen().update_all();
//			smartScrollPane.repaint();
//			Animator.getInstance().rerenderViewports();
		}
	}
}
