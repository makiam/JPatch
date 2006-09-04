package test;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import jpatch.boundary.*;
import jpatch.boundary.newtools.*;
import jpatch.entity.*;

public class GlTest {

	public static Model model = new Model();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("OpenGL Test");
		Iterable<Model> models = new Iterable<Model>() {
			public Iterator<Model> iterator() {
				return new Iterator<Model>() {
					private boolean active = true;
					public boolean hasNext() {
						return active;
					}
					public Model next() {
						if (active) {
							active = false;
							return model;
						}
						return null;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
		ControlPoint cp0 = new ControlPoint(model);
		ControlPoint cp1 = new ControlPoint(model);
		ControlPoint cp2 = new ControlPoint(model);
		ControlPoint cp3 = new ControlPoint(model);
		cp0.position.set(-100, 0, 0);
		cp1.position.set( 0, 100, 0);
		cp2.position.set( 100, 0, 0);
		cp3.position.set( 0,-100, 0);
		cp0.setLoop(true);
		cp0.setNext(cp1);
		cp1.setNext(cp2);
		cp2.setNext(cp3);
		cp3.setNext(cp0);
		cp0.setPrev(cp3);
		cp1.setPrev(cp0);
		cp2.setPrev(cp1);
		cp3.setPrev(cp2);
		model.addCurve(cp0);
		model.initControlPoints();
		Viewport viewport = new ViewportGl(0, Viewport.View.FRONT, models);
		frame.setLayout(new BorderLayout());
		frame.add(viewport.getComponent(), BorderLayout.CENTER);
		frame.add(new ViewportAttributeEditor(viewport), BorderLayout.WEST);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		model.xml(System.out, "");
		
		JPatchTool tool = new AddCurveTool();
		tool.registerListeners(new Viewport[] { viewport } );
	}

}
