package jpatch.boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * A JTextArea that serves as an output-console. It only reads <em>lines</em> from the InputStreams!
 * @author sascha
 */
public class Console extends JTextArea {

	/** autoscroll flag */
	private boolean scroll = true;
	/** a list of all ReaderThreads */
	private List<Thread> threads = new ArrayList<Thread>();
	
	/**
	 * Constructor
	 */
	public Console() {
		/* make uneditable, set colors and font */
		setEditable(false);
		setBackground(Color.WHITE);
		setForeground(Color.BLACK);
		setFont(new Font("monospaced", Font.PLAIN, 12));
		
		/* Add a mouselistener to show a popup menu */
		addMouseListener(new MouseAdapter() {
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
			
			/**
			 * Creates a popup menu with two MenuItems:
			 * One to toggle the autoscroll flag, the other one
			 * to clear the console.
			 * @param e the MouseEvent
			 */
			private void showPopup(MouseEvent e) {
				JPopupMenu popup = new JPopupMenu();
				JCheckBoxMenuItem miScroll = new JCheckBoxMenuItem("autoscroll", isAutoScroll());
				JMenuItem miClear = new JMenuItem("clear console");
				miScroll.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						toggleAutoScroll();
					}
				});
				miClear.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						clearText();
					}
				});
				popup.add(miScroll);
				popup.add(new JSeparator());
				popup.add(miClear);
				popup.show(Console.this, e.getX(), e.getY());
			}
		});
		
		
	}
	
	/**
	 * Add an InputStream. The contend of the Stream will be
	 * appended (line by line!) to the console until the stream
	 * is closed.
	 * You can add more that one stream (e.g. the standard and
	 * the error stream).
	 * 
	 * @param in The InputStream to read
	 */
	public void addInputStream(InputStream in) {
		/* Start the reader thread */
		Thread thread = new ReaderThread(in);
		threads.add(thread);
		thread.start();
	}
	
	/**
	 * clears the text
	 */
	public void clearText() {
		setText("");	// This JTextArea method is thread-safe, thus no synchronization is required!
	}
	
	/**
	 * appends text to the console
	 * @param s
	 */
	public void println(String s) {
		append(s + "\n");	// This JTextArea method is thread-safe, thus no synchronization is required!
		/*
		 * If autoscroll is on and we haven't scheduled a caret-update
		 * yet, add caretUpdater runnable to the EventQueue to execute
		 * it on the EventDispatch thread.
		 */
//		if (isAutoScroll() && !updating){
//			updating = true;
//			EventQueue.invokeLater(caretUpdater);
//		}
	}
	
	/**
	 * Synchronized method to query the autoscroll flag
	 */
	private synchronized boolean isAutoScroll() {
		return scroll;
	}
	
	/**
	 * Synchronized method to toggle the autoscroll flag
	 */
	private synchronized void toggleAutoScroll() {
		scroll = !scroll;
	}
	
	/**
	 * Calling this method will block until all InputStreams are closed.
	 */
	public void waitFor() {
		try {
			/* Make a copy of the ReaderThread-List to aviod concurrent modification */
			List<Thread> list;
			synchronized(threads) {
				list = new ArrayList<Thread>(threads);
			}
			/* Join each running ReaderThread */
			for (Thread thread : list) {
				thread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class ReaderThread extends Thread {
		/** A BufferedReader to read lines from the InputStream */
		private BufferedReader reader;
		
		/** Constructor */
		private ReaderThread(InputStream in) {
			/* create a BufferedReader to read lines from the InputStream */
			reader = new BufferedReader(new InputStreamReader(in));
		}
		
		/** A flag telling if we've scheduled a caret update */
		private volatile boolean updating;	// declared volatile because it's accessed by different threads
		
		/** A runnable to update the caret position */
		private Runnable caretUpdater = new Runnable() {
			public void run() {
				updating = false;
				setCaretPosition(getDocument().getLength());
			}
		};
		
		/**
		 * The run method starts reading lines from the InputStream
		 * and appending them to the JTextField's document.
		 */
		@Override
		public void run() {
			try {
				/* Read lines from the stream */
				String line;
				while ((line = reader.readLine()) != null) {
					append(line + "\n");	// This JTextArea method is thread-safe, thus no synchronization is required!
					/*
					 * If autoscroll is on and we haven't scheduled a caret-update
					 * yet, add caretUpdater runnable to the EventQueue to execute
					 * it on the EventDispatch thread.
					 */
					if (isAutoScroll() && !updating){
						updating = true;
						EventQueue.invokeLater(caretUpdater);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			/* Remove this thread from the list of ReaderThreads. */
			synchronized(threads) {
				threads.remove(this);
			}
		}
	}
}
