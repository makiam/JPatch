package jpatch.renderer;

import inyo.RtInterface;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

import jpatch.auxilary.ReadTiff;
import jpatch.boundary.*;
import jpatch.boundary.settings.*;
import jpatch.entity.*;
import patterns.TextureParser;

public class AnimationRenderer {
	
	private Console console;
	private ProgressDisplay progressDisplay;
	private volatile Renderer renderer;
	private volatile Process process;
	private RenderExtension re = new RenderExtension(new String[] {
			"povray", "",
			"renderman", ""
	});
	private volatile boolean abort = false;
	private volatile boolean finished = false;
	private JButton buttonAbort = new JButton("Abort");
	
	public void renderFrames(final String prefix, final int first, final int last) {
		try {
			Animation anim = MainFrame.getInstance().getAnimation();
			progressDisplay = new ProgressDisplay(first, last);
			final NumberFormat numberFormat = new DecimalFormat("000000");
			/* synchronize to create memory barrier in EventDispatching thread */
			
			synchronized(this) {
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						/* synchronize to create memory barrier in rendering thread */
						synchronized(this) {
							for (int frame = first; frame <= last; frame++) {
								if (abort)
									return;
								progressDisplay.setProgress(frame);
								progressDisplay.setTitle("Rendering frame " + frame + ", going to " + last + " (" + (last - frame) + " frames to go)");
								renderFrame(frame, prefix + numberFormat.format(frame));
							};
							progressDisplay.setTitle("Finished, " + (last - first + 1) + " frames rendered.");
							progressDisplay.setProgress(last + 1);
							finished = true;
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									buttonAbort.setText("Close");
								}
							});
						}
					}
				}).start();
			}
			
			progressDisplay.setLocationRelativeTo(MainFrame.getInstance());
			progressDisplay.setVisible(true);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class ProgressDisplay extends JDialog {
		private JProgressBar progressBar;
		private BufferedImage image;
		private JPanel imagePanel = new JPanel() {
			private Dimension dim = new Dimension(Settings.getInstance().export.imageWidth, Settings.getInstance().export.imageHeight);
			@Override
			public Dimension getPreferredSize() {
				if (image != null)
					dim.setSize(image.getWidth(), image.getHeight());
				return dim;
			}

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (image != null)
					g.drawImage(image, (getWidth() - image.getWidth()) >> 1, (getHeight() - image.getHeight()) >> 1, null);
			}
		};
		
		private ActionListener abortActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (finished) {
					ProgressDisplay.this.dispose();
				} else if (JOptionPane.showConfirmDialog(ProgressDisplay.this, "Abort rendering?", "Abort", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
					abort();
					ProgressDisplay.this.dispose();
				}
			}
		};
		
		ProgressDisplay(int start, int end) throws IOException {
			super(MainFrame.getInstance(), true);
			console = new Console();
			buttonAbort.addActionListener(abortActionListener);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					if (finished) {
						ProgressDisplay.this.dispose();
					} else if (JOptionPane.showConfirmDialog(ProgressDisplay.this, "Abort rendering?", "Abort", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
						abort();
						ProgressDisplay.this.dispose();
					}
				}
			});
			progressBar = new JProgressBar(start, end + 1);
//			progressBar.setBorder(new TitledBorder("Progress"));
//			imagePanel.setBorder(new TitledBorder("Output image"));
			getContentPane().setLayout(new BorderLayout());
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
			splitPane.setOneTouchExpandable(true);
			JScrollPane scrollPane1 = new JScrollPane(imagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			JScrollPane scrollPane2 = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//			scrollPane.setBorder(new TitledBorder("Renderer output console"));
//			scrollPane1.setPreferredSize(new Dimension(720, 320));
//			scrollPane2.setPreferredSize(new Dimension(720, 220));
			splitPane.add(scrollPane1);
			splitPane.add(scrollPane2);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(buttonAbort);
			getContentPane().add(progressBar, BorderLayout.NORTH);
			getContentPane().add(splitPane, BorderLayout.CENTER);
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			setSize(750, 550);
			splitPane.setDividerLocation(300);	
		}
		
		private void setProgress(int frame) {
			progressBar.setValue(frame);
		}
		
		private void setImage(BufferedImage image) {
			this.image = image;
			imagePanel.validate();
			imagePanel.repaint();
		}
	}
	
	
	void loadImage(File imageFile) {
		if (abort)
			return;
		if (imageFile.exists()) {
			BufferedImage image;
			try {
				if (imageFile.getPath().endsWith(".tif") && !ImageIO.getImageReadersByFormatName("tiff").hasNext()) {
					image = (new ReadTiff()).loadImage(imageFile);
				}
				else image = ImageIO.read(imageFile);
				Graphics g = image.getGraphics();
				g.setColor(Color.BLACK);
				g.drawString(imageFile.getParent() + File.separator, 8, 16);
				g.drawString(imageFile.getName(), 8, 32);
				g.setColor(Color.WHITE);
				g.drawString(imageFile.getParent() + File.separator, 7, 15);
				g.drawString(imageFile.getName(), 7, 31);
				progressDisplay.setImage(image);
			} catch (Exception e) {
				Settings settings = Settings.getInstance();
				image = new BufferedImage(settings.export.imageWidth, settings.export.imageHeight, BufferedImage.TYPE_INT_RGB);
				Graphics g = image.getGraphics();
				g.setColor(Color.RED);
				g.drawString("Can't display image - error reading file", 8, 16);
				progressDisplay.setImage(image);
			}
		}
		else {
			Settings settings = Settings.getInstance();
			BufferedImage image = new BufferedImage(settings.export.imageWidth, settings.export.imageHeight, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.getGraphics();
			g.setColor(Color.RED);
			g.drawString("Can't display image - file not found", 8, 16);
			progressDisplay.setImage(image);
		}
	}
		
	public void renderFrame(int position, String frameName) {
		Settings settings = Settings.getInstance();
		//progressDisplay.clearText();
		//progressDisplay.show();
		/* output geometry to temporary file */
		Animation anim = MainFrame.getInstance().getAnimation();
		anim.setPosition(position);
		MainFrame.getInstance().getTimelineEditor().setCurrentFrame(position);
		System.out.println("renderFrame " + frameName + " with " + settings.export.rendererToUse);
		switch (settings.export.rendererToUse) {
			case PREVIZ: {
				console.append("Rendering " + frameName + " using Pre-viz renderer.\n");
				console.append("Working directory is \"" + settings.export.workingDirectory + "\".\n");
				renderer = new PreVizRenderer(anim.getModels(), anim.getActiveCamera(), anim.getLights());
				Image image = ((PreVizRenderer) renderer).render();
				if (image != null) {
					File imageFile = new File(settings.export.workingDirectory, frameName + ".png");
					console.append("Done. Saving file \"" + imageFile.getName() + "\".\n");
					try {
						ImageIO.write((BufferedImage) image, "png", imageFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					progressDisplay.setImage((BufferedImage) image);
				} else {
					console.append("Unable to render image.\n");
				}
			}
			break;
			case INYO: {
//				console.clearText();
				console.append("Rendering " + frameName + " using Inyo.\n");
				console.append("Working directory is \"" + settings.export.workingDirectory + "\".\n");
				console.append("Invoking Inyo.\n");
				TextureParser.setTexturePath(settings.export.inyo.textureDirectory.getPath());
				renderer = new InyoRenderer3(anim.getModels(), anim.getActiveCamera(), anim.getLights());
				Image image = ((InyoRenderer3) renderer).render(new RtInterface());
				if (image != null) {
					File imageFile = new File(settings.export.workingDirectory, frameName + ".png");
					console.append("Done. Saving file \"" + imageFile.getName() + "\".\n");
					try {
						ImageIO.write((BufferedImage) image, "png", imageFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					loadImage(imageFile);
				} else {
					console.append("Unable to render image.\n");
				}
				//RendererThread rendererThread = new InyoRendererThread(renderer);
				//progressDisplay.setRendererThread(rendererThread);
				////imagePanel.setImage(image);
				////progressDisplay.repaint();
			}
			break;	
			case RENDERMAN: {
				renderer = new RibRenderer4();
				
				//models, Animator.getInstance().getActiveCamera(), lights);
				File ribFile = new File(settings.export.workingDirectory, frameName + ".rib");
				
//				console.clearText();
				console.append("Rendering " + frameName + " using RenderMAN.\n");
				console.append("Working directory is \"" + settings.export.workingDirectory + "\".\n");
//				console.append("Writing geometry file \"" + ribFile.getName() + "\"...");
				
//				console.append("done.\n\n");
				
				String[] ribCmd = { settings.export.renderman.executable.getPath(), "--verbose=2" };
				String[] ribEnv = settings.export.renderman.environmentVariables.split(";");
				
				
					File imageFile = new File(settings.export.workingDirectory, frameName + ".tif");
					if (imageFile.exists()) imageFile.delete();
					
					StringBuffer sb = new StringBuffer();
					sb.append("Invoking renderer with:\n");
					for (int i = 0; i < ribCmd.length; sb.append(ribCmd[i++]).append(" "));
					sb.append("\n");
					
					if (ribEnv != null) {
						sb.append("Environment variables:\n");
						for (int i = 0; i < ribEnv.length; sb.append(ribEnv[i++]).append("\n"));
					}
					
					sb.append("\n>>>>>>>>>> Begin of renderer output >>>>>>>>>>\n");
					console.append(sb.toString());
				try {
					process = Runtime.getRuntime().exec(ribCmd, ribEnv, settings.export.workingDirectory);
					console.addInputStream(process.getInputStream());
					console.addInputStream(process.getErrorStream());
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
					((RibRenderer4) renderer).writeToFile(anim.getModels(), anim.getActiveCamera(), anim.getLights(), writer, frameName + ".tif");
					writer.close();
					
					process.waitFor();
					console.waitFor();
					console.append("<<<<<<<<<<  End of renderer output  <<<<<<<<<<\n\n");
					if (settings.export.deletePerFrameFilesAfterRendering) {
						console.append("Deleting \"" + ribFile + "\".\n");
						console.setCaretPosition(console.getDocument().getLength());
						System.out.println("***");
						ribFile.delete();
					}
					
					loadImage(imageFile);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
			break;
			case POVRAY: {
				File povrayFile = new File(settings.export.workingDirectory, frameName + ".pov");
				
//				console.clearText();
				console.append("Rendering " + frameName + " using POV-Ray.\n");
				console.append("Working directory is \"" + settings.export.workingDirectory + "\".\n");
//				console.append("Writing geometry file \"" + povrayFile.getName() + "\"...");
				
//				console.append("done.\n\n");
				
				ArrayList<String> listCmd = new ArrayList<String>();
				listCmd.add(settings.export.povray.executable.getPath());
				if (settings.export.povray.version == PovraySettings.Version.UNIX) {
					listCmd.add("+I-");
				} else {
//					listCmd.add("/RENDER");
					listCmd.add("/NORESTORE");
					listCmd.add("/EXIT");
				}
				listCmd.add("+O" + quote(frameName) + ".png");
				listCmd.add("+W" + settings.export.imageWidth);
				listCmd.add("+H" + settings.export.imageHeight);
				listCmd.add("-D");
				listCmd.add("-P");
				listCmd.add("+FN8");
				switch (settings.export.povray.antialiasingMethod) {
				case OFF:
					listCmd.add("-A");
					break;
				case METHOD_1:
					listCmd.add("+A" + settings.export.povray.antialiasingThreshold);
					listCmd.add("+AM1");
					listCmd.add("+R" + settings.export.povray.antialiasingLevel);
					break;
				case METHOD_2:
					listCmd.add("+A" + settings.export.povray.antialiasingThreshold);
					listCmd.add("+AM2");
					listCmd.add("+R" + settings.export.povray.antialiasingLevel);
					break;
				}
				if (settings.export.povray.antialiasingJitter != 0) {
					listCmd.add("+J" + settings.export.povray.antialiasingJitter);
				} else {
					listCmd.add("-J");
				}
				
				String[] povCmd = listCmd.toArray(new String[0]);
				String[] povEnv = settings.export.povray.environmentVariables.split(";");
				
				StringBuffer sb = new StringBuffer();
				sb.append("Invoking POV-Ray with:\n");
				
				for (int i = 0; i < povCmd.length; sb.append(povCmd[i++]).append(" "));
				sb.append("\n");
				
				if (povEnv != null) {
					sb.append("Environment variables:\n");
					for (int i = 0; i < povEnv.length; sb.append(povEnv[i++]).append("\n"));
				}
				
				sb.append("\n>>>>>>>>>> Begin of POV-Ray output >>>>>>>>>>\n");
				console.append(sb.toString());
				
				//try {
					File imageFile = new File(settings.export.workingDirectory, quote(frameName) + ".png");
					if (imageFile.exists()) imageFile.delete();
					try {
						process = Runtime.getRuntime().exec(povCmd, povEnv, settings.export.workingDirectory);
						console.addInputStream(process.getInputStream());
						console.addInputStream(process.getErrorStream());
						
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
						renderer = new PovrayRenderer3();
						((PovrayRenderer3) renderer).writeFrame(anim.getModels(), anim.getActiveCamera(), anim.getLights(), re.getRenderString("povray", ""), writer);
						writer.close();
						
						process.waitFor();		// wait for process to finish;
						console.waitFor();	// wait for console output;
						console.append("<<<<<<<<<<  End of POV-Ray output  <<<<<<<<<<\n\n");
//						if (settings.export.deletePerFrameFilesAfterRendering) {
//							
//							console.append("Deleting \"" + povrayFile + "\".\n");
//							console.setCaretPosition(console.getDocument().getLength());
//							System.out.println("***");
//							povrayFile.delete();
//						}
						loadImage(imageFile);
					} catch (Exception e) {
						e.printStackTrace();
					}
					//RendererThread rendererThread = new RendererThread(pov);
					//progressDisplay.setRendererThread(rendererThread);
					
					//try {
					//	while (rendererThread.running) Thread.sleep(100);
					//} catch (InterruptedException e) {
					//	e.printStackTrace();
					//}
					//
					//////Process pov = Runtime.getRuntime().exec(povCmd);
					////System.out.println("pov started");
					////
					////BufferedReader br = new BufferedReader(new InputStreamReader(pov.getErrorStream()));
					////String line;
					////while ((line = br.readLine()) != null) {
					////	//System.out.println(line);
					////}
					////
					////pov.waitFor();
					////System.out.println("pov finished");
					//
					//if (settings.bDeleteSources) povrayFile.delete();
					//
					////imageFrame.setTitle(frameName);
					//if (imageFile.exists()) {
					//	//imageFrame.setTitle(frameName);
					//	Image image = ImageIO.read(imageFile);
					//	if (warn && image == null) {
					//		JOptionPane.showMessageDialog(getComponent(), "The image seems to be corrupted", "Can't display image", JOptionPane.WARNING_MESSAGE);
					//	} else {
					//		imagePanel.setImage(image);
					//		progressDisplay.repaint();
					//	}
					//} else JOptionPane.showMessageDialog(getComponent(), "The renderer did not output an image. Check the renderer settings.", "Can't find image", JOptionPane.WARNING_MESSAGE);
					//
				//} catch (Exception exception) {
				//	exception.printStackTrace();
				//}
			}
		}
	}
	
	private String quote(String s) {
		return s.replace(' ', '_');
	}
	
	public synchronized void abort() {
		abort = true;
		if (renderer != null)
			renderer.abort();
		if (process != null) {
			try {
				process.getInputStream().close();
				process.getErrorStream().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			process.destroy();
		}
	}
}
