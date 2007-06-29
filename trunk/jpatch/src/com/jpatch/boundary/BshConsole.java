package com.jpatch.boundary;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;

import bsh.*;
import bsh.util.JConsole;

public class BshConsole implements Runnable {
	
	JConsole console = new JConsole();
	Interpreter interpreter = new Interpreter();
	
	public static void main(String[] args) {
		new BshConsole();
	}
	
	public BshConsole() {
		
//		interpreter.setOut(System.out);
//		interpreter.setErr(System.err);
//		new Thread(this).start();
//		JConsole console = new JConsole();
//		interpreter.setConsole(console);
//		JFrame f = new JFrame();
//		f.add(new JTextArea());
//		f.setSize(800, 600);
//		f.setVisible(true);
//		new Thread(interpreter).start();
		
//		
		new Thread(this).start();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				JFrame f = new JFrame();
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.add(console);
				f.setSize(800, 600);
				f.setVisible(true);
				
			}
		});
		
	}
	
	public void run() {
		interpreter.setOut(console.getOut());
		interpreter.setErr(console.getErr());
		interpreter.getOut().println("Bsh interpreter ready. Type \\q to quit.");
		String line;
		BufferedReader reader = new BufferedReader(console.getIn());
		int b;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.equals("\\q")) {
					break;
				}
				final String l = line;
				System.out.println(l);
				EventQueue.invokeAndWait(new Runnable() {
					public void run() {
						try {
							interpreter.eval(l);
						} catch (EvalError e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
//				interpreter.getOut().print("\n>");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		interpreter.getOut().print("Bsh interpreter stopped.");
	}
}
