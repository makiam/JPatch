package jpatch.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import jpatch.boundary.MainFrame;
import jpatch.boundary.settings.Settings;
import jpatch.entity.Animation;

public class AnimationRenderer {
	
	public void testShowDisplay() {
		try {
			Animation anim = MainFrame.getInstance().getAnimation();
			Process p = Runtime.getRuntime().exec("watch ls");
			final ProgressDisplay progressDisplay = new ProgressDisplay((int) anim.getStart(), (int) anim.getEnd(), p.getInputStream());
			progressDisplay.setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static class ProgressDisplay extends JDialog {
		private JProgressBar progressBar;
		private Image image;
		private JTextArea console = new JTextArea(10, 50);
		private Dimension minDim = new Dimension(0, 0);
		private Dimension dimension = new Dimension(Settings.getInstance().export.imageWidth, Settings.getInstance().export.imageHeight);
		private JPanel imagePanel = new JPanel() {
			@Override
			public Dimension getMaximumSize() {
				return dimension;
			}
			@Override
			public Dimension getMinimumSize() {
				return minDim;
			}
			@Override
			public Dimension getPreferredSize() {
				return dimension;
			}
			@Override
			public void paintComponent(Graphics g) {
				if (image != null)
					g.drawImage(image, 0, 0, null);
			}
		};
		private JButton buttonAbort = new JButton("Abort");
		private volatile boolean scroll = true;
		ProgressDisplay(int start, int end, final InputStream in) throws IOException {
			super(MainFrame.getInstance(), true);
			console.setEditable(false);
			console.setBackground(Color.WHITE);
			console.setFont(new Font("monospaced", Font.PLAIN, 12));
			progressBar = new JProgressBar(start, end);
			progressBar.setBorder(new TitledBorder("Progress"));
			imagePanel.setBorder(new TitledBorder("Output image"));
			getContentPane().setLayout(new BorderLayout());
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
			splitPane.setOneTouchExpandable(true);
			JScrollPane scrollPane = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setBorder(new TitledBorder("Renderer output console"));
			splitPane.add(imagePanel);
			splitPane.add(scrollPane);
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(buttonAbort);
			getContentPane().add(progressBar, BorderLayout.NORTH);
			getContentPane().add(splitPane, BorderLayout.CENTER);
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			pack();
			new Thread() {
				@Override
				public void run() {
					final byte[] buffer = new byte[1024];
					try {
						for (;;) {
							final int bytesRead = in.read(buffer);
							if (bytesRead == -1)
								break;
							final String str = new String(buffer, 0, bytesRead);
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									console.append(str);
									if (scroll)
										console.setCaretPosition(console.getDocument().getLength());
								}
							});
						}	
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
			console.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.isPopupTrigger())
						showPopup(e);
				}
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger())
						showPopup(e);
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					if (e.isPopupTrigger())
						showPopup(e);
				}
				private void showPopup(MouseEvent e) {
					JPopupMenu popup = new JPopupMenu();
					JCheckBoxMenuItem miScroll = new JCheckBoxMenuItem("autoscroll", scroll);
					JMenuItem miClear = new JMenuItem("clear console");
					miScroll.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							synchronized(ProgressDisplay.this) {
								scroll = !scroll;
							}
						}
					});
					miClear.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							console.setText("");
						}
					});
					popup.add(miScroll);
					popup.add(miClear);
					popup.show(console, e.getX(), e.getY());
				}
			});
		}
	}
}
